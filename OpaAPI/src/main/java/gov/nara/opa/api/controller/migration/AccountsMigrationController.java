package gov.nara.opa.api.controller.migration;

import gov.nara.opa.api.services.migration.AccountsMigrationService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.validation.migration.AccountsMigrationRequestParameters;
import gov.nara.opa.api.validation.migration.AccountsMigrationValidator;
import gov.nara.opa.api.valueobject.migration.AccountsMigrationValueObject;
import gov.nara.opa.architecture.logging.OpaLogger;
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
public class AccountsMigrationController extends AbstractBaseController {

  private static OpaLogger logger = OpaLogger.getLogger(AccountsMigrationController.class);
  
  @Autowired
  private AccountsMigrationValidator validator;
  
  @Autowired
  private AccountsMigrationService migrationService;
  
  public static final String DATA_MIGRATION_ACTION = "data-migration";
  public static final String DATA_MIGRATION_PARENT_ENTITY_NAME = "data";
  
  @RequestMapping(value = { "/" + Constants.PUBLIC_API_PATH + "/"
      + Constants.API_VERS_NUM + "/migration/accounts" }, method = RequestMethod.POST)
  public ResponseEntity<String> dataMigration(
      @Valid AccountsMigrationRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) {
    logger.info("Starting account migration");
    
    //Validate
    logger.info("Account validation");
    ValidationResult validationResult = validator.validate(
        bindingResult, request);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          DATA_MIGRATION_ACTION);
    }
    
    //Do migration
    logger.info("Migrating data");
    AccountsMigrationValueObject accountsMigration = migrationService.doMigration(requestParameters, validationResult);
    if(!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          DATA_MIGRATION_ACTION);
    }
    
    logger.info("Migration successful");
    return createSuccessResponseEntity(DATA_MIGRATION_PARENT_ENTITY_NAME,
        requestParameters, accountsMigration, request, DATA_MIGRATION_ACTION);
  }
  
}
