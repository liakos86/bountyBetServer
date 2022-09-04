package gr.server.data.api.model.league;

import java.util.Map;

public class League {
	
	int id;
	int sport_id;
	int section_id;
	String slug;
	String name;
	Map<String, String> name_translations;
	boolean has_logo;
	String logo;
	

}
