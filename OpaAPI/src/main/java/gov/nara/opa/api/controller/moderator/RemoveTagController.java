package gov.nara.opa.api.controller.moderator;

import gov.nara.opa.api.services.moderator.RemoveTagService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.validation.moderator.TagsDeleteModeratorValidator;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.validation.moderator.TagsModeratorRequestParameters;
import gov.nara.opa.common.valueobject.annotation.tags.TagValueObject;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Removes an active tag
 */
@Controller
public class RemoveTagController extends AbstractBaseController {

  @Autowired
  private RemoveTagService removeTagService;

  @Autowired
  private TagsDeleteModeratorValidator tagsModeratorValidator;

  public static final String REMOVE_TAG_ACTION = "removeTag";

  public static final String TAG_PARENT_ENTITY_NAME = "tag";

  @RequestMapping(value = {
      "/{apiType}/" + Constants.API_VERS_NUM
          + "/moderator/tags/id/{naId}",
      "/{apiType}/" + Constants.API_VERS_NUM
          + "/moderator/tags/id/{naId}/objects/{objectId:.+}" }, method = RequestMethod.DELETE)
  public ResponseEntity<String> removeTag(
      @Valid TagsModeratorRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) throws BadSqlGrammarException, DataAccessException, UnsupportedEncodingException {

    ValidationResult validationResult = tagsModeratorValidator.validate(
        bindingResult, request);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          REMOVE_TAG_ACTION);
    }

    TagValueObject tag = (TagValueObject) validationResult.getContextObjects()
        .get(TagsDeleteModeratorValidator.TAG_VALUE_OBJECT_KEY);
    removeTagService.removeTag(requestParameters, tag, request.getSession()
        .getId());
    return createSuccessResponseEntity(TAG_PARENT_ENTITY_NAME,
        requestParameters, tag, request, REMOVE_TAG_ACTION);
  }
}
