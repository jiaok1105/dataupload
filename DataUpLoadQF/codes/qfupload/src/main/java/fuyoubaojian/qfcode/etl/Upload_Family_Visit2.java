package fuyoubaojian.qfcode.etl;

import fuyoubaojian.qfcode.utils.AddressUtil;
import fuyoubaojian.qfcode.utils.TimeUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * 儿童管理卡及新生儿家庭方式记录上传
 */
@Component
public class Upload_Family_Visit2 extends BaseTask implements TaskInterface {
	private static final Logger logger = Logger.getLogger(Upload_Family_Visit2.class);
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
		logger.info("开始迁移【" + date1 + "到" + date2 + "】的出生登记信息");
		// 查询结果
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("select  distinct ")
		.append("concat('131000',cp.id) as  SJXT_ID, ")//市级人口出生信息系统中对新生儿的唯一编码SAN..80必填，廊坊市行政区代码131000
		.append("cp.id as  XSE_BM, ")//市内新生儿唯一标识SAN..80
		.append("cp.name as  XSE_XM, ")//新生儿拟在公安管理部门正式登记注册的姓名SA..50
		.append("cp.sex as  XSE_XBDM, ")//新生儿生理性别的代码SN1S101-01必填
		.append("DATE_FORMAT(cp.birthday,'%Y-%m-%d %H:%i:%S') as  XSE_CSSJ, ")//新生儿出生当日的公元纪年日期和时间的完整描述DTDT12必填，YYYYMMDDHHMM
		.append("cp.yz_week*7+cp.yz_day as  XSE_YZ, ")//新生儿出生时母亲妊娠时长，计量单位为天NN2..3必填（机构外不必填）
		
		.append("cp.weight as  XSE_TZ, ")//新生儿出生后1h内体重的测量值，计量单位为gNN3..4300-9999必填（机构外不必填）
		.append("cp.height as  XSE_SC, ")//新生儿出生后1h内身长的测量值，计量单位为cmNN2,125.0-70.0必填（机构外不必填）
		.append("drsheng.text as  CSDD_S, ")//新生儿出生地址中的省、自治区或直辖市名称SA..70必填
		.append("drshi.text as  CSDD_SD, ")//新生儿出生地址中的市、地区或州的名称SA..70必填
		.append("drxian.text as  CSDD_XQ, ")//新生儿出生地址中的县、市或区名称SA..70必填
		.append("drxian.id as  CSD_QHDM, ")//新生儿出生地的县级及县级以上行政区划的代码SN6S101-03必填
		.append("case cp.hospital  when cp.hospital then cp.hospital else '/' end as  YLJGMC, ")//助产机构的名称SAN..100必填，医疗卫生机构外分娩填“/”
		.append("hh.zzjg_code as  YLJGDM, ")//医疗机构代码SAN..50对应着机构数据中的系统编码字段 
		.append("ifnull(cp.deliver,'/') as  JSRY, ")//接生人员在公安户籍管理部门登记注册的姓名SA..50必填，若无接生人员，则填“/”
		.append("pm.`name` as  MQ_XM, ")//新生儿母亲在公安管理部门登记注册的姓名SA..50
		.append("cp.gravida_id as  MQ_BAH, ")//母亲分娩的《住院病案首页》中的病案号SAN..64
		.append("pm.birthday as  MQ_CSRQ, ")//母亲出生时的公元纪年日期DD8YYYYMMDD
		.append("year(from_days(datediff(now(), pm.birthday)))  as  MQ_NL, ")//新生儿出生时的母亲年龄SN2
		.append("tc.code as  MQ_GJ, ")//母亲所属国籍SN3S101-04
		.append("if(ds2.`code` = '99' or ds2.`code` is null or length(ds3.`code`)>2,'97',CAST(ds2.code AS SIGNED)) as  MQ_MZ,  ")//母亲所属民族SN1..2S101-02
		.append("drsheng1.text  as  MQ_HJ, ")//母亲户口所在地的省级行政区划名称SA..70
		.append("drsheng1.code as  MQ_HJSJDM, ")//母亲户口所在地的省级行政区划名代码SN6
		.append("pm.xzaddr as  MQ_ZZ, ")//母亲家庭地址的省、市、县、乡镇、村及门牌号码SAN..200
		.append("case pm.card_type_code  when '01' then '01' when '02' then '06' when '04' then '03' else '99'  end as  MQ_SFZJLX, ")//母亲有效身份证件的类别SN2S101-05
		.append("pm.card_id as  MQ_SFZJHM,  ")//母亲有效身份证件上唯一的法定标识符SAN..18
		.append("pd.name as  FQ_XM, ")//父亲在公安管理部门登记注册的姓名SA..50
		.append("pd.birthday as  FQ_CSRQ, ")//父亲出生时的公元纪年日期DD8YYYYMMDD
		.append("year(from_days(datediff(now(), pd.birthday))) as  FQ_NL, ")//新生儿出生时的父亲年龄NN2
		.append("tc1.code as  FQ_GJ, ")//父亲所属国籍SN3S101-04
		.append("if(ds3.`code` = '99' or ds3.`code` is null or length(ds3.`code`)>2,'97',CAST(ds3.code AS SIGNED)) as  FQ_MZ, ")//父亲所属民族SN1..2S101-02
		.append("drsheng2.text as  FQ_HJ, ")//父亲户口所在地的省级行政区划名称SA..70非必填
		.append("drsheng2.code as  FQ_HJSJDM, ")//父亲户口所在地的省级行政区划名代码SN6非必填
		.append("pd.xzaddr as  FQ_ZZ, ")//父亲家庭地址的省、市、县、乡镇、村及门牌号码SAN..200
		.append("case pd.card_type_code  when '01' then '01' when '02' then '06' when '04' then '03' else '99'  end as  FQ_SFZJLX, ")//父亲有效身份证件的类别SN2S101-05
		.append("pd.card_id as  FQ_SFZHM,  ")//父亲身份证件上唯一的法定标识符SAN..18
		.append("if(cp.update_time is NULL,cp.add_time,cp.update_time) as  SJXRSJ  ")//最后修改时的公元纪年日期和时间的完整描述DTDT12必填

