package gov.nara.opa.api.controller.annotation.tags;

import gov.nara.opa.api.services.annotation.tags.CreateTagsService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.usagelogging.annotation.tags.TagsLogger;
import gov.nara.opa.api.utils.PageNumberUtils;
import gov.nara.opa.api.validation.annotation.tags.TagsCreateRequestParameters;
import gov.nara.opa.api.validation.annotation.tags.TagsCreateValidator;
import gov.nara.opa.api.validation.annotation.tags.TagsSearchRequestParameters;
import gov.nara.opa.api.validation.annotation.tags.TagsSearchValidator;
import gov.nara.opa.api.validation.search.SolrParamsValidator;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.valueobject.annotation.tags.TagsCollectionValueObject;

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

@Controller
public class CreateTagController extends AbstractBaseController {

  @Autowired
  private TagsCreateValidator tagsValidator;

  @Autowired
  private TagsSearchValidator tagsSearchValidator;

  @Autowired
  private CreateTagsService createTagsService;

  public static final String CREATE_TAG_ACTION = "save";
  public static final String CREATE_TAG_SEARCH_ACTION = "saveSearch";
  public static final String TAGS_PARENT_ENTITY_NAME = "tags";

  @Autowired
  private PageNumberUtils pageNumberUtils;

  @Autowired
  SolrParamsValidator solrParamsValidator;

  
  @RequestMapping(value = {
      "/{apiType}/" + Constants.API_VERS_NUM + "/id/{naId}/tags",
      "/{apiType}/" + Constants.API_VERS_NUM
          + "/id/{naId}/objects/{objectId:.+}/tags" }, method = RequestMethod.POST)
  public ResponseEntity<String> createTag(
      @Valid TagsCreateRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) throws BadSqlGrammarException, DataAccessException, UnsupportedEncodingException {

    
    ValidationResult validationResult = tagsValidator.validate(bindingResult,
        request);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          CREATE_TAG_ACTION);
    }

    requestParameters.setHttpSessionId(request.getSession().getId());
    TagsCollectionValueObject tags = createTagsService
        .createTags(requestParameters);

    TagsLogger.logTags(tags, this.getClass(), CREATE_TAG_ACTION,
        requestParameters.getApiType());

    return createSuccessResponseEntity(TAGS_PARENT_ENTITY_NAME,
        requestParameters, tags, request, CREATE_TAG_ACTION);
  }

  @RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM }, method = RequestMethod.POST, params = "tag")
  public ResponseEntity<String> createSearchTag(
      @Valid TagsSearchRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request) throws BadSqlGrammarException, DataAccessException, UnsupportedEncodingException {

    ValidationResult validationResult = tagsSearchValidator.validate(
        bindingResult, request, AbstractRequestParameters.PUBLIC_API_TYPE);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          CREATE_TAG_SEARCH_ACTION);
    }
    requestParameters.setQueryParameters(request.getParameterMap());
    solrParamsValidator.validate(validationResult,
        requestParameters.getQueryParameters());
    TagsCreateRequestParameters baseRequestParameters = tagsSearchValidator
        .createAddBaseRequestParameters(request, requestParameters,
            validationResult);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          CREATE_TAG_SEARCH_ACTION);
    }

    validationResult.setValidatedRequest(baseRequestParameters);
    tagsValidator.performCustomValidation(validationResult, request);
    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          CREATE_TAG_SEARCH_ACTION);
    }

    baseRequestParameters.setHttpSessionId(request.getSession().getId());
    TagsCollectionValueObject tags = createTagsService
        .createTags(baseRequestParameters);

    TagsLogger.logTags(tags, this.getClass(), CREATE_TAG_SEARCH_ACTION,
        baseRequestParameters.getApiType());

    return createSuccessResponseEntity(TAGS_PARENT_ENTITY_NAME,
        baseRequestParameters, tags, request, CREATE_TAG_SEARCH_ACTION);

  }

}
