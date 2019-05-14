package fuyoubaojian.qfcode.etl;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import fuyoubaojian.qfcode.utils.AddressUtil;

/**
 * 《出生医学证明》签发数据D301
 */
@Component
public class Upload_Test extends BaseTask implements TaskInterface {
	private static final Logger logger = Logger.getLogger(Upload_Test.class);

	@Override
	public String getSQL(JdbcTemplate jdbcTemplate) {

		logger.info("开始迁移【廊坊出生证 机构信息数据】");
		// 查询结果
		StringBuffer buffer = new StringBuffer();

		System.out.print("+++++++++++++++++++++++++++++++");
		buffer.append(" select ");//
		buffer.append(" hh.id as id,	");// #系统编码 各市级系统内针对各机构的系统编码 S AN..50
		buffer.append(" hh.text as text,	");// #机构级别 各机构的机构级别代码 S N1 S101-19 必填
		buffer.append(" hh.code as code	");// #数据写入时间 最后修改时的公元纪年日期和时间的完整描述 DT DT12 YYYYMMDDHHMM 必填

		buffer.append("from  t_hos_hospital hh");

		/*buffer.append(" select ");//
		buffer.append(" hh.zzjg_code as XTBM,	");// #系统编码 各市级系统内针对各机构的系统编码 S AN..50
													// 必填，市内唯一，有组织机构代码的，系统编码必须采用组织机构代码,对于非地方的医疗机构,不存在组织机构代码,按照系统中编码上报
		buffer.append(" hh.code_lx as XTBMLB,	");// #系统编码类别 系统编码所属类别代码 S N1 S101-17 必填
		buffer.append(" hh.jg_name as JGMC,	");// #机构名称 各市级系统内针对系统编码对应的机构名称 S AN..200 必填
		buffer.append(" hh.dx_code as QXXZQHBM,	");// #区县行政区划编码 机构所在区县行政区划编码 N N6 S101-03 必填
		buffer.append(" hh.is_xzgl as XZGLJG,	");// #行政管理机构 是否为卫生计生行政管理机构代码 L T/F 0 否 1 是 #必填
		buffer.append(" hh.is_wtjg as SWTGLJG,	");// #受委托管理机构 是否为卫生计生行政管理部门委托负责《出生医学证明》事务性管理工作的机构代码 L T/F 0 否 1 是 #必填
		buffer.append(" hh.is_qfjg as QFJG,	");// #签发机构 是否为签发机构代码 L T/F 0 否 1 是 必填
		buffer.append(" hh.is_zcjg as ZCJG,	");// #助产机构 是否具有助产技术服务资质代码 L T/F 0 否 1 是 必填
		buffer.append(" hh.sz_level as XZJB,	");// #行政级别 各机构的行政级别代码 S N1 0 省（自治区、直辖市）\ 1 市（地级市、地区、州）\2 县（区、县级市） \3
													// 乡（镇、街道办事处）
		buffer.append(" hh.jg_level as JGJB,	");// #机构级别 各机构的机构级别代码 S N1 S101-19 必填
		buffer.append(" hh.add_time as SJXRSJ,	");// #数据写入时间 最后修改时的公元纪年日期和时间的完整描述 DT DT12 YYYYMMDDHHMM 必填
		buffer.append(" hh.jg_time as riqi	");// #机构日期 DT DT 2017/1/1
												// 如上传的机构数据是2017年的则该字段上传2017/1/1,如上传的机构数据是2018年的则该字段上传2018/1/1

		buffer.append("from  t_hos_hospital_temp181229 hh");*/

		logger.info(buffer.toString());
		return buffer.toString();
	}

	@Override
	public String getUpdateSQL(Map<String, Object> objs) {
		// 更新目标库
		StringBuffer buffer = new StringBuffer();
		String id = AddressUtil.toStr(objs.get("id"));
		String text = AddressUtil.toStr(objs.get("text"));
		String code = AddressUtil.toStr(objs.get("code"));
		logger.info("id====" + id);
		logger.info("text====" + text);
		logger.info("code====" + code);

		/*buffer.append("INSERT INTO d101 VALUES(");

		buffer.append("to_date('").append(AddressUtil.toStr(objs.get("riqi"))).append("','yyyy/MM/dd'),");// 如上传的机构数据是2017年的则该字段上传2017/1/1,如上传的机构数据是2018年的则该字段上传2018/1/1
		buffer.append("'").append(AddressUtil.toStr(objs.get("XTBM"))).append("',");// D101-01系统编码各市级系统内针对各机构的系统编码S必填，市内唯一，有组织机构代码的，系统编码必须采用组织机构代码,对于非地方的医疗机构,不存在组织机构代码,按照系统中编码上报
		buffer.append("'").append(AddressUtil.toStr(objs.get("XTBMLB"))).append("',");// D101-02系统编码类别系统编码所属类别代码S必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("JGMC"))).append("',");// D101-03机构名称各市级系统内针对系统编码对应的机构名称S必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("QXXZQHBM"))).append("',");// D101-04区县行政区划编码机构所在区县行政区划编码N必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("XZGLJG"))).append("',");// D101-05行政管理机构是否为卫生计生行政管理机构代码L必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("SWTGLJG"))).append("',");// D101-06受委托管理机构是否为卫生计生行政管理部门委托负责《出生医学证明》事务性管理工作的机构代码L必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("QFJG"))).append("',");// D101-07签发机构是否为签发机构代码L必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("ZCJG"))).append("',");// D101-08助产机构是否具有助产技术服务资质代码L必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("XZJB"))).append("',");// D101-09行政级别各机构的行政级别代码S
		buffer.append("'").append(AddressUtil.toStr(objs.get("JGJB"))).append("',");// D101-10机构级别各机构的机构级别代码S必填
		buffer.append("to_date('").append(AddressUtil.toStr(objs.get("SJXRSJ"))).append("','yyyy-mm-dd hh24:mi:ss')");// D101-11数据写入时间最后修改时的公元纪年日期和时间的完整描述DT必填

		buffer.append(")"); */
		return buffer.toString();
	}

	@Override
	public Map<String,Object> getStateSQL(Map<String, Object> objs) {
		return null;
	}


	@Override
	public void logBefore() {
		logger.info("开始迁移出生机构信息");
	}

	@Override
	public void logLook(int i) {
		logger.info("查询出生机构信息" + i + "条");
	}

	@Override
	public void logAfter(JdbcTemplate jdbcTemplate, Map<String, Object> objs) {
		logger.info("成功迁移出生机构信息[" + objs.get("JGMC") + "]");
	}

	@Override
	public String getDeleteSql() {

		StringBuffer buffer = new StringBuffer();

		return buffer.toString();
	}

}
