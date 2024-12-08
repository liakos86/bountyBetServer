package gr.server.data.user.model.objects;

import java.io.Serializable;
import java.util.List;

import gr.server.data.bet.enums.BetStatus;


/**
 * A bet that is placed by a {@link User}.
 * Involves a list of predictions.
 * 
 * @author liakos
 *
 */
public class UserBet implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	String mongoId;

	String mongoUserId;
	
	long betPlaceMillis;
	
	/**
	 * Every bet participates in a monthy contest. 
	 * This is the yyyy/MM representation of the belonging month.
	 */
	Integer belongingMonth;

	Integer belongingYear;
	
	int betStatus;
	
	boolean predictionsSettled = false;
	
	double betAmount;
	
	List<UserPrediction> predictions;
	
//	String betPlaceDate;
	
	public String getMongoId() {
		return mongoId;
	}

	public void setMongoId(String mongoId) {
		this.mongoId = mongoId;
	}

	public String getMongoUserId() {
		return mongoUserId;
	}

	public void setMongoUserId(String mongoUserId) {
		this.mongoUserId = mongoUserId;
	}

//	public String getBetPlaceDate() {
//		return betPlaceDate;
//	}
//
//	public void setBetPlaceDate(String betPlaceDate) {
//		this.betPlaceDate = betPlaceDate;
//	}
	
	public List<UserPrediction> getPredictions() {
		return predictions;
	}

	public void setPredictions(List<UserPrediction> predictions) {
		this.predictions = predictions;
	}

	public double getBetAmount() {
		return betAmount;
	}

	public void setBetAmount(double betAmount) {
		this.betAmount = betAmount;
	}


	public int getBetStatus() {
		return betStatus;
	}

	public void setBetStatus(int betStatus) {
		this.betStatus = betStatus;
	}
	
	public Integer getBelongingMonth() {
		return belongingMonth;
	}

	public void setBelongingMonth(Integer belongingMonth) {
		this.belongingMonth = belongingMonth;
	}

	public long getBetPlaceMillis() {
		return betPlaceMillis;
	}

	public void setBetPlaceMillis(long betPlaceMillis) {
		this.betPlaceMillis = betPlaceMillis;
	}
	
	public Integer getBelongingYear() {
		return belongingYear;
	}

	public void setBelongingYear(Integer belongingYear) {
		this.belongingYear = belongingYear;
	}

	public Double getPossibleEarnings() {
		 Double possibleEarnings = this.getBetAmount();
	      for (UserPrediction userPrediction : this.getPredictions()) {
			possibleEarnings *= userPrediction.getOddValue();
		}
	    return possibleEarnings;
	}

}
