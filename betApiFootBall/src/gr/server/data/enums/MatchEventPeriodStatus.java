package gr.server.data.enums;

/**
 * Status values found in the field: 'status_more'
 * 
 * Final results only, will be ignored for a game that has not started.
 * 
 * @author liako
 *
 */
public enum MatchEventPeriodStatus {
	
	INPROGRESS_1ST_HALF("1st half"),
	
	INPROGRESS_2ND_HALF("2nd half"),
	
	INPROGRESS_HALFTIME("Halftime"),
	
	INPROGRESS_1ST_EXTRA("1st extra"),
	
	INPROGRESS_2ND_EXTRA("2nd extra"),
	
	GAME_FINISHED("FT"),
	
	FINAL_RESULT_ONLY("FRO"),
	
	EMPTY("-")
	
	;

	private String statusStr;

	MatchEventPeriodStatus(String statusStr) {
		this.statusStr = statusStr;
	}
	
	public static MatchEventPeriodStatus fromStatusMoreText(String str) {
		for (MatchEventPeriodStatus status : MatchEventPeriodStatus.values()) {
			if (status.statusStr.equals(str)) {
				return status;
			}
		}
		
		return null;
	}

	public String getStatusStr() {
		return statusStr;
	}

}
