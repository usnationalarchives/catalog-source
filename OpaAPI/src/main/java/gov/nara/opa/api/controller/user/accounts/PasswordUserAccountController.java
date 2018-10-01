package gov.nara.opa.api.controller.user.accounts;

import gov.nara.opa.api.services.user.accounts.PasswordUserAccountService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.validation.user.accounts.ForgotUserNameValidator;
import gov.nara.opa.api.validation.user.accounts.RequestPasswordResetRequestParameters;
import gov.nara.opa.api.validation.user.accounts.RequestPasswordResetValidator;
import gov.nara.opa.api.validation.user.accounts.ResetAccountPasswordRequestParameters;
import gov.nara.opa.api.validation.user.accounts.ResetAccountPasswordValidator;
import gov.nara.opa.api.validation.user.accounts.SetNewPasswordRequestParameters;
import gov.nara.opa.api.validation.user.accounts.SetNewPasswordValidator;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class PasswordUserAccountController extends AbstractBaseController {

  @Autowired
  RequestPasswordResetValidator requestPasswordResetValidator;

  @Autowired
  @Qualifier(value = "setNewPasswordValidator")
  SetNewPasswordValidator setNewPassworResetValidator;

  @Autowired
  @Qualifier(value = "resetAccountPasswordValidator")
  ResetAccountPasswordValidator resetAccountPasswordValidator;

  @Autowired
  PasswordUserAccountService passwordUserAccountService;

  public static final String REQUEST_PASSWORD_RESET_ACTION = "requestPasswordReset";

  public static final String RESET_PASSWORD_ACTION = "resetPassword";

  public static final String SET_NEW_PASSWORD_ACTION = "setNewPassword";

  public static final String USER_PARENT_ENTITY_NAME = "user";

  @RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM
      + "/accounts/requestpasswordreset/{userName:.+}" }, method = RequestMethod.POST)
  public ResponseEntity<String> requestPasswordReset(
      @Valid RequestPasswordResetRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) {

    ValidationResult validationResult = requestPasswordResetValidator.validate(
        bindingResult, request);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          REQUEST_PASSWORD_RESET_ACTION);
    }

    UserAccountValueObject userAccount = (UserAccountValueObject) validationResult
        .getContextObjects().get(
            ForgotUserNameValidator.USER_ACCOUNT_OBJECT_KEY);
    passwordUserAccountService.requestPasswordReset(userAccount);

    LinkedHashMap<String, Object> responseValues = new LinkedHashMap<String, Object>();
    responseValues.put(USER_PARENT_ENTITY_NAME, "success");

    return createSuccessResponseEntity(requestParameters, responseValues,
        request, REQUEST_PASSWORD_RESET_ACTION);
  }

  @RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM
      + "/accounts/resetpassword/{userName:.+}" }, method = RequestMethod.GET)
  public ResponseEntity<String> resetUserAccountPassword(
      @Valid ResetAccountPasswordRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) {

    ValidationResult validationResult = resetAccountPasswordValidator.validate(
        bindingResult, request);
    UserAccountValueObject userAccount = (UserAccountValueObject) validationResult
        .getContextObjects().get(
            ForgotUserNameValidator.USER_ACCOUNT_OBJECT_KEY);
    if (!validationResult.isValid()) {
      if (userAccount != null) {
        passwordUserAccountService.resetAccountPassword(userAccount);
      }
      return createErrorResponseEntity(validationResult, request,
          RESET_PASSWORD_ACTION);
    }

    LinkedHashMap<String, Object> responseValues = new LinkedHashMap<String, Object>();
    responseValues.put(USER_PARENT_ENTITY_NAME, "success");

    return createSuccessResponseEntity(requestParameters, responseValues,
        request, RESET_PASSWORD_ACTION);
  }

  @RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM
      + "/accounts/setnewpassword/{userName:.+}" }, method = RequestMethod.POST)
  public ResponseEntity<String> setNewUserAccountPassword(
      @Valid SetNewPasswordRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) {

    ValidationResult validationResult = setNewPassworResetValidator.validate(
        bindingResult, request);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          SET_NEW_PASSWORD_ACTION);
    }

    UserAccountValueObject userAccount = (UserAccountValueObject) validationResult
        .getContextObjects().get(
            ForgotUserNameValidator.USER_ACCOUNT_OBJECT_KEY);
    passwordUserAccountService.setAccountNewPassword(requestParameters,
        userAccount);

    LinkedHashMap<String, Object> responseValues = new LinkedHashMap<String, Object>();
    responseValues.put(USER_PARENT_ENTITY_NAME, "success");

    return createSuccessResponseEntity(requestParameters, responseValues,
        request, SET_NEW_PASSWORD_ACTION);
  }
}
