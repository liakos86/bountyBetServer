package gr.server.transaction.helper;

import com.mongodb.client.ClientSession;

import gr.server.mongo.util.MongoUtils;

public abstract class MongoTransactionalBlock {
	
	protected ClientSession session;
	
	public abstract void begin() throws Exception;
	
	public void execute(){
		try{
			session = MongoUtils.getMongoClient().startSession();
			session.startTransaction();
			begin();
			
			session.commitTransaction();
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("ROLLING BACK " + session);
			session.abortTransaction();
		}finally{
			System.out.println("CLOSING " + session);
			session.close();
		}
	}

}
