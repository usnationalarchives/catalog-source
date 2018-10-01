package gov.nara.opa.api.services.impl.export;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.nara.opa.api.security.OPAAuthenticationProvider;
import gov.nara.opa.api.services.export.AccountExportService;
import gov.nara.opa.api.services.export.NonBulkExportService;
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.services.user.lists.ViewUserListService;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.user.lists.UserList;
import gov.nara.opa.api.user.lists.UserListItem;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.api.validation.export.CreateAccountExportValidator;
import gov.nara.opa.api.validation.search.SolrParamsValidator;
import gov.nara.opa.api.valueobject.user.lists.UserListItemValueObject;
import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.SolrUtils;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorMessageConstants;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.dataaccess.export.AccountExportDao;
import gov.nara.opa.common.services.SingletonServices;
import gov.nara.opa.common.services.docstransforms.Constants;
import gov.nara.opa.common.services.export.AccountExportDbProxyService;
import gov.nara.opa.common.services.solr.SolrGateway;
import gov.nara.opa.common.storage.OpaStorage;
import gov.nara.opa.common.storage.OpaStorageFactory;
import gov.nara.opa.common.storage.StorageUtils;
import gov.nara.opa.common.validation.export.CreateAccountExportRequestParameters;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.common.valueobject.export.AccountExportValueObjectConstants;
import gov.nara.opa.common.valueobject.export.AccountExportValueObjectHelper;

/**
 * improved error handling in createAccountExport and initiateRequest.
 * On caught exception, the validation error and error id are created. The
 * exception is then logged with the error id. The validation error should be handled
 * properly by the controller.
 */
@Component
public class AccountExportServiceImpl implements AccountExportService, AccountExportValueObjectConstants, Constants {

	private static int MAX_EXPORTS_DELETED = 2000;

	public static final String ANONYMOUS_USER_NAME = "Anonymous";
	@Autowired
	AccountExportDao accountExportDao;

	@Autowired
	AccountExportValueObjectHelper accountExportHelper;

	@Autowired
	AccountExportDbProxyService accountExportDaoDbProxy;

	@Autowired
	SolrGateway solrGateway;

	@Autowired
	NonBulkExportService nonBulkExportService;

	@Autowired
	ConfigurationService configurationService;

	@Autowired
	SolrParamsValidator solrParamsValidator;

	@Autowired
	ViewUserListService viewUserListService;

	@Autowired
	OpaStorageFactory opaStorageFactory;

	@Autowired
	StorageUtils storageUtils;

	@Value("${export.maxTimeForNonBulkExportRequestMillis}")
	Integer maxTimeForNonBulkExportRequest;

	@Value("${export.maxFileSizeForNonBulkExportsInBytes}")
	Integer maxFileSizeForNonBulkExports;

	@Value("${export.waitBetweenStatusChecksMillis}")
	Integer waitBetweenExportStatusChecksMillis;

	public static final String OPA_IDS_SOLR_PARAMETER_NAME = "opaIds";

	private static OpaLogger logger = OpaLogger.getLogger(AccountExportServiceImpl.class);

	@Value("${export.output.location}")
	String exportOutputLocation;

	@Value("${s3ExportsLocation}")
	String s3ExportsLocation;

	@Override
	public AccountExportValueObject createAccountExport(Integer accountId, ValidationResult validationResult,
			HttpServletResponse response, AccountExportValueObject accountExport, Map<String, String[]> queryParameters,
			HttpServletRequest request) {
		int searchTimeout = configurationService.getConfig().getSearchRunTime();

		initiateRequest(accountExport, accountId, validationResult, queryParameters, searchTimeout);

		if (!validationResult.isValid()) {
			return null;
		}
		Integer exportId = accountExport.getExportId();
		logger.trace(String.format("Created export request with id:%1$d and param count:%2$d", exportId,
				queryParameters.size()));
		logger.trace(queryParameters.toString());

		if (!accountExport.getBulkExport()) {
			try {
				nonBulkExportService.executeExport(accountExport, response, request);
			} catch (IOException e) {
				throw new OpaRuntimeException(e);
			} catch (TimeoutException e) {
				String errorId = UUID.randomUUID().toString();
				logger.error("Timeout exception. error Id="+errorId, e);
				ValidationError error = new ValidationError();
				error.setErrorCode(ErrorCodeConstants.SEARCH_ENGINE_TIMEOUT);
				error.setErrorMessage(
						"Search server timed out - Please narrow your search or consider using the cursor-based approach. "+"Exception="+e+", Error Id="+errorId);
				validationResult.addCustomValidationError(error);
				validationResult.setHttpStatus(HttpStatus.BAD_REQUEST);
				return null;
			}
		} else {
			accountExport = accountExportDao.selectById(exportId);
		}

		logger.trace("Completed export request with id " + accountExport.getExportId());

		return accountExport;
	}

