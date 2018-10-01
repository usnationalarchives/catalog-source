package gov.nara.opa.common.valueobject.annotation.transcriptions;

import gov.nara.opa.architecture.utils.TimestampUtils;
import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TranscriptionValueObject extends AbstractWebEntityValueObject {

  private Integer annotationId;
  private String annotation;
  private Integer savedVersionNumber;
  private String annotationMD5;
  private Integer firstAnnotationId;
  private Boolean status;
  private String naId;
  private String objectId;
  private Integer pageNum;
  private String opaId;
  private Integer accountId;
  private Timestamp annotationTS;
  
  private Boolean hasTranscription;
  private Boolean isLocked;
  
  private UserAccountValueObject user;
  private UserAccountValueObject lockUser;
  
  //private String lockUserName;
  //private String lockUserFullName;
  //private Boolean lockUserDisplayName;
  //private Boolean lockUserIsNaraStaff;
  private Timestamp lockTS;
  
  private List<TranscriptionValueObject> previousTranscriptions;
  

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


  public Boolean getIsLocked() {
    return isLocked;
  }

  public void setIsLocked(Boolean isLocked) {
    this.isLocked = isLocked;
  }

  public Boolean getHasTranscription() {
    return hasTranscription;
  }

  public void setHasTranscription(Boolean hasTranscription) {
    this.hasTranscription = hasTranscription;
  }

  public Timestamp getLockTS() {
    return lockTS;
  }

  public void setLockTS(Timestamp lockTS) {
    this.lockTS = lockTS;
  }

  public List<TranscriptionValueObject> getPreviousTranscriptions() {
    return previousTranscriptions;
  }

  public void setPreviousTranscriptions(
      List<TranscriptionValueObject> previousTranscriptions) {
    this.previousTranscriptions = previousTranscriptions;
  }

  public UserAccountValueObject getUser() {
    return user;
  }

  public void setUser(UserAccountValueObject user) {
    this.user = user;
  }

  public UserAccountValueObject getLockUser() {
    return lockUser;
  }

  public void setLockUser(UserAccountValueObject lockUser) {
    this.lockUser = lockUser;
  }

  @Override
  public Map<String, Object> getDatabaseContent() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    LinkedHashMap<String, Object> aspireContent = new LinkedHashMap<String, Object>();
    
    if(hasTranscription) {
      aspireContent.put("@lastModified", TimestampUtils.getUtcString(getAnnotationTS()));
      aspireContent.put("@pageNumber", getPageNum());
    }
    
    aspireContent.put("isLocked", (getIsLocked() ? "true" : "false"));
    
    if(hasTranscription) {    
      aspireContent.put("@accountId", getAccountId());
      aspireContent.put("@userName", user.getUserName());
      aspireContent.put("@fullName", user.getFullName());
      aspireContent.put("@displayFullName", user.getDisplayFullName() ? "true"
          : "false");
      
      
      // Authoritative (true/false)
      aspireContent.put("@isAuthoritative", (user.isNaraStaff() ? "true" : "false"));
  
      // Version number
      aspireContent.put("@version", getSavedVersionNumber());
    
    }
    
    
    if (isLocked) {
      LinkedHashMap<String, Object> lockInfo = new LinkedHashMap<String, Object>();
      lockInfo.put("@id", lockUser.getUserName());
      lockInfo.put("@fullName", lockUser.getFullName());
      lockInfo.put("@displayFullName", lockUser.getDisplayFullName() ? "true"
          : "false");
      lockInfo.put("@isNaraStaff", lockUser.isNaraStaff() ? "true"
          : "false");
      lockInfo.put("@when",
          TimestampUtils.getUtcString(getLockTS()));
      aspireContent.put("lockedBy", lockInfo);
    }
    
    if (hasTranscription) {
      // Users
      LinkedHashMap<String, Object> usersInfo = new LinkedHashMap<String, Object>();
      usersInfo
          .put("@total",
              (previousTranscriptions != null ? previousTranscriptions.size()
                  : 0));

      // Process users
      ArrayList<LinkedHashMap<String, Object>> userList = new ArrayList<LinkedHashMap<String, Object>>();

      if (previousTranscriptions != null) {
        for (TranscriptionValueObject previousTranscription : previousTranscriptions) {
          LinkedHashMap<String, Object> userInfo = new LinkedHashMap<String, Object>();

          userInfo.put("@id", previousTranscription.getUser().getUserName());
          userInfo.put("@fullName", previousTranscription.getUser().getFullName());
          userInfo.put("@displayFullName", previousTranscription.getUser().getDisplayFullName() ? "true"
              : "false");
          userInfo.put("@isNaraStaff", previousTranscription.getUser().isNaraStaff());
          userInfo.put("@lastModified", TimestampUtils
              .getUtcString(previousTranscription.getAnnotationTS()));
          userList.add(userInfo);

        }
      }

      usersInfo.put("user", userList);

      aspireContent.put("users", usersInfo);

      // Text
      aspireContent.put("text", getAnnotation());
    }
    
    return aspireContent;
  }

}
