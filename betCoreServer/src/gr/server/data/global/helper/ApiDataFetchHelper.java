package gr.server.data.global.helper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.jms.JMSException;

import gr.server.application.BetServerContextListener;
import gr.server.common.util.DateUtils;
import gr.server.data.api.cache.FootballApiCache;
import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.model.events.Player;
import gr.server.data.api.model.events.PlayerStatistics;
import gr.server.data.api.model.events.Players;
import gr.server.data.api.model.league.League;
import gr.server.data.api.model.league.LeagueWithData;
import gr.server.data.api.model.league.Season;
import gr.server.data.api.model.league.StandingRow;
import gr.server.data.api.model.league.StandingTable;
import gr.server.data.api.model.league.Team;
import gr.server.data.constants.SportScoreApiConstants;
import gr.server.data.enums.MatchEventStatus;
import gr.server.data.live.helper.LiveUpdatesHelper;
import gr.server.impl.client.MockApiClient;
import gr.server.impl.client.SportScoreClient;

public class ApiDataFetchHelper {

	public static void fetchLeagueStandings() {
		for (Entry<Integer, League> leagueEntry : FootballApiCache.ALL_LEAGUES.entrySet()) {

			// TODO reducing calls for testing
			if (FootballApiCache.SPAIN_LA_LIGA == leagueEntry.getKey().intValue()) {
				Season currentSeason = null;

				if (BetServerContextListener.ENV != null && BetServerContextListener.ENV.equals("MOCK")) {
					currentSeason = MockApiClient.getSeasonsFromFile().getData().get(0);
				} else {
					currentSeason = SportScoreClient.getCurrentSeason(leagueEntry.getKey());
				}

				if (currentSeason == null) {
					continue;
				}

				StandingTable standingTable = null;
				if (BetServerContextListener.ENV != null && BetServerContextListener.ENV.equals("MOCK")) {
					standingTable = MockApiClient.getStandingTableFromFile();
				} else {
					standingTable = SportScoreClient.getSeasonStandings(currentSeason.getId());
				}

				currentSeason.setStandingTable(standingTable);

//				LeagueWithData leagueWithData = leagueEntry.getValue();

				currentSeason.setLeague(leagueEntry.getValue());
				List<Season> leagueSeasons = new ArrayList<>();
				leagueSeasons.add(currentSeason);

				leagueEntry.getValue().getSeasonIds().add(currentSeason.getId());

				FootballApiCache.SEASONS_PER_LEAGUE.put(leagueEntry.getKey(), leagueSeasons);
			}
		}

		// TODO: all leagues have the same standings for testing purposes
		List<Season> seasons = FootballApiCache.SEASONS_PER_LEAGUE.get(FootballApiCache.SPAIN_LA_LIGA);
		for (Entry<Integer, League> leagueEntry : FootballApiCache.ALL_LEAGUES.entrySet()) {
			FootballApiCache.SEASONS_PER_LEAGUE.put(leagueEntry.getKey(), seasons);
			leagueEntry.getValue().getSeasonIds().add(seasons.get(0).getId());
		}

	}

	/**
	 * Gets the football events for multiple days.
	 * 
	 */
	public static void fetchEventsIntoLeaguesAndExtractMatchEvents() {
		Map<Integer, Date> datesToFetch = DateUtils.getDatesToFetch();

		for (Entry<Integer, Date> dateEntry : datesToFetch.entrySet()) {
			List<MatchEvent> events = eventsForDate(dateEntry.getValue());

			// TODO: We should get this info from websocket
//				try {
			System.out.println("GETTING EVENTS " + events.size());
//					new LiveUpdatesHelper().updateEventsAndPublishFirebaseTopicMessages(events);
			splitEventsIntoLeaguesAndDays(dateEntry.getKey(), events, false);
//				} catch (JMSException e) {
//					System.out.println("JMS ERRORRRRRRRRR");
//					e.printStackTrace();
//				}; 
		}
		;

	}

