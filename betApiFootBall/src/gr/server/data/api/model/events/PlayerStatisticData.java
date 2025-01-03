package gr.server.data.api.model.events;

import java.io.Serializable;
import java.util.List;

public class PlayerStatisticData implements Serializable{
	
/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

//	Map<String, String> statistics_items = new HashMap<>();
//	List<StatisticsItem> statistics_items ;
	
	String group_name;// "Matches", "Attacking", "Passes", "Defending", "Cards", "Other (per game)"
	Integer matches_total;
	Integer goals;
	Integer assists;

	
//	Integer matches_starting;
//	Integer minutes_per_game;
	
//	Double goals_average;
//	Double total_shots_per_game;
//	Integer big_chance_missed;
	
//	PlayerStatisticData more;
	
//	"goal_conversion": "0%",
//	"penalties": "0/0",
//	"penalties_conversion": "0%",
//	"set_piece_goals": "0/0",
//	"set_pieces_conversion": "0%",
//	"goals_inside_box": "0/0",
//	"goals_outside_box": "0/0",
//	"headed_goals": "0",
//	"left_foot_goals": "0",
//	"right_foot_goals": "0",
//	"penalty_won": "0",
//	"group_name": "Attacking"
	
//	"touches": "1.0",
//	"big_chance_created": "0",
//	"key_passes": "0.0",
//	"accurate_passes_per_game": "0.0 (0%)",
//	"successful_passes_own_half": "0.0 (0%)",
//	"successful_passes_opposition_half": "0.0 (0%)",
//	"successful_long_passes": "0.0 (0%)",
//	"accurate_chipped_passes": "0.0 (0%)",
//	"successful_crosses_and_corners": "0.0 (0%)",
//	"group_name": "Passes"
	
	
//	"interceptions_per_game": "0.0",
//	"tackles_per_game": "0.0",
//	"possession_won_final_third": "0.0",
//	"challenges_lost_per_game": "0.0",
//	"total_clearances_per_game": "0.0",
//	"error_lead_to_a_shot": "0",
//	"error_lead_toa_goal": "0",
//	"penalties_conceded": "0",
//	"group_name": "Defending"
	
//	"successful_dribbles_per_game": "0.0 (0%)",
//	"duels_won_per_game": "0.0 (0%)",
//	"ground_duels_won_per_game": "0.0 (0%)",
//	"aerial_duels_won_per_game": "0.0 (0%)",
//	"possession_lost": "1.0",
//	"fouls": "0.0",
//	"was_fouled": "0.0",
//	"offsides": "0.0",
//	"group_name": "Other (per game)"
	
	Integer yellow_cards;//": "0",
	Integer yellow_red_cards;//": "0",
	Integer red_cards;//": "0",
	//"group_name": "Cards"
//	public Map<String, String> getStatistics_items() {
//		return statistics_items;
//	}
//	public void setStatistics_items(Map<String, String> statistics_items) {
//		this.statistics_items = statistics_items;
//	}
	public Integer getMatches_total() {
		return matches_total;
	}
	public void setMatches_total(Integer matches_total) {
		this.matches_total = matches_total;
	}
//	public Integer getMatches_starting() {
//		return matches_starting;
//	}
//	public void setMatches_starting(Integer matches_starting) {
//		this.matches_starting = matches_starting;
//	}
//	public Integer getMinutes_per_game() {
//		return minutes_per_game;
//	}
//	public void setMinutes_per_game(Integer minutes_per_game) {
//		this.minutes_per_game = minutes_per_game;
//	}
	public String getGroup_name() {
		return group_name;
	}
	public void setGroup_name(String group_name) {
		this.group_name = group_name;
	}
	public Integer getGoals() {
		return goals;
	}
	public void setGoals(Integer goals) {
		this.goals = goals;
	}
//	public Double getGoals_average() {
//		return goals_average;
//	}
//	public void setGoals_average(Double goals_average) {
//		this.goals_average = goals_average;
//	}
//	public Double getTotal_shots_per_game() {
//		return total_shots_per_game;
//	}
//	public void setTotal_shots_per_game(Double total_shots_per_game) {
//		this.total_shots_per_game = total_shots_per_game;
//	}
//	public Integer getBig_chance_missed() {
//		return big_chance_missed;
//	}
//	public void setBig_chance_missed(Integer big_chance_missed) {
//		this.big_chance_missed = big_chance_missed;
//	}
//	public PlayerStatisticData getMore() {
//		return more;
//	}
//	public void setMore(PlayerStatisticData more) {
//		this.more = more;
//	}
	public Integer getAssists() {
		return assists;
	}
	public void setAssists(Integer assists) {
		this.assists = assists;
	}
	public Integer getYellow_cards() {
		return yellow_cards;
	}
	public void setYellow_cards(Integer yellow_cards) {
		this.yellow_cards = yellow_cards;
	}
	public Integer getYellow_red_cards() {
		return yellow_red_cards;
	}
	public void setYellow_red_cards(Integer yellow_red_cards) {
		this.yellow_red_cards = yellow_red_cards;
	}
	public Integer getRed_cards() {
		return red_cards;
	}
	public void setRed_cards(Integer red_cards) {
		this.red_cards = red_cards;
	}
//	public List<StatisticsItem> getStatistics_items() {
//		return statistics_items;
//	}
//	public void setStatistics_items(List<StatisticsItem> statistics_items) {
//		this.statistics_items = statistics_items;
//	}
	
	
	

}
