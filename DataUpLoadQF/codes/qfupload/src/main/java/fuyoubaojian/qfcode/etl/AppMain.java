package fuyoubaojian.qfcode.etl;

import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 启动容器
 */
public class AppMain {
	private static final Logger logger = Logger.getLogger(AppMain.class);
	public static void main(String[] args) {
		//项目测试
		AbstractApplicationContext context = new ClassPathXmlApplicationContext("application-context.xml");
		//文件目录
		//AbstractApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"file:D:\\qf_uploadData\\application-context.xml"});
		logger.info("spring context has stated successfully!");
	}
}
