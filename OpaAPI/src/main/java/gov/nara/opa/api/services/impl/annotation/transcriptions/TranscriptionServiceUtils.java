package gov.nara.opa.api.services.impl.annotation.transcriptions;

import gov.nara.opa.api.annotation.locks.AnnotationLock;
import gov.nara.opa.api.annotation.transcriptions.Transcription;
import gov.nara.opa.api.dataaccess.annotation.logs.AnnotationLogDao;
//import gov.nara.opa.api.system.logging.APILogger;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.common.valueobject.annotation.logs.AnnotationLogValueObject;

import java.io.UnsupportedEncodingException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;

public class TranscriptionServiceUtils {

	private static OpaLogger logger = OpaLogger
			.getLogger(TranscriptionServiceUtils.class);

	public static AnnotationLogValueObject getNewAnnotationLogEntry(
			Transcription transcription, int accountId,
			AnnotationLogValueObject previousLogEntry, int version,
			int affectedUser, String action, String sessionId) {
		return getNewAnnotationLogEntry(transcription, accountId,
				previousLogEntry, version, affectedUser, action, sessionId,
				true, 0, null);
	}

	public static AnnotationLogValueObject getNewAnnotationLogEntry(
			Transcription transcription, int accountId,
			AnnotationLogValueObject previousLogEntry, int version,
			int affectedUser, String action, String sessionId, boolean status,
			int reasonId, String notes) {
		AnnotationLogValueObject newLogEntry = new AnnotationLogValueObject();
		newLogEntry.setAnnotationType("TR");
		newLogEntry.setAnnotationId(transcription.getAnnotationId());
		newLogEntry
				.setFirstAnnotationId(previousLogEntry != null ? previousLogEntry
						.getFirstAnnotationId() : transcription
						.getFirstAnnotationId());
		newLogEntry.setVersionNum(version);
		newLogEntry.setAnnotationMD5(transcription.getAnnotationMD5());
		newLogEntry.setStatus(status);
		newLogEntry.setNaId(transcription.getNaId());
		newLogEntry.setObjectId(transcription.getObjectId());
		newLogEntry.setPageNum(transcription.getPageNum());
		// TODO: opa_id
		newLogEntry.setAccountId(accountId);
		newLogEntry.setSessionId(sessionId);
		newLogEntry.setReasonId(reasonId);
		newLogEntry.setNotes(notes);
		newLogEntry.setAffectsAccountId(affectedUser);

		newLogEntry
				.setFirstAccountId(previousLogEntry != null ? previousLogEntry
						.getFirstAccountId() : accountId);
		newLogEntry.setAction(action);

		return newLogEntry;
	}

	public static AnnotationLogValueObject getNewAnnotationLogEntry(
			Transcription transcription, int accountId, int firstAnnotationId,
			int firstAccountId, int version, int affectedUser, String action,
			String sessionId) {
		return getNewAnnotationLogEntry(transcription, accountId,
				firstAnnotationId, firstAccountId, version, affectedUser,
				action, sessionId, true, 0, null);
	}

	public static AnnotationLogValueObject getNewAnnotationLogEntry(
			Transcription transcription, int accountId, int firstAnnotationId,
			int firstAccountId, int version, int affectedUser, String action,
			String sessionId, boolean status, int reasonId, String notes) {
		AnnotationLogValueObject newLogEntry = new AnnotationLogValueObject();
		newLogEntry.setAnnotationType("TR");
		newLogEntry.setAnnotationId(transcription.getAnnotationId());
		newLogEntry.setFirstAnnotationId(firstAnnotationId);
		newLogEntry.setVersionNum(version);
		newLogEntry.setAnnotationMD5(transcription.getAnnotationMD5());
		newLogEntry.setStatus(status);
		newLogEntry.setNaId(transcription.getNaId());
		newLogEntry.setObjectId(transcription.getObjectId());
		newLogEntry.setPageNum(transcription.getPageNum());
		// TODO: opa_id
		newLogEntry.setAccountId(accountId);
		newLogEntry.setSessionId(sessionId);
		newLogEntry.setReasonId(reasonId);
		newLogEntry.setNotes(notes);
		newLogEntry.setAffectsAccountId(affectedUser);

		newLogEntry.setFirstAccountId(firstAccountId);
		newLogEntry.setAction(action);

		// log.trace("getNewAnnotationLogEntry", "New entry:" +
		// newLogEntry.toString());

		return newLogEntry;
	}

	public static AnnotationLock getLockInstance(int accountId, String naId,
			String objectId) {
		AnnotationLock lock = new AnnotationLock();
		lock.setAccountId(accountId);
		lock.setNaId(naId);
		lock.setObjectId(objectId);

		return lock;
	}

	/**
	 * Gets the new transcription version number
	 * 
	 * @param naId
	 * @param objectId
	 * @return The next available transcription version
	 */
	public static int getNewVersion(AnnotationLogDao dao, String naId,
			String objectId) {
		int result = 1;
		try {
			result = dao.getHighestVersion("TR", naId, objectId) + 1;

		} catch (BadSqlGrammarException e) {
			logger.error(e.getMessage(), e);
			// throw new OpaRuntimeException(e);
		} catch (DataAccessException e) {
			logger.error(e.getMessage(), e);
			// throw new OpaRuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
			// throw new OpaRuntimeException(e);
		}

		return result;
	}

}
