package gov.nara.opa.api.controller.user.accounts;

import gov.nara.opa.api.services.user.accounts.ModifyUserAccountService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.validation.common.accounts.CommonUserAccountValidator;
import gov.nara.opa.api.validation.user.accounts.DeactivateUserAccountRequestParameters;
import gov.nara.opa.api.validation.user.accounts.DeactivateUserAccountValidator;
import gov.nara.opa.api.validation.user.accounts.ModifyUserAccountRequestParameters;
import gov.nara.opa.api.validation.user.accounts.ModifyUserAccountValidator;
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

@Controller
public class ModifyUserAccountController extends AbstractBaseController {

  @Autowired
  ModifyUserAccountValidator modifyUserAccountValidator;

  public static final String MODIFY_USER_PROFILE_ACTION = "modify";

  public static final String DEACTIVATE_USER_ACTION = "deactivate";

  public static final String USER_PARENT_ENTITY_NAME = "user";

  @Autowired
  ModifyUserAccountService modifyUserAccountService;

  @Autowired
  DeactivateUserAccountValidator deactivateUserAccountValidator;

  @RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM
      + "/accounts/modify/{userName:.+}" }, method = RequestMethod.POST)
  public ResponseEntity<String> modifyUserAccount(
      @Valid ModifyUserAccountRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) {

    ValidationResult validationResult = modifyUserAccountValidator.validate(
        bindingResult, request);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          MODIFY_USER_PROFILE_ACTION);
    }

    UserAccountValueObject userAccount = (UserAccountValueObject) validationResult
        .getContextObjects().get(
            CommonUserAccountValidator.USER_ACCOUNT_OBJECT_KEY);
    modifyUserAccountService.update(userAccount, requestParameters);

    return createSuccessResponseEntity(USER_PARENT_ENTITY_NAME,
        requestParameters, userAccount, request, MODIFY_USER_PROFILE_ACTION);
  }

  @RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM
      + "/accounts/deactivate/{userName:.+}" }, method = RequestMethod.PUT)
  public ResponseEntity<String> deactivateUserAccount(
      @Valid DeactivateUserAccountRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) {

    ValidationResult validationResult = deactivateUserAccountValidator
        .validate(bindingResult, request);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          DEACTIVATE_USER_ACTION);
    }

    UserAccountValueObject userAccount = (UserAccountValueObject) validationResult
        .getContextObjects().get(
            CommonUserAccountValidator.USER_ACCOUNT_OBJECT_KEY);
    modifyUserAccountService.deactivateAccount(userAccount);

    return createSuccessResponseEntity(USER_PARENT_ENTITY_NAME,
        requestParameters, userAccount, request, DEACTIVATE_USER_ACTION);
  }

}
