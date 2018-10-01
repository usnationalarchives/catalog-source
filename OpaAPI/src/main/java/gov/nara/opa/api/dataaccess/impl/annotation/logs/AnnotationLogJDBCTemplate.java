package gov.nara.opa.api.dataaccess.impl.annotation.logs;

import gov.nara.opa.api.dataaccess.annotation.logs.AnnotationLogDao;
import gov.nara.opa.architecture.dataaccess.AbstractOpaDbJDBCTemplate;
import gov.nara.opa.common.dataaccess.utils.StoredProcedureDataAccessUtils;
import gov.nara.opa.common.valueobject.annotation.logs.AnnotationLogValueObject;
import gov.nara.opa.common.valueobject.annotation.logs.AnnotationLogValueObjectConstants;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class AnnotationLogJDBCTemplate extends AbstractOpaDbJDBCTemplate
		implements AnnotationLogDao, AnnotationLogValueObjectConstants {

	@Override
	public boolean insert(AnnotationLogValueObject annotationLog)
			throws UnsupportedEncodingException {
		if (annotationLog.getNaId() != null) {
			annotationLog.getNaId().getBytes("UTF-8");
		}

		if (annotationLog.getObjectId() != null) {
			annotationLog.getObjectId().getBytes("UTF-8");
		}

		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("accountId", annotationLog.getAccountId());
		inParamMap.put("logAction", annotationLog.getAction());

		inParamMap.put("annotationId", annotationLog.getAnnotationId());
		inParamMap.put("annotationMd5", annotationLog.getAnnotationMD5());
		inParamMap.put("annotationType", annotationLog.getAnnotationType());
		inParamMap.put("firstAccountId", annotationLog.getFirstAccountId());
		inParamMap.put("firstAnnotationId",
				annotationLog.getFirstAnnotationId());
		inParamMap.put("languageIso", annotationLog.getLanguageISO());
		inParamMap.put("naId", annotationLog.getNaId());
		inParamMap.put("logNotes", annotationLog.getNotes());
		inParamMap.put("objectId", annotationLog.getObjectId());
		inParamMap.put("opaId", annotationLog.getOpaId());
		inParamMap.put("pageNum", annotationLog.getPageNum());
		inParamMap.put("parentId", annotationLog.getParentId());

		inParamMap.put("logSequence", annotationLog.getSequence());
		inParamMap.put("sessionId", annotationLog.getSessionId());
		inParamMap.put("logStatus", annotationLog.getStatus());
		inParamMap.put("versionNum", annotationLog.getVersionNum());

		if (annotationLog.getAffectsAccountId() != null
				&& annotationLog.getAffectsAccountId() != 0) {
			inParamMap.put("affectsAccountId",
					annotationLog.getAffectsAccountId());
		} else {
			inParamMap.put("affectsAccountId", null);
		}

		if (annotationLog.getReasonId() != null
				&& annotationLog.getReasonId() > 0) {
			inParamMap.put("reasonId", annotationLog.getReasonId());
		} else {
			inParamMap.put("reasonId", null);
		}

		int annotationLogId = StoredProcedureDataAccessUtils
				.executeWithIntResult(getJdbcTemplate(),
						"spInsertAnnotationLogs", inParamMap, "logId");
		annotationLog.setLogId(new Integer(annotationLogId));
		boolean result = true;

		if (annotationLog.getFirstAnnotationId() != null
				&& annotationLog.getFirstAnnotationId() == 0) {
			List<AnnotationLogValueObject> activeLogs = select(
					annotationLog.getAnnotationType(), annotationLog.getNaId(),
					annotationLog.getObjectId(), annotationLog.getStatus());
			if (activeLogs.size() > 0) {
				annotationLog.setFirstAnnotationId(activeLogs.get(0)
						.getAnnotationId());
				result = update(annotationLog);
			} else {
				throw new NullPointerException("AnnotationLog not found");
			}
		}

		return result;
	}

	@Override
	public boolean update(AnnotationLogValueObject annotationLog)
			throws DataAccessException, UnsupportedEncodingException,
			BadSqlGrammarException {

		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("logId", annotationLog.getLogId());
		inParamMap.put("accountId", annotationLog.getAccountId());
		inParamMap.put("logAction", annotationLog.getAction());
		inParamMap.put("annotationId", annotationLog.getAnnotationId());
		inParamMap.put("annotationMd5", annotationLog.getAnnotationMD5());
		inParamMap.put("annotationType", annotationLog.getAnnotationType());
		inParamMap.put("firstAccountId", annotationLog.getFirstAccountId());
		inParamMap.put("firstAnnotationId",
				annotationLog.getFirstAnnotationId());
		inParamMap.put("naId", annotationLog.getNaId());
		inParamMap.put("logNotes", annotationLog.getNotes());
		inParamMap.put("objectId", annotationLog.getObjectId());
		inParamMap.put("opaId", annotationLog.getOpaId());
		inParamMap.put("pageNum", annotationLog.getPageNum());
		inParamMap.put("parentId", annotationLog.getParentId());
		inParamMap.put("logSequence", annotationLog.getSequence());
		inParamMap.put("sessionId", annotationLog.getSessionId());
		inParamMap.put("logStatus", annotationLog.getStatus());
		inParamMap.put("versionNum", annotationLog.getVersionNum());
		inParamMap.put("languageIso", annotationLog.getLanguageISO());
		if (annotationLog.getAffectsAccountId() != null
				&& annotationLog.getAffectsAccountId() != 0) {
			inParamMap.put("affectsAccountId",
					annotationLog.getAffectsAccountId());
		} else {
			inParamMap.put("affectsAccountId", null);
		}

		if (annotationLog.getReasonId() != null
				&& annotationLog.getReasonId() > 0) {
			inParamMap.put("reasonId", annotationLog.getReasonId());
		} else {
			inParamMap.put("reasonId", null);
		}

		boolean result = StoredProcedureDataAccessUtils.execute(
				getJdbcTemplate(), "spUpdateAnnotationLog", inParamMap);

		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AnnotationLogValueObject> select(String annotationType,
			String naId, String objectId, boolean status)
			throws DataAccessException, UnsupportedEncodingException,
			BadSqlGrammarException {

		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("annotationType", annotationType);
		inParamMap.put("naId", naId);
		inParamMap.put("objectId", objectId);
		inParamMap.put("logStatus", status);

		return (List<AnnotationLogValueObject>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(), "spGetAnnotationLogs",
						new AnnotationLogValueObjectRowMapper(), inParamMap);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<AnnotationLogValueObject> select(String annotationType,
			String naId, String objectId, Integer parentId, boolean status)
			throws DataAccessException, UnsupportedEncodingException,
			BadSqlGrammarException {

		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("annotationType", annotationType);
		inParamMap.put("naId", naId);
		inParamMap.put("objectId", objectId);
		inParamMap.put("parentId", parentId);
		inParamMap.put("logStatus", status);

		return (List<AnnotationLogValueObject>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(), "spGetAnnotationLogsWithParentId",
						new AnnotationLogValueObjectRowMapper(), inParamMap);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AnnotationLogValueObject> select(String annotationType,
			String naId, String objectId) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException {

		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("annotationType", annotationType);
		inParamMap.put("naId", naId);
		inParamMap.put("objectId", objectId);

		List<AnnotationLogValueObject> logs = (List<AnnotationLogValueObject>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(),
						"spGetAnnotationLogByAnnotationTypeNaIdAndObjectType",
						new AnnotationLogValueObjectRowMapper(), inParamMap);

		return logs;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AnnotationLogValueObject> select(String annotationType,
			int annotationId) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("annotationType", annotationType);
		inParamMap.put("annotationId", annotationId);

		List<AnnotationLogValueObject> logs = (List<AnnotationLogValueObject>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(),
						"spGetAnnotationLogByAnnotationTypeAndId",
						new AnnotationLogValueObjectRowMapper(), inParamMap);

		return logs;
	}

	@SuppressWarnings("unchecked")
	@Override
	public AnnotationLogValueObject select(int logId)
			throws DataAccessException, UnsupportedEncodingException,
			BadSqlGrammarException {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("logId", logId);

		List<AnnotationLogValueObject> logs = (List<AnnotationLogValueObject>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(), "spGetAnnotationLogByLogId",
						new AnnotationLogValueObjectRowMapper(), inParamMap);

		return (logs != null && logs.size() > 0 ? logs.get(0) : null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public AnnotationLogValueObject selectByVersion(String annotationType,
			String naId, String objectId, int version)
			throws DataAccessException, UnsupportedEncodingException,
			BadSqlGrammarException {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("annotationType", annotationType);
		inParamMap.put("naId", naId);
		inParamMap.put("objectId", objectId);
		inParamMap.put("versionNum", version);

		List<AnnotationLogValueObject> results = (List<AnnotationLogValueObject>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(), "spGetAnnotationLogByVersion",
						new AnnotationLogValueObjectRowMapper(), inParamMap);

		return (results != null && results.size() > 0 ? results.get(0) : null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public int getHighestVersion(String annotationType, String naId,
			String objectId) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException {

		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("annotationType", annotationType);
		inParamMap.put("naId", naId);
		inParamMap.put("objectId", objectId);

		List<AnnotationLogValueObject> logs = (List<AnnotationLogValueObject>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(), "spGetAnnotationLogsHigherVersion",
						new AnnotationLogValueObjectRowMapper(), inParamMap);

		AnnotationLogValueObject log = (logs != null && logs.size() > 0 ? logs
				.get(0) : null);

		return (log != null ? log.getVersionNum() : 0);
	}

	@Override
	public int disableByAnnotationId(Integer annotationId, String annotationType) {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("logStatus", false);
		inParamMap.put("annotationId", annotationId);
		inParamMap.put("annotationType", annotationType);

		return StoredProcedureDataAccessUtils.executeWithNumberOfChanges(
				getJdbcTemplate(),
				"spUpdateAnnotationLogStatusByAnnotationIdAndType", inParamMap);
	}

	@Override
	public int disableByLogId(int logId) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("logStatus", false);
		inParamMap.put("logId", logId);

		return StoredProcedureDataAccessUtils.executeWithNumberOfChanges(
				getJdbcTemplate(), "spUpdateAnnotationLogStatusByLogId",
				inParamMap);
	}

	@Override
	public List<AnnotationLogValueObject> select(String annotationType,
			String naId, String objectId, String language, boolean status)
			throws DataAccessException, UnsupportedEncodingException,
			BadSqlGrammarException {
		// TODO Auto-generated method stub
		return null;
	}
}