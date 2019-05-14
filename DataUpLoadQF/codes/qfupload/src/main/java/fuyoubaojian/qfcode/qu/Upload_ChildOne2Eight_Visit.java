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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 儿童 1-8 月随访上传
 */
@Component
public class Upload_ChildOne2Eight_Visit extends BaseTask implements TaskInterface {
	private static final Logger logger = Logger.getLogger(Upload_ChildOne2Eight_Visit.class);
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
		logger.info("开始上传【" + date1 + "到" + date2 + "】的儿童 1-8 月随访上传记录信息");
		// 查询结果
		StringBuffer buffer = new StringBuffer();
		logger.info("开始查询儿童 1-8 月随访上传信息");
		logger.info("+++++++++++++++++++++++++++++++======SQL:");
		// 儿童1-8月随访上传
		buffer.append(" SELECT one.id as chicheckoneid,cp.archives_num as archivesnum,	");
		buffer.append(" cp.card_id as idcard ,");    // 身份证号码 ,是 ,String
		buffer.append(" cp.name as name ,	");    // 姓名 ,是 ,String
		buffer.append(" cp.hk_region as areacode ,	");    // 区域编码,是 ,String
		buffer.append(" '0' as bt ,	");    // 步态,是 ,String
		buffer.append(" DATE_FORMAT(one.check_date,'%Y-%m-%d') as createDate ,	");   // 创建日期,是 ,String
		buffer.append(" DATE_FORMAT(cp.birthday,'%Y-%m-%d') as csrq ,	");    // 出生日期,是 ,String
		buffer.append(" cp.hk_region as cun ,	");    // 所属区划编码,是 ,String
		buffer.append(" cp.hkaddr as cunName ,	");    // 所属区划名称,是 ,String
		buffer.append(" DATE_FORMAT(one.add_time,'%Y-%m-%d') as czsj ,	");    // 操作时间,是 ,String
		buffer.append(" one.add_user_id as czy ,	");    // 操作人 id,是 ,String
		buffer.append(" if((one.r_ear='' or one.r_ear is NULL or one.r_ear='1') and (one.l_ear='' or one.l_ear is NULL or one.l_ear='1'),'0','1') as er ,	");    // 耳外观： 0 未见异常1 异常,是 ,String
		buffer.append(" if(one.belly='' or one.belly is NULL or one.belly='1','0','1') as fb ,	");    // 腹部： 0 未见异常1 异常,是 ,String
		buffer.append(" if(apn.phone='' or apn.phone is NULL,'13800138000',apn.phone) as fqlxdh ,	");    // 父亲联系电话,是 ,String
		buffer.append(" apn.name as fqname ,	");    // 父亲姓名,是 ,String
		buffer.append(" if(one.evaluation='' or one.evaluation is NULL or one.evaluation='01','0','1') as fypg ,	");    // 发育评估（单选）0、通过； 1、未通过,是 ,String
		//buffer.append(" if(one.pudendum='' or one.pudendum is NULL or one.pudendum='1','0','1') as gm ,	");    // 肛门： 0 未见异常1 异常,是 ,String "
		buffer.append(" if(one.genitals in('2','3','4','5','6','7','8','9','10','11'),'1','0') as gm ,	");
		buffer.append(" cp.id as grdabh ,	");    // 个人档案编号,是 ,String
		buffer.append(" if(one.has_disease='' or one.has_disease is NULL or one.has_disease='未患病','0','1') as hbqk ,	");    // 两次随访间患病情况（单选）0、未患病； 1、患病,是 ,String"
		buffer.append(" one.field_sport as hwhd ,	");    // 户外活动(小时/日),是 ,String
		buffer.append(" cp.id as id ,	");    // id,是 ,String
		buffer.append(" if(one.neck='' or one.neck is NULL or one.neck='1','0','1') as jbbk ,	");    // 颈部包块： 0 无， 1：有,是 ,String "
		buffer.append(" apv.phone as jtdh ,	");    // 家庭电话,是 ,String
		buffer.append(" if(one.mouth='' or one.mouth is NULL or one.mouth='1','0','1') as kq ,	");    // 口腔： 0 未见异常1 异常,是 ,String "
		buffer.append(" hd.name as lrrName ,	");    // 录入人姓名,是 ,String
		buffer.append(" apv.name as lxr ,	");    // 联系人,是 ,String
		buffer.append(" apv.phone as lxrdh ,	");    // 联系人电话,是 ,String
		buffer.append(" concat(if(one.complexion='1','1','0'),',',if(one.complexion='2','1','0'),',',if(one.complexion not in('1','2'),'1','0')) as mianse ,	");    // 面色：红润，黄染，其他(msqt)(0:未选中 1:已选中),是 ,String"
		//buffer.append(" CASE WHEN one.complexion='1' THEN '0' WHEN one.complexion='2' THEN '1' ELSE '2' END as mianse,");// String,面色：红润，黄染，其他(msqt)(0:未选中 1:已选中),
		buffer.append(" apv.name as mqname ,	");    // 母亲姓名,是 ,String
		buffer.append(" apv.card_id as mqsfzh ,	");    // 母亲身份证号,是 ,String
		//buffer.append(" cp.name as name ,	");    // (儿童)姓名,是 ,String
		buffer.append(" if(one.face='' or one.face is NULL or one.face='01','0','1') as pifu ,	");    // 皮肤： 0：未见异常，1：异常,是 ,String "
		buffer.append(" concat(if(one.mesogastrium='2','2','1'),',',if(one.mesogastrium='1','2','1'),',',if(one.mesogastrium='3','2','1'),',',if(one.mesogastrium not in('1','2','3'),'2','1')) as qb ,	");   // 脐带：未脱，脱落，脐部有渗出，其他(qdqt)(0:未选中 1:已选中),是 ,String"
		buffer.append(" if((one.mesogastrium='1') ,'1','0') as qb3 ,	");   // 脐带：未脱，脱落，脐部有渗出，其他(qdqt)(0:未选中 1:已选中),是 ,String" 0未检异常  1异常
		buffer.append(" if(one.fontanelle='2','0','1') as qianxin ,	");    // 前囟（单选）0、闭合； 1、未闭,是 ,String "
		buffer.append(" one.km_size1 as qx ,	");    // 前囟(左),是 ,String
		buffer.append(" one.km_size2 as qx1 ,	");    //前囟(右),是 ,String
		buffer.append(" cp.name as ryxm ,	");    // (儿童)姓名,是 ,String
		buffer.append(" case when one.h_assess like '上%' then '0'	");
		buffer.append(" when one.h_assess like '中%' then '1'	");
		buffer.append(" when one.h_assess like '下%' then '2' else '1' end as scfl ,	");   // 身长分类（单选）0、上； 1、中； 2、下,是 ,String "
		//buffer.append(" case when TIMESTAMPDIFF(MONTH,cp.birthday,one.check_date)<='0' THEN '0'	");
		//buffer.append(" when '0'<TIMESTAMPDIFF(MONTH,cp.birthday,one.check_date) and TIMESTAMPDIFF(MONTH,cp.birthday,one.check_date)<='3' THEN '1'	");
		//buffer.append(" when '3'<TIMESTAMPDIFF(MONTH,cp.birthday,one.check_date) and TIMESTAMPDIFF(MONTH,cp.birthday,one.check_date)<='6' THEN '2'	");
		//buffer.append(" when '6'<TIMESTAMPDIFF(MONTH,cp.birthday,one.check_date) and TIMESTAMPDIFF(MONTH,cp.birthday,one.check_date)<='8' THEN '3' end as sfcs ,	");    // 随访次数(儿童健康体检类别）,是 ,String
		// 0、满月;1、 3 月;2、 6 月;3、 8 月;4、 12 月;5、18 月;6、 24 月;7、 30 月"
		buffer.append(" case when(one.rule_month=1) THEN '0' when (one.rule_month=3) THEN '1'	when (one.rule_month=6) THEN '2' when (one.rule_month=8) THEN '3' end as sfcs ,");
		buffer.append(" DATE_FORMAT(one.check_date,'%Y-%m-%d') as sfrq ,	");    // 随访日期,是 ,String
		buffer.append(" hd.name as sfys ,	");    // 随访医生,是 ,String
		buffer.append(" one.height as shenchang ,	");    // 身长(cm),是 ,String
		buffer.append(" cp.hkaddr as ssdwName ,	");    // 所属区划名称,是 ,String
		buffer.append(" cp.hk_region as sssq ,	");    // 所属区划编码,是 ,String
		buffer.append(" if(one.limb='' or one.limb is NULL or one.limb='1','0','1') as sz ,	");    // 四肢： 0 未见异常， 1异常,是 ,String "
		buffer.append(" round(one.weight/1000,3) as tizhong ,	");    // 体重(kg),是 ,String
		buffer.append(" if((one.r_hearing='02' or one.l_hearing='02'),'1','0') as tl ,	");    // 听力： 0 通过， 1 未通过,是 ,String "
		buffer.append(" one.head_size as tw ,	");    // 头围(cm),是 ,String
		buffer.append(" case when one.w_assess like '上%' then '0'	");
		buffer.append(" when one.w_assess like '中%' then '1'	");
		buffer.append(" when one.w_assess like '下%' then '2' else '1' end as tzfl ,	");    // 体重分类(单选)0、上； 1、中； 2、下,是 ,String "
		buffer.append(" one.vitd as wss ,	");    // 服用维生素 D(IU/日),是 ,String
		buffer.append(" if(one.genitals in('2','3','4','5','6','7','8','9','10','11'),'1','0') as wszq ,	");    // 外生殖器： 0 未见异常， 1 异常,是 ,String "
		buffer.append(" '1' as wzd ,	");    // 完整度,是 ,String
		buffer.append(" DATE_FORMAT(one.next_date,'%Y-%m-%d') as xcsfrq ,	");    // 下次随访日期,是 ,String
		buffer.append(" if(one.heart='' or one.heart is NULL or one.heart='1','0','1') as xf ,	");    // 心肺： 0 未见异常， 1异常,是 ,String "
		buffer.append(" cp.sex as xingbie ,	");    // 性别： 1 男 2 女,是 ,String
		buffer.append(" cp.xz_address as xxdz ,	");    // 详细地址,是 ,String
		buffer.append(" if((one.r_eye='' or one.r_eye is NULL or one.r_eye='1') and (one.l_eye='' or one.l_eye is NULL or one.l_eye='1'),'0','1') as yan ,	");    // 眼睛： 0 未见异常， 1异常,是 ,String "
		buffer.append(" concat(if(one.guide='1','1','0'),if(one.guide='2','1','0'),	");
		buffer.append(" if(one.guide='3','1','0'),if(one.guide='4','1','0'),if(one.guide='5','1','0'),'0') as zhidao ,	");
		// 指导(多选),是 ,String 科学喂养，生长发育，疾病预防，预防伤害，口腔保健，其他(qtzd)
		buffer.append(" if(one.changer in('1','是'),'1','0') as zhuanzhen ,	");    // 转诊： 0 无， 1 有,是 ,String
		buffer.append(" '0' as zxbzw 	");   // 注销标志位 0：未注销， 1：注销,是 ,String
		buffer.append(" FROM t_chi_check_one one	");
		buffer.append(" JOIN t_chi_person AS cp ON cp.id=one.person_id and cp.statue='0'	");
		buffer.append(" LEFT JOIN t_arc_person AS apv ON apv.id=cp.mother_id and apv.statue='0'	");
		buffer.append(" LEFT JOIN t_arc_person AS apn ON apn.id=cp.father_id and apn.statue='0'	");
		buffer.append(" JOIN t_hos_doctor AS hd ON hd.user_id=one.add_user_id	");
		buffer.append(" JOIN t_hos_hospital AS hh ON hh.id=hd.hospital_id	");
		//buffer.append(" WHERE one.statue='0' and TIMESTAMPDIFF(MONTH,cp.birthday,one.check_date)<='8'");//and cp.card_id ='370800201801010102'
		buffer.append(" WHERE one.statue='0' and one.rule_month<='8'");//and cp.card_id ='370800201801010102'
		buffer.append(" and one.check_date >= ").append(date1);
		buffer.append(" and one.check_date <= ").append(date2);
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
		String chicheckoneid = AddressUtil.toStr(objs.get("chicheckoneid"));//业务id
		String archivesnum = AddressUtil.toStr(objs.get("archivesnum"));//档案编号
		String jsondata;
		Map<String,Object > data = new HashMap<String,Object>();
		Map<String, Object> dataInJson = new HashMap<String, Object>();
		Map<String, Object> itemInJson = new HashMap<String, Object>();
		/* AppCode */
		data.put("AppCode", "1103");
		data.put("InJsonString", dataInJson);
		/* InJsonString 60 true*/
		dataInJson.put("name", AddressUtil.toStr(objs.get("name")));
		dataInJson.put("grdabh", AddressUtil.toStr(objs.get("archivesnum")));
		dataInJson.put("machineId", "JNWFFY001");
		dataInJson.put("item", itemInJson);
		/* item */
		itemInJson.put("areacode", AddressUtil.toStr(objs.get("areacode")));
		itemInJson.put("bt", AddressUtil.toStr(objs.get("bt")));
		itemInJson.put("createDate", AddressUtil.toStr(objs.get("createDate")));
		itemInJson.put("csrq", AddressUtil.toStr(objs.get("csrq")));
		itemInJson.put("cun", AddressUtil.toStr(objs.get("cun")));
		itemInJson.put("cunName", AddressUtil.toStr(objs.get("cunName")));
		itemInJson.put("czsj", AddressUtil.toStr(objs.get("czsj")));
		itemInJson.put("czy", AddressUtil.toStr(objs.get("czy")));
		itemInJson.put("er", AddressUtil.toStr(objs.get("er")));
		itemInJson.put("fb", AddressUtil.toStr(objs.get("fb")));
		itemInJson.put("fqlxdh", AddressUtil.toStr(objs.get("fqlxdh")));
		itemInJson.put("fqname", AddressUtil.toStr(objs.get("fqname")));
		itemInJson.put("fypg", AddressUtil.toStr(objs.get("fypg")));
		itemInJson.put("gm", AddressUtil.toStr(objs.get("gm")));
		itemInJson.put("grdabh", AddressUtil.toStr(objs.get("grdabh")));
		itemInJson.put("hbqk", AddressUtil.toStr(objs.get("hbqk")));
		itemInJson.put("hwhd", AddressUtil.toStr(objs.get("hwhd")));
		itemInJson.put("id", AddressUtil.toStr(objs.get("id")));
		itemInJson.put("jbbk", AddressUtil.toStr(objs.get("jbbk")));
		itemInJson.put("jtdh", AddressUtil.toStr(objs.get("jtdh")));
		itemInJson.put("kq", AddressUtil.toStr(objs.get("kq")));
		itemInJson.put("lrrName", AddressUtil.toStr(objs.get("lrrName")));
		itemInJson.put("lxr", AddressUtil.toStr(objs.get("lxr")));
		itemInJson.put("lxrdh", AddressUtil.toStr(objs.get("lxrdh")));
		itemInJson.put("mianse", AddressUtil.toStr(objs.get("mianse")));
		itemInJson.put("mqname", AddressUtil.toStr(objs.get("mqname")));
		itemInJson.put("mqsfzh", AddressUtil.toStr(objs.get("mqsfzh")));
		itemInJson.put("name", AddressUtil.toStr(objs.get("name")));
		itemInJson.put("pifu", AddressUtil.toStr(objs.get("pifu")));
		String sfcs = AddressUtil.toStr(objs.get("sfcs"));
		if(StringUtils.equals(sfcs, "0")){
			itemInJson.put("qb", AddressUtil.toStr(objs.get("qb")));
		}else if(StringUtils.equals(sfcs, "1")){
			itemInJson.put("qb", AddressUtil.toStr(objs.get("qb3")));
		}else{
			itemInJson.put("qb", "");
		}
		//itemInJson.put("qb", AddressUtil.toStr(objs.get("qb")));
		itemInJson.put("qianxin", AddressUtil.toStr(objs.get("qianxin")));
		itemInJson.put("qx", AddressUtil.toStr(objs.get("qx")));
		itemInJson.put("qx1", AddressUtil.toStr(objs.get("qx1")));
		itemInJson.put("ryxm", AddressUtil.toStr(objs.get("ryxm")));
		itemInJson.put("scfl", AddressUtil.toStr(objs.get("scfl")));
		itemInJson.put("sfcs", AddressUtil.toStr(objs.get("sfcs")));
		itemInJson.put("sfrq", AddressUtil.toStr(objs.get("sfrq")));
		itemInJson.put("sfys", AddressUtil.toStr(objs.get("sfys")));
		itemInJson.put("shenchang", AddressUtil.toStr(objs.get("shenchang")));
		itemInJson.put("ssdwName", AddressUtil.toStr(objs.get("ssdwName")));
		itemInJson.put("sssq", AddressUtil.toStr(objs.get("sssq")));
		itemInJson.put("sz", AddressUtil.toStr(objs.get("sz")));
		itemInJson.put("tizhong", AddressUtil.toStr(objs.get("tizhong")));
		itemInJson.put("tl", AddressUtil.toStr(objs.get("tl")));
		itemInJson.put("tw", AddressUtil.toStr(objs.get("tw")));
		itemInJson.put("tzfl", AddressUtil.toStr(objs.get("tzfl")));
		itemInJson.put("wss", AddressUtil.toStr(objs.get("wss")));
		itemInJson.put("wszq", AddressUtil.toStr(objs.get("wszq")));
		itemInJson.put("wzd", AddressUtil.toStr(objs.get("wzd")));
		itemInJson.put("xcsfrq", AddressUtil.toStr(objs.get("xcsfrq")));
		itemInJson.put("xf", AddressUtil.toStr(objs.get("xf")));
		itemInJson.put("xingbie", AddressUtil.toStr(objs.get("xingbie")));
		itemInJson.put("xxdz", AddressUtil.toStr(objs.get("xxdz")));
		itemInJson.put("yan", AddressUtil.toStr(objs.get("yan")));
		itemInJson.put("zhidao", AddressUtil.toStr(objs.get("zhidao")));
		itemInJson.put("zhuanzhen", AddressUtil.toStr(objs.get("zhuanzhen")));
		itemInJson.put("zxbzw", AddressUtil.toStr(objs.get("zxbzw")));

