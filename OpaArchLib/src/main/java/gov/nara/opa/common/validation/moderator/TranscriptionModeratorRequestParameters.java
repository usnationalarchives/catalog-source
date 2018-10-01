package gov.nara.opa.common.validation.moderator;

import java.util.LinkedHashMap;

import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorMessageConstants;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;
import gov.nara.opa.architecture.web.validation.constraint.OpaSize;
import gov.nara.opa.common.validation.FieldConstraintConstants;

public class TranscriptionModeratorRequestParameters extends
    AbstractRequestParameters {

  @OpaNotNullAndNotEmpty
  private String apiType;
  
  @OpaNotNullAndNotEmpty
  @OpaSize(max = FieldConstraintConstants.MAX_NAID_SIZE_LENGTH, message = ArchitectureErrorMessageConstants.EXCEEDS_SIZE_MAX_CHAR)
  private String naId;
  
  @OpaNotNullAndNotEmpty
  @OpaSize(max = FieldConstraintConstants.MAX_OBJECTID_SIZE_LENGTH, message = ArchitectureErrorMessageConstants.EXCEEDS_SIZE_MAX_CHAR)
  private String objectId;
  
  @OpaNotNullAndNotEmpty
  private int versionNumber;
  
  @OpaNotNullAndNotEmpty
  private int reasonId;
  
  @OpaSize(max = FieldConstraintConstants.TRANSCRIPTION_NOTES_LENGTH, message = ArchitectureErrorMessageConstants.EXCEEDS_SIZE_MAX_CHAR)
  private String notes;
  
  
  public String getApiType() {
    return apiType;
  }


  public void setApiType(String apiType) {
    this.apiType = apiType;
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


  public int getVersionNumber() {
    return versionNumber;
  }


  public void setVersionNumber(int versionNumber) {
    this.versionNumber = versionNumber;
  }


  public int getReasonId() {
    return reasonId;
  }


  public void setReasonId(int reasonId) {
    this.reasonId = reasonId;
  }


  public String getNotes() {
    return notes;
  }


  public void setNotes(String notes) {
    this.notes = notes;
  }


  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    // TODO Auto-generated method stub
    return null;
  }

}
