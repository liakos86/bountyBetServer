
package gr.server.data.api.model.h2h;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class H2h {

    @SerializedName("firstTeam_VS_secondTeam")
    @Expose
    private List<FirstTeamVSSecondTeam> firstTeamVSSecondTeam = null;
    @SerializedName("firstTeam_lastResults")
    @Expose
    private List<FirstTeamLastResult> firstTeamLastResults = null;
    @SerializedName("secondTeam_lastResults")
    @Expose
    private List<SecondTeamLastResult> secondTeamLastResults = null;

    public List<FirstTeamVSSecondTeam> getFirstTeamVSSecondTeam() {
        return firstTeamVSSecondTeam;
    }

    public void setFirstTeamVSSecondTeam(List<FirstTeamVSSecondTeam> firstTeamVSSecondTeam) {
        this.firstTeamVSSecondTeam = firstTeamVSSecondTeam;
    }

    public List<FirstTeamLastResult> getFirstTeamLastResults() {
        return firstTeamLastResults;
    }

    public void setFirstTeamLastResults(List<FirstTeamLastResult> firstTeamLastResults) {
        this.firstTeamLastResults = firstTeamLastResults;
    }

    public List<SecondTeamLastResult> getSecondTeamLastResults() {
        return secondTeamLastResults;
    }

    public void setSecondTeamLastResults(List<SecondTeamLastResult> secondTeamLastResults) {
        this.secondTeamLastResults = secondTeamLastResults;
    }

}