	public static List<MatchEvent> eventsForDate(Date date) {
		List<MatchEvent> events = new ArrayList<>();

		try {
			if (BetServerContextListener.ENV != null && BetServerContextListener.ENV.equals("MOCK")) {
				throw new RuntimeException("MOCK ENABLED");
//				events = MockApiClient.getEventsFromFile("events1").getData();
			} else {
				// events = MockApiClient.getEventsFromFile("events1").getData();
				// if (false)
				events = SportScoreClient.getEvents(date).getData();
			}

		} catch (IOException | ParseException | InterruptedException | URISyntaxException e) {
			System.out.println("EVENTS ERROR " + date);
			e.printStackTrace();
		}

		return events;
	}

	/**
	 * Gets the football events . If live events are requested
	 * {@link SportScoreApiConstants#GET_LIVE_EVENTS_BY_SPORT_URL}. If not,
	 * {@link SportScoreApiConstants#GET_EVENTS_BY_SPORT_DATE_URL} is used with
	 * 'today' as param. The latter will contain live events also.
	 * 
	 * First we obtain all the events for today with live=false. Then we obtain the
	 * live events. Every live event that was also retrieved in the first step will
	 * be replaced.
	 * 
	 * @param live
	 */
	public static void fetchLiveEventsIntoLeagues() {
		List<MatchEvent> events = new ArrayList<>();
		try {
			events = SportScoreClient.getLiveEvents().getData();
			System.out.println(Thread.currentThread().getName() + " LLIVE EVENTS ARE " + events.size());
			
			// events.forEach(e -> System.out.println("live status: " + e));
			// we mutate the existing score and event status here.
			new LiveUpdatesHelper().updateEventsAndPublishFirebaseTopicMessages(events);
			splitEventsIntoLeaguesAndDays(0, events, true);
		} catch (IOException | ParseException | InterruptedException | URISyntaxException e) {
			System.out.println("LIVE EVENTS ERROR ");
			e.printStackTrace();
			return;
		} catch (JMSException e) {
			System.out.println("JMS ERROR ");
			e.printStackTrace();
		}

	}

//	private static void splitEventsIntoLiveLeagues(List<MatchEvent> events) {
//		for (MatchEvent matchEvent : events) {
//			
//			League matchLeague = FootballApiCache.ALL_LEAGUES.get(matchEvent.getLeague_id());
//			if (matchLeague == null) {
//				continue;
//			}
//			
//			if (FootballApiCache.ALL_EVENTS.containsKey(matchEvent.getId())) {
//				FootballApiCache.ALL_EVENTS.get(matchEvent.getId()).deepCopy(matchEvent);
//			}else {
//				FootballApiCache.ALL_EVENTS.put(matchEvent.getId(), matchEvent);
//			}
//			
//			MatchEvent cachedEvent = FootballApiCache.ALL_EVENTS.get(matchEvent.getId());
//			
//			if (MatchEventStatus.FINISHED == MatchEventStatus.fromStatusText(cachedEvent.getStatus())
//					&& cachedEvent.getWinner_code() != 0 && !FootballApiCache.FINISHED_EVENTS.contains(cachedEvent)) {
//				try {
//					FootballApiCache.FINISHED_EVENTS.put(cachedEvent);
//					System.out.println("ADDING TO FINISHED " + cachedEvent);
//
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			
//			if (!MatchEventStatus.INPROGRESS.equals(MatchEventStatus.fromStatusText(cachedEvent.getStatus()))) {
//				FootballApiCache.LIVE_EVENTS.remove(cachedEvent.getId());
//				continue;
//			}
//			
//			LeagueWithData leagueWithData = FootballApiCache.ALL_LEAGUES_WITH_EVENTS_PER_DAY.get(0).get(cachedEvent.getId());
//			if (!leagueWithData.getMatchEvents().contains(cachedEvent)) {
//				leagueWithData.getMatchEvents().add(cachedEvent);
//			}
//			
//			MatchEvent liveEventFromCache = FootballApiCache.LIVE_EVENTS.get(cachedEvent.getId());
//			if (liveEventFromCache == null) {
//				FootballApiCache.LIVE_EVENTS.put(cachedEvent.getId(), cachedEvent);
//				continue;
//			}
//		
//			//liveEventFromCache.deepCopy(matchEvent);
//		}		
//	}

//	/**
//	 * Input is a date with all its games.
//	 * We find the league that every game belongs to and add the game to that league's games.
//	 * 
//	 * @param date
//	 * @param incomingEvents
//	 */
//	private static void splitEventsIntoLeaguesAndDays2(Integer position, List<MatchEvent> incomingEvents, boolean isLiveUpdate) {
//		for (MatchEvent incomingEvent : incomingEvents) {
//			League cachedLeague = FootballApiCache.ALL_LEAGUES.get(incomingEvent.getLeague_id());
//			//TODO: can be null because we fetch mock leagues to reduce calls, but we fetch real events
//			if (cachedLeague == null) {
//				continue;
//			}
//			
//			
//			if (FootballApiCache.ALL_EVENTS.containsKey(incomingEvent.getId())) {
//				FootballApiCache.ALL_EVENTS.get(incomingEvent.getId()).deepCopy(incomingEvent);
//			}else {
//				FootballApiCache.ALL_EVENTS.put(incomingEvent.getId(), incomingEvent);
//			}
//			
//			if (MatchEventStatus.FINISHED == MatchEventStatus.fromStatusText(incomingEvent.getStatus())
//					&& incomingEvent.getWinner_code() != 0 && !FootballApiCache.FINISHED_EVENTS.contains(incomingEvent)) {
//				try {
//					System.out.println("ADDING TO FINISHED " + incomingEvent);
//					FootballApiCache.FINISHED_EVENTS.put(incomingEvent);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
//		
//		
//		List<LeagueWithData> existingLeaguesWithData = FootballApiCache.ALL_LEAGUES_WITH_EVENTS_PER_DAY.get(position);
//		for (LeagueWithData replacementLeagueWithData : replacementLeaguesWithData) {
//			if (!existingLeaguesWithData.contains(replacementLeagueWithData)) {
//				existingLeaguesWithData.add(replacementLeagueWithData);
//				replacementLeagueWithData.getMatchEvents().forEach(e -> FootballApiCache.ALL_EVENTS.put(e.getId(), e));
//				replacementLeagueWithData.getMatchEvents().forEach(e -> {
//						if (MatchEventStatus.INPROGRESS == MatchEventStatus.fromStatusText(e.getStatus())) {
//							FootballApiCache.LIVE_EVENTS.put(e.getId(), e);	
//						}
//				});
//			}else {
//				//will copy below the existing ones
//			}
//		}
//		
//		for (LeagueWithData existingLeagueWithData : new ArrayList<>(existingLeaguesWithData)) {
//			if (!isLiveUpdate && !replacementLeaguesWithData.contains(existingLeagueWithData)) {
//				existingLeaguesWithData.remove(existingLeagueWithData);
//				existingLeagueWithData.getMatchEvents().forEach(e -> FootballApiCache.LIVE_EVENTS.remove(e));
//			}else if (replacementLeaguesWithData.contains(existingLeagueWithData)){
//				
//				LeagueWithData replacementLeagueWithData = incomingLeagueDataMap.get(existingLeagueWithData.getLeagueId());
//				List<MatchEvent> replacementEvents = replacementLeagueWithData.getMatchEvents();
//				List<MatchEvent> oldMatchEvents = existingLeagueWithData.getMatchEvents();
//				
//				for (MatchEvent replacement : replacementEvents) {
//					if (!oldMatchEvents.contains(replacement)) {
//						oldMatchEvents.add(replacement);
//					}
//					if (!FootballApiCache.ALL_EVENTS.containsKey(replacement.getId())) {
//						FootballApiCache.ALL_EVENTS.put(replacement.getId(), replacement);
//					}
//					
//					if (oldMatchEvents.contains(replacement)) {
//						oldMatchEvents.stream().filter(e -> e.getId() == replacement.getId()).collect(Collectors.toList()).get(0).deepCopy(replacement);
//					}
//				}
//				
//				for (MatchEvent existingEvent : oldMatchEvents) {
//					if (!replacementEvents.contains(existingEvent) && !isLiveUpdate) {
//						oldMatchEvents.remove(existingEvent);
//					}
//				}
//						
//			}
//		}
//		
//		
//	}

