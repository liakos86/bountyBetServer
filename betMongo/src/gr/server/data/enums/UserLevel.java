package gr.server.data.enums;

import java.util.ArrayList;
import java.util.List;

public enum UserLevel {
	
	BETTING_VISITOR(0, 0, 0, 0, 0, 0),

	JUNIOR_BETTING_AGENT(1, 1, 0, 0, 0, 0),
	
	BASIC_BETTING_AGENT(2, 1, 0, 1, 0, 0),
	
	UPCOMING_BETTING_STAR(3, 10, 5, 5, 0, 0),
	
	SENIOR_BETTING_AGENT(4, 30, 10, 20, 0, 0),
	
	BETTING_EXPERT(5, 50, 10, 20, 1, 0),
	
	BETTING_MAVEN(6, 50, 10, 20, 1, 1);
	
	private int minPlacedBets;
	private int minWonBets;
	private int minWonPredictions;
	private int bounties;
	private int trophies;
	private int code;
	
	UserLevel(int code, int minPlacedBets, int minWonBets, int minWonPredictions, 
			int bounties, int trophies) {
		this.code = code;
		this.bounties = bounties;
		this.minPlacedBets = minPlacedBets;
		this.minWonBets = minWonBets;
		this.minWonPredictions = minWonPredictions;
		this.trophies = trophies;
	}
	
    public static UserLevel from(int minPlacedBets, int minWonBets, int minWonPredictions, 
			int bounties, int trophies) {
    	for (UserLevel level : sortedValues()) {
    		if (minPlacedBets >= level.minPlacedBets && minWonBets >= level.minWonBets
    				&& minWonPredictions >= level.minWonPredictions && trophies >= level.trophies
    				&& bounties >= level.bounties) {
    			return level;
    		}
    	}
    	
    	return BETTING_VISITOR;
    }
    
    public int getCode() {
		return code;
	}

	static List<UserLevel> sortedValues() {
    	List<UserLevel> sortedValues = new ArrayList<>();
    	sortedValues.add(BETTING_MAVEN);
    	sortedValues.add(BETTING_EXPERT);
    	sortedValues.add(SENIOR_BETTING_AGENT);
    	sortedValues.add(UPCOMING_BETTING_STAR);
    	sortedValues.add(BASIC_BETTING_AGENT);
    	sortedValues.add(JUNIOR_BETTING_AGENT);
    	sortedValues.add(BETTING_VISITOR);
		return sortedValues;
	}
    
}
