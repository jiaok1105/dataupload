package fuyoubaojian.qfcode.utils;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;

/**
 * 操作日期的工具类
 */
public class DateUtil {

	/**
	 * 时间格式yyyyMMddHHmmss.
	 */
	public static final String FORMAT_14 = "yyyyMMddHHmmss";

	/**
	 * 时间格式yyyy-MM-dd HH:mm:ss.
	 */
	public static final String FORMAT_19 = "yyyy-MM-dd HH:mm:ss";

	/**
	 * 时间格式yyyy-MM-dd HH:mm:ss:SSS.
	 */
	public static final String FORMAT_21 = "yyyy-MM-dd HH:mm:ss:SSS";

	/** 转化Date */
	public static Date format2Date(String str) {
		Date date = null;
		date = java.sql.Date.valueOf(str); // 只保留日期部分，返回的是java.sql.Date
		return date;
	}

	public static String formatDate(String date) {
		if (date == null || date.trim().equals(""))
			return "";
		return date.replaceAll("-", "");
	}

	/**
	 * 把YYYYMMDDHHMMSS格式化成YYYY-MM-DD;
	 * 
	 * @param date
	 * @return
	 */
	public static String formatDate1(String date) {
		if (date == null || date.trim().equals(""))
			return "";
		StringBuffer buf = new StringBuffer(date);
		return (buf.insert(6, '-').insert(4, '-').toString()).substring(0, 10);
	}

	/**
	 * 把YYYYMMDD个串格式化成YYYY-MM-DD
	 * 
	 * @param date
	 * @return
	 */
	public static String formatFrom2(String date) {
		if (date == null || date.trim().equals(""))
			return "";
		StringBuffer buf = new StringBuffer(date);
		return buf.insert(6, '-').insert(4, '-').toString();
	}

	/**
	 * 提取下拉框中年列表
	 * 
	 * @return
	 */
	public static List<String> getYearList() {
		List<String> lReturn = new ArrayList<String>();

		for (int i = 2008; i < 2020; i++) {
			lReturn.add(String.valueOf(i));
		}

		return lReturn;
	}

	/**
	 * 根据给定的年份和季度提取对应的季未日期
	 */
	public static String getQuarterOfLastDate(String ogYear, int iValue) {
		String strRetDate = "";
		switch (iValue) {
		case 1:
			strRetDate = ogYear + "0331";
			break;
		case 2:
			strRetDate = ogYear + "0630";
			break;
		case 3:
			strRetDate = ogYear + "0930";
			break;
		case 4:
			strRetDate = ogYear + "1231";
			break;
		}

		return strRetDate;
	}

	/**
	 * 根据参数ogdate，得到ogdate这个月的最后一个日期，例如：getLastDate("200308")=20030831
	 * 参数ogdate必须是6位（到月）或8位（到日）
	 * 
	 * @param ogdate
	 * @return
	 */
	public static String getMonthLastDate(String ogdate) {
		if (ogdate.length() == 6)
			ogdate = ogdate + "01";
		else {// 把ogdate变成前6位加01的串，如20030805-->20030801
			ogdate = ogdate.substring(0, 6) + "01";
		}
		ogdate = getNextDateByMonth(ogdate, 1);
		ogdate = getNextDateByNum(ogdate, -1);
		return ogdate;
	}

	/**
	 * 得到+i以后的日期，i可以是负数
	 * 
	 * @param s
	 *            (yyyyMMdd)
	 * @param i
	 * @return
	 */
	public static String getNextDateByNum(String s, int i) {
		s = formatDate(s);
		SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyyMMdd");
		Date date = simpledateformat.parse(s, new ParsePosition(0));
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(5, i);
		date = calendar.getTime();
		s = simpledateformat.format(date);
		return s;
	}

