package gr.server.data.global.helper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.jms.JMSException;

import gr.server.common.logging.CommonLogger;
//import gr.server.application.BetServerContextListener;
import gr.server.common.util.DateUtils;
import gr.server.data.api.cache.FootballApiCache;
import gr.server.data.api.model.events.MatchEvent;
import gr.server.data.api.model.events.MatchEventIncidentsWithStatistics;
import gr.server.data.api.model.events.Player;
import gr.server.data.api.model.events.PlayerSeasonStatistic;
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
import gr.server.impl.client.MongoClientHelperImpl;
import gr.server.impl.client.SportScoreClient;

public class ApiDataFetchHelper {

	public void fetchLeagueStandings(Set<League> leagues) {
		
		
		for (League leagueEntry : leagues) {

			// TODO reducing calls for testing
				Season currentSeason = null;

				//if (BetServerContextListener.ENV != null && BetServerContextListener.ENV.equals("MOCK")) {
					currentSeason = MockApiClient.getSeasonsFromFile().getData().get(0);
				//} else {
//					currentSeason = SportScoreClient.getCurrentSeason(leagueEntry.getKey());
//				}

				if (currentSeason == null) {
					continue;
				}

				StandingTable standingTable = null;
//				if (BetServerContextListener.ENV != null && BetServerContextListener.ENV.equals("MOCK")) {
					standingTable = MockApiClient.getStandingTableFromFile();
//				} else {
//					standingTable = SportScoreClient.getSeasonStandings(currentSeason.getId());
//				}

					
				for (StandingRow row :  standingTable.getStandings_rows()) {
					if (row.getTeam()==null) {
						System.out.println("ROW TEAM NULL");
					}
					Players players = MockApiClient.getPlayersOfTeamFromFile(row.getTeam().getId());
					for (Player player : players.getData()) {
						
						PlayerSeasonStatistic playerStatisticsFromFile = MockApiClient.getPlayerStatisticsFromFile();
//						PlayerSeasonStatistic playerStatisticsFromFile = SportScoreClient.getPlayerStatisticsForSeason(player.getId(), currentSeason.getId());
						playerStatisticsFromFile.setPlayer(player);
						standingTable.getSeason_player_statistics().add(playerStatisticsFromFile);
					}
				}
					
				currentSeason.setStandingTable(standingTable);

				currentSeason.setLeague(leagueEntry);
				List<Season> leagueSeasons = new ArrayList<>();
				leagueSeasons.add(currentSeason);

				leagueEntry.getSeasonIds().add(currentSeason.getId());

				FootballApiCache.SEASONS_PER_LEAGUE.put(leagueEntry.getId(), leagueSeasons);
			
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
			CommonLogger.logger.error("GETTING EVENTS " + events.size());
			if (events.isEmpty()) {
				continue;
			}
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
//			if (BetServerContextListener.ENV != null && BetServerContextListener.ENV.equals("MOCK")) {
//				throw new RuntimeException("MOCK ENABLED");
//			} else {
				events = SportScoreClient.getEventsByDate(date).getData();
//			}

		} catch (IOException | ParseException | InterruptedException | URISyntaxException e) {
			CommonLogger.logger.error("EVENTS ERROR " + date + "   " +  e);
		}catch (Exception e) {
			CommonLogger.logger.error("ERROR EVENTS " + e);
			
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
			CommonLogger.logger.error(Thread.currentThread().getName() + " LIVE EVENTS ARE " + events.size());
	
			// we mutate the existing score and event status here.
			new LiveUpdatesHelper().updateEventsAndPublishFirebaseTopicMessages(events);
			splitEventsIntoLeaguesAndDays(0, events, true);
		} catch (IOException | ParseException | InterruptedException | URISyntaxException e) {
			CommonLogger.logger.error("LIVE EVENTS ERROR " + e);
//			e.printStackTrace();
			return;
		} catch (JMSException e) {
			CommonLogger.logger.error("JMS ERROR LIVE");
			e.printStackTrace();
		}catch (Exception e) {
			CommonLogger.logger.error("ERROR LIVE " + e.getMessage());
		}

	}


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
		Map<Integer, LeagueWithData> incomingLeagueDataMap = computeIncomingEventsPerLeagueMap(incomingEvents);// new

		List<LeagueWithData> replacementLeaguesWithData = incomingLeagueDataMap.values().stream()
				.collect(Collectors.toList());

