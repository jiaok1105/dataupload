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
 * 儿童 3-6 岁随访记录上传
 */
@Component
public class Upload_ChildThree2Six_Visit extends BaseTask implements TaskInterface {
	private static final Logger logger = Logger.getLogger(Upload_ChildThree2Six_Visit.class);
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
		logger.info("开始上传【" + date1 + "到" + date2 + "】的儿童 3-6 岁随访记录上传记录信息");
		// 查询结果
		StringBuffer buffer = new StringBuffer();
		logger.info("开始查询儿童 3-6 岁随访记录信息");
		logger.info("+++++++++++++++++++++++++++++++======SQL:");
		buffer.append(" SELECT three.id as chicheckthreeid,cp.archives_num as archivesnum,");//64
		//buffer.append(" '0' as machineId,	");    // 设备id（同DealerId）,是String
		buffer.append(" cp.card_id as idcard,	");   // 身份证号码,是String
		buffer.append(" cp.name as name,	");    // 姓名,是String
		buffer.append(" case when(three.rule_month=36) THEN '3' when (three.rule_month=48) THEN '4'	when (three.rule_month=60) THEN '5' when (three.rule_month=72) THEN '6' end as bz,	");    // 备注,是String
		buffer.append(" DATE_FORMAT(cp.birthday,'%Y-%m-%d') as csrq,	");    // 出生日期,是String
		buffer.append(" cp.hk_region as cun,	");    // 行政区划编码,是String
		buffer.append(" cp.hkaddr as cunName,	");    // 行政区划名称,是String
		buffer.append(" if(three.belly='' or three.belly is NULL or three.belly='1','0','1') as fb,	");    // 腹部：0未见异常1异常,是String
		buffer.append(" if(three.disease like '%7%','1','0') as feiyan,	");// 两次随访间患病情况(肺炎)次数,是String
		buffer.append(" if(three.disease like '%2%','1','0') as fuxie,	");// 两次随访间患病情况(腹泻)次数,是String
		buffer.append(" cp.id as grdabh,	");// 个人档案编号,是String
		buffer.append(" if(three.disease='','无',getdictext('儿童系统','体格检查-患病记录',three.disease)) as hbqt,	");//- 两次随访间患病情况其他,是String
		buffer.append(" apv.phone as jtdh,	");// 家庭电话,是String
		buffer.append(" apv.name as lxr,	");// 联系人,是String
		buffer.append(" apv.phone as lxrdh,	");// 联系人电话,是String
		buffer.append(" apv.name as mqname,	");// 母亲姓名,是String
		buffer.append(" apv.card_id as mqsfzh,	");// 母亲身份证号,是String
		//buffer.append(" cp.name as name,	");// (儿童)姓名,是String
		buffer.append(" '无' as qt,	");// 体格检查：其他,是String
		buffer.append(" cp.name as ryxm,	");// (儿童)姓名,是String
		buffer.append(" case when three.h_assess like '上%' then '0'	");
		buffer.append(" when three.h_assess like '中%' then '1'	");
		buffer.append(" when three.h_assess like '下%' then '2' else '1' end as scfl,	");// 身长分类（单选）0、上；1、中；2、下,是String
		buffer.append(" DATE_FORMAT(three.check_date,'%Y-%m-%d') as sfrq,	");// 随访日期,是String
		buffer.append(" hd.name as sfys,	");// 随访医生,是String
		buffer.append(" three.add_user_id as sfyscode,	");// 随方医生编码,是String
		buffer.append(" three.height as shenchang,	");// 身长(cm),是String
		buffer.append(" hh.text as ssdwName,	");// 所属机构名称,是String
		buffer.append(" hh.code as sssq,	");// 所属机构编码,是String
		buffer.append(" CONCAT(if(three.wh_assess ='正常','1','0'),',',if(three.wh_assess ='慢性严重营养不良','1','0'),',',if(three.wh_assess ='消瘦','1','0'),',',if(three.wh_assess = '生长迟缓','1','0'),',',if(three.wh_assess = '超重','1','0'),',',if(three.wh_assess ='肥胖','1','0')) as tgfypg,");
		//buffer.append(" when '消瘦' then '2' when '生长迟缓' then '3' when '超重' then '4' when '肥胖' then '4' else '0' end as tgfypg,	");// 体格发育评价:正常:0;低体重:1;消瘦:2;发育迟缓:3;超重:4,是String
		buffer.append(" round(three.weight/1000,3) as tizhong,	");// 体重(kg),是String
		buffer.append(" if((three.r_hearing='02' or three.l_hearing='02'),'0','1') as tl,	");// 听力：0通过，1未通过,是String
		buffer.append(" case when three.w_assess like '上%' then '0'	");
		buffer.append(" when three.w_assess like '中%' then '1'	");
		buffer.append(" when three.w_assess like '下%' then '2' else '1' end as tzfl,	");// 体重分类(单选)0、上；1、中；2、下,是String
		buffer.append(" three.l_vision as zy,	");// 左眼视力,是String
		buffer.append(" three.r_vision as yy,	");// 右眼视力,是String
		buffer.append(" '0' as waishang,	");// 两次随访间患病情况：因外伤住院,是String
		buffer.append(" '1' as wzd,	");// 完整度,是String
		buffer.append(" DATE_FORMAT(three.next_date,'%Y-%m-%d') as xcsfrq,	");// 下次随访日期,是String
		buffer.append(" if(three.chest='1' or three.chest='' or three.chest is NULL,'0','1') as xf,	");// 胸部：0未见异常，1异常,是String
		buffer.append(" '0' as xhdb,	");// 血红蛋白(g/L),是String
		buffer.append(" '0' as xhdbqt,	");// 血红蛋白其他,是String
		buffer.append(" cp.sex as xingbie,	");// 性别：1男2女,是String
		buffer.append(" cp.xz_address as xxdz,	"); // 详细地址,是String
		buffer.append(" concat(if(three.has_disease='未患病','1','0'),',',if(three.disease like '%7%','1','0'),',',if(three.disease like '%2%','1','0'),',',	");
		buffer.append(" '0',',',if(three.disease like '%99%','1','0')) as yeqhbqk,	");// 两次随访间患病情况,无，肺炎，因腹泻住院，因外伤住院，其他。,是String
		buffer.append(" three.tooth as ys,three.jc_tooth as jc,	");// 牙数/龋齿数(颗),是String
		buffer.append(" '' as zdqt,	");// 指导其他,是String
		buffer.append(" concat(if(three.guide like '%科学喂养%','1','0'),if(three.guide like '%生长发育%','1','0'),	");
		buffer.append(" if(three.guide like '%疾病预防%','1','0'),if(three.guide like '%预防伤害%','1','0'),if(three.guide like '%口腔保健%','1','0'),'0') as zhidao,	");// 指导(多选)合理膳食，生长发育，疾病预防，预防伤害，口腔保健，其他(qtzd),是String
		buffer.append(" if(three.changer in('1','是'),'1','0') as zhuanzhen,	");// 转诊：0无，1有,是String
		buffer.append(" three.change_unit as zzjg,	");// 转诊机构,是String
		buffer.append(" '' as zzqt,	");// 转诊其他,是String
		buffer.append(" three.change_cause as zzyy,	");// 转诊原因,是String
		buffer.append(" '' as ZZLXR,	");// 转诊联系人,是String
		buffer.append(" '' as ZZLXFS,	");// 转诊联系方式,是String
		buffer.append(" '' as ZZRESULT,	");// 转诊结果0：不到位1：到位,是String
		buffer.append(" DATE_FORMAT(three.check_date,'%Y-%m-%d') as CZSJ,	");// 操作时间,是String
		buffer.append(" three.add_user_id as CZY,	");// 操作人id,是String
		buffer.append(" DATE_FORMAT(three.add_time,'%Y-%m-%d') as CREATEDATE,	");// 录入日期,是String
		buffer.append(" if(three.update_user_id is NULL,three.add_user_id,three.update_user_id) as updateid,	");// 更新人id,是String
		buffer.append(" if(three.update_time is NULL,DATE_FORMAT(three.add_time,'%Y-%m-%d') , DATE_FORMAT(three.update_time,'%Y-%m-%d')) as updatetime	");// 更新时间,是String
		buffer.append(" FROM t_chi_check_three three	");
		buffer.append(" JOIN t_chi_person AS cp ON cp.id=three.person_id and cp.statue='0'	");
		buffer.append(" LEFT JOIN t_arc_person AS apv ON apv.id=cp.mother_id and apv.statue='0'	");
		buffer.append(" LEFT JOIN t_arc_person AS apn ON apn.id=cp.father_id and apn.statue='0'	");
		buffer.append(" JOIN t_hos_doctor AS hd ON hd.user_id=three.add_user_id	");
		buffer.append(" JOIN t_hos_hospital AS hh ON hh.id=hd.hospital_id	");
		buffer.append(" WHERE three.statue='0' and three.rule_month>='36' ");//and cp.card_id ='370800201801010102'
		buffer.append(" and three.check_date >= ").append(date1);
		buffer.append(" and three.check_date <= ").append(date2);

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
		String chicheckthreeid = AddressUtil.toStr(objs.get("chicheckthreeid"));//业务id
		String archivesnum = AddressUtil.toStr(objs.get("archivesnum"));//档案编号
		String jsondata;
		Map<String,Object > data = new HashMap<String,Object>();
		Map<String, Object> dataInJson = new HashMap<String, Object>();
		Map<String, Object> itemInJson = new HashMap<String, Object>();
		/* AppCode */
		data.put("AppCode", "1105");
		data.put("InJsonString", dataInJson);
		/* InJsonString   57 true*/
		dataInJson.put("name", AddressUtil.toStr(objs.get("name")));
		dataInJson.put("grdabh", AddressUtil.toStr(objs.get("archivesnum")));
		dataInJson.put("machineId", "JNWFFY001");
		dataInJson.put("item", itemInJson);
		/* item */
		itemInJson.put("bz", AddressUtil.toStr(objs.get("bz")));//备注
		itemInJson.put("csrq", AddressUtil.toStr(objs.get("csrq")));//出生日期
		itemInJson.put("cun", AddressUtil.toStr(objs.get("cun")));//行政区划编码
		itemInJson.put("cunName", AddressUtil.toStr(objs.get("cunName")));//行政区划名称
		itemInJson.put("fb", AddressUtil.toStr(objs.get("fb")));//腹部：0 未见异常1 异常
		itemInJson.put("feiyan", AddressUtil.toStr(objs.get("feiyan")));//两次随访间患病情况(肺炎)次数
		itemInJson.put("fuxie", AddressUtil.toStr(objs.get("fuxie")));//两次随访间患病情况(腹泻)次数
		itemInJson.put("grdabh", AddressUtil.toStr(objs.get("grdabh")));//个人档案编号
		itemInJson.put("hbqt", AddressUtil.toStr(objs.get("hbqt")));//两次随访间患病情况其他
		itemInJson.put("jtdh", AddressUtil.toStr(objs.get("jtdh")));//家庭电话
		itemInJson.put("lxr", AddressUtil.toStr(objs.get("lxr")));//联系人
		itemInJson.put("lxrdh", AddressUtil.toStr(objs.get("lxrdh")));//联系人电话
		itemInJson.put("mqname", AddressUtil.toStr(objs.get("mqname")));//母亲姓名
		itemInJson.put("mqsfzh", AddressUtil.toStr(objs.get("mqsfzh")));//母亲身份证号
		itemInJson.put("name", AddressUtil.toStr(objs.get("name")));//(儿童)姓名
		itemInJson.put("qt", AddressUtil.toStr(objs.get("qt")));//体格检查：其他
		itemInJson.put("ryxm", AddressUtil.toStr(objs.get("ryxm")));//(儿童)姓名
		itemInJson.put("scfl", AddressUtil.toStr(objs.get("scfl")));//身长分类（单选） 0、上；1、中；2、下
		itemInJson.put("sfrq", AddressUtil.toStr(objs.get("sfrq")));//随访日期
		itemInJson.put("sfys", AddressUtil.toStr(objs.get("sfys")));//随访医生
		itemInJson.put("sfyscode", AddressUtil.toStr(objs.get("sfyscode")));//随方医生编码
		itemInJson.put("shenchang", AddressUtil.toStr(objs.get("shenchang")));//身长(cm)
		itemInJson.put("ssdwName", AddressUtil.toStr(objs.get("ssdwName")));//所属机构名称
		itemInJson.put("sssq", AddressUtil.toStr(objs.get("sssq")));//所属机构编码
		itemInJson.put("tgfypg", AddressUtil.toStr(objs.get("tgfypg")));//体格发育评价:正常:0 ;低体重:1;消瘦:2;发 育迟缓:3;超重:4
		itemInJson.put("tizhong", AddressUtil.toStr(objs.get("tizhong")));//体重(kg)
		itemInJson.put("tl", AddressUtil.toStr(objs.get("tl")));//听力：0 通过，1 未 通过
		itemInJson.put("tzfl", AddressUtil.toStr(objs.get("tzfl")));//体重分类(单选) 0、上；1、中；2、下
		itemInJson.put("zy", AddressUtil.toStr(objs.get("zy")));//左眼视力
		itemInJson.put("yy", AddressUtil.toStr(objs.get("yy")));//右眼视力
		itemInJson.put("waishang", AddressUtil.toStr(objs.get("waishang")));//两次随访间患病情况：因外伤住院
		itemInJson.put("wzd", AddressUtil.toStr(objs.get("wzd")));//完整度
		itemInJson.put("xcsfrq", AddressUtil.toStr(objs.get("xcsfrq")));//下次随访日期
		itemInJson.put("xf", AddressUtil.toStr(objs.get("xf")));//胸部：0 未见异常，1 异常
		itemInJson.put("xhdb", AddressUtil.toStr(objs.get("xhdb")));//血红蛋白(g/L)
		itemInJson.put("xhdbqt", AddressUtil.toStr(objs.get("xhdbqt")));//血红蛋白其他
		itemInJson.put("xingbie", AddressUtil.toStr(objs.get("xingbie")));//性别：1 男 2 女
		itemInJson.put("xxdz", AddressUtil.toStr(objs.get("xxdz")));//详细地址
		itemInJson.put("yeqhbqk", AddressUtil.toStr(objs.get("yeqhbqk")));//两次随访间患病情况
		itemInJson.put("ys", AddressUtil.toStr(objs.get("ys")));//牙数/龋齿数(颗)   牙数
		itemInJson.put("jc", AddressUtil.toStr(objs.get("jc")));//牙数/龋齿数(颗)   龋齿数(颗)
		itemInJson.put("zdqt", AddressUtil.toStr(objs.get("zdqt")));//指导其他
		itemInJson.put("zhidao", AddressUtil.toStr(objs.get("zhidao")));//指导(多选)
		itemInJson.put("zhuanzhen", AddressUtil.toStr(objs.get("zhuanzhen")));//转诊：0 无，1 有
		itemInJson.put("zzjg", AddressUtil.toStr(objs.get("zzjg")));//转诊机构
		itemInJson.put("zzqt", AddressUtil.toStr(objs.get("zzqt")));//转诊其他
		itemInJson.put("zzyy", AddressUtil.toStr(objs.get("zzyy")));//转诊原因
		itemInJson.put("ZZLXR", AddressUtil.toStr(objs.get("ZZLXR")));//转诊联系人
		itemInJson.put("ZZLXFS", AddressUtil.toStr(objs.get("ZZLXFS")));//转诊联系方式
		itemInJson.put("ZZRESULT", AddressUtil.toStr(objs.get("ZZRESULT")));//转诊结果 0：不到位1：到位
		itemInJson.put("CZSJ", AddressUtil.toStr(objs.get("CZSJ")));//操作时间
		itemInJson.put("CZY", AddressUtil.toStr(objs.get("CZY")));//操作人 id
		itemInJson.put("CREATEDATE", AddressUtil.toStr(objs.get("CREATEDATE")));//录入日期
		itemInJson.put("updateid", AddressUtil.toStr(objs.get("updateid")));//更新人 id
		itemInJson.put("updatetime", AddressUtil.toStr(objs.get("updatetime")));//更新时间


