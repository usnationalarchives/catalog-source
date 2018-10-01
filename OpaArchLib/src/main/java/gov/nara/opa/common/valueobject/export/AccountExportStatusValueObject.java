package gov.nara.opa.common.valueobject.export;

import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The main reason this class was added is to allow for
 * faster retrieval of account export status information w/o the need to
 * populate all fields from the table on every single getAccountStatus
 * request
 */
public class AccountExportStatusValueObject extends
    AbstractWebEntityValueObject implements AccountExportValueObjectConstants {

  private AccountExportStatusEnum requestStatus;
  private String errorMessage;
  private Integer totalRecordsToBeProcessed;
  private Integer totalRecordsProcessed;
  private String url;
  private Integer accountId;
  private Integer exportId;

  public AccountExportStatusEnum getRequestStatus() {
    return requestStatus;
  }

  public void setRequestStatus(AccountExportStatusEnum requestStatus) {
    this.requestStatus = requestStatus;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public Integer getTotalRecordsToBeProcessed() {
    return totalRecordsToBeProcessed;
  }

  public void setTotalRecordsToBeProcessed(Integer totalRecordsToBeProcessed) {
    this.totalRecordsToBeProcessed = totalRecordsToBeProcessed;
  }

  public Integer getTotalRecordsProcessed() {
    return totalRecordsProcessed;
  }

  public void setTotalRecordsProcessed(Integer totalRecordsProcessed) {
    this.totalRecordsProcessed = totalRecordsProcessed;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  @Override
  public Map<String, Object> getDatabaseContent() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    LinkedHashMap<String, Object> aspireContent = new LinkedHashMap<String, Object>();

    aspireContent.put(EXPORT_ID_ASP, getExportId());
    aspireContent.put(EXPORT_STATUS_ASP, getRequestStatus());

    aspireContent.put(EXPORT_DOWNLOAD_URL_ASP, AccountExportValueObjectHelper
        .getDownloadUrl(getExportId(), getAccountId(),
            AbstractRequestParameters.INTERNAL_API_TYPE, getUrl()));
    aspireContent.put(EXPORT_PERCENTAGE_COMPLETE_ASP,
        AccountExportValueObjectHelper.getPercentageComplete(
            getTotalRecordsToBeProcessed(), getTotalRecordsProcessed()));

    if (getErrorMessage() != null) {
      aspireContent.put(EXPORT_ERROR_MESSAGE_ASP, getErrorMessage());
    }

    return aspireContent;
  }

  public Integer getAccountId() {
    return accountId;
  }

  public void setAccountId(Integer accountId) {
    this.accountId = accountId;
  }

  public Integer getExportId() {
    return exportId;
  }

  public void setExportId(Integer exportId) {
    this.exportId = exportId;
  }
}
