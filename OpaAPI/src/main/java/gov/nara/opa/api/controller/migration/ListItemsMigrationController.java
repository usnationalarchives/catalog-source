package gov.nara.opa.api.controller.migration;

import gov.nara.opa.api.services.migration.ListItemsMigrationService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.validation.migration.ListItemsMigrationRequestParameters;
import gov.nara.opa.api.validation.migration.ListItemsMigrationValidator;
import gov.nara.opa.api.valueobject.migration.ListItemsMigrationValueObject;
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
public class ListItemsMigrationController extends AbstractBaseController {

  @Autowired
  private ListItemsMigrationValidator validator;
  
  @Autowired
  private ListItemsMigrationService migrationService;
  
  public static final String DATA_MIGRATION_ACTION = "data-migration";
  public static final String DATA_MIGRATION_PARENT_ENTITY_NAME = "data";
  
  static OpaLogger log = OpaLogger.getLogger(ListItemsMigrationController.class);
  
  @RequestMapping(value = { "/" + Constants.PUBLIC_API_PATH + "/"
      + Constants.API_VERS_NUM + "/migration/listItems" }, method = RequestMethod.POST)
  public ResponseEntity<String> dataMigration(
      @Valid ListItemsMigrationRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) {
    
    ValidationResult validationResult = validator.validate(
        bindingResult, request);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          DATA_MIGRATION_ACTION);
    }

    requestParameters.setHttpSessionId(request.getSession().getId());
    ListItemsMigrationValueObject dataMigration = migrationService
        .doMigration(requestParameters, validationResult);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          DATA_MIGRATION_ACTION);
    }

    return createSuccessResponseEntity(DATA_MIGRATION_PARENT_ENTITY_NAME,
        requestParameters, dataMigration, request, DATA_MIGRATION_ACTION);
    
  }
  
  
}
