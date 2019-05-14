package fuyoubaojian.qfcode.utils;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

public class DicSystemUtil {
//	private static final Logger logger = Logger.getLogger(DicSystemUtil.class);
	public static Object getText(Object code,String bigType,String type, JdbcTemplate template) {
		
		String query = "SELECT text FROM t_dic_system WHERE big_type='"+bigType+"' and type ='"+type+"'  and code='" + code.toString()+"';";
		Map<String, Object> map = null;
		try {
			map = template.queryForMap(query);
		} catch (DataAccessException e) {
			return null;
		}
		return map.get("text");
		
	}

	public static Object getEncodeing(Object code,String bigType,String type, JdbcTemplate template) {
		
		String query = "SELECT encoding FROM t_dic_system WHERE big_type='"+bigType+"' and type ='"+type+"'  and code='" + code.toString()+"';";
		Map<String, Object> map = null;
		try {
			map = template.queryForMap(query);
		} catch (DataAccessException e) {
			return null;
		}
		return map.get("encoding");
		
	}

	public static String getCareerString(Object obj) {
		
		String newText="";
		//职业
		String career=AddressUtil.toStr(obj);
		if("04".equals(career)){
			newText="'2-4',";
		}else if("05".equals(career)){
			newText="'4-7/4-8',";
		}else if("06".equals(career)){
			newText="'4-3',";
		}else if("07".equals(career)){
			newText="'4',";
		}else if("08".equals(career)){
			newText="'4-6',";
		}else if("09".equals(career)){
			newText="'6/7/8/9',";
		}else if("10".equals(career)){
			newText="'8-8/8-9',";
		}else if("11".equals(career)){
			newText="'5',";
		}else if("12".equals(career)){
			newText="'5-3',";
		}else if("13".equals(career)){
			newText="'5-4',";
		}else if("14".equals(career)){
			newText="'3-1',";
		}else if("15".equals(career)){
			newText="'1-3',";
		}else if("16".equals(career)){
			newText="'4-7/4-8',";
		}else if("17".equals(career)){
			newText="'3-9',";
		}else{
			newText="'3-9',";
		}
		return newText;
		
	}

	public static String getEducationLevel(Object obj) {
		
		String newText="";
		//文化程度
		String career=AddressUtil.toStr(obj);
		if("1".equals(career)){
			newText="'10',";
		}else if("2".equals(career)){
			newText="'20',";
		}else if("3".equals(career)){
			newText="'30',";
		}else if("4".equals(career)){
			newText="'40',";
		}else if("5".equals(career)){
			newText="'47',";
		}else if("6".equals(career)){
			newText="'60',";
		}else if("7".equals(career)){
			newText="'70',";
		}else if("8".equals(career)){
			newText="'80',";
		}else if("9".equals(career)){
			newText="'90',";
		}else{
			newText="'90',";
		}
		return newText;
		
	}

	public static String getLaborwayString(Object obj) {
		String newText="";
		//分娩方式
		String laborway=AddressUtil.toStr(obj);
		if("1".equals(laborway)){
			newText="1";
		}else if("3".equals(laborway)){
			newText="4";
		}else if("21".equals(laborway)){
			newText="3";
		}else if("22".equals(laborway)){
			newText="6";
		}else if("23".equals(laborway)){
			newText="2";
		}else if("9".equals(laborway)){
			newText="7";
		}else{
			newText="7";
		}
		return newText;
		
	}

	//既往病史代码
	public static String getPassHistoryString(Object obj) {
		String newText="";
		//既往史
		String passhistory=AddressUtil.toStr(obj);

		//1无2心脏病6肾脏疾病 3肝脏疾病4贫血5糖尿病7高血压8其他，多于一个以“|”间隔
		if(passhistory.indexOf("99")>-1){
			newText="1";
		}else{
			if(passhistory.indexOf("01")>-1){
				newText=newText+"2|";
			}
			if(passhistory.indexOf("12")>-1){
				newText=newText+"6|";
			}
			if(passhistory.indexOf("11")>-1){
				newText=newText+"3|";
			}
			if(passhistory.indexOf("07")>-1){
				newText=newText+"7|";
			}
			if(passhistory.indexOf("05")>-1){
				newText=newText+"4|";
			}
			if(passhistory.indexOf("06")>-1){
				newText=newText+"5|";
			}
			if(newText.indexOf("|")>-1){
				newText=newText.substring(0, newText.length()-1);
			}
			//其他
			if(newText.length()==0){
				newText="8";
			}
		}
		return newText;
		
	}

	//家族史代码
	public static String getFamilyHistoryString(Object obj) {
		String newText="";
		//家族史
		String familyhistory=AddressUtil.toStr(obj);

		//1遗传性疾病史　2精神疾病史 3其他
		if(familyhistory.indexOf("99")>-1){
			newText="3";
		}else{
			if(familyhistory.indexOf("13")>-1){
				newText="1";
			}
			if(familyhistory.indexOf("12")>-1){
				newText="2";
			}
			//其他
			if(newText.length()==0){
				newText="3";
			}
		}
		return newText;
		
	}
	
