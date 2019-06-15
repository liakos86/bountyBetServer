
package gr.server.data.api.model.events;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Home {

    @SerializedName("starting_lineups")
    @Expose
    private List<StartingLineup> startingLineups = null;
    @SerializedName("substitutes")
    @Expose
    private List<Substitute> substitutes = null;
    @SerializedName("coach")
    @Expose
    private List<Coach> coach = null;
    @SerializedName("substitutions")
    @Expose
    private List<Substitution> substitutions = null;

    public List<StartingLineup> getStartingLineups() {
        return startingLineups;
    }

    public void setStartingLineups(List<StartingLineup> startingLineups) {
        this.startingLineups = startingLineups;
    }

    public List<Substitute> getSubstitutes() {
        return substitutes;
    }

    public void setSubstitutes(List<Substitute> substitutes) {
        this.substitutes = substitutes;
    }

    public List<Coach> getCoach() {
        return coach;
    }

    public void setCoach(List<Coach> coach) {
        this.coach = coach;
    }

    public List<Substitution> getSubstitutions() {
        return substitutions;
    }

    public void setSubstitutions(List<Substitution> substitutions) {
        this.substitutions = substitutions;
    }

}
