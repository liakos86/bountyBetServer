package gr.server.data.api.model.events;

public class MatchOdd {
	
	Double value;
	
	Integer change;

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	public Integer getChange() {
		return change;
	}

	public void setChange(Integer change) {
		this.change = change;
	}

	@Override
	public String toString() {
		return this.value + " change:"+this.change;
	}

}
