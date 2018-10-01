package gov.nara.opa.api.validation.annotation.comments;

import gov.nara.opa.api.controller.annotation.comments.CommentController;
import gov.nara.opa.api.services.search.SingleSearchRecordRetrieval;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.api.validation.OpaApiAbstractValidator;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.architecture.exception.OpaRuntimeException;
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
public class CommentsSearchValidator extends OpaApiAbstractValidator {

	@Override
	public void performCustomValidation(ValidationResult validationResult,
			HttpServletRequest request) {
	}

	@Autowired
	SingleSearchRecordRetrieval searchRecordRetrieval;

	public CommentsCreateRequestParameters createAddBaseRequestParameters(
			HttpServletRequest request,
			CommentsSearchRequestParameters searchRequestParameters,
			ValidationResult validationResult) {

	  try {
  		SearchRecordValueObject searchRecord = searchRecordRetrieval
  				.getSearchRecord(searchRequestParameters.getQueryParameters());
  		if (searchRecord == null) {
  			addNoRecordsFoundError(validationResult);
  			return null;
  		}
  
  		CommentsCreateRequestParameters requestParameters = new CommentsCreateRequestParameters();
  		requestParameters.setApiType(searchRequestParameters.getApiType());
  		requestParameters.setFormat(searchRequestParameters.getFormat());
  		
  		String naId = searchRecord.getNaId();
  		if(StringUtils.isNullOrEmtpy(naId)) {
  			naId = searchRecord.getParentDescriptionNaId();
  		}
  		if(StringUtils.isNullOrEmtpy(naId)) {
  			throw new OpaRuntimeException(String.format("NaId is null for comment: %1$s", requestParameters.getText()));
  		}
		requestParameters.setNaId(naId);
  		
  		if (searchRecord.getResultType().equals(ResultTypesValidator.RESULT_TYPE_OBJECT) || searchRecord.getObjectId() != null) {
  			requestParameters.setObjectId(searchRecord.getObjectId());
  		}
  		requestParameters.setPretty(searchRequestParameters.isPretty());
  		requestParameters.setText(searchRequestParameters.getComment());
  		return requestParameters;
	  } catch(Exception ex) {
      ValidationUtils.setValidationError(validationResult,
          ArchitectureErrorCodeConstants.SEARCH_ERROR,
          ArchitectureErrorMessageConstants.SEARCH_SERVER_UNAVAILABLE,
          CommentController.CREATE_COMMENT_SEARCH_ACTION,
          HttpStatus.INTERNAL_SERVER_ERROR);
	    
	    return null;
	  }
	}

	private void addNoRecordsFoundError(ValidationResult validationResult) {
    ValidationUtils.setValidationError(validationResult,
        ErrorCodeConstants.SEARCH_RESULTS_NOT_FOUND,
        ErrorConstants.SEARCH_INSERT_ERROR,
        CommentController.CREATE_COMMENT_SEARCH_ACTION, HttpStatus.NOT_FOUND);
	}
}
