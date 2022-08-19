package gr.server.data.constants;

public interface Fields {

	/**
	 * Every mongo object holds an auto generated mongo id.
	 */
	public static final String MONGO_ID = "_id";
	
	public static final String USERNAME = "username";
	
	/**
	 * Used as foreign key for bets, awards, etc.
	 */
	public static final String FOREIGN_KEY_USER_ID = "userId";
	
	public static final String USER_BALANCE = "balance";

	public static final String BET_BELONGING_MONTH = "belongingMonth";

	public static final String BET_PLACE_DATE = "betPlaceDate";

	public static final String BET_STATUS = "betStatus";

	public static final String BET_AMOUNT = "betAmount";

	public static final String BET_MONGO_USER_ID = "mongoUserId";

	public static final String USER_OVERALL_WON_EVENTS = "overallWonEventsCount";
	
	public static final String USER_OVERALL_LOST_EVENTS = "overallLostEventsCount";
	
	public static final String USER_OVERALL_WON_SLIPS = "overallWonSlipsCount";
	
	public static final String USER_OVERALL_LOST_SLIPS = "overallLostSlipsCount";

	public static final String AWARD_WINNER = "winner";
	public static final String AWARD_MONTH = "awardMonth";
	public static final String AWARD_BALANCE = "winningBalance";

	public static final String USER_AWARDS = "userAwards";

	public static final String USER_AWARDS_IDS = "userAwardsIds";
	

	public static final String MATCH_FULL_DATE = "match_full_date";

	public static final String MATCH_ID = "match_id";

}
