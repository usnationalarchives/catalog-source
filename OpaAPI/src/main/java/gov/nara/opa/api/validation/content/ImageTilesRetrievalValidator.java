package gov.nara.opa.api.validation.content;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.nara.opa.api.annotation.transcriptions.TranscriptionErrorCode;
import gov.nara.opa.api.annotation.transcriptions.TranscriptionErrorConstants;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.utils.PageNumberUtils;
import gov.nara.opa.architecture.web.validation.AbstractBaseValidator;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;

@Component
public class ImageTilesRetrievalValidator extends AbstractBaseValidator {
  
  @Autowired
  private PageNumberUtils pageNumberUtils;

  @Override
  protected void performCustomValidation(ValidationResult validationResult,
      HttpServletRequest request) {
    ImageTilesRetrievalRequestParameters requestParameters = (ImageTilesRetrievalRequestParameters)validationResult.getValidatedRequest();
    boolean isValid = validateObjectId(requestParameters, validationResult);
    if(isValid) {
      //validatePageNum(requestParameters, validationResult);
    }
    
  }
  
  private boolean validateObjectId(ImageTilesRetrievalRequestParameters requestParameters, ValidationResult validationResult) {
    String objectId = requestParameters.getObjectId();
    
    //Validate objectId greater than 0
    Integer objectIdInt = Integer.valueOf(objectId);
    if(objectIdInt == null || objectIdInt <= 0) {
      ValidationError error = new ValidationError();
      error.setErrorCode(TranscriptionErrorCode.INVALID_PARAMETER.toString());
      error.setErrorMessage(TranscriptionErrorConstants.INVALID_OBJECT_ID);
      
      validationResult.addCustomValidationError(error);
      return false;
    }
    return true;
  }
  
  private void validatePageNum(ImageTilesRetrievalRequestParameters requestParameters, ValidationResult validationResult) {
    
    //Validate page num works
    int pageNum = pageNumberUtils.getPageNumber(Constants.INTERNAL_API_PATH, requestParameters.getNaId(), requestParameters.getObjectId());
    if(pageNum <= 0) {
      ValidationError error = new ValidationError();
      error.setErrorCode(TranscriptionErrorCode.INVALID_PARAMETER.toString());
      error.setErrorMessage(TranscriptionErrorConstants.INVALID_OBJECT_ID);
      
      validationResult.addCustomValidationError(error);
    }
  }
  

}
