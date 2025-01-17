package gr.server.data.api.enums;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public enum ChangeEvent implements Serializable{

	@SerializedName("0")
	NONE(0),

	@SerializedName("1")
	HOME_GOAL(1),
	
	@SerializedName("2")
	AWAY_GOAL(2),
	
	@SerializedName("3")
	MATCH_START(3),

	@SerializedName("4")
	RED_CARD(4),
	
	@SerializedName("5")
	HALF_TIME(5),
	
	@SerializedName("6")
	MATCH_END(6),
	
	@SerializedName("7")
	SECOND_HALF_START(7),
	
	@SerializedName("8")
	AWAITING_EXTRA_TIME(8),
	
	@SerializedName("9")
	DISALLOWED_GOAL(9);
	
	private static final long serialVersionUID = 1L;

	Integer changeCode;

	ChangeEvent(Integer changeCode) {
		this.changeCode = changeCode;
	}

	public Integer getChangeCode() {
		return changeCode;
	}

//	  static ChangeEvent ofCode(int code){
//	    for (ChangeEvent status in ChangeEvent.values){
//	      if (code == status.changeCode){
//	        return status;
//	      }
//	    }
//
//	    return ChangeEvent.NONE;
//	  }

}