		jsondata = JSON.toJSONString(data);
		logger.info("=================jsondata:" + jsondata);// jsondata
		//String state = "";
		Map<String,Object> resuletMap = new HashMap<String, Object>();
		try {
			resuletMap = HttpClientUtils.getJson(jsondata);
			/*Object stateO = (Object) resuletMap.get("state");
			state = String.valueOf(stateO);*/
			resuletMap.put("idcard",idcard);
			resuletMap.put("jsondata",jsondata);
			resuletMap.put("chicheckthreeid",chicheckthreeid);
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
		logger.info("开始上传儿童 3-6 岁随访记录");
	}

	@Override
	public void logLook(int i) {
		logger.info("查询儿童 3-6 岁随访记录信息" + i + "条");
	}

	@Override
	public void logAfter(JdbcTemplate jdbcTemplate,Map<String, Object> objs) {
		String newdate = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
		String idcard = (String) objs.get("idcard");
		JSONObject jsondata = (JSONObject) objs.get("jsondata");
		String chicheckthreeid = (String) objs.get("chicheckthreeid");
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
		bf.append("'").append(chicheckthreeid).append("' and upload_type='儿童3-6岁随访(t_chi_check_three)'");
		String sqld = bf.toString();
		logger.info("儿童 3-6 岁随访记录更新日志库["+sqld+"]");
		jdbcTemplate.execute(sqld);
		// 更新目标库
		StringBuffer buffer = new StringBuffer();
		buffer.append("INSERT INTO t_qf_data_upload(add_time,business_id,state,stateType,upload_reason,upload_json,delete_flag,upload_type) VALUES(");
		buffer.append("'").append(newdate).append("',");
		buffer.append("'").append(chicheckthreeid).append("',");// chicheckthreeid
		buffer.append("'").append(state).append("',");// state
		buffer.append("'").append(stateType).append("',");// stateType
		buffer.append("'").append(message).append("',");
		buffer.append("'").append(jsondata).append("',");
		buffer.append("'").append("0").append("',");// 默认0 未删除
		buffer.append("'").append("儿童3-6岁随访(t_chi_check_three)").append("'");
		buffer.append(")"); //

		String sql = buffer.toString();
		logger.info("儿童 3-6 岁随访记录更新日志库["+sql+"]");
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
		//System.out.println(new Upload_ChildThree2Six_Visit().getSQL());
	}

}
