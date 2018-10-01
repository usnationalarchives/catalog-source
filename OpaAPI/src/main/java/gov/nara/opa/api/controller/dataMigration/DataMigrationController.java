package gov.nara.opa.api.controller.dataMigration;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import gov.nara.opa.api.controller.annotation.tags.CreateTagController;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.validation.dataMigration.DataMigrationStartRequestParameters;
import gov.nara.opa.api.validation.dataMigration.DataMigrationStartValidator;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.ValidationResult;

@Controller
public class DataMigrationController extends AbstractBaseController {

  public static final String DATA_MIGRATION_ACTION = "dataMigration";
  public static final String DATA_MIGRATION_PARENT_ENTITY_NAME = "dataMigration";
  
  @Autowired
  private DataMigrationStartValidator dataMigrationValidator;
  
  private static Logger log = Logger.getLogger(CreateTagController.class);
  
  @RequestMapping(value = {
      "/{apiType}/" + Constants.API_VERS_NUM + "/migration" }, method = RequestMethod.POST)
  public ResponseEntity<String> startDataMigration(@Valid DataMigrationStartRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) {
    
    log.trace("Validating parameters");
    ValidationResult validationResult = dataMigrationValidator.validate(bindingResult, request);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          DATA_MIGRATION_ACTION);
    }
    
    requestParameters.setHttpSessionId(request.getSession().getId());

    
    
    return null;
  }
  
}
