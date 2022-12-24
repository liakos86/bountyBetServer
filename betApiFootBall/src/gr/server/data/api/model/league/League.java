package gr.server.data.api.model.league;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gr.server.data.api.model.events.MatchEvent;

public class League implements Comparable<League>{
	
	List<Season> seasons = new ArrayList<>();
	
	List<MatchEvent> matchEvents = new ArrayList<>();
	
	Sport sport;
	
	Section section;
	
	int id;
	int sport_id;
	int section_id;
	String slug;
	String name;
	Map<String, String> name_translations;
	boolean has_logo;
	String logo;
	
	String start_date;
    String end_date;
    
    Integer priority;
    Map<String, Object> host;
    
    Integer tennis_points;
    List<Map<String, String>>facts;
     
    Integer most_count;

	public League(int id) {
		this.id = id;
	}

	public Sport getSport() {
		return sport;
	}

	public void setSport(Sport sport) {
		this.sport = sport;
	}

	public Section getSection() {
		return section;
	}

	public void setSection(Section section) {
		this.section = section;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSport_id() {
		return sport_id;
	}

	public void setSport_id(int sport_id) {
		this.sport_id = sport_id;
	}

	public int getSection_id() {
		return section_id;
	}

	public void setSection_id(int section_id) {
		this.section_id = section_id;
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

	public Map<String, String> getName_translations() {
		return name_translations;
	}

	public void setName_translations(Map<String, String> name_translations) {
		this.name_translations = name_translations;
	}

	public boolean isHas_logo() {
		return has_logo;
	}

	public void setHas_logo(boolean has_logo) {
		this.has_logo = has_logo;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getStart_date() {
		return start_date;
	}

	public void setStart_date(String start_date) {
		this.start_date = start_date;
	}

	public String getEnd_date() {
		return end_date;
	}

	public void setEnd_date(String end_date) {
		this.end_date = end_date;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public Map<String, Object> getHost() {
		return host;
	}

	public void setHost(Map<String, Object> host) {
		this.host = host;
	}

	public Integer getTennis_points() {
		return tennis_points;
	}

	public void setTennis_points(Integer tennis_points) {
		this.tennis_points = tennis_points;
	}

	public List<Map<String, String>> getFacts() {
		return facts;
	}

	public void setFacts(List<Map<String, String>> facts) {
		this.facts = facts;
	}

	public Integer getMost_count() {
		return most_count;
	}

	public void setMost_count(Integer most_count) {
		this.most_count = most_count;
	}
	
	public List<MatchEvent> getMatchEvents() {
		return matchEvents;
	}

	public void setMatchEvents(List<MatchEvent> matchEvents) {
		this.matchEvents = matchEvents;
	}
	
	public List<Season> getSeasons() {
		if (this.seasons == null) {
			seasons = new ArrayList<>();
		}
		
		return seasons;
	}

	public void setSeasons(List<Season> seasons) {
		this.seasons = seasons;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof League)) {
			return false;
		}
		
		League other = (League) obj;
		return other.id == this.id;
	}
	
	@Override
	public int hashCode() {
		return this.id * 23;
	}
	
	@Override
	public String toString() {
		if (this.name != null) {
			if (this.section !=null)
				return this.name + " with priority: " + this.priority + " and with Section: "  + this.section;
			else
				return this.name + " with priority: " + this.priority ;
		}
		
		return String.valueOf(this.id);
	}

	@Override
	public int compareTo(League other) {
		if (this.section == null) {
			return 1;
		}
		
		if (other.section == null) {
			return -1;
		}
		
		if (this.section.priority == other.section.priority ) {
			return 0;
		}
		
		return other.section.priority - this.section.priority;
	}

	
	public League deepCopy() {
		League copy = new League(id);
		copy.setSport(sport);
		copy.setSection(section);
		copy.setSeasons(seasons);
		copy.setMatchEvents(new ArrayList<>());
		return copy;
	}
    
}
