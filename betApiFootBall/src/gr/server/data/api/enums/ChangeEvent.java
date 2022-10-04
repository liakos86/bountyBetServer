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
	RED_CARD(3);
	
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