		.append("from  t_chi_person cp ")

		//.append("left join t_gra_labor gl on gl.person_id = cp.gravida_id ")//孕周从儿童档案就可以获取
		//.append("left join t_gra_child gc on gc.person_id = cp.gravida_id ")//没有使用
		.append("left join t_arc_person pm on pm.id=cp.mother_id ")//
		.append("left join t_arc_person pd on pd.id=cp.father_id ")//
		//.append("left join t_cert_distr cd on cd.chi_person_id = cp.gravida_id ")//没有使用
		
		.append(" join t_hos_doctor hd on hd.user_id=cp.add_user_id  ")
		.append(" join t_hos_hospital_temp181229 hh on hh.hos_id = hd.hospital_id ")//
		
		.append("left join temp_countrycode tc on tc.dscode = pm.country_code ")//
		.append("left join temp_countrycode tc1 on tc1.dscode = pd.country_code ")//
		.append("left join t_dic_system ds2 on ds2.`code`=pm.nation_code and ds2.type='民族' ")//
		.append("left join t_dic_system ds3 on ds3.`code`=pd.nation_code and ds3.type='民族' ")//
		
		.append("left join t_dic_region drsheng on drsheng.code=substr(cp.cs_region,1,2)   ")//新生儿出生地省级行政代码
		.append("left join t_dic_region drshi on drshi.code=substr(cp.cs_region,1,4)    ")//新生儿出生地市级行政代码
		.append("left join t_dic_region drxian on drxian.code=substr(cp.cs_region,1,6)  ")//新生儿出生地县区级行政代码
		//.append("left join t_dic_region drxiang on drxiang.code=substr(cp.cs_region,1,9) ")//
		.append("left join t_dic_region drsheng1 on drsheng1.code=substr(pm.hk_region_code,1,2)  ")//
		.append("left join t_dic_region drsheng2 on drsheng2.code=substr(pd.hk_region_code,1,2)  ")//