	/**
	 * 得到+i以后的日期，i可以是负数
	 * 
	 * @param s
	 * @param s
	 *            (格式)
	 * @param i
	 * @return
	 */
	public static String getNextDateByNum(String s, int i, String format) {
		if(StringUtils.isBlank(s)){
			return "";
		}
		SimpleDateFormat simpledateformat = new SimpleDateFormat(format);
		Date date = simpledateformat.parse(s, new ParsePosition(0));
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(5, i);
		date = calendar.getTime();
		s = simpledateformat.format(date);
		return s;
	}
	

	/**
	 * 得到+i以后的月，i可以是负数
	 * 
	 * @param s
	 * @param i
	 * @return
	 */
	public static String getNextDateByMonth(String s, int i) {
		s = formatDate1(s);
		SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = simpledateformat.parse(s, new ParsePosition(0));
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(2, i);
		date = calendar.getTime();
		s = simpledateformat.format(date);
		return s;
	}
	/**
	 * 得到+i以后的月，i可以是负数，added by sumaomao
	 * 
	 * @param s
	 * @param i
	 * @return
	 */
	public static String getDateByMonth(String s, int i) {
		SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = simpledateformat.parse(s, new ParsePosition(0));
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(2, i);
		date = calendar.getTime();
		s = simpledateformat.format(date);
		return s;
	}
	
	/**
	 * 得到+i以后的天，i可以是负数，added by sumaomao
	 * 
	 * @param s
	 * @param i
	 * @return
	 */
	public static String getDateByDays(Date s, int i) {
		if(s==null){
			return "";
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(s);
		calendar.add(Calendar.DAY_OF_MONTH, i);
		Date date = calendar.getTime();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		String d1 = format.format(date);
		return d1;
	}
	

	/**
	 * 系统当前时间(hhmmss)
	 * 
	 * @return
	 */
	public static String getSysTime() {
		SimpleDateFormat simpledateformat = new SimpleDateFormat("hhmmss");
		String s = simpledateformat.format(Calendar.getInstance().getTime());
		return s;
	}

	/**
	 * 得到系统当前日期(yyyyMMdd)
	 * 
	 * @return
	 */
	public static String getSysDate() {
		SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyyMMdd");
		String s = simpledateformat.format(Calendar.getInstance().getTime());
		return s;
	}

	/**
	 * 得到系统当前年(yyyy)
	 * 
	 * @return
	 */
	public static String getSysYear() {
		SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyyMMdd");
		String s = simpledateformat.format(Calendar.getInstance().getTime());
		return s.substring(0, 4);
	}

	/**
	 * 得到系统当前日期(yyyy-MM-dd)
	 * 
	 * @return
	 */
	public static String getCurrentDate() {
		return DateFormatUtils.format(new Date(), "yyyy-MM-dd");
	}

	/**
	 * 提取当前日期时间(yyyy-MM-dd HH:mm:ss)
	 * 
	 * @return
	 */
	public final static String getCurrentDateTime() {

		SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String s = simpledateformat.format(Calendar.getInstance().getTime());
		return s;
	}

	/**
	 * 提取当前时间
	 * 
	 * @return
	 */
	public final static String getCurrentTime() {
		StringBuffer ret = new StringBuffer();
		Calendar cal = Calendar.getInstance();
		String strH = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
		if (strH.length() == 1) {
			strH = "0" + strH;
		}

		String strM = String.valueOf(cal.get(Calendar.MINUTE));
		if (strM.length() == 1) {
			strM = "0" + strM;
		}

		String strS = String.valueOf(cal.get(Calendar.SECOND));
		if (strS.length() == 1) {
			strS = "0" + strS;
		}
		ret.append(strH + ":");
		ret.append(strM + ":");
		ret.append(strS);
		return ret.toString();
	}

	/**
	 * 日期转换成中文方式
	 * 
	 * @param fdate
	 * @return
	 */
	public static String getCNDate(String fdate) {
		if (fdate == null || fdate.length() < 6) {
			return fdate;
		}
		String cur_date = fdate;
		cur_date = cur_date.substring(0, 4) + "年" + cur_date.substring(4, 6) + "月" + cur_date.substring(6) + "日";
		return cur_date;
	}

	/**
	 * 时间转换成中文方式
	 * 
	 * @param ftime
	 * @return
	 */
	public static String getCNTime(String ftime) {
		String cur_time = ftime;
		cur_time = cur_time.substring(0, 2) + "时" + cur_time.substring(2, 4) + "分" + cur_time.substring(4) + "秒";
		return cur_time;
	}

	/**
	 * 计算两个日期相差的天数
	 * 
	 * @param startDate
	 *            格式：yyyy-MM-dd
	 * @param endDate
	 *            格式：yyyy-MM-dd
	 * @return 返回两日期相差的天数
	 */
	public static int dateMargin(String startDate, String endDate) {
		Date d1 = null;
		Date d2 = null;
		try {
			if(StringUtils.isBlank(startDate)||StringUtils.isBlank(endDate)||startDate.length()<10||endDate.length()<10){
				return -2;
			}
			d1 = DateUtils.parseDate(startDate.substring(0,10), new String[]{"yyyy-MM-dd"});
			d2 = DateUtils.parseDate(endDate.substring(0,10), new String[]{"yyyy-MM-dd"});
			long time = d2.getTime() - d1.getTime();
			return Integer.parseInt(String.valueOf(time/(1000*3600*24)));
		} catch (Exception e) {
			return -1;
		}
	}

	/**
	 * 根据给定的日期提取对应的季度
	 * 
	 * @param strDate
	 * @return
	 */
	public static String getQuarter(String strDate) {
		String strCurrMonth = strDate.substring(4, 6);

		int iMonth = Integer.parseInt(strCurrMonth);
		if (iMonth < 4) {
			return "1";
		} else if (iMonth > 3 && iMonth < 7) {
			return "2";
		} else if (iMonth > 6 && iMonth < 10) {
			return "3";
		} else {
			return "4";
		}

	}

	/**
	 * 得到本月的第一天
	 * 
	 * @return
	 */
	public static String getMonthFirstDay() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
		SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd");
		return simpledateformat.format(calendar.getTime());
	}

