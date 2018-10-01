package gov.nara.opa.api.validation.annotation.tags;

import gov.nara.opa.api.controller.annotation.tags.CreateTagController;
import gov.nara.opa.api.controller.annotation.tags.DeleteTagController;
import gov.nara.opa.api.services.search.SingleSearchRecordRetrieval;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.utils.PageNumberUtils;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.api.validation.OpaApiAbstractValidator;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorMessageConstants;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.validation.search.ResultTypesValidator;
import gov.nara.opa.common.valueobject.search.SearchRecordValueObject;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class TagsSearchValidator extends OpaApiAbstractValidator {

	@Autowired
	private PageNumberUtils pageNumberUtils;

	private OpaLogger logger = OpaLogger.getLogger(TagsSearchValidator.class);

	@Override
	public void performCustomValidation(ValidationResult validationResult,
			HttpServletRequest request) {

	}

	@Autowired
	private SingleSearchRecordRetrieval searchRecordRetrieval;

	public TagsCreateRequestParameters createAddBaseRequestParameters(
			HttpServletRequest request,
			TagsSearchRequestParameters searchRequestParameters,
			ValidationResult validationResult) {

		try {
			SearchRecordValueObject searchRecord = searchRecordRetrieval
					.getSearchRecord(searchRequestParameters
							.getQueryParameters());
			if (searchRecord == null) {
				addNoRecordsFoundError(validationResult);
				return null;
			}
			TagsCreateRequestParameters requestParameters = new TagsCreateRequestParameters();
			String apiType = searchRequestParameters.getApiType();

			requestParameters.setApiType(apiType);
			requestParameters.setFormat(searchRequestParameters.getFormat());
			if (searchRecord.getResultType().equals(
					ResultTypesValidator.RESULT_TYPE_OBJECT) || StringUtils.isNullOrEmtpy(searchRecord.getNaId())) {
				logger.debug("Record is object");
				
				String naId = searchRecord.getNaId();
				if(StringUtils.isNullOrEmtpy(naId)) {
					naId = searchRecord.getParentDescriptionNaId();
				}
				String objectId = searchRecord.getObjectId();

				if(StringUtils.isNullOrEmtpy(naId)) {
					throw new OpaRuntimeException(String.format("NaId is null for tag: %1$s", requestParameters.getText()));
				}
				
				// Get page num
				int pageNum = pageNumberUtils.getPageNumber(apiType, naId,
						objectId);
				
				requestParameters.setObjectId(objectId);
				requestParameters.setNaId(naId);
				requestParameters.setPageNum(pageNum);

			} else {
				String naId = searchRecord.getNaId();
				if(StringUtils.isNullOrEmtpy(naId)) {
					logger.debug("Getting naId from parent description, non object");
					naId = searchRecord.getParentDescriptionNaId();
				}
				if(!StringUtils.isNullOrEmtpy(naId)) {				
					requestParameters.setNaId(naId);
				} else {
					throw new OpaRuntimeException(String.format("NaId is null for result type: %1$s", searchRecord.getResultType()));
				}
			}
			if (searchRecord.getResultType().equals("object")) {
				requestParameters.setObjectId(searchRecord.getObjectId());
			}
			requestParameters.setPretty(searchRequestParameters.isPretty());
			requestParameters.setText(searchRequestParameters.getTag());
			return requestParameters;

		} catch (Exception ex) {
			logger.error(ex);

			ValidationUtils
					.setValidationError(
							validationResult,
							ArchitectureErrorCodeConstants.SEARCH_ERROR,
							ArchitectureErrorMessageConstants.SEARCH_SERVER_UNAVAILABLE,
							CreateTagController.CREATE_TAG_SEARCH_ACTION,
							HttpStatus.INTERNAL_SERVER_ERROR);

			return null;
		}
	}

	private void addNoRecordsFoundError(ValidationResult validationResult) {
		ValidationUtils.setValidationError(validationResult,
				ErrorCodeConstants.SEARCH_RESULTS_NOT_FOUND,
				ErrorConstants.SEARCH_INSERT_ERROR,
				CreateTagController.CREATE_TAG_SEARCH_ACTION,
				HttpStatus.NOT_FOUND);
	}

	public TagsDeleteRequestParameters createDeleteBaseRequestParameters(
			HttpServletRequest request,
			TagsSearchRequestParameters searchRequestParameters,
			ValidationResult validationResult) {

		try {
			SearchRecordValueObject searchRecord = searchRecordRetrieval
					.getSearchRecord(searchRequestParameters
							.getQueryParameters());
			if (searchRecord == null) {
				addNoRecordsFoundError(validationResult);
				return null;
			}
			TagsDeleteRequestParameters requestParameters = new TagsDeleteRequestParameters();
			requestParameters.setApiType(searchRequestParameters.getApiType());
			requestParameters.setFormat(searchRequestParameters.getFormat());
			requestParameters.setNaId(searchRecord.getNaId());
			if (searchRecord.getResultType().equals("object")) {
				requestParameters.setObjectId(searchRecord.getObjectId());
			}
			requestParameters.setPretty(searchRequestParameters.isPretty());
			requestParameters.setText(searchRequestParameters.getTag());
			return requestParameters;
		} catch (Exception ex) {
			logger.error(ex);

			ValidationUtils
					.setValidationError(
							validationResult,
							ArchitectureErrorCodeConstants.SEARCH_ERROR,
							ArchitectureErrorMessageConstants.SEARCH_SERVER_UNAVAILABLE,
							DeleteTagController.DELETE_SEARCH_TAG_ACTION,
							HttpStatus.INTERNAL_SERVER_ERROR);

			return null;
		}
	}
}