		.append(" WHERE cp.statue=0 AND cp.add_time").append(">= ").append(date1).append(" and cp.add_time <= ").append(date2);
		logger.info(buffer.toString());
		return buffer.toString();
	}

	@Override
	public String getUpdateSQL(Map<String, Object> objs) {
		// 更新目标库
		StringBuffer buffer = new StringBuffer();
		buffer.append("INSERT INTO d201 VALUES(");

		buffer.append("'").append(AddressUtil.toStr(objs.get("SJXT_ID"))).append("',");//D201-01系统ID市级人口出生信息系统中对新生儿的唯一编码S必填,系统主键（上传规则为地市级行政区划代码+市级系统主键，如石家庄市则该字段上传规则为130100+市级系统id）
		buffer.append("'").append(AddressUtil.toStr(objs.get("XSE_BM"))).append("',");//D201-02新生儿编码市内新生儿唯一标识S
		buffer.append("'").append(AddressUtil.toStr(objs.get("XSE_XM"))).append("',");//D201-03新生儿姓名新生儿拟在公安管理部门正式登记注册的姓名S
		buffer.append("'").append(AddressUtil.toStr(objs.get("XSE_XBDM"))).append("',");//D201-04新生儿性别新生儿生理性别的代码S必填
		
		buffer.append("to_date('").append(AddressUtil.toStr(objs.get("XSE_CSSJ"))).append("','yyyy-mm-dd hh24:mi:ss'),");//D201-05新生儿出生时间新生儿出生当日的公元纪年日期和时间的完整描述DT必填，YYYYMMDDHHMM
		buffer.append("'").append(AddressUtil.toStr(objs.get("XSE_YZ"))).append("',");//D201-06出生孕周（天）新生儿出生时母亲妊娠时长，计量单位为天N必填（机构外不必填）
		buffer.append("'").append(AddressUtil.toStr(objs.get("XSE_TZ"))).append("',");//D201-07出生体重（g）新生儿出生后1h内体重的测量值，计量单位为gN必填（机构外不必填）
		buffer.append("'").append(AddressUtil.toStr(objs.get("XSE_SC"))).append("',");//D201-08出生身长（cm）新生儿出生后1h内身长的测量值，计量单位为cmN必填（机构外不必填）
		buffer.append("'").append(AddressUtil.toStr(objs.get("CSDD_S"))).append("',");//D201-09出生地点-省（自治区、直辖市）新生儿出生地址中的省、自治区或直辖市名称S必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("CSDD_SD"))).append("',");//D201-10出生地点-市（地区、州）新生儿出生地址中的市、地区或州的名称S必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("CSDD_XQ"))).append("',");//D201-11出生地点-县（市、区）新生儿出生地址中的县、市或区名称S必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("CSD_QHDM"))).append("',");//D201-15出生地点_行政区划代码新生儿出生地的县级及县级以上行政区划的代码S必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("YLJGMC"))).append("',");//D201-25医疗机构名称助产机构的名称S必填，医疗卫生机构外分娩填“/”
		buffer.append("'").append(AddressUtil.toStr(objs.get("YLJGDM"))).append("',");//D201-26医疗机构代码医疗机构代码S对应着机构数据中的系统编码字段 
		buffer.append("'").append(AddressUtil.toStr(objs.get("JSRY"))).append("',");//D201-27接生人员接生人员在公安户籍管理部门登记注册的姓名S必填，若无接生人员，则填“/”
		buffer.append("'").append(AddressUtil.toStr(objs.get("MQ_XM"))).append("',");//D201-28母亲姓名新生儿母亲在公安管理部门登记注册的姓名S
		buffer.append("'").append(AddressUtil.toStr(objs.get("MQ_BAH"))).append("',");//D201-29母亲病案号母亲分娩的《住院病案首页》中的病案号S
		String mqcssj = AddressUtil.toStr(objs.get("MQ_CSRQ"));//D201-30母亲出生日期母亲出生时的公元纪年日期DYYYYMMDD
		if(mqcssj.length()>7)
			buffer.append("to_date('").append(mqcssj.replace("-","")).append("','yyyyMMdd'),");
		else
			buffer.append("'").append(mqcssj).append("',");
		
		buffer.append("'").append(AddressUtil.toStr(objs.get("MQ_NL"))).append("',");//D201-31母亲年龄新生儿出生时的母亲年龄S
		buffer.append("'").append(AddressUtil.toStr(objs.get("MQ_GJ"))).append("',");//D201-32母亲国籍母亲所属国籍S
		buffer.append("'").append(AddressUtil.toStr(objs.get("MQ_MZ"))).append("',");//D201-33母亲民族母亲所属民族S
		buffer.append("'").append(AddressUtil.toStr(objs.get("MQ_HJ"))).append("',");//D201-34母亲户籍所在地母亲户口所在地的省级行政区划名称S
		buffer.append("'").append(AddressUtil.toStr(objs.get("MQ_HJSJDM"))).append("',");//D201-35母亲户籍所在地省级行政区划代码母亲户口所在地的省级行政区划名代码S
		buffer.append("'").append(AddressUtil.toStr(objs.get("MQ_ZZ"))).append("',");//D201-36母亲住址母亲家庭地址的省、市、县、乡镇、村及门牌号码S
		buffer.append("'").append(AddressUtil.toStr(objs.get("MQ_SFZJLX"))).append("',");//D201-37母亲有效身份证件类型母亲有效身份证件的类别S
		buffer.append("'").append(AddressUtil.toStr(objs.get("MQ_SFZJHM"))).append("',");//D201-38母亲有效身份证件号码母亲有效身份证件上唯一的法定标识符S
		buffer.append("'").append(AddressUtil.toStr(objs.get("FQ_XM"))).append("',");//D201-39父亲姓名父亲在公安管理部门登记注册的姓名S
		String fqcssj = AddressUtil.toStr(objs.get("FQ_CSRQ"));//D201-40父亲出生日期父亲出生时的公元纪年日期DYYYYMMDD
		if(fqcssj.length()>7)
			buffer.append("to_date('").append(fqcssj.replace("-","")).append("','yyyyMMdd'),");
		else
			buffer.append("'").append(fqcssj).append("',");

		buffer.append("'").append(AddressUtil.toStr(objs.get("FQ_NL"))).append("',");//D201-41父亲年龄新生儿出生时的父亲年龄N
		buffer.append("'").append(AddressUtil.toStr(objs.get("FQ_GJ"))).append("',");//D201-42父亲国籍父亲所属国籍S
		buffer.append("'").append(AddressUtil.toStr(objs.get("FQ_MZ"))).append("',");//D201-43父亲民族父亲所属民族S
		buffer.append("'").append(AddressUtil.toStr(objs.get("FQ_HJ"))).append("',");//D201-44父亲户籍所在地父亲户口所在地的省级行政区划名称S非必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("FQ_HJSJDM"))).append("',");//D201-45父亲户籍所在地省级行政区划代码父亲户口所在地的省级行政区划名代码S非必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("FQ_ZZ"))).append("',");//D201-46父亲住址父亲家庭地址的省、市、县、乡镇、村及门牌号码S
		buffer.append("'").append(AddressUtil.toStr(objs.get("FQ_SFZJLX"))).append("',");//D201-47父亲有效身份证件类型父亲有效身份证件的类别S
		buffer.append("'").append(AddressUtil.toStr(objs.get("FQ_SFZHM"))).append("',");//D201-48父亲有效身份证件号码父亲身份证件上唯一的法定标识符S
//		buffer.append("to_date('").append(AddressUtil.toStr(objs.get("SJXRSJ"))).append("','yyyy-mm-dd hh24:mi:ss')");//D201-49数据写入时间最后修改时的公元纪年日期和时间的完整描述DT必填
		buffer.append("sysdate");// D101-11数据写入时间最后修改时的公元纪年日期和时间的完整描述DT必填
		buffer.append(")"); // 
		return buffer.toString();
	}

	@Override
	public Map<String,Object> getStateSQL(Map<String, Object> objs) {
		return null;
	}


	@Override
	public void logBefore() {
		logger.info("开始迁移出生登记信息信息");
	}

	@Override
	public void logLook(int i) {
		logger.info("查询出生登记信息" + i + "条");
	}

	@Override
	public void logAfter(JdbcTemplate jdbcTemplate,Map<String, Object> objs) {
		//更新下上传状态 alter table t_chi_person add cert_upload smallint default 0;
		String update="update t_chi_person set cert_upload=1 where id="+objs.get("XSE_BM");
		jdbcTemplate.execute(update);
		
		logger.info("出生登记上传["+update+"]");
	}

	@Override
	public String getDeleteSql() {
		StringBuffer buffer = new StringBuffer();
		if ("TRUE".equals(getDel_flag())) {

			logger.info("开始删除已有的出生登记信息");
			buffer.append("DELETE FROM CA_NEONATE_VISIT");
		}
		return buffer.toString();
	}

	public static void main(String[] args) {
		//System.out.println(new Upload_Family_Visit2().getSQL());
	}
	
}