	@Override
	public AccountExportValueObject createAccountExport(CreateAccountExportRequestParameters requestParameters,
			Integer accountId, ValidationResult validationResult, HttpServletResponse response,
			HttpServletRequest request) {
		String userName = accountId == null ? ANONYMOUS_USER_NAME
				: OPAAuthenticationProvider.getUserNameForLoggedInUser();
		AccountExportValueObject accountExport = accountExportHelper.createAccountExportForInsert(requestParameters,
				accountId, userName, requestParameters.getQueryParameters(),
				configurationService.getConfig().getBulkExpDays());
		return createAccountExport(accountId, validationResult, response, accountExport,
				requestParameters.getQueryParameters(), request);
	}

	protected AccountExportValueObject initiateRequest(AccountExportValueObject accountExport, Integer accountId,
			ValidationResult validationResult, Map<String, String[]> queryParameters, int searchTimeout) {

		if (!solrParamsValidator.validate(validationResult, accountExport.getQueryParameters())) {
			return accountExport;
		}
		populateExportIdsFromList(accountExport, validationResult);
		int totalNumberOfRecsToBeProcessed = -1;
		try {
			totalNumberOfRecsToBeProcessed = getTotalNumberOfRecsToBeProcessed(queryParameters, accountExport,
					validationResult, searchTimeout);
			logger.trace(String.format(
					"------------------- AccountExportServiceImpl... line 182 totalNumberOfRecsToBeProcessed: %s",
					totalNumberOfRecsToBeProcessed));
		} catch (TimeoutException tex) {
			String errorId = UUID.randomUUID().toString();
			logger.error("Timeout exception. error Id="+errorId, tex);
			ValidationError error = new ValidationError();
			error.setErrorCode(ErrorCodeConstants.SEARCH_ENGINE_TIMEOUT);
			error.setErrorMessage(
					"Search server timed out - Please narrow your search or consider using the cursor-based approach. "+"Exception="+tex+", Error Id="+errorId);
			validationResult.addCustomValidationError(error);
			validationResult.setHttpStatus(HttpStatus.BAD_REQUEST);
			return null;
		} catch (Exception ex) {
			String errorId = UUID.randomUUID().toString();
			logger.error("Error executing solr query. error Id="+errorId, ex);
			// create a default message
			String message="";
			if(ex instanceof SolrServerException){
				message+="Solr threw an exception: ";
			}else{
				message+="an exception was thrown: ";
			}
			message+=",Exception="+ex+" exception message="+ex.getMessage()+", errorId="+errorId;
			ValidationError error = new ValidationError();
			error.setErrorCode(ErrorCodeConstants.SEARCH_ENGINE_ERROR);
			// set the default message
			error.setErrorMessage(message);
			// if applicable, set the message to something else
			if (ex.getCause() != null && ex.getCause().getCause() instanceof SolrServerException) {
				error.setErrorMessage(ArchitectureErrorMessageConstants.SEARCH_SERVER_UNAVAILABLE+" ,"+message);
			} else {
				if (ex.getMessage().matches("OutOfMemory")) {
					error.setErrorMessage(ArchitectureErrorMessageConstants.SEARCH_SERVER_OUT_OF_MEMORY+" ,"+message);
				} else {
					error.setErrorMessage(ArchitectureErrorMessageConstants.SEARCH_SERVER_INTERNAL_ERROR+" ,"+message);
				}
			}

			validationResult.addCustomValidationError(error);
			validationResult.setHttpStatus(HttpStatus.BAD_REQUEST);
			return null;
		}

		// Fail if no records to be processed and it's a bulk export or print
		if (totalNumberOfRecsToBeProcessed == 0
				&& (accountExport.getBulkExport() || accountExport.getExportFormat().equals(EXPORT_FORMAT_PRINT))) {
			ValidationError error = new ValidationError();
			if (accountExport.getExportFormat().equals(EXPORT_FORMAT_PRINT)) {
				error.setErrorCode(ErrorCodeConstants.PRINT_RESULTS_NOT_FOUND);
			} else if (accountExport.getCallerType() == CALLER_TYPE_SEARCH) {
				error.setErrorCode(ErrorCodeConstants.SEARCH_RESULTS_NOT_FOUND);
			} else {
				error.setErrorCode(ErrorCodeConstants.EXPORT_RESULTS_NOT_FOUND);
			}
			error.setErrorMessage(ErrorConstants.RESULTS_NOT_FOUND);
			validationResult.addCustomValidationError(error);
			validationResult.setHttpStatus(HttpStatus.NOT_FOUND);
			return null;
		}

		accountExport.setTotalRecordsToBeProcessed(totalNumberOfRecsToBeProcessed);
		if (CALLER_TYPE_EXPORT == accountExport.getCallerType()) {
			logger.debug("calling database. accountExportDaoDbProxy.create(accountExport)");
			if (accountExport.getBulkExport() && accountExport.getExportFormat().equals(EXPORT_FORMAT_CSV)
					&& accountExport.getApiType().equals(PUBLIC_API_PATH)) {
				accountExport.setApiType(INTERNAL_API_PATH);
				accountExportDaoDbProxy.create(accountExport);
				accountExport.setApiType(PUBLIC_API_PATH);
			} else {
				accountExportDaoDbProxy.create(accountExport);
			}
		} else {
			accountExport.setExportId(ThreadLocalRandom.current().nextInt(1000000000, 2000000000));
		}
		return accountExport;
	}

