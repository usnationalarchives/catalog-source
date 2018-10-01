package gov.nara.opa.api.services.impl.moderator;

import gov.nara.opa.api.annotation.locks.AnnotationLock;
import gov.nara.opa.api.annotation.locks.AnnotationLockErrorCode;
import gov.nara.opa.api.annotation.transcriptions.Transcription;
import gov.nara.opa.api.annotation.transcriptions.TranscriptionErrorCode;
import gov.nara.opa.api.annotation.transcriptions.TranscriptionErrorConstants;
import gov.nara.opa.api.dataaccess.annotation.logs.AnnotationLogDao;
import gov.nara.opa.api.dataaccess.annotation.transcriptions.TranscriptionDao;
import gov.nara.opa.api.services.ServiceResponseObject;
import gov.nara.opa.api.services.annotation.locks.CreateLockService;
import gov.nara.opa.api.services.annotation.locks.DeleteLockService;
import gov.nara.opa.api.services.annotation.locks.ValidateLockService;
import gov.nara.opa.api.services.annotation.transcriptions.ViewTranscriptionService;
import gov.nara.opa.api.services.impl.annotation.transcriptions.TranscriptionServiceUtils;
import gov.nara.opa.api.services.moderator.RemoveTranscriptionService;
import gov.nara.opa.api.services.moderator.RestoreTranscriptionService;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.common.valueobject.annotation.logs.AnnotationLogValueObject;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class RestoreTranscriptionServiceImpl implements
		RestoreTranscriptionService {
	private static OpaLogger logger = OpaLogger
			.getLogger(RestoreTranscriptionServiceImpl.class);

	@Autowired
	private ValidateLockService lockValidator;

	@Autowired
	private CreateLockService lockCreator;

	@Autowired
	private DeleteLockService lockDeleter;

	@Autowired
	private RemoveTranscriptionService removeTranscriptionService;

	@Autowired
	private ViewTranscriptionService viewTranscriptionService;

	@Autowired
	private TranscriptionDao transcriptionDao;

	@Autowired
	private AnnotationLogDao annotationLogDao;

	@Override
	public ServiceResponseObject restoreTranscription(String naId,
			String objectId, int version, int reasonId, String notes,
			String sessionId, int accountId) {
		TranscriptionErrorCode errorCode = TranscriptionErrorCode.NONE;
		HashMap<String, Object> resultHashMap = new HashMap<String, Object>();
		ServiceResponseObject result = null;
		Transcription transcription = null;

		// Retrieve transcription
		try {

			// Get actual version from annotation log
			AnnotationLogValueObject log = annotationLogDao.selectByVersion(
					"TR", naId, objectId, version);

			if (log != null) {
				int transcriptionId = log.getAnnotationId();

				transcription = transcriptionDao.select(transcriptionId);
			}

			if (transcription != null) {
				result = restoreTranscription(transcription, reasonId, notes,
						sessionId, accountId);

			} else {
				throw new Exception("Transcription not found");
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			errorCode = TranscriptionErrorCode.INTERNAL_ERROR;
			errorCode.setErrorMessage(e.getMessage());
		}

		if (result == null) {
			result = new ServiceResponseObject(errorCode, resultHashMap);
		}

		return result;
	}

	@Override
	public ServiceResponseObject restoreTranscription(
			Transcription transcription, int reasonId, String notes,
			String sessionId, int accountId) {
		TranscriptionErrorCode errorCode = TranscriptionErrorCode.NONE;
		HashMap<String, Object> resultHashMap = new HashMap<String, Object>();

		ServiceResponseObject responseObject = null;

		try {

			// Determine if a transcription with another version is active and
			// disable
			// Find active transcription
			Transcription activeTranscription = transcriptionDao.select(
					transcription.getNaId(), transcription.getObjectId(), 1);
			if (activeTranscription != null) {
				// Check if it's not the same transcription
				if (activeTranscription.getAnnotationId() != transcription
						.getAnnotationId()) {
					removeTranscriptionService.removeTranscription(
							activeTranscription, reasonId, notes, sessionId,
							accountId);
				} else {
					errorCode = TranscriptionErrorCode.DUPLICATE_ANNOTATION;
					errorCode
							.setErrorMessage(TranscriptionErrorConstants.sameTranscription);
				}
			}

			if (errorCode == TranscriptionErrorCode.NONE) {
				// Acquire lock
				responseObject = lockCreator.create(TranscriptionServiceUtils
						.getLockInstance(accountId, transcription.getNaId(),
								transcription.getObjectId()));
				errorCode = translateLockErrorCode(responseObject);
			}

			if (errorCode == TranscriptionErrorCode.NONE) {
				// Lock was acquired
				AnnotationLock lock = (AnnotationLock) responseObject
						.getContentMap().get("AnnotationLock");

				// Get new version
				int version = TranscriptionServiceUtils.getNewVersion(
						annotationLogDao, transcription.getNaId(),
						transcription.getObjectId());

				// Create new log entry
				AnnotationLogValueObject newLogEntry = TranscriptionServiceUtils
						.getNewAnnotationLogEntry(transcription, accountId,
								null, version, transcription.getAccountId(),
								"RESTORE", sessionId, true, reasonId, notes);
				if (!annotationLogDao.insert(newLogEntry)) {
					throw new Exception("Unable to insert new entry");
				}

				// Enable version and set new version
				transcription.setStatus(true);
				transcription.setSavedVersNum(version);
				if (!transcriptionDao.update(transcription)) {
					throw new Exception("Unable to update transcription");
				}

				// Release lock
				responseObject = lockDeleter.delete(lock.getNaId(),
						lock.getObjectId(), null, accountId);
				errorCode = translateLockErrorCode(responseObject);

				if (errorCode == TranscriptionErrorCode.NONE) {
					// Return transcription information
					responseObject = viewTranscriptionService
							.getFullTranscription(transcription.getNaId(),
									transcription.getObjectId());
					errorCode = (TranscriptionErrorCode) responseObject
							.getErrorCode();
				}

				if (errorCode == TranscriptionErrorCode.NONE) {
					HashMap<String, Object> results = (HashMap<String, Object>) responseObject
							.getContentMap();
					resultHashMap.putAll(results);
				}

			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			errorCode = TranscriptionErrorCode.INTERNAL_ERROR;
		}

		ServiceResponseObject result = new ServiceResponseObject(errorCode,
				resultHashMap);
		return result;
	}

	private TranscriptionErrorCode translateLockErrorCode(
			ServiceResponseObject responseObject) {
		return TranscriptionErrorCode
				.valueOf(((AnnotationLockErrorCode) responseObject
						.getErrorCode()).toString());
	}

}
