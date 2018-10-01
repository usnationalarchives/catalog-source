package gov.nara.opa.api.controller.administrator;

import gov.nara.opa.api.services.administrator.AdministratorModifyAccountService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.validation.administrator.AdministratorDeactivateAccountRequestParameters;
import gov.nara.opa.api.validation.administrator.AdministratorDeactivateAccountValidator;
import gov.nara.opa.api.validation.administrator.AdministratorModifyAccountRequestParameters;
import gov.nara.opa.api.validation.administrator.AdministratorModifyAccountValidator;
import gov.nara.opa.api.validation.administrator.AdministratorReactivateAccountRequestParameters;
import gov.nara.opa.api.validation.administrator.AdministratorReactivateAccountValidator;
import gov.nara.opa.api.validation.administrator.AdministratorRequestPasswordResetRequestParameters;
import gov.nara.opa.api.validation.administrator.AdministratorRequestPasswordResetValidator;
import gov.nara.opa.api.validation.common.accounts.CommonUserAccountValidator;
import gov.nara.opa.api.validation.user.accounts.ForgotUserNameValidator;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class AdministratorModifyAccountController extends
    AbstractBaseController {

  @Autowired
  AdministratorDeactivateAccountValidator deactivateAccountValidator;

  @Autowired
  AdministratorReactivateAccountValidator reactivateAccountValidator;

  @Autowired
  AdministratorModifyAccountValidator modifyAccountValidator;

  @Autowired
  AdministratorRequestPasswordResetValidator requestPasswordResetValidator;

  @Autowired
  AdministratorModifyAccountService modifyAccountService;

  public static final String MODIFY_ACCOUNT_ACTION = "modifyUserAccount";

  public static final String DEACTIVATE_ACCOUNT_ACTION = "deactivateUserAccount";

  public static final String REACTIVATE_ACCOUNT_ACTION = "reactivateUserAccount";

  public static final String REQUEST_PASSWORD_RESET_ACTION = "requestPasswordReset";

  public static final String USER_PARENT_ENTITY_NAME = "user";

  @RequestMapping(value = "/{apiType}/" + Constants.API_VERS_NUM
      + "/administrator/accounts/modify/{userName:.+}", method = RequestMethod.POST)
  public ResponseEntity<String> modifyUserAccount(
      @Valid AdministratorModifyAccountRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) {

    return performModify(requestParameters, bindingResult, request, null);
  }
  
  @RequestMapping(value = "/"+ Constants.PUBLIC_API_PATH +"/" + Constants.API_VERS_NUM
      + "/users/{userName:.+}/accounts", method = RequestMethod.PUT, params = {"action=adminChange","email"})
  public ResponseEntity<String> publicModifyUserAccount(
      @Valid AdministratorModifyAccountRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) {

    return performModify(requestParameters, bindingResult, request, requestParameters.getAction());
  }
  
  private ResponseEntity<String> performModify(
      @Valid AdministratorModifyAccountRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request, String action) {
    ValidationResult validationResult = modifyAccountValidator.validate(
        bindingResult, request);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          (action == null ? MODIFY_ACCOUNT_ACTION : action) );
    }

    UserAccountValueObject userAccount = (UserAccountValueObject) validationResult
        .getContextObjects().get(
            CommonUserAccountValidator.USER_ACCOUNT_OBJECT_KEY);
    modifyAccountService.update(userAccount, requestParameters);
    return createSuccessResponseEntity("user", requestParameters, userAccount,
        request, (action == null ? MODIFY_ACCOUNT_ACTION : action));
  }
  
  
  

  @RequestMapping(value = "/{apiType}/" + Constants.API_VERS_NUM
      + "/administrator/accounts/deactivate/{userName:.+}", method = RequestMethod.PUT)
  public ResponseEntity<String> deactivateUserAccount(
      @Valid AdministratorDeactivateAccountRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) {

    return performDeactivation(requestParameters, bindingResult, request, null);
  }
  
  @RequestMapping(value = "/" + Constants.PUBLIC_API_PATH + "/" + Constants.API_VERS_NUM
      + "/users/{userName:.+}/accounts", method = RequestMethod.PUT, params = {"action=adminChange","status=inactive"})
  public ResponseEntity<String> publicDeactivateUserAccount(
      @Valid AdministratorDeactivateAccountRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) {

    return performDeactivation(requestParameters, bindingResult, request, requestParameters.getAction());
  }
  
  private ResponseEntity<String> performDeactivation(
      @Valid AdministratorDeactivateAccountRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request, String action) {
   
    ValidationResult validationResult = deactivateAccountValidator.validate(
        bindingResult, request);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          (action == null ? DEACTIVATE_ACCOUNT_ACTION : action));
    }

    UserAccountValueObject userAccount = (UserAccountValueObject) validationResult
        .getContextObjects().get(
            CommonUserAccountValidator.USER_ACCOUNT_OBJECT_KEY);
    modifyAccountService.deactivate(userAccount, requestParameters);
    return createSuccessResponseEntity("user", requestParameters, userAccount,
        request, (action == null ? DEACTIVATE_ACCOUNT_ACTION : action));
    
  }
  
  

  
  @RequestMapping(value = "/{apiType}/" + Constants.API_VERS_NUM
      + "/administrator/accounts/reactivate/{userName:.+}", method = RequestMethod.PUT)
  public ResponseEntity<String> reactivateUserAccount(
      @Valid AdministratorReactivateAccountRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) {

    return performReactivation(requestParameters, bindingResult, request, null);
  }

  @RequestMapping(value = "/" + Constants.PUBLIC_API_PATH + "/" + Constants.API_VERS_NUM
      + "/users/{userName:.+}/accounts", method = RequestMethod.PUT, params = {"action=reactivate"})
  public ResponseEntity<String> publicReactivateUserAccount(
      @Valid AdministratorReactivateAccountRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) {

    return performReactivation(requestParameters, bindingResult, request, requestParameters.getAction());
  }
  
  private ResponseEntity<String> performReactivation(
      @Valid AdministratorReactivateAccountRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request, String action) {
    
    ValidationResult validationResult = reactivateAccountValidator.validate(
        bindingResult, request);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          (action == null ? REACTIVATE_ACCOUNT_ACTION : action));
    }

    UserAccountValueObject userAccount = (UserAccountValueObject) validationResult
        .getContextObjects().get(
            CommonUserAccountValidator.USER_ACCOUNT_OBJECT_KEY);
    modifyAccountService.reactivate(userAccount, requestParameters);
    return createSuccessResponseEntity("user", requestParameters, userAccount,
        request, (action == null ? REACTIVATE_ACCOUNT_ACTION : action));
    
  }
  
  
  
  
  @RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM
      + "/administrator/accounts/requestpasswordreset/{userName:.+}" }, method = RequestMethod.POST)
  public ResponseEntity<String> requestPasswordReset(
      @Valid AdministratorRequestPasswordResetRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) {

    return performRequest(requestParameters, bindingResult, request, null);
  }

  @RequestMapping(value = { "/" + Constants.PUBLIC_API_PATH + "/" + Constants.API_VERS_NUM
      + "/users/{userName:.+}/accounts" }, method = RequestMethod.PUT, params = {"action=resetPassword"})
  public ResponseEntity<String> publicRequestPasswordReset(
      @Valid AdministratorRequestPasswordResetRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) {

    return performRequest(requestParameters, bindingResult, request, requestParameters.getAction());
  }
  
  private ResponseEntity<String> performRequest(
      @Valid AdministratorRequestPasswordResetRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request, String action) {
    
    ValidationResult validationResult = requestPasswordResetValidator.validate(
        bindingResult, request);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          (action == null ? REQUEST_PASSWORD_RESET_ACTION : action));
    }

    UserAccountValueObject userAccount = (UserAccountValueObject) validationResult
        .getContextObjects().get(
            ForgotUserNameValidator.USER_ACCOUNT_OBJECT_KEY);
    modifyAccountService.requestPasswordReset(userAccount, requestParameters);

    LinkedHashMap<String, Object> responseValues = new LinkedHashMap<String, Object>();
    responseValues.put(USER_PARENT_ENTITY_NAME, "success");

    return createSuccessResponseEntity(requestParameters, responseValues,
        request, (action == null ? REQUEST_PASSWORD_RESET_ACTION : action));
    
  }

  
}
