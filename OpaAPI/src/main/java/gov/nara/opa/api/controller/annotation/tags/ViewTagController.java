package gov.nara.opa.api.controller.annotation.tags;

import gov.nara.opa.api.services.annotation.tags.ViewTagService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.api.validation.annotation.tags.TagsViewRequestParameters;
import gov.nara.opa.api.validation.annotation.tags.TagsViewValidator;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;
import gov.nara.opa.common.valueobject.annotation.tags.TagValueObject;
import gov.nara.opa.common.valueobject.annotation.tags.TagsCollectionValueObject;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class ViewTagController extends AbstractBaseController {

  static OpaLogger log = OpaLogger.getLogger(ViewTagController.class);
  
  public static final String INFO_TAG_MESSAGE = "naId=%1$s,objectId=%2$s,action=%3$s,tagText=%4$s";
  public static final String GET_TAG_ACTION = "getTag";
  
  @Autowired
  private ViewTagService viewTagService;

  @Autowired
  private TagsViewValidator tagsValidator;

  public static final String VIEW_TAG_ACTION = "";
  public static final String TAGS_PARENT_ENTITY_NAME_TAGS = "tags";
  public static final String TAGS_PARENT_ENTITY_NAME_TAG = "tag";

  @RequestMapping(value = {
      "/{apiType}/" + Constants.API_VERS_NUM + "/id/{naId}/tags",
      "/{apiType}/" + Constants.API_VERS_NUM
          + "/id/{naId}/objects/{objectId:.+}/tags" }, method = RequestMethod.GET)
  public ResponseEntity<String> viewTags(
      @Valid TagsViewRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) {

    ValidationResult validationResult = tagsValidator.validate(bindingResult,
        request);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          VIEW_TAG_ACTION);
    }

    TagsCollectionValueObject tags = viewTagService.getTags(requestParameters);

    AbstractWebEntityValueObject responseObject = null;
    String entityName = null;

    Integer totalTags = tags.getTotalTags();
    if (totalTags.intValue() == 0) {
      addNoRecordsFoundError(validationResult);
      return createErrorResponseEntity(validationResult, request,
          VIEW_TAG_ACTION);
    } else if (totalTags.intValue() == 1 && requestParameters.getText() != null) {
      responseObject = tags.getTags().get(0);
      entityName = TAGS_PARENT_ENTITY_NAME_TAG;
    } else {
      responseObject = tags;
      entityName = TAGS_PARENT_ENTITY_NAME_TAGS;
    }
    
    for(TagValueObject tag : tags.getTags() ) {
      log.info(String.format(INFO_TAG_MESSAGE, tag.getNaId(), tag.getObjectId(), GET_TAG_ACTION, tag.getAnnotation()));
    }

    return createSuccessResponseEntity(entityName, requestParameters,
        responseObject, request, VIEW_TAG_ACTION);
  }

  private void addNoRecordsFoundError(ValidationResult validationResult) {
    ValidationError error = new ValidationError();
    error.setErrorCode(ErrorCodeConstants.TAG_NOT_FOUND);
    error.setErrorMessage(ErrorConstants.ACTIVE_TAG_NOT_FOUND);
    validationResult.addCustomValidationError(error);
    validationResult.setHttpStatus(HttpStatus.NOT_FOUND);
  }

}
