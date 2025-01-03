package gr.server.common.bean;

import java.io.Serializable;

public class AuthorizationBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String uniqueDeviceId;

	public String getUniqueDeviceId() {
		return uniqueDeviceId;
	}

	public void setUniqueDeviceId(String uniqueDeviceId) {
		this.uniqueDeviceId = uniqueDeviceId;
	}
	
}
