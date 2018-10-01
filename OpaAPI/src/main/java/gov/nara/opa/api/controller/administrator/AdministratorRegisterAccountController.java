package gov.nara.opa.api.controller.administrator;

import gov.nara.opa.api.services.administrator.AdministratorRegisterAccountService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.validation.administrator.AdministratorRegisterAccountRequestParameters;
import gov.nara.opa.api.validation.administrator.AdministratorRegisterAccountValidator;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Provides the entry point for CRUD user operations to be
 * performed by an administrator
 */
@Controller
public class AdministratorRegisterAccountController extends
    AbstractBaseController {

  @Autowired
  private AdministratorRegisterAccountValidator registerAccountValidator;

  @Autowired
  private AdministratorRegisterAccountService registerAccountService;

  public static final String REGISTER_ACCOUNT_ACTION = "registerUserAccount";

  static OpaLogger log = OpaLogger
      .getLogger(AdministratorRegisterAccountController.class);


  public ResponseEntity<String> publicRegisterUserAccount(
      @Valid AdministratorRegisterAccountRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) {
    
    ValidationResult validationResult = registerAccountValidator.validate(
        bindingResult, request, Constants.PUBLIC_API_PATH);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          REGISTER_ACCOUNT_ACTION);
    }

    UserAccountValueObject userAccount = registerAccountService
        .registerAccount(requestParameters);

    return createSuccessResponseEntity("user", requestParameters, userAccount,
        request, REGISTER_ACCOUNT_ACTION);
  }
  
  
  /**
   * Method called when an administrator registers an account
   * 
   * @param requestParameters
   *          The URL parameters will be bound into property is of the
   *          RegisterAccountRequest POJO. See that class' javadoc for details
   *          on the parameters.
   * @param bindingResult
   *          The result of spring binding which creates the object and
   *          validates the properties of RegisterAccountRequest POJO
   * @param request
   *          The HttpRequest
   * @return
   */
  @RequestMapping(value = {
      "/"+ Constants.PUBLIC_API_PATH +"/" + Constants.API_VERS_NUM + "/administrator", 
      "/"+ Constants.INTERNAL_API_PATH +"/" + Constants.API_VERS_NUM + "/administrator/accounts/register" }, 
      method = RequestMethod.POST)
  public ResponseEntity<String> registerUserAccount(
      @Valid AdministratorRegisterAccountRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) {

    ValidationResult validationResult = registerAccountValidator.validate(
        bindingResult, request);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          REGISTER_ACCOUNT_ACTION);
    }

    UserAccountValueObject userAccount = registerAccountService
        .registerAccount(requestParameters);

    return createSuccessResponseEntity("user", requestParameters, userAccount,
        request, REGISTER_ACCOUNT_ACTION);

  }

}
