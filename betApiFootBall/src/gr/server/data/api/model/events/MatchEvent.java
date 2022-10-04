package gr.server.data.api.model.events;

import java.util.Map;

import gr.server.data.api.enums.ChangeEvent;
import gr.server.data.api.model.league.Challenge;
import gr.server.data.api.model.league.League;
import gr.server.data.api.model.league.Season;
import gr.server.data.api.model.league.Section;
import gr.server.data.api.model.league.Team;
import gr.server.data.api.model.league.TimeDetails;

public class MatchEvent {
	
	ChangeEvent changeEvent;
	boolean markedForRemoval;
	
	Object sport; 
	
	Integer id;
	Integer sport_id;
	Integer league_id;
	Integer challenge_id;
	Integer season_id;
	Integer venue_id;
	Integer referee_id;
	
	String slug;
	String name;
	
	String start_at;
	String status;
	String status_more;
	String status_loc;
	
	String status_for_client;
	TimeDetails time_details;
	Object time_live; // can be string or int. e.g. 54 or 'Halhtime'.
	
	Integer home_team_id;
	Integer away_team_id;
	Team home_team;
	Team away_team;
	Integer priority;
	Score home_score;
	Score away_score;
	int winner_code;
	Integer aggregated_winner_code;
	boolean result_only;
	String coverage;
	String ground_type;
	int round_number;
	int series_count;
	int medias_count;
	String status_lineup;
	String first_supply;
	String cards_code;
	String event_data_change;
	String lasted_period;
	int default_period_count;
	Integer attendance;
	Integer cup_match_order;
	Integer cup_match_in_round;
	String periods;
	Map<String, Object> round_info;
	String periods_time;
	MatchOdds main_odds;
	League league; 
	Challenge challenge; 
	Season season; 
	Section section;
	
	public boolean homeGoalScored(Score homeScoreNew) {
		if (this.home_score == null) {
			return false;
		}
		
		return this.home_score.scoreChanged(homeScoreNew);
	}
	
	public boolean awayGoalScored(Score awayScoreNew) {
		if (this.away_score == null) {
			return false;
		}
		
		return this.away_score.scoreChanged(awayScoreNew);
	}
	
