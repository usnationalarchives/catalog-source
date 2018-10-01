package gov.nara.opa.api.services.migration;

import gov.nara.opa.api.validation.migration.TagsMigrationRequestParameters;
import gov.nara.opa.api.valueobject.migration.TagsMigrationValueObject;
import gov.nara.opa.architecture.web.validation.ValidationResult;

public interface TagsMigrationService {

  /**
   * @param requestParameters
   * @return
   */
  public TagsMigrationValueObject doMigration(
      TagsMigrationRequestParameters requestParameters, ValidationResult validationResult);
}
