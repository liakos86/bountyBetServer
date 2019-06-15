
package gr.server.data.api.model.events;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Substitution {

    @SerializedName("lineup_player")
    @Expose
    private String lineupPlayer;
    @SerializedName("lineup_number")
    @Expose
    private String lineupNumber;
    @SerializedName("lineup_position")
    @Expose
    private String lineupPosition;
    @SerializedName("lineup_time")
    @Expose
    private String lineupTime;

    public String getLineupPlayer() {
        return lineupPlayer;
    }

    public void setLineupPlayer(String lineupPlayer) {
        this.lineupPlayer = lineupPlayer;
    }

    public String getLineupNumber() {
        return lineupNumber;
    }

    public void setLineupNumber(String lineupNumber) {
        this.lineupNumber = lineupNumber;
    }

    public String getLineupPosition() {
        return lineupPosition;
    }

    public void setLineupPosition(String lineupPosition) {
        this.lineupPosition = lineupPosition;
    }

    public String getLineupTime() {
        return lineupTime;
    }

    public void setLineupTime(String lineupTime) {
        this.lineupTime = lineupTime;
    }

}
