package gr.server.data.user.model.objects;

import java.io.Serializable;

public class UserPurchase implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String purchaseToken;
	private String platform;
	private String mongoUserId;
	private String productId;
	private String status;
	private Integer purchaseMonth;
	private Integer purchaseYear;
	
	

	public String getMongoUserId() {
		return mongoUserId;
	}

	public void setMongoUserId(String mongoUserId) {
		this.mongoUserId = mongoUserId;
	}

	public String getPurchaseToken() {
		return purchaseToken;
	}

	public void setPurchaseToken(String purchaseToken) {
		this.purchaseToken = purchaseToken;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productName) {
		this.productId = productName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getPurchaseMonth() {
		return purchaseMonth;
	}

	public void setPurchaseMonth(Integer purchaseMonth) {
		this.purchaseMonth = purchaseMonth;
	}

	public Integer getPurchaseYear() {
		return purchaseYear;
	}

	public void setPurchaseYear(Integer purchaseYear) {
		this.purchaseYear = purchaseYear;
	}

}
