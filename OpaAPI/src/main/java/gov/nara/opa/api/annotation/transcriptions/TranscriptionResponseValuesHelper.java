package gov.nara.opa.api.annotation.transcriptions;

import gov.nara.opa.api.annotation.locks.AnnotationLock;
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.system.logging.APILogger;
import gov.nara.opa.api.user.accounts.UserAccount;
import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.utils.TimestampUtils;
import gov.nara.opa.common.valueobject.annotation.logs.AnnotationLogValueObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Encapsulates the creation of the object that will be processed in the
 * AspireObject and converted to either json or xml
 */
@Component
@Scope("prototype")
public class TranscriptionResponseValuesHelper {
	private static APILogger log = APILogger
			.getLogger(TranscriptionResponseValuesHelper.class);

	@Autowired
	private ConfigurationService configService;

	private AnnotationLock annotationLock;
	private UserAccount userAccount;
	private UserAccount lockUserAccount;
	private Transcription transcription;
	private AnnotationLogValueObject annotationLog;
	private HashMap<Integer, UserAccount> contributorMap;
	private LinkedHashMap<Integer, Transcription> previousTranscriptions;

	public TranscriptionResponseValuesHelper() {
	}

	public TranscriptionResponseValuesHelper(AnnotationLock annotationLock,
			UserAccount userAccount, UserAccount lockUserAccount, Transcription transcription) {
		this.annotationLock = annotationLock;
		this.userAccount = userAccount;
		this.lockUserAccount = lockUserAccount;
		this.transcription = transcription;
	}

	public AnnotationLock getAnnotationLock() {
		return annotationLock;
	}

	public void setAnnotationLock(AnnotationLock annotationLock) {
		this.annotationLock = annotationLock;
	}

	public UserAccount getUserAccount() {
		return userAccount;
	}

	public void setUserAccount(UserAccount userAccount) {
		this.userAccount = userAccount;
	}

	public UserAccount getLockUserAccount() {
		return lockUserAccount;
	}

	public void setLockUserAccount(UserAccount lockUserAccount) {
		this.lockUserAccount = lockUserAccount;
	}

	public Transcription getTranscription() {
		return transcription;
	}

	public void setTranscription(Transcription transcription) {
		this.transcription = transcription;
	}

	public AnnotationLogValueObject getAnnotationLog() {
		return annotationLog;
	}

	public void setAnnotationLog(AnnotationLogValueObject annotationLog) {
		this.annotationLog = annotationLog;
	}

	public HashMap<Integer, UserAccount> getContributorMap() {
		return contributorMap;
	}

	public void setContributorMap(HashMap<Integer, UserAccount> contributorMap) {
		this.contributorMap = contributorMap;
	}

	public LinkedHashMap<Integer, Transcription> getPreviousTranscriptions() {
		return previousTranscriptions;
	}

	public void setPreviousTranscriptions(
			LinkedHashMap<Integer, Transcription> previousTranscriptions) {
		this.previousTranscriptions = previousTranscriptions;
	}

	/**
	 * Sets all internal fields to null. This is necessary because Spring will
	 * instantiate this as a singleton class.
	 */
	public void Init() {
		this.annotationLock = null;
		this.annotationLog = null;
		this.contributorMap = null;
		this.previousTranscriptions = null;
		this.transcription = null;
		this.userAccount = null;
		this.lockUserAccount = null;
	}

	/**
	 * Builds a LinkedHashMap that will be passed to the APIResponse instance to
	 * process the output. Must have at least a valid user account. When a
	 * transcription doesn't exist it must have at least a lock object. When a
	 * transcription exists may have both the previous transcriptions and
	 * contributors or none.
	 * 
	 * @return A LinkedHashMap instance with the requested output from the objects
	 *         that were set.
	 */
	public LinkedHashMap<String, Object> getResponseValues() {
		LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();

		if (transcription == null && annotationLock == null) {
			log.debug("getResponseValues", "no transcription nor lock");
			result.put("result", "transcription not found");
		} else {

			// Last modified date
			if (transcription != null) {
				try {
					result.put(
							"@lastModified", TimestampUtils.getUtcString(transcription
									.getAnnotationTS()));
					result.put("@pageNumber", transcription.getPageNum());
				} catch (Exception e) {
					log.debug("null pointer!!");
				}
			}

			// Lock (true/false)
			result.put("isLocked", (annotationLock != null ? "true" : "false"));

			if (transcription != null) {
				result.put("@accountId", userAccount.getAccountId());
				result.put("@userName", userAccount.getUserName());
				result.put("@fullName", userAccount.getFullName());
				result.put("@displayFullName", userAccount.isDisplayNameFlag() ? "true"
						: "false");

				// Authoritative (true/false)
				result.put("@isAuthoritative",
						(userAccount.getEmailAddress().endsWith(configService.getConfig().getNaraEmail()) ? "true"
								: "false"));

				// Version number
				result.put("@version",
						(transcription != null ? transcription.getSavedVersNum() : 1));
			}

			// locked by
			if (annotationLock != null) {
				LinkedHashMap<String, Object> lockInfo = new LinkedHashMap<String, Object>();
				lockInfo.put("@id", lockUserAccount.getUserName());
				lockInfo.put("@fullName", lockUserAccount.getFullName());
				lockInfo.put("@displayFullName", lockUserAccount.isDisplayNameFlag() ? "true"
						: "false");
				lockInfo.put("@isNaraStaff", lockUserAccount.isNaraStaff() ? "true"
						: "false");
				lockInfo.put("@when",
						TimestampUtils.getUtcString(annotationLock.getLockTS()));
				result.put("lockedBy", lockInfo);
			}

			if (transcription != null) {
				// Users
				LinkedHashMap<String, Object> usersInfo = new LinkedHashMap<String, Object>();
				usersInfo
				.put("@total",
						(previousTranscriptions != null ? previousTranscriptions.size()
								: 0));

				// Process users
				ArrayList<LinkedHashMap<String, Object>> userList = new ArrayList<LinkedHashMap<String, Object>>();

				if (previousTranscriptions != null) {
					for (Transcription previousTranscription : previousTranscriptions
							.values()) {
						LinkedHashMap<String, Object> userInfo = new LinkedHashMap<String, Object>();
						try {
							UserAccount user = contributorMap.get(previousTranscription
									.getAccountId());

							userInfo.put("@id", user.getUserName());
							userInfo.put("@fullName", user.getFullName());
							userInfo.put("@displayFullName", user.isDisplayNameFlag() ? "true"
									: "false");
							userInfo.put("@isNaraStaff", user.isNaraStaff());
							userInfo.put("@lastModified", TimestampUtils
									.getUtcString(previousTranscription.getAnnotationTS()));
							userList.add(userInfo);
						} catch (Exception e) {
							throw new OpaRuntimeException("Null contributorMap");
						}

					}
				}

				usersInfo.put("user", userList);

				result.put("users", usersInfo);

				// Text
				result.put("text",
						(transcription != null ? transcription.getAnnotation() : ""));
			}
		}
		return result;
	}

}
