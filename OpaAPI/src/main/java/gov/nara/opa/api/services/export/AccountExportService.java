package gov.nara.opa.api.services.export;

import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.validation.export.CreateAccountExportRequestParameters;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface AccountExportService {

  void deleteAccountExport(AccountExportValueObject accountExport);

  AccountExportValueObject createAccountExport(Integer accountId,
      ValidationResult validationResult, HttpServletResponse response,
      AccountExportValueObject accountExport,
      Map<String, String[]> queryParameters, HttpServletRequest request);

  AccountExportValueObject createAccountExport(
      CreateAccountExportRequestParameters requestParameters,
      Integer accountId, ValidationResult validationResult,
      HttpServletResponse response, HttpServletRequest request);
  
  /**
   * Removes all expired exports from both database and storage
   * @return The number of exports removed
   */
  int removeExpiredExports();

}
