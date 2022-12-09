package gr.server.data.enums;

public enum MatchEventStatus {
	
	INPROGRESS("inprogress"),
	
	CANCELLED("cancelled"),
	
	NOTSTARTED("notstarted"),
	
	POSTPONED("postponed"),
	
	FINISHED("finished");

	private String statusStr;

	MatchEventStatus(String statusStr) {
		this.statusStr = statusStr;
	}
	
	MatchEventStatus ofStatus(String str) {
		for (MatchEventStatus status : MatchEventStatus.values()) {
			if (status.statusStr.equals(str)) {
				return status;
			}
		}
		
		return null;
	}

}
