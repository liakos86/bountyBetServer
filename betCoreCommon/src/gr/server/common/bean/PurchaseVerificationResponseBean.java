package gr.server.common.bean;

import java.io.Serializable;

public class PurchaseVerificationResponseBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public PurchaseVerificationResponseBean(boolean verified) {
		this.verified = verified;
	}

	private boolean verified = false;

	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}
	
}
