package gr.server.common;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

import gr.server.data.constants.SportScoreApiConstants;

public class QuickTest { 

	
	@Test
	public void test() throws ParseException {
		Date localTime = new Date();
		 
        DateFormat s = new SimpleDateFormat(SportScoreApiConstants.MATCH_START_TIME_FORMAT);
 
        s.setTimeZone(TimeZone.getTimeZone("GMT"));
 
        System.out.println("local Time:" + localTime);
 
        String curr = s.format(localTime);
		System.out.println("Time IN Gmt : "
                           + curr);
        
        String gameTimeStr = "2022-12-09 09:00:00";
        Date gameTime = s.parse(gameTimeStr);
        System.out.println("Game time "+  gameTime);
        
        long x = localTime.getTime() - gameTime.getTime();
		System.out.println(x/60000);
        
//		System.out.println("Time IN Gmt : "
//                + parse);
//        
//        System.out.println("Time IN Gmt : "
//                +  s.getCalendar().getInstance().getTime());
//        
//        System.out.println("Time IN Gmt : "
//                +  localTime.toGMTString());
	}
	
}