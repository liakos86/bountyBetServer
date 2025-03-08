package gr.server.application;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import gr.server.common.logging.CommonLogger;
import gr.server.data.api.cache.FootballApiCache;
import gr.server.data.api.model.league.League;
import gr.server.data.api.model.league.Section;
import gr.server.data.api.model.league.Team;
import gr.server.data.live.helper.FireBaseConnectionHelper;
import gr.server.impl.client.MongoClientHelperImpl;
import gr.server.impl.client.SportScoreClient;
import gr.server.mongo.util.MongoUtils;
import gr.server.transaction.helper.ExecutorsBetHelper;
import gr.server.transaction.helper.MongoTransactionalBlock;

@WebListener
public class BetServerContextListener implements ServletContextListener {
	
	//public static String ENV = System.getProperty("gr.server.environment");

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		System.out.println("SERVER SHUTTING DOWN");
//		RestApplication.disconnectActiveMq();
		
		SportScoreClient.shutDown();

		MongoUtils.getMongoClient().close();
	}

	/**
	 * First we connect to the local active mq in order to produce live topic messages later.
	 * Secondly we get all the available sections. (e.g. World, Africa, Greece etc.
	 * Then we get all the leagues that belong to the sections.
	 * Afterwards we retrieve all the REDIS cached matches for today +/- 3 days.
	 * Then, if we are missing any date from above, we retrieve all the matches from SPORTSCORE api.
	 * After that we retrieve the current live matches once.
	 * Finally we open a persistent web socket connection with the api server.
	 * Any live match events will be published to the live events ACTIVE MQ topic.
	 */
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		CommonLogger.logger.error("context init...");
		CommonLogger.logger.info("context init...");
		
		FireBaseConnectionHelper.connectFirebase();
		CommonLogger.logger.error("firebase init...");
		fetchSectionsAndLeaguesAndTeamsFromDbIntoCache();
		CommonLogger.logger.error("sections leagues init...");
		ExecutorsBetHelper betHelper = new ExecutorsBetHelper();
		betHelper.scheduleSections();
		CommonLogger.logger.error("sched section init...");
		betHelper.scheduleLeagues();
		CommonLogger.logger.error("sched leagues init...");
		betHelper.scheduleEvents();
		CommonLogger.logger.error("sched events init...");
		betHelper.scheduleLiveEvents();
		CommonLogger.logger.error("sched live init...");
		betHelper.scheduleSettlePredictions();
		CommonLogger.logger.error("settle pred init...");
		betHelper.scheduleSettleWithdrawnPredictions();
		CommonLogger.logger.error("settle w pred init...");
		betHelper.scheduleSettleDelayedPredictions();
		CommonLogger.logger.error("settle del pred init...");
		betHelper.scheduleSettleBets();
		CommonLogger.logger.error("settle bet init...");
		betHelper.scheduleMonthWinnerCheck();
		CommonLogger.logger.error("winner init...");
		betHelper.scheduleFetchLeaderBoard(60, 3*60, TimeUnit.SECONDS);
		CommonLogger.logger.error("lead init...");
		betHelper.scheduleFetchStandings();
		CommonLogger.logger.error("stand init...");
		betHelper.scheduleFetchStatistics();
		CommonLogger.logger.error("stats init...");
	}

	private void fetchSectionsAndLeaguesAndTeamsFromDbIntoCache() {
		
		new MongoTransactionalBlock<Void>() {
			
			@Override
			public void begin() throws Exception {
				MongoClientHelperImpl mongoClientHelperImpl = new MongoClientHelperImpl();
				List<Section> sectionsFromDb = mongoClientHelperImpl.getSectionsFromDb(session);
				CommonLogger.logger.error("sections " + sectionsFromDb.size());

				sectionsFromDb.forEach(
						s-> {
							if (FootballApiCache.SUPPORTED_SECTION_IDS.containsKey(s.getId())) {
								FootballApiCache.ALL_SECTIONS.put(s.getId(), s);	
							}
						});
				
				CommonLogger.logger.error("section OK");
				List<League> leaguesFromDb = mongoClientHelperImpl.getLeaguesFromDb(session);
				leaguesFromDb.forEach(
						l->{
							if (FootballApiCache.SUPPORTED_SECTION_IDS.containsKey(l.getSection_id())
									&& FootballApiCache.SUPPORTED_SECTION_IDS.get(l.getSection_id()).contains(l.getId())) {
								FootballApiCache.ALL_LEAGUES.put(l.getId(), l);	
								CommonLogger.logger.error("section OK");
							}
						});
				
				CommonLogger.logger.error("leagues OK");
				List<Team> teamsFromDb = mongoClientHelperImpl.getTeamsFromDb(session);
				teamsFromDb.forEach(l->FootballApiCache.ALL_TEAMS.put(l.getId(), l));

				CommonLogger.logger.error("teams OK");
				
				MongoUtils.DB_DATA_FETCHED = true;
			}
		}.execute();
		
	}

//	private SportScoreWebSocketClient initiateWebSocket() {
//		URI uri = null;
//		try {
//			uri = new URI(SportScoreApiConstants.SOCKET_CONN_URL);
//		} catch (URISyntaxException e) {
//			e.printStackTrace();
//			return null;
//		}
//
//		SportScoreWebSocketClient client = new SportScoreWebSocketClient(uri, new WebSocketMessageHandlerImpl());
//		client.sendMessage(SportScoreApiConstants.SOCKET_BOOTSTRAP_MSG);
//		return client;
//	}

}
