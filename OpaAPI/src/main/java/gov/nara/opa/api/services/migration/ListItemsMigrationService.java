package gov.nara.opa.api.services.migration;

import gov.nara.opa.api.validation.migration.ListItemsMigrationRequestParameters;
import gov.nara.opa.api.valueobject.migration.ListItemsMigrationValueObject;
import gov.nara.opa.architecture.web.validation.ValidationResult;

public interface ListItemsMigrationService {

  /**
   * Performs the migration of list items
   * 
   * @param requestParameters
   * @param validationResult
   * @return
   */
  public ListItemsMigrationValueObject doMigration(
      ListItemsMigrationRequestParameters requestParameters,
      ValidationResult validationResult);
  
  public ListItemsMigrationValueObject doMigration(
      String action, String sourceDatabaseName,
      ValidationResult validationResult, Boolean fullDetail);

}
