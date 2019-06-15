
package gr.server.data.api.model.events;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Away {

    @SerializedName("starting_lineups")
    @Expose
    private List<StartingLineup_> startingLineups = null;
    @SerializedName("substitutes")
    @Expose
    private List<Substitute_> substitutes = null;
    @SerializedName("coach")
    @Expose
    private List<Coach_> coach = null;
    @SerializedName("substitutions")
    @Expose
    private List<Substitution_> substitutions = null;

    public List<StartingLineup_> getStartingLineups() {
        return startingLineups;
    }

    public void setStartingLineups(List<StartingLineup_> startingLineups) {
        this.startingLineups = startingLineups;
    }

    public List<Substitute_> getSubstitutes() {
        return substitutes;
    }

    public void setSubstitutes(List<Substitute_> substitutes) {
        this.substitutes = substitutes;
    }

    public List<Coach_> getCoach() {
        return coach;
    }

    public void setCoach(List<Coach_> coach) {
        this.coach = coach;
    }

    public List<Substitution_> getSubstitutions() {
        return substitutions;
    }

    public void setSubstitutions(List<Substitution_> substitutions) {
        this.substitutions = substitutions;
    }

}
