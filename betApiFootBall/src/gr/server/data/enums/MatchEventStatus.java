package gr.server.data.enums;

public enum MatchEventStatus {
	
	INPROGRESS("inprogress"),
	
	CANCELLED("cancelled"),
	
	NOTSTARTED("notstarted"),
	
	POSTPONED("postponed"),
	
	FINISHED("finished"),
	
	DELAYED("delayed"),
	
	INTERRUPTED("interrupted"),
	
	SUSPENDED("suspended"),
	
    WILL_CONTINUE ("willcontinue");

	private String statusStr;

	MatchEventStatus(String statusStr) {
		this.statusStr = statusStr;
	}
	
	public static MatchEventStatus fromStatusText(String str) {
		for (MatchEventStatus status : MatchEventStatus.values()) {
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
