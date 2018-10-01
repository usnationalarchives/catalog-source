package gov.nara.opa.api.controller.export;

import gov.nara.opa.api.services.export.AccountExportService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.validation.export.DeleteAccountExportRequestParameters;
import gov.nara.opa.api.validation.export.DeleteAccountExportValidator;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class DeleteAccountExportController extends AbstractBaseController {

  public static final String DELETE_ACCOUNT_EXPORT_ACTION = "deleteAccountExport";

  public static final String ACCOUNT_EXPORT_ENTITY_NAME = "accountExport";

  @Autowired
  DeleteAccountExportValidator deleteAccountExportValidator;

  @Autowired
  AccountExportService accountExportService;

  @RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM
      + "/exports/auth" }, method = RequestMethod.DELETE)
  public ResponseEntity<String> deleteExport(
      @Valid DeleteAccountExportRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request,
      HttpServletResponse response) {

    ValidationResult validationResult = deleteAccountExportValidator.validate(
        bindingResult, request, AbstractRequestParameters.INTERNAL_API_TYPE);
    if (!validationResult.isValid()) {
      createErrorResponseEntity(validationResult, request,
          DELETE_ACCOUNT_EXPORT_ACTION);
    }

    @SuppressWarnings("unchecked")
    AccountExportValueObject accountExport = (AccountExportValueObject) validationResult
        .getContextObjects().get(DeleteAccountExportValidator.ACCOUNT_EXPORT);

    accountExportService.deleteAccountExport(accountExport);
    return createSuccessResponseEntity(ACCOUNT_EXPORT_ENTITY_NAME,
        requestParameters, accountExport, request, DELETE_ACCOUNT_EXPORT_ACTION);
  }
}
