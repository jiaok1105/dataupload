package fuyoubaojian.qfcode.etl;

import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import fuyoubaojian.qfcode.utils.AddressUtil;
import fuyoubaojian.qfcode.utils.TimeUtils;


/**
 * 出生医学登记数据D201
 */
@Component
public class Upload_cszmqf extends BaseTask implements TaskInterface {
	private static final Logger logger = Logger.getLogger(Upload_cszmqf.class);
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
//		startDate = "2017-01-01";
//		endDate = "2017-12-31";
		
		
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
		logger.info("开始迁移【" + date1 + "到" + date2 + "】的出生证-签发");
		// 查询结果
		StringBuffer buffer = new StringBuffer();

		buffer.append("  select distinct  ")//  
		.append("  concat('131000',cp.id) as  SJXT_ID,  ")//  市级人口出生信息系统中对新生儿的唯一编码SAN..80必填,系统主键（上传规则为地市级行政区划代码+市级系统主键，如石家庄市则该字段上传规则为130100+市级系统id）
		.append("  cp.id as  XSE_BM,  ")//  市内新生儿唯一标识SAN.80
		.append("  cd.cert_no as  CSYXZMBH,  ")//  新生儿纸质《出生医学证明》的顺序号SAN.10必填
		.append("  cp.name as  XSE_XM,  ")//  新生儿拟在公安管理部门正式登记注册的姓名SA..50必填
		.append("  cp.sex as  XSE_XBDM,  ")//  新生儿生理性别的代码SN1S101-01必填
		.append("  DATE_FORMAT(cp.birthday,'%Y-%m-%d %H:%i:%S') as XSE_CSRQ,")//  新生儿出生当日的公元纪年日期和时间的完整描述DTDT12必填，YYYYMMDDHHMM
		.append("  (cp.yz_week*7+cp.yz_day) as  CS_YZ,  ")//  新生儿出生时母亲妊娠时长，计量单位为天NN2..3必填（机构外不必填）
		.append("  cp.weight as  CS_TZ,  ")//  新生儿出生后1h内体重的测量值，计量单位为gNN3..4300-9999必填（机构外不必填）
		.append("  cp.height as  CS_SC,  ")//  新生儿出生后1h内身长的测量值，计量单位为cmNN2,125.0-70.0必填（机构外不必填）
		.append("  drsheng.text as  CSDZ_S,  ")//  新生儿出生地址中的省、自治区或直辖市名称SA..70必填
		.append("  drshi.text as  CSDZ_SD,  ")//  新生儿出生地址中的市、地区或州的名称SA..70必填
		.append("  drxian.text as  CSDZ_XQ,  ")//  新生儿出生地址中的县、市或区名称SA..70必填
		.append("  substr(cp.cs_region,1,6) as  CSD_QHDM,  ")//  新生儿出生地的县级及县级以上行政区划的代码SN6S101-03必填
		.append("  case cp.hospital  when cp.hospital then cp.hospital else '/' end as  YLJGMC,  ")//  助产机构的名称SAN..100必填，医疗卫生机构外分娩填“/”
		.append("  if(cp.deliver='','/',cp.deliver) as  JSRYXM,  ")//  接生人员在公安户籍管理部门登记注册的姓名SA.50必填，若无接生人员，则填“/”
		.append("  pm.name as  MQ_XM,  ")//  新生儿母亲在公安管理部门登记注册的姓名SA..50
		.append("  pm.birthday as  MQ_CSRQ,  ")//  母亲出生时的公元纪年日期DD8YYYYMMDD
		.append("  year(from_days(datediff(gl.labor_date, pm.birthday))) as  MQ_NL,  ")//  新生儿出生时的母亲年龄SN2
		.append("  tc.code as  MQ_GJ,  ")//  母亲所属国籍SN3S101-04
		.append("  if(ds2.`code` = '99' or ds2.`code` is null or length(ds3.`code`)>2,'97',CAST(ds2.code AS SIGNED)) as  MQ_MZ, ")//  母亲所属民族SN1..2S101-02
		.append("  drsheng1.text as  MQ_HJ,  ")//  母亲户口所在地的省级行政区划名称SA..70
		.append("  drsheng1.code as  MQ_HJSJDM,  ")//  母亲户口所在地的省级行政区划名代码SN2
		.append("  pm.xzaddr as  MQ_ZZ,  ")//  母亲家庭地址的省、市、县、乡镇、村及门牌号码SAN..200
		.append("  drxian1.code as  MQ_ZZXZQHDM,  ")//  母亲家庭地址的中华人民共和国县级及县级以上行政区划的代码SN6S101-03
		.append("  case pm.card_type_code when '01' then '01' when '02' then '06' when '04' then '03' else '99'  end as  MQ_SFZJLX,  ")//  母亲有效身份证件的类别SN2S101-05
		.append("  if(pm.card_type_code='99','其他证件名称','') as  MQ_QTSFZJLBMC,  ")//  母亲其他的有效身份证件类别名称SA..50
		.append("  pm.card_id as  MQ_SFZJHM,  ")//  母亲有效身份证件上唯一的法定标识符SAN..18
		.append("  pd.name as  FQ_XM,  ")//  父亲在公安管理部门登记注册的姓名SA..50
		.append("  pd.birthday as  FQ_CSRQ,  ")//  父亲出生时的公元纪年日期DD8YYYYMMDD
		.append("  year(from_days(datediff(pd.add_time, pd.birthday))) as  FQ_NL,  ")//  新生儿出生时的父亲年龄NN2
		.append("  tc1.code as  FQ_GJ,  ")//  父亲所属国籍SN3S101-04
		.append("  if(ds2.`code` = '99' or ds2.`code` is null or length(ds3.`code`)>2,'97',CAST(ds3.code AS SIGNED)) as  FQ_MZ,  ")//  父亲所属民族SN1..2S101-02
		.append("  pd.xzaddr as  FQ_ZZ,  ")//  父亲家庭地址的省、市、县、乡镇、村及门牌号码SAN..200
		.append("  drxian2.code as  FQ_ZZXZQHDM,  ")//  父亲家庭地址的中华人民共和国县级及县级以上行政区划的代码SN6S101-03
		.append("  case pd.card_type_code when '01' then '01' when '02' then '06' when '04' then '03' else '99'  end  as  FQ_SFZJLX,  ")//  父亲有效身份证件的类别SN2S101-05
		.append("  if(pd.card_type_code='99','其他证件名称','') as  FQ_QTSFZJLBMC,  ")//  父亲其他的有效身份证件类别名称SA..50
		.append("  pd.card_id as  FQ_SFZJHM,  ")//  父亲身份证件上唯一的法定标识符SAN..18
		.append("   cd.lz_name as  LZR_XM,  ")//  领证人员在公安管理部门登记注册的姓名SA..50必填
		.append("  case cd.lz_relation when '52' then '1' when '51' then '2' when cd.lz_relation in('61','62','63','64') then '3' else '9' end  as  LZR_YXSEGX,  ")//  领证人与新生儿的关系SN1S101-11必填
		.append("  case cd.lz_card_type when '01' then '01' when '02' then '06' when '04' then '03' else '99'  end as  LZR_SFZJLB,  ")//  领证人有效身份证件的类别SN2S101-05必填
		.append("  if(cd.lz_card_type='99','其他证件名称','')  as  LZR_QTSFZJLBMC,  ")//  领证人其他的有效身份证件类别名称SA..50
		.append("  cd.lz_card_id as  LZR_SFZJHM,  ")//  领证人有效身份证件上唯一的法定标识符SA..18必填
		.append("  hh.jg_name as  QFJG_MC,  ")//  《出生医学证明》签发机构的组织机构名称SAN..100必填
		.append("  hh.dx_code as  QFJG_QHDM,  ")//  《出生医学证明》签发机构所在县区的县级及以上行政区划代码SN6S101-03必填
		.append("  hh.zzjg_code as  QFJG_ZZJGDM,  ")//  《出生医学证明》签发机构的组织机构代码SAN..50必填, 对应着机构数据中的系统编码字段
		.append("  null as  QFJG_LBDM,  ")//  《出生医学证明》签发机构的类别代码SN4WS218-2002
		.append("  hd.name as  QF_RY,  ")//  签发人员在公安管理部门登记注册的姓名SA..50必填
		.append("  case  when cd.distr_type='11' and gl.id is not null then '1'   ")//  
		.append("  when cd.distr_type='12' and gl.id is not null then '2'   ")//  
		.append("  when cd.distr_type='13' and gl.id is not null then '3'   ")//  
		.append("  when cd.distr_type='14' and gl.id is null then '4'   ")//  
		.append("  when cd.distr_type='11' and gl.id is null then '4'   ")//  
		.append("  when cd.distr_type='12' and gl.id is null then '5'   ")//  
		.append("  when cd.distr_type='13' and gl.id is null then '6' end  as  QF_LX,  ")//  《出生医学证明》的签发类型SN1S101-06必填
		.append("  cd.past_cert_no as  YZ_BH,  ")//  新生儿原《出生医学证明》上的编号SAN..10换发、补发时录入
		.append("  if(cd.change_situation = '1','1','2')  as  YZ_JHQK,  ")//  原《出生医学证明》正页、副页交回情况SN1S101-13换发时必填
		.append("  if(cd.distr_type = '12','9','') as  HFYY,  ")//  《出生医学证明》的换发原因SN1S101-09换发时必填
		.append("  '' as  QTHFYY,  ")//  《出生医学证明》其他换发原因SAN..100
		.append("  if(cd.distr_type = '13','1','')   as  BFYY,  ")//  《出生医学证明》的补发原因SN2S101-10补发时必填
		.append("  '' as  QTBFYY,  ")//  《出生医学证明》其它补发原因SAN..100
		.append("  DATE_FORMAT(cd.print_time,'%Y%m%d') as  QF_RQ,   ")//  机构签发《出生医学证明》的公元纪年日期DD8必填，YYYYMMDD
		.append("  cd.memo as  QF_BZ,  ")//  对整个签发信息的备注说明SAN..200
		.append("  '' as  SHRY,  ")//  审核人员在公安管理部门正式登记注册的姓氏和名称SA..50换发、补发时录入
		.append("  cd.lz_ti_go_cai_liao as  LZRXTJDZMCL,  ")//  领证人需提交的证明材料代码SN30S101-15若有多个证明材料，用逗号加空格“, ”隔开，如：1, 2, 3
		.append("  '' as  QTZMCL,  ")//  领证人需提交其它证明材料SAN..500
		.append("  if(cd.update_time is NULL,DATE_FORMAT(cd.add_time,'%Y-%m-%d %H:%i:%S'),DATE_FORMAT(cd.update_time,'%Y-%m-%d %H:%i:%S')) as  SJXRSJ,cd.id as cert_id ")//  最后修改时的公元纪年日期和时间的完整描述DTDT12
		
