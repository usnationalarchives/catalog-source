package gov.nara.opa.api.controller.administrator;

import gov.nara.opa.api.services.administrator.AddAccountReasonService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.validation.administrator.AddAccountReasonRequestParameters;
import gov.nara.opa.api.validation.administrator.AddAccountReasonValidator;
import gov.nara.opa.api.valueobject.administrator.AccountReasonValueObject;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.ValidationResult;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class AddAccountReasonController extends AbstractBaseController {

  public static final String ADD_ACCOUNT_REASON_ACTION = "addAccountReason";

  public static final String ACCOUNT_REASON_PARENT_ENTITY_NAME = "AccountReason";

  @Autowired
  AddAccountReasonValidator addAccountReasonValidator;

  @Autowired
  AddAccountReasonService addAccountReasonService;

  @RequestMapping(value = {"/{apiType}/" + Constants.API_VERS_NUM
      + "/administrator/accounts/reasons",
      "/" + Constants.PUBLIC_API_PATH + "/" + Constants.API_VERS_NUM
      + "/system/reasons"}, method = RequestMethod.POST)
  public ResponseEntity<String> addAccountReason(
      @Valid AddAccountReasonRequestParameters addAccountReasonRequest,
      BindingResult bindingResult, HttpServletRequest request) {

    ValidationResult validationResult = addAccountReasonValidator.validate(
        bindingResult, request);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          ADD_ACCOUNT_REASON_ACTION);
    }

    AccountReasonValueObject userAccount = addAccountReasonService
        .create(addAccountReasonRequest);
    return createSuccessResponseEntity(ACCOUNT_REASON_PARENT_ENTITY_NAME,
        addAccountReasonRequest, userAccount, request,
        ADD_ACCOUNT_REASON_ACTION);
  }
}