	/**
	 * Input is a date with all its games. We find the league that every game
	 * belongs to and add the game to that league's games.
	 * 
	 * @param date
	 * @param incomingEvents
	 */
	private static void splitEventsIntoLeaguesAndDays(Integer position, List<MatchEvent> incomingEvents,
			boolean isLiveUpdate) {

		// 1. split the list of incoming events into a map of events per league
//		System.out.println(isLiveUpdate + " RECEIVED LIVE:" + incomingEvents.size());
		Map<Integer, LeagueWithData> incomingLeagueDataMap = computeIncomingEventsPerLeagueMap(incomingEvents);// new

//		int filtered = 0;
		List<LeagueWithData> replacementLeaguesWithData = incomingLeagueDataMap.values().stream()
				.collect(Collectors.toList());

//for(LeagueWithData r : replacementLeaguesWithData) {
//	filtered = filtered + r.getMatchEvents().size();
//}

//		System.out.println(isLiveUpdate + "FILTERED LIVE:" + filtered);

		List<LeagueWithData> existingLeaguesOfDayWithData = FootballApiCache.ALL_LEAGUES_WITH_EVENTS_PER_DAY
				.get(position);

		for (LeagueWithData replacementLeagueWithData : replacementLeaguesWithData) {
//			System.out.println("Checking " + replacementLeagueWithData.getLeagueId());	
			// will in happen first call. else it must be a league added later on, not
			// possible.
			if (!existingLeaguesOfDayWithData.contains(replacementLeagueWithData)) {
				existingLeaguesOfDayWithData.add(replacementLeagueWithData);
				replacementLeagueWithData.getMatchEvents().forEach(e -> FootballApiCache.ALL_EVENTS.put(e.getId(), e));
				replacementLeagueWithData.getMatchEvents().forEach(e -> {
					if (MatchEventStatus.INPROGRESS == MatchEventStatus.fromStatusText(e.getStatus())) {
//							if (FootballApiCache.LIVE_EVENTS.containsKey(e.getId())) {
//								throw new RuntimeException("LIVE EXISTING????????????????????");
//
//							}

						FootballApiCache.LIVE_EVENTS.put(e.getId(), e);
					}
				});

//				System.out.println("FROM LIVE " + isLiveUpdate + " IS ADDING A NEW LEAGUE "
//						+ FootballApiCache.ALL_LEAGUES.get(replacementLeagueWithData.getLeagueId()).getName());

			} else {
//				System.out.println("FOUND " + replacementLeagueWithData.getLeagueId());
				// league was already there will copy below the existing ones
			}
		}

		for (LeagueWithData existingLeagueWithData : new ArrayList<>(existingLeaguesOfDayWithData)) {

			// System.out.println("Cheking in existing " +
			// existingLeagueWithData.getLeagueId());
			if (!replacementLeaguesWithData.contains(existingLeagueWithData)) {
				if (!isLiveUpdate) {// unlikely to happen. must be a league that existed but now is gone.
					existingLeaguesOfDayWithData.remove(existingLeagueWithData);
					existingLeagueWithData.getMatchEvents().forEach(e -> {
						FootballApiCache.LIVE_EVENTS.remove(e.getId());
						FootballApiCache.ALL_EVENTS.remove(e.getId());
					});

//					System.out.println("FROM LIVE " + isLiveUpdate + " IS REMOVED LEAGUE "
//							+ FootballApiCache.ALL_LEAGUES.get(existingLeagueWithData.getLeagueId()).getName());

				}
			} else { // league exists in both old and incoming

				LeagueWithData replacementLeagueWithData = incomingLeagueDataMap
						.get(existingLeagueWithData.getLeagueId());
				List<MatchEvent> replacementEvents = replacementLeagueWithData.getMatchEvents();

				List<MatchEvent> oldMatchEvents = existingLeagueWithData.getMatchEvents();

				for (MatchEvent replacement : replacementEvents) {

					MatchEvent oldEvent = null;

					if (!oldMatchEvents.contains(replacement)) {
						oldMatchEvents.add(replacement);
						oldEvent = replacement;
					} else {
						oldEvent = oldMatchEvents.stream().filter(e -> e.getId().equals(replacement.getId()))
								.collect(Collectors.toList()).get(0);
						oldEvent.deepCopy(replacement);
					}

					if (!FootballApiCache.ALL_EVENTS.containsKey(oldEvent.getId())) {
						FootballApiCache.ALL_EVENTS.put(oldEvent.getId(), oldEvent);
					}

					if (MatchEventStatus.INPROGRESS == MatchEventStatus.fromStatusText(oldEvent.getStatus())) {
						if (!FootballApiCache.LIVE_EVENTS.containsKey(oldEvent.getId())) {
							FootballApiCache.LIVE_EVENTS.put(oldEvent.getId(), oldEvent);
//							System.out.println("ADDING LIVE NEW " + oldEvent.getHome_team().getName());
						}
					} else {
						if (FootballApiCache.LIVE_EVENTS.containsKey(oldEvent.getId())) {
							FootballApiCache.LIVE_EVENTS.remove(oldEvent.getId());
//							System.out.println("REMOVING OLD LIVE " + oldEvent.getHome_team().getName());
						}
					}

				}

				// again seems unlikely, an existing event of a league is now absent
				for (MatchEvent existingEvent : new ArrayList<>(oldMatchEvents)) {

					if (!replacementEvents.contains(existingEvent) && !isLiveUpdate) {

//						System.out.println("REMOVING OLD EVENT " + existingEvent.getHome_team().getName());

						oldMatchEvents.remove(existingEvent);
					}
				}

			}
		}

	}

