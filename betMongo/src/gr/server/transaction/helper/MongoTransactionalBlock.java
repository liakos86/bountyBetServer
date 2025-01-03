package gr.server.transaction.helper;

import com.mongodb.client.ClientSession;

import gr.server.mongo.util.MongoUtils;

public abstract class MongoTransactionalBlock<T> {
	
	public abstract void begin() throws Exception;

	protected ClientSession session;
	
	protected T result;
	
	public boolean execute(){
		boolean success = false;
		
		try{
			session = MongoUtils.getMongoClient().startSession();
			
			if (session == null) {
				throw new Exception("MONGO SESSION NULL");
			}
			
			session.startTransaction();
			begin();
			
			session.commitTransaction();
			
			success = true;
		}catch(Exception e){
			success = false;
			e.printStackTrace();
//			System.out.println("ROLLING BACK " + session);
			if (session != null && session.hasActiveTransaction()) {
				session.abortTransaction();
			}
		}finally{
//			System.out.println(Thread.currentThread().getName() + " CLOSING " + session);
			if (session != null) {
				session.close();
			}
		}
		
		return success;
	}
	
	public T getResult() {
		return result;
	}

}
