package fuyoubaojian.qfcode.qu;

import com.alibaba.fastjson.JSON;
import fuyoubaojian.qfcode.etl.BaseTask;
import fuyoubaojian.qfcode.etl.TaskInterface;
import fuyoubaojian.qfcode.utils.AddressUtil;
import fuyoubaojian.qfcode.utils.HttpClientUtils;
import fuyoubaojian.qfcode.utils.TimeUtils;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 妇女 2-5 次随访上传
 */
@Component
public class Upload_GraTwo2Five_Visit extends BaseTask implements TaskInterface {
	private static final Logger logger = Logger.getLogger(Upload_GraTwo2Five_Visit.class);
	private String startDate;
	private String endDate;

	private String del_flag;

	@Resource(name = "jdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	public String getDel_flag() {
		return del_flag;
	}

	public void setDel_flag(String del_flag) {
		this.del_flag = del_flag;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public void doData(List<Map<String, Object>> objs){
		int count = 0;
		int nocount = 0;
		int nroalount = 0;
		for (Map<String, Object> map : objs){
			Object gpidO = (Object) map.get("gpidfive");
			String gpid = String.valueOf(gpidO);

			StringBuffer buffer = new StringBuffer();
			logger.info("开始查询妇女 2-5 次随访记录信息");
			logger.info("+++++++++++++++++++++++++++++++======SQL:");
			buffer.append(" SELECT grv.id as gragrvid,");
			//buffer.append(" '0' as machineId, ");// 设备 id（同DealerId）,是 String"
			buffer.append(" apv.card_id as idcard, ");// 身份证号码,是 String
			buffer.append(" apv.name as name, ");// 姓名,是 String
			buffer.append(" if(grv.has_except='1','0','1') as feilei, ");// 分类：未见异常： 0；异常： 1,是 String
			buffer.append(" grv.high_risk as flyc, ");// 分类异常：填写异常内容,是 String "
			buffer.append(" grv.girth_paunch as fuwei, ");// 腹围,是 String
			buffer.append(" grv.fundal_height as gdgd, ");// 宫底高度,是 String
			buffer.append(" apv.id as grdabh, ");// 个人档案编号,是 String
			//buffer.append(" grv.id as id, ");// 随访主键 id,是 String
			buffer.append(" copy.yc_num as jkid, ");// 随访记录卡号：个人档案编号+“_”+""number"": 递增,是 String   孕次
			//buffer.append(" apv.name as name, ");// 姓名,是 String
			buffer.append(" '无' as paizhao, ");// 随访照片路径,是 String
			buffer.append(" '无' as ndb, ");// 尿蛋白,是 String
			buffer.append(" '无' as qtjc, ");// 其他辅助检查,是 String
			buffer.append(" gp.fz_num as sfcs, ");// 随访次数,是 String
			buffer.append(" DATE_FORMAT(grv.check_date,'%Y-%m-%d') as sfrq, ");// 随访日期,是 String
			buffer.append(" hd.name as sfys, ");// 随访医生,是 String
			buffer.append(" grv.add_user_id as sfyscode, ");// 随访医生 id,是 String
			buffer.append(" hh.region_code as sssq, ");// 医生所属社区编码,是 String
			buffer.append(" grv.weight as tizhong, ");// 体重,是 String
			buffer.append(" grv.fetal_position as tw, ");// 胎位,是 String
			buffer.append(" grv.fetal_heart as txl, ");// 胎心率,是 String
			buffer.append(" '0' as wzd, ");// 完整度,是 String
			buffer.append(" DATE_FORMAT(grv.next_date,'%Y-%m-%d') as xcsfrq, ");// 下次随访日期,是 String
			buffer.append(" '无' as xhdbz, ");// 血红蛋白,是 String
			buffer.append(" grv.up_blood as xy, ");// 血压,是 String
			buffer.append(" grv.low_blood as xy1, ");// 血压,是 String
			buffer.append(" grv.yz_week as yunzhou, ");// 孕周(周),是 String
			buffer.append(" grv.yz_day as yunzhoutian, ");// 孕周(天),是 String
			buffer.append(" '' as zdqt, ");// 指导其他,是 String
			buffer.append(" concat(if(grv.deal_with like '%生活方式%','1','0'),',',if(grv.deal_with like '%营养%','1','0'),',',if(grv.deal_with like '%心理%','1','0'),',', ");
			buffer.append(" if(grv.deal_with like '%运动%','1','0'),',',if(grv.deal_with like '%自我监测%','1','0'),',',if(grv.deal_with like '%分娩准备%','1','0'),',', ");
			buffer.append(" if(grv.deal_with like '%母乳喂养%','1','0'),',','0') as zhidao, "); // 多选指导:生活方式，营养，心理，运动，自我监测，分娩准备，母乳喂养,是 String
			buffer.append(" if(grv.changer='是','1','0') as zhuanzhen, ");// 转诊：无： 0；有： 1,是 String
			buffer.append(" grv.main_suit as zhusu, ");// 主诉,是 String
			buffer.append(" '' as zxbzw, ");// 注销标志位,是 String
			buffer.append(" grv.change_unit as zzjg, "); // 转诊机构,是 String
			buffer.append(" grv.change_cause as zzyy, ");// 转诊原因,是 String
			buffer.append(" '' as zzlxr, ");// 转诊联系人,是 String
			buffer.append(" '' as zzlxfs, ");// 转诊联系人电话,是 String
			buffer.append(" '' as zzresult, ");// 转诊结果： 0：到位，1：不到位,是 String
			buffer.append(" '1' as xqxcqsc, ");// 免费血清学产前筛查:0:是,1：否ps：第二次随访才有,是 String
			buffer.append(" '' as xqxcqscqk, ");// 当免费血清学产前筛查为是的时候可选,是 String
			buffer.append(" hh.text as cqjcjgmc, ");// 产前检查机构名称,是 String
			buffer.append(" '0' as sffs ");// 随访方式:0:门诊,1:家庭,2:电话,是 String
			buffer.append(" FROM t_gra_return_visit grv ");
			buffer.append(" JOIN t_gra_person AS gp ON gp.id=grv.person_id and gp.statue='0' ");
			buffer.append(" JOIN t_arc_person AS apv ON apv.id=gp.base_info_id and apv.statue='0' ");
			buffer.append(" LEFT JOIN t_arc_person AS apn ON apn.id=gp.husband_info_id and apn.statue='0' ");
			buffer.append(" JOIN t_hos_doctor AS hd ON hd.user_id=grv.add_user_id ");
			buffer.append(" JOIN t_hos_hospital AS hh ON hh.id=hd.hospital_id LEFT JOIN t_gra_build_copy copy on gp.id=copy.person_id");
			buffer.append(" WHERE 1=1");
			buffer.append(" and gp.id = ").append(gpid);
			buffer.append(" order by grv.check_date asc");

			logger.info(" ======SQL"+ buffer.toString());
			List<Map<String, Object>> objs1 = null;
			try {
				objs1 = jdbcTemplate.queryForList(buffer.toString());
				logLook(objs1.size());
			} catch (DataAccessException e) {
				logger.error("查询失败",e);
			}

			try {
				if(!CollectionUtils.isEmpty(objs1)){
					for (int i = 0; i < objs1.size(); i++) {
						nroalount++;
						try{
							Map<String,Object> resuletMap = getStateSQL(objs1.get(i),i+1);
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

				}
			} catch (DataAccessException e) {
				logger.error("数据上传失败",e);
			}

			logger.info("数据总计" +nroalount+ "条，上传成功" +count+ "条，失败" +nocount+ "条");
		}
	}

	@Override
	public String getSQL(JdbcTemplate jdbcTemplate) {
		boolean flag = TimeUtils.checkDate(startDate, endDate);
		String date1 = DateFormatUtils.format(TimeUtils.getDateToNow(-1, 3), "yyyy-MM-dd");
		String date2 = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
		if (flag) {
			date1 = "'" + startDate + " 00:00:00'";
			date2 = "'" + endDate + " 23:59:59'";
		} else {
			date1 = "concat(DATE_SUB(CURDATE(),INTERVAL 1 DAY),' 00:00:00')";
			date2 = "concat(DATE_SUB(CURDATE(),INTERVAL 1 DAY),' 23:59:59')";
		}
		this.setStartDate(date1);
		this.setEndDate(date2);
		logger.info("开始上传【" + date1 + "到" + date2 + "】的妇女 2-5 次随访记录信息");

		StringBuffer buffer1 = new StringBuffer();
		logger.info("开始查询妇女 2-5 次随访总记录id");
		logger.info("+++++++++++++++++++++++++++++++======SQL:");
		buffer1.append(" SELECT gp.id as gpidfive");
		buffer1.append(" FROM t_gra_return_visit grv ");
		buffer1.append(" JOIN t_gra_person AS gp ON gp.id=grv.person_id and gp.statue='0' ");
		buffer1.append(" JOIN t_arc_person AS apv ON apv.id=gp.base_info_id and apv.statue='0' ");
		buffer1.append(" LEFT JOIN t_arc_person AS apn ON apn.id=gp.husband_info_id and apn.statue='0' ");
		buffer1.append(" JOIN t_hos_doctor AS hd ON hd.user_id=grv.add_user_id ");
		buffer1.append(" JOIN t_hos_hospital AS hh ON hh.id=hd.hospital_id LEFT JOIN t_gra_build_copy copy on gp.id=copy.person_id");
		buffer1.append(" WHERE grv.statue='0' ");//and apv.card_id='370829199802031127'
		buffer1.append(" and grv.check_date >= ").append(date1);
		buffer1.append(" and grv.check_date <= ").append(date2);
		buffer1.append(" group by gp.id ");
		List<Map<String, Object>> objs = null;
		// 查询结果
		//StringBuffer buffer = new StringBuffer();
		logger.info(buffer1.toString());
		try {
			objs = jdbcTemplate.queryForList(buffer1.toString());
			doData(objs);
		} catch (DataAccessException e) {
			logger.error("查询失败",e);
		}
		return buffer1.toString();
	}

	@Override
	public String getUpdateSQL(Map<String, Object> objs) {
		return null;
	}

	@Override
	public Map<String, Object> getStateSQL(Map<String, Object> objs) {
		return null;
	}


	public Map<String,Object> getStateSQL(Map<String, Object> objs,int num) {
		String idcard = AddressUtil.toStr(objs.get("idcard"));//身份证号
		String gragrvid = AddressUtil.toStr(objs.get("gragrvid"));//业务id
		String jsondata;
		Map<String,Object > data = new HashMap<String,Object>();
		Map<String, Object> dataInJson = new HashMap<String, Object>();
		Map<String, Object> itemInJson = new HashMap<String, Object>();
		/* AppCode */
		data.put("AppCode", "3103");
		data.put("InJsonString", dataInJson);
		/* InJsonString 43 true*/
		dataInJson.put("name", AddressUtil.toStr(objs.get("name")));
		dataInJson.put("idcard", AddressUtil.toStr(objs.get("idcard")));
		dataInJson.put("machineId", "JNWFFY001");
		dataInJson.put("item", itemInJson);
		/* item */
		itemInJson.put("feilei", AddressUtil.toStr(objs.get("feilei")));//分类：未见异常：0；异常：1
		itemInJson.put("flyc", AddressUtil.toStr(objs.get("flyc")));//分类异常：填写异常内容
		itemInJson.put("fuwei", AddressUtil.toStr(objs.get("fuwei")));//腹围
		itemInJson.put("gdgd", AddressUtil.toStr(objs.get("gdgd")));//宫底高度
		itemInJson.put("grdabh", AddressUtil.toStr(objs.get("grdabh")));//个人档案编号
		//itemInJson.put("id", AddressUtil.toStr(objs.get("id")));//随访主键 id
		itemInJson.put("jkid", AddressUtil.toStr(objs.get("jkid")));//随访记录卡号：个人档案编号 +“_”+"number": 递 增
		itemInJson.put("name", AddressUtil.toStr(objs.get("name")));//姓名
		itemInJson.put("paizhao", AddressUtil.toStr(objs.get("paizhao")));//随访照片路径
		itemInJson.put("ndb", AddressUtil.toStr(objs.get("ndb")));//尿蛋白
		itemInJson.put("qtjc", AddressUtil.toStr(objs.get("qtjc")));//其他辅助检查
		if(num<=4){
			num = num+1;
			itemInJson.put("sfcs", num);//随访次数
		}else {
			itemInJson.put("sfcs", "5");//随访次数
		}

		itemInJson.put("sfrq", AddressUtil.toStr(objs.get("sfrq")));//随访日期
		itemInJson.put("sfys", AddressUtil.toStr(objs.get("sfys")));//随访医生
		itemInJson.put("sfyscode", AddressUtil.toStr(objs.get("sfyscode")));//随访医生 id
		itemInJson.put("sssq", AddressUtil.toStr(objs.get("sssq")));//医生所属社区编码
		itemInJson.put("tizhong", AddressUtil.toStr(objs.get("tizhong")));//体重
		itemInJson.put("tw", AddressUtil.toStr(objs.get("tw")));//胎位
		itemInJson.put("txl", AddressUtil.toStr(objs.get("txl")));//胎心率
		itemInJson.put("wzd", AddressUtil.toStr(objs.get("wzd")));//完整度
		itemInJson.put("xcsfrq", AddressUtil.toStr(objs.get("xcsfrq")));//下次随访日期
		itemInJson.put("xhdbz", AddressUtil.toStr(objs.get("xhdbz")));//血红蛋白
		itemInJson.put("xy", AddressUtil.toStr(objs.get("xy")));//血压
		itemInJson.put("xy1", AddressUtil.toStr(objs.get("xy1")));//血压
		itemInJson.put("yunzhou", AddressUtil.toStr(objs.get("yunzhou")));//孕周(周)
		itemInJson.put("yunzhoutian", AddressUtil.toStr(objs.get("yunzhoutian")));//孕周(天)
		itemInJson.put("zdqt", AddressUtil.toStr(objs.get("zdqt")));//指导其他
		if(num<=3){
			String zhidao = AddressUtil.toStr(objs.get("zhidao"));
			if(num==2){//2次随访  "zhidao":"0,0,0,0,0,0,0,0"
				zhidao = zhidao.substring(0,9);
			}else if(num==3){//3次随访
				zhidao = zhidao.substring(0,13);
			}
			itemInJson.put("zhidao", zhidao);//指导
		}else {
			itemInJson.put("zhidao", AddressUtil.toStr(objs.get("zhidao")));//指导
		}
		itemInJson.put("zhuanzhen", AddressUtil.toStr(objs.get("zhuanzhen")));//转诊：无：0；有：1
		itemInJson.put("zhusu", AddressUtil.toStr(objs.get("zhusu")));//主诉
		itemInJson.put("zxbzw", AddressUtil.toStr(objs.get("zxbzw")));//注销标志位
		itemInJson.put("zzjg", AddressUtil.toStr(objs.get("zzjg")));//转诊机构
		itemInJson.put("zzyy", AddressUtil.toStr(objs.get("zzyy")));//转诊原因
		itemInJson.put("zzlxr", AddressUtil.toStr(objs.get("zzlxr")));//转诊联系人
		itemInJson.put("zzlxfs", AddressUtil.toStr(objs.get("zzlxfs")));//转诊联系人电话
		itemInJson.put("zzresult", AddressUtil.toStr(objs.get("zzresult")));//转诊结果：0：到位，1：不到位
		itemInJson.put("xqxcqsc", AddressUtil.toStr(objs.get("xqxcqsc")));//免费血清学产前筛查:0:是 ,1：否 ps：第二次随访才有
		itemInJson.put("xqxcqscqk", AddressUtil.toStr(objs.get("xqxcqscqk")));//当免费血清学产前筛查为是的时候可选
		itemInJson.put("cqjcjgmc", AddressUtil.toStr(objs.get("cqjcjgmc")));//产前检查机构名称
		itemInJson.put("sffs", AddressUtil.toStr(objs.get("sffs")));//随访方式:0:门诊,1:家庭,2:电 话

		jsondata = JSON.toJSONString(data);
		logger.info("============jsondata:" + jsondata);// jsondata
		//String state = "";
		Map<String,Object> resuletMap = new HashMap<String, Object>();
		try {
			resuletMap = HttpClientUtils.getJson(jsondata);
			/*Object stateO = (Object) resuletMap.get("state");
			state = String.valueOf(stateO);*/
			resuletMap.put("idcard",idcard);
			resuletMap.put("jsondata",jsondata);
			resuletMap.put("gragrvid",gragrvid);
			logger.info("===========idcard:" + idcard );// idcard
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			data.clear();
		}
		return resuletMap;
	}


	@Override
	public void logBefore() {
		logger.info("开始上传妇女 2-5 次随访记录");
	}

	@Override
	public void logLook(int i) {
		logger.info("查询妇女 2-5 次随访记录信息" + i + "条");
	}

	@Override
	public void logAfter(JdbcTemplate jdbcTemplate,Map<String, Object> objs) {
		String newdate = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
		String idcard = (String) objs.get("idcard");
		JSONObject jsondata = (JSONObject) objs.get("jsondata");
		String gragrvid = (String) objs.get("gragrvid");
		Object stateO = (Object) objs.get("state");
		String state = String.valueOf(stateO);
		/**
		 * stateType 1.无档案或者重复档案  2.无管理卡或管理卡重复  3.参数或数据格式错误  4.已存在数据  5.其他错误  6.查询接口无该次随访数据
		 */
		Object stateTypeO = (Object) objs.get("stateType");
		String stateType = String.valueOf(stateTypeO);
		String message = (String) objs.get("message");

		//delete
		StringBuffer bf = new StringBuffer();
		bf.append("DELETE FROM t_qf_data_upload");
		bf.append(" where delete_flag='0' and business_id=");
		bf.append("'").append(gragrvid).append("' and upload_type='妇女2-5次随访记录信息(t_gra_return_visit)'");
		String sqld = bf.toString();
		logger.info("妇女 2-5 次随访记录信息更新日志库["+sqld+"]");
		jdbcTemplate.execute(sqld);
		// 更新目标库
		StringBuffer buffer = new StringBuffer();
		buffer.append("INSERT INTO t_qf_data_upload(add_time,business_id,state,stateType,upload_reason,upload_json,delete_flag,upload_type) VALUES(");
		buffer.append("'").append(newdate).append("',");
		buffer.append("'").append(gragrvid).append("',");// gragfvid
		buffer.append("'").append(state).append("',");// state
		buffer.append("'").append(stateType).append("',");// stateType
		buffer.append("'").append(message).append("',");
		buffer.append("'").append(jsondata).append("',");
		buffer.append("'").append("0").append("',");// 默认0 未删除
		buffer.append("'").append("妇女2-5次随访记录信息(t_gra_return_visit)").append("'");
		buffer.append(")"); //

		String sql = buffer.toString();
		logger.info("妇女 2-5 次随访记录信息更新日志库["+sql+"]");
		jdbcTemplate.execute(sql);
	}

	@Override
	public String getDeleteSql() {
		/*StringBuffer buffer = new StringBuffer();
		if ("TRUE".equals(getDel_flag())) {

			logger.info("开始删除已有的儿童管理卡及新生儿家庭方式记录上传信息");
			buffer.append("DELETE FROM CA_NEONATE_VISIT");
		}
		return buffer.toString();*/
		return "";
	}

	public static void main(String[] args) {
		//System.out.println(new Upload_GraTwo2Five_Visit().getSQL());
	}

}
