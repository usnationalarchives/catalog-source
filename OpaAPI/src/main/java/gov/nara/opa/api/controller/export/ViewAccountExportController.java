package gov.nara.opa.api.controller.export;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import gov.nara.opa.api.security.OPAAuthenticationProvider;
import gov.nara.opa.api.services.impl.export.NonBulkExportServiceImpl;
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.utils.BrowserUtils;
import gov.nara.opa.api.validation.export.GetAccountExportStatusRequestParameters;
import gov.nara.opa.api.validation.export.GetAccountExportStatusValidator;
import gov.nara.opa.api.validation.export.GetAccountExportsRequestParameters;
import gov.nara.opa.api.validation.export.GetAccountExportsSummaryStatusValidator;
import gov.nara.opa.api.validation.export.GetAccountExportsValidator;
import gov.nara.opa.api.validation.export.GetExportFileRequestParameters;
import gov.nara.opa.api.validation.export.GetExportFileValidator;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.TimestampUtils;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.architecture.web.valueobject.SimpleWebEntityValueObject;
import gov.nara.opa.common.dataaccess.export.AccountExportDao;
import gov.nara.opa.common.storage.OpaStorageFactory;
import gov.nara.opa.common.storage.StorageUtils;
import gov.nara.opa.common.valueobject.export.AccountExportCollectionValueObject;
import gov.nara.opa.common.valueobject.export.AccountExportStatusValueObject;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;

@Controller
public class ViewAccountExportController extends AbstractBaseController {

    private static OpaLogger logger = OpaLogger.getLogger(ViewAccountExportController.class);
	
	@Value("${export.output.location}")
	private String exportOutputLocation;

	@Value("${export.nonbulk.output.location}")
	private String exportNonBulkOutputLocation;

	@Autowired
	private GetExportFileValidator exportFileValidator;

	@Autowired
	private GetAccountExportsValidator accountExportsValidator;

	@Autowired
	private GetAccountExportsSummaryStatusValidator accountExportsSummaryStatusValidator;

	@Autowired
	private GetAccountExportStatusValidator accountExportStatusValidator;

	@Autowired
	private StorageUtils storageUtils;
	
	@Autowired
	private OpaStorageFactory opaStorageFactory;
	
	@Autowired
	private ConfigurationService configurationService;
	
	@Autowired
	private AccountExportDao accountExportDao;
	
	public static final String GET_EXPORT_FILE_ACTION = "getExportFile";

	public static final String GET_ACCOUNT_EXPORTS_ACTION = "getAccountExports";

	public static final String GET_ACCOUNT_EXPORTS_SUMMARY_STATUS_ACTION = "getAccountExportsSummaryStatus";

	public static final String GET_ACCOUNT_EXPORT_STATUS_ACTION = "getAccountExportStatus";

	public static final String ACCOUNT_EXPORTS_ENTITY_NAME = "accountExports";

	public static final String ACCOUNT_EXPORT_SUMMARY_ENTITY_NAME = "accountExportSummary";

	public static final String ACCOUNT_EXPORTS_STATUS_SUMMARY_ENTITY_NAME = "accountExportsStatusSummary";

	@Value(value = "${useS3Storage}")
	boolean useS3Storage;

