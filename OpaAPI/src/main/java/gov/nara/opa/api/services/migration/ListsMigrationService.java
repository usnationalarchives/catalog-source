package gov.nara.opa.api.services.migration;

import gov.nara.opa.api.validation.migration.ListsMigrationRequestParameters;
import gov.nara.opa.api.valueobject.migration.ListsMigrationValueObject;
import gov.nara.opa.architecture.web.validation.ValidationResult;

public interface ListsMigrationService {

  /**
   * Performs the migration of lits
   * 
   * @param requestParameters
   * @param validationResult
   * @return
   */
  public ListsMigrationValueObject doMigration(
      ListsMigrationRequestParameters requestParameters,
      ValidationResult validationResult);

}
