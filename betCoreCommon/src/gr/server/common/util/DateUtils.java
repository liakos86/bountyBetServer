package gr.server.common.util;


import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gr.server.common.CommonConstants;
import gr.server.common.ServerConstants;
import gr.server.common.StringConstants;

public class DateUtils {
	
//    private final static int MIDNIGHT = 0;
//    private final static int ONE_MINUTE = 1;
	
//	 @SuppressWarnings("deprecation")
//	public static Date getTomorrowMidnight(){
//	        Date date2am = new Date(); 
//	           date2am.setHours(MIDNIGHT); 
//	           date2am.setMinutes(ONE_MINUTE); 
//	           return date2am;
//	      }

	 public static long millisSinceStartOfMonth() {
	        ZonedDateTime now = ZonedDateTime.now();
	        ZonedDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
	        return startOfMonth.toInstant().toEpochMilli();
	    }
	
	 
	 public static boolean isNextMonth(String dateStr) {

	 
	 SimpleDateFormat sdf = new SimpleDateFormat(ServerConstants.DATE_WITH_TIME_FORMAT);// "yyyy-MM-dd HH:mm:ss");
     
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

//	@SuppressWarnings("deprecation")
//	public static String getPastMonthAsString(Integer monthsToSubtract) {
//		SimpleDateFormat df = new SimpleDateFormat();
//		Date date = new Date();
//		date.setMonth(date.getMonth() - monthsToSubtract);
//		String format = df.format(date);
//		return format;
//	}

//	public static Date getStaleEventsDate() {
//		Calendar instance = Calendar.getInstance();
//		instance.add(Calendar.DATE, -5);
//		return instance.getTime();
//	}

//	public static Date getBountiesExpirationDate() {
//		Calendar instance = Calendar.getInstance();
//		instance.add(Calendar.DATE, -8);
//		return instance.getTime();
//	}
	
	public static Map<Integer, Date> getDatesToFetch() {
		
		int daysOffSet = ServerConstants.BET_DAYS_OFFSET;
		
		Map<Integer, Date> datesToFetch = new LinkedHashMap<>();
		
		datesToFetch.put(0, new Date());
		
//			Calendar instance = Calendar.getInstance();
//			instance.add(Calendar.DATE, 1);
//		datesToFetch.put(1, instance.getTime());

//		for (int i = -1 * daysOffSet; i <= daysOffSet; i++ ) {
//			Date date = instance.getTime();
//			
//			if(datesToFetch.size()<2)//TODO remove later
//			datesToFetch.put(i, date);
//		}
		
		return datesToFetch;
	}
	
	public static String todayStr() {
		return new SimpleDateFormat(CommonConstants.BASE_DATE_FORMAT).format(new Date());
	}
	
	public static String dateStr(Date date) {
		return new SimpleDateFormat(CommonConstants.BASE_DATE_FORMAT).format(date);
	}

	public static boolean gamesExistInNextMonth(List<String> start_at_list) {
		int monthValue = LocalDate.now().getMonthValue();
		
		for (String string : start_at_list) {
			if (string == null || string.length() < 10) {
				throw new RuntimeException("INVALID START TIME FORMAT");
			}
			
			
			
			if (!  (monthValue == Integer.parseInt(string.split(StringConstants.MINUS)[1]))) {
				return true;
			}
			
			
		}
		
		return false;
	}

	public static int getMonthAsInt(int offset) {
		if (-11 > offset || 11 < offset) {
			throw new RuntimeException("MONTH REQUIRED INVALID");
		}
		
		int curr = LocalDate.now().getMonthValue();
		if (offset == 0) {
			return curr;
		}
		
		int desired = curr + offset;
		if (desired < 1) {
			return (12 - desired);
		}
		
		if (desired > 12) {
			return (desired - 12);
		}
		
		
		return desired;
	}

	public static int getYearOfPreviousMonthAsInt() {
		int currMonth = getMonthAsInt(0);
		int currYear = LocalDate.now().getYear();
		
		return currMonth == 1 ? currYear - 1 : currYear;
	}

	public static int getNextMonthOf(int month) {
		if (month < 1 || month > 12) {
			throw new RuntimeException("Invalid month " + month);
		}
		
		int desired = month + 1;
		
		if (desired > 12) {
			return (desired - 12);
		}
		
		
		return desired;
	}

}
