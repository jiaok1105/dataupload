package fuyoubaojian.qfcode.etl;

import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.xfire.client.Client;

/**
 * 启动容器
 */
public class AppMainTest {
	public static void main(String[] args) {			
		StringBuilder xmlCheckIn=new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		xmlCheckIn.append("<webService>");
		xmlCheckIn.append("<request>");
		xmlCheckIn.append("<userCode>321202001000</userCode>");
		xmlCheckIn.append("<unitsCode>321202001000</unitsCode>");
		xmlCheckIn.append("<licenseId>1</licenseId>");
		xmlCheckIn.append("</request>");
		xmlCheckIn.append("</webService>");

		try{
			System.out.println("ws输入：" + xmlCheckIn.toString());
			Client client=new Client(new URL("http://58.222.224.142:8080/SupHibmsInt/webservice/supHibms/supHibms?wsdl"));
			Object[] objects = client.invoke("checkIn", new String[] {xmlCheckIn.toString()});
			if (objects != null && objects.length > 0) {
				String result = (String) objects[0];
				System.out.println("ws输出：" + result);
				int beginIndex=result.indexOf("businessNo");
				int endIndex=result.lastIndexOf("businessNo");
				System.out.println(beginIndex);
				System.out.println(endIndex);
				System.out.println(result.substring(beginIndex+11, endIndex-2));
			}
		}catch (MalformedURLException e) {
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		}
}
