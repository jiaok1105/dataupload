package fuyoubaojian.qfcode.qu;

import com.alibaba.fastjson.JSON;
import fuyoubaojian.qfcode.etl.BaseTask;
import fuyoubaojian.qfcode.etl.TaskInterface;
import fuyoubaojian.qfcode.utils.AddressUtil;
import fuyoubaojian.qfcode.utils.HttpClientUtils;
import fuyoubaojian.qfcode.utils.TimeUtils;
import net.sf.json.JSONObject;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 妇女产后访视上传
 */
@Component
public class Upload_GraChAnd42Day_Ch_Visit extends BaseTask implements TaskInterface {
	private static final Logger logger = Logger.getLogger(Upload_GraChAnd42Day_Ch_Visit.class);
	private String startDate;
	private String endDate;

	private String del_flag;

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
		logger.info("开始上传【" + date1 + "到" + date2 + "】的妇女产后访视和 42 天记录信息");
		// 查询结果
		StringBuffer buffer = new StringBuffer();
		logger.info("开始查询妇女产后访视和 42 天记录信息");
		logger.info("+++++++++++++++++++++++++++++++======SQL:");
		buffer.append(" SELECT	hcall.id as grahcallid,");
		//buffer.append(" 'WL10001' as machineId,	");// 设备 id（同DealerId）,是 String
		buffer.append(" apv.card_id as idcard,	");// 身份证号码,是 String
		buffer.append(" apv.name as name,	");// 姓名,是 String
		buffer.append(" hcall.change_unit as cljg,	");// 转诊机构,是 String
		buffer.append(" hcall.change_cause as clyy,	");// 转诊原因,是 String
		buffer.append(" DATE_FORMAT(tgl.leave_hospital_date,'%Y-%m-%d') as cyrq,	");// 出院日期,是 String
		buffer.append(" if(hcall.el_taste='1' or hcall.el_taste='' or hcall.el_taste is NULL,'0','1') as elu,	");// 恶露：未见异常： 0；异常： 1,是 String
		buffer.append(" if(hcall.el_taste='2','有臭味','无') as elyc,	");// 恶露异常,是 String
		buffer.append(" if(hcall.classify='未见异常','0','0') as fenlei,	");// 分类：未见异常： 0；异常： 1,是 String
		buffer.append(" if(hcall.classify='异常',hcall.classify_other,'无') as flyc,	");// 分类异常,是 String
		buffer.append(" DATE_FORMAT(tgl.labor_date,'%Y-%m-%d') as fmsj,	");// 分娩时间,是 String
		buffer.append(" apv.id as grdabh,	");// 个人档案编号,是 String
		//buffer.append(" hcall.id as id,	");// 主键 id,是 String
		buffer.append(" copy.yc_num as jkid,	");// 单条访视 id,是 String
		buffer.append(" apv.name as name,	");// 姓名,是 String
		buffer.append(" hcall.other as qita,	");// 其他,是 String
		buffer.append(" if(hcall.breast in('1','2'),'1','0') as rufang,	");// 乳 房：未见异常：0；异常： 1,是 String
		buffer.append(" case hcall.breast when '1' then '硬结' when '2' then '红肿' else '无' end as rfyc,	");// 乳 房异常信息,是 String
		buffer.append(" DATE_FORMAT(hcall.fs_date,'%Y-%m-%d') as sfrq,	");// 随访日期,是 String
		buffer.append(" hd.name as sfys,	");// 随访医生,是 String
		buffer.append(" hcall.add_user_id as sfyscode,	");// 随访医生 id,是 String
		buffer.append(" if(hcall.fb_incision in('1','6','7','8') or hcall.fb_incision='' or hcall.fb_incision is NULL,'0','1')  as shangkou,	");// 伤 口：未见异常：0；异常： 1,是 String
		buffer.append(" case fb_incision when '2' then '红肿' when '3' then '渗液' when '4' then '裂开' when '5' then '化脓' else '无' end as skyc,	");// 伤口异常信息,是 String
		buffer.append(" hh.region_code as sssq,	");// 医生所属社区,是 String
		buffer.append(" hcall.temperature as tiwen,	");// 体 温,是 String
		buffer.append(" '1' as wzd,	");// 完整度,是 String
		buffer.append(" DATE_FORMAT(hcall.next_fs_date,'%Y-%m-%d') as xcsfrq,	");// 下次随访日期,是 String
		buffer.append(" hcall.up_blood as xy,	");// 血压,是 String
		buffer.append(" hcall.low_blood as xy1,	");// 血压,是 String
		buffer.append(" if(hcall.health='' or hcall.health is NULL,'未见异常',hcall.health) as ybjkzk,	");// 一般健康情况,是 String
		buffer.append(" if(hcall.mental='' or hcall.mental is NULL,'未见异常',hcall.mental) as ybxlzk,	");// 一般心理状况,是 String
		buffer.append(" '无' as zdqt,	");// 指导其他,是 String
		buffer.append(" if(hcall.uterus='2','异常','无') as zgyc,	");// 子宫异常,是 String
		buffer.append(" concat(if(hcall.guidance like '%个人卫生%','1','0'),if(hcall.guidance like '%心理%','1','0'),if(hcall.guidance like '%营养%','1','0'),");// 多选指导:个人卫生，心理，营养，母乳喂养，新生儿护理与喂养，其他(zzqt),是 String
		buffer.append(" if(hcall.changer='是','1','0'),'0') as zhuanzhen,	");// 转诊：无： 0，有： 1,是 String
		buffer.append(" if(hcall.uterus='1','0','1') as zigong,	");// 子 宫：未见异常：0；异常： 1,是 String
		buffer.append(" '' as zzlxr,	");// 转诊联系人,是 String
		buffer.append(" '' as zzresult,	");// 转诊结果： 0：到位，1：不到位,是 String
		buffer.append(" '' as zzlxfs,	");// 转诊联系人电话,是 String
		buffer.append(" hcall.add_user_id as czy,	");// 录入人 id,是 String
		buffer.append(" DATE_FORMAT(hcall.add_time,'%Y-%m-%d') as czrq,	");// 录入日期,是 String
		buffer.append(" '0' as sfbz,	");// 0:产后访视， 1:42 天检查,是 String
		buffer.append(" DATE_FORMAT(hcall.add_time,'%Y-%m-%d') as createdate,	");// 录入日期,是 String
		buffer.append(" hcall.update_user_id as updateid,	");// 修改人 ID,是 String
		buffer.append(" if(hcall.update_time is NULL,DATE_FORMAT(hcall.add_time,'%Y-%m-%d'),DATE_FORMAT(hcall.update_time,'%Y-%m-%d')) as updatetime,");// 修改日期,是 String
		buffer.append(" concat(if(hcall.guidance like '%个人卫生%','1','0'),',',if(hcall.guidance like '%心理%','1','0'),',',if(hcall.guidance like '%营养%','1','0'),',',if(hcall.guidance like '%母乳喂养%','1','0'),',',if(hcall.guidance like '%新生儿护理与喂养%','1','0'),',',if(hcall.guidance like '%其他%','1','0')) as zhidao");
		buffer.append(" FROM t_gra_housecall hcall	");
		buffer.append(" JOIN t_gra_person AS gp ON gp.id=hcall.person_id and gp.statue='0'	");
		buffer.append(" JOIN t_arc_person AS apv ON apv.id=gp.base_info_id and apv.statue='0'	");
		buffer.append(" JOIN t_gra_labor AS tgl ON tgl.person_id=hcall.person_id and tgl.statue='0'	");
		buffer.append(" JOIN t_hos_doctor AS hd ON hd.user_id=hcall.add_user_id	");
		buffer.append(" JOIN t_hos_hospital AS hh ON hh.id=hd.hospital_id LEFT JOIN t_gra_build_copy copy on gp.id=copy.person_id	");
		buffer.append(" WHERE hcall.statue='0' ");//and apv.card_id='370829199802031127'
		buffer.append(" and hcall.fs_date >= ").append(date1);
		buffer.append(" and hcall.fs_date <= ").append(date2);
		buffer.append(" ORDER BY hcall.add_time ASC");

