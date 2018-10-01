package gov.nara.opa.api.validation.administrator;

import gov.nara.opa.api.dataaccess.user.UserAccountDao;
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.api.validation.OpaApiAbstractValidator;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorMessageConstants;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;

import java.util.LinkedHashSet;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Validator used for validating Administrator register
 * account requests
 * 
 */
@Component
public class AdministratorRegisterAccountValidator extends
    OpaApiAbstractValidator {

  @Autowired
  private UserAccountDao administratorUserAccountDao;
  
  @Autowired
  private ConfigurationService configService;


  private static final LinkedHashSet<String> orderedValidatedItemCodes = new LinkedHashSet<String>();

  static {
    orderedValidatedItemCodes.add("userName");
    orderedValidatedItemCodes.add("password");
    orderedValidatedItemCodes.add("userType");
  }

  @Override
  protected void performCustomValidation(ValidationResult validationResult,
      HttpServletRequest request) {
    if(validateCustomRedirectionParams(validationResult)) {
      validateFullNameForNaraStaff(validationResult, request);
    }
  }

  @Override
  protected LinkedHashSet<String> getOrderedValidatedItemCodes() {
    return orderedValidatedItemCodes;
  }
  
  
  /**
   * Validate that if either returnUrl or returnText are provided, the other one is provided as well.
   * @param validationResult
   */
  private boolean validateCustomRedirectionParams(ValidationResult validationResult) {
    AdministratorRegisterAccountRequestParameters params = (AdministratorRegisterAccountRequestParameters)validationResult
        .getValidatedRequest();
    
    String returnUrl = params.getReturnUrl();
    String returnText = params.getReturnText();
    
    //Url is provided but text isn't
    if((!StringUtils.isNullOrEmtpy(returnUrl) && StringUtils.isNullOrEmtpy(returnText)) 
      || (!StringUtils.isNullOrEmtpy(returnText) && StringUtils.isNullOrEmtpy(returnUrl))) {
      ValidationError error = new ValidationError();
      error.setErrorCode(ErrorCodeConstants.MISSING_VALUE);
      error.setErrorMessage(ErrorConstants.MISSING_REDIRECTION_VALUE);
      validationResult.addCustomValidationError(error);
      return false;
    }
    
    return true;
  }
  
  
  private boolean validateFullNameForNaraStaff(ValidationResult validationResult,
      HttpServletRequest request) {
    AdministratorRegisterAccountRequestParameters params = (AdministratorRegisterAccountRequestParameters)validationResult
        .getValidatedRequest();
    
    //Validate full name is not empty when user is nara staff
    if(params.getEmail().toLowerCase()
        .endsWith(configService.getConfig().getNaraEmail()) && StringUtils.isNullOrEmtpy(params.getFullName()) ) {
      ValidationError error = new ValidationError();
      error.setErrorCode(ArchitectureErrorCodeConstants.MISSING_PARAMETER);
      error.setErrorMessage(String.format(ArchitectureErrorMessageConstants.NOT_NULL_AND_NOT_EMPTY, "Full Name"));
      validationResult.addCustomValidationError(error);
      return false;
    }
    
    return true;
  }
  
}
