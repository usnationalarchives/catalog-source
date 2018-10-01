package gov.nara.opa.api.controller.user.accounts;

import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.validation.common.accounts.CommonUserAccountValidator;
import gov.nara.opa.api.validation.user.accounts.ViewUserAccountRequestParameters;
import gov.nara.opa.api.validation.user.accounts.ViewUserAccountValidator;
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
public class ViewUserAccountController extends AbstractBaseController {

  @Autowired
  ViewUserAccountValidator viewUserAccountValidator;

  public static final String GET_USER_PROFILE_ACTION = "getUserProfile";

  public static final String USER_PARENT_ENTITY_NAME = "user";

  @RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM
      + "/accounts/profile/{userName:.+}" }, method = RequestMethod.GET)
  public ResponseEntity<String> viewUserProfile(
      @Valid ViewUserAccountRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) {

    ValidationResult validationResult = viewUserAccountValidator.validate(
        bindingResult, request);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          GET_USER_PROFILE_ACTION);
    }

    UserAccountValueObject userAccount = (UserAccountValueObject) validationResult
        .getContextObjects().get(
            CommonUserAccountValidator.USER_ACCOUNT_OBJECT_KEY);

    return createSuccessResponseEntity(USER_PARENT_ENTITY_NAME,
        requestParameters, userAccount, request, GET_USER_PROFILE_ACTION);
  }

}
