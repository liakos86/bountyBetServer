package gr.server.transaction.helper;

import gr.server.mongo.application.Mongo;

import com.mongodb.client.ClientSession;

public abstract class TransactionalBlock {
	
	public ClientSession session;
	
	public abstract void begin() throws Exception;
	
	public void execute(){
		try{
			session = Mongo.getMongoClient().startSession();
			session.startTransaction();
			begin();
			
			session.commitTransaction();
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("ROLLING BACK");
			session.abortTransaction();
		}
	}

}