	@SuppressWarnings("unchecked")
	private int getTotalNumberOfRecsToBeProcessed(Map<String, String[]> queryParameters,
			AccountExportValueObject accountExport, ValidationResult validationResult, int searchTimeout)
			throws InterruptedException, ExecutionException, TimeoutException {
		prepareQueryParameters(queryParameters, accountExport, validationResult);

		QueryResponse response = null;
		SolrDocumentList results = null;
		int numFound = 0;
		int totalNoOfEstimatedRecords = 0;

		Object listItemsObject = validationResult.getContextObjects()
				.get(CreateAccountExportValidator.LIST_ITEMS_OBJECT);
		List<UserListItemValueObject> listItems = null;
		if (listItemsObject != null) {
			listItems = (List<UserListItemValueObject>) listItemsObject;
		}

		if (listItems != null && accountExport.getListName() != null && queryParameters.containsKey("opaIds")) {
			String[] tempOpaIds = queryParameters.get("opaIds").clone();
			List<String> items = Arrays.asList(tempOpaIds[0].split("[\\s,]+"));
			numFound = items.size();
			totalNoOfEstimatedRecords = numFound;
		} else {
			// response = solrGateway.solrQuery(queryParameters);
			logger.trace("START - Getting record count");
			if (accountExport.getCallerType() == CALLER_TYPE_SEARCH) {
				response = SolrUtils.doSearchWithTimeout(solrGateway, queryParameters, searchTimeout);
			} else {
				response = SolrUtils.doSearchWithTimeout(solrGateway, queryParameters,
						configurationService.getConfig().getMaxNonBulkTimer());
			}
			logger.trace("END - Getting record count");

			// remove these 2 parameters as we they were written termporarily to
			// do the
			// Solr query
			queryParameters.remove(CreateAccountExportRequestParameters.ROWS_HTTP_PARAM_NAME);
			queryParameters.remove(CreateAccountExportRequestParameters.OFFSET_HTTP_PARAM_NAME);
			results = response.getResults();
			if (results == null) {
				return 0;
			}

			// if the value in "nextCursorMark" == "cursorMark", we're beyond
			// the total set
			if (response.getNextCursorMark() != null && queryParameters.containsKey("cursorMark")
					&& response.getNextCursorMark().equals(queryParameters.get("cursorMark")[0])) {
				return 0;
			}

			numFound = (int) results.getNumFound();
			int offset = accountExport.getCursorMarkOffset() != null ? accountExport.getCursorMarkOffset()
					: accountExport.getOffset() != null ? accountExport.getOffset() : 0;
			if (offset != 0) {
				// numFound could be negative if offset is bigger
				numFound = Math.max(0, numFound - offset);
			}

			Object estimatedRecords = validationResult.getContextObjects()
					.get(CreateAccountExportValidator.TOTAL_NO_OF_ESTIMATED_RECORDS);
			if (estimatedRecords == null) {
				throw new OpaRuntimeException("The estimated number of records does not seem to have been calculated");
			}

			totalNoOfEstimatedRecords = ((Integer) estimatedRecords).intValue();
		}

		if (totalNoOfEstimatedRecords != -1 && totalNoOfEstimatedRecords <= numFound) {
			return totalNoOfEstimatedRecords;
		}

		return numFound;
	}

