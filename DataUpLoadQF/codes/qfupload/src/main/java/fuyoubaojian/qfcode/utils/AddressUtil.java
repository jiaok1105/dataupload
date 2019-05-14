package fuyoubaojian.qfcode.utils;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

public class AddressUtil {
	
	public static Map<String, Object> getCsAddressHierarchy(String addrCode, JdbcTemplate jdbcTemplate) {
		
		Map<String, Object> result = new HashMap<String, Object>();
		if(addrCode==null||addrCode.length()<6){
			return result;
		}
		result.put("DM_CSD_SHEN", addrCode.substring(0, 2)+"0000");
		result.put("DM_CSD_SHI", addrCode.substring(0, 4)+"00");
		result.put("DM_CSD_QU", addrCode.substring(0, 6));
		result.put("CSD_SHEN", getText(addrCode.substring(0, 2), jdbcTemplate));
		result.put("CSD_SHI", getText(addrCode.substring(0, 4), jdbcTemplate));
		result.put("CSD_QU", getText(addrCode.substring(0, 6), jdbcTemplate));
		if(addrCode.length() >= 9) {
			result.put("DM_CSD_XIANG", addrCode.substring(0, 9));
			result.put("CSD_XIANG", getText(addrCode.substring(0, 9), jdbcTemplate));
			
		}
		if(addrCode.length() >= 12) {
			result.put("DM_CSD_JWBM", addrCode.substring(0, 12));
			result.put("CSD_JW", getText(addrCode.substring(0, 12), jdbcTemplate));
			
		}
		
		return result;
		
	}
	
	private static String getText(String code, JdbcTemplate template) {		
		
		String query = "select text from t_dic_region where code = " + code;
		
		Map<String, Object> map = null;
		try {
			map = template.queryForMap(query);
		} catch (DataAccessException e) {
			return null;
		}
		return (String)map.get("text");
		
	}

	public static String toStr(Object obj,String defaultValue) {
		String newText = toStr(obj);
		
		if(newText.equals(""))
			return defaultValue;
		else
			return newText;
	}

	public static String toStr(Object obj) {
		
		String newText="";
		if(obj!=null){
			newText=String.valueOf(obj).trim();	
			newText.replace("'","‘");
		}
		return newText;
		
	}
	
	public static int toInt(Object obj) {
		int newText=0;
		try {
			newText=Integer.parseInt(toStr(obj));
		} catch (Exception e) {
			return 0;
		}
		return newText;		
	}

	/**
	 * 将小数点去掉
	 * @param obj
	 * @return
	 */
	public static String toStringByIn(Object obj){
		String str = toStr(obj);
		int strIn=Double.valueOf(str).intValue();
		String strO = String.valueOf(strIn);
		return strO;
	}

}
