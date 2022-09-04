package gr.server.data.api.model.league;

import java.io.Serializable;

public class Section implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	int id;
	int sport_id;
	String slug;
	String name;
	int priority;
	String flag;

}