		jsondata = JSON.toJSONString(data);
		logger.info("================jsondata:" + jsondata);// jsondata
		//String state = "";
		Map<String,Object> resuletMap = new HashMap<String, Object>();
		try {
			resuletMap = HttpClientUtils.getJson(jsondata);
			/*Object stateO = (Object) resuletMap.get("state");
			state = String.valueOf(stateO);*/
			resuletMap.put("idcard",idcard);
			resuletMap.put("jsondata",jsondata);
			resuletMap.put("chicheckoneid",chicheckoneid);
			logger.info("============idcard:" + idcard + "archivesnum:"+archivesnum);// idcard
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
		logger.info("开始上传儿童 1-8 月随访");
	}

	@Override
	public void logLook(int i) {
		logger.info("查询儿童 1-8 月随访上传信息" + i + "条");
	}

	@Override
	public void logAfter(JdbcTemplate jdbcTemplate,Map<String, Object> objs) {
		String newdate = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
		String idcard = (String) objs.get("idcard");
		JSONObject jsondata = (JSONObject) objs.get("jsondata");
		String chicheckoneid = (String) objs.get("chicheckoneid");
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
		bf.append("'").append(chicheckoneid).append("' and upload_type='儿童1-8月随访信息(t_chi_check_one)'");
		String sqld = bf.toString();
		logger.info("儿童 1-8 月随访信息更新日志库["+sqld+"]");
		jdbcTemplate.execute(sqld);
		// 更新目标库
		StringBuffer buffer = new StringBuffer();
		buffer.append("INSERT INTO t_qf_data_upload(add_time,business_id,state,stateType,upload_reason,upload_json,delete_flag,upload_type) VALUES(");
		buffer.append("'").append(newdate).append("',");
		buffer.append("'").append(chicheckoneid).append("',");// chipersonid
		buffer.append("'").append(state).append("',");// state
		buffer.append("'").append(stateType).append("',");// stateType
		buffer.append("'").append(message).append("',");
		buffer.append("'").append(jsondata).append("',");
		buffer.append("'").append("0").append("',");// 默认0 未删除
		buffer.append("'").append("儿童1-8月随访信息(t_chi_check_one)").append("'");
		buffer.append(")"); //

		String sql = buffer.toString();
		logger.info("儿童 1-8 月随访信息更新日志库["+sql+"]");
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
		//System.out.println(new Upload_ChildOne2Eight_Visit().getSQL());
	}

}