	private void prepareQueryParameters(Map<String, String[]> queryParameters, AccountExportValueObject accountExport,
			ValidationResult validationResult) {
		String[] exportIds = queryParameters.get(CreateAccountExportRequestParameters.EXPORT_IDS_HTTP_PARAM_NAME);
		boolean isExport = false;
		if (exportIds != null) {
			queryParameters.remove(CreateAccountExportRequestParameters.EXPORT_IDS_HTTP_PARAM_NAME);
			queryParameters.put(OPA_IDS_SOLR_PARAMETER_NAME, exportIds);
		}
		if (accountExport.getOffset() != null) {
			queryParameters.put(CreateAccountExportRequestParameters.OFFSET_HTTP_PARAM_NAME,
					new String[] { accountExport.getOffset().toString() });
		}

		queryParameters.put(CreateAccountExportRequestParameters.ROWS_HTTP_PARAM_NAME, new String[] { "1" });

		if (!isExport
				&& gov.nara.opa.api.system.Constants.PUBLIC_API_PATH.equalsIgnoreCase(accountExport.getApiType())) {
			processAPIParameters(queryParameters, validationResult, accountExport);
		}
	}

	private void processExistsParameter(Map<String, String[]> queryParameters, ValidationResult validationResult) {
		if (queryParameters.containsKey("exists")) {
			String tempFQ = "";
			if (queryParameters.containsKey("fq")) {
				tempFQ = queryParameters.get("fq")[0] + " AND (";
			} else {
				tempFQ = "(";
			}
			String[] existFields = queryParameters.get("exists");

			StringBuffer sb = new StringBuffer();
			boolean isFirst = true;
			for (String field : existFields) {
				if (!SingletonServices.DAS_WHITE_LIST.contains(field)) {
					logger.error("Exists Parameter Processing - Parameter Error for :" + field);
					ValidationError error = new ValidationError();
					error.setErrorCode("INVALID_PARAMETER");
					error.setErrorMessage("The http request parameter - " + field
							+ " - is not allowed. Please remove this parameter and resubmit the request.");
					validationResult.addCustomValidationError(error);
					validationResult.setHttpStatus(HttpStatus.BAD_REQUEST);
					return;
				}

				String[] fieldParts = field.split("\\.");
				String lastPart = fieldParts[fieldParts.length - 1];

				sb.append(" ");
				if (!isFirst) {
					sb.append(" AND ");
				}
				sb.append("ex_" + lastPart);
				sb.append(":");
				sb.append("\"");
				sb.append("{" + field + "}");
				sb.append("\"");
				isFirst = false;
			}
			tempFQ = tempFQ + sb.toString() + ")";
			queryParameters.put("fq", new String[] { tempFQ });
			queryParameters.remove("exists");
		}
	}

	private void processNotExistParameter(Map<String, String[]> queryParameters, ValidationResult validationResult) {
		if (queryParameters.containsKey("not_exist")) {
			String tempFQ = "";
			if (queryParameters.containsKey("fq")) {
				tempFQ = queryParameters.get("fq")[0] + " AND NOT(";
			} else {
				tempFQ = "NOT(";
			}
			String[] notExistFields = queryParameters.get("not_exist");

			StringBuffer sb = new StringBuffer();
			boolean isFirst = true;
			for (String field : notExistFields) {
				if (!SingletonServices.DAS_WHITE_LIST.contains(field)) {
					logger.error("Not Exist Parameter Processing - Parameter Error for :" + field);
					ValidationError error = new ValidationError();
					error.setErrorCode("INVALID_PARAMETER");
					error.setErrorMessage("The http request parameter - " + field
							+ " - is not allowed. Please remove this parameter and resubmit the request.");
					validationResult.addCustomValidationError(error);
					validationResult.setHttpStatus(HttpStatus.BAD_REQUEST);
					return;
				}
				String[] fieldParts = field.split("\\.");
				String lastPart = fieldParts[fieldParts.length - 1];

				sb.append(" ");
				if (!isFirst) {
					sb.append(" OR ");
				}
				sb.append("ex_" + lastPart);
				sb.append(":");
				sb.append("\"");
				sb.append("{" + field + "}");
				sb.append("\"");
				isFirst = false;
			}
			tempFQ = tempFQ + sb.toString() + ")";
			queryParameters.put("fq", new String[] { tempFQ });
			queryParameters.remove("not_exist");
		}
	}

