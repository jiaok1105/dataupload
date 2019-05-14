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
 * 妇女管理卡产前随访上传
 */
@Component
public class Upload_GraManageCq_Visit extends BaseTask implements TaskInterface {
	private static final Logger logger = Logger.getLogger(Upload_GraManageCq_Visit.class);
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
		logger.info("开始上传【" + date1 + "到" + date2 + "】的妇女管理卡产前随访记录信息");
		// 查询结果
		StringBuffer buffer = new StringBuffer();
		logger.info("开始查询妇女管理卡产前随访记录信息");
		logger.info("+++++++++++++++++++++++++++++++======SQL:");
		buffer.append(" SELECT	 gfv.id as gragfvid,");
		//buffer.append(" '0' as machineId ,	");// 设备 id（同DealerId） ,是 ,String
		buffer.append(" apv.card_id as idcard ,	");// 身份证号码 ,是 ,String
		buffer.append(" apv.name as name ,	");// 姓名 ,是 ,String
		buffer.append(" apv.id as grdabh ,	");// 个人档案编号,是 ,String
		buffer.append(" gbc.yc_num as jkid ,	");// 卡号:根据管理卡个数递增,是 ,String    孕次
		buffer.append(" '无' as bchao ,	");// B 超,是 ,String
		buffer.append(" ALB.result_statue  as bdb ,	");// 白蛋白,是 ,String
		buffer.append(" gfv.weight_index as bmi ,	");// bmi,是 ,String
		buffer.append(" WBC.result_statue as bxb ,	");// 白细胞计数值,是 ,String
		buffer.append(" concat(if(gfv.deal_with like '%个人卫生%','1','0'),',',	");
		buffer.append(" if(gfv.deal_with like '%心理%','1','0'),',',	");
		buffer.append(" if(gfv.deal_with like '%膳食%','1','0'),',',	");
		buffer.append(" if(gfv.deal_with like '%避免致畸因素和疾病对胚胎的不良影响%','1','0'),',',	");
		buffer.append(" if(gfv.deal_with like '%产前筛查宣传告知%','1','0'),',',	");
		buffer.append(" if(gfv.deal_with like '%生活方式%','1','0')) as byzd ,	");// 多选保健指导：个人卫生，心理，营养，避免致畸因素和疾病对胚胎的不良影响，产前筛查宣传告知，其他(byzdqt),是 ,String
		buffer.append(" if(gfv.deal_with like '%生活方式%','生活方式','') as byzdqt ,	");// 保健指导其他,是 ,String
		buffer.append(" gbc.ydfm_num as chanci ,	");// (阴道分娩)产次,是 ,String
		buffer.append(" gbc.jx_num as csqxe ,	");// 孕产史(出生缺陷儿),是 ,String
		buffer.append(" case gfv.lung WHEN '2' then '呼吸音粗' WHEN '3' THEN '肺部罗音' WHEN '4' THEN '其他' else '无' end as fbyc ,	");// 肺部异常其他,是 ,String
		buffer.append(" if(gfv.lung in('2','3','4'),'1','0') as feibu ,	");// 肺部 0：未见异常；1：异常,是 ,String
		buffer.append(" case gfv.annex when '2' then '有肿块' when '3' then '其他' when '4' then '拒检' when '1' then '未查' else '无' end as fjyc ,	");// 附件异常其他,是 ,String
		buffer.append(" if(gbc.hasops='' or gbc.hasops is NULL or gbc.hasops like '%99%','0','1') as fksss ,	");// 妇科手术史 0：无1：有,是 ,String
		buffer.append(" if(gbc.hasops like '%8%','其他','无') as fksssqt ,	");// 妇科手术史其他是 String
		buffer.append(" if(gfv.annex='9' or gfv.annex='' or gfv.annex is NULL,'0','1') as fujian ,	");// 附件 0：未见异常；1：异常是 String
		buffer.append(" case gfv.uterine_neck when '2' then '外口粘合' when '3' then '宫颈瘢痕' when '4' then '宫颈水肿'	");
		buffer.append(" when '5' then '宫颈坚韧' when '6' then '宫颈肌瘤' when '7' then '其他' else '无' end as gjyc ,	");// 宫颈异常其他是 String
		buffer.append(" if(gfv.uterine_neck='9' or gfv.uterine_neck='' or gfv.uterine_neck is NULL,'0','1') as gongji ,	");// 宫颈 0：未见异常；1：异常是 String
		buffer.append(" concat(if(gbc.grs_code like '%99%','1','0'),',',if(gbc.grs_code like '%1%','1','0'),',',if(gbc.grs_code like '%2%','1','0')	,','");
		buffer.append(" ,if(gbc.grs_code like '%3%','1','0'),',',if(gbc.grs_code like '%4%','1','0'),',',if(gbc.grs_code like '%5%','1','0'),',',if(gbc.grs_code like '%7%','1','0')) as grs ,	");// 多选个人史：无，吸烟，饮酒，服用药物，接触有毒有害物质，接触放射线，其他(grsqt)是 String
		buffer.append(" '' as grsqt ,	");// 个人史其他是 String
		buffer.append(" case when HIV.result_statue='阴性' then '0' when HIV.result_statue='阳性' then '1' else '2' end as hiv ,	");// HIV 抗体检测 0:阴性， 1 阳性， 2：未检查是 String
		buffer.append(" TBiL.result as jhdhs ,	");// 结合胆红素(μmol/L)是 String
		buffer.append(" concat(if(gbc.jwbs_code='' or gbc.jwbs_code is NULL or gbc.jwbs_code LIKE '%99%','1','0'),',',if(gbc.jwbs_code like '%01%','1','0'),',',if(gbc.jwbs_code like '%12%','1','0'),','	");
		buffer.append(" ,if(gbc.jwbs_code like '%11%','1','0'),',',if(gbc.jwbs_code like '%07%','1','0'),',',if(gbc.jwbs_code like '%05%','1','0'),','	");
		buffer.append(" ,if(gbc.jwbs_code like '%06%','1','0'),',',if(gbc.jwbs_code like '%18%','1','0')) as jws ,	");//-- 多选既往病史是 String 无，心脏病，肾脏疾病，肝脏疾病，高血压，贫血，糖尿病，其他(jwsqt), 1：已选中0：未选中是 String
		buffer.append(" ''  as jwsqt ,	");// 既往病史其他是 String
		//buffer.append(" concat(if(gbc.jzs_code like '%99%' or gbc.jzs_code='' or gbc.jzs_code is NULL,'1','0'),',',	");
		buffer.append(" concat(if(gbc.jzs_code like '%99%' or gbc.jzs_code='' or gbc.jzs_code is NULL,'1','0'),',',	 if(gbc.jzs_code like '%13%','1','0'),',',if(gbc.jzs_code like '%12%','1','0'),',',if(gbc.jzs_code like '%15%' or gbc.jzs_code like '%01%' or gbc.jzs_code like '%02%' or gbc.jzs_code like '%03%' or gbc.jzs_code like '%04%' or gbc.jzs_code like '%05%' or gbc.jzs_code like '%06%' or gbc.jzs_code like '%07%' or gbc.jzs_code like '%08%' or gbc.jzs_code like '%09%' or gbc.jzs_code like '%10%' or gbc.jzs_code like '%11%' or gbc.jzs_code like '%14%','1','0')) as jzs,	");//多选家族史：无，遗传性疾病史，精神疾病史，其他(jzsqt)是 String
		buffer.append(" '' as jzsqt ,	");// 家族史其他是 String
		buffer.append(" (gbc.zrlc_num+gbc.rglc_num) as liuchan ,	");// 孕产史(流产)是 String
		buffer.append(" DATE_FORMAT(gbc.last_menses,'%Y-%m-%d') as mcyj ,	");// 末次月经是 String
		buffer.append(" '' as mcyjbx ,	");// 末次月经不详是 String
		buffer.append(" RPR.result_statue as mdxqsy ,	");// 梅毒血清学试验 0:阴性， 1 阳性， 2：未检查是 String
		//buffer.append(" apv.name as name ,	");// 姓名是 String
		buffer.append(" '无' as ncgqt ,	");// 尿 常 规其他是 String
		buffer.append(" PRO1.result_statue as ndb ,	");// 尿 常 规(尿蛋白)是 String
		buffer.append(" '无' as niaotang ,	");// 尿 常 规(尿糖)是 String
		buffer.append(" '无' as nqx ,	");// 尿 常 规(尿潜血)是 String
		buffer.append(" '无' as ntt ,	");// 尿 常 规(尿酮体)是 String
		buffer.append(" gbc.pgc_num as pgc ,	");// 产次(剖宫产)是 String
		buffer.append(" gfv.height as shengao ,	");// 身高是 String
		buffer.append(" gbc.sc_num as sichan ,	");// 孕产史(死产)是 String
		buffer.append(" gbc.st_num as sitai ,	");// 孕产史(死胎)是 String
		buffer.append(" apv.hkaddr as sssq ,	");// 所属行政区划是 String
		buffer.append(" DATE_FORMAT(gbc.add_time,'%Y-%m-%d') as tbrq ,	");// 填表日期是 String
		buffer.append(" gbc.yz_week as tbyz ,	");// 填表孕周(周)是 String
		buffer.append(" gbc.yz_day as tbyztian ,	");// 填表孕周(天)是 String
		buffer.append(" gfv.weight as tizhong ,	");// 体重是 String
		buffer.append(" if(gfv.vulvae='2','1','0') as waiyin ,	");// 外阴 0：未见异常；1：异常是 String
		buffer.append(" '无' as wyyc ,	");// 外阴异常其他是 String
		buffer.append(" '无' as xcgqt ,	");// 血 常 规其他是 String
		buffer.append(" HGB.result_statue as xhdb ,	");// 血 常 规(血红蛋白值)是 String
		buffer.append(" PLT.result_statue as xxb ,	");// 血小板计数值是 String
		buffer.append(" if(gfv.heart in('2','3','4','5','6'),'1','0') as xinzang ,	");// 心脏 0：未见异常；1：异常是 String
		buffer.append(" BUN.result_statue as xnsd ,	");// 血尿素氮是 String
		buffer.append(" ALT.result_statue as xqgbzam ,	");// 血清谷丙转氨酶是 String
		buffer.append(" AST.result_statue as xqgczam ,	");// 血清谷草转氨酶是 String
		buffer.append(" CREA.result_statue as xqjg ,	");// 血清肌酐是 String
		buffer.append(" gbc.newbabydeath as xsesw ,	");// 孕产史(新生儿死亡)是 String
		buffer.append(" GLU.result_statue as xt ,	");// 血 糖是 String
		buffer.append(" ABO.result_statue as xuexiabo ,	");// 血型是 String
		buffer.append(" RH.result_statue as xuexirh ,	");// 血型 Rh是 String
		buffer.append(" PLT.result_statue as xxb ,	");// 血小板是 String
		buffer.append(" gfv.up_blood as xy ,	");// 血压是 String
		buffer.append(" gfv.low_blood as xy1 ,	");// 血压是 String
		buffer.append(" '无' as xzyc ,	");// 心脏异常其他是 String
		buffer.append(" gbc.expect_date as ycq ,	");// 预 产 期是 String
		buffer.append(" '0,0,0,1,0' as ydfmw ,	");// 多选阴道分泌物:未见异常，滴虫，假丝酵母菌，未检查，其他是 String
		buffer.append(" '' as ydfmwqt ,	");// 阴道分泌物其他是 String
		buffer.append(" '5' as ydqjd ,	");// 阴道清洁度:单选0：请选择， 1：Ⅰ度， 2：Ⅱ级， 3：Ⅲ级， 4：Ⅵ级，5：未检查是 String
		buffer.append(" '无' as ydyc ,	");// 阴道异常其他是 String
		buffer.append(" SUBSTR(gp.current_age FROM 1 FOR 2) as yfnl ,	");// 孕妇年龄是 String
		buffer.append(" if(gfv.cunt in('2','3','4','5','6'),'1','0') as yindao ,	");// 阴道 0：未见异常；1：异常是 String
		buffer.append(" gbc.yc_num as yunci ,	");// 孕次是 String
		buffer.append(" HBsAb.result_statue as yxgybmkt ,	");// 乙型肝炎表面抗体是 String
		buffer.append(" HBsAg.result_statue as yxgybmky ,	");// 乙型肝炎表面抗原是 String
		buffer.append(" HBeAb.result_statue as yxgyeke ,	");// 乙型肝炎 e 抗体是 String
		buffer.append(" HBeAg.result_statue as yxgyeky ,	");// 乙型肝炎 e 抗原是 String
		buffer.append(" HBcAb.result_statue as yxgykxkt ,	");// 乙型肝炎核心抗体是 String
		buffer.append(" TBiL.result_statue as zdhs,	");// 总胆红素是 String
		buffer.append(" apn.phone as zfdh ,	");// 丈夫电话是 String
		buffer.append(" apn.name as zfname ,	");// 丈夫姓名是 String
		buffer.append(" year(from_days(datediff(gbc.add_time,apn.birthday))) as zfnl ,	");// 丈夫年龄是 String
		buffer.append(" '无' as zgyc ,	");// 子宫异常其他是 String
		buffer.append(" if(gfv.changer='是','1','0') as zhuanzhen ,	");// 转 诊: 0 无， 1 有是 String
		buffer.append(" '0' as zigong ,	");// 子宫 0：未见异常；1：异常是 String
		buffer.append(" if(gfv.assess='' or gfv.assess is NULL,'0','1') as ztpg ,	");// 总体评估 0：未见异常； 1：异常是 String
		buffer.append(" gfv.assess as ztpgyc ,	");// 总体评估异常其他是 String
		buffer.append(" gfv.change_unit as zzjg ,	");// 转诊机构及科室是 String
		buffer.append(" gfv.change_cause as zzyy ,	");// 转诊原因是 String
		buffer.append(" DATE_FORMAT(gfv.add_time,'%Y-%m-%d') as czsj ,	");// 录入日期是 String
		buffer.append(" '' as czy ,	");// 对应新系统用户 id是 String
		buffer.append(" DATE_FORMAT(gfv.next_date,'%Y-%m-%d') as xcsfrq ,	");// 下次随访日期是 String
		buffer.append(" DATE_FORMAT(gfv.add_time,'%Y-%m-%d') as createdate ,	");// 录入日期是 String
		buffer.append(" '' as zzlxr ,	");// 转诊联系人是 String
		buffer.append(" '' as zzlxfs ,	");// 转诊联系人电话是 String
		buffer.append(" '' as zzresult ,	");// 转诊结果： 0：到位，1：不到位是 String
		buffer.append(" gfv.check_doctor_name as sfys	");// 随访医生是 String
		buffer.append(" FROM t_gra_first_visit gfv	");
		buffer.append(" JOIN t_gra_person AS gp ON gp.id=gfv.person_id and gp.statue='0'	");
		buffer.append(" JOIN t_arc_person AS apv ON apv.id=gp.base_info_id and apv.statue='0'	");
		buffer.append(" LEFT JOIN t_arc_person AS apn ON apn.id=gp.husband_info_id and apn.statue='0'	");
		buffer.append(" JOIN t_gra_build_copy AS gbc ON gbc.person_id=gp.id and gbc.statue='0'	");
		buffer.append(" LEFT JOIN t_gra_assist_check_fv AS ALB ON ALB.person_id=gfv.person_id and ALB.dic_assist_id='127' and ALB.statue='0'	");//-- 白蛋白浓度
		buffer.append(" LEFT JOIN t_gra_assist_check_fv AS WBC ON WBC.person_id=gfv.person_id and WBC.dic_assist_id='240' and WBC.statue='0'	");//-- 白细胞计数值
		buffer.append(" LEFT JOIN t_gra_assist_check_fv AS HIV ON HIV.person_id=gfv.person_id and HIV.dic_assist_id='191' and HIV.statue='0'	");//-- HIV抗体检测
		buffer.append(" LEFT JOIN t_gra_assist_check_fv AS TBiL ON TBiL.person_id=gfv.person_id and TBiL.dic_assist_id='120' and TBiL.statue='0'	");//-- 总胆红素值
		buffer.append(" LEFT JOIN t_gra_assist_check_fv AS RPR ON RPR.person_id=gfv.person_id and RPR.dic_assist_id='192' and RPR.statue='0'	");//-- 梅毒
		buffer.append(" LEFT JOIN t_gra_assist_check_fv AS PRO1 ON PRO1 .person_id=gfv.person_id and PRO1.dic_assist_id='240' and PRO1.statue='0'	");//-- 尿蛋白
		buffer.append(" LEFT JOIN t_gra_assist_check_fv AS HGB ON HGB.person_id=gfv.person_id and HGB.dic_assist_id='67' and HGB.statue='0'	");//-- 血红蛋白值
		buffer.append(" LEFT JOIN t_gra_assist_check_fv AS PLT ON PLT.person_id=gfv.person_id and PLT.dic_assist_id='82' and PLT.statue='0'	");//-- 血小板计数值
		buffer.append(" LEFT JOIN t_gra_assist_check_fv AS BUN ON BUN.person_id=gfv.person_id and BUN.dic_assist_id='131' and BUN.statue='0'	");//-- 血尿素氮检测值
		buffer.append(" LEFT JOIN t_gra_assist_check_fv AS ALT ON ALT.person_id=gfv.person_id and ALT.dic_assist_id='122' and ALT.statue='0'	");//-- 血清谷丙转氨酶
		buffer.append(" LEFT JOIN t_gra_assist_check_fv AS AST ON AST.person_id=gfv.person_id and AST.dic_assist_id='123' and AST.statue='0'	");//-- 血清谷草转氨酶
		buffer.append(" LEFT JOIN t_gra_assist_check_fv AS CREA ON CREA.person_id=gfv.person_id and CREA.dic_assist_id='132' and CREA.statue='0'	");//-- 血肌酐值
		buffer.append(" LEFT JOIN t_gra_assist_check_fv AS GLU ON GLU.person_id=gfv.person_id and GLU.dic_assist_id='184' and GLU.statue='0'	");//-- 血糖
		buffer.append(" LEFT JOIN t_gra_assist_check_fv AS ABO ON ABO.person_id=gfv.person_id and ABO.dic_assist_id='212' and ABO.statue='0'	");//-- ABO血型
		buffer.append(" LEFT JOIN t_gra_assist_check_fv AS RH ON RH.person_id=gfv.person_id and RH.dic_assist_id='204' and RH.statue='0'	");//-- RH血型
		buffer.append(" LEFT JOIN t_gra_assist_check_fv AS HBsAg ON HBsAg.person_id=gfv.person_id and HBsAg.dic_assist_id='136' and HBsAg.statue='0'	");//-- 乙型肝炎表面抗原
		buffer.append(" LEFT JOIN t_gra_assist_check_fv AS HBsAb ON HBsAb.person_id=gfv.person_id and HBsAb.dic_assist_id='137' and HBsAb.statue='0'	");//-- 乙型肝炎表面抗体
		buffer.append(" LEFT JOIN t_gra_assist_check_fv AS HBeAg ON HBeAg.person_id=gfv.person_id and HBeAg.dic_assist_id='138' and HBeAg.statue='0'	");//-- 乙型肝炎表面e抗原
		buffer.append(" LEFT JOIN t_gra_assist_check_fv AS HBeAb ON HBeAb.person_id=gfv.person_id and HBeAb.dic_assist_id='139' and HBeAb.statue='0'	");//-- 乙型肝炎表面e抗体
		buffer.append(" LEFT JOIN t_gra_assist_check_fv AS HBcAb ON HBcAb .person_id=gfv.person_id and HBcAb.dic_assist_id='140' and HBcAb.statue='0'	");//-- 乙型肝炎表面核心抗体
		buffer.append(" WHERE gfv.statue='0' ");//and apv.card_id='370829199802031127'
		buffer.append(" and gfv.check_date >= ").append(date1);
		buffer.append(" and gfv.check_date <= ").append(date2);

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
		String gragfvid = AddressUtil.toStr(objs.get("gragfvid"));//业务id
		String jsondata;
		Map<String,Object > data = new HashMap<String,Object>();
		Map<String, Object> dataInJson = new HashMap<String, Object>();
		Map<String, Object> itemInJson = new HashMap<String, Object>();
		/* AppCode */
		data.put("AppCode", "3101");
		data.put("InJsonString", dataInJson);
		/* InJsonString 98 true*/
		dataInJson.put("name", AddressUtil.toStr(objs.get("name")));
		dataInJson.put("idcard", AddressUtil.toStr(objs.get("idcard")));
		dataInJson.put("machineId", "JNWFFY001");
		dataInJson.put("item", itemInJson);
		/* item */
		itemInJson.put("grdabh", AddressUtil.toStr(objs.get("grdabh")));//个人档案编号
		itemInJson.put("jkid", AddressUtil.toStr(objs.get("jkid")));//卡号:根据管理卡个数递增
		itemInJson.put("bchao", AddressUtil.toStr(objs.get("bchao")));//B 超
		itemInJson.put("bdb", AddressUtil.toStr(objs.get("bdb")));//白蛋白
		itemInJson.put("bmi", AddressUtil.toStr(objs.get("bmi")));//bmi
		itemInJson.put("bxb", AddressUtil.toStr(objs.get("bxb")));//白细胞计数值
		itemInJson.put("byzd", AddressUtil.toStr(objs.get("byzd")));//保健指导
		itemInJson.put("byzdqt", AddressUtil.toStr(objs.get("byzdqt")));//保健指导其他
		itemInJson.put("chanci", AddressUtil.toStr(objs.get("chanci")));//(阴道分娩)产次
		itemInJson.put("csqxe", AddressUtil.toStr(objs.get("csqxe")));//孕产史(出生缺陷儿)
		itemInJson.put("fbyc", AddressUtil.toStr(objs.get("fbyc")));//肺部异常其他
		itemInJson.put("feibu", AddressUtil.toStr(objs.get("feibu")));//肺部 0：未见异常；1：异常
		itemInJson.put("fjyc", AddressUtil.toStr(objs.get("fjyc")));//附件异常其他
		itemInJson.put("fksss", AddressUtil.toStr(objs.get("fksss")));//妇科手术史 0：无1：有
		itemInJson.put("fksssqt", AddressUtil.toStr(objs.get("fksssqt")));//妇科手术史其他
		itemInJson.put("fujian", AddressUtil.toStr(objs.get("fujian")));//附件 0：未见异常；1：异常
		itemInJson.put("gjyc", AddressUtil.toStr(objs.get("gjyc")));//宫颈异常其他
		itemInJson.put("gongji", AddressUtil.toStr(objs.get("gongji")));//宫颈 0：未见异常；1：异常
		itemInJson.put("grs", AddressUtil.toStr(objs.get("grs")));//个人史
		itemInJson.put("grsqt", AddressUtil.toStr(objs.get("grsqt")));//个人史其他
		itemInJson.put("hiv", AddressUtil.toStr(objs.get("hiv")));//HIV 抗体检测 0:阴性，1 阳性，2：未检 查
		itemInJson.put("jhdhs", AddressUtil.toStr(objs.get("jhdhs")));//结合胆红素(μmol/L)
		itemInJson.put("jws", AddressUtil.toStr(objs.get("jws")));//既往病史
		itemInJson.put("jwsqt", AddressUtil.toStr(objs.get("jwsqt")));//既往病史其他
		itemInJson.put("jzs", AddressUtil.toStr(objs.get("jzs")));//家族史
		itemInJson.put("jzsqt", AddressUtil.toStr(objs.get("jzsqt")));//家族史其他
		itemInJson.put("liuchan", AddressUtil.toStr(objs.get("liuchan")));//孕产史(流产)
		itemInJson.put("mcyj", AddressUtil.toStr(objs.get("mcyj")));//末次月经
		itemInJson.put("mcyjbx", AddressUtil.toStr(objs.get("mcyjbx")));//末次月经不详
		itemInJson.put("mdxqsy", AddressUtil.toStr(objs.get("mdxqsy")));//梅毒血清学试验 0:阴性，1 阳性，2：未 检查
		itemInJson.put("name", AddressUtil.toStr(objs.get("name")));//姓名
		itemInJson.put("ncgqt", AddressUtil.toStr(objs.get("ncgqt")));//尿 常 规其他
		itemInJson.put("ndb", AddressUtil.toStr(objs.get("ndb")));//尿 常 规(尿蛋白)
		itemInJson.put("niaotang", AddressUtil.toStr(objs.get("niaotang")));//尿 常 规(尿糖)
		itemInJson.put("nqx", AddressUtil.toStr(objs.get("nqx")));//尿 常 规(尿潜血)
		itemInJson.put("ntt", AddressUtil.toStr(objs.get("ntt")));//尿 常 规(尿酮体)
		itemInJson.put("pgc", AddressUtil.toStr(objs.get("pgc")));//产次(剖宫产)
		itemInJson.put("shengao", AddressUtil.toStr(objs.get("shengao")));//身高
		itemInJson.put("sichan", AddressUtil.toStr(objs.get("sichan")));//孕产史(死产)
		itemInJson.put("sitai", AddressUtil.toStr(objs.get("sitai")));//孕产史(死胎)
		itemInJson.put("sssq", AddressUtil.toStr(objs.get("sssq")));//所属行政区划
		itemInJson.put("tbrq", AddressUtil.toStr(objs.get("tbrq")));//填表日期
		itemInJson.put("tbyz", AddressUtil.toStr(objs.get("tbyz")));//填表孕周(周)
		itemInJson.put("tbyztian", AddressUtil.toStr(objs.get("tbyztian")));//填表孕周(天)
		itemInJson.put("tizhong", AddressUtil.toStr(objs.get("tizhong")));//体重
		itemInJson.put("waiyin", AddressUtil.toStr(objs.get("waiyin")));//外阴 0：未见异常；1：异常
		itemInJson.put("wyyc", AddressUtil.toStr(objs.get("wyyc")));//外阴异常其他
		itemInJson.put("xcgqt", AddressUtil.toStr(objs.get("xcgqt")));//血 常 规其他
		itemInJson.put("xhdb", AddressUtil.toStr(objs.get("xhdb")));//血 常 规(血红蛋白 值)
		itemInJson.put("xxb", AddressUtil.toStr(objs.get("xxb")));//血小板计数值
		itemInJson.put("xinzang", AddressUtil.toStr(objs.get("xinzang")));//心脏 0：未见异常；
		itemInJson.put("xnsd", AddressUtil.toStr(objs.get("xnsd")));//血尿素氮
		itemInJson.put("xqgbzam", AddressUtil.toStr(objs.get("xqgbzam")));//血清谷丙转氨酶
		itemInJson.put("xqgczam", AddressUtil.toStr(objs.get("xqgczam")));//血清谷草转氨酶
		itemInJson.put("xqjg", AddressUtil.toStr(objs.get("xqjg")));//血清肌酐
		itemInJson.put("xsesw", AddressUtil.toStr(objs.get("xsesw")));//孕产史(新生儿死亡)
		itemInJson.put("xt", AddressUtil.toStr(objs.get("xt")));//血 糖
		itemInJson.put("xuexiabo", AddressUtil.toStr(objs.get("xuexiabo")));//血 型
		itemInJson.put("xuexirh", AddressUtil.toStr(objs.get("xuexirh")));//血型 Rh
		itemInJson.put("xxb", AddressUtil.toStr(objs.get("xxb")));//血小板
		itemInJson.put("xy", AddressUtil.toStr(objs.get("xy")));//血压
		itemInJson.put("xy1", AddressUtil.toStr(objs.get("xy1")));//血压
		itemInJson.put("xzyc", AddressUtil.toStr(objs.get("xzyc")));//心脏异常其他
		itemInJson.put("ycq", AddressUtil.toStr(objs.get("ycq")));//预 产 期
		itemInJson.put("ydfmw", AddressUtil.toStr(objs.get("ydfmw")));//阴道分泌物
		itemInJson.put("ydfmwqt", AddressUtil.toStr(objs.get("ydfmwqt")));//阴道分泌物其他
		itemInJson.put("ydqjd", AddressUtil.toStr(objs.get("ydqjd")));//阴道清洁度
		itemInJson.put("ydyc", AddressUtil.toStr(objs.get("ydyc")));//阴道异常其他
		itemInJson.put("yfnl", AddressUtil.toStr(objs.get("yfnl")));//孕妇年龄
		itemInJson.put("yindao", AddressUtil.toStr(objs.get("yindao")));//阴道 0：未见异常1：异常
		itemInJson.put("yunci", AddressUtil.toStr(objs.get("yunci")));//孕次
		itemInJson.put("yxgybmkt", AddressUtil.toStr(objs.get("yxgybmkt")));//乙型肝炎表面抗体
		itemInJson.put("yxgybmky", AddressUtil.toStr(objs.get("yxgybmky")));//乙型肝炎表面抗原
		itemInJson.put("yxgyeke", AddressUtil.toStr(objs.get("yxgyeke")));//乙型肝炎 e 抗体
		itemInJson.put("yxgyeky", AddressUtil.toStr(objs.get("yxgyeky")));//乙型肝炎 e 抗原
		itemInJson.put("yxgykxkt", AddressUtil.toStr(objs.get("yxgykxkt")));//乙型肝炎核心抗体
		itemInJson.put("zdhs", AddressUtil.toStr(objs.get("zdhs")));//总胆红素
		itemInJson.put("zfdh", AddressUtil.toStr(objs.get("zfdh")));//丈夫电话
		itemInJson.put("zfname", AddressUtil.toStr(objs.get("zfname")));//丈夫姓名
		itemInJson.put("zfnl", AddressUtil.toStr(objs.get("zfnl")));//丈夫年龄
		itemInJson.put("zgyc", AddressUtil.toStr(objs.get("zgyc")));//子宫异常其他
		itemInJson.put("zhuanzhen", AddressUtil.toStr(objs.get("zhuanzhen")));//转 诊: 0 无，1 有
		itemInJson.put("zigong", AddressUtil.toStr(objs.get("zigong")));//子宫 0：未见异常；1：异常
		itemInJson.put("ztpg", AddressUtil.toStr(objs.get("ztpg")));//总体评估 0：未见异常； 1：异常
		itemInJson.put("ztpgyc", AddressUtil.toStr(objs.get("ztpgyc")));//总体评估异常其他
		itemInJson.put("zzjg", AddressUtil.toStr(objs.get("zzjg")));//转诊机构及科室
		itemInJson.put("zzyy", AddressUtil.toStr(objs.get("zzyy")));//转诊原因
		itemInJson.put("czsj", AddressUtil.toStr(objs.get("czsj")));//录入日期
		itemInJson.put("czy", AddressUtil.toStr(objs.get("czy")));//对应新系统用户 id
		itemInJson.put("xcsfrq", AddressUtil.toStr(objs.get("xcsfrq")));//下次随访日期
		itemInJson.put("createdate", AddressUtil.toStr(objs.get("createdate")));//录入日期
		itemInJson.put("zzlxr", AddressUtil.toStr(objs.get("zzlxr")));//转诊联系人
		itemInJson.put("zzlxfs", AddressUtil.toStr(objs.get("zzlxfs")));//转诊联系人电话
		itemInJson.put("zzresult", AddressUtil.toStr(objs.get("zzresult")));//转诊结果：0：到位，1：不到位
		itemInJson.put("sfys", AddressUtil.toStr(objs.get("sfys")));//随访医生


