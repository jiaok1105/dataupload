package fuyoubaojian.qfcode.utils;

import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 孕产妇的信息工具
 */
public class GraPersonUtil {
	
	/**
	 * 根据ID获取孕产妇信息
	 * @param graId
	 * @param jdbcTemplate
	 * @return
	 */
	public static Map<String, Object> getGraPersonById(String graId, JdbcTemplate jdbcTemplate) {
		String query = "SELECT * from t_gra_person where id=" + graId;
		
		Map<String, Object> map = null;
		try {
			map = jdbcTemplate.queryForMap(query);
		} catch (DataAccessException e) {
			return null;
		}
		return map;
	}
	
	/**
	 * 根据ID获取人员基本信息
	 * @param arcId
	 * @param jdbcTemplate
	 * @return
	 */
	public static Map<String, Object> getArcPersonById(String arcId, JdbcTemplate jdbcTemplate) {
		String query = "SELECT * from t_arc_person where id=" + arcId;
		
		Map<String, Object> map = null;
		try {
			map = jdbcTemplate.queryForMap(query);
		} catch (DataAccessException e) {
			return null;
		}
		return map;
	}

	/**
	 * 根据personId获取分娩信息
	 * @param personId
	 * @param jdbcTemplate
	 * @return
	 */
	public static Map<String, Object> getGraLaborByPersonId(String personId, JdbcTemplate jdbcTemplate) {
		String query = "SELECT * from t_gra_labor where person_id=" + personId;
		
		Map<String, Object> map = null;
		try {
			map = jdbcTemplate.queryForMap(query);
		} catch (DataAccessException e) {
			return null;
		}
		return map;
	}
	
	/**
	 * 根据personId获取分娩信息
	 * @param personId
	 * @param jdbcTemplate
	 * @return
	 */
	public static Map<String, Object> getGraBuildCopyByPersonId(String personId, JdbcTemplate jdbcTemplate) {
		String query = "SELECT bc.*,(bc.zrlc_num+bc.rglc_num) as zlc_num from t_gra_build_copy bc where bc.person_id=" + personId;
		
		Map<String, Object> map = null;
		try {
			map = jdbcTemplate.queryForMap(query);
		} catch (DataAccessException e) {
			return null;
		}
		return map;
	}
	
	/**
	 * 根据personId获取初诊信息
	 * @param personId
	 * @param jdbcTemplate
	 * @return
	 */
	public static Map<String, Object> getGraFirstVisitByPersonId(String personId, JdbcTemplate jdbcTemplate) {
		String query = "SELECT * from t_gra_first_visit where person_id=" + personId;
		
		Map<String, Object> map = null;
		try {
			map = jdbcTemplate.queryForMap(query);
		} catch (DataAccessException e) {
			return null;
		}
		return map;
	}
}
