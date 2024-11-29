package gr.server.common.util;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import gr.server.common.CommonConstants;
import gr.server.common.ServerConstants;

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

	 
	 public static boolean isNextMonth(String dateStr) {

	 
	 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     
     try {
         // Parse the input string to get the Date object
         Date inputDate = sdf.parse(dateStr);

         // Get the current date
         Calendar currentCalendar = Calendar.getInstance();
         
         // Get the current month and year
         int currentMonth = currentCalendar.get(Calendar.MONTH);  // 0-based (January = 0)
         int currentYear = currentCalendar.get(Calendar.YEAR);
         
         // Create a Calendar instance for the input date
         Calendar inputCalendar = Calendar.getInstance();
         inputCalendar.setTime(inputDate);
         
         // Get the month and year of the input date
         int inputMonth = inputCalendar.get(Calendar.MONTH); // 0-based (January = 0)
         int inputYear = inputCalendar.get(Calendar.YEAR);

         // If the input date is in the next month
         if (inputYear == currentYear && inputMonth == currentMonth + 1) {
             return true;
         }
         
         // Handle the case where the input month is in the next year (e.g., December -> January)
         if (inputYear == currentYear + 1 && currentMonth == 11 && inputMonth == 0) {
             return true;
         }

     } catch (Exception e) {
         e.printStackTrace();
     }
     
     return false;
 }
	 
//	public static boolean isLessThanOffsetOfNextMonth() {        
//		Calendar currentDate = Calendar.getInstance();
//        Calendar nextMonth = (Calendar) currentDate.clone();
//        
//        nextMonth.add(Calendar.MONTH, 1);
//        nextMonth.set(Calendar.DAY_OF_MONTH, 1);
//        
//        nextMonth.add(Calendar.DATE, -1);  // go back to the last day of the current month
//        nextMonth.set(Calendar.HOUR_OF_DAY, 23);
//        nextMonth.set(Calendar.MINUTE, 59);
//        nextMonth.set(Calendar.SECOND, 59);
//        nextMonth.set(Calendar.MILLISECOND, 999);
//        
//        long diffInMillis = nextMonth.getTimeInMillis() - currentDate.getTimeInMillis();     
//        int daysUntilNextMonth = (int) (diffInMillis / (24 * 60 * 60 * 1000));
//        
//        return daysUntilNextMonth <= ServerConstants.BET_DAYS_OFFSET;
//	}
 
	 
	 
	public static boolean isFirstDayOfMonth() {
		return Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == 1;
	}

	@SuppressWarnings("deprecation")
	public static String getPastMonthAsString(Integer monthsToSubtract) {
		SimpleDateFormat df = new SimpleDateFormat();
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
	
	public static Map<Integer, Date> getDatesToFetch() {
		
		int daysOffSet = ServerConstants.BET_DAYS_OFFSET;
		
		Map<Integer, Date> datesToFetch = new LinkedHashMap<>();

		for (int i = -1 * daysOffSet; i <= daysOffSet; i++ ) {
			Calendar instance = Calendar.getInstance();
			instance.add(Calendar.DATE, daysOffSet);
			Date date = instance.getTime();
			
			if(datesToFetch.size()<2)//TODO remove later
			datesToFetch.put(i, date);
		}
		
		return datesToFetch;
	}
	
	public static String todayStr() {
		return new SimpleDateFormat(CommonConstants.BASE_DATE_FORMAT).format(new Date());
	}
	
	public static String dateStr(Date date) {
		return new SimpleDateFormat(CommonConstants.BASE_DATE_FORMAT).format(date);
	}

	public static boolean isInNextMonth(String start_at) {
		// TODO Auto-generated method stub
		return false;
	}

}
