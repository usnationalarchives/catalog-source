package gov.nara.opa.api.controller.nameValueLists;

import gov.nara.opa.api.services.nameValueListItems.NameValueListItemsService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.validation.nameValueListItems.NameValueListItemRequestParameters;
import gov.nara.opa.api.validation.nameValueListItems.NameValueListItemsValidator;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class NameValueListItemsController extends AbstractBaseController {

  public static final String DEFAULT_ACTION = "nameValueListItemAction";
  public static final String GET_LIST_NAMES_ACTION = "getListNames";
  public static final String GET_LIST_ACTION = "getList";
  public static final String GET_LIST_ITEM_ACTION = "getListItem";
  public static final String INSERT_ITEM_ACTION = "insertItem";
  public static final String UPDATE_ITEM_ACTION = "updateItem";
  public static final String DELETE_ITEM_ACTION = "deleteItem";
  public static final String DELETE_LIST_ACTION = "deleteList";
  
  public static final String LIST_NAME_ENTITY = "listName";
  public static final String LIST_ENTITY = "list";
  public static final String LIST_ITEM_ENTITY = "listItem";
  
  
  public static Set<String> validActions;
  static {
    validActions = new HashSet<String>();
    validActions.add(GET_LIST_NAMES_ACTION);
    validActions.add(GET_LIST_ACTION);
    validActions.add(GET_LIST_ITEM_ACTION);
    validActions.add(INSERT_ITEM_ACTION);
    validActions.add(UPDATE_ITEM_ACTION);
    validActions.add(DELETE_ITEM_ACTION);
    validActions.add(DELETE_LIST_ACTION);
  }
  
  
  @Autowired
  private NameValueListItemsValidator validator;
  
  @Autowired
  private NameValueListItemsService service;
  
  
  @RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM + "/listManager" }, method = RequestMethod.GET)
  public ResponseEntity<String> getListData(
      @Valid NameValueListItemRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request,
      HttpServletResponse response) {
    
    ValidationResult validationResult = validator.validate(bindingResult, request); 

    String action = requestParameters.getAction();

    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          (!StringUtils.isNullOrEmtpy(action) ? action : DEFAULT_ACTION));
    }
    
    AbstractWebEntityValueObject responseObject = null;
    String entityName;
    switch(action) {
      case GET_LIST_NAMES_ACTION:
        entityName = LIST_NAME_ENTITY;
        responseObject = service.getListNames(requestParameters, validationResult);
        break;
      case GET_LIST_ACTION:
        entityName = LIST_ENTITY;
        responseObject = service.getListItemsByListName(requestParameters, validationResult);
        break;
      case GET_LIST_ITEM_ACTION:
        entityName = LIST_ITEM_ENTITY;
        responseObject = service.getListItem(requestParameters, validationResult);
        break;
      default:
        entityName = LIST_ENTITY;
        break;
    }
    
    if(!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          action);
    }
    
    return createSuccessResponseEntity(entityName, requestParameters,
        responseObject, request, action);
  }
  
  @RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM + "/listManager" }, method = RequestMethod.PUT)
  public ResponseEntity<String> addListItem(
      @Valid NameValueListItemRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request,
      HttpServletResponse response) {
    
    ValidationResult validationResult = validator.validate(bindingResult, request); 

    String action = requestParameters.getAction();

    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          (!StringUtils.isNullOrEmtpy(action) ? action : DEFAULT_ACTION));
    }
    
    AbstractWebEntityValueObject responseObject = service.insertListItem(requestParameters, validationResult);
    
    if(!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          action);
    }
    
    return createSuccessResponseEntity(LIST_ITEM_ENTITY, requestParameters,
        responseObject, request, action);
  }
  
  @RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM + "/listManager" }, method = RequestMethod.POST)
  public ResponseEntity<String> updateListItem(
      @Valid NameValueListItemRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request,
      HttpServletResponse response) {
    
    ValidationResult validationResult = validator.validate(bindingResult, request); 

    String action = requestParameters.getAction();

    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          (!StringUtils.isNullOrEmtpy(action) ? action : DEFAULT_ACTION));
    }
    
    AbstractWebEntityValueObject responseObject = service.updateListItem(requestParameters, validationResult);
    
    if(!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          action);
    }
    
    return createSuccessResponseEntity(LIST_ITEM_ENTITY, requestParameters,
        responseObject, request, action);
    
  }
  
  
  @RequestMapping(value = { "/{apiType}/" + Constants.API_VERS_NUM + "/listManager" }, method = RequestMethod.DELETE)
  public ResponseEntity<String> deleteFromList(
      @Valid NameValueListItemRequestParameters requestParameters,
      BindingResult bindingResult, HttpServletRequest request,
      HttpServletResponse response) {
    
    ValidationResult validationResult = validator.validate(bindingResult, request); 

    String action = requestParameters.getAction();

    if (!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          (!StringUtils.isNullOrEmtpy(action) ? action : DEFAULT_ACTION));
    }
    
    AbstractWebEntityValueObject responseObject = null;
    service.deleteListItem(requestParameters, validationResult);
    
    
    if(!validationResult.isValid()) {
      return createErrorResponseEntity(validationResult, request,
          action);
    }
    
    return createSuccessResponseEntity(LIST_ITEM_ENTITY, requestParameters,
        responseObject, request, action);    
  }
  
  
  
  
}
