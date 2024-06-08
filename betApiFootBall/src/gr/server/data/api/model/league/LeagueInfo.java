package gr.server.data.api.model.league;

import java.util.Map;

public class LeagueInfo{

	Section section;
	Sport sport;
	
	int id;
	int sport_id;
	int section_id;
	String slug;
	String name;
	Map<String, String> name_translations;
	boolean has_logo;
	String logo;
	
	
    Integer priority;
   

	public LeagueInfo() {
//		this.id = id;
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


	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}
	
	

	

	
//	public List<Season> getSeasons() {
//		if (this.seasons == null) {
//			seasons = new ArrayList<>();
//		}
//		
//		return seasons;
//	}
//
//	public void setSeasons(List<Season> seasons) {
//		this.seasons = seasons;
//	}

	public Sport getSport() {
		return sport;
	}


	public void setSport(Sport sport) {
		this.sport = sport;
	}


	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LeagueInfo)) {
			return false;
		}
		
		LeagueInfo other = (LeagueInfo) obj;
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

//	@Override
//	public int compareTo(League other) {
//		
//		if (other.priority == null) {
//			return -1;
//		}
//		
//		if (this.priority == null) {
//			return 1;
//		}
//		
//		if (other.priority > this.priority) {
//			return 1;
//		}
//		
//		return -1;
//	}

	
//	public LeagueInfo deepCopy() {
//		LeagueInfo copy = new LeagueInfo(id);
//		copy.setSport(sport);
//		copy.setSection(section);
//		copy.setSeasons(seasons);
//		copy.setMatchEvents(new ArrayList<>());
//		return copy;
//	}
    
}