	public Object getSport() {
		return sport;
	}
	public void setSport(Object sport) {
		this.sport = sport;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getSport_id() {
		return sport_id;
	}
	public void setSport_id(Integer sport_id) {
		this.sport_id = sport_id;
	}
	public Integer getHome_team_id() {
		return home_team_id;
	}
	public void setHome_team_id(Integer home_team_id) {
		this.home_team_id = home_team_id;
	}
	public Integer getAway_team_id() {
		return away_team_id;
	}
	public void setAway_team_id(Integer away_team_id) {
		this.away_team_id = away_team_id;
	}
	public Integer getLeague_id() {
		return league_id;
	}
	public void setLeague_id(Integer league_id) {
		this.league_id = league_id;
	}
	public Integer getChallenge_id() {
		return challenge_id;
	}
	public void setChallenge_id(Integer challenge_id) {
		this.challenge_id = challenge_id;
	}
	public Integer getSeason_id() {
		return season_id;
	}
	public void setSeason_id(Integer season_id) {
		this.season_id = season_id;
	}
	public Integer getVenue_id() {
		return venue_id;
	}
	public void setVenue_id(Integer venue_id) {
		this.venue_id = venue_id;
	}
	public Integer getReferee_id() {
		return referee_id;
	}
	public void setReferee_id(Integer referee_id) {
		this.referee_id = referee_id;
	}
	public String getSlug() {
		return slug;
	}
	public void setSlug(String slug) {
		this.slug = slug;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getStatus_more() {
		return status_more;
	}
	public void setStatus_more(String status_more) {
		this.status_more = status_more;
	}
	public TimeDetails getTime_details() {
		return time_details;
	}
	public void setTime_details(TimeDetails time_details) {
		this.time_details = time_details;
	}
	public Team getHome_team() {
		return home_team;
	}
	public void setHome_team(Team home_team) {
		this.home_team = home_team;
	}
	public Team getAway_team() {
		return away_team;
	}
	public void setAway_team(Team away_team) {
		this.away_team = away_team;
	}
	public String getStart_at() {
		return start_at;
	}
	public void setStart_at(String start_at) {
		this.start_at = start_at;
	}
	public Integer getPriority() {
		return priority;
	}
	public void setPriority(Integer priority) {
		this.priority = priority;
	}
	public Score getHome_score() {
		return home_score;
	}
	public void setHome_score(Score home_score) {
		this.home_score = home_score;
	}
	public Score getAway_score() {
		return away_score;
	}
	public void setAway_score(Score away_score) {
		this.away_score = away_score;
	}
	public int getWinner_code() {
		return winner_code;
	}
	public void setWinner_code(int winner_code) {
		this.winner_code = winner_code;
	}
	public Integer getAggregated_winner_code() {
		return aggregated_winner_code;
	}
	public void setAggregated_winner_code(Integer aggregated_winner_code) {
		this.aggregated_winner_code = aggregated_winner_code;
	}
	public boolean isResult_only() {
		return result_only;
	}
	public void setResult_only(boolean result_only) {
		this.result_only = result_only;
	}
	public String getCoverage() {
		return coverage;
	}
	public void setCoverage(String coverage) {
		this.coverage = coverage;
	}
	public String getGround_type() {
		return ground_type;
	}
	public void setGround_type(String ground_type) {
		this.ground_type = ground_type;
	}
	public int getRound_number() {
		return round_number;
	}
	public void setRound_number(int round_number) {
		this.round_number = round_number;
	}
	public int getSeries_count() {
		return series_count;
	}
	public void setSeries_count(int series_count) {
		this.series_count = series_count;
	}
	public int getMedias_count() {
		return medias_count;
	}
	public void setMedias_count(int medias_count) {
		this.medias_count = medias_count;
	}
	public String getStatus_lineup() {
		return status_lineup;
	}
	public void setStatus_lineup(String status_lineup) {
		this.status_lineup = status_lineup;
	}
	public String getFirst_supply() {
		return first_supply;
	}
	public void setFirst_supply(String first_supply) {
		this.first_supply = first_supply;
	}
	public String getCards_code() {
		return cards_code;
	}
	public void setCards_code(String cards_code) {
		this.cards_code = cards_code;
	}
	public String getEvent_data_change() {
		return event_data_change;
	}
	public void setEvent_data_change(String event_data_change) {
		this.event_data_change = event_data_change;
	}
	public String getLasted_period() {
		return lasted_period;
	}
	public void setLasted_period(String lasted_period) {
		this.lasted_period = lasted_period;
	}
	public int getDefault_period_count() {
		return default_period_count;
	}
	public void setDefault_period_count(int default_period_count) {
		this.default_period_count = default_period_count;
	}
	public Integer getAttendance() {
		return attendance;
	}
	public void setAttendance(Integer attendance) {
		this.attendance = attendance;
	}
	public Integer getCup_match_order() {
		return cup_match_order;
	}
	public void setCup_match_order(Integer cup_match_order) {
		this.cup_match_order = cup_match_order;
	}
	public Integer getCup_match_in_round() {
		return cup_match_in_round;
	}
	public void setCup_match_in_round(Integer cup_match_in_round) {
		this.cup_match_in_round = cup_match_in_round;
	}
	public String getPeriods() {
		return periods;
	}
	public void setPeriods(String periods) {
		this.periods = periods;
	}
	public Map<String, Object> getRound_info() {
		return round_info;
	}
	public void setRound_info(Map<String, Object> round_info) {
		this.round_info = round_info;
	}
	public String getPeriods_time() {
		return periods_time;
	}
	public void setPeriods_time(String periods_time) {
		this.periods_time = periods_time;
	}
	public MatchOdds getMain_odds() {
		return main_odds;
	}
	public void setMain_odds(MatchOdds main_odds) {
		this.main_odds = main_odds;
	}
	public League getLeague() {
		return league;
	}
	public void setLeague(League league) {
		this.league = league;
	}
	public Challenge getChallenge() {
		return challenge;
	}
	public void setChallenge(Challenge challenge) {
		this.challenge = challenge;
	}
	public Season getSeason() {
		return season;
	}
	public void setSeason(Season season) {
		this.season = season;
	}
	public Section getSection() {
		return section;
	}
	public void setSection(Section section) {
		this.section = section;
	}
	
	public String getStatus_loc() {
		return status_loc;
	}

	public void setStatus_loc(String status_loc) {
		this.status_loc = status_loc;
	}
	
	public Object getTime_live() {
		return time_live;
	}

	public void setTime_live(Object time_live) {
		this.time_live = time_live;
	}

	public String getStatus_for_client() {
		return status_for_client;
	}

	public void setStatus_for_client(String status_for_client) {
		this.status_for_client = status_for_client;
	}

	public ChangeEvent getChangeEvent() {
		return changeEvent;
	}

	public void setChangeEvent(ChangeEvent changeEvent) {
		this.changeEvent = changeEvent;
	}

	public boolean isMarkedForRemoval() {
		return markedForRemoval;
	}

	public void setMarkedForRemoval(boolean markedForRemoval) {
		this.markedForRemoval = markedForRemoval;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MatchEvent)) {
			return false;
		}
		MatchEvent other = (MatchEvent) obj;
		return this.id.equals(other.id);
	}
	
	@Override
	public int hashCode() {
		return this.id * 37;
	}
	
}
