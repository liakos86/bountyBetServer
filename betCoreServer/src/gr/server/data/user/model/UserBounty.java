package gr.server.data.user.model;

import java.io.Serializable;

/**
 * This class binds a {@link User} with another via their {@link UserPrediction}s.
 * The holder of this instance has unlimited access to the 'bounty' {@link User}'s predictions
 * for a week.
 * 
 * @author liakos
 *
 */
public class UserBounty
implements Serializable{

	private static final long serialVersionUID = 1L;
	
	/**
	 * Unique object id as defined by mongoDb during insert.
	 */
	String mongoId;

	String collectionDate;
	
	String bountyUserMongoId;

	public String getMongoId() {
		return mongoId;
	}

	public void setMongoId(String mongoId) {
		this.mongoId = mongoId;
	}

	public String getCollectionDate() {
		return collectionDate;
	}

	public void setCollectionDate(String collectionDate) {
		this.collectionDate = collectionDate;
	}

	public String getBountyUserMongoId() {
		return bountyUserMongoId;
	}

	public void setBountyUserMongoId(String bountyUserMongoId) {
		this.bountyUserMongoId = bountyUserMongoId;
	}
	
}