		List<LeagueWithData> existingLeaguesOfDayWithData = FootballApiCache.ALL_LEAGUES_WITH_EVENTS_PER_DAY
				.get(position);

		for (LeagueWithData replacementLeagueWithData : replacementLeaguesWithData) {
			// will in happen first call. else it must be a league added later on, not
			// possible.
			if (!existingLeaguesOfDayWithData.contains(replacementLeagueWithData)) {
				existingLeaguesOfDayWithData.add(replacementLeagueWithData);
				replacementLeagueWithData.getMatchEvents().forEach(e -> FootballApiCache.ALL_EVENTS.put(e.getId(), e));
				replacementLeagueWithData.getMatchEvents().forEach(e -> {
					if (MatchEventStatus.INPROGRESS == MatchEventStatus.fromStatusText(e.getStatus())) {

						FootballApiCache.LIVE_EVENTS.put(e.getId(), e);
					}
				});

			} else {
				// league was already there will copy below the existing ones
			}
		}

		for (LeagueWithData existingLeagueWithData : new ArrayList<>(existingLeaguesOfDayWithData)) {

			if (!replacementLeaguesWithData.contains(existingLeagueWithData)) {
				if (!isLiveUpdate) {// unlikely to happen. must be a league that existed but now is gone.
					existingLeaguesOfDayWithData.remove(existingLeagueWithData);
					existingLeagueWithData.getMatchEvents().forEach(e -> {
						FootballApiCache.LIVE_EVENTS.remove(e.getId());
						FootballApiCache.ALL_EVENTS.remove(e.getId());
					});

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
						}
					} else {
						if (FootballApiCache.LIVE_EVENTS.containsKey(oldEvent.getId())) {
							FootballApiCache.LIVE_EVENTS.remove(oldEvent.getId());
						}
					}

				}

				// again seems unlikely, an existing event of a league is now absent
				for (MatchEvent existingEvent : new ArrayList<>(oldMatchEvents)) {

					if (!replacementEvents.contains(existingEvent) && !isLiveUpdate) {

						oldMatchEvents.remove(existingEvent);
					}
				}

			}
		}

	}

	private static Map<Integer, LeagueWithData> computeIncomingEventsPerLeagueMap(List<MatchEvent> incomingEvents) {
		Map<Integer, LeagueWithData> incomingLeagueDataMap = new HashMap<>();
		Set<Team> teams = new HashSet<>();
		for (MatchEvent incomingEvent : incomingEvents) {

			League cachedLeague = FootballApiCache.ALL_LEAGUES.get(incomingEvent.getLeague_id());
			// TODO: can be null because we fetch mock leagues to reduce calls, but we fetch
			// real events
			// Also can be an unsupported league
			if (cachedLeague == null) {
				continue;
			}

			FootballApiCache.checkForCaching(incomingEvent);
			Team home_team = incomingEvent.getHome_team();
			Team away_team = incomingEvent.getAway_team();
			teams.add(away_team);
			teams.add(home_team);
			
			LeagueWithData incomingLeagueData = incomingLeagueDataMap.get(incomingEvent.getLeague_id());

			if (incomingLeagueData == null) {
				incomingLeagueData = new LeagueWithData();
				incomingLeagueData.setLeagueId(cachedLeague.getId());

				incomingLeagueDataMap.put(cachedLeague.getId(), incomingLeagueData);
			}

			incomingLeagueData.getMatchEvents().add(incomingEvent);
		}
		
		new MongoClientHelperImpl().updateTeams(teams);


		return incomingLeagueDataMap;
	}

	public static void fetchSections() {
		try {
			SportScoreClient.getSections().forEach(s -> FootballApiCache.ALL_SECTIONS.put(s.getId(), s));
		} catch (IOException | InterruptedException e) {
			System.out.println("SECTIONS ERROR");
			e.printStackTrace();
		}
	}

	
	
	public void fetchEventStatistics(Set<Integer> eventIds) {
		SportScoreClient sportScoreClient = new SportScoreClient();
		for (Integer eventId : eventIds) {
			MatchEventIncidentsWithStatistics updateLiveStats = sportScoreClient.updateLiveStats(eventId);
			if (FootballApiCache.ALL_MATCH_STATS.containsKey(eventId)) {
				FootballApiCache.ALL_MATCH_STATS.remove(eventId);
			}
			
			
			FootballApiCache.ALL_MATCH_STATS.put(eventId, updateLiveStats);
		}
		
	}
	
}
