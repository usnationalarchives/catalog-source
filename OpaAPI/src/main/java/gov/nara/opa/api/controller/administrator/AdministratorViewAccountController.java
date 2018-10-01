package gov.nara.opa.api.controller.administrator;

import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.user.accounts.UserAccount;
import gov.nara.opa.api.utils.SessionUtils;
import gov.nara.opa.api.validation.administrator.AdministratorSearchAccountsRequestParameters;
import gov.nara.opa.api.validation.administrator.AdministratorSearchAccountsValidator;
import gov.nara.opa.api.validation.administrator.AdministratorViewAccountRequestParameters;
import gov.nara.opa.api.validation.administrator.AdministratorViewAccountValidator;
import gov.nara.opa.api.validation.common.accounts.CommonUserAccountValidator;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.valueobject.user.accounts.SearchUserAccountCollectionValueObject;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountCollectionValueObject;
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
public class AdministratorViewAccountController extends AbstractBaseController {

  @Autowired
  private AdministratorViewAccountValidator viewAccountValidator;

  @Autowired
  private AdministratorSearchAccountsValidator searchAccountsValidator;

  public static final String VIEW_ACCOUNT_ACTION = "viewUserAccount";

  public static final String SEARCH_ACCOUNT_ACTION = "searchUserAccounts";

  public static final String USER_ACCOUNT_ENTITY_NAME = "user";

  public static final String USER_ACCOUNTS_ENTITY_NAME = "users";

  static OpaLogger log = OpaLogger
      .getLogger(AdministratorViewAccountController.class);

  @RequestMapping(value = {
      "/" + Constants.INTERNAL_API_PATH + "/" + Constants.API_VERS_NUM
          + "/administrator/accounts/profile/{userName:.+}",
      "/" + Constants.PUBLIC_API_PATH + "/" + Constants.API_VERS_NUM
          + "/users/{userName:.+}" }, method = RequestMethod.GET)
  public ResponseEntity<String> viewUserProfile(
      @Valid AdministratorViewAccountRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) {

    ValidationResult validationResult = viewAccountValidator.validate(
        bindingResult, request);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          VIEW_ACCOUNT_ACTION);
    }

    UserAccountValueObject userAccount = (UserAccountValueObject) validationResult
        .getContextObjects().get(
            CommonUserAccountValidator.USER_ACCOUNT_OBJECT_KEY);

    return createSuccessResponseEntity(USER_ACCOUNT_ENTITY_NAME,
        requestParameters, userAccount, request, VIEW_ACCOUNT_ACTION);

  }

  @RequestMapping(value = { "/" + Constants.PUBLIC_API_PATH + "/"
      + Constants.API_VERS_NUM + "/users" }, method = RequestMethod.GET)
  public ResponseEntity<String> publicSearchAccounts(
      @Valid AdministratorSearchAccountsRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) {

    ValidationResult validationResult = searchAccountsValidator.validate(
        bindingResult, request);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          SEARCH_ACCOUNT_ACTION);
    }

    UserAccountCollectionValueObject users = (UserAccountCollectionValueObject) validationResult
        .getContextObjects().get(
            CommonUserAccountValidator.USER_ACCOUNT_OBJECT_KEY);

    UserAccount requestingUser = SessionUtils.getSessionUser();
    
    boolean showPrivateData = requestingUser != null && (requestingUser.isAdministrator() || requestingUser.isAdminModerator());
    
    SearchUserAccountCollectionValueObject searchUsers = new SearchUserAccountCollectionValueObject(users, showPrivateData);
    
    return createSuccessResponseEntity(USER_ACCOUNTS_ENTITY_NAME,
        requestParameters, searchUsers, request, requestParameters.getAction());
  }

  @RequestMapping(value = { "/{iapi}/" + Constants.API_VERS_NUM
      + "/administrator/accounts/search" }, method = RequestMethod.GET)
  public ResponseEntity<String> searchAccounts(
      @Valid AdministratorSearchAccountsRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) {

    ValidationResult validationResult = searchAccountsValidator.validate(
        bindingResult, request);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          SEARCH_ACCOUNT_ACTION);
    }

    UserAccountCollectionValueObject users = (UserAccountCollectionValueObject) validationResult
        .getContextObjects().get(
            CommonUserAccountValidator.USER_ACCOUNT_OBJECT_KEY);

    return createSuccessResponseEntity(USER_ACCOUNTS_ENTITY_NAME,
        requestParameters, users, request, SEARCH_ACCOUNT_ACTION);

  }

}
