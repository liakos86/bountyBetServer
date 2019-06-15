
package gr.server.data.api.model.league;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Standing {

    @SerializedName("country_name")
    @Expose
    private String countryName;
    @SerializedName("league_id")
    @Expose
    private String leagueId;
    @SerializedName("league_name")
    @Expose
    private String leagueName;
    @SerializedName("team_name")
    @Expose
    private String teamName;
    @SerializedName("overall_league_position")
    @Expose
    private String overallLeaguePosition;
    @SerializedName("overall_league_payed")
    @Expose
    private String overallLeaguePayed;
    @SerializedName("overall_league_W")
    @Expose
    private String overallLeagueW;
    @SerializedName("overall_league_D")
    @Expose
    private String overallLeagueD;
    @SerializedName("overall_league_L")
    @Expose
    private String overallLeagueL;
    @SerializedName("overall_league_GF")
    @Expose
    private String overallLeagueGF;
    @SerializedName("overall_league_GA")
    @Expose
    private String overallLeagueGA;
    @SerializedName("overall_league_PTS")
    @Expose
    private String overallLeaguePTS;
    @SerializedName("home_league_position")
    @Expose
    private String homeLeaguePosition;
    @SerializedName("home_league_payed")
    @Expose
    private String homeLeaguePayed;
    @SerializedName("home_league_W")
    @Expose
    private String homeLeagueW;
    @SerializedName("home_league_D")
    @Expose
    private String homeLeagueD;
    @SerializedName("home_league_L")
    @Expose
    private String homeLeagueL;
    @SerializedName("home_league_GF")
    @Expose
    private String homeLeagueGF;
    @SerializedName("home_league_GA")
    @Expose
    private String homeLeagueGA;
    @SerializedName("home_league_PTS")
    @Expose
    private String homeLeaguePTS;
    @SerializedName("away_league_position")
    @Expose
    private String awayLeaguePosition;
    @SerializedName("away_league_payed")
    @Expose
    private String awayLeaguePayed;
    @SerializedName("away_league_W")
    @Expose
    private String awayLeagueW;
    @SerializedName("away_league_D")
    @Expose
    private String awayLeagueD;
    @SerializedName("away_league_L")
    @Expose
    private String awayLeagueL;
    @SerializedName("away_league_GF")
    @Expose
    private String awayLeagueGF;
    @SerializedName("away_league_GA")
    @Expose
    private String awayLeagueGA;
    @SerializedName("away_league_PTS")
    @Expose
    private String awayLeaguePTS;

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getLeagueId() {
        return leagueId;
    }

    public void setLeagueId(String leagueId) {
        this.leagueId = leagueId;
    }

    public String getLeagueName() {
        return leagueName;
    }

    public void setLeagueName(String leagueName) {
        this.leagueName = leagueName;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getOverallLeaguePosition() {
        return overallLeaguePosition;
    }

    public void setOverallLeaguePosition(String overallLeaguePosition) {
        this.overallLeaguePosition = overallLeaguePosition;
    }

    public String getOverallLeaguePayed() {
        return overallLeaguePayed;
    }

    public void setOverallLeaguePayed(String overallLeaguePayed) {
        this.overallLeaguePayed = overallLeaguePayed;
    }

    public String getOverallLeagueW() {
        return overallLeagueW;
    }

    public void setOverallLeagueW(String overallLeagueW) {
        this.overallLeagueW = overallLeagueW;
    }

    public String getOverallLeagueD() {
        return overallLeagueD;
    }

    public void setOverallLeagueD(String overallLeagueD) {
        this.overallLeagueD = overallLeagueD;
    }

    public String getOverallLeagueL() {
        return overallLeagueL;
    }

    public void setOverallLeagueL(String overallLeagueL) {
        this.overallLeagueL = overallLeagueL;
    }

    public String getOverallLeagueGF() {
        return overallLeagueGF;
    }

    public void setOverallLeagueGF(String overallLeagueGF) {
        this.overallLeagueGF = overallLeagueGF;
    }

    public String getOverallLeagueGA() {
        return overallLeagueGA;
    }

    public void setOverallLeagueGA(String overallLeagueGA) {
        this.overallLeagueGA = overallLeagueGA;
    }

    public String getOverallLeaguePTS() {
        return overallLeaguePTS;
    }

    public void setOverallLeaguePTS(String overallLeaguePTS) {
        this.overallLeaguePTS = overallLeaguePTS;
    }

    public String getHomeLeaguePosition() {
        return homeLeaguePosition;
    }

    public void setHomeLeaguePosition(String homeLeaguePosition) {
        this.homeLeaguePosition = homeLeaguePosition;
    }

    public String getHomeLeaguePayed() {
        return homeLeaguePayed;
    }

    public void setHomeLeaguePayed(String homeLeaguePayed) {
        this.homeLeaguePayed = homeLeaguePayed;
    }

    public String getHomeLeagueW() {
        return homeLeagueW;
    }

    public void setHomeLeagueW(String homeLeagueW) {
        this.homeLeagueW = homeLeagueW;
    }

    public String getHomeLeagueD() {
        return homeLeagueD;
    }

    public void setHomeLeagueD(String homeLeagueD) {
        this.homeLeagueD = homeLeagueD;
    }

    public String getHomeLeagueL() {
        return homeLeagueL;
    }

    public void setHomeLeagueL(String homeLeagueL) {
        this.homeLeagueL = homeLeagueL;
    }

    public String getHomeLeagueGF() {
        return homeLeagueGF;
    }

    public void setHomeLeagueGF(String homeLeagueGF) {
        this.homeLeagueGF = homeLeagueGF;
    }

    public String getHomeLeagueGA() {
        return homeLeagueGA;
    }

    public void setHomeLeagueGA(String homeLeagueGA) {
        this.homeLeagueGA = homeLeagueGA;
    }

    public String getHomeLeaguePTS() {
        return homeLeaguePTS;
    }

    public void setHomeLeaguePTS(String homeLeaguePTS) {
        this.homeLeaguePTS = homeLeaguePTS;
    }

    public String getAwayLeaguePosition() {
        return awayLeaguePosition;
    }

    public void setAwayLeaguePosition(String awayLeaguePosition) {
        this.awayLeaguePosition = awayLeaguePosition;
    }

    public String getAwayLeaguePayed() {
        return awayLeaguePayed;
    }

    public void setAwayLeaguePayed(String awayLeaguePayed) {
        this.awayLeaguePayed = awayLeaguePayed;
    }

    public String getAwayLeagueW() {
        return awayLeagueW;
    }

    public void setAwayLeagueW(String awayLeagueW) {
        this.awayLeagueW = awayLeagueW;
    }

    public String getAwayLeagueD() {
        return awayLeagueD;
    }

    public void setAwayLeagueD(String awayLeagueD) {
        this.awayLeagueD = awayLeagueD;
    }

    public String getAwayLeagueL() {
        return awayLeagueL;
    }

    public void setAwayLeagueL(String awayLeagueL) {
        this.awayLeagueL = awayLeagueL;
    }

    public String getAwayLeagueGF() {
        return awayLeagueGF;
    }

    public void setAwayLeagueGF(String awayLeagueGF) {
        this.awayLeagueGF = awayLeagueGF;
    }

    public String getAwayLeagueGA() {
        return awayLeagueGA;
    }

    public void setAwayLeagueGA(String awayLeagueGA) {
        this.awayLeagueGA = awayLeagueGA;
    }

    public String getAwayLeaguePTS() {
        return awayLeaguePTS;
    }

    public void setAwayLeaguePTS(String awayLeaguePTS) {
        this.awayLeaguePTS = awayLeaguePTS;
    }

}