	/**
	 * 台州上传，儿童 发育评价转换
	 * @param target
	 * @return
	 */
	public static String getTZ_Assess(String target) {
//		 * NBB04.01.107
//			值	值含义	说    明
//			0	-3SD	
//			1	-2SD	
//			2	-1SD	
//			3	均值	
//			4	+1SD	
//			5	+2SD	
////			6	+3SD
//		---------------
//上
//上等
//下
//下等
//中
//中+
//中-
//中上
//中上等
//中下
//中下等
//中等
//中等偏上
//中等偏下
//无
		String reStr;
		if("中下等".equals(target) || "中-".equals(target) || "中等偏下".equals(target))
			reStr = "2";
		else if("下".equals(target) || "下等".equals(target))
			reStr = "1";
		if("中上等".equals(target) || "中+".equals(target) || "中等偏上".equals(target))
			reStr = "4";
		else
			reStr = "3";
		return reStr;
	}
	
	/**
	 * 台州上传，喂养方式转换
	 * 
	 * @param target
	 * @return
	 */
	public static String getTZ_FeedWay(String target) {
//		B.4.1.2.2	新生儿访视记录-喂养方式代码表 NBB04.01.102
//		值	值含义	说    明
//		1	母乳喂养	
//		2	纯母乳喂养	
//		3	人工喂养	
//		4	混合喂养	
//		9	其他方式	
//		--------------------------------------
//		1	纯母乳	访视-喂养方式
//		2	混合喂养	访视-喂养方式
//		3	人工喂养	访视-喂养方式
		String reStr;
		if("2".equals(target))
			reStr = "4";
		else if("1".equals(target))
			reStr = "2";
		else
			reStr = "3";
		return reStr;
	}
	
	
	public static String getTZ_BirthWay(String target) {
//		分娩方式代码表 NBB04.01.005
//		值	值含义	说    明
//		1	头位阴道自然分娩	
//		2	臀助产	
//		3	臀牵引	
//		4	胎头牵引	
//		5	产钳	
//		6	临产前剖宫产	
//		7	临产后剖宫产	
//		8	毁胎术	
//		9	其他	
//		99	不详	
//		--------------------------------------
//		1	阴道自然分娩	分娩方式
//		11	会阴切开	分娩方式
//		12	会阴未切	分娩方式
//		2	阴道手术助产	分娩方式
//		21	产钳助产	分娩方式
//		22	臀位助产	分娩方式
//		23	胎头吸引	分娩方式
//		3	剖宫产	分娩方式
//		9	其他	分娩方式
		String reStr;
		if("1".equals(target) || "11".equals(target) || "12".equals(target))
			reStr = "1";
		else if("22".equals(target))
			reStr = "2";
		else if("23".equals(target))
			reStr = "4";
		else if("21".equals(target))
			reStr = "5";
		else if("3".equals(target))
			reStr = "6";
		else 
			reStr = "9";
		return reStr;
	}

	
	public static String getZJCS_Risk(String target) {
		String ret = "";
		if (StringUtils.isEmpty(target)) {
			return ret;
		}
		if(target.equals("9") || target.equals("221"))
			ret= "1";//年龄<18岁或≥35岁
		else if(target.equals("3") || target.equals("1") || target.equals("222"))
			ret= "2";//身高<145cm:体重<45Kg或>70Kg
		else if(target.equals("204"))
			ret= "347";//早产（＜34
		else if(target.equals("195"))
			ret= "323";//≥36周胎位异常
		else if(target.equals("63"))
			ret= "26";//贫血(血色素≤60克/L)
		else if(target.equals("31"))
			ret= "290";//≥3次引流产史		
		else if(target.equals("20"))
			ret= "285";//体重指数（BMI）>24（早孕期）
		else if(target.equals("88"))
			ret= "312";//胎膜早破（34～37
		else if(target.equals("185"))
			ret= "313";//脐带绕颈（≥2
		else if(target.equals("86"))
			ret= "347";//早产（＜34		
		else if(target.equals("80"))
			ret= "415";//严重感染
		else if(target.equals("29"))
			ret= "18";//流产(自然产、人工)>=2次
		else if(target.equals("35") || target.equals("36"))
			ret= "295";//瘢痕子宫(手术≥2次)
		else if(target.equals("86"))
			ret= "347";//早产（＜34
		else
			ret= "999";//其他
		
		
		return ret;
	}

	public static String getZJCS_TaiFW_CODE(String target) {
		String ret = "";
		if (StringUtils.isEmpty(target)) {
			return ret;
		}
		if(target.equals("1LOA"))
			ret= "1";
		else if(target.equals("ROA"))
			ret= "2";
		else if(target.equals("LOP"))
			ret= "3";
		else if(target.equals("ROP"))
			ret= "4";
		else if(target.equals("LOT"))
			ret= "5";
		else if(target.equals("ROT"))
			ret= "6";
		else if(target.equals("LMA"))
			ret= "7";
		else if(target.equals("RMA"))
			ret= "8";
		else if(target.equals("LMP"))
			ret= "9";
		else if(target.equals("RMP"))
			ret= "10";
		else if(target.equals("LMT"))
			ret= "11";
		else if(target.equals("RMT"))
			ret= "12";
		else if(target.equals("LSA"))
			ret= "13";
		else if(target.equals("RSA"))
			ret= "14";
		else if(target.equals("LSP"))
			ret= "15";
		else if(target.equals("RSP"))
			ret= "16";
		else if(target.equals("LST"))
			ret= "17";
		else if(target.equals("RST"))
			ret= "18";
		else if(target.equals("LScA"))
			ret= "19";
		else if(target.equals("RScA"))
			ret= "20";
		else if(target.equals("LScP"))
			ret= "21";
		else if(target.equals("RScP"))
			ret= "22";
		else if(target.equals("NO"))
			ret= "99";
		else
			ret= "0";	
		
		return ret;
	}

}
