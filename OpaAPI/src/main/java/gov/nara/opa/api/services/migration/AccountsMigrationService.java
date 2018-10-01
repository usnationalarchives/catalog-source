package gov.nara.opa.api.services.migration;

import gov.nara.opa.api.validation.migration.AccountsMigrationRequestParameters;
import gov.nara.opa.api.valueobject.migration.AccountsMigrationValueObject;
import gov.nara.opa.architecture.web.validation.ValidationResult;

public interface AccountsMigrationService {
  
  /**
   * Performs the migration of accounts
   * 
   * @param requestParameters
   */
  public AccountsMigrationValueObject doMigration(AccountsMigrationRequestParameters requestParameters, 
      ValidationResult validationResult);
  
}