		logger.info(buffer.toString());
		return buffer.toString();
	}

	@Override
	public String getUpdateSQL(Map<String, Object> objs) {
		return null;
	}


	@Override
	public Map<String,Object> getStateSQL(Map<String, Object> objs) {
		String idcard = AddressUtil.toStr(objs.get("idcard"));//身份证号
		String grahcallid = AddressUtil.toStr(objs.get("grahcallid"));//业务id
		String jsondata;
		Map<String,Object > data = new HashMap<String,Object>();
		Map<String, Object> dataInJson = new HashMap<String, Object>();
		Map<String, Object> itemInJson = new HashMap<String, Object>();
		/* AppCode */
		data.put("AppCode", "3104");
		data.put("InJsonString", dataInJson);
		/* InJsonString  45 true*/
		dataInJson.put("name", AddressUtil.toStr(objs.get("name")));
		dataInJson.put("idcard", AddressUtil.toStr(objs.get("idcard")));
		dataInJson.put("machineId", "JNWFFY001");
		dataInJson.put("item", itemInJson);
		/* item */
		itemInJson.put("cljg", AddressUtil.toStr(objs.get("cljg")));//转诊机构
		itemInJson.put("clyy", AddressUtil.toStr(objs.get("clyy")));//转诊原因
		itemInJson.put("cyrq", AddressUtil.toStr(objs.get("cyrq")));//出院日期
		itemInJson.put("elu", AddressUtil.toStr(objs.get("elu")));//恶露：未见异常：0；异常：1
		itemInJson.put("elyc", AddressUtil.toStr(objs.get("elyc")));//恶露异常
		itemInJson.put("fenlei", AddressUtil.toStr(objs.get("fenlei")));//分类：未见异常：0；异常：1
		itemInJson.put("flyc", AddressUtil.toStr(objs.get("flyc")));//分类异常
		itemInJson.put("fmsj", AddressUtil.toStr(objs.get("fmsj")));//分娩时间
		itemInJson.put("grdabh", AddressUtil.toStr(objs.get("grdabh")));//个人档案编号
		//itemInJson.put("id", AddressUtil.toStr(objs.get("id")));//主键 id
		itemInJson.put("jkid", AddressUtil.toStr(objs.get("jkid")));//单条访视 id
		itemInJson.put("name", AddressUtil.toStr(objs.get("name")));//姓名
		itemInJson.put("qita", AddressUtil.toStr(objs.get("qita")));//其他
		itemInJson.put("rfyc", AddressUtil.toStr(objs.get("rfyc")));//乳 房：未见异常：0；异常：1
		itemInJson.put("rufang", AddressUtil.toStr(objs.get("rufang")));//乳 房异常信息
		itemInJson.put("sfrq", AddressUtil.toStr(objs.get("sfrq")));//随访日期
		itemInJson.put("sfys", AddressUtil.toStr(objs.get("sfys")));//随访医生
		itemInJson.put("sfyscode", AddressUtil.toStr(objs.get("sfyscode")));//随访医生 id
		itemInJson.put("shangkou", AddressUtil.toStr(objs.get("shangkou")));//伤 口：未见异常：0；异常：1
		itemInJson.put("skyc", AddressUtil.toStr(objs.get("skyc")));//伤 口异常信息
		itemInJson.put("sssq", AddressUtil.toStr(objs.get("sssq")));//医生所属社区
		itemInJson.put("tiwen", AddressUtil.toStr(objs.get("tiwen")));//体 温
		itemInJson.put("wzd", AddressUtil.toStr(objs.get("wzd")));//完整度
		itemInJson.put("xcsfrq", AddressUtil.toStr(objs.get("xcsfrq")));//下次随访日期

		itemInJson.put("xy", AddressUtil.toStringByIn(objs.get("xy")));//血压
		itemInJson.put("xy1", AddressUtil.toStringByIn(objs.get("xy1")));//血压

		itemInJson.put("ybjkzk", AddressUtil.toStr(objs.get("ybjkzk")));//一般健康情况
		itemInJson.put("ybxlzk", AddressUtil.toStr(objs.get("ybxlzk")));//一般心理状况
		itemInJson.put("zdqt", AddressUtil.toStr(objs.get("zdqt")));//指 导其他
		itemInJson.put("zgyc", AddressUtil.toStr(objs.get("zgyc")));//子宫异常
		itemInJson.put("zhidao", AddressUtil.toStr(objs.get("zhidao")));//指导
		itemInJson.put("zhuanzhen", AddressUtil.toStr(objs.get("zhuanzhen")));//转诊：无：0，有：1
		itemInJson.put("zigong", AddressUtil.toStr(objs.get("zigong")));//子 宫：未见异常：0；异常：1
		itemInJson.put("zzlxr", AddressUtil.toStr(objs.get("zzlxr")));//转诊联系人
		itemInJson.put("zzresult", AddressUtil.toStr(objs.get("zzresult")));//转诊结果：0：到位，1：不到位
		itemInJson.put("zzlxfs", AddressUtil.toStr(objs.get("zzlxfs")));//转诊联系人电话
		itemInJson.put("czy", AddressUtil.toStr(objs.get("czy")));//录入人 id
		itemInJson.put("czrq", AddressUtil.toStr(objs.get("czrq")));//录入日期
		itemInJson.put("sfbz", AddressUtil.toStr(objs.get("sfbz")));//0:产后访视，1:42 天检查
		itemInJson.put("createdate", AddressUtil.toStr(objs.get("createdate")));//录入日期
		itemInJson.put("updateid", AddressUtil.toStr(objs.get("updateid")));//修改人 ID
		itemInJson.put("updatetime", AddressUtil.toStr(objs.get("updatetime")));//修改日期

		jsondata = JSON.toJSONString(data);
		logger.info("==============jsondata:" + jsondata);// jsondata
		//String state = "";
		Map<String,Object> resuletMap = new HashMap<String, Object>();
		try {
			resuletMap = HttpClientUtils.getJson(jsondata);
			/*Object stateO = (Object) resuletMap.get("state");
			state = String.valueOf(stateO);*/
			resuletMap.put("idcard",idcard);
			resuletMap.put("jsondata",jsondata);
			resuletMap.put("grahcallid",grahcallid);
			logger.info("===============idcard:" + idcard );// idcard
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			data.clear();
		}
		/*if(StringUtils.equals(state,"1")){//1成功  0失败  2已经存在该次随访记录,暂时不提供更新操作! 未建管理卡
			return "1";
		}else if(StringUtils.equals(state,"2")){//2已经存在该次随访记录,暂时不提供更新操作   未上传身份证号，请检查
			return "2";
		}else if(StringUtils.equals(state,"0")){//0失败
			return "0";
		}*/
		return resuletMap;
	}

	@Override
	public void logBefore() {
		logger.info("开始上传妇女产后访视和 42 天记录");
	}

	@Override
	public void logLook(int i) {
		logger.info("查询妇女产后访视和 42 天记录信息" + i + "条");
	}

	@Override
	public void logAfter(JdbcTemplate jdbcTemplate,Map<String, Object> objs) {
		String newdate = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
		String idcard = (String) objs.get("idcard");
		JSONObject jsondata = (JSONObject) objs.get("jsondata");
		String grahcallid = (String) objs.get("grahcallid");
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
		bf.append("'").append(grahcallid).append("' and upload_type='妇女产后访视记录(t_gra_housecall)'");
		String sqld = bf.toString();
		logger.info("妇女产后访视和 42 天记录更新日志库["+sqld+"]");
		jdbcTemplate.execute(sqld);
		// 更新目标库
		StringBuffer buffer = new StringBuffer();
		buffer.append("INSERT INTO t_qf_data_upload(add_time,business_id,state,stateType,upload_reason,upload_json,delete_flag,upload_type) VALUES(");
		buffer.append("'").append(newdate).append("',");
		buffer.append("'").append(grahcallid).append("',");// grahcallid
		buffer.append("'").append(state).append("',");// state
		buffer.append("'").append(stateType).append("',");// stateType
		buffer.append("'").append(message).append("',");
		buffer.append("'").append(jsondata).append("',");
		buffer.append("'").append("0").append("',");// 默认0 未删除
		buffer.append("'").append("妇女产后访视记录(t_gra_housecall)").append("'");
		buffer.append(")"); //

		String sql = buffer.toString();
		logger.info("妇女产后访视和 42 天记录更新日志库["+sql+"]");
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
		//System.out.println(new Upload_GraChAnd42Day_Ch_Visit().getSQL());
	}

}
