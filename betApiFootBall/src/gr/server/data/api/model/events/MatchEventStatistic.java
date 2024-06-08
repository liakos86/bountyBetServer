package gr.server.data.api.model.events;

import java.io.Serializable;

public class MatchEventStatistic implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	int id;
    String period; //"ALL"
    String group; //"tvdata"
    String name; //"Shots", corner_kicks, yellow_cards,
    String home; //"6"
    String away; //"8"
    
    
//    {"data":[{"id":23737536,"event_id":2070730,"period":"all",
//    	"group":"tvdata","name":"corner_kicks","home":"8","away":"6",
//    	"compare_code":1},
//             {"id":23738121,"event_id":2070730,"period":"all",
//    		"group":"tvdata","name":"yellow_cards",
//    		"home":"3","away":"3","compare_code":3}],"meta":null}
    
    int compare_code;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getPeriod() {
		return period;
	}
	public void setPeriod(String period) {
		this.period = period;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getHome() {
		return home;
	}
	public void setHome(String home) {
		this.home = home;
	}
	public String getAway() {
		return away;
	}
	public void setAway(String away) {
		this.away = away;
	}
	public int getCompare_code() {
		return compare_code;
	}
	public void setCompare_code(int compare_code) {
		this.compare_code = compare_code;
	}
    
}
