package gov.nara.opa.common.valueobject.annotation.translations;

import gov.nara.opa.architecture.utils.TimestampUtils;
import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class TranslationValueObject extends AbstractWebEntityValueObject 
	implements TranslationValueObjectConstants {
	
	private Integer annotationId;
	private String annotation;
	private String language;
	private Integer savedVersionNumber;
	private Integer firstAnnotationId;
	private String annotationMD5;
	private Boolean status;
	private String naId;
	private String objectId;
	private Integer pageNum;
	private String opaId;
	private Integer accountId;
	private Timestamp annotationTS;

	private boolean hasBeenModified;
	private boolean isLocked;

	private UserAccountValueObject user;

	public Integer getAnnotationId() {
	    return annotationId;
	}

	public void setAnnotationId(Integer annotationId) {
	    this.annotationId = annotationId;
	}
	
	public String getAnnotation() {
	    return annotation;
	}
	
	public void setAnnotation(String annotation) {
	    this.annotation = annotation;
	}
	
	public String getLanguage() {
		return language;
	}
	
	public void setLanguage(String language) {
		this.language = language;
	}
	
	public Integer getSavedVersionNumber() {
	    return savedVersionNumber;
	}
	
	public void setSavedVersionNumber(Integer savedVersionNumber) {
	    this.savedVersionNumber = savedVersionNumber;
	}
	
	public String getAnnotationMD5() {
	    return annotationMD5;
	}
	
	public void setAnnotationMD5(String annotationMD5) {
	    this.annotationMD5 = annotationMD5;
	}
	  
	public Integer getFirstAnnotationId() {
	    return firstAnnotationId;
	}
	
	public void setFirstAnnotationId(Integer firstAnnotationId) {
	    this.firstAnnotationId = firstAnnotationId;
	}
	
	public Boolean getStatus() {
	    return status;
	}
	
	public void setStatus(Boolean status) {
	    this.status = status;
	}
	
	public String getNaId() {
	    return naId;
	}
	
	public void setNaId(String naId) {
	    this.naId = naId;
	}
	
	public String getObjectId() {
	    return objectId;
	}
	
	public void setObjectId(String objectId) {
	    this.objectId = objectId;
	}
	
	public Integer getPageNum() {
	    return pageNum;
	}
	
	public void setPageNum(Integer pageNum) {
	    this.pageNum = pageNum;
	}
	
	public String getOpaId() {
	    return opaId;
	}
	
	public void setOpaId(String opaId) {
	    this.opaId = opaId;
	}
	
	public Integer getAccountId() {
	    return accountId;
	}
	
	public void setAccountId(Integer accountId) {
	    this.accountId = accountId;
	}
	
	public Timestamp getAnnotationTS() {
	    return annotationTS;
	}
	
	public void setAnnotationTS(Timestamp annotationTS) {
	    this.annotationTS = annotationTS;
	}
	
	public UserAccountValueObject getUser() {
	    return user;
	}
	
	public void setUser(UserAccountValueObject user) {
	    this.user = user;
	}

	public boolean isLocked() {
		return isLocked;
	}

	public void lock(boolean isLocked) {
		this.isLocked = isLocked;
	}

	public void modify() {
		hasBeenModified = true;
	}

	/*
	 * (non-Javadoc)
	 * @see gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject#getDatabaseContent()
	 */
	@Override
	public Map<String, Object> getDatabaseContent() {
		Map<String, Object> databaseContent = new HashMap<String, Object>();

		databaseContent.put(ANNOTATION_DB, getAnnotation());
		databaseContent.put(LANGUAGE_DB, getLanguage());
		databaseContent.put(SAVED_VERS_NUM_DB, getSavedVersionNumber());
		databaseContent.put(FIRST_ANNOTATION_ID_DB, getFirstAnnotationId());
		databaseContent.put(ANNOTATION_MD5_DB, getAnnotationMD5());
		databaseContent.put(STATUS_DB, getStatus());
		databaseContent.put(NA_ID_DB, getNaId());
		databaseContent.put(OBJECT_ID_DB, getObjectId());
		databaseContent.put(PAGE_NUM_DB, getPageNum());
		databaseContent.put(OPA_ID_DB, getOpaId());
		databaseContent.put(ACCOUNT_ID_DB, getUser().getAccountId());
		databaseContent.put(ANNOTATION_TS_DB, getAnnotationTS());

		return databaseContent;
	}

	/*
	 * (non-Javadoc)
	 * @see gov.nara.opa.architecture.web.valueobject.AbstractWebValueObject#getAspireObjectContent(java.lang.String)
	 */
	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		LinkedHashMap<String, Object> aspireContent = 
				new LinkedHashMap<String, Object>();

		aspireContent.put(CODE_ASP, getLanguage());
		if (hasBeenModified) {
			aspireContent.put(LAST_MODIFIED_ASP, TimestampUtils.
					getUtcString(getAnnotationTS()));
			aspireContent.put(PAGE_NUM_ASP, getPageNum());
		}
		aspireContent.put(IS_LOCKED_ASP, (isLocked() ? "true" : "false"));

		if (hasBeenModified) {
			aspireContent.put(ACCOUNT_ID_ASP, getAccountId());
			aspireContent.put(USERNAME_ASP, getUser().getUserName());
			aspireContent.put(FULLNAME_ASP, getUser().getFullName());
			aspireContent.put(DISPLAY_FULLNAME_ASP, (
					getUser().getDisplayFullName() ? "true" : "false"));
			aspireContent.put(IS_AUTHORITATIVE_ASP, (
					getUser().isNaraStaff() ? "true" : "false"));
			aspireContent.put(VERSION_ASP, getSavedVersionNumber());
		}

		if (isLocked()) {
			LinkedHashMap<String, Object> lockInfo = 
					new LinkedHashMap<String, Object>();
			lockInfo.put(ID_ASP, getUser().getUserName());
			lockInfo.put(FULLNAME_ASP, getUser().getFullName());
			lockInfo.put(DISPLAY_FULLNAME_ASP, (
					getUser().getDisplayFullName() ? "true" : "false"));
			lockInfo.put(IS_NARASTAFF_ASP, (
					getUser().isNaraStaff() ? "true" : "false"));
			lockInfo.put(WHEN_ASP, 
					TimestampUtils.getUtcString(getAnnotationTS()));
			aspireContent.put(LOCKED_BY_ASP, lockInfo);
		}

		if (hasBeenModified) {
			LinkedHashMap<String, Object> usersInfo = 
					new LinkedHashMap<String, Object>();
			usersInfo.put(TOTAL_RECORDS_ASP, 0);
			ArrayList<LinkedHashMap<String, Object>> userList = 
					new ArrayList<LinkedHashMap<String, Object>>();
			usersInfo.put(USER_ASP, userList);
			aspireContent.put(USERS_ASP, usersInfo);
		}

		return aspireContent;
	}

}
