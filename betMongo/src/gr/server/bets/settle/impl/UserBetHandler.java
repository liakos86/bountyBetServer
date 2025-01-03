package gr.server.bets.settle.impl;

import java.util.Set;

import org.bson.Document;

import gr.server.handle.def.TaskHandler;
import gr.server.impl.client.MongoClientHelperImpl;

public class UserBetHandler implements TaskHandler<Document>, Runnable {

	public static final int NUM_WORKERS = 4;
	private Set<Document> betDocuments;
	
	public UserBetHandler(Set<Document> betDocuments) {
		this.betDocuments = betDocuments;
	}

	@Override
	public boolean handle(Set<Document> toHandle) {
		
		try {
			return new MongoClientHelperImpl().settleOpenBets(toHandle);
		} catch (Exception e) {
			return false;
		}
		
	}

	@Override
	public void run() {
		
		handle(betDocuments);
		
	}
	
	

}
