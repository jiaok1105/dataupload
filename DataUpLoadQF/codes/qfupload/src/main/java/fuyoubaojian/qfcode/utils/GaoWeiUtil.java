package fuyoubaojian.qfcode.utils;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

public class GaoWeiUtil {
//	private static final Logger logger = Logger.getLogger(DicSystemUtil.class);
	public static Object getText(Object code, JdbcTemplate template) {
		
		String query = "SELECT name FROM t_dic_highrisk WHERE code='" + code.toString()+"';";
		Map<String, Object> map = null;
		try {
			map = template.queryForMap(query);
		} catch (DataAccessException e) {
			return null;
		}
		return map.get("name");
		
	}

	public static String[] getGaoWeiInfo(Object yKID, JdbcTemplate template) {
		String[] result = new String[3];
		
		StringBuffer query = new StringBuffer("select DISTINCT PERSON_ID,HIGH_RISK,GRADE,HAS_HIGH_RISK from ( ");
		query.append("select person_id,high_risk,grade,has_high_risk from t_gra_first_visit where person_id='")
		.append(yKID)
		.append("' union all ")
		.append("select person_id,high_risk,grade,has_high_risk from t_gra_return_visit where person_id='")
		.append(yKID)
		.append("' union all ")
		.append("select person_id,high_risk,grade,has_high_risk from t_gra_labor where person_id='")
		.append(yKID)
		.append("' ) ");
		List<Map<String, Object>> list = null;
		try {
			list = template.queryForList(query.toString());
		} catch (DataAccessException e) {
			return result;
		}
		StringBuffer GWYSMC = new StringBuffer();
		StringBuffer GWYSBM = new StringBuffer(";");
		StringBuffer GWPF = new StringBuffer();
		String SFGW = "0";
		for (Map<String, Object> map : list) {
			String HIGH_RISKs = map.get("HIGH_RISK").toString();
			if(HIGH_RISKs!=null&&HIGH_RISKs.length()>0){
				String[] risks = HIGH_RISKs.split(",");
				for (String risk : risks) {
					if(GWYSBM.indexOf(";"+risk+";")==-1){
						GWYSBM.append(risk).append(";");
						GWYSMC.append(getText(risk,template)).append(";");
					}
				}
			}
			GWPF.append( map.get("GRADE").toString()).append(",");
		}
		String[] grades = GWPF.toString().split(",");
		int i = 0 ;
		for (String grade : grades) {
			int j = Integer.parseInt(grade);
			if(i<j){
				i=j;
			}
		}
		GWPF = new StringBuffer(i);
		if(i>0){
			SFGW="1";
		}
		result[0]=GWYSMC.toString();
		result[1]=GWPF.toString();
		result[2]=SFGW;		
		return result;
	}


}
