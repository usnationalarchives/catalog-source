package gov.nara.opa.api.valueobject.user.contributions;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import gov.nara.opa.architecture.utils.TimestampUtils;
import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

public class UserContributedCommentValueObject extends
		AbstractWebEntityValueObject implements
		UserContributedCommentValueObjectConstants {

	private Integer annotationId;
	private String annotation;
	private Boolean status;
	private String naId;
	private String objectId;
	private Integer pageNum;
	private String opaId;
	private Integer accountId;
	private Timestamp annotationTS;
	private Integer parentId;
	private Integer replies;

	// opa titles related fields
	private String type;
	private String title;
	private Integer totalPages;

	// account related fields
	private String userName;
	private String fullName;
	private Boolean isNaraStaff;
	private Boolean displayNameFlag;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Integer getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(Integer totalPages) {
		this.totalPages = totalPages;
	}

	public Integer getReplies() {
		return replies;
	}

	public void setReplies(Integer replies) {
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
		aspireContent.put(ANNOTATION_ID_ASP, getAnnotationId());

		if (getParentId() != null) {
			aspireContent.put(PARENT_ID_ASP, getParentId());
		}

		if (getTitle() != null) {
			aspireContent.put(OPA_TITLE_ASP, getTitle());
		}

		if (getNaId() != null) {
			aspireContent.put(NA_ID_ASP, getNaId());
		}

		if (getObjectId() != null) {
			aspireContent.put(OBJECT_ID_ASP, getObjectId());
		}

		if (getTotalPages() != null) {
			aspireContent.put(TOTAL_PAGES_ASP, getTotalPages());
		}

		if (getPageNum() != null) {
			aspireContent.put(PAGE_NUM_ASP, getPageNum());
		} else {
			aspireContent.put(PAGE_NUM_ASP, getTotalPages());
		}

		if (getReplies() != null) {
			aspireContent.put(REPLIES_ASP, getReplies());
		}

		if (getAnnotationTS() != null) {
			aspireContent.put(LAST_MODIFIED_ASP,
					TimestampUtils.getUtcString(getAnnotationTS()));
		}

		return aspireContent;
	}

	@Override
	public Map<String, Object> getDatabaseContent() {
		Map<String, Object> databaseContent = new HashMap<String, Object>();
		return databaseContent;
	}
}
