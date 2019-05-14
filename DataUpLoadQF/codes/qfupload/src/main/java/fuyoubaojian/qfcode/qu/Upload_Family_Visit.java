package fuyoubaojian.qfcode.qu;

import java.util.*;
import com.alibaba.fastjson.JSON;
import fuyoubaojian.qfcode.etl.BaseTask;
import fuyoubaojian.qfcode.etl.TaskInterface;
import fuyoubaojian.qfcode.utils.HttpClientUtils;
import fuyoubaojian.qfcode.utils.TimeUtils;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import fuyoubaojian.qfcode.utils.AddressUtil;

/**
 * 儿童管理卡及新生儿家庭方式记录上传
 */
@Component
public class Upload_Family_Visit extends BaseTask implements TaskInterface {
	private static final Logger logger = Logger.getLogger(Upload_Family_Visit.class);
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
		updateArchivesNum(jdbcTemplate, date1, date2);//获取档案编号
		logger.info("开始上传【" + date1 + "到" + date2 + "】的儿童管理卡及新生儿家庭方式记录信息");
		// 查询结果
		StringBuffer buffer = new StringBuffer();
		logger.info("开始查询儿童管理卡及新生儿家庭方式记录信息");
		logger.info("+++++++++++++++++++++++++++++++======SQL:");
		buffer.append(" SELECT cp.id as chipersonid,cp.archives_num as archivesnum,");
		buffer.append(" cp.card_id as idcard,cp.hk_region as areacode,");                 //String,区域编码,,
		buffer.append(" if(hcall.nose='2','1','0') as bi,");    // String,鼻： 0 未见异常，1 异常,,"
		buffer.append(" cp.yz_day as cfyzt,");    // String,出生孕周（天）,,
		buffer.append(" DATE_FORMAT(cp.add_time,'%Y-%m-%d') as createDate,");//String,创建日期,,
		buffer.append(" case tgl.labor_way when '1' then '1,0,0,0,0,0,0' when '23' then '0,1,0,0,0,0,0' when '21' then '0,0,1,0,0,0,0'");
		buffer.append(" when '3' then '0,0,0,1,0,0,0' when '22' then '0,0,0,0,1,0,0' else '0,0,0,0,0,0,1' end as csqk,");// String,出生情况,顺产，头吸，产钳，剖宫，双多胎，臀位，其他(csqkqt),"
		buffer.append(" DATE_FORMAT(cp.birthday,'%Y-%m-%d') as csrq,");// String,出生日期,,
		buffer.append(" cp.height as cssc,");// String,出生身长(cm),,
		buffer.append(" (cp.weight/1000) as cstz,");// String,出生体重(kg),,
		buffer.append(" cp.yz_week as csyz,");// String,出生孕周(周),,
		buffer.append(" cp.hk_region as cun,");// String,所属区划编码,,
		buffer.append(" cp.hkaddr as cunName,");// String,所属区划名称,,
		buffer.append(" hd.user_id as czy,");// String,操作人 id,,
		buffer.append(" case hcall.feces_type when '1' then '0' when '2' then '1' else '2' end as db,");// String,大便： 0 糊状， 1稀， 2 其他,,"
		buffer.append(" if(hcall.ear in('2','3','4','5'),'1','0') as er,");// String,耳外观： 0 未见异常1 异常,,
		buffer.append(" if(hcall.abdomen is NULL or hcall.abdomen='' or hcall.abdomen='01','0','1') as fb,");// String,腹部： 0 未见异常1 异常,,"
		buffer.append(" DATE_FORMAT(apn.birthday,'%Y-%m-%d') as fqcsrq,");// String,父亲出生日期,,
		buffer.append(" apn.phone as fqlxdh,");// String,父亲联系电话,,
		buffer.append(" apn.card_id as fqsfzh,");// String,父亲身份证号,,
		buffer.append(" case when apn.profession_code in('10','11','13','14') then '5-14'");
		buffer.append(" when apn.profession_code in('05','06','07') then '5-13'");
		buffer.append(" when apn.profession_code in('04','08') then '5-11'");
		buffer.append(" when apn.profession_code='14' then '5-12' when apn.profession_code='09' then '5-15'");
		buffer.append(" when apn.profession_code='15' then '5-18' when apn.profession_code='16' then '5-22' else '5-17' end as fqzy,");// String,父亲职业(详情见下方字典),,"
		buffer.append(" if(hcall.anus is NULL or hcall.anus='' or hcall.anus='1','0','1') as gm,");// String,肛门： 0 未见异常1 异常,,"
		buffer.append(" cp.id as grdabh,");// String,个人档案编号,,
		buffer.append(" case hcall.jaundice when '2' then '1,0,0,0,0' when '3' then '0,1,0,0,0' when '4' then '0,0,1,0,0' when '5' then '0,0,0,1,0' else '0,0,0,0,1' end as hdbw,");// String,黄疸部位： 面部，躯干，四肢，手足，无(0:未选中 1:已选中),,"
		buffer.append(" hcall.breathing_rate as hxpl,");// String,呼吸频率(次/分钟),,
		buffer.append(" if(hcall.neck='2','1','0') as jbbk,");// String,颈部包块： 0 无， 1：有,,"
		buffer.append(" case when cp.comorbidity='2' then '1' when cp.comorbidity='3' then '2'");
		buffer.append(" when cp.comorbidity in('1','4','5','6','7','8','9','99') then '3' else '0' end as jbqk,");// String,妊娠期患病疾病情况,0：无， 1：糖尿病， 2：妊娠期高血压， 3：其他(jbqkqt),"
		buffer.append(" concat(if(cp.dis_num='0' or cp.dis_num='' or cp.dis_num is NULL,'1','0'),',',");
		buffer.append(" if(cp.dis_num>'0' and sd.sc_result in('1','3'),'1','0'),',',");
		buffer.append(" if(cp.dis_num>'0' and sd.tsh_result='阳性','1','0'),',',");
		buffer.append(" if(cp.dis_num>'0' and sd.pku_result='阳性','1','0'),',',");
		buffer.append(" if(cp.dis_num>'0' and sd.cah_result='阳性','1','0'),',',");
		buffer.append(" if(cp.dis_num>'0' and sd.g6pd_result='阳性','1','0'),',',");
		buffer.append(" '0') as jbscyx,");// String,新生儿疾病筛查,未进行，检查均阴性，甲低，苯丙酮尿症，先天性肾上腺皮脂增生症，葡萄糖-6-磷酸脱氢酶缺乏症，其他遗传代谢,"
		//buffer.append(" if(sd.sc_result in('2','4'),'阳性','阴性')  as jbscyx,");// String,疾病筛查阳性,,
		buffer.append(" cp.hospital as jcjg,");// String,助产机构名称,,
		buffer.append(" hh.text as jddw,");//String,建档单位,,   改成管理单位
		buffer.append(" hh.tel as jddwdh,");// String,建档单位电话,,改成管理单位电话
		buffer.append(" apv.tel as jtdh,");// String,家庭电话,,
		buffer.append(" cp.xzaddr as jtzz,");// String,详细住址（家庭住址）,,"
		buffer.append(" if(hcall.spine='02','1','0') as jz,");// String,脊柱: 0 未见异常1 异常,,"
		buffer.append(" '' as kh,");//String,,
		buffer.append(" if(hcall.mouth in('4','5','6','99'),'1','0') as kq,");// String,口腔： 0 未见异常1 异常,"
		buffer.append(" hd.name as lrrName,");// String,录入人姓名,
		buffer.append(" apv.name as lxr,"); //String,联系人,
		buffer.append(" apv.tel as lxrdh,");// String,联系人电话,
		buffer.append(" hcall.pulse_rate as maibo,");// String,脉搏(次/分钟),
		buffer.append(" CASE WHEN hcall.face_colour='1' THEN '0' WHEN hcall.face_colour='2' THEN '1' ELSE '2' END as mianse,");// String,面色：红润，黄染，其他(msqt)(0:未选中 1:已选中),
		buffer.append(" DATE_FORMAT(apv.birthday,'%Y-%m-%d') as mqcsrq,");// String,母亲出生日期,
		buffer.append(" apv.phone as mqlxdh,");// String,母亲联系电话,
		buffer.append(" apv.name as mqname,");// String,母亲姓名,
		buffer.append(" apv.card_id as mqsfzh,");// String,母亲身份证号,
		buffer.append(" (hcall.weight/1000) as mqtz,");// String,目前体重(kg),
		buffer.append(" case when apv.profession_code in('10','11','13','14') then '5-14'");
		buffer.append(" when apv.profession_code in('05','06','07') then '5-13'");
		buffer.append(" when apv.profession_code in('04','08') then '5-11'");
		buffer.append(" when apv.profession_code='14' then '5-12' when apv.profession_code='09' then '5-15'");
		buffer.append(" when apv.profession_code='15' then '5-18' when apv.profession_code='16' then '5-22' else '5-17' end as mqzy,");// String,母亲职业(详情见下方字典),"
		buffer.append(" cp.name as name,");//String,(儿童)姓名,
		buffer.append(" if(hcall.vomit='有','1','0') as ot,");// String,呕吐： 0 无， 1 有,
		buffer.append(" concat(if(hcall.skin='01','1','0'),if(hcall.skin='07','1','0'),if(hcall.skin='08','1','0'),if(hcall.skin not in('01','07','08'),'1','0'))as pf,");// String,皮肤：未见异常，湿疹，糜烂，其他(pfqt)(0:未选中 1:已选中),"
		buffer.append(" concat(if(hcall.navel_cord='2','2','1'),',',if(hcall.navel_cord='1','2','1'),',','1',',',if(hcall.navel_cord='3','2','1')) as qd,");// String,脐带：未脱，脱落，脐部有渗出，其他(qdqt)(0:未选中 1:已选中),"
		buffer.append(" hcall.km_size1 as qx,");// String,前囟(左),
		buffer.append(" hcall.km_size2 as qx1,");// String,前囟(右),
		buffer.append(" case hcall.fontanelle when '1' then '0' when '2' then '1' when '3' then '2'");
		buffer.append(" when '4' then '3' else '3' end as qxqk,");// String,前囟状况： 0：正常，1：澎隆， 2：凹陷，3 其他(qxqt),"
		buffer.append(" cp.name as ryxm,");// String,(儿童)姓名,
		buffer.append(" if(cp.catface='' or cp.catface is NULL,'0','1') as sfjx,");// String,是否有畸型： 0 无，1：有,"
		buffer.append(" DATE_FORMAT(hcall.check_date,'%Y-%m-%d') as sfrq,");// String,随访日期,
		buffer.append(" hdhcall.name as sfys,");// String,随访医生,
		buffer.append(" hdhcall.user_id as sfyscode,");// String,随访医生编码,
		buffer.append(" cp.hkaddr as ssdwName,");// String,所属区划名称,
		buffer.append(" cp.hk_region as sssq,");// String,所属区划编码,
		buffer.append(" if(hcall.limb='2','1','0') as szhdd,");// String,四肢活动度： 0 未见异常， 1 异常,"
		buffer.append(" hcall.temperature as tiwen,");// String,体温(℃ ),
		buffer.append(" '' as tlsc,");// String,新生儿听力筛查：0：通过， 1 未通过，2 未筛查， 3：不详,"
		buffer.append(" '' as updateid,");// String,更新人 id,
		buffer.append(" if(cp.update_time is NULL, DATE_FORMAT(cp.add_time,'%Y-%m-%d') , DATE_FORMAT(cp.update_time,'%Y-%m-%d')) as updatetime,");// String,更新时间,
		buffer.append(" if(hcall.pudendum in('2','3','4','5','6','7','8','9','10','11'),'1','0') as wszq,");// String,外生殖器： 0 未见异常， 1 异常,"
		buffer.append(" CASE hcall.feed_way WHEN '1' THEN '0' WHEN '2' THEN '1' WHEN '3' THEN '2' END as wyfs,");//String,喂养方式： 0 母乳， 1混合喂养 2 人工,"
		buffer.append(" '' as wzd,");// String,完整度,
		buffer.append(" hcall.next_place as xcsfdd,");// String,下次随访地点,
		buffer.append(" DATE_FORMAT(hcall.next_date,'%Y-%m-%d') as xcsfrq,");// String,下次随访日期,
		buffer.append(" if(hcall.heart_lungs in('2','3','4','5'),'1','0') as xf,");// String,心肺： 0 未见异常， 1异常,"
		buffer.append(" cp.sex as xingbie,");//String,性别： 1 男 2 女,
		buffer.append(" if(cp.choke='有','1','0') as xsezx,");// String,新生儿窒息： 0 无，1 有,"
		buffer.append(" cp.cs_address as xxdz,");// String,详细地址,
		buffer.append(" if(hcall.eye in('2','3','4','5'),'1','0') as yan,");// String,眼睛： 0 未见异常， 1异常,"
		buffer.append(" concat(if(hcall.guide LIKE '%喂养指导%','1','0'),',',if(hcall.guide LIKE '%发育指导%','1','0'),',', if(hcall.guide LIKE '%防病指导%','1','0'),',',if(hcall.guide LIKE '%预防伤害指导%','1','0'),',',if(hcall.guide LIKE '%口腔保健指导%','1','0'),',','0') as zhidao,");// String,指导(多选)喂养指导，发育指导，防病指导，预防伤害指导，口腔保健指导，其他(zdqt),
		buffer.append(" if(hcall.changer in('1','是'),'1','0') as zhuanzhen, ");// String,转诊： 0 无， 1 有
		buffer.append(" '0' as zxbzw ");// String,注销标志位 0：未注销， 1：注销
		buffer.append(" FROM t_chi_person cp");
		buffer.append(" LEFT JOIN t_gra_child AS gc ON gc.id=cp.gra_child_id and gc.statue='0'");
		buffer.append(" LEFT JOIN t_gra_labor AS tgl ON tgl.person_id=gc.person_id and tgl.statue='0'");
		buffer.append(" LEFT JOIN t_arc_person AS apv ON apv.id=cp.mother_id");
		buffer.append(" LEFT JOIN t_arc_person AS apn ON apn.id=cp.father_id");
		buffer.append(" LEFT JOIN t_chi_housecall AS hcall ON hcall.person_id=cp.id and hcall.statue='0'");
		buffer.append(" LEFT JOIN t_sc_card AS sc ON sc.child_id=cp.id and sc.statue='0'");
		buffer.append(" LEFT JOIN t_sc_data AS sd ON sd.code=sc.id");
		buffer.append(" JOIN t_hos_doctor AS hd ON hd.user_id=cp.add_user_id");
		buffer.append(" JOIN t_hos_hospital AS hh ON hh.id=cp.manage_unit_id");
		buffer.append(" LEFT JOIN t_hos_doctor AS hdhcall ON hdhcall.user_id=hcall.add_user_id");
		buffer.append(" WHERE 1=1 and cp.statue='0'");// cp.card_id='370800201801010102'
		buffer.append(" and cp.add_time >= ").append(date1);
		buffer.append(" and cp.add_time <= ").append(date2);

