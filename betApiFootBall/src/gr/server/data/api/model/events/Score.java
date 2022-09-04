package gr.server.data.api.model.events;

public class Score {
	
	int current;
	
	int display;
	
	int period_1;
	
	int period_2;
	
	int normal_time;

	public int getCurrent() {
		return current;
	}

	public void setCurrent(int current) {
		this.current = current;
	}

	public int getDisplay() {
		return display;
	}

	public void setDisplay(int display) {
		this.display = display;
	}

	public int getPeriod_1() {
		return period_1;
	}

	public void setPeriod_1(int period_1) {
		this.period_1 = period_1;
	}

	public int getPeriod_2() {
		return period_2;
	}

	public void setPeriod_2(int period_2) {
		this.period_2 = period_2;
	}

	public int getNormal_time() {
		return normal_time;
	}

	public void setNormal_time(int normal_time) {
		this.normal_time = normal_time;
	}
	
}
