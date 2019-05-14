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
 * 儿童 12-30 月随访记录上传
 */
@Component
public class Upload_ChildTwelve2Thirty_Visit extends BaseTask implements TaskInterface {
	private static final Logger logger = Logger.getLogger(Upload_ChildTwelve2Thirty_Visit.class);
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
		logger.info("开始上传【" + date1 + "到" + date2 + "】的儿童 12-30 月随访记录信息");
		// 查询结果
		StringBuffer buffer = new StringBuffer();
		logger.info("开始查询儿童 12-30 月随访记录信息");
		logger.info("+++++++++++++++++++++++++++++++======SQL:");
		buffer.append(" SELECT	two.id as chichecktwoid,cp.archives_num as archivesnum,");
		//buffer.append(" '0' as machineId,	");// 设备 id（同DealerId） ,是 ,String
		buffer.append(" cp.card_id as idcard,	");// 身份证号码 ,是 ,String
		buffer.append(" cp.name as name,	");// 姓名 ,是 ,String
		buffer.append(" '0' as item,	");// 数据项 ,是 ,Json
		buffer.append(" DATE_FORMAT(cp.birthday,'%Y-%m-%d') as csrq,	");// 出生日期,是 ,String
		buffer.append(" cp.hk_region as cun,	");// 行政区划编码,是 ,String
		buffer.append(" cp.hkaddr as cunName,	");// 行政区划名称,是 ,String
		buffer.append(" two.tooth as cys,	");// 出牙数,是 ,String
		buffer.append(" if((two.r_ear='' or two.r_ear is NULL or two.r_ear='1') and (two.l_ear='' or two.l_ear is NULL or two.l_ear='1'),'0','1') as er,	");// 耳 0；未见异常 1：异常,是 ,String
		buffer.append(" if(two.belly='' or two.belly is NULL or two.belly='1','0','1') as fb,	");// 腹部 0；未见异常1：异常,是 ,String
		buffer.append(" if(two.walking='1' or two.walking='' or two.walking is NULL,'0','1') as fypg,	");// 运动发育评估0:通过， 1：未通过,是 ,String
		buffer.append(" concat(if(two.rickets_sign='','1','0'),',',if(two.rickets_sign like '4','1','0'),',',if(two.rickets_sign like '5','1','0'),',',	");
		buffer.append(" if(two.rickets_sign like '5','1','0'),',',if(two.rickets_sign like '7','1','0'),',',if(two.rickets_sign like '8','1','0'),','	");
		buffer.append(" ,if(two.rickets_sign like '9','1','0'),',',if(two.rickets_sign like '10','1','0')) as glbtz,	");//佝偻病体征:无,肋串珠，肋软骨沟，鸡胸，手足镯， O型腿， X型腿,是 ,String
		buffer.append(" case when rickets_symptom like '%1%' then '夜惊' when rickets_symptom like '%2%' then '多汗' when rickets_symptom like '%3%' then '烦躁' else NULL end as glbzz,	");// 可疑佝偻病症状,是 ,String
		//buffer.append(" if(two.genitals='' or two.genitals is NULL or two.genitals='1','0','1') as gm,	");// 肛门/外生殖器：0 未见异常1 异常,是 ,String
		buffer.append(" if(two.genitals in('2','3','4','5','6','7','8','9','10','11'),'1','0') as gm ,	");    // 外生殖器： 0 未见异常， 1 异常,是 ,String "
		buffer.append(" if(two.has_disease='' or two.has_disease is NULL or two.has_disease='未患病','0','1') as hbqk,	");// 两次随访间患病情况（单选）0、未患病； 1、患病,是 ,String"
		buffer.append(" '0' as hbqt,	");// 两次随访间患病情况其他,是 ,String "
		buffer.append(" two.field_sport as hwhd,	");// 户外活动(小时/日),是 ,String
		buffer.append(" two.id as id,	");// id,是 ,String
		buffer.append(" apv.phone as jtdh,	");// 家庭电话,是 ,String
		buffer.append(" '0' as kq,	");// 口腔： 0 未见异常1 异常,是 ,String "
		buffer.append(" apv.name as lxr,	");// 联系人,是 ,String
		buffer.append(" apv.phone as lxrdh,	");// 联系人电话,是 ,String
		buffer.append(" concat(if(two.complexion='1','1','0'),',',if(two.complexion='2','1','0'),',',if(two.complexion not in('1','2'),'1','0')) as mianse,	");// 面色：红润，黄染，其他(msqt)(0:未选中 1:已选中),是 ,String"
		//buffer.append(" CASE WHEN two.complexion='1' THEN '0' WHEN two.complexion='2' THEN '1' ELSE '2' END as mianse,");// String,面色：红润，黄染，其他(msqt)(0:未选中 1:已选中),
		buffer.append(" apv.name as mqname,	");// 母亲姓名,是 ,String
		//buffer.append(" cp.name as name,	");// (儿童)姓名,是 ,String
		buffer.append(" '无' as paizhao,	");// 照片
		buffer.append(" if(two.face='' or two.face is NULL or two.face='01','0','1') as pifu,	");// 皮肤： 0：未见异常，1：异常,是 ,String "
		buffer.append(" if(two.fontanelle='2','1','0') as qianxin,	");// 前囟（单选）0、闭合； 1、未闭,是 ,String "
		buffer.append(" two.km_size1 as qx,	");// 前囟(左),是 ,String
		buffer.append(" two.km_size1 as qx1,	");// 前囟(右),是 ,String
		buffer.append(" two.jc_tooth as qzs,	");// 出牙/龋齿数(颗),是 ,String
		buffer.append(" cp.name as ryxm,	");// (儿童)姓名,是 ,String
		buffer.append(" case when two.h_assess like '上%' then '0'	");
		buffer.append(" when two.h_assess like '中%' then '1'	");
		buffer.append(" when two.h_assess like '下%' then '2' else '1' end as scfl,	");// 身长分类（单选）0、上； 1、中； 2、下,是 ,String "
		//buffer.append(" case when '12'<=TIMESTAMPDIFF(MONTH,cp.birthday,two.check_date) and TIMESTAMPDIFF(MONTH,cp.birthday,two.check_date)<'18' THEN '4'	");
		//buffer.append(" when '18'<=TIMESTAMPDIFF(MONTH,cp.birthday,two.check_date) and TIMESTAMPDIFF(MONTH,cp.birthday,two.check_date)<'24' THEN '5'	");
		//buffer.append(" when '24'<=TIMESTAMPDIFF(MONTH,cp.birthday,two.check_date) and TIMESTAMPDIFF(MONTH,cp.birthday,two.check_date)<='30' THEN '6' end  as sfcs,	");//  随访次数(儿童健康体检类别）,是 ,String 0、满月;1、 3 月;2、 6 月;3、 8 月;4、 12 月;5、18 月;6、 24 月;7、 30 月
		buffer.append(" case when(two.rule_month=12) THEN '4' when (two.rule_month=18) THEN '5'	when (two.rule_month=24) THEN '6' when (two.rule_month=30) THEN '7' end as sfcs ,");
		buffer.append(" DATE_FORMAT(two.check_date,'%Y-%m-%d') as sfrq ,	");// 随访日期,是 ,String
		buffer.append(" hd.name as sfys ,	");// 随访医生,是 ,String
		buffer.append(" two.add_user_id as sfyscode ,	");// 随方医生编码,是 ,String
		buffer.append(" two.height as shenchang ,	");// 身长(cm),是 ,String
		buffer.append(" hh.text as ssdwName ,	");// 所属机构名称,是 ,String
		buffer.append(" hh.code as sssq ,	");// 所属机构编码,是 ,String
		buffer.append(" if(two.limb='' or two.limb is NULL or two.limb='1','0','1') as sz ,	");// 四肢： 0 未见异常， 1异常,是 ,String "
		buffer.append(" round(two.weight/1000,3) as tizhong ,	");// 体重(kg),是 ,String
		buffer.append(" if((two.r_hearing='02' or two.l_hearing='02'),'1','0') as tl ,	");// 听力： 0 通过， 1 未通过,是 ,String "
		buffer.append(" case when two.w_assess like '上%' then '0'	");
		buffer.append(" when two.w_assess like '中%' then '1'	");
		buffer.append(" when two.w_assess like '下%' then '2' else '1' end as tzfl ,	");// 体重分类(单选)0、上； 1、中； 2、下,是 ,String "
		buffer.append(" two.vitd as wss ,	");// 服用维生素 D(IU/日),是 ,String
		buffer.append(" if(two.genitals in('2','3','4','5','6','7','8','9','10','11'),'1','0') as wszq ,	");// 外生殖器： 0 未见异常， 1 异常,是 ,String "
		buffer.append(" if(two.heart='' or two.heart is NULL or two.heart='1','0','1') as xf ,	");// 胸部： 0 未见异常， 1异常,是 ,String "
		buffer.append(" cp.sex as xingbie ,	");// 性别： 1 男 2 女,是 ,String
		buffer.append(" cp.xz_address as xxdz ,	");// 详细地址,是 ,String
		buffer.append(" if((two.r_eye='' or two.r_eye is NULL or two.r_eye='1') and (two.l_eye='' or two.l_eye is NULL or two.l_eye='1'),'0','1') as yan ,	");// 眼睛： 0 未见异常， 1异常,是 ,String "
		//buffer.append(" concat(if(two.guide='1','1','0'),if(two.guide='2','1','0'),	");
		buffer.append(" concat(if(two.guide like '%科学喂养%','1','0'),',',if(two.guide like '%生长发育%','1','0'),',',if(two.guide like '%疾病预防%','1','0'),',',	");
		buffer.append(" if(two.guide like '%预防伤害%','1','0'),',',if(two.guide like '%口腔保健%','1','0'),',',if(two.guide like '%其他%','1','0')) as zhidao,	");//指导(多选),是 ,String 科学喂养，生长发育，疾病预防，预防伤害，口腔保健，其他(qtzd)
		//buffer.append(" if(two.changer in('1','是'),'1','0') as zhidao ,	");// 指导(多选),是 ,String
		buffer.append(" if(two.changer in('1','是'),'1','0') as zhuanzhen ,	");// 转诊 0：无； 1：有；,是 ,String "
		buffer.append(" two.change_unit as zzjg ,	");// 转诊机构,是 ,String
		buffer.append(" '' as zzqt ,	");// 转诊其他,是 ,String
		buffer.append(" two.change_cause as zzyy ,	");// 转诊原因,是 ,String
		buffer.append(" '' as ZZLXR ,	");// 转诊联系人,是 ,String
		buffer.append(" '' as ZZLXFS ,	");// 转诊联系方式,是 ,String
		buffer.append(" '' as ZZRESULT ,	");// 转诊结果 0：不到位1：到位,是 ,String "
		buffer.append(" DATE_FORMAT(two.check_date,'%Y-%m-%d') as CZSJ ,	");// 操作时间,是 ,String
		buffer.append(" two.add_user_id as CZY ,	");// 操作人 id,是 ,String
		buffer.append(" DATE_FORMAT(two.add_time,'%Y-%m-%d') as CREATEDATE ,	");// 录入日期,是 ,String
		buffer.append(" two.update_user_id as updateid ,	");// 更新人 id,是 ,String
		buffer.append(" if(two.update_time is NULL, DATE_FORMAT(two.add_time,'%Y-%m-%d'), DATE_FORMAT(two.update_time,'%Y-%m-%d')) as updatetime,");// 更新时间,是 ,String
		buffer.append(" if(two.walking='2','1','0') as bt,");// 步态 0：未见异常；1：异常
		buffer.append(" '0' as xhdb,");
		buffer.append(" '无' as zdqt");// 指导其他,是,String
		buffer.append(" FROM t_chi_check_two two	");
		buffer.append(" JOIN t_chi_person AS cp ON cp.id=two.person_id and cp.statue='0'	");
		buffer.append(" LEFT JOIN t_arc_person AS apv ON apv.id=cp.mother_id and apv.statue='0'	");
		buffer.append(" LEFT JOIN t_arc_person AS apn ON apn.id=cp.father_id and apn.statue='0'	");
		buffer.append(" JOIN t_hos_doctor AS hd ON hd.user_id=two.add_user_id	");
		buffer.append(" JOIN t_hos_hospital AS hh ON hh.id=hd.hospital_id	");
		//buffer.append(" WHERE two.statue='0' and '12'<=TIMESTAMPDIFF(MONTH,cp.birthday,two.check_date)<='30' and cp.card_id ='370800201801010102'	");//and cp.card_id ='370800201801010102'
		buffer.append(" WHERE two.statue='0' and '12'<=two.rule_month<='30' 	");//and cp.card_id ='370800201801010102'
		buffer.append(" and two.check_date >= ").append(date1);
		buffer.append(" and two.check_date <= ").append(date2);

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
		String chichecktwoid = AddressUtil.toStr(objs.get("chichecktwoid"));//业务id
		String archivesnum = AddressUtil.toStr(objs.get("archivesnum"));//档案编号
		String jsondata;
		Map<String,Object > data = new HashMap<String,Object>();
		Map<String, Object> dataInJson = new HashMap<String, Object>();
		Map<String, Object> itemInJson = new HashMap<String, Object>();
		/* AppCode */
		data.put("AppCode", "1104");
		data.put("InJsonString", dataInJson);
		/* InJsonString  65 true*/
		dataInJson.put("name", AddressUtil.toStr(objs.get("name")));
		dataInJson.put("grdabh", AddressUtil.toStr(objs.get("archivesnum")));
		dataInJson.put("machineId", "JNWFFY001");
		dataInJson.put("item", itemInJson);
		/* item */
		itemInJson.put("csrq", AddressUtil.toStr(objs.get("csrq")));//出生日期
		itemInJson.put("cun", AddressUtil.toStr(objs.get("cun")));//行政区划编码
		itemInJson.put("cunName", AddressUtil.toStr(objs.get("cunName")));//行政区划名称
		itemInJson.put("cys", AddressUtil.toStr(objs.get("cys")));//出牙数
		itemInJson.put("er", AddressUtil.toStr(objs.get("er")));//耳 0；未见异常 1：异常
		itemInJson.put("fb", AddressUtil.toStr(objs.get("fb")));//腹部 0；未见异常1：异常
		itemInJson.put("fypg", AddressUtil.toStr(objs.get("fypg")));//运动发育评估0:通过，1：未通过
		itemInJson.put("glbtz", AddressUtil.toStr(objs.get("glbtz")));//佝偻病体征
		itemInJson.put("glbzz", AddressUtil.toStr(objs.get("glbzz")));//可疑佝偻病症状
		itemInJson.put("gm", AddressUtil.toStr(objs.get("gm")));//肛门/外生殖器：0 未见异常 1 异常
		itemInJson.put("hbqk", AddressUtil.toStr(objs.get("hbqk")));//两次随访间患病情况（单选）0、未患病；1、患病
		itemInJson.put("hbqt", AddressUtil.toStr(objs.get("hbqt")));//两次随访间患病情况其他
		itemInJson.put("hwhd", AddressUtil.toStr(objs.get("hwhd")));//户外活动(小时/日)
		itemInJson.put("id", AddressUtil.toStr(objs.get("id")));//id
		itemInJson.put("jtdh", AddressUtil.toStr(objs.get("jtdh")));//家庭电话
		itemInJson.put("kq", AddressUtil.toStr(objs.get("kq")));//口腔：0 未见异常1 异常
		itemInJson.put("lxr", AddressUtil.toStr(objs.get("lxr")));//联系人
		itemInJson.put("lxrdh", AddressUtil.toStr(objs.get("lxrdh")));//联系人电话
		itemInJson.put("mianse", AddressUtil.toStr(objs.get("mianse")));//面色：红润，黄染，其他(msqt) (0:未选中 1:已选中)
		itemInJson.put("mqname", AddressUtil.toStr(objs.get("mqname")));//母亲姓名
		itemInJson.put("name", AddressUtil.toStr(objs.get("name")));//(儿童)姓名
		itemInJson.put("paizhao", AddressUtil.toStr(objs.get("paizhao")));//照片
		itemInJson.put("pifu", AddressUtil.toStr(objs.get("pifu")));//皮肤：0：未见异常，1：异常
		itemInJson.put("qianxin", AddressUtil.toStr(objs.get("qianxin")));//前囟（单选）0、闭合；1、未闭
		itemInJson.put("qx", AddressUtil.toStr(objs.get("qx")));//前囟(左)
		itemInJson.put("qx1", AddressUtil.toStr(objs.get("qx1")));//前囟(右)
		itemInJson.put("qzs", AddressUtil.toStr(objs.get("qzs")));//出牙/龋齿数(颗)
		itemInJson.put("ryxm", AddressUtil.toStr(objs.get("ryxm")));//(儿童)姓名
		itemInJson.put("scfl", AddressUtil.toStr(objs.get("scfl")));//身长分类（单选）0、上；1、中；2、下
		itemInJson.put("sfcs", AddressUtil.toStr(objs.get("sfcs")));//随访次数(儿童健康体检类别）
		itemInJson.put("sfrq", AddressUtil.toStr(objs.get("sfrq")));// 随访日期
		itemInJson.put("sfys", AddressUtil.toStr(objs.get("sfys")));//随访医生
		itemInJson.put("sfyscode", AddressUtil.toStr(objs.get("sfyscode")));//随方医生编码
		itemInJson.put("shenchang", AddressUtil.toStr(objs.get("shenchang")));//身长(cm)
		itemInJson.put("ssdwName", AddressUtil.toStr(objs.get("ssdwName")));//所属机构名称
		itemInJson.put("sssq", AddressUtil.toStr(objs.get("sssq")));//所属机构编码
		itemInJson.put("sz", AddressUtil.toStr(objs.get("sz")));//四肢：0 未见异常，1异常
		itemInJson.put("tizhong", AddressUtil.toStr(objs.get("tizhong")));//体重(kg)
		itemInJson.put("tl", AddressUtil.toStr(objs.get("tl")));//听力：0 通过，1 未通过
		itemInJson.put("tzfl", AddressUtil.toStr(objs.get("tzfl")));//体重分类(单选)0、上；1、中；2、下
		itemInJson.put("wss", AddressUtil.toStr(objs.get("wss")));//服用维生素 D(IU/日)
		itemInJson.put("wszq", AddressUtil.toStr(objs.get("wszq")));//外生殖器：0 未见异常，1 异常
		itemInJson.put("xf", AddressUtil.toStr(objs.get("xf")));//胸部：0 未见异常，1异常
		itemInJson.put("xingbie", AddressUtil.toStr(objs.get("xingbie")));//性别：1 男 2 女
		itemInJson.put("xxdz", AddressUtil.toStr(objs.get("xxdz")));//详细地址
		itemInJson.put("yan", AddressUtil.toStr(objs.get("yan")));//眼睛：0 未见异常，1异常
		itemInJson.put("zhidao", AddressUtil.toStr(objs.get("zhidao")));//指导(多选)
		itemInJson.put("zdqt", AddressUtil.toStr(objs.get("zdqt")));//指导其他
		itemInJson.put("zhuanzhen", AddressUtil.toStr(objs.get("zhuanzhen")));//转诊 0：无； 1：有
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
		itemInJson.put("bt", AddressUtil.toStr(objs.get("bt")));//步态 0：未见异常；1：异常
		itemInJson.put("xhdb", AddressUtil.toStr(objs.get("xhdb")));//血红蛋白(g/L)


