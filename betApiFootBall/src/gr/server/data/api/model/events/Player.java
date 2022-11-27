package gr.server.data.api.model.events;

import java.util.HashMap;
import java.util.Map;

public class Player {
	
	int id;
	
    int sport_id;
    
    String slug;
    
    String name;
    
    Map <String, String> name_translations = new HashMap<>();
    
    String name_short;
    
    boolean has_photo;
    
    String photo;
    
    String position;
    
    String position_name;

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

	public String getName_short() {
		return name_short;
	}

	public void setName_short(String name_short) {
		this.name_short = name_short;
	}

	public boolean isHas_photo() {
		return has_photo;
	}

	public void setHas_photo(boolean has_photo) {
		this.has_photo = has_photo;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getPosition_name() {
		return position_name;
	}

	public void setPosition_name(String position_name) {
		this.position_name = position_name;
	}
    
}
