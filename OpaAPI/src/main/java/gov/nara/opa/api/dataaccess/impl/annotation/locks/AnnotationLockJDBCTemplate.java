package gov.nara.opa.api.dataaccess.impl.annotation.locks;

import gov.nara.opa.api.annotation.locks.AnnotationLock;
import gov.nara.opa.api.dataaccess.annotation.locks.AnnotationLockDao;
import gov.nara.opa.architecture.dataaccess.AbstractOpaDbJDBCTemplate;
import gov.nara.opa.common.dataaccess.utils.GenericRowMapper;
import gov.nara.opa.common.dataaccess.utils.StoredProcedureDataAccessUtils;

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
public class AnnotationLockJDBCTemplate extends AbstractOpaDbJDBCTemplate
		implements AnnotationLockDao {

	@Override
	public boolean insert(AnnotationLock annotationLock)
			throws DataAccessException, UnsupportedEncodingException,
			BadSqlGrammarException {
		boolean result = false;

		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("accountId", annotationLock.getAccountId());
		annotationLock.getNaId().getBytes("UTF-8");
		inParamMap.put("naId", annotationLock.getNaId());
		annotationLock.getObjectId().getBytes("UTF-8");
		inParamMap.put("objectId", annotationLock.getObjectId());
		if (annotationLock.getOpaId() != null) {
			annotationLock.getOpaId().getBytes("UTF-8");
			inParamMap.put("opaId", annotationLock.getOpaId());
		} else {
			inParamMap.put("opaId", null);
		}
		if (annotationLock.getLanguageISO() != null) {
			annotationLock.getLanguageISO().getBytes("UTF-8");
			inParamMap.put("languageIso", annotationLock.getLanguageISO());
		} else {
			inParamMap.put("languageIso", null);
		}

		result = StoredProcedureDataAccessUtils.execute(getJdbcTemplate(),
				"spInsertAnnotationLock", inParamMap);
		return result;
	}

	@Override
	public boolean validateLock(int accountId, String naId, String objectId,
			String languageISO, int minuteWindow) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException {

		List<AnnotationLock> locks = getLocks(naId, objectId, languageISO,
				minuteWindow);
		for (AnnotationLock lock : locks) {
			if (lock.getAccountId() == accountId) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean delete(String naId, String objectId, String languageISO,
			int accountId) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException {
		boolean result = false;

		naId.getBytes("UTF-8");
		objectId.getBytes("UTF-8");
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("naId", naId);
		inParamMap.put("objectId", objectId);
		inParamMap.put("accountId", accountId);

		if (languageISO != null) {
			languageISO.getBytes("UTF-8");
			inParamMap.put("languageIso", languageISO);
			result = StoredProcedureDataAccessUtils.execute(getJdbcTemplate(),
					"spDeleteAnnotationLockWithLanguageISO", inParamMap);
		} else {
			result = StoredProcedureDataAccessUtils.execute(getJdbcTemplate(),
					"spDeleteAnnotationLock", inParamMap);
		}

		// TODO: implement for non-transcriptions
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AnnotationLock> getLocks(String naId, String objectId,
			String languageISO, int minuteWindow) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException {

		naId.getBytes("UTF-8");
		objectId.getBytes("UTF-8");
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("naId", naId);
		inParamMap.put("objectId", objectId);
		inParamMap.put("minuteWindow", minuteWindow);

		if (languageISO != null) {
			languageISO.getBytes("UTF-8");
			inParamMap.put("languageIso", languageISO);
		} else {
			inParamMap.put("languageIso", null);
		}

		List<AnnotationLock> locks = (List<AnnotationLock>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(), "spGetAnnotationLocks",
						new GenericRowMapper<AnnotationLock>(new AnnotationLockExtractor()), inParamMap);
		return locks;
	}

	@Override
	public int clearExpiredLocks(int minuteWindow) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("minuteWindow", minuteWindow);

		int result = StoredProcedureDataAccessUtils
				.executeWithNumberOfChanges(getJdbcTemplate(),
						"spDeleteExpiredAnnotationLocks", inParamMap);
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AnnotationLock> getLocksForUser(int accountId, int minuteWindow)
			throws DataAccessException, UnsupportedEncodingException,
			BadSqlGrammarException {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("accountId", accountId);
		inParamMap.put("minuteWindow", minuteWindow);

		List<AnnotationLock> locks = (List<AnnotationLock>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(),
						"spGetAnnotationLocksByAccountUser",
						new AnnotationLockRowMapper(), inParamMap);

		return locks;
	}

	@Override
	public boolean update(AnnotationLock annotationLock)
			throws DataAccessException, UnsupportedEncodingException,
			BadSqlGrammarException {
		boolean result = false;
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("lockId", annotationLock.getLockId());
		result = StoredProcedureDataAccessUtils.execute(getJdbcTemplate(),
				"spUpdateAnnotationLock", inParamMap);

		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public AnnotationLock getLock(int lockId) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException {

		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("lockId", lockId);
		List<AnnotationLock> locks = (List<AnnotationLock>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(), "spGetAnnotationLockByLockId",
						new AnnotationLockRowMapper(), inParamMap);

		return locks.get(0);
	}

}
