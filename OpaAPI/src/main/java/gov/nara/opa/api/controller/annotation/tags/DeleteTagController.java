package gov.nara.opa.api.controller.annotation.tags;

import java.io.UnsupportedEncodingException;

import gov.nara.opa.api.response.APIResponse;
import gov.nara.opa.api.services.annotation.tags.DeleteTagService;
import gov.nara.opa.api.services.annotation.tags.ViewTagService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.usagelogging.annotation.tags.TagsLogger;
import gov.nara.opa.api.validation.annotation.tags.TagsDeleteRequestParameters;
import gov.nara.opa.api.validation.annotation.tags.TagsDeleteValidator;
import gov.nara.opa.api.validation.annotation.tags.TagsSearchRequestParameters;
import gov.nara.opa.api.validation.annotation.tags.TagsSearchValidator;
import gov.nara.opa.api.validation.search.SolrParamsValidator;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.valueobject.annotation.tags.TagValueObject;

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

@Controller
public class DeleteTagController extends AbstractBaseController {

  @Autowired
  private DeleteTagService deleteTagService;

  @Autowired
  private ViewTagService viewTagService;

  @Autowired
  private APIResponse apiResponse;

  @Autowired
  TagsDeleteValidator tagsValidator;

  public static final String DELETE_TAG_ACTION = "deleteTag";

  public static final String DELETE_SEARCH_TAG_ACTION = "deleteSearchTag";

  public static final String TAG_PARENT_ENTITY_NAME = "tag";

  @Autowired
  private TagsSearchValidator tagsSearchValidator;

  @Autowired
  SolrParamsValidator solrParamsValidator;

  @RequestMapping(value = {
      "/{apiType}/" + Constants.API_VERS_NUM + "/id/{naId}/tags",
      "/{apiType}/" + Constants.API_VERS_NUM
          + "/id/{naId}/objects/{objectId:.+}/tags" }, method = RequestMethod.DELETE)
  public ResponseEntity<String> deleteTagObject(
      @Valid TagsDeleteRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) throws BadSqlGrammarException, DataAccessException, UnsupportedEncodingException {

    ValidationResult validationResult = tagsValidator.validate(bindingResult,
        request);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          DELETE_TAG_ACTION);
    }

    TagValueObject tag = (TagValueObject) validationResult.getContextObjects()
        .get(TagsDeleteValidator.TAG_VALUE_OBJECT_KEY);
    deleteTagService.deleteTag(tag, request.getSession().getId());

    TagsLogger.logTag(tag, this.getClass(), DELETE_TAG_ACTION,
        requestParameters.getApiType());

    return createSuccessResponseEntity(TAG_PARENT_ENTITY_NAME,
        requestParameters, tag, request, DELETE_TAG_ACTION);
  }

  @RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM }, method = RequestMethod.DELETE, params = "tag")
  public ResponseEntity<String> deleteSearchTagObject(
      @Valid TagsSearchRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) throws BadSqlGrammarException, DataAccessException, UnsupportedEncodingException {

    ValidationResult validationResult = tagsSearchValidator.validate(
        bindingResult, request);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          DELETE_SEARCH_TAG_ACTION);
    }

    requestParameters.setQueryParameters(request.getParameterMap());
    solrParamsValidator.validate(validationResult,
        requestParameters.getQueryParameters());
    TagsDeleteRequestParameters baseRequestParameters = tagsSearchValidator
        .createDeleteBaseRequestParameters(request, requestParameters,
            validationResult);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          DELETE_SEARCH_TAG_ACTION);
    }

    validationResult.setValidatedRequest(baseRequestParameters);
    tagsValidator.performCustomValidation(validationResult, request);

    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          DELETE_SEARCH_TAG_ACTION);
    }

    TagValueObject tag = (TagValueObject) validationResult.getContextObjects()
        .get(TagsDeleteValidator.TAG_VALUE_OBJECT_KEY);
    deleteTagService.deleteTag(tag, request.getSession().getId());

    TagsLogger.logTag(tag, this.getClass(), DELETE_SEARCH_TAG_ACTION,
        requestParameters.getApiType());

    return createSuccessResponseEntity(TAG_PARENT_ENTITY_NAME,
        requestParameters, tag, request, DELETE_SEARCH_TAG_ACTION);
  }
}
