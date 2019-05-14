package fuyoubaojian.qfcode.etl;

import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;

/**
 * 曲阜妇女儿童妇幼数据上传基类
 */
public abstract class BaseTask implements TaskInterface {
	private static final Logger logger = Logger.getLogger(BaseTask.class);

	@Resource(name = "jdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	@Resource(name = "jingjiangJdbcTemplate")
	private JdbcTemplate jingjiangJdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public JdbcTemplate getJingjiangJdbcTemplate() {
		return jingjiangJdbcTemplate;
	}

	public void execute() {
		//开启日志
		logBefore();
		String sql = getSQL(jdbcTemplate);
		List<Map<String, Object>> objs = null;
		try {
			objs = jdbcTemplate.queryForList(sql);

			if(objs.size()>0){
				Map<String, Object> map1 = objs.get(0);
				Object gpidfive = (Object) map1.get("gpidfive");
				if(null != gpidfive){
					return;
				}
			}
			logLook(objs.size());
		} catch (DataAccessException e) {
			logger.error("查询失败",e);
		}
		//先删除后插入，避免重复插入
//		try {
//			String deleteSql = getDeleteSql();
//			if(deleteSql!=null && deleteSql.length()>1){
//				jingjiangJdbcTemplate.execute(deleteSql);
//				logger.info("删除成功");
//			}
//		} catch (DataAccessException e) {
//			logger.error("删除失败",e);
//		}
		try {
			if(!CollectionUtils.isEmpty(objs)){
				int count = 0;
				int nocount = 0;
				for (int i = 0; i < objs.size(); i++) {
					try{
						//Integer state = getStateSQL(objs.get(i));
						Map<String,Object> resuletMap = getStateSQL(objs.get(i));
						//输出下insert into 语句，检查是否正确
						/*logger.info("1成功  0失败  2未建管理卡state：" + state);*/
						//jingjiangJdbcTemplate.execute(updateSQL);
						/*logAfter(jdbcTemplate,objs.get(i));*/
						Object stateO = (Object) resuletMap.get("state");
						String state = String.valueOf(stateO);
						if(StringUtils.equals(state,"1")){//1成功  0失败  2已经存在该次随访记录,暂时不提供更新操作! 未建管理卡
							count ++;
						}else if(StringUtils.equals(state,"2")){//2已经存在该次随访记录,暂时不提供更新操作   未上传身份证号，请检查
							nocount ++;
						}
						logAfter(jdbcTemplate,resuletMap);


					} catch (Exception e) {
						logger.error("上传失败" + e.getMessage());
						e.printStackTrace();
					}

				}
				logger.info("数据总计" +objs.size()+ "条，上传成功" +count+ "条，失败" +nocount+ "条");


			}
		} catch (DataAccessException e) {
			logger.error("数据上传失败",e);
		}
	}


	public abstract String getDeleteSql();

	/**
	 * 启动日志
	 *
	 * @return
	 */
	public abstract void logBefore();

	/**
	 * 查询数据日志
	 *
	 * @param i
	 * @return
	 */
	public abstract void logLook(int i);

	/**
	 * 迁移数据日志
	 *
	 * @param i
	 * @return
	 */
	public abstract void logAfter(JdbcTemplate jdbcTemplate, Map<String, Object> objs);

	/**
	 * 查询数据sql
	 *
	 * @return
	 */
	public abstract String getSQL(JdbcTemplate jdbcTemplate);

	/**
	 * 更新库sql
	 *
	 * @return
	 */
	public abstract String getUpdateSQL(Map<String, Object> objs);
	/**
	 * 上传 返回jsonstate
	 *
	 * @return
	 */
	public abstract Map<String,Object> getStateSQL(Map<String, Object> objs);

}
