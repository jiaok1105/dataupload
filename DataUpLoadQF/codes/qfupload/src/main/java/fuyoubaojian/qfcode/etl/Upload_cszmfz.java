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
 * 《出生医学证明》废证登记数据D401
 */
@Component
public class Upload_cszmfz extends BaseTask implements TaskInterface {
	private static final Logger logger = Logger.getLogger(Upload_cszmfz.class);
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
		logger.info("开始迁移【" + date1 + "到" + date2 + "】的出生证 - 废证");
		// 查询结果
		StringBuffer buffer = new StringBuffer();
		buffer.append("  select distinct  ")//
				.append("   CONCAT('131000',cd.id) as  SJXT_ID,   ")// 市级人口出生信息系统中对废证记录的唯一标识SAN..50必填,系统主键（上传规则为地市级行政区划代码+市级系统主键，如石家庄市则该字段上传规则为130100+市级系统id）
				.append("   DATE_FORMAT(cd.add_time,'%Y%m%d') as  FZ_RQ, ")// 废证的公元纪年日期DD8必填，YYYYMMDD
				.append("   cd.cert_no as  FZ_QSBH,   ")// 废证起始的编号SAN..10必填
				.append("   cd.cert_no as  FZ_ZZBH,   ")// 废证终止的编号SAN..10必填
				.append("  '1' as FZ_SL,   ")// 废证数量SAN..10必填
				.append("  case when cd.distr_type='17' then '1' when cd.distr_type='15' then '3' when cd.distr_type='16' then '2' else '4' end as  FZ_YY,   ")// 《出生医学证明》作废的原因SN1S101-08必填
				.append("   '' as  FZ_QTYY,   ")// 《出生医学证明》作废的其他原因SAN..100废证原因为其他时必填
				.append("   hd.`name` as  FZ_JBR,   ")// 经办人在公安管理部门登记注册的姓名SA..50必填
				
				.append("   hh.zzjg_code as  FZ_DJJGDM,   ")// 废证登记机构的组织机构代码SAN..50必填, 对应着机构数据中的系统编码字段
				.append("   hh.jg_name as  FZ_DJJGMC,   ")// 废证登记机构的组织机构名称SAN..100必填, 对应着机构数据中的系统名称字段
				.append("   if(cd.update_time is NULL,DATE_FORMAT(cd.add_time,'%Y-%m-%d %H:%i:%S'),DATE_FORMAT(cd.update_time,'%Y-%m-%d %H:%i:%S')) as  SJXRSJ,cd.id ")// 最后修改时的公元纪年日期和时间的完整描述DTDT12必填
				.append("  from t_cert_distr cd  ")//
				.append("  join t_hos_hospital_temp181229 hh on hh.hos_id=cd.distr_hospital_id  ")//
				.append("  JOIN t_hos_doctor hd on hd.user_id=cd.add_user_id  ")//
				.append("  where cd.distr_type in('7','8','15','16','17') and cd.delete_flag=0  ")//

				.append(" AND cd.add_time").append(">= ").append(date1).append(" and cd.add_time <= ").append(date2);
		logger.info(buffer.toString());
		return buffer.toString();
	}

	@Override
	public String getUpdateSQL(Map<String, Object> objs) {
		// 更新目标库
		StringBuffer buffer = new StringBuffer();
		buffer.append("INSERT INTO d401 VALUES(");
		buffer.append("'").append(AddressUtil.toStr(objs.get("SJXT_ID"))).append("',");// D401-001系统ID市级人口出生信息系统中对废证记录的唯一标识AN..50必填,系统主键（上传规则为地市级行政区划代码+市级系统主键，如石家庄市则该字段上传规则为130100+市级系统id）
		buffer.append("to_date('").append(AddressUtil.toStr(objs.get("FZ_RQ"))).append("','yyyyMMdd'),");// D401-01废证日期废证的公元纪年日期D8必填，YYYYMMDD
		buffer.append("'").append(AddressUtil.toStr(objs.get("FZ_QSBH"))).append("',");// D401-02废证起始编号废证起始的编号AN..10必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("FZ_ZZBH"))).append("',");// D401-03废证终止编号废证终止的编号AN..10必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("FZ_SL"))).append("',");// D401-04废证数量废证数量AN..10必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("FZ_YY"))).append("',");// D401-05废证原因《出生医学证明》作废的原因N1必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("FZ_QTYY"))).append("',");// D401-06废证其他原因《出生医学证明》作废的其他原因AN..100废证原因为其他时必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("FZ_JBR"))).append("',");// D401-07经办人经办人在公安管理部门登记注册的姓名A..50必填
		buffer.append("'").append(AddressUtil.toStr(objs.get("FZ_DJJGDM"))).append("',");// D401-08废证登记机构的组织机构代码废证登记机构的组织机构代码AN..50必填,
																							// 对应着机构数据中的系统编码字段
		buffer.append("'").append(AddressUtil.toStr(objs.get("FZ_DJJGMC"))).append("',");// D401-09废证登记机构名称废证登记机构的组织机构名称AN..100必填,
															// 对应着机构数据中的系统名称字段
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
		logger.info("开始迁移出生证 - 废证");
	}

	@Override
	public void logLook(int i) {
		logger.info("查询出生证 - 废证" + i + "条");
	}

	@Override
	public void logAfter(JdbcTemplate jdbcTemplate,Map<String, Object> objs) {
		//更新下上传状态 alter table t_cert_distr add fz_upload smallint default 0;
		String update="update t_cert_distr set fz_upload=1 where id="+objs.get("id");
		jdbcTemplate.execute(update);
		
		logger.info("出生证 - 废证上传["+update+"]");
	}

	@Override
	public String getDeleteSql() {
		String date1 = this.getStartDate();
		String date2 = this.getEndDate();

		StringBuffer buffer = new StringBuffer();
		if ("TRUE".equals(getDel_flag())) {

			logger.info("开始删除已有的出生证 - 废证信息");
			buffer.append("DELETE FROM CA_NEONATE_VISIT");
		}
		return buffer.toString();
	}

	public static void main(String[] args) {
		//System.out.println(new Upload_cszmfz().getSQL());
	}
}
