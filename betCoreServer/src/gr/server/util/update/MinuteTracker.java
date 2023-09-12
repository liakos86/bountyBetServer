//package gr.server.util.update;
//
//import java.util.HashSet;
//import java.util.Set;
//
//import gr.server.data.api.model.events.MatchEvent;
//
//public class MinuteTracker {
//	
//	
//	private static Set<MatchEvent> EVENTS ;
//	
//	public MinuteTracker() {
//		EVENTS = new HashSet<>(); 
//	}
//	
//	public void track(MatchEvent event) {
//		event.calculateLiveMinute();
//		EVENTS.add(event);
//	}
//	
//	public void discard(MatchEvent event) {
//		System.out.println("**************** REMOVING TRACK " + event.getId() + " *****************");
//		EVENTS.remove(event);
//	}
//	
//	public void refresh(){
//		EVENTS.forEach(e-> e.calculateLiveMinute());
//	}
//
//}
