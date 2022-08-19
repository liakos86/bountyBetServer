package gr.server.transaction.helper;

import com.mongodb.client.ClientSession;

import gr.server.mongo.util.SyncHelper;

public abstract class TransactionalBlock {
	
	protected ClientSession session;
	
	public abstract void begin() throws Exception;
	
	public void execute(){
		try{
			session = SyncHelper.getMongoClient().startSession();
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