	private static Map<Integer, LeagueWithData> computeIncomingEventsPerLeagueMap(List<MatchEvent> incomingEvents) {
		Map<Integer, LeagueWithData> incomingLeagueDataMap = new HashMap<>();
		for (MatchEvent incomingEvent : incomingEvents) {

			League cachedLeague = FootballApiCache.ALL_LEAGUES.get(incomingEvent.getLeague_id());
			// TODO: can be null because we fetch mock leagues to reduce calls, but we fetch
			// real events
			// Also can be an unsupported league
			if (cachedLeague == null) {
//				System.out.println("IGNORING LIVE:" + incomingEvent.getHome_team().getName());
				continue;
			}

			if (MatchEventStatus.FINISHED == MatchEventStatus.fromStatusText(incomingEvent.getStatus())
					&& incomingEvent.getWinner_code() != 0
					&& !FootballApiCache.FINISHED_EVENTS.contains(incomingEvent)) {
				try {
					// System.out.println("ADDING TO FINISHED " + incomingEvent);
					FootballApiCache.FINISHED_EVENTS.put(incomingEvent);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			LeagueWithData incomingLeagueData = incomingLeagueDataMap.get(incomingEvent.getLeague_id());

			if (incomingLeagueData == null) {
				incomingLeagueData = new LeagueWithData();
				incomingLeagueData.setLeagueId(cachedLeague.getId());

				incomingLeagueDataMap.put(cachedLeague.getId(), incomingLeagueData);
			}

			incomingLeagueData.getMatchEvents().add(incomingEvent);
		}

		return incomingLeagueDataMap;
	}

	public static void fetchSections() {
		try {
			SportScoreClient.getSections().forEach(s -> FootballApiCache.ALL_SECTIONS.put(s.getId(), s));
		} catch (IOException e) {
			System.out.println("SECTIONS ERROR");
			e.printStackTrace();
		}
	}

	public static void fetchPlayerStatistics() {// 31730 laliga2024 31497

		Season season = FootballApiCache.SEASONS_PER_LEAGUE.get(FootballApiCache.SPAIN_LA_LIGA).get(0);

		// for (Entry<Integer, Season> seasonStandingEntry :
		// FootballApiCache.STANDINGS.entrySet()) {
		// Season season = seasonStandingEntry.getValue();
		StandingTable standingTable = season.getStandingTable();
		Map<Player, PlayerStatistics> season_player_statistics = standingTable.getSeason_player_statistics();
		int rows = 0;
		for (StandingRow standingRow : standingTable.getStandings_rows()) {
			Team team = standingRow.getTeam();
			Players playersOfTeam = SportScoreClient.getPlayersByTeamId(team.getId());
			int i = 0;
			for (Player player : playersOfTeam.getData()) {
				PlayerStatistics playerStatistics = SportScoreClient.getPlayerStatistics(player.getId());
				season_player_statistics.put(player, playerStatistics);

				if (i > 1) {
					break;
				}
				++i;
				// TODO remove when ready
			}

			if (rows > 4) {
				break;
			}
		}
		// }

	}

}