	/**
	 * 得到本月的最后一天
	 * 
	 * @return
	 */
	public static String getMonthLastDay() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd");
		return simpledateformat.format(calendar.getTime());
	}

	/**
	 * 得到上个月的最后一天
	 * 
	 * @return
	 */
	public static String getPreviousMonthEnd() {
		String str = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		Calendar lastDate = Calendar.getInstance();
		lastDate.add(Calendar.MONTH, -1);// 减一个月
		lastDate.set(Calendar.DATE, 1);// 把日期设置为当月第一天
		lastDate.roll(Calendar.DATE, -1);// 日期回滚一天，也就是本月最后一天
		str = sdf.format(lastDate.getTime());
		return str;
	}

	/**
	 * 得到上个月的第一天
	 * 
	 * @return
	 */
	public static String getPreviousMonthFirst() {
		String str = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		Calendar lastDate = Calendar.getInstance();
		lastDate.set(Calendar.DATE, 1);// 设为当前月的1号
		lastDate.add(Calendar.MONTH, -1);// 减一个月，变为下月的1号
		// lastDate.add(Calendar.DATE,-1);//减去一天，变为当月最后一天

		str = sdf.format(lastDate.getTime());
		return str;
	}

	// 获得本年第一天的日期 *
	public static String getCurrYearFirst() {
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");// 可以方便地修改日期格式
		String year = dateFormat.format(date);
		return year + "-01-01";
	}

	// 获得本年最后一天的日期 *
	public static String getCurrentYearEnd() {
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");// 可以方便地修改日期格式
		String years = dateFormat.format(date);
		return years + "-12-31";
	}

	// 获得上年第一天的日期 *
	public static String getPreviousYearFirst() {
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");// 可以方便地修改日期格式
		String years = dateFormat.format(date);
		int years_value = Integer.parseInt(years);
		years_value--;
		return years_value + "-1-1";
	}

	// 获得本季度
	public static String getThisSeasonTime(int month) {
		int array[][] = { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 }, { 10, 11, 12 } };
		int season = 1;
		if (month >= 1 && month <= 3) {
			season = 1;
		}
		if (month >= 4 && month <= 6) {
			season = 2;
		}
		if (month >= 7 && month <= 9) {
			season = 3;
		}
		if (month >= 10 && month <= 12) {
			season = 4;
		}
		int start_month = array[season - 1][0];
		int end_month = array[season - 1][2];

		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");// 可以方便地修改日期格式
		String years = dateFormat.format(date);
		int years_value = Integer.parseInt(years);

		int start_days = 1;// years+"-"+String.valueOf(start_month)+"-1";//getLastDayOfMonth(years_value,start_month);
		int end_days = getLastDayOfMonth(years_value, end_month);
		String seasonDate = years_value + "-" + start_month + "-" + start_days + ";" + years_value + "-" + end_month + "-" + end_days;
		return seasonDate;

	}

	/**
	 * 获取某年某月的最后一天
	 * 
	 * @param year
	 *            年
	 * @param month
	 *            月
	 * @return 最后一天
	 */
	private static int getLastDayOfMonth(int year, int month) {
		if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
			return 31;
		}
		if (month == 4 || month == 6 || month == 9 || month == 11) {
			return 30;
		}
		if (month == 2) {
			if (isLeapYear(year)) {
				return 29;
			} else {
				return 28;
			}
		}
		return 0;
	}

	/**
	 * 是否闰年
	 * 
	 * @param year
	 *            年
	 * @return
	 */
	public static boolean isLeapYear(int year) {
		return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
	}

	/**
	 * 计算二个日期间的(当前月份)
	 * 
	 * @param rq_beg
	 *            (格式：yyyy-mm-dd)
	 * @param rq_end
	 * @return
	 */
	public static String[] getDate(String rq_beg, String rq_end) {
		if (rq_beg == null || rq_end == null) {
			return null;
		}
		String strRet[] = null;
		if (rq_beg.substring(0, 7).equals(rq_end.substring(0, 7))) {
			int ibeg = Integer.parseInt(rq_beg.substring(8));
			int iend = Integer.parseInt(rq_end.substring(8));
			strRet = new String[iend - ibeg + 1];
			for (int i = ibeg; i <= iend; i++) {
				if (i < 10) {
					strRet[i - ibeg] = rq_beg.substring(0, 7) + "-0" + i;
				} else {
					strRet[i - ibeg] = rq_beg.substring(0, 7) + "-" + i;
				}
			}
		}

		return strRet;
	}

	public static String[] getDate(String month) {
		String rq_beg = month + "-01";
		String rq_end = formatFrom2(getMonthLastDate(month.substring(0, 4) + month.substring(5, 7)));
		String[] stv = getDate(rq_beg, rq_end);

		return stv;
	}

	public static String[] getMonth(String month_beg, String month_end) {
		String nf_beg = month_beg.substring(0, 4);
		String nf_end = month_end.substring(0, 4);
		String yf_beg = month_beg.substring(5, 7);
		String yf_end = month_end.substring(5, 7);
		int iyfb = Integer.parseInt(yf_beg);
		int iyfe = Integer.parseInt(yf_end);

		String[] datev = null;
		if (nf_beg.equals(nf_end)) {
			datev = new String[iyfe - iyfb + 1];
			for (int i = iyfb; i <= iyfe; i++) {
				if (i < 10) {
					datev[i - iyfb] = nf_beg + "-0" + i;
				} else {
					datev[i - iyfb] = nf_beg + "-" + String.valueOf(i);
				}
			}
		} else {
			datev = new String[12 - iyfb + iyfe + 1];
			for (int i = iyfb; i <= 12; i++) {
				if (i < 10) {
					datev[i - iyfb] = nf_beg + "-0" + i;
				} else {
					datev[i - iyfb] = nf_beg + "-" + String.valueOf(i);
				}
			}
			for (int i = 1; i <= iyfe; i++) {
				if (i < 10) {
					datev[12 - iyfb + i] = nf_end + "-0" + i;
				} else {
					datev[12 - iyfb + i] = nf_end + "-" + String.valueOf(i);
				}
			}
		}
		return datev;
	}

	/**
	 * 计算两个日期的年数差，用来计算年龄
	 */
	public static int getNlYear(String begdate, String enddate) {
		int a = 0;
		int a1 = Integer.valueOf(begdate.substring(0, 4));
		int a2 = Integer.valueOf(enddate.substring(0, 4));
		a = a1 - a2;
		return a;
	}

	/**
	 * 把字符串转换成日期格式 Create Time: 12-4-19 上午8:48
	 * 
	 * @author: wujuna
	 * @param dateStr
	 *            字符串格式
	 * @param formatStr
	 *            被转换字符串的时间格式，例如："yyyy-MM-dd HH:mm:ss"
	 * @return Date 日期格式
	 */
	public static Date stringToDate(String dateStr, String formatStr) {
		SimpleDateFormat formatDate = new SimpleDateFormat(formatStr);
		Date date = null;
		try {
			date = formatDate.parse(dateStr);
		} catch (ParseException e) {
			// e.printStackTrace();
		}
		return date;
	}

	/**
	 * 把日期按照指定格式转换成字符串格式 Create Time: 12-4-19 上午8:48
	 * 
	 * @author: wujuna
	 * @param date
	 *            日期
	 * @param formatStr
	 *            指定时间格式，例如："yyyy-MM-dd HH:mm:ss"
	 * @return String 字符串
	 */
	public static String dateToString(Date date, String formatStr) {
		SimpleDateFormat formatDate = new SimpleDateFormat(formatStr);
		return formatDate.format(date);

	}

	/**
	 * 日期加小时数得到的结果
	 * 
	 * @param date
	 *            日期
	 * @param x
	 * @return Date 日期加小时数得到的结果
	 */
	public static Date addHour(Date date, int x) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.HOUR_OF_DAY, x);// 24小时制
		date = cal.getTime();
		return date;
	}

	/**
	 * 增加
	 * 
	 * @param date
	 *            日期
	 * @param x
	 * @return Date 日期加年数得到的结果
	 */
	public static Date addYear(Date date, int x) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.YEAR, x);
		date = cal.getTime();
		return date;
	}
	
	//返回两个时间差的月数，超过15天算一个月
	public static Integer dateDiffMonth2(String beginDateStr, String endDateStr){
	   beginDateStr = formatDate1(beginDateStr);
	   endDateStr = formatDate1(endDateStr);
	   Date sDate = format2Date(beginDateStr); //转化成日期对象
	   Date eDate = format2Date(endDateStr);

	    //获得各自的年、月、日
	    Integer sY    = sDate.getYear();     
	    Integer sM    = sDate.getMonth()+1;
	    Integer sD    = sDate.getDate();
	    Integer eY    = eDate.getYear();
	    Integer eM    = eDate.getMonth()+1;
	    Integer eD    = eDate.getDate();
	    
	    
	    //var flagD = 0;   //日期标记：
	    Integer flagM = 0;    //月份进/减位标记
	    Integer flagY = 0;    //年份进/减位标记
	    Integer months = 0;   //相隔约数，返回值
	    
	    Integer d = eD - sD;  //日期相差天数
	    if(d>0&&d>=15)  //如果为正，且大于15天，月份进一
	    {
	     flagM = 1;
	    }
	    if(d<0&&30+d<15)  //如果为负，且相隔天数<15，月份减一
	    {
	     flagM = -1;
	    }
	    
	    Integer m = eM + flagM - sM;   //相隔月数 = 结束月份 + 月份进/减位标记 - 开始月份
	    if(m<0)                    //如果小于0，年数减一，月数为12减去相隔月数
	    {    
	        flagY = -1;
	        m = 12 + m;
	    }
	    
	    Integer y = eY + flagY - sY;  //相隔年数 = 结束年份 + 年份进/减位标记 - 开始年份
	      
	    if(y>=0)                 //如果大于等于0，则返回值为年份数*12 + 月份数，否则返回0
	        months = y*12 + m;

	    return months;
	}
}
