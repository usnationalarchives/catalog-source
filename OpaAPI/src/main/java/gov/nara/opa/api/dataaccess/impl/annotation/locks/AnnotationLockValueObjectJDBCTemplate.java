package gov.nara.opa.api.dataaccess.impl.annotation.locks;

import gov.nara.opa.api.dataaccess.annotation.locks.AnnotationLockValueObjectDao;
import gov.nara.opa.architecture.dataaccess.AbstractOpaDbJDBCTemplate;
import gov.nara.opa.common.AnnotationConstants;
import gov.nara.opa.common.dataaccess.utils.GenericRowMapper;
import gov.nara.opa.common.dataaccess.utils.StoredProcedureDataAccessUtils;
import gov.nara.opa.common.valueobject.annotation.lock.AnnotationLockValueObject;
import gov.nara.opa.common.valueobject.annotation.lock.AnnotationLockValueObjectConstants;

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
public class AnnotationLockValueObjectJDBCTemplate extends AbstractOpaDbJDBCTemplate 
	implements AnnotationLockValueObjectDao, AnnotationLockValueObjectConstants {

	@Override
	public boolean insert(AnnotationLockValueObject annotationLock)
			throws DataAccessException, UnsupportedEncodingException,
			BadSqlGrammarException {
		boolean result = false;
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("accountId", annotationLock.getAccountId());
		annotationLock.getNaId().getBytes(AnnotationConstants.UTF8_ENCODING);
		inParamMap.put("naId", annotationLock.getNaId());
		annotationLock.getObjectId().getBytes(AnnotationConstants.UTF8_ENCODING);
		inParamMap.put("objectId", annotationLock.getObjectId());
		if (annotationLock.getOpaId() != null) {
			annotationLock.getOpaId().getBytes(AnnotationConstants.UTF8_ENCODING);
			inParamMap.put("opaId", annotationLock.getOpaId());
		} else {
			inParamMap.put("opaId", null);
		}
		if (annotationLock.getLanguageISO() != null) {
			annotationLock.getLanguageISO().getBytes(AnnotationConstants.UTF8_ENCODING);
			inParamMap.put("languageIso", annotationLock.getLanguageISO());
		} else {
			inParamMap.put("languageIso", null);
		}

		result = StoredProcedureDataAccessUtils.execute(getJdbcTemplate(),
				"spInsertAnnotationLock", inParamMap);
		return result;
	}

	@Override
	public boolean update(AnnotationLockValueObject annotationLock)
			throws DataAccessException, UnsupportedEncodingException,
			BadSqlGrammarException {
		boolean result = false;
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("lockId", annotationLock.getLockId());
		result = StoredProcedureDataAccessUtils.execute(getJdbcTemplate(),
				"spUpdateAnnotationLock", inParamMap);
		return result;
	}

	@Override
	public boolean validateLock(int accountId, String naId, String objectId,
			String languageISO, int minuteWindow) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException {
		List<AnnotationLockValueObject> locks = getLocks(naId, objectId, languageISO,
				minuteWindow);
		for (AnnotationLockValueObject lock : locks) {
			if (lock.getAccountId() == accountId) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AnnotationLockValueObject> getLocks(String naId,
			String objectId, String languageISO, int minuteWindow)
			throws DataAccessException, UnsupportedEncodingException,
			BadSqlGrammarException {
		naId.getBytes(AnnotationConstants.UTF8_ENCODING);
		objectId.getBytes(AnnotationConstants.UTF8_ENCODING);
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("naId", naId);
		inParamMap.put("objectId", objectId);
		inParamMap.put("minuteWindow", minuteWindow);

		if (languageISO != null) {
			languageISO.getBytes(AnnotationConstants.UTF8_ENCODING);
			inParamMap.put("languageIso", languageISO);
		} else {
			inParamMap.put("languageIso", null);
		}

		List<AnnotationLockValueObject> locks = (List<AnnotationLockValueObject>) 
				StoredProcedureDataAccessUtils.execute(getJdbcTemplate(), 
						"spGetAnnotationLocks", 
						new GenericRowMapper<AnnotationLockValueObject>(
								new AnnotationLockValueObjectExtractor()), inParamMap);
		return locks;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AnnotationLockValueObject> getLocksForUser(int accountId,
			int minuteWindow) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("accountId", accountId);
		inParamMap.put("minuteWindow", minuteWindow);

		List<AnnotationLockValueObject> locks = (List<AnnotationLockValueObject>) 
				StoredProcedureDataAccessUtils.execute(getJdbcTemplate(),
						"spGetAnnotationLocksByAccountUser",
						new GenericRowMapper<AnnotationLockValueObject>(
								new AnnotationLockValueObjectExtractor()), inParamMap);

		return locks;
	}

	@SuppressWarnings("unchecked")
	@Override
	public AnnotationLockValueObject getLock(int lockId)
			throws DataAccessException, UnsupportedEncodingException,
			BadSqlGrammarException {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("lockId", lockId);
		List<AnnotationLockValueObject> locks = (List<AnnotationLockValueObject>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(), "spGetAnnotationLockByLockId",
						new GenericRowMapper<AnnotationLockValueObject>(
								new AnnotationLockValueObjectExtractor()), inParamMap);
		return locks.get(0);
	}

	@Override
	public boolean delete(String naId, String objectId, String languageISO,
			int accountId) throws DataAccessException,
			UnsupportedEncodingException, BadSqlGrammarException {
		boolean result = false;
		naId.getBytes(AnnotationConstants.UTF8_ENCODING);
		objectId.getBytes(AnnotationConstants.UTF8_ENCODING);
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("naId", naId);
		inParamMap.put("objectId", objectId);
		inParamMap.put("accountId", accountId);

		if (languageISO != null) {
			languageISO.getBytes(AnnotationConstants.UTF8_ENCODING);
			inParamMap.put("languageIso", languageISO);
			result = StoredProcedureDataAccessUtils.execute(getJdbcTemplate(),
					"spDeleteAnnotationLockWithLanguageISO", inParamMap);
		} else {
			result = StoredProcedureDataAccessUtils.execute(getJdbcTemplate(),
					"spDeleteAnnotationLock", inParamMap);
		}
		return result;
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

}
