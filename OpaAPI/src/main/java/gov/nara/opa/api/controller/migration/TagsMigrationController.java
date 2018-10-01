package gov.nara.opa.api.controller.migration;

import gov.nara.opa.api.services.migration.TagsMigrationService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.validation.migration.TagsMigrationRequestParameters;
import gov.nara.opa.api.validation.migration.TagsMigrationValidator;
import gov.nara.opa.api.valueobject.migration.TagsMigrationValueObject;
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
public class TagsMigrationController extends AbstractBaseController {

  @Autowired
  private TagsMigrationValidator tagsMigrationValidator;

  @Autowired
  private TagsMigrationService tagsMigrationService;

  public static final String DATA_MIGRATION_ACTION = "data-migration";
  public static final String DATA_MIGRATION_PARENT_ENTITY_NAME = "data";

  static OpaLogger log = OpaLogger.getLogger(TagsMigrationController.class);

  @RequestMapping(value = { "/" + Constants.PUBLIC_API_PATH + "/"
      + Constants.API_VERS_NUM + "/migration/tags" }, method = RequestMethod.POST)
  public ResponseEntity<String> dataMigration(
      @Valid TagsMigrationRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) {

    ValidationResult validationResult = tagsMigrationValidator.validate(
        bindingResult, request);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          DATA_MIGRATION_ACTION);
    }

    requestParameters.setHttpSessionId(request.getSession().getId());
    TagsMigrationValueObject dataMigration = tagsMigrationService
        .doMigration(requestParameters, validationResult);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          DATA_MIGRATION_ACTION);
    }

    return createSuccessResponseEntity(DATA_MIGRATION_PARENT_ENTITY_NAME,
        requestParameters, dataMigration, request, DATA_MIGRATION_ACTION);
  }

}
