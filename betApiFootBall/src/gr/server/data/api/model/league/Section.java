package gr.server.data.api.model.league;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * It is the parent of a list of {@link League}s. Can be:
 *  - England
 *  - Europe
 *  - World
 *  
 * @author liako
 *
 */
public class Section implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	Map<String, String> name_translations = new HashMap<>();
	
	int id;
	int sport_id;
	String slug;
	String name;
	int priority;
	String flag;
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
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
	}
	public Map<String, String> getName_translations() {
		return name_translations;
	}
	public void setName_translations(Map<String, String> name_translations) {
		this.name_translations = name_translations;
	}
	
	@Override
	public String toString() {
		return "Section: " + this.name + " with priority " + this.priority;
	}
	
}
