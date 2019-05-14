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
 * 《出生医学证明》证件流转信息D501
 */
@Component
public class Upload_cszmlz extends BaseTask implements TaskInterface {
	private static final Logger logger = Logger.getLogger(Upload_cszmlz.class);
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
//		
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
		logger.info("开始迁移【" + date1 + "到" + date2 + "】的出生证明-流转");
		// 查询结果
		StringBuffer buffer = new StringBuffer();
		buffer.append("select ").append(" CONCAT('131000',cr.id) as SJXT_ID,  ")// 市级人口出生信息系统中对整间流转记录记录的唯一标识SAN..50必填
				.append("  srchh.zzjg_code AS CK_JGDM,  ")// 证件出库机构的组织机构代码SAN..50必填, 对应着机构数据中的系统编码字段
				.append("  srchh.jg_name as CK_JGMC,  ")// 证件出库机构的组织机构名称SAN..100必填
				.append("  case cr.ref_status when '1' then '1' when '3' then '2' when '6' then '3' when '8' then '6' when '9' then '4' when '2' then '5'  end  AS CRK_LX,  ")// 《出生医学证明》出入库类型SN1S101-07必填
				.append("  DATE_FORMAT(cr.add_time,'%Y%m%d') as CRK_RQ,  ")// 证件出入库日期DD8必填，YYYYMMDD
				.append("  cr.range_begin as CRK_QSBH,  ")// QSBH,--证件出入库的起始编号SAN10必填
				.append("  cr.range_end as CRK_ZZBH,  ")// 证件出入库的终止编号SAN10必填
				.append("  cr.range_qty as CRK_SL,  ")// 证件出入库的数量NN..7必填
				.append("  tohh.zzjg_code as RK_JGDM,  ")// 证件入库机构的组织机构代码SAN..50必填, 对应着机构数据中的系统编码字段
				.append("  tohh.jg_name as RK_JGMC,  ")// 证件入库机构的组织机构名称SAN..100必填
				.append("  cr.memo as CRK_BZ,  ")// 出入库特殊原因说明SAN..200
				.append("  hh.jg_name as TB_MC,  ")// 填报报表单位名称SAN..100必填, 对应着机构数据中的系统名称字段
				.append("  hh.zzjg_code as TB_ZZJGDM,  ")// 填报报表单位组织机构代码SAN..50必填, 对应着机构数据中的系统编码字段
				.append("  if(cr.update_time is null,DATE_FORMAT(cr.add_time,'%Y-%m-%d %H:%i:%S'),DATE_FORMAT(cr.update_time,'%Y-%m-%d %H:%i:%S')) as SJXRSJ,cr.id ")// 最后修改时的公元纪年日期和时间的完整描述DTDT12必填
				
				.append(" from t_cert_ref cr  ")
				.append(" join t_hos_hospital_temp181229 srchh on srchh.hos_id=cr.src_hos_id  ")
				.append(" join t_hos_hospital_temp181229 tohh on tohh.hos_id=cr.dest_hos_id  ")
				
				.append(" join t_hos_doctor hd on cr.add_user_id=hd.user_id")
				.append(" join t_hos_hospital_temp181229 hh on hh.hos_id =hd.hospital_id")
				
				.append(" WHERE cr.update_time").append(">= ").append(date1).append(" and cr.update_time <= ")
				.append(date2);
		logger.info(buffer.toString());
		return buffer.toString();
	}

	@Override
	public String getUpdateSQL(Map<String, Object> objs) {
		// 更新目标库
		StringBuffer buffer = new StringBuffer();
		buffer.append("INSERT INTO d501 VALUES(");

		buffer.append("'").append(AddressUtil.toStr(objs.get("SJXT_ID"))).append("',");// D501-001系统ID市级人口出生信息系统中对整间流转记录记录的唯一标识AN..50必填,系统主键（上传规则为地市级行政区划代码+市级系统主键，如石家庄市则该字段上传规则为130100+市级系统id）
		buffer.append("'").append(AddressUtil.toStr(objs.get("CK_JGDM"))).append("',");// D501-01出库机构的组织机构代码证件出库机构的组织机构代码AN..50必填,
																						// 对应着机构数据中的系统编码字段
		buffer.append("'").append(AddressUtil.toStr(objs.get("CK_JGMC"))).append("',");// D501-02出库机构名称证件出库机构的组织机构名称AN..100必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("CRK_LX"))).append("',");// D501-03出入库类型《出生医学证明》出入库类型N1必填
		buffer.append("to_date('").append(AddressUtil.toStr(objs.get("CRK_RQ"))).append("','yyyyMMdd'),");// D501-04出入库日期证件出入库日期D8必填，YYYYMMDD
		buffer.append("'").append(AddressUtil.toStr(objs.get("CRK_QSBH"))).append("',");// D501-05起始编号证件出入库的起始编号AN10必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("CRK_ZZBH"))).append("',");// D501-06终止编号证件出入库的终止编号AN10必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("CRK_SL"))).append("',");// D501-07数量证件出入库的数量N..7必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("RK_JGDM"))).append("',");// D501-08入库机构代码证件入库机构的组织机构代码AN..50必填,
																						// 对应着机构数据中的系统编码字段
		buffer.append("'").append(AddressUtil.toStr(objs.get("RK_JGMC"))).append("',");// D501-09入库机构名称证件入库机构的组织机构名称AN..100必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("CRK_BZ"))).append("',");// D501-10备注出入库特殊原因说明AN..200
		buffer.append("'").append(AddressUtil.toStr(objs.get("TB_MC"))).append("',");// D501-11填报单位名称填报报表单位名称AN..100必填,
																						// 对应着机构数据中的系统名称字段
		buffer.append("'").append(AddressUtil.toStr(objs.get("TB_ZZJGDM"))).append("',");// D501-12填报单位组织机构代码填报报表单位组织机构代码AN..50必填,
																							// 对应着机构数据中的系统编码字段
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
		logger.info("开始迁移出生证明-流转");
	}

	@Override
	public void logLook(int i) {
		logger.info("查询出生证明-流转" + i + "条");
	}

	@Override
	public void logAfter(JdbcTemplate jdbcTemplate, Map<String, Object> objs) {
		// 更新下上传状态 alter table t_cert_ref add cert_upload smallint default 0;
		String update = "update t_cert_ref set cert_upload=1 where id=" + objs.get("id");
		jdbcTemplate.execute(update);

		logger.info("出生证明-流转上传[" + update + "]");
	}

	@Override
	public String getDeleteSql() {
		String date1 = this.getStartDate();
		String date2 = this.getEndDate();

		StringBuffer buffer = new StringBuffer();
		if ("TRUE".equals(getDel_flag())) {

			logger.info("开始删除已有的出生证明-流转");
			buffer.append("DELETE FROM CA_NEONATE_VISIT");
		}
		return buffer.toString();
	}

	public static void main(String[] args) {
		//System.out.println(new Upload_cszmlz().getSQL());
	}
}
