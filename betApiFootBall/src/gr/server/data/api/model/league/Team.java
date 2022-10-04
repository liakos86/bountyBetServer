package gr.server.data.api.model.league;

import java.util.Map;

public class Team {
	
	int id;
	
	int sport_id;
	
	int category_id;
	
	int venue_id;
	
	int manager_id;
	
	String slug;
	
	String name;
	
	String name_short;
	
	String name_full;
	
	String name_code;
	
	boolean has_sub;
	
	boolean has_logo;
	
	String logo;
	
	Map<String, String> name_translations;
	
	String gender;
	
	boolean is_nationality;
	
	String country_code;
	
	String country;
	
	String flag;
	
	String foundation;
	
	String details;

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

	public int getCategory_id() {
		return category_id;
	}

	public void setCategory_id(int category_id) {
		this.category_id = category_id;
	}

	public int getVenue_id() {
		return venue_id;
	}

	public void setVenue_id(int venue_id) {
		this.venue_id = venue_id;
	}

	public int getManager_id() {
		return manager_id;
	}

	public void setManager_id(int manager_id) {
		this.manager_id = manager_id;
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

	public String getName_short() {
		return name_short;
	}

	public void setName_short(String name_short) {
		this.name_short = name_short;
	}

	public String getName_full() {
		return name_full;
	}

	public void setName_full(String name_full) {
		this.name_full = name_full;
	}

	public String getName_code() {
		return name_code;
	}

	public void setName_code(String name_code) {
		this.name_code = name_code;
	}

	public boolean isHas_sub() {
		return has_sub;
	}

	public void setHas_sub(boolean has_sub) {
		this.has_sub = has_sub;
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

	public Map<String, String> getName_translations() {
		return name_translations;
	}

	public void setName_translations(Map<String, String> name_translations) {
		this.name_translations = name_translations;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public boolean isIs_nationality() {
		return is_nationality;
	}

	public void setIs_nationality(boolean is_nationality) {
		this.is_nationality = is_nationality;
	}

	public String getCountry_code() {
		return country_code;
	}

	public void setCountry_code(String country_code) {
		this.country_code = country_code;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String getFoundation() {
		return foundation;
	}

	public void setFoundation(String foundation) {
		this.foundation = foundation;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}
	
}
