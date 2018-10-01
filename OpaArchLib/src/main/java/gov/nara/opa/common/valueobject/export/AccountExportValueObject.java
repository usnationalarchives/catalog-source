package gov.nara.opa.common.valueobject.export;

import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;
import gov.nara.opa.common.services.docstransforms.Constants;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class AccountExportValueObject extends AbstractWebEntityValueObject
		implements AccountExportValueObjectConstants {

	private Integer exportId;
	private Integer accountId;
	private String exportType;
	private String exportName;
	private Boolean includeThumbnails = false;
	private Boolean includeComments = false;
	private Boolean includeContent = false;
	private Boolean includeTags = false;
	private Boolean includeTranscriptions = false;
	private Boolean includeTranslations = false;
	private Boolean includeMetadata = true;
	private Boolean includeEmbeddedThumbnails = false;
	private Boolean bulkExport = false;
	private String exportFormat;
	/**
	 * actualExportFormat is used by csv export which 
	 * makes uses the json export first and converts it to csv.
	 */
	private String actualExportFormat = "";  // used for csv export
	private String listName;
	private String url;
	private AccountExportStatusEnum requestStatus;
	private String errorMessage;
	private String processingHint;
	private Timestamp requestTs;
	private Timestamp completedTs;
	private Timestamp lastActionTs;
	private ArrayList<String> bulkExportContent;
	private Long fileSize;
	private Timestamp expiresTs;

	private Integer serverInstanceId;
	private Long springJobExecutionId;
	private AtomicInteger recordsWritten = new AtomicInteger(0);

	private String sort;
	private String apiType;

	private Integer rows;
	private Integer offset;
	private String cursorMark;
	private Integer cursorMarkOffset;
	private Integer totalErrors;
	private Integer totalSkipped;
	private Integer totalRecordsToBeProcessed;
	private Integer totalRecordsProcessed;

	private Map<String, String[]> queryParameters = new ConcurrentHashMap<String, String[]>();

	private boolean includeOpaResponseWrapper = false;
	LinkedHashMap<String, Object> apiRequestParams;
	private String printingFormat = Constants.PRINTING_RECORD_LINE;

	private int callerType = CALLER_TYPE_EXPORT;

	private boolean defaultResultFieldsSet = false;

	private ValidationError error;

	private File fileToWriteTo;

	public File getFileToWriteTo() {
		return fileToWriteTo;
	}

	public void setFileToWriteTo(File fileToWriteTo) {
		this.fileToWriteTo = fileToWriteTo;
	}

	public ValidationError getError() {
		return error;
	}

	public void setError(ValidationError error) {
		this.error = error;
	}

	@Override
	public Map<String, Object> getDatabaseContent() {
		Map<String, Object> databaseContent = new LinkedHashMap<String, Object>();
		databaseContent.put(EXPORT_ID_DB, getExportId());
		databaseContent.put(ACCOUNT_ID_DB, getAccountId());
		databaseContent.put(EXPORT_TYPE_DB, getExportType());
		databaseContent.put(EXPORT_NAME_DB, getExportName());
		databaseContent.put(LIST_NAME_DB, getListName());
		databaseContent.put(BULK_EXPORT_DB, getBulkExport());
		databaseContent.put(INCLUDE_EMBEDDED_THUMBNAILS_DB,
				getIncludeEmbeddedThumbnails());
		databaseContent.put(INCLUDE_THUMBNAILS_DB, getIncludeThumbnails());
		databaseContent.put(INCLUDE_COMMENTS_DB, getIncludeComments());
		databaseContent.put(INCLUDE_METADATA_DB, getIncludeMetadata());
		databaseContent.put(INCLUDE_CONTENT_DB, getIncludeContent());
		databaseContent.put(INCLUDE_TAGS_DB, getIncludeTags());
		databaseContent.put(INCLUDE_TRANSCRIPTIONS_DB,
				getIncludeTranscriptions());
		databaseContent.put(INCLUDE_TRANSLATIONS_DB, getIncludeTranslations());
		databaseContent.put(EXPORT_FORMAT_DB, getExportFormat());
		databaseContent.put(API_TYPE_DB, getApiType());
		databaseContent.put(URL_DB, getUrl());
		databaseContent.put(REQUEST_STATUS_DB, getRequestStatus().toString());
		databaseContent.put(ERROR_MESSAGE_DB, getErrorMessage());
		databaseContent.put(PROCESSING_HINT_DB, getProcessingHint());
		databaseContent.put(REQUEST_TS_DB, getRequestTs());
		databaseContent.put(COMPLETED_TS_DB, getCompletedTs());
		databaseContent.put(LAST_ACTION_TS_DB, getLastActionTs());
		databaseContent.put(QUERY_PARAMETERS_DB, AccountExportValueObjectHelper
				.toJsonString(getQueryParameters()));
		databaseContent.put(BULK_EXPORT_CONTENT_DB,
				AccountExportValueObjectHelper
						.toJsonString(getBulkExportContent()));
		databaseContent.put(SERVER_INSTANCE_ID_DB, getServerInstanceId());
		databaseContent.put(SPRING_JOB_EXECUTION_ID_DB,
				getSpringJobExecutionId());
		databaseContent.put(SORT_DB, getSort());
		databaseContent.put(ROWS_DB, getRows());
		databaseContent.put(OFFSET_DB, getOffset());
		databaseContent
				.put(TOTAL_RECS_PROCESSED_DB, getTotalRecordsProcessed());
		databaseContent.put(TOTAL_ERRORS_DB, getTotalErrors());
		databaseContent.put(TOTAL_SKIPPED_DB, getTotalSkipped());
		databaseContent.put(TOTAL_RECS_TO_BE_PROCESSED_DB,
				getTotalRecordsToBeProcessed());
		databaseContent.put(FILE_SIZE_DB, getFileSize());
		databaseContent.put(EXPIRES_TS_DB, getExpiresTs());
		return databaseContent;
	}

	@Override
	public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
		LinkedHashMap<String, Object> aspireContent = new LinkedHashMap<String, Object>();
		aspireContent.put(EXPORT_ID_ASP, getExportId());
		aspireContent.put(EXPORT_NAME_ASP, getExportName() + " - "
				+ getExportId());
		aspireContent.put(EXPORT_STATUS_ASP, getRequestStatus());
		if (getExportFormat() != null) {
			aspireContent.put(EXPORT_FORMAT_ASP, getExportFormat()
					.toUpperCase());
		}

		DateTime dt = new DateTime(getRequestTs().getTime());
		String dtString = dt.toDateTime(DateTimeZone.UTC).toString();
		aspireContent.put(EXPORT_REQUEST_TS_ASP, dtString);

		dt = new DateTime(getExpiresTs().getTime());
		dtString = dt.toDateTime(DateTimeZone.UTC).toString();
		aspireContent.put(EXPORT_BULK_EXPIRES_ASP, dtString);

		aspireContent.put(EXPORT_DOWNLOAD_URL_ASP,
				AccountExportValueObjectHelper.getDownloadUrl(getExportId(),
						getAccountId(),
						AbstractRequestParameters.INTERNAL_API_TYPE, getUrl()));
		aspireContent.put(EXPORT_PERCENTAGE_COMPLETE_ASP,
				AccountExportValueObjectHelper.getPercentageComplete(
						getTotalRecordsToBeProcessed(),
						getTotalRecordsProcessed()));
		aspireContent.put(EXPORT_BULK_EXPORT_ASP, getBulkExport());
		aspireContent.put(EXPORT_FILE_SIZE_ASP, getFileSize());
		aspireContent.put(EXPORT_TOTAL_PROCESSED_RECORDS_ASP,
				getTotalRecordsProcessed());
		if (getErrorMessage() != null) {
			aspireContent.put(EXPORT_ERROR_MESSAGE_ASP, getErrorMessage());
		}
		return aspireContent;

	}

	public Integer getExportId() {
		return exportId;
	}

	public void setExportId(Integer exportId) {
		this.exportId = exportId;
	}

	public Integer getAccountId() {
		return accountId;
	}

	public void setAccountId(Integer accountId) {
		this.accountId = accountId;
	}

	public String getExportType() {
		return exportType;
	}

	public void setExportType(String exportType) {
		this.exportType = exportType;
	}

	public String getExportName() {
		return exportName;
	}

	public void setExportName(String name) {
		this.exportName = name;
	}

	public String getListName() {
		return listName;
	}

	public void setListName(String listName) {
		this.listName = listName;
	}

	public Boolean getIncludeThumbnails() {
		return includeThumbnails;
	}

	public void setIncludeThumbnails(Boolean includeThumbnails) {
		this.includeThumbnails = includeThumbnails;
	}

	public Boolean getIncludeComments() {
		return includeComments;
	}

	public void setIncludeComments(Boolean includeComments) {
		this.includeComments = includeComments;
	}

	public Boolean getIncludeContent() {
		return includeContent;
	}

	public void setIncludeContent(Boolean includeContent) {
		this.includeContent = includeContent;
	}

	public Boolean getIncludeTags() {
		return includeTags;
	}

	public void setIncludeTags(Boolean includeTags) {
		this.includeTags = includeTags;
	}

	public Boolean getIncludeTranscriptions() {
		return includeTranscriptions;
	}

	public void setIncludeTranscriptions(Boolean includeTranscriptions) {
		this.includeTranscriptions = includeTranscriptions;
	}

	public Boolean getIncludeTranslations() {
		return includeTranslations;
	}

	public void setIncludeTranslations(Boolean includeTranslations) {
		this.includeTranslations = includeTranslations;
	}

	public String getExportFormat() {
		return exportFormat;
	}

	public void setExportFormat(String exportFormat) {
		this.exportFormat = exportFormat;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public AccountExportStatusEnum getRequestStatus() {
		return requestStatus;
	}

	public void setRequestStatus(AccountExportStatusEnum requestStatus) {
		this.requestStatus = requestStatus;
	}

	public String getProcessingHint() {
		return processingHint;
	}

	public void setProcessingHint(String processingHint) {
		this.processingHint = processingHint;
	}

	public Timestamp getRequestTs() {
		return requestTs;
	}

	public void setRequestTs(Timestamp requestTs) {
		this.requestTs = requestTs;
	}

	public Timestamp getCompletedTs() {
		return completedTs;
	}

	public void setCompletedTs(Timestamp completedTs) {
		this.completedTs = completedTs;
	}

	public Timestamp getLastActionTs() {
		return lastActionTs;
	}

	public void setLastActionTs(Timestamp lastActionTs) {
		this.lastActionTs = lastActionTs;
	}

	public Integer getServerInstanceId() {
		return serverInstanceId;
	}

	public void setServerInstanceId(Integer serverInstanceId) {
		this.serverInstanceId = serverInstanceId;
	}

	public Long getSpringJobExecutionId() {
		return springJobExecutionId;
	}

	public void setSpringJobExecutionId(Long springJobExecutionId) {
		this.springJobExecutionId = springJobExecutionId;
	}

	public Boolean getIncludeMetadata() {
		return includeMetadata;
	}

	public void setIncludeMetadata(Boolean includeMetadata) {
		this.includeMetadata = includeMetadata;
	}

	public Boolean getIncludeEmbeddedThumbnails() {
		return includeEmbeddedThumbnails;
	}

	public void setIncludeEmbeddedThumbnails(Boolean includeEmbeddedThumbnails) {
		this.includeEmbeddedThumbnails = includeEmbeddedThumbnails;
	}

	public Boolean getBulkExport() {
		return bulkExport;
	}

	public void setBulkExport(Boolean bulkExport) {
		this.bulkExport = bulkExport;
	}

	public Map<String, String[]> getQueryParameters() {
		return queryParameters;
	}

	public void setQueryParameters(Map<String, String[]> queryParameters) {
		this.queryParameters = queryParameters;
	}

	public Integer getRows() {
		return rows;
	}

	public void setRows(Integer rows) {
		this.rows = rows;
	}

	public Integer getOffset() {
		return offset;
	}

	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	public String getCursorMark() {
		return cursorMark;
	}

	public void setCursorMark(String cursorMark) {
		this.cursorMark = cursorMark;
	}

	public Integer getCursorMarkOffset() {
		return cursorMarkOffset;
	}

	public void setCursorMarkOffset(Integer cursorMarkOffset) {
		this.cursorMarkOffset = cursorMarkOffset;
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

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public ArrayList<String> getBulkExportContent() {
		return bulkExportContent;
	}

	public void setBulkExportContent(ArrayList<String> bulkExportContent) {
		this.bulkExportContent = bulkExportContent;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public Long getFileSize() {
		return fileSize;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	public Timestamp getExpiresTs() {
		return expiresTs;
	}

	public void setExpiresTs(Timestamp expiresTs) {
		this.expiresTs = expiresTs;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof AccountExportValueObject)) {
			return false;
		}
		return this.getExportId().equals(
				((AccountExportValueObject) o).getExportId());
	}

	public String[] getExportIds() {
		if (queryParameters != null) {
			return queryParameters
					.get(AccountExportValueObject.EXPORT_IDS_HTTP_PARAM_NAME);
		}
		return null;
	}

	public AtomicInteger getRecordsWritten() {
		return recordsWritten;
	}

	public void setRecordsWritten(AtomicInteger recorsWritten) {
		this.recordsWritten = recorsWritten;
	}

	public String getApiType() {
		return apiType;
	}

	public void setApiType(String apiType) {
		this.apiType = apiType;
	}

	public Integer getTotalErrors() {
		return totalErrors;
	}

	public void setTotalErrors(Integer totalErrors) {
		this.totalErrors = totalErrors;
	}

	public Integer getTotalSkipped() {
		return totalSkipped;
	}

	public void setTotalSkipped(Integer totalSkipped) {
		this.totalSkipped = totalSkipped;
	}

	public boolean writeExportFile() {
		if (CALLER_TYPE_PRINT == getCallerType()
				|| CALLER_TYPE_SEARCH == getCallerType()) {
			return false;
		}
		return true;
	}

	public boolean extractValuesNeeded() {
		if (AbstractRequestParameters.INTERNAL_API_TYPE.equals(getApiType())) {
			return true;
		}
		if (Constants.EXPORT_FORMAT_JSON.equals(getExportFormat())
				&& Constants.EXPORT_FORMAT_CSV.equalsIgnoreCase(getActualExportFormat())) {
			return true;
		}
		if (Constants.EXPORT_FORMAT_JSON.equals(getExportFormat())
				|| Constants.EXPORT_FORMAT_XML.equals(getExportFormat())) {
			return false;
		}
		return true;
	}

	public String getNonBulkExportFileName() {
		return String.format("nara-non-bulk-export-%1$d.%2$s", getExportId(),
				getExportFormat());
	}

	public boolean getIncludeOpaResponseWrapper() {
		return includeOpaResponseWrapper;
	}

	public void setIncludeOpaResponseWrapper(boolean includeOpaResponseWrapper) {
		this.includeOpaResponseWrapper = includeOpaResponseWrapper;
	}

	public LinkedHashMap<String, Object> getApiRequestParams() {
		return apiRequestParams;
	}

	public void setApiRequestParams(
			LinkedHashMap<String, Object> apiRequestParams) {
		this.apiRequestParams = apiRequestParams;
	}

	public String getPrintingFormat() {
		return printingFormat;
	}

	public void setPrintingFormat(String printingFormat) {
		this.printingFormat = printingFormat;
	}

	public boolean isPrettyPrint() {
		boolean prettyPrint = getPrintingFormat().equals(
				Constants.PRINTING_FORMAT_PRETTY_TRUE) ? true : false;
		return prettyPrint;
	}

	public int getCallerType() {
		return callerType;
	}

	public void setCallerType(int callerType) {
		this.callerType = callerType;
	}

	public boolean isDefaultResultFieldsSet() {
		return defaultResultFieldsSet;
	}

	public void setDefaultResultFieldsSet(boolean defaultResultFieldsSet) {
		this.defaultResultFieldsSet = defaultResultFieldsSet;
	}
	/**
	 * @return the actualExportFormat
	 */
	public String getActualExportFormat() {
		return actualExportFormat;
	}

	/**
	 * @param actualExportFormat the actualExportFormat to set
	 */
	public void setActualExportFormat(String actualExportFormat) {
		this.actualExportFormat = actualExportFormat;
	}
	public AccountExportValueObject clone(){
		AccountExportValueObject a=new AccountExportValueObject();
		a.exportId=exportId;
		a.accountId=accountId;
		a.exportType=exportType;
		a.exportName=exportName;
		a.includeThumbnails=includeThumbnails;
		a.includeComments=includeComments;
		a.includeContent=includeContent;
		a.includeTags=includeTags;
		a.includeTranscriptions=includeTranscriptions;
		a.includeTranslations=includeTranslations;
		a.includeMetadata=includeMetadata;
		a.includeEmbeddedThumbnails=includeEmbeddedThumbnails;
		a.bulkExport=bulkExport;
		a.exportFormat=exportFormat;
		a.actualExportFormat=actualExportFormat;
		a.listName=listName;
		a.url=url;
		a.requestStatus=requestStatus;
		a.errorMessage=errorMessage;
		a.processingHint=processingHint;
		a.requestTs=requestTs;
		a.completedTs=completedTs;
		a.lastActionTs=lastActionTs;
		a.bulkExportContent=bulkExportContent;
		a.fileSize=fileSize;
		a.expiresTs=expiresTs;
		a.serverInstanceId=serverInstanceId;
		a.springJobExecutionId=springJobExecutionId;
		a.recordsWritten=recordsWritten;
		a.sort=sort;
		a.apiType=apiType;
		a.rows=rows;
		a.offset=offset;
		a.totalErrors=totalErrors;
		a.totalSkipped=totalSkipped;
		a.totalRecordsToBeProcessed=totalRecordsToBeProcessed;
		a.totalRecordsProcessed=totalRecordsProcessed;
		return a;
	}

}
