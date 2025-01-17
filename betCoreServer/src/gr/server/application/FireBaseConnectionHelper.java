//package gr.server.application;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Map;
//
//import com.google.auth.oauth2.GoogleCredentials;
//import com.google.firebase.FirebaseApp;
//import com.google.firebase.FirebaseOptions;
//import com.google.firebase.messaging.AndroidConfig;
//import com.google.firebase.messaging.AndroidConfig.Priority;
//import com.google.firebase.messaging.FcmOptions;
//import com.google.firebase.messaging.FirebaseMessaging;
//import com.google.firebase.messaging.FirebaseMessagingException;
//import com.google.firebase.messaging.Message;
//import com.google.firebase.messaging.Notification;
//import com.google.gson.Gson;
//
//import gr.server.data.api.model.events.transients.ChangeEventSoccer;
//import gr.server.data.constants.FireBaseConstants;
//
//public class FireBaseConnectionHelper {
//	
//	boolean connected = false;
//
//	public boolean connect() {
//		try {			
//			InputStream serviceAccount = getClass().getClassLoader()
////					.getResourceAsStream("application_default_credentials.json");
//			.getResourceAsStream("bountybet-firebase-8e1e2a10a6cc.json");
//			FirebaseApp app = FirebaseApp.initializeApp(
//					FirebaseOptions.builder()
//					.setCredentials(
//							//GoogleCredentials.getApplicationDefault()
//							GoogleCredentials.fromStream(serviceAccount)
//							)
//							.setProjectId("bountybet-firebase").build());
//			connected = true;
//			return true;
//		} catch (IOException e1) {
//			e1.printStackTrace();
//			connected = false;
//			return false;
//		}catch (Exception e) {
//			e.printStackTrace();
//			connected = false;
//			return false;
//		}
//	}
//
//	public void sendTopicMessage(Map<String, String> messageParams) {
//		if (!connected) { // TODO investigate null
//			connect();
//			if (!connected) {
//				return;
//			}
//		}
//				
////		ChangeEventSoccer changeEventSoccer = new ChangeEventSoccer(messageParams);
////		String json = new Gson().toJson(changeEventSoccer);
//
//		// See documentation on defining a message payload.
//		// Don't define a Notification. If you do so, it will always pop on device.
//		Message message = Message.builder()
////		    .putData("changeEvent", json)
//				.putAllData(messageParams)
//		    .setTopic(FireBaseConstants.TOPIC_SOCCER_EVENTS)
//		   
////		    .setFcmOptions(FcmOptions.builder()
////		    		.setAnalyticsLabel("SoccerTopicAnalyticsLabel")
////		    		.build())
//		    .setAndroidConfig(AndroidConfig.builder()
//                    .setTtl(30*1000) // 30 seconds
//                    .setPriority(Priority.HIGH)
//                    .build())
//		  //  .setNotification(Notification.builder().setTitle("title").build())
//		    .build();
//	
//
//		// Send a message to the devices subscribed to the provided topic.
//		String response;
//		try {
//			response = FirebaseMessaging.getInstance().send(message);
//			System.out.println(FireBaseConstants.TOPIC_SOCCER_EVENTS + " - EventId" + messageParams.get("eventId") + ", Successfully sent message: " + response);
//		} catch (FirebaseMessagingException e) {
//			System.out.println("Could not sent message: " + e.getMessage());
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//}