	private void processExactMatch(Map<String, String[]> queryParameters, ValidationResult validationResult) {
		String tempFQ = "";
		if (queryParameters.containsKey("fq")) {
			tempFQ = queryParameters.get("fq")[0];
		}
		for (String key : queryParameters.keySet()) {
			String cleanKey = key;
			boolean isNotField = false;
			boolean isExactMatch = false;
			if (key.endsWith("_not")) {
				cleanKey = key.replace("_not", "");
				isNotField = true;
				isExactMatch = true;
			}
			if (key.endsWith("_is")) {
				cleanKey = key.replace("_is", "");
				isExactMatch = true;
			}
			if (!SingletonServices.SOLR_FIELDS_WHITE_LIST.contains(cleanKey)
					&& !SingletonServices.DAS_WHITE_LIST.contains(cleanKey)
					&& !SingletonServices.SOLR_FIELDS_INTERNAL_WHITE_LIST.contains(cleanKey)) {
				logger.error("Exact Match Parameter Processing - Parameter Error for :" + cleanKey);
				ValidationError error = new ValidationError();
				error.setErrorCode("INVALID_PARAMETER");
				error.setErrorMessage("The http request parameter - " + cleanKey
						+ " - is not allowed. Please remove this parameter and resubmit the request.");
				validationResult.addCustomValidationError(error);
				validationResult.setHttpStatus(HttpStatus.BAD_REQUEST);
				return;
			}
			if (isExactMatch && SingletonServices.DAS_WHITE_LIST.contains(cleanKey)) {
				String field = cleanKey;
				String[] fieldParts = field.split("\\.");
				String lastPart = fieldParts[fieldParts.length - 1];

				String value = queryParameters.get(key)[0];

				if (value.endsWith("\"") && value.startsWith("\"")) {
					value = value.substring(1, value.length() - 1);
				}

				tempFQ += " " + (isNotField ? "-" : "+") + "ex_" + lastPart + ":\"{" + field + "}" + value + "\"";
				queryParameters.remove(key);
			}
		}
		if (!tempFQ.isEmpty()) {
			queryParameters.put("fq", new String[] { tempFQ });
		}
	}

	private void processCursorMarkParameter(Map<String, String[]> queryParameters, ValidationResult validationResult,
			AccountExportValueObject accountExport) {
		if (queryParameters.containsKey("cursorMark")) {
			// get the offset from the cursorMark value <cursormark>-<offset>
			String cursorMark = queryParameters.get("cursorMark")[0];
			String[] cursorMarkParts = cursorMark.split("-");
			if (cursorMarkParts.length == 2) {
				accountExport.setCursorMarkOffset(Integer.parseInt(cursorMarkParts[1]));
				queryParameters.put("cursorMark", new String[] { cursorMarkParts[0] });
			}
			cursorMark = queryParameters.get("cursorMark")[0];
			// check for escaped "/" characters ("\/") and replace...
			if (cursorMark.toLowerCase().contains("\\")) {
				cursorMark = cursorMark.replaceAll("\\\\", "");
				queryParameters.put("cursorMark", new String[] { cursorMark });
			}
			// handle "+" characters
			cursorMark = queryParameters.get("cursorMark")[0];
			cursorMark = cursorMark.replaceAll("\\s{1}", "+");
			queryParameters.put("cursorMark", new String[] { cursorMark });

			accountExport.setCursorMark(cursorMark);

			// if they are using cursorMark, they must sort with opaId
			if (queryParameters.containsKey(CreateAccountExportRequestParameters.SORT_PARAM_NAME)) {
				String[] sorts = queryParameters.get(CreateAccountExportRequestParameters.SORT_PARAM_NAME);
				// first see if it's already there
				StringBuffer sb = new StringBuffer();
				for (String one : sorts) {
					sb.append(one);
					sb.append(",");
				}

				if (!sb.toString().contains("opaId")) {
					sb.append("opaId asc");
				} else {
					// remove the comma
					sb.deleteCharAt(sb.length() - 1);
				}

				String modifiedSort = sb.toString();
				queryParameters.remove(CreateAccountExportRequestParameters.SORT_PARAM_NAME);
				queryParameters.put(CreateAccountExportRequestParameters.SORT_PARAM_NAME,
						new String[] { modifiedSort });

			} else {
				queryParameters.put(CreateAccountExportRequestParameters.SORT_PARAM_NAME,
						new String[] { "score desc, ingestedDateTime desc, opaId asc" });
			}
		}

	}

