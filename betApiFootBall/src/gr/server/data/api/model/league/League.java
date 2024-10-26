package gr.server.data.api.model.league;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * League can be a nation one, a continental one or even a world one:
 *  - English Premier League
 *  - European Football League
 *  - World Cup Soccer
 * 
 * @author liako
 *
 */
public class League implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	int id;
	int sport_id;
	int section_id;
	String slug;
	String name;
	Map<String, String> name_translations;
	boolean has_logo;
	String logo;
    int priority;
 
	String start_date;
    String end_date;
    
    Map<String, Object> host;
    
    Integer tennis_points;
    Integer most_count;

    List<Integer> seasonIds = new ArrayList<>();
    List<Map<String, String>>facts;
     
    
	public String getStart_date() {
		return start_date;
	}

	public List<Integer> getSeasonIds() {
		return seasonIds;
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

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
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
		
				return this.name + " with priority: " + this.priority + " and id " + id;
		}
		
		return String.valueOf(this.id);
	}
    
}
