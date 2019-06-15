package gr.server.data.api.model.league;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Country
implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@SerializedName("country_id")
	@Expose
	String countryId;
	
	@SerializedName("country_name")
	@Expose
	String countryName;

	public String getCountryId() {
		return countryId;
	}

	public void setCountryId(String countryId) {
		this.countryId = countryId;
	}

	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}
	
}
