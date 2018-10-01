package gov.nara.opa.api.controller.administrator;

import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.validation.administrator.ViewAccountNotesRequestParameters;
import gov.nara.opa.api.validation.administrator.ViewAccountNotesValidator;
import gov.nara.opa.api.validation.administrator.ViewAccountReasonRequestParameters;
import gov.nara.opa.api.validation.administrator.ViewAccountReasonValidator;
import gov.nara.opa.api.valueobject.administrator.AccountReasonCollectionValueObject;
import gov.nara.opa.api.valueobject.administrator.AccountReasonValueObject;
import gov.nara.opa.api.valueobject.user.logs.AccountNotesCollectionValueObject;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.ValidationResult;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class ViewAccountReasonController extends AbstractBaseController {

  @Autowired
  ViewAccountReasonValidator viewAccountReasonValidator;

  @Autowired
  ViewAccountNotesValidator viewAccountNotesValidator;

  public static final String VIEW_ACCOUNT_REASON_ACTION = "viewAccountReason";

  public static final String VIEW_ACCOUNT_NOTES_ACTION = "viewAccountNotes";

  public static final String ACCOUNT_REASON_PARENT_ENTITY_NAME = "reasons";

  public static final String ACCOUNT_NOTES_PARENT_ENTITY_NAME = "notes";

  @RequestMapping(value = {
      "/" + Constants.INTERNAL_API_PATH + "/" + Constants.API_VERS_NUM
      + "/administrator/accounts/reasons",
      "/" + Constants.PUBLIC_API_PATH + "/" + Constants.API_VERS_NUM
      + "/system/reasons"
      }, method = RequestMethod.GET)
  public ResponseEntity<String> viewAnnotationReason(
      @Valid ViewAccountReasonRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) {

    ValidationResult validationResult = viewAccountReasonValidator.validate(
        bindingResult, request);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          VIEW_ACCOUNT_REASON_ACTION);
    }

    @SuppressWarnings("unchecked")
    List<AccountReasonValueObject> accountReasons = (List<AccountReasonValueObject>) validationResult
        .getContextObjects().get(
            ViewAccountReasonValidator.ACCOUNT_REASON_OBJECT_KEY);

    return createSuccessResponseEntity(ACCOUNT_REASON_PARENT_ENTITY_NAME,
        requestParameters, new AccountReasonCollectionValueObject(
            accountReasons), request, VIEW_ACCOUNT_REASON_ACTION);
  }

  @RequestMapping(value = {
      "/" + Constants.INTERNAL_API_PATH + "/" + Constants.API_VERS_NUM
      + "/administrator/accounts/notes/{userName:.+}",
      "/" + Constants.PUBLIC_API_PATH + "/" + Constants.API_VERS_NUM
      + "/users/{userName:.+}/notes"
      }, method = RequestMethod.GET)
  public ResponseEntity<String> viewAccountNotes(
      @Valid ViewAccountNotesRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) {

    ValidationResult validationResult = viewAccountNotesValidator.validate(
        bindingResult, request);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          VIEW_ACCOUNT_NOTES_ACTION);
    }

    AccountNotesCollectionValueObject notes = (AccountNotesCollectionValueObject) validationResult
        .getContextObjects().get(
            ViewAccountNotesValidator.ACCOUNT_NOTES_OBJECT_KEY);

    return createSuccessResponseEntity(ACCOUNT_NOTES_PARENT_ENTITY_NAME,
        requestParameters, notes, request, VIEW_ACCOUNT_NOTES_ACTION);
  }

}
