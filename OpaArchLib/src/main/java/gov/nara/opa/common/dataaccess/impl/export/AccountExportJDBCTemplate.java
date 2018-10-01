package gov.nara.opa.common.dataaccess.impl.export;

import gov.nara.opa.architecture.dataaccess.AbstractOpaDbJDBCTemplate;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.common.dataaccess.export.AccountExportDao;
import gov.nara.opa.common.dataaccess.utils.StoredProcedureDataAccessUtils;
import gov.nara.opa.common.valueobject.export.AccountExportStatusEnum;
import gov.nara.opa.common.valueobject.export.AccountExportStatusValueObject;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.common.valueobject.export.AccountExportValueObjectConstants;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class AccountExportJDBCTemplate extends AbstractOpaDbJDBCTemplate
		implements AccountExportDao, AccountExportValueObjectConstants {

	/**
	 * Inserts Account Export object representation into database
	 * 
	 * @param accountExport
	 *            The Account Export object to be inserted into the database
	 * 
	 */
	@Override
	public void create(AccountExportValueObject accountExport) {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("exportId", accountExport.getExportId());
		inParamMap.put("accountId", accountExport.getAccountId());
		inParamMap.put("exportName", accountExport.getExportName());
		inParamMap.put("exportType", accountExport.getExportType());
		inParamMap.put("bulkExport", accountExport.getBulkExport());
		inParamMap.put("apiType", accountExport.getApiType());
		inParamMap.put("bulkExportContent", StringUtils.convertMapToJSONString(accountExport.getBulkExportContent()));
		inParamMap.put("includeThumbnails", accountExport.getIncludeThumbnails());
		inParamMap.put("includeEmbeddedThumbnails", accountExport.getIncludeEmbeddedThumbnails());
		inParamMap.put("includeComments", accountExport.getIncludeComments());
		inParamMap.put("includeContent", accountExport.getIncludeContent());
		inParamMap.put("includeMetadata", accountExport.getIncludeMetadata());
		inParamMap.put("includeTags", accountExport.getIncludeTags());
		inParamMap.put("includeTranscriptions", accountExport.getIncludeTranscriptions());
		inParamMap.put("includeTranslations", accountExport.getIncludeTranslations());
		inParamMap.put("exportFormat", accountExport.getExportFormat());
		inParamMap.put("url", accountExport.getUrl());
		inParamMap.put("queryParameters", StringUtils.convertMapToJSONString(accountExport.getQueryParameters()));
		inParamMap.put("rows", accountExport.getRows());
		inParamMap.put("offset", accountExport.getOffset());
		inParamMap.put("listName", accountExport.getListName());
		inParamMap.put("sort", accountExport.getSort());
		inParamMap.put("requestStatus", accountExport.getRequestStatus());
		inParamMap.put("errorMessage", accountExport.getErrorMessage());
		inParamMap.put("totalRecsProcessed", accountExport.getTotalRecordsProcessed());
		inParamMap.put("totalErrors", accountExport.getTotalErrors());
		inParamMap.put("totalSkipped", accountExport.getTotalSkipped());
		inParamMap.put("totalRecsToBeProcessed", accountExport.getTotalRecordsToBeProcessed());
		inParamMap.put("fileSize", accountExport.getFileSize());
		inParamMap.put("processingHint", accountExport.getProcessingHint());
		inParamMap.put("springJobExecutionId", accountExport.getSpringJobExecutionId());
		inParamMap.put("serverInstanceId", accountExport.getServerInstanceId());
		inParamMap.put("requestTs", accountExport.getRequestTs());
		inParamMap.put("completedTs", accountExport.getCompletedTs());
		inParamMap.put("lastActionTs", accountExport.getLastActionTs());
		inParamMap.put("expiresTs", accountExport.getExpiresTs());

		int exportId = StoredProcedureDataAccessUtils.executeWithIntResult(getJdbcTemplate(), "spInsertAccountExport",
				inParamMap, "exportIdOut");
		accountExport.setExportId(new Integer(exportId));
	}

	/**
	 * Gets a list of queued exports that haven't been scheduled
	 * 
	 * @param limit
	 *            determines the total number of items to retrieve
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<AccountExportValueObject> getNewQueuedExports(Integer limit) {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("rows", limit);
		inParamMap.put("requestStatus", AccountExportStatusEnum.QUEUED.toString());

		return (List<AccountExportValueObject>) StoredProcedureDataAccessUtils.execute(getJdbcTemplate(),
				"spGetNewQueuedExports", new AccountExportValueObjectRowMapper(), inParamMap);
	}

	/**
	 * Gets an Account Export object by its Id from the database
	 * 
	 * @param exportId
	 *            export identifier to look for in the database
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public AccountExportValueObject selectById(Integer exportId) {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("exportId", exportId);
		List<AccountExportValueObject> results = (List<AccountExportValueObject>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(), "spSelectExportById", new AccountExportValueObjectRowMapper(), inParamMap);

		return (results != null && results.size() > 0 ? results.get(0) : null);
	}

	/**
	 * Gets the current status of an export
	 * 
	 * @param exportId
	 *            export identifier to look for in the database
	 * @return
	 */
	@Override
	public String getCurrentStatus(Integer exportId) {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("exportId", exportId);
		String status = (String) StoredProcedureDataAccessUtils.executeWithStringResult(getJdbcTemplate(),
				"spSelectRequestStatus", inParamMap, "requestStatus");

		return status;
	}

	/**
	 * Gets the status of an export
	 * 
	 * @param exportId
	 *            export identifier to look for in the database
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public AccountExportStatusValueObject getCurrentStatusObject(Integer exportId) {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("exportId", exportId);
		List<AccountExportStatusValueObject> results = (List<AccountExportStatusValueObject>) StoredProcedureDataAccessUtils
				.execute(getJdbcTemplate(), "spGetExportStatus", new AccountExportStatusValueObjectRowMapper(),
						inParamMap);

		return (results != null && results.size() > 0 ? results.get(0) : null);
	}

	/**
	 * Updates the account export representation in the database
	 * 
	 * @param accountExport
	 *            account export to update in database
	 * @return
	 */
	@Override
	public void update(AccountExportValueObject accountExport) {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("exportId", accountExport.getExportId());
		inParamMap.put("accountId", accountExport.getAccountId());
		inParamMap.put("exportName", accountExport.getExportName());
		inParamMap.put("exportType", accountExport.getExportType());
		inParamMap.put("bulkExport", accountExport.getBulkExport());
		inParamMap.put("apiType", accountExport.getApiType());
		inParamMap.put("bulkExportContent", StringUtils.convertMapToJSONString(accountExport.getBulkExportContent()));
		inParamMap.put("includeThumbnails", accountExport.getIncludeThumbnails());
		inParamMap.put("includeEmbeddedThumbnails", accountExport.getIncludeEmbeddedThumbnails());
		inParamMap.put("includeComments", accountExport.getIncludeComments());
		inParamMap.put("includeContent", accountExport.getIncludeContent());
		inParamMap.put("includeMetadata", accountExport.getIncludeMetadata());
		inParamMap.put("includeTags", accountExport.getIncludeTags());
		inParamMap.put("includeTranscriptions", accountExport.getIncludeTranscriptions());
		inParamMap.put("includeTranslations", accountExport.getIncludeTranslations());
		inParamMap.put("exportFormat", accountExport.getExportFormat());
		inParamMap.put("url", accountExport.getUrl());
		inParamMap.put("queryParameters", StringUtils.convertMapToJSONString(accountExport.getQueryParameters()));
		inParamMap.put("rows", accountExport.getRows());
		inParamMap.put("offset", accountExport.getOffset());
		inParamMap.put("listName", accountExport.getListName());
		inParamMap.put("sort", accountExport.getSort());
		inParamMap.put("requestStatus", accountExport.getRequestStatus());
		inParamMap.put("errorMessage", accountExport.getErrorMessage());
		inParamMap.put("totalRecsProcessed", accountExport.getTotalRecordsProcessed());
		inParamMap.put("totalSkipped", accountExport.getTotalSkipped());
		inParamMap.put("totalRecsToBeProcessed", accountExport.getTotalRecordsToBeProcessed());
		inParamMap.put("totalErrors", accountExport.getTotalErrors());
		inParamMap.put("fileSize", accountExport.getFileSize());
		inParamMap.put("processingHint", accountExport.getProcessingHint());
		inParamMap.put("springJobExecutionId", accountExport.getSpringJobExecutionId());
		inParamMap.put("serverInstanceId", accountExport.getServerInstanceId());
		inParamMap.put("requestTs", accountExport.getRequestTs());
		inParamMap.put("completedTs", accountExport.getCompletedTs());
		inParamMap.put("lastActionTs", accountExport.getLastActionTs());
		inParamMap.put("expiresTs", accountExport.getExpiresTs());

		StoredProcedureDataAccessUtils.execute(getJdbcTemplate(), "spUpdateAccountExport", inParamMap);
	}

	/**
	 * Updates request status and job execution id
	 * 
	 * @param exportId
	 *            identifier of the account export to update
	 * @param requestStatus
	 *            request status to be updated in the account export
	 * @param springJobExecutionId
	 *            spring job execution id to be updated in the account export
	 * @param lastActionTs
	 *            last action timestamp to be updated in the account export
	 * @return
	 */
	@Override
	public void updateRequestStatusAndJobExecutionId(Integer exportId, String requestStatus, Long springJobExecutionId,
			Timestamp lastActionTs) {
		if (lastActionTs == null) {
			lastActionTs = new Timestamp(new Date().getTime());
		}

		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("exportId", exportId);
		inParamMap.put("requestStatus", requestStatus);
		inParamMap.put("lastActionTs", lastActionTs);
		inParamMap.put("springJobExecutionId", springJobExecutionId);

		StoredProcedureDataAccessUtils.execute(getJdbcTemplate(), "spUpdateRequestStatusAndJobExecutionId", inParamMap);
	}

	/**
	 * Updates total number of records processed and file size of the export
	 * 
	 * @param exportId
	 *            identifier of the account export to update
	 * @param recordsProcessed
	 *            number of records processed
	 * @param fileSize
	 *            current file size of the export
	 * @param lastActionTs
	 *            last action timestamp to be updated in the account export
	 * @return
	 */
	@Override
	public void incrementRecordsProcessed(Integer exportId, Integer recordsProcessed, Long fileSize,
			Timestamp lastActionTs) {
		if (lastActionTs == null) {
			lastActionTs = new Timestamp(new Date().getTime());
		}

		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("exportId", exportId);
		inParamMap.put("totalRecsProcessed", recordsProcessed);
		inParamMap.put("fileSize", fileSize);
		inParamMap.put("lastActionTs", lastActionTs);

		StoredProcedureDataAccessUtils.execute(getJdbcTemplate(), "spUpdateRecordsProcessed", inParamMap);
	}

	/**
	 * Gets a list of the bulk exports that are associated to an account. The
	 * list is sorted by requested time
	 * 
	 * @param accountId
	 *            identifier for the account to filter exports
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<AccountExportValueObject> getExportsForAccount(Integer accountId) {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("accountId", accountId);

		return (List<AccountExportValueObject>) StoredProcedureDataAccessUtils.execute(getJdbcTemplate(),
				"spSelectExportsForAccount", new AccountExportValueObjectRowMapper(), inParamMap);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AccountExportValueObject> getExpiredExports() {
		Map<String, Object> inParamMap = new HashMap<String, Object>();

		return (List<AccountExportValueObject>) StoredProcedureDataAccessUtils.execute(getJdbcTemplate(),
				"spGetExpiredExports", new AccountExportValueObjectRowMapper(), inParamMap);
	}

	/**
	 * Gets the status summary of the bulk exports performed by an account
	 * 
	 * @param accountId
	 *            identifier for the account to look
	 * @return
	 */
	public LinkedHashMap<String, Object> getStatusSummary(Integer accountId) {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("accountId", accountId);
		List<Map<String, Object>> rows = StoredProcedureDataAccessUtils.executeWithListResults(getJdbcTemplate(),
				"spSelectStatusSummaryByAccount", inParamMap);
		LinkedHashMap<String, Object> results = new LinkedHashMap<String, Object>();
		for (Map<String, Object> row : rows) {
			results.put((String) row.get("CURRENT_STATUS"), row.get("TOTAL"));
		}
		return results;
	}

	/**
	 * Deletes an account export by its id
	 * 
	 * @param accountExportId
	 *            identifier for export to delete
	 * @return
	 */
	@Override
	public void deleteAccountExport(Integer accountExportId) {
		Map<String, Object> inParamMap = new HashMap<String, Object>();
		inParamMap.put("exportId", accountExportId);

		StoredProcedureDataAccessUtils.execute(getJdbcTemplate(), "spDeleteAccountExportById", inParamMap);
	}

}