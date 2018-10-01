package gov.nara.opa.api.controller.export;

import gov.nara.opa.api.security.OPAAuthenticationProvider;
import gov.nara.opa.api.services.export.AccountExportService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.validation.export.CreateAccountExportValidator;
import gov.nara.opa.architecture.logging.ApiTypeLoggingEnum;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.logging.usage.UsageLogCode;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.architecture.web.valueobject.SimpleWebEntityValueObject;
import gov.nara.opa.common.validation.export.CreateAccountExportRequestParameters;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;

import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class CreateAccountExportController extends AbstractBaseController {

	public static final String EXPORT_LOG_MESSAGE = "Action=export, Format=%1$s, "
			+ "TotalRecords=%2$d, ExportOptions=%3$s";

	@Autowired
	private CreateAccountExportValidator createAccountExportValidator;

	@Autowired
	private AccountExportService createAccountExportService;

	public static final String CREATE_EXPORT_ACTION = "createExport";
	public static final String ACCOUNT_EXPORT_PARENT_ENTITY_NAME = "accountExport";
	public static final String EXPORT_FILE_PARENT_ENTITY_NAME = "exportFile";

	static OpaLogger log = OpaLogger
			.getLogger(CreateAccountExportController.class);

	@RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM }, method = RequestMethod.POST, params = "action=export")
	public ResponseEntity<String> createExportPublicApiAuth(
			@Valid CreateAccountExportRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request,
			HttpServletResponse response) {
		return createExport(requestParameters, bindingResult, request,
				OPAAuthenticationProvider.getAccountIdForLoggedInUser(),
				Constants.PUBLIC_API_PATH, response);
	}

	@RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM
			+ "/exports/auth" }, method = RequestMethod.POST)
	public ResponseEntity<String> createExportInternalApiAuth(
			@Valid CreateAccountExportRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request,
			HttpServletResponse response) {
		return createExport(requestParameters, bindingResult, request,
				OPAAuthenticationProvider.getAccountIdForLoggedInUser(),
				Constants.INTERNAL_API_PATH, response);
	}

	@RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM
			+ "/exports/noauth" }, method = RequestMethod.POST)
	public ResponseEntity<String> createExportInternalApiNonAuth(
			@Valid CreateAccountExportRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request,
			HttpServletResponse response) {
		return createExport(requestParameters, bindingResult, request, null,
				Constants.INTERNAL_API_PATH, response);
	}

	private ResponseEntity<String> createExport(
			@Valid CreateAccountExportRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request,
			Integer accountId, String apiType, HttpServletResponse response) {

		ValidationResult validationResult = null;
		if (apiType == null) {
			validationResult = createAccountExportValidator.validate(
					bindingResult, request);
		} else {
			validationResult = createAccountExportValidator.validate(
					bindingResult, request, apiType);
		}
		// createAccountExportValidator.validateAccountId(validationResult,
		// accountId);

		if (!validationResult.isValid()) {
			return createErrorResponseEntity(validationResult, request,
					CREATE_EXPORT_ACTION);
		}

		AccountExportValueObject accountExport = createAccountExportService
				.createAccountExport(requestParameters, accountId,
						validationResult, response, request);
		ValidationError executionError = accountExport.getError();
		if (executionError != null) {
			validationResult.addCustomValidationError(executionError);
		}

		if (!validationResult.isValid()) {
			return createErrorResponseEntity(validationResult, request,
					CREATE_EXPORT_ACTION);
		}

		String options = getOptionString(accountExport);

		log.usage(
				this.getClass(),
				ApiTypeLoggingEnum.toApiTypeLoggingEnum(apiType),
				UsageLogCode.EXPORT,
				String.format(EXPORT_LOG_MESSAGE,
						accountExport.getExportFormat(),
						accountExport.getTotalRecordsProcessed(), options));

		return createSuccessfulExportResponse(requestParameters, accountExport,
				request, response, accountId);
	}

	private ResponseEntity<String> createSuccessfulExportResponse(
			CreateAccountExportRequestParameters requestParameters,
			AccountExportValueObject accountExport, HttpServletRequest request,
			HttpServletResponse response, Integer accountId) {

		Integer exportId = accountExport.getExportId();
		if (accountExport.getBulkExport()) {
			LinkedHashMap<String, Object> responseObject = new LinkedHashMap<String, Object>();
			responseObject.put("bulkExportId", exportId);
			return createSuccessResponseEntity(
					ACCOUNT_EXPORT_PARENT_ENTITY_NAME, requestParameters,
					new SimpleWebEntityValueObject(responseObject), request,
					CREATE_EXPORT_ACTION);
		} else {

			if (accountExport.writeExportFile()
					&& accountExport.getApiType().equals(
							AbstractRequestParameters.INTERNAL_API_TYPE)) {
				String authPathToken = accountId == null ? "noauth" : "auth";
				String url = String.format(
						"/OpaAPI/%1$s/v1/exports/%2$s/nbfiles/%3$d",
						accountExport.getApiType(), authPathToken,
						accountExport.getExportId());

				LinkedHashMap<String, Object> responseObject = new LinkedHashMap<String, Object>();
				responseObject.put("url", url);
				return createSuccessResponseEntity(
						EXPORT_FILE_PARENT_ENTITY_NAME, requestParameters,
						new SimpleWebEntityValueObject(responseObject),
						request, CREATE_EXPORT_ACTION);
			}
		}
		return null;
	}

	private String getOptionString(AccountExportValueObject accountExport) {
		String result = "{";

		result += accountExport.getExportType();

		if (accountExport.getIncludeThumbnails()) {
			result += ",thumbnails";
		}

		if (accountExport.getIncludeTags()) {
			result += ",tags";
		}

		if (accountExport.getIncludeComments()) {
			result += ",comments";
		}

		if (accountExport.getIncludeTranscriptions()) {
			result += ",transcriptions";
		}

		if (accountExport.getIncludeTranslations()) {
			result += ",tranlations";
		}

		if (accountExport.getIncludeMetadata()) {
			result += ",metadata";
		}

		if (accountExport.getIncludeEmbeddedThumbnails()) {
			result += ",embedded_thumbnails";
		}

		if (accountExport.getIncludeContent()) {
			result += ",content";
		}

		result += "}";

		return result;
	}

}