		jsondata = JSON.toJSONString(data);
		logger.info("==========jsondata:" + jsondata);//jsondata
		//String state = "";
		Map<String,Object> resuletMap = new HashMap<String, Object>();
		try {
			resuletMap = HttpClientUtils.getJson(jsondata);
			/*Object stateO = (Object) resuletMap.get("state");
			state = String.valueOf(stateO);*/
			resuletMap.put("idcard",idcard);
			resuletMap.put("jsondata",jsondata);
			resuletMap.put("gragfvid",gragfvid);
			logger.info("============idcard:" + idcard );// idcard
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
		logger.info("开始上传妇女管理卡产前随访记录");
	}

	@Override
	public void logLook(int i) {
		logger.info("查询妇女管理卡产前随访记录信息" + i + "条");
	}

	@Override
	public void logAfter(JdbcTemplate jdbcTemplate,Map<String, Object> objs) {
		String newdate = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
		String idcard = (String) objs.get("idcard");
		JSONObject jsondata = (JSONObject) objs.get("jsondata");
		String gragfvid = (String) objs.get("gragfvid");
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
		bf.append("'").append(gragfvid).append("' and upload_type='妇女管理卡产前随访记录信息(t_gra_first_visit)'");
		String sqld = bf.toString();
		logger.info("妇女管理卡产前随访记录信息更新日志库["+sqld+"]");
		jdbcTemplate.execute(sqld);

		// 更新目标库
		StringBuffer buffer = new StringBuffer();
		buffer.append("INSERT INTO t_qf_data_upload(add_time,business_id,state,stateType,upload_reason,upload_json,delete_flag,upload_type) VALUES(");
		buffer.append("'").append(newdate).append("',");
		buffer.append("'").append(gragfvid).append("',");// gragfvid
		buffer.append("'").append(state).append("',");// state
		buffer.append("'").append(stateType).append("',");// stateType
		buffer.append("'").append(message).append("',");
		buffer.append("'").append(jsondata).append("',");
		buffer.append("'").append("0").append("',");// 默认0 未删除
		buffer.append("'").append("妇女管理卡产前随访记录信息(t_gra_first_visit)").append("'");
		buffer.append(")"); //

		String sql = buffer.toString();
		logger.info("妇女管理卡产前随访记录信息更新日志库["+sql+"]");
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
		//System.out.println(new Upload_GraManageCq_Visit().getSQL());
	}

}