	private void processAPIParameters(Map<String, String[]> queryParameters, ValidationResult validationResult,
			AccountExportValueObject accountExport) {
		processExactMatch(queryParameters, validationResult);
		processCursorMarkParameter(queryParameters, validationResult, accountExport);
		processExistsParameter(queryParameters, validationResult);
		processNotExistParameter(queryParameters, validationResult);
	}

	private void populateExportIdsFromList(AccountExportValueObject accountExport, ValidationResult validationResult) {
		Object listItemsObject = validationResult.getContextObjects()
				.get(CreateAccountExportValidator.LIST_ITEMS_OBJECT);

		int accountId = accountExport.getAccountId() == null ? -1 : accountExport.getAccountId().intValue();
		if (accountId > 0) {
			UserList list = viewUserListService.getList(accountExport.getListName(), accountId);
			int offset = accountExport.getOffset() == null ? 0 : accountExport.getOffset().intValue();
			int rows = accountExport.getRows() == null ? 0 : accountExport.getRows().intValue();

			if (list != null) {
				List<UserListItem> itemsFullList = viewUserListService.getListItems(list.getListId());
				List<UserListItem> itemsList = new ArrayList<UserListItem>();
				if (itemsFullList != null && itemsFullList.size() > 0) {

					for (int x = offset; x < Math.min((offset + rows), itemsFullList.size()); x++) {
						itemsList.add(itemsFullList.get(x));
					}
				}

				if (listItemsObject != null && itemsList.size() > 0) {
					populateExportIdsFromList(accountExport, itemsList);
				}
			}
		}
	}

	private void populateExportIdsFromList(AccountExportValueObject accountExport, List<UserListItem> listItems) {
		StringBuffer exportIds = new StringBuffer();
		int i = 0;
		int size = listItems.size();
		for (UserListItem listItem : listItems) {
			exportIds.append(listItem.getOpaId());
			i++;
			if (i < size) {
				exportIds.append(",");
			}
		}
		accountExport.getQueryParameters().put(CreateAccountExportRequestParameters.EXPORT_IDS_HTTP_PARAM_NAME,
				new String[] { exportIds.toString() });
	}

	@Override
	public int removeExpiredExports() {
		List<AccountExportValueObject> expiredExports = accountExportDao.getExpiredExports();
		logger.debug(String.format("Total exports to delete: %1$d", expiredExports.size()));
		if (expiredExports != null && expiredExports.size() > 0) {
			int i = 0;
			for (AccountExportValueObject export : expiredExports) {
				if (i < MAX_EXPORTS_DELETED) {
					deleteAccountExport(export);
					i++;
				} else {
					break;
				}
			}

			int total = Math.min(expiredExports.size(), MAX_EXPORTS_DELETED);
			logger.debug(String.format("Total exports deleted: %1$d", total));
			return total;
		}
		return 0;

	}

	@Override
	@Transactional
	public void deleteAccountExport(AccountExportValueObject accountExport) {
		// Attempt to delete the export in Storage
		// Delete export file
		String exportId = accountExport.getExportId().toString();

		String finalUrl = s3ExportsLocation + accountExportHelper.getExportFileRelativeLocation(accountExport, false); // exportId
																														// +
																														// "/"
																														// +
																														// fileName;
		String finalCompressedUrl = s3ExportsLocation
				+ accountExportHelper.getExportFileRelativeLocation(accountExport, true); // exportId
																							// +
																							// "/"
																							// +
																							// compressedFileName;

		OpaStorage storage = opaStorageFactory.createOpaStorage();
		try {
			if (storage.exists(finalUrl)) {
				storage.deleteFiles(finalUrl);
			} else if (storage.exists(finalCompressedUrl)) {
				storage.deleteFiles(finalCompressedUrl);
			} else {
				logger.debug(String.format("Export '%1$s' not found in storage", exportId));
			}
			accountExportDao.deleteAccountExport(accountExport.getExportId());
			logger.debug(String.format("Export '%1$s' has been deleted", exportId));
		} catch (Exception e) {
			throw new OpaRuntimeException(e);
		}

	}

}
