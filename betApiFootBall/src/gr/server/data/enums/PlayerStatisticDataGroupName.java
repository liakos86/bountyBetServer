package gr.server.data.enums;

public enum PlayerStatisticDataGroupName {
	
	MATCHES("Matches"), 
	
	ATTACKING("Attacking"), 
	
	PASSES("Passes"), 
	
	DEFENDING("Defending"), 
	
	CARDS("Cards"), 
	
	OTHER("Other (per game)");

	private String type;

	PlayerStatisticDataGroupName(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
	

}
