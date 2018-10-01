package gov.nara.opa.common.valueobject.annotation.comments;

import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CommentValueObject extends AbstractWebEntityValueObject implements
		CommentValueObjectConstants {

	private Integer annotationId;
	private String annotation;
	private Boolean status;
	private String naId;
	private String objectId;
	private Integer pageNum;
	private String opaId;
	private Integer accountId;
	private Timestamp annotationTS;
	private Timestamp annotationCreatedTS;
	private String annotationMD5;
	private Integer parentId;
	private Integer sequence;
	private List<CommentValueObject> replies;

	// account related fields
	private String userName;
	private String fullName;
	private Boolean isNaraStaff;
	private Boolean displayNameFlag;

	public List<CommentValueObject> getReplies() {
		return replies;
	}

	public void setReplies(List<CommentValueObject> replies) {
		this.replies = replies;
	}

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

	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	public Integer getSequence() {
		return sequence;
	}

	public void setSequence(Integer sequence) {
		this.sequence = sequence;
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
		if (pageNum != null && pageNum != 0) {
			this.pageNum = pageNum;
		}
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

	public Timestamp getAnnotationCreatedTS() {
		return annotationCreatedTS;
	}

	public void setAnnotationCreatedTS(Timestamp annotationCreatedTS) {
		this.annotationCreatedTS = annotationCreatedTS;
	}

	public String getAnnotationMD5() {
		return annotationMD5;
	}

	public void setAnnotationMD5(String annotationMD5) {
		this.annotationMD5 = annotationMD5;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public Boolean getIsNaraStaff() {
		return isNaraStaff;
	}

	public void setIsNaraStaff(Boolean isNaraStaff) {
		this.isNaraStaff = isNaraStaff;
	}

	public Boolean getDisplayNameFlag() {
		return displayNameFlag;
	}

	public void setDisplayNameFlag(Boolean displayNameFlag) {
		this.displayNameFlag = displayNameFlag;
	}

	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		LinkedHashMap<String, Object> aspireContent = new LinkedHashMap<String, Object>();
		aspireContent.put(ANNOTATION_COMMENT_ASP, getAnnotation());

		if (getAnnotationId() != null) {
			aspireContent.put(ANNOTATION_ID_ASP, getAnnotationId());
		}

		if (getPageNum() != null) {
			aspireContent.put(PAGE_NUM_ASP, getPageNum());
		}

		if (getUserName() != null) {
			aspireContent.put(USER_NAME_ASP, getUserName());
		}

		if (getFullName() != null && (getDisplayNameFlag() || getIsNaraStaff())) {
			aspireContent.put(FULL_NAME_ASP, getFullName());
		}

		aspireContent.put(DISPLAY_FULL_NAME, getDisplayNameFlag());
		aspireContent.put(REMOVED_BY_MODERATOR_ASP, !getStatus());

		if (getIsNaraStaff() != null) {
			aspireContent.put(IS_NARA_STAFF_ASP, getIsNaraStaff());
		}

		if (getAnnotationTS() != null) {
			aspireContent.put(LAST_MODIFIED_ASP, getAnnotationTS());
		}

		if (getAnnotationTS() != null) {
			aspireContent.put(CREATED_ASP, getAnnotationCreatedTS());
		}

		if (getReplies() != null) {
			aspireContent.put(REPLIES_ASP, getReplies());
		}

		return aspireContent;
	}

	@Override
	public Map<String, Object> getDatabaseContent() {
		Map<String, Object> databaseContent = new HashMap<String, Object>();

		databaseContent.put(ANNOTATION_DB, getAnnotation());
		databaseContent.put(ANNOTATION_MD5_DB, getAnnotationMD5());
		databaseContent.put(STATUS_DB, getStatus());
		databaseContent.put(NA_ID_DB, getNaId());
		databaseContent.put(OBJECT_ID_DB, getObjectId());
		databaseContent.put(PAGE_NUM_DB, getPageNum());
		databaseContent.put(OPA_ID_DB, getOpaId());
		databaseContent.put(ACCOUNT_ID_DB, getAccountId());
		databaseContent.put(ANNOTATION_TS_DB, getAnnotationTS());
		databaseContent.put(ANNOTATION_CREATED_TS_DB, getAnnotationCreatedTS());

		return databaseContent;
	}
}
