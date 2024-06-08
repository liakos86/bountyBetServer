package gr.server.data.constants;

import gr.server.common.CommonConstants;

public interface ActiveMqConstants {
	
	String CONNECTION_TCP_URL = "tcp://" +CommonConstants.SERVER_IP+ ":61616";
	
	String TOPIC_SOCCER_EVENTS = "Soccer/MatchEventsLive";
	

}
