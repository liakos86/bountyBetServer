package gr.server.program;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.model.events.helper.ApiDataFetchHelper;
import gr.server.data.enums.MatchEventStatus;
import gr.server.impl.client.MongoClientHelperImpl;
import gr.server.transaction.helper.MongoTransactionalBlock;

public class SettleProgram {
	
	public static void main(String[] args) {
		
		System.out.println("Settle predictions Working in thread: " + Thread.currentThread().getName());

		List<MatchEvent> todaysMatches = new ArrayList<>();// ApiDataFetchHelper.eventsForDate(new Date());
		
		Set<MatchEvent> todaysFinishedEvents = todaysMatches.stream().filter(
							match -> MatchEventStatus.FINISHED.getStatusStr().equals(match.getStatus()))
					.collect(Collectors.toSet());
		
		new MongoTransactionalBlock() {
			
			@Override
			public void begin() throws Exception {
				new MongoClientHelperImpl().settlePredictions(session, todaysFinishedEvents);
			}
		}.execute();

		
		System.out.println("SETTLE PROG END!!!!!!!!!!!");
	}

}
