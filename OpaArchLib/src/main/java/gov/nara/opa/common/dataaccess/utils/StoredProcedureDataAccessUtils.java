package gov.nara.opa.common.dataaccess.utils;

import java.util.Map;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

public class StoredProcedureDataAccessUtils {

	static Logger log = Logger.getLogger(StoredProcedureDataAccessUtils.class);

	/**
	 * @param jdbcTemplate
	 *            The JDBC template
	 * @param spName
	 *            The store procedure name
	 * @param mapper
	 *            RowMapper used to map the results
	 * @param inParamMap
	 *            The parameters of the store procedure
	 * @return List of results
	 */
	@SuppressWarnings("rawtypes")
	public static Object execute(JdbcTemplate jdbcTemplate, String spName,
			RowMapper mapper, Map<String, Object> inParamMap) {
		Map<String, Object> simpleJdbcCallResult = null;
		SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate)
		.withProcedureName(spName)
		.returningResultSet("results", mapper);
		if (inParamMap != null) {
			SqlParameterSource in = new MapSqlParameterSource(inParamMap);
			simpleJdbcCallResult = simpleJdbcCall.execute(in);
		} else {
			simpleJdbcCallResult = simpleJdbcCall.execute();
		}
		return simpleJdbcCallResult.get("results");
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> executeGeneric(JdbcTemplate jdbcTemplate, String spName,
			RowMapper<T> mapper, Map<String, Object> inParamMap) {
		Map<String, Object> simpleJdbcCallResult = null;
		SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate)
		.withProcedureName(spName)
		.returningResultSet("results", mapper);
		if (inParamMap != null) {
			SqlParameterSource in = new MapSqlParameterSource(inParamMap);
			simpleJdbcCallResult = simpleJdbcCall.execute(in);
		} else {
			simpleJdbcCallResult = simpleJdbcCall.execute();
		}
		return (List<T>)simpleJdbcCallResult.get("results");
	}

	/**
	 * @param jdbcTemplate
	 *            The JDBC template
	 * @param spName
	 *            The store procedure name
	 * @param inParamMap
	 *            The parameters of the store procedure
	 * @return true if the store procedure updates any row, false otherwise
	 */
	public static boolean execute(JdbcTemplate jdbcTemplate, String spName,
			Map<String, Object> inParamMap) {
		boolean result = false;
		int updateCount = executeWithNumberOfChanges(jdbcTemplate, spName,
				inParamMap);
		if (updateCount > 0) {
			result = true;
		}
		return result;
	}

	/**
	 * @param jdbcTemplate
	 *            The JDBC template
	 * @param spName
	 *            The store procedure name
	 * @param inParamMap
	 *            The parameters of the store procedure
	 * @return Number of records updated
	 */
	public static int executeWithNumberOfChanges(JdbcTemplate jdbcTemplate,
			String spName, Map<String, Object> inParamMap) {
		SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate)
		.withProcedureName(spName);
		SqlParameterSource in = new MapSqlParameterSource(inParamMap);
		Map<String, Object> simpleJdbcCallResult = simpleJdbcCall.execute(in);
		int updateCount = (int) simpleJdbcCallResult.get("#update-count-1");
		return updateCount;
	}

	/**
	 * @param jdbcTemplate
	 *            The JDBC template
	 * @param spName
	 *            The store procedure name
	 * @param inParamMap
	 *            The parameters of the store procedure
	 * @param outParam
	 *            Name of the out param
	 * @return Integer value of the out param
	 */
	public static int executeWithIntResult(JdbcTemplate jdbcTemplate,
			String spName, Map<String, Object> inParamMap, String outParam) {
		SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate)
		.withProcedureName(spName);
		SqlParameterSource in = new MapSqlParameterSource(inParamMap);
		Map<String, Object> simpleJdbcCallResult = simpleJdbcCall.execute(in);
		if (simpleJdbcCallResult.containsKey(outParam)) {
			return Integer.parseInt(String.valueOf(simpleJdbcCallResult
					.get(outParam)));
		} else {
			return Integer.parseInt(String.valueOf(simpleJdbcCallResult
					.get(outParam.toLowerCase())));
		}
	}

	/**
	 * @param jdbcTemplate
	 *            The JDBC template
	 * @param spName
	 *            The store procedure name
	 * @param inParamMap
	 *            The parameters of the store procedure
	 * @param outParam
	 *            Name of the out param
	 * @return String value of the out param
	 */
	public static String executeWithStringResult(JdbcTemplate jdbcTemplate,
			String spName, Map<String, Object> inParamMap, String outParam) {
		SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate)
		.withProcedureName(spName);
		SqlParameterSource in = new MapSqlParameterSource(inParamMap);
		Map<String, Object> simpleJdbcCallResult = simpleJdbcCall.execute(in);
		if (simpleJdbcCallResult.containsKey(outParam)) {
			return 
					simpleJdbcCallResult.get(outParam) != null ? 
							String.valueOf(simpleJdbcCallResult.get(outParam)) : 
								null;
		} else {
			return 
					simpleJdbcCallResult.get(outParam) != null ? 
							String.valueOf(simpleJdbcCallResult.get(outParam.toLowerCase())) : 
								null;
		}
	}

	/**
	 * @param jdbcTemplate
	 *            The JDBC template
	 * @param spName
	 *            The store procedure name
	 * @param inParamMap
	 *            The parameters of the store procedure
	 * @param outParam
	 *            Name of the out param
	 * @return String value of the out param
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> executeWithListResults(JdbcTemplate jdbcTemplate,
			String spName, Map<String, Object> inParamMap) {
		SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate)
		.withProcedureName(spName);
		SqlParameterSource in = new MapSqlParameterSource(inParamMap);
		Map<String, Object> simpleJdbcCallResult = simpleJdbcCall.execute(in);
		return (List<Map<String, Object>>) simpleJdbcCallResult.get("#result-set-1");
	}

}