	@RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM
			+ "/exports/auth/files/{exportId}" }, method = RequestMethod.GET)
	public void getExportFileAuth(
			@Valid GetExportFileRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		getExportFile(requestParameters, bindingResult, request, response,
				OPAAuthenticationProvider.getAccountIdForLoggedInUser(),
				exportOutputLocation);
	}

	@RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM
			+ "/exports/auth/nbfiles/{exportId}" }, method = RequestMethod.GET)
	public void getExportNonBulkFileAuth(
			@Valid GetExportFileRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		getExportFile(requestParameters, bindingResult, request, response,
				OPAAuthenticationProvider.getAccountIdForLoggedInUser(),
				exportNonBulkOutputLocation);
	}

	@RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM
			+ "/exports/noauth/files/{exportId}" }, method = RequestMethod.GET)
	public void getExportFileNoauth(
			@Valid GetExportFileRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		getExportFile(requestParameters, bindingResult, request, response,
				null, exportOutputLocation);
	}

	@RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM
			+ "/exports/noauth/nbfiles/{exportId}" }, method = RequestMethod.GET)
	public void getExportNonBulkFileNoauth(
			@Valid GetExportFileRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		getExportFile(requestParameters, bindingResult, request, response,
				null, exportNonBulkOutputLocation);
	}

	public void getExportFile(
			@Valid GetExportFileRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request,
			HttpServletResponse response, Integer accountId,
			String exportFolderLocation) throws IOException {

		ValidationResult validationResult = exportFileValidator.validate(
				bindingResult, request);
		if (!validationResult.isValid()) {
			ResponseEntity<String> errorResponse = createErrorResponseEntity(
					validationResult, request, GET_EXPORT_FILE_ACTION);
			response.setStatus(errorResponse.getStatusCode().value());
			response.getOutputStream()
					.write(errorResponse.getBody().getBytes());
			return;
		}

		AccountExportValueObject accountExport = (AccountExportValueObject) validationResult
				.getContextObjects().get(GetExportFileValidator.ACCOUNT_EXPORT);
		String exportFilePath = accountExport.getUrl();
		Path exportLocationPath = Paths.get(exportFilePath);
		String fileName = exportLocationPath.getFileName().toString();
		exportFilePath = storageUtils.getExportPath(exportFolderLocation,
				exportFilePath);

		NonBulkExportServiceImpl.setContentTypeAndDisposition(response,
				accountExport, fileName, BrowserUtils.isBrowserIE8(request));
		
		//Update expiration date
		int expirationDays = configurationService.getConfig().getBulkExpDays();
		accountExport.setExpiresTs(TimestampUtils.addDaysToTs(TimestampUtils.getUtcTimestamp(), expirationDays));
		accountExportDao.update(accountExport);
		logger.info(String.format("Updated expiration date for export: %1$d", accountExport.getExportId()));

		writeFileContentToResponseOldStyle(exportFilePath, response, request, opaStorageFactory.createOpaStorage());
	}

	@RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM
			+ "/exports/auth" }, method = RequestMethod.GET)
	public ResponseEntity<String> getAccountExports(
			@Valid GetAccountExportsRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		ValidationResult validationResult = accountExportsValidator.validate(
				bindingResult, request,
				AbstractRequestParameters.INTERNAL_API_TYPE);
		if (!validationResult.isValid()) {
			return createErrorResponseEntity(validationResult, request,
					GET_ACCOUNT_EXPORTS_ACTION);
		}

		@SuppressWarnings("unchecked")
		List<AccountExportValueObject> accountExports = (List<AccountExportValueObject>) validationResult
				.getContextObjects().get(
						GetAccountExportsValidator.ACCOUNT_EXPORTS);
		AccountExportCollectionValueObject responseEntity = new AccountExportCollectionValueObject(
				accountExports);
		return createSuccessResponseEntity(ACCOUNT_EXPORTS_ENTITY_NAME,
				requestParameters, responseEntity, request,
				GET_ACCOUNT_EXPORTS_ACTION);
	}

	@RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM
			+ "/exports/auth/status/{bulkExportId}", }, method = RequestMethod.GET)
	public ResponseEntity<String> getAccountExportStatus(
			@Valid GetAccountExportStatusRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		return getAccountExportStatusInternal(requestParameters, bindingResult,
				request, response, requestParameters.getApiType());
	}

	@RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM }, method = RequestMethod.GET, params = "bulkExportId")
	public ResponseEntity<String> getAccountExportStatusPublic(
			@Valid GetAccountExportStatusRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		return getAccountExportStatusInternal(requestParameters, bindingResult,
				request, response, AbstractRequestParameters.PUBLIC_API_TYPE);
	}

	private ResponseEntity<String> getAccountExportStatusInternal(
			@Valid GetAccountExportStatusRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request,
			HttpServletResponse response, String apiType) throws IOException {
		ValidationResult validationResult = accountExportStatusValidator
				.validate(bindingResult, request, apiType);
		if (!validationResult.isValid()) {
			return createErrorResponseEntity(validationResult, request,
					GET_ACCOUNT_EXPORT_STATUS_ACTION);
		}

		AccountExportStatusValueObject accountExportStatus = (AccountExportStatusValueObject) validationResult
				.getContextObjects().get(
						GetAccountExportStatusValidator.ACCOUNT_EXPORT);
		accountExportStatus.setExportId(requestParameters.getBulkExportId());

		return createSuccessResponseEntity(ACCOUNT_EXPORT_SUMMARY_ENTITY_NAME,
				requestParameters, accountExportStatus, request,
				GET_ACCOUNT_EXPORT_STATUS_ACTION);
	}

	@RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM
			+ "/exports/auth/summarystatus" }, method = RequestMethod.GET)
	public ResponseEntity<String> getAccountExportsSummaryStatus(
			@Valid GetAccountExportsRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		ValidationResult validationResult = accountExportsSummaryStatusValidator
				.validate(bindingResult, request,
						AbstractRequestParameters.INTERNAL_API_TYPE);
		if (!validationResult.isValid()) {
			return createErrorResponseEntity(validationResult, request,
					GET_ACCOUNT_EXPORTS_SUMMARY_STATUS_ACTION);
		}

		@SuppressWarnings("unchecked")
		LinkedHashMap<String, Object> summaryStatus = (LinkedHashMap<String, Object>) validationResult
				.getContextObjects()
				.get(GetAccountExportsSummaryStatusValidator.ACCOUNT_EXPORTS_SUMMARY_STATUS);
		if (!summaryStatus.containsKey("Complete")) {
			summaryStatus.put("Complete", 0);
		}

		if (!summaryStatus.containsKey("Pending")) {
			summaryStatus.put("Pending", 0);
		}
		SimpleWebEntityValueObject responseEntity = new SimpleWebEntityValueObject(
				summaryStatus);
		return createSuccessResponseEntity(
				ACCOUNT_EXPORTS_STATUS_SUMMARY_ENTITY_NAME, requestParameters,
				responseEntity, request,
				GET_ACCOUNT_EXPORTS_SUMMARY_STATUS_ACTION);
	}
}
