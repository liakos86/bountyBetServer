package gr.server.data.api.model.league;

public class TimeDetails {
	
    String prefix;
    Integer initial;
	Integer max;
    Long timestamp;
    Integer extra;
    Long currentPeriodStartTimestamp;
    Integer injuryTime1;
    Integer injuryTime2;
    
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public Integer getInitial() {
		return initial;
	}
	public void setInitial(Integer initial) {
		this.initial = initial;
	}
	public Integer getMax() {
		return max;
	}
	public void setMax(Integer max) {
		this.max = max;
	}
	public Long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
	public Integer getExtra() {
		return extra;
	}
	public void setExtra(Integer extra) {
		this.extra = extra;
	}
	public Long getCurrentPeriodStartTimestamp() {
		return currentPeriodStartTimestamp;
	}
	public void setCurrentPeriodStartTimestamp(Long currentPeriodStartTimestamp) {
		this.currentPeriodStartTimestamp = currentPeriodStartTimestamp;
	}
	public Integer getInjuryTime1() {
		return injuryTime1;
	}
	public void setInjuryTime1(Integer injuryTime1) {
		this.injuryTime1 = injuryTime1;
	}
	public Integer getInjuryTime2() {
		return injuryTime2;
	}
	public void setInjuryTime2(Integer injuryTime2) {
		this.injuryTime2 = injuryTime2;
	}
	
	@Override
	public String toString() {
		
		if  (currentPeriodStartTimestamp == null) {
			return "no timestamp";
		}
		return "tmst:" + currentPeriodStartTimestamp;
	}
	public void deepCopy(TimeDetails incoming) {
		this.currentPeriodStartTimestamp = incoming.currentPeriodStartTimestamp;
		this.extra = incoming.extra;
		this.initial = incoming.initial;
		this.injuryTime1 = incoming.injuryTime1;
		this.injuryTime2 = incoming.injuryTime2;
		this.max = incoming.max;
		this.prefix = incoming.prefix;
		this.timestamp = incoming.timestamp;	
	}
	
}
