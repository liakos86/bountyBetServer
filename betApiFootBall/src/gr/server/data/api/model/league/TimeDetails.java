package gr.server.data.api.model.league;

public class TimeDetails {
	
    String prefix;
    Integer initial;
	Integer max;
    Long timestamp;
    Integer extra;
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
    
    

}
