package gr.server.data.user.model.objects;

public class NonMongoUserFields {
	
	/**
	 * Current month's position of user.
	 */
	Long position;
	
	/**
	 * Number of won slips.
	 */
	Integer wonSlipsCount;
	
	/**
	 * Number of lost slips.
	 */
	Integer lostSlipsCount;
	
	/**
	 * Number of won events.
	 */
	Integer wonEventsCount;
	
	/**
	 * Number of lost events.
	 */
	Integer lostEventsCount;
	
	/**
	 * Total/overall Number of won slips.
	 */
	Integer overallWonSlipsCount;
	
	/**
	 * Total/overall Number of lost slips.
	 */
	Integer overallLostSlipsCount;
	
	/**
	 * Total/overall Number of won events.
	 */
	Integer overallWonEventsCount;
	
	/**
	 * Total/overall Number of lost events.
	 */
	Integer overallLostEventsCount;
	
	public Integer getOverallWonSlipsCount() {
		return overallWonSlipsCount;
	}

	public void setOverallWonSlipsCount(Integer overallWonSlipsCount) {
		this.overallWonSlipsCount = overallWonSlipsCount;
	}

	public Integer getOverallLostSlipsCount() {
		return overallLostSlipsCount;
	}

	public void setOverallLostSlipsCount(Integer overallLostSlipsCount) {
		this.overallLostSlipsCount = overallLostSlipsCount;
	}

	public Integer getOverallWonEventsCount() {
		return overallWonEventsCount;
	}

	public void setOverallWonEventsCount(Integer overallWonEventsCount) {
		this.overallWonEventsCount = overallWonEventsCount;
	}

	public Integer getOverallLostEventsCount() {
		return overallLostEventsCount;
	}

	public void setOverallLostEventsCount(Integer overallLostEventsCount) {
		this.overallLostEventsCount = overallLostEventsCount;
	}

	public Integer getWonSlipsCount() {
		return wonSlipsCount;
	}

	public void setWonSlipsCount(Integer wonSlipsCount) {
		this.wonSlipsCount = wonSlipsCount;
	}

	public Integer getLostSlipsCount() {
		return lostSlipsCount;
	}

	public void setLostSlipsCount(Integer lostSlipsCount) {
		this.lostSlipsCount = lostSlipsCount;
	}

	public Integer getWonEventsCount() {
		return wonEventsCount;
	}

	public void setWonEventsCount(Integer wonEventsCount) {
		this.wonEventsCount = wonEventsCount;
	}

	public Integer getLostEventsCount() {
		return lostEventsCount;
	}

	public void setLostEventsCount(Integer lostEventsCount) {
		this.lostEventsCount = lostEventsCount;
	}
	
	public Long getPosition() {
		return position;
	}

	public void setPosition(Long position) {
		this.position = position;
	}
	
}