		.append("  FROM t_cert_distr cd    ")
		.append("  join t_chi_person cp on cp.id=cd.chi_person_id  ")//  
		.append("  left join t_arc_person pm on pm.id=cp.mother_id  ")//  
		.append("  left join t_arc_person pd on pd.id=cp.father_id  ")//  
		.append("  left join t_gra_labor gl on gl.person_id = cp.gravida_id  ")//  
		//.append("  left join t_gra_child gc on gc.id=cp.gra_child_id  ")//  

		//.append("  left join t_hos_doctor hdj on hdj.user_id=gc.deliver_id  ")//  接生人		
		.append("  join t_hos_doctor hd on hd.user_id=cd.add_user_id  ")//  签发人员
		//.append("  left join t_hos_doctor hd1 on hd1.user_id=cd.update_user_id  ")//  更新人员
		
		.append("  join t_hos_hospital_temp181229 hh on hh.hos_id=hd.hospital_id  ")//  签发单位
		//.append("  left join t_hos_hospital_temp181229 hhj on hhj.hos_id=hdj.hospital_id  ")//  接生单位
		
		.append("  left join temp_countrycode tc on tc.dscode = pm.country_code  ")//  
		.append("  left join temp_countrycode tc1 on tc1.dscode = pd.country_code  ")//  
		.append("  left join t_dic_system ds2 on ds2.`code`=pm.nation_code and ds2.type='民族'  ")//  
		.append("  left join t_dic_system ds3 on ds3.`code`=pd.nation_code and ds3.type='民族'  ")//  
		
