package gr.server.util;


import gr.server.data.constants.ApiFootBallConstants;
import gr.server.data.constants.ServerConstants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
	
    private final static int MIDNIGHT = 0;
    private final static int ONE_MINUTE = 1;
	
	 @SuppressWarnings("deprecation")
	public static Date getTomorrowMidnight(){
	        Date date2am = new Date(); 
	           date2am.setHours(MIDNIGHT); 
	           date2am.setMinutes(ONE_MINUTE); 
	           return date2am;
	      }

	public static boolean isFirstDayOfMonth() {
		return Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == 1;
	}

	@SuppressWarnings("deprecation")
	public static String getPastMonthAsString(Integer monthsToSubtract) {
		SimpleDateFormat df = new SimpleDateFormat(ApiFootBallConstants.AWARD_DATE_FORMAT);
		Date date = new Date();
		date.setMonth(date.getMonth() - monthsToSubtract);
		String format = df.format(date);
		return format;
	}

	public static Date getStaleEventsDate() {
		Calendar instance = Calendar.getInstance();
		instance.add(Calendar.DATE, -5);
		return instance.getTime();
	}

	public static Date getBountiesExpirationDate() {
		Calendar instance = Calendar.getInstance();
		instance.add(Calendar.DATE, -8);
		return instance.getTime();
	}
	
	public static String todayStr() {
		return new SimpleDateFormat(ServerConstants.BASE_DATE_FORMAT).format(new Date());
	}
	
	public static String dateStr(Date date) {
		return new SimpleDateFormat(ServerConstants.BASE_DATE_FORMAT).format(date);
	}

}