		logger.info(buffer.toString());
		return buffer.toString();
	}

	@Override
	public String getUpdateSQL(Map<String, Object> objs) {
		return null;
	}


	@Override
	public Map<String,Object> getStateSQL(Map<String, Object> objs) {
		//============================================= test1
		/*String id = AddressUtil.toStr(objs.get("id"));
		String text = AddressUtil.toStr(objs.get("text"));
		String code = AddressUtil.toStr(objs.get("code"));
		logger.info("id====" + id);
		logger.info("text====" + text);
		logger.info("code====" + code);*/

		//====================================================test2
		/*String name = "王乐乐";
		String idcard = "370203198703021234";
		String machineId = "WL10001";

		String areacode = "370812";
		String bi = "0";
		String cfyzt = "5";
		String createDate = "2017-09-21";*/
		//====================================================
		String idcard = AddressUtil.toStr(objs.get("idcard"));//身份证号
		String chipersonid = AddressUtil.toStr(objs.get("chipersonid"));//业务id
		String archivesnum = AddressUtil.toStr(objs.get("archivesnum"));//档案编号
		String jsondata;
		Map<String,Object > data = new HashMap<String,Object>();
		Map<String, Object> dataInJson = new HashMap<String, Object>();
		Map<String, Object> itemInJson = new HashMap<String, Object>();
		/* AppCode */
		data.put("AppCode", "1101");
		data.put("InJsonString", dataInJson);
		/* InJsonString  81 true*/
		dataInJson.put("name", AddressUtil.toStr(objs.get("name")));//儿童姓名
		dataInJson.put("grdabh", AddressUtil.toStr(objs.get("archivesnum")));
		dataInJson.put("machineId", "JNWFFY001");//暂时空的
		dataInJson.put("item", itemInJson);
		/* item */
		itemInJson.put("areacode", AddressUtil.toStr(objs.get("areacode")));
		itemInJson.put("bi", AddressUtil.toStr(objs.get("bi")));
		itemInJson.put("cfyzt", AddressUtil.toStr(objs.get("cfyzt")));
		itemInJson.put("createDate", AddressUtil.toStr(objs.get("createDate")));
		itemInJson.put("csqk", AddressUtil.toStr(objs.get("csqk")));
		itemInJson.put("csrq", AddressUtil.toStr(objs.get("csrq")));
		itemInJson.put("cssc", AddressUtil.toStr(objs.get("cssc")));
		itemInJson.put("cstz", AddressUtil.toStr(objs.get("cstz")));
		itemInJson.put("csyz", AddressUtil.toStr(objs.get("csyz")));
		itemInJson.put("cun", AddressUtil.toStr(objs.get("cun")));
		itemInJson.put("cunName", AddressUtil.toStr(objs.get("cunName")));
		itemInJson.put("czy", AddressUtil.toStr(objs.get("czy")));
		itemInJson.put("db", AddressUtil.toStr(objs.get("db")));
		itemInJson.put("er", AddressUtil.toStr(objs.get("er")));
		itemInJson.put("fb", AddressUtil.toStr(objs.get("fb")));
		itemInJson.put("fqcsrq", AddressUtil.toStr(objs.get("fqcsrq")));
		itemInJson.put("fqlxdh", AddressUtil.toStr(objs.get("fqlxdh")));
		itemInJson.put("fqsfzh", AddressUtil.toStr(objs.get("fqsfzh")));
		itemInJson.put("fqzy", AddressUtil.toStr(objs.get("fqzy")));
		itemInJson.put("gm", AddressUtil.toStr(objs.get("gm")));
		itemInJson.put("grdabh", AddressUtil.toStr(objs.get("grdabh")));
		itemInJson.put("hdbw", AddressUtil.toStr(objs.get("hdbw")));
		itemInJson.put("hxpl", AddressUtil.toStr(objs.get("hxpl")));
		itemInJson.put("jbbk", AddressUtil.toStr(objs.get("jbbk")));
		itemInJson.put("jbqk", AddressUtil.toStr(objs.get("jbqk")));
		//itemInJson.put("jbsc", AddressUtil.toStr(objs.get("jbsc")));
		itemInJson.put("jbscyx", AddressUtil.toStr(objs.get("jbscyx")));
		itemInJson.put("jcjg", AddressUtil.toStr(objs.get("jcjg")));
		itemInJson.put("jddw", AddressUtil.toStr(objs.get("jddw")));
		itemInJson.put("jddwdh", AddressUtil.toStr(objs.get("jddwdh")));
		itemInJson.put("jtdh", AddressUtil.toStr(objs.get("jtdh")));
		itemInJson.put("jtzz", AddressUtil.toStr(objs.get("jtzz")));
		itemInJson.put("jz", AddressUtil.toStr(objs.get("jz")));
		itemInJson.put("kh", AddressUtil.toStr(objs.get("kh")));
		itemInJson.put("kq", AddressUtil.toStr(objs.get("kq")));
		itemInJson.put("lrrName", AddressUtil.toStr(objs.get("lrrName")));
		itemInJson.put("lxr", AddressUtil.toStr(objs.get("lxr")));
		itemInJson.put("lxrdh", AddressUtil.toStr(objs.get("lxrdh")));
		itemInJson.put("maibo", AddressUtil.toStr(objs.get("maibo")));
		itemInJson.put("mianse", AddressUtil.toStr(objs.get("mianse")));
		itemInJson.put("mqcsrq", AddressUtil.toStr(objs.get("mqcsrq")));
		itemInJson.put("mqlxdh", AddressUtil.toStr(objs.get("mqlxdh")));
		itemInJson.put("mqname", AddressUtil.toStr(objs.get("mqname")));
		itemInJson.put("mqsfzh", AddressUtil.toStr(objs.get("mqsfzh")));
		itemInJson.put("mqtz", AddressUtil.toStr(objs.get("mqtz")));
		itemInJson.put("mqzy", AddressUtil.toStr(objs.get("mqzy")));

		itemInJson.put("name", AddressUtil.toStr(objs.get("name")));
		itemInJson.put("ot", AddressUtil.toStr(objs.get("ot")));
		itemInJson.put("pf", AddressUtil.toStr(objs.get("pf")));
		itemInJson.put("qd", AddressUtil.toStr(objs.get("qd")));
		itemInJson.put("qx", AddressUtil.toStr(objs.get("qx")));
		itemInJson.put("qx1", AddressUtil.toStr(objs.get("qx1")));
		itemInJson.put("qxqk", AddressUtil.toStr(objs.get("qxqk")));
		itemInJson.put("ryxm", AddressUtil.toStr(objs.get("ryxm")));
		itemInJson.put("sfjx", AddressUtil.toStr(objs.get("sfjx")));
		itemInJson.put("sfrq", AddressUtil.toStr(objs.get("sfrq")));
		itemInJson.put("sfys", AddressUtil.toStr(objs.get("sfys")));
		itemInJson.put("sfyscode", AddressUtil.toStr(objs.get("sfyscode")));
		itemInJson.put("ssdwName", AddressUtil.toStr(objs.get("ssdwName")));
		itemInJson.put("sssq", AddressUtil.toStr(objs.get("sssq")));
		itemInJson.put("szhdd", AddressUtil.toStr(objs.get("szhdd")));
		itemInJson.put("tiwen", AddressUtil.toStr(objs.get("tiwen")));
		itemInJson.put("tlsc", AddressUtil.toStr(objs.get("tlsc")));
		itemInJson.put("updateid", AddressUtil.toStr(objs.get("updateid")));
		itemInJson.put("updatetime", AddressUtil.toStr(objs.get("updatetime")));
		itemInJson.put("wszq", AddressUtil.toStr(objs.get("wszq")));
		itemInJson.put("wyfs", AddressUtil.toStr(objs.get("wyfs")));
		itemInJson.put("wzd", AddressUtil.toStr(objs.get("wzd")));
		itemInJson.put("xcsfdd", AddressUtil.toStr(objs.get("xcsfdd")));
		itemInJson.put("xcsfrq", AddressUtil.toStr(objs.get("xcsfrq")));
		itemInJson.put("xf", AddressUtil.toStr(objs.get("xf")));
		itemInJson.put("xingbie", AddressUtil.toStr(objs.get("xingbie")));
		itemInJson.put("xsezx", AddressUtil.toStr(objs.get("xsezx")));
		itemInJson.put("xxdz", AddressUtil.toStr(objs.get("xxdz")));
		itemInJson.put("yan", AddressUtil.toStr(objs.get("yan")));
		itemInJson.put("zhidao", AddressUtil.toStr(objs.get("zhidao")));
		itemInJson.put("zhuanzhen", AddressUtil.toStr(objs.get("zhuanzhen")));
		itemInJson.put("zxbzw", AddressUtil.toStr(objs.get("zxbzw")));

		jsondata = JSON.toJSONString(data);
		logger.info("============jsondata:" + jsondata);// jsondata
		//String state = "";
		Map<String,Object> resuletMap = new HashMap<String, Object>();
		try {
			resuletMap = HttpClientUtils.getJson(jsondata);
			resuletMap.put("idcard",idcard);
			resuletMap.put("jsondata",jsondata);
			resuletMap.put("chipersonid",chipersonid);
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
		logger.info("开始儿童管理卡及新生儿家庭方式记录上传");
	}

	@Override
	public void logLook(int i) {
		logger.info("查询儿童管理卡及新生儿家庭方式记录上传信息" + i + "条");
	}

	@Override
	public void logAfter(JdbcTemplate jdbcTemplate,Map<String, Object> objs) {

		String newdate = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
		String idcard = (String) objs.get("idcard");
		JSONObject jsondata = (JSONObject) objs.get("jsondata");
		String chipersonid = (String) objs.get("chipersonid");
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
		bf.append("'").append(chipersonid).append("' and upload_type='儿童管理卡及新生儿家庭访视(t_chi_person)'");
		String sqld = bf.toString();
		logger.info("儿童管理卡及新生儿家庭方式记录信息更新日志库["+sqld+"]");
		jdbcTemplate.execute(sqld);
		// 更新目标库  add
		StringBuffer buffer = new StringBuffer();
		buffer.append("INSERT INTO t_qf_data_upload(add_time,business_id,state,stateType,upload_reason,upload_json,delete_flag,upload_type) VALUES(");
		buffer.append("'").append(newdate).append("',");
		buffer.append("'").append(chipersonid).append("',");// chipersonid
		buffer.append("'").append(state).append("',");// state
		buffer.append("'").append(stateType).append("',");// stateType
		buffer.append("'").append(message).append("',");
		buffer.append("'").append(jsondata).append("',");
		buffer.append("'").append("0").append("',");// 默认0 未删除
		buffer.append("'").append("儿童管理卡及新生儿家庭访视(t_chi_person)").append("'");
		buffer.append(")"); //

		String sql = buffer.toString();
		logger.info("儿童管理卡及新生儿家庭方式记录信息更新日志库["+sql+"]");
		jdbcTemplate.execute(sql);
	}

	public void updateArchivesNum(JdbcTemplate jdbcTemplate,String date1,String date2) {
		// 查询结果
		StringBuffer buffer = new StringBuffer();
		List<Map<String, Object>> objs = null;
		logger.info("+++++++++++++++++++++++++++++++需要获取档案编号的t_chi_person的data======SQL:");
		buffer.append(" SELECT cp.id as chipersonid,cp.archives_num as archivesnum,");
		buffer.append(" cp.card_id as idcard,DATE_FORMAT(cp.birthday,'%Y-%m-%d') as birthday,cp.name");
		buffer.append(" FROM t_chi_person cp");
		buffer.append(" WHERE 1=1 and cp.statue='0' and cp.card_id='370800201801010102'");// cp.card_id='370800201801010102'
		buffer.append(" and cp.add_time >= ").append(date1);
		buffer.append(" and cp.add_time <= ").append(date2);
		logger.info(buffer.toString());
		objs = jdbcTemplate.queryForList(buffer.toString());
		String jsondata;
		Map<String,Object > data = new HashMap<String,Object>();
		Map<String, Object> dataInJson = new HashMap<String, Object>();
		for (int i = 0; i < objs.size(); i++) {
			try{
				String archivesnum = AddressUtil.toStr(objs.get(i).get("archivesnum"));
				String chipersonid = AddressUtil.toStr(objs.get(i).get("chipersonid"));
				if(null == archivesnum || StringUtils.equals(archivesnum,"")){
					/* AppCode */
					data.put("AppCode", "1202");
					data.put("InJsonString", dataInJson);
					/* InJsonString */
					dataInJson.put("name", AddressUtil.toStr(objs.get(i).get("name")));//儿童姓名
					dataInJson.put("csrq", AddressUtil.toStr(objs.get(i).get("birthday")));
					/*dataInJson.put("name", "张逸航");//儿童姓名
					dataInJson.put("csrq", "2018-01-13");*/
					dataInJson.put("machineId", "JNWFFY001");//JNWFFY001
					jsondata = JSON.toJSONString(data);
					logger.info("+++++++++++++++++++++++++++++++需要获取档案编号的jsondata:" + jsondata);
					Map<String,Object> resuletMap = HttpClientUtils.getJson(jsondata);
					String success = (String) resuletMap.get("success");
					if(StringUtils.equals(success, "1")){//成功
						List<Map<String,Object>> l = (List<Map<String, Object>>) resuletMap.get("data");
						if (l != null && l.size() == 1) {
							Map<String,Object> map = l.get(0);
							//update
							StringBuffer bf = new StringBuffer();
							bf.append("update t_chi_person set archives_num='").append(AddressUtil.toStr(map.get("grdabh"))).append("'");
							bf.append(" where id=");
							bf.append("'").append(chipersonid).append("'");
							String sqld = bf.toString();
							jdbcTemplate.execute(sqld);
						}else {
							//update
							StringBuffer bf = new StringBuffer();
							bf.append("update t_chi_person set archives_num='").append("more").append("'");
							bf.append(" where id=");
							bf.append("'").append(chipersonid).append("'");
							String sqld = bf.toString();
							jdbcTemplate.execute(sqld);
						}
					}
				}
			} catch (Exception e) {
				logger.error("获取档案编号失败" + e.getMessage());
				e.printStackTrace();
			}
		}
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
		//System.out.println(new Upload_Family_Visit().getSQL(null));
		// 查询结果
		StringBuffer buffer = new StringBuffer();
		List<Map<String, Object>> objs = null;
		logger.info("+++++++++++++++++++++++++++++++需要获取档案编号的t_chi_person的data======SQL:");
		buffer.append(" SELECT cp.id as chipersonid,cp.archives_num as archivesnum,");
		buffer.append(" cp.card_id as idcard,cp.birthday,cp.name");
		buffer.append(" FROM t_chi_person cp");
		buffer.append(" WHERE 1=1 and cp.statue='0'");// cp.card_id='370800201801010102'
		buffer.append(" and cp.add_time >= ").append("");
		buffer.append(" and cp.add_time <= ").append("");
		System.out.println(buffer.toString());
	}

}
