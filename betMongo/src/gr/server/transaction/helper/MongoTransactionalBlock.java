package gr.server.transaction.helper;

import com.mongodb.client.ClientSession;

import gr.server.common.logging.CommonLogger;
import gr.server.mongo.util.MongoUtils;

public abstract class MongoTransactionalBlock<T> {
	
	public abstract void begin() throws Exception;

	protected ClientSession session;
	
	protected T result;
	
	protected int retries = 0;
	
	public boolean execute(){
		boolean success = false;
		
		session = MongoUtils.getMongoClient().startSession();
		if (session == null) {
			throw new RuntimeException("MONGO SESSION NULL");
		}
		
		for(int i = 0; i <= retries; i++) {
		
			try{
				session.startTransaction();
				begin();
				session.commitTransaction();
				success = true;
				break;
			}catch(Exception e){
				result = null;
				success = false;
				if (session != null && session.hasActiveTransaction()) {
					session.abortTransaction();
				}
				
				if (i == retries) {
					CommonLogger.logger.error(this.getClass().getCanonicalName() + ": " + e.getStackTrace());
					e.printStackTrace();
//					break;
				}else {
					CommonLogger.logger.warn(this.getClass().getCanonicalName() + " will retry: " + e.getStackTrace());
					System.out.println("RETRYING AFTER ::::: " + e.getClass().getCanonicalName());
				}
				
			}finally{
				if (session != null && (success || i == retries)) {
					session.close();
				}
			}
		}
		
		return success;
	}
	
	public T getResult() {
		return result;
	}
	
	public void setRetries(int retries) {
		this.retries = retries;
	}

}
