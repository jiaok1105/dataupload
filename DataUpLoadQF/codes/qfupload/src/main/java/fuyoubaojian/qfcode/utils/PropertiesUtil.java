package fuyoubaojian.qfcode.utils;

import java.util.Properties;

public class PropertiesUtil {
	public static Properties prop = new Properties();
	
	public static String getProp(String key) {
		return prop.getProperty(key);
	}
	
	public static void setProp(String key, String value){
		prop.setProperty(key, value);
	}
}
