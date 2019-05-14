package fuyoubaojian.qfcode.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

public class TimeUtils {
	public static String defDtPtn = "yyyy-MM-dd HH:mm:ss";// 缺省日期格式

	/**
	 * 
	 * @日期 Jan 11, 2010 12:38:42 PM
	 * @描述 获取当前时间，格式：yyyy-MM-dd hh:mm:ss
	 * @return String
	 */
	public static String getCurrentDate() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(defDtPtn);
		return simpleDateFormat.format(new Date());
	}

	public static boolean checkDate(String startDate, String endDate) {
		try {
			Date start = DateUtils.parseDate(startDate, new String[]{"yyyy-MM-dd"});
			Date end = DateUtils.parseDate(endDate, new String[]{"yyyy-MM-dd"});
			if(end.after(start)){
				return true;
			}
		} catch (ParseException e) {
			return false;
		}
		
		return false;
	}

	/**
	 * 功能描述：得到当前时间之前或之后的数据<br>
	 * 
	 * @param number
	 *            正数为以后时间 负数为之前时间 type 1 年 2 月 3 日
	 * @return long <BR>
	 */
	@SuppressWarnings("static-access")
	public static Date getDateToNow(int number, int type) {
		Calendar cal = Calendar.getInstance();
		Date date = new Date();
		cal.setTime(date);
		if (type == 1) {
			cal.add(cal.YEAR, number);
		} else if (type == 2) {
			cal.add(cal.MONTH, number);
		} else if (type == 3) {
			cal.add(cal.DAY_OF_MONTH, number);
		}

		return cal.getTime();
	}

	public static Integer getIntCurrYear() {
		// 获得当前日期
		Calendar cldCurrent = Calendar.getInstance();
		// 获得年月日
		String strYear = String.valueOf(cldCurrent.get(Calendar.YEAR));
		return Integer.valueOf(strYear);
	}

	/**
	 * 获取月龄
	 * 
	 * @param birth
	 * @return
	 */
	public static int getMonths(String birthday) {
		int y = 0;
		Date d = new Date();
		try {
			Date birth = DateUtils.parseDate(birthday, new String[]{"yyyy-MM-dd HH:mm:ss"});
			y = getDiffMonth(birth, d);
			Calendar cal = Calendar.getInstance();
			cal.setTime(d);
			int dayOfMonthDate = cal.get(Calendar.DAY_OF_MONTH);
			cal.setTime(birth);
			int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);
			if (dayOfMonthBirth > dayOfMonthDate) {
				y--;
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());

		}
		return y;
	}

	/*
	 * public static int getMonths(Date begin, Date end) { int y = 0; try { if
	 * (end.getTime() >= begin.getTime()) { long l = end.getTime() -
	 * begin.getTime(); long t = l / (3600 * 24 * 1000); y = (int) (t / 30); } }
	 * catch (Exception e) { e.printStackTrace(); } return y; }
	 */
	public static int getDiffMonth(Date beginDate, Date endDate) {
		Calendar calbegin = Calendar.getInstance();
		Calendar calend = Calendar.getInstance();
		calbegin.setTime(beginDate);
		calend.setTime(endDate);
		int m_begin = calbegin.get(Calendar.MONTH) + 1;
		int m_end = calend.get(Calendar.MONTH) + 1;

		int checkmonth = m_end - m_begin
				+ (calend.get(Calendar.YEAR) - calbegin.get(Calendar.YEAR))
				* 12;

		return checkmonth;
	}

	public static String formatString(String date,int dateLength){
		if(date == null){
			return null;
		}
		if(dateLength == 0){
			dateLength = 8;//yyyyMMdd
		}
		if(date.indexOf("-") == -1){
			return date;
		}else {
			date = date.replaceAll("-", "").trim();
			date = date.replaceAll(":", "").trim();
			date = date.replaceAll(" ", "").trim();
			date = date.substring(0,dateLength);
		}
		return date;
	}
	public static void main(String[] args) {
		boolean b = TimeUtils.checkDate("2017-01-01", "2017-02-01");
		//DateFormatUtils.format(TimeUtils.getDateToNow(-1,3), "yyyy-MM-dd")
		System.out.println(b);

	}

}
