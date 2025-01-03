package gr.server.data.constants;

public interface MongoFields {
	
	String OR = "$or";
	
	String AND = "$and";

	/**
	 * Every mongo object holds an auto generated mongo id.
	 */
	String MONGO_ID = "_id";
	
	String USERNAME = "username";
	
	String PASSWORD = "password";
	
	String EMAIL = "email";
	
	String VALIDATED = "validated";
	
	String ID = "id";
	
	/**
	 * Used as foreign key for bets, awards, etc.
	 */
//	String FOREIGN_KEY_USER_ID = "userId";
	
	String USER_BALANCE = "balance";

//	String USER_BALANCE_LAST_MONTH = "balanceLastMonth";

//	String BET_BELONGING_MONTH_YEAR = "belongingMonthYear";
	String BET_BELONGING_MONTH = "belongingMonth";

	String BET_BELONGING_YEAR = "belongingYear";

	//String BET_PLACE_DATE = "betPlaceDate";

	String BET_STATUS = "betStatus";

	String BET_AMOUNT = "betAmount";
	
//	String BET_PREDICTIONS_SETTLED = "betPredictionsSettled";

	String MONGO_USER_ID = "mongoUserId";

	String USER_OVERALL_WON_EVENTS = "overallWonEventsCount";
	
	String USER_OVERALL_LOST_EVENTS = "overallLostEventsCount";
	
	String USER_OVERALL_WON_SLIPS = "overallWonSlipsCount";
	
	String USER_OVERALL_LOST_SLIPS = "overallLostSlipsCount";
	
	String USER_MONTHLY_WON_EVENTS = "monthlyWonEventsCount";
	
	String USER_MONTHLY_LOST_EVENTS = "monthlyLostEventsCount";
	
	String USER_MONTHLY_WON_SLIPS = "monthlyWonSlipsCount";
	
	String USER_MONTHLY_LOST_SLIPS = "monthlyLostSlipsCount";
	

	String AWARD_YEAR = "awardYear";
	
	String AWARD_MONTH = "awardMonth";
	
	String AWARD_PLACEMENT = "awardPlacement";

	String USER_BALANCE_MONTH = "month";
	
//	String USER_BALANCE_YEAR = "year";
	
	String AWARD_BALANCE = "winningBalance";

	String USER_AWARDS = "userAwards";

	String USER_AWARDS_IDS = "userAwardsIds";

	String MATCH_FULL_DATE = "match_full_date";
	
	String USER_BET_PREDICTIONS = "predictions";
	
	String EVENT_ID = "eventId";

//	String SECTION_ID = "id";

	String USER_BET_PREDICTION_STATUS = "predictionStatus";
	
	String USER_BET_PREDICTION_SETTLE_STATUS = "predictionSettleStatus";
	
	String USER_BET_PREDICTION_TYPE = "predictionType";

	String USER_BET_PREDICTION_BET_MONGO_ID = "mongoBetId";

	String USER_BET_PLACEMENT_MILLIS = "betPlacementMillis";

	String USER_BET_PREDICTION_ODD_VALUE = "oddValue";

	String USER_BET_PREDICTION_CATEGORY = "predictionCategory";

	String USER_BET_PREDICTION_HOME_TEAM_ID = "homeTeamId";

	String USER_BET_PREDICTION_AWAY_TEAM_ID = "awayTeamId";
	
//	String USER_BET_POSSIBLE_WINNINGS = "possibleWinnings";
	
	String SETTLED_EVENT_BELONGING_MONTH = "eventBelongingMonth";
	String SETTLED_EVENT_BELONGING_DAY = "eventBelongingDay";
	String SETTLED_EVENT_BELONGING_YEAR = "eventBelongingYear";
	
	String TEAM_ID = "teamId";
	
	String TEAM_SPORT_ID = "teamSportId";
	
	String TEAM_LOGO_URL = "teamLogoUrl";
	
	String TEAM_NAME = "teamName";
	
	String TRANSLATIONS = "translations";

	String SPORT_ID = "sportId";


}
