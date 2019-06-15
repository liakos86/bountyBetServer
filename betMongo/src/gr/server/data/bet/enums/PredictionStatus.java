package gr.server.data.bet.enums;

public enum PredictionStatus {
	
	PENDING(1, "Pending"),
	
	CORRECT(2, "Correct"),
	
	MISSED(3, "Missed");
	
	private String statusString;
	private int code;

	private PredictionStatus(int code, String statusString) {
		this.setCode(code);
		this.setStatusString(statusString);
	}
	
	public PredictionStatus fromCode(int code) throws Exception {
		for(PredictionStatus status : PredictionStatus.values()){
			if (code == status.getCode()){
				return status;
			}
		}
		throw new Exception("Prediction code: "+code+" not found");
	}

	public String getStatusString() {
		return statusString;
	}

	public void setStatusString(String statusString) {
		this.statusString = statusString;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

}
