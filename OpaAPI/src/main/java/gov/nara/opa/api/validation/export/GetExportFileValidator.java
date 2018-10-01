package gov.nara.opa.api.validation.export;

import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.AbstractBaseValidator;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.dataaccess.export.AccountExportDao;
import gov.nara.opa.common.valueobject.export.AccountExportStatusEnum;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class GetExportFileValidator extends AbstractBaseValidator {

  @Autowired
  AccountExportDao accountExportDao;

  public static final String ACCOUNT_EXPORT = "accountExport";

  @Override
  protected void performCustomValidation(ValidationResult validationResult,
      HttpServletRequest request) {
    GetExportFileRequestParameters requestParameters = (GetExportFileRequestParameters) validationResult
        .getValidatedRequest();

    Integer bulkExportId = requestParameters.getExportId();
    AccountExportValueObject accountExport = accountExportDao
        .selectById(bulkExportId);

    if (accountExport == null) {
      ValidationError error = new ValidationError();
      error.setErrorCode(ErrorCodeConstants.EXPORT_NOT_FOUND);
      error.setErrorMessage(String.format(
          ErrorConstants.EXPORT_NOT_FOUND_FOR_EXPORT_ID, bulkExportId));
      validationResult.setHttpStatus(HttpStatus.NOT_FOUND);
      validationResult.addCustomValidationError(error);
      return;
    }

    AccountExportStatusEnum status = accountExport.getRequestStatus();

    if (!(status.equals(AccountExportStatusEnum.COMPLETED) || status
        .equals(AccountExportStatusEnum.COMPLETED))) {
      ValidationError error = new ValidationError();
      error.setErrorCode(ErrorCodeConstants.EXPORT_ERROR);
      error.setErrorMessage(ErrorConstants.EXPORT_NOT_READY);
      validationResult.addCustomValidationError(error);

    }
    validationResult.addContextObject(ACCOUNT_EXPORT, accountExport);

  }
}
