package gr.server.transaction.helper;

import com.mongodb.client.ClientSession;

import gr.server.mongo.util.MongoUtils;

public abstract class MongoTransactionalBlock {
	
	protected ClientSession session;
	
	public abstract void begin() throws Exception;
	
	public boolean execute(){
		boolean success = false;
		
		try{
			session = MongoUtils.getMongoClient().startSession();
			session.startTransaction();
			begin();
			
			session.commitTransaction();
			
			success = true;
		}catch(Exception e){
			success = false;
			e.printStackTrace();
			System.out.println("ROLLING BACK " + session);
			session.abortTransaction();
		}finally{
			System.out.println(Thread.currentThread().getName() + " CLOSING " + session);
			if (session.hasActiveTransaction())
			session.close();
		}
		
		return success;
	}

}