		jsondata = JSON.toJSONString(data);
		logger.info("=============jsondata:" + jsondata);// jsondata
		//String state = "";
		Map<String,Object> resuletMap = new HashMap<String, Object>();
		try {
			resuletMap = HttpClientUtils.getJson(jsondata);
			/*Object stateO = (Object) resuletMap.get("state");
			state = String.valueOf(stateO);*/
			resuletMap.put("idcard",idcard);
			resuletMap.put("jsondata",jsondata);
			resuletMap.put("chichecktwoid",chichecktwoid);
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
		logger.info("开始上传儿童 12-30 月随访记录");
	}

	@Override
	public void logLook(int i) {
		logger.info("查询儿童 12-30 月随访记录上传信息" + i + "条");
	}

	@Override
	public void logAfter(JdbcTemplate jdbcTemplate,Map<String, Object> objs) {
		String newdate = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
		String idcard = (String) objs.get("idcard");
		JSONObject jsondata = (JSONObject) objs.get("jsondata");
		String chichecktwoid = (String) objs.get("chichecktwoid");
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
		bf.append("'").append(chichecktwoid).append("' and upload_type='儿童12-30月随访(t_chi_check_two)'");
		String sqld = bf.toString();
		logger.info("儿童 12-30 月随访记录更新日志库["+sqld+"]");
		jdbcTemplate.execute(sqld);
		// 更新目标库
		StringBuffer buffer = new StringBuffer();
		buffer.append("INSERT INTO t_qf_data_upload(add_time,business_id,state,stateType,upload_reason,upload_json,delete_flag,upload_type) VALUES(");
		buffer.append("'").append(newdate).append("',");
		buffer.append("'").append(chichecktwoid).append("',");// chipersonid
		buffer.append("'").append(state).append("',");// state
		buffer.append("'").append(stateType).append("',");// stateType
		buffer.append("'").append(message).append("',");
		buffer.append("'").append(jsondata).append("',");
		buffer.append("'").append("0").append("',");// 默认0 未删除
		buffer.append("'").append("儿童12-30月随访(t_chi_check_two)").append("'");
		buffer.append(")"); //

		String sql = buffer.toString();
		logger.info("儿童 12-30 月随访记录更新日志库["+sql+"]");
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
		//System.out.println(new Upload_ChildTwelve2Thirty_Visit().getSQL());
	}

}
