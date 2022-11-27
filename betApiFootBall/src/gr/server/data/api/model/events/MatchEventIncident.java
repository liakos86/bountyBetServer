package gr.server.data.api.model.events;

public class MatchEventIncident {
	
	int id;
    int event_id ;
    String incident_type; //"Card"
    Integer time ;
    Object time_over ;
    Integer order ;
    String text;
    Integer scoring_team ;
    Integer player_team ;
    Integer home_score ;
    Integer away_score ;
    String card_type ; // "Yellow"
    boolean is_missed ;
    String reason ; //"foul"
    Integer length ;
    Player player ;
    Player player_two_in ;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getEvent_id() {
		return event_id;
	}
	public void setEvent_id(int event_id) {
		this.event_id = event_id;
	}
	public String getIncident_type() {
		return incident_type;
	}
	public void setIncident_type(String incident_type) {
		this.incident_type = incident_type;
	}
	public Integer getTime() {
		return time;
	}
	public void setTime(Integer time) {
		this.time = time;
	}
	public Object getTime_over() {
		return time_over;
	}
	public void setTime_over(Object time_over) {
		this.time_over = time_over;
	}
	public Integer getOrder() {
		return order;
	}
	public void setOrder(Integer order) {
		this.order = order;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public Integer getScoring_team() {
		return scoring_team;
	}
	public void setScoring_team(Integer scoring_team) {
		this.scoring_team = scoring_team;
	}
	public Integer getPlayer_team() {
		return player_team;
	}
	public void setPlayer_team(Integer player_team) {
		this.player_team = player_team;
	}
	public Integer getHome_score() {
		return home_score;
	}
	public void setHome_score(Integer home_score) {
		this.home_score = home_score;
	}
	public Integer getAway_score() {
		return away_score;
	}
	public void setAway_score(Integer away_score) {
		this.away_score = away_score;
	}
	public String getCard_type() {
		return card_type;
	}
	public void setCard_type(String card_type) {
		this.card_type = card_type;
	}
	public boolean isIs_missed() {
		return is_missed;
	}
	public void setIs_missed(boolean is_missed) {
		this.is_missed = is_missed;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public Integer getLength() {
		return length;
	}
	public void setLength(Integer length) {
		this.length = length;
	}
	public Player getPlayer() {
		return player;
	}
	public void setPlayer(Player player) {
		this.player = player;
	}
	public Player getPlayer_two_in() {
		return player_two_in;
	}
	public void setPlayer_two_in(Player player_two_in) {
		this.player_two_in = player_two_in;
	}
    
}
