package gr.server.bets.settle.impl;

import java.util.Set;

import gr.server.data.api.model.bean.OpenBetDelayedLeagueDatesBean;
import gr.server.handle.def.TaskHandler;
import gr.server.impl.client.MongoClientHelperImpl;

public class UserBetDelayedHandler implements TaskHandler<OpenBetDelayedLeagueDatesBean>, Runnable {

	public static final int NUM_WORKERS = 1;
	private Set<OpenBetDelayedLeagueDatesBean> missingDatesPerLeague;
	
	public UserBetDelayedHandler(Set<OpenBetDelayedLeagueDatesBean> missingDatesPerLeague) {
		this.missingDatesPerLeague = missingDatesPerLeague;
	}

	@Override
	public boolean handle(Set<OpenBetDelayedLeagueDatesBean> toHandle) {
		
		try {
			return new MongoClientHelperImpl().settleDelayedOpenPredictions(toHandle);
		} catch (Exception e) {
			return false;
		}
		
	}

	@Override
	public void run() {
		handle(missingDatesPerLeague);
	}
	
	

}