		.append("  join t_dic_region drsheng on drsheng.code=substr(cp.cs_region,1,2)  ")//  
		.append("  join t_dic_region drshi on drshi.code=substr(cp.cs_region,1,4)  ")//  
		.append("  join t_dic_region drxian on drxian.code=substr(cp.cs_region,1,6)  ")//  
		.append("  left join t_dic_region drsheng1 on drsheng1.code=substr(pm.hk_region_code,1,2)   ")//  
		.append("  left join t_dic_region drxian1 on drxian1.code=substr(pm.hk_region_code,1,6)  ")//  
		.append("  left join t_dic_region drxian2 on drxian2.code=substr(pd.hk_region_code,1,6)  ")//  
		.append("  where  cd.delete_flag='0' and cd.distr_type in ('11','12','13','14') ")
        .append("  and cd.add_time").append(">= ").append(date1).append(" and cd.add_time <= ").append(date2);
		
		logger.info(buffer.toString());
		return buffer.toString();
	}

	@Override
	public String getUpdateSQL(Map<String, Object> objs) {
		// 更新目标库
		StringBuffer buffer = new StringBuffer();
		buffer.append("INSERT INTO d301 VALUES(");
		buffer.append("'").append(AddressUtil.toStr(objs.get("SJXT_ID"))).append("',");//D301-01系统ID市级人口出生信息系统中对新生儿的唯一编码AN..80必填,系统主键（上传规则为地市级行政区划代码+市级系统主键，如石家庄市则该字段上传规则为130100+市级系统id）
		buffer.append("'").append(AddressUtil.toStr(objs.get("XSE_BM"))).append("',");//D301-02新生儿编码市内新生儿唯一标识AN.80
		buffer.append("'").append(AddressUtil.toStr(objs.get("CSYXZMBH"))).append("',");//D301-03出生医学证明编号新生儿纸质《出生医学证明》的顺序号AN.10必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("XSE_XM"))).append("',");//D301-04新生儿姓名新生儿拟在公安管理部门正式登记注册的姓名A..50必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("XSE_XBDM"))).append("',");//D301-05性别新生儿生理性别的代码N1必填
		
		buffer.append("to_date('").append(AddressUtil.toStr(objs.get("XSE_CSRQ"))).append("','yyyy-mm-dd hh24:mi:ss'),");//D301-06出生时间新生儿出生当日的公元纪年日期和时间的完整描述DT12必填，YYYYMMDDHHMM
		buffer.append("'").append(AddressUtil.toStr(objs.get("CS_YZ"))).append("',");//D301-07出生孕周（天）新生儿出生时母亲妊娠时长，计量单位为天N2..3必填（机构外不必填）
		buffer.append("'").append(AddressUtil.toStr(objs.get("CS_TZ"))).append("',");//D301-08出生体重（g）新生儿出生后1h内体重的测量值，计量单位为gN3..4必填（机构外不必填）
		buffer.append("'").append(AddressUtil.toStr(objs.get("CS_SC"))).append("',");//D301-09出生身长（cm）新生儿出生后1h内身长的测量值，计量单位为cmN2,1必填（机构外不必填）
		buffer.append("'").append(AddressUtil.toStr(objs.get("CSDZ_S"))).append("',");//D301-10出生地点-省（自治区、直辖市）新生儿出生地址中的省、自治区或直辖市名称A..70必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("CSDZ_SD"))).append("',");//D301-11出生地点-市（地区、州）新生儿出生地址中的市、地区或州的名称A..70必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("CSDZ_XQ"))).append("',");//D301-12出生地点-县（市、区）新生儿出生地址中的县、市或区名称A..70必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("CSD_QHDM"))).append("',");//D301-13出生地点_行政区划代码新生儿出生地的县级及县级以上行政区划的代码N6必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("YLJGMC"))).append("',");//D301-14医疗机构名称助产机构的名称AN..100必填，医疗卫生机构外分娩填“/”
		buffer.append("'").append(AddressUtil.toStr(objs.get("JSRYXM"))).append("',");//D301-15接生人员接生人员在公安户籍管理部门登记注册的姓名A.50必填，若无接生人员，则填“/”
		buffer.append("'").append(AddressUtil.toStr(objs.get("MQ_XM"))).append("',");//D301-16母亲姓名新生儿母亲在公安管理部门登记注册的姓名A..50
		String mqcssj = AddressUtil.toStr(objs.get("MQ_CSRQ"));//D301-17母亲出生日期母亲出生时的公元纪年日期D8YYYYMMDD
		if(mqcssj.length()>7)
			buffer.append("to_date('").append(mqcssj.replace("-","")).append("','yyyyMMdd'),");
		else
			buffer.append("'").append(mqcssj).append("',");
		
		buffer.append("'").append(AddressUtil.toStr(objs.get("MQ_NL"))).append("',");//D301-18母亲年龄新生儿出生时的母亲年龄N2
		buffer.append("'").append(AddressUtil.toStr(objs.get("MQ_GJ"))).append("',");//D301-19母亲国籍母亲所属国籍N3
		buffer.append("'").append(AddressUtil.toStr(objs.get("MQ_MZ"))).append("',");//D301-20母亲民族母亲所属民族N1..2
		buffer.append("'").append(AddressUtil.toStr(objs.get("MQ_HJ"))).append("',");//D301-21母亲户籍所在地母亲户口所在地的省级行政区划名称A..70
		buffer.append("'").append(AddressUtil.toStr(objs.get("MQ_HJSJDM"))).append("',");//D301-22母亲户籍所在地省级行政区划代码母亲户口所在地的省级行政区划名代码N2
		buffer.append("'").append(AddressUtil.toStr(objs.get("MQ_ZZ"))).append("',");//D301-23母亲住址母亲家庭地址的省、市、县、乡镇、村及门牌号码AN..200
		buffer.append("'").append(AddressUtil.toStr(objs.get("MQ_ZZXZQHDM"))).append("',");//D301-24母亲住址行政区划代码母亲家庭地址的中华人民共和国县级及县级以上行政区划的代码N6
		buffer.append("'").append(AddressUtil.toStr(objs.get("MQ_SFZJLX"))).append("',");//D301-25母亲有效身份证件类型母亲有效身份证件的类别N2
		buffer.append("'").append(AddressUtil.toStr(objs.get("MQ_QTSFZJLBMC"))).append("',");//D301-26母亲其他有效身份证件类别名称母亲其他的有效身份证件类别名称A..50
		buffer.append("'").append(AddressUtil.toStr(objs.get("MQ_SFZJHM"))).append("',");//D301-27母亲有效身份证件号码母亲有效身份证件上唯一的法定标识符AN..18
		buffer.append("'").append(AddressUtil.toStr(objs.get("FQ_XM"))).append("',");//D301-28父亲姓名父亲在公安管理部门登记注册的姓名A..50
		String fqcssj = AddressUtil.toStr(objs.get("FQ_CSRQ"));//D301-29父亲出生日期父亲出生时的公元纪年日期D8YYYYMMDD
		if(fqcssj.length()>7)
			buffer.append("to_date('").append(fqcssj.replace("-","")).append("','yyyyMMdd'),");
		else
			buffer.append("'").append(fqcssj).append("',");

		buffer.append("'").append(AddressUtil.toStr(objs.get("FQ_NL"))).append("',");//D301-30父亲年龄新生儿出生时的父亲年龄N2
		buffer.append("'").append(AddressUtil.toStr(objs.get("FQ_GJ"))).append("',");//D301-31父亲国籍父亲所属国籍N3
		buffer.append("'").append(AddressUtil.toStr(objs.get("FQ_MZ"))).append("',");//D301-32父亲民族父亲所属民族N1..2
		buffer.append("'").append(AddressUtil.toStr(objs.get("FQ_ZZ"))).append("',");//D301-33父亲住址父亲家庭地址的省、市、县、乡镇、村及门牌号码AN..200
		buffer.append("'").append(AddressUtil.toStr(objs.get("FQ_ZZXZQHDM"))).append("',");//D301-34父亲住址行政区划代码父亲家庭地址的中华人民共和国县级及县级以上行政区划的代码N6
		buffer.append("'").append(AddressUtil.toStr(objs.get("FQ_SFZJLX"))).append("',");//D301-35父亲有效身份证件类型父亲有效身份证件的类别N2
		buffer.append("'").append(AddressUtil.toStr(objs.get("FQ_QTSFZJLBMC"))).append("',");//D301-36父亲其他有效身份证件类别名称父亲其他的有效身份证件类别名称A..50
		buffer.append("'").append(AddressUtil.toStr(objs.get("FQ_SFZJHM"))).append("',");//D301-37父亲有效身份证件号码父亲身份证件上唯一的法定标识符AN..18
		buffer.append("'").append(AddressUtil.toStr(objs.get("LZR_XM"))).append("',");//D301-38领证人员姓名领证人员在公安管理部门登记注册的姓名A..50必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("LZR_YXSEGX"))).append("',");//D301-39领证人与新生儿关系领证人与新生儿的关系N1必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("LZR_SFZJLB"))).append("',");//D301-40领证人有效身份证件类别领证人有效身份证件的类别N2必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("LZR_QTSFZJLBMC"))).append("',");//D301-41领证人其他有效身份证件类别名称领证人其他的有效身份证件类别名称A..50
		buffer.append("'").append(AddressUtil.toStr(objs.get("LZR_SFZJHM"))).append("',");//D301-42领证人有效身份证件号码领证人有效身份证件上唯一的法定标识符A..18必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("QFJG_MC"))).append("',");//D301-43签发机构名称《出生医学证明》签发机构的组织机构名称AN..100必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("QFJG_QHDM"))).append("',");//D301-44签发机构行政区划代码《出生医学证明》签发机构所在县区的县级及以上行政区划代码N6必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("QFJG_ZZJGDM"))).append("',");//D301-45签发机构的组织机构代码《出生医学证明》签发机构的组织机构代码AN..50必填, 对应着机构数据中的系统编码字段
		buffer.append("'").append(AddressUtil.toStr(objs.get("QFJG_LBDM"))).append("',");//D301-46签发机构类别代码《出生医学证明》签发机构的类别代码N4
		buffer.append("'").append(AddressUtil.toStr(objs.get("QF_RY"))).append("',");//D301-47签发人员签发人员在公安管理部门登记注册的姓名A..50必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("QF_LX"))).append("',");//D301-48签发类型《出生医学证明》的签发类型N1必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("YZ_BH"))).append("',");//D301-49原出生医学证明编号新生儿原《出生医学证明》上的编号AN..10换发、补发时录入
		buffer.append("'").append(AddressUtil.toStr(objs.get("YZ_JHQK"))).append("',");//D301-50原证件正、副页交回情况原《出生医学证明》正页、副页交回情况N1换发时必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("HFYY"))).append("',");//D301-51换发原因类别《出生医学证明》的换发原因N1换发时必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("QTHFYY"))).append("',");//D301-52其他换发原因《出生医学证明》其他换发原因AN..100
		buffer.append("'").append(AddressUtil.toStr(objs.get("BFYY"))).append("',");//D301-53补发原因类别《出生医学证明》的补发原因N2补发时必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("QTBFYY"))).append("',");//D301-54其它补发原因《出生医学证明》其它补发原因AN..100
		buffer.append("to_date('").append(AddressUtil.toStr(objs.get("QF_RQ"))).append("','yyyyMMdd'),");//D301-55签发日期机构签发《出生医学证明》的公元纪年日期D8必填，YYYYMMDD
		buffer.append("'").append(AddressUtil.toStr(objs.get("QF_BZ"))).append("',");//D301-56签发备注对整个签发信息的备注说明AN..200
		buffer.append("'").append(AddressUtil.toStr(objs.get("SHRY"))).append("',");//D301-57审核人员审核人员在公安管理部门正式登记注册的姓氏和名称A..50换发、补发时录入
		buffer.append("'").append(AddressUtil.toStr(objs.get("LZRXTJDZMCL"))).append("',");//D301-58领证人需提供和提交的证明材料领证人需提交的证明材料代码N30若有多个证明材料，用逗号加空格“, ”隔开，如：1, 2, 3
		buffer.append("'").append(AddressUtil.toStr(objs.get("QTZMCL"))).append("',");//D301-59其它证明材料领证人需提交其它证明材料AN..500
//		buffer.append("to_date('").append(AddressUtil.toStr(objs.get("SJXRSJ"))).append("','yyyy-mm-dd hh24:mi:ss')");// D101-11数据写入时间最后修改时的公元纪年日期和时间的完整描述DT必填
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
		logger.info("开始迁移出生证-签发");
	}

	@Override
	public void logLook(int i) {
		logger.info("查询新出生证-签发" + i + "条");
	}

	@Override
	public void logAfter(JdbcTemplate jdbcTemplate,Map<String, Object> objs) {
		//更新下上传状态 alter table t_cert_distr add cert_upload smallint default 0;
		String update="update t_cert_distr set cert_upload=1 where id="+objs.get("cert_id");
		jdbcTemplate.execute(update);
		
		logger.info("出生证-签发上传["+update+"]");
	}


	@Override
	public String getDeleteSql() {
		String date1 = this.getStartDate();
		String date2 = this.getEndDate();

		StringBuffer buffer = new StringBuffer();
		if ("TRUE".equals(getDel_flag())) {

			logger.info("开始删除已有的出生证-签发");
			buffer.append("DELETE FROM CA_NEONATE_VISIT");
		}
		return buffer.toString();
	}
	
	public static void main(String[] args) {
		//System.out.println(new Upload_cszmqf().getSQL());
	}

}
