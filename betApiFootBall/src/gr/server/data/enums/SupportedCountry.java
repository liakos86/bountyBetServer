package gr.server.data.enums;

/**
 * The leagues that we decide to provide coverage for.
 * CountryId is the id that the ApiFootball has assigned to every league.
 * League can be a country or a european league, e.g England or Uefa ChL.
 * 
 * @author liakos
 *
 */
public enum SupportedCountry {
	
//	CHL (163, "Champions League"),
//	EUROPA_LEAGUE(164, "Europa League"),
//	 UEFA (165, "UEFA"),
	 ENGLAND(44,"England");
//	 ITALY(170,"Italy"),
//	 SPAIN(171,"Spain");
	 
	 
	 SupportedCountry(Integer countryId, String countryName){
		 this.countryId = countryId;
		 this.countryName = countryName;
	 }
	 
	 
	 Integer countryId;
	 
	 String countryName;

	public Integer getCountryId() {
		return countryId;
	}

	public String getCountryName() {
		return countryName;
	}
	 
	 }
