package gov.nara.opa.api.validation.nameValueListItems;

import gov.nara.opa.api.controller.nameValueLists.NameValueListItemsController;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.web.validation.AbstractBaseValidator;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorMessageConstants;
import gov.nara.opa.architecture.web.validation.ValidationResult;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class NameValueListItemsValidator extends AbstractBaseValidator {

  @Override
  protected void performCustomValidation(ValidationResult validationResult,
      HttpServletRequest request) {
    if (validateActionVsMethod(validationResult, request)
        && validateListName(validationResult, request)
        && validateItemName(validationResult, request)
        && validateItemValue(validationResult, request)) {
    }

  }

  private boolean validateActionVsMethod(ValidationResult validationResult,
      HttpServletRequest request) {
    NameValueListItemRequestParameters requestParams = (NameValueListItemRequestParameters) validationResult
        .getValidatedRequest();
    String action = requestParams.getAction();

    boolean result = true;
    
    switch (request.getMethod()) {
      case "GET":
        if (!action.equals(NameValueListItemsController.GET_LIST_ACTION)
            && !action
                .equals(NameValueListItemsController.GET_LIST_ITEM_ACTION)
            && !action
                .equals(NameValueListItemsController.GET_LIST_NAMES_ACTION)) {
          result = false;
        }
        break;
      case "PUT":
        if (!action.equals(NameValueListItemsController.INSERT_ITEM_ACTION)) {
          result = false;
        }        
        break;
      case "POST":
        if (!action.equals(NameValueListItemsController.UPDATE_ITEM_ACTION)) {
          result = false;
        }        
        break;
      case "DELETE":
        if (!action.equals(NameValueListItemsController.DELETE_ITEM_ACTION)
            && !action.equals(NameValueListItemsController.DELETE_LIST_ACTION)) {
          result = false;
        }        
        break;
      default:
    }
    
    if(!result) {
      ValidationUtils.setValidationError(validationResult,
          ArchitectureErrorCodeConstants.INVALID_VALUE,
          ErrorConstants.INVALID_ACTION_FOR_HTTP_METHOD_MESSAGE, requestParams.getAction(),
          HttpStatus.BAD_REQUEST);
    }

    return result;
  }

  private boolean validateListName(ValidationResult validationResult,
      HttpServletRequest request) {
    NameValueListItemRequestParameters requestParams = (NameValueListItemRequestParameters) validationResult
        .getValidatedRequest();
    String action = requestParams.getAction();

    if (!action.equals(NameValueListItemsController.GET_LIST_NAMES_ACTION)) {
      if (StringUtils.isNullOrEmtpy(requestParams.getListName())) {
        ValidationUtils.setValidationError(validationResult,
            ArchitectureErrorCodeConstants.MISSING_PARAMETER, String.format(
                ArchitectureErrorMessageConstants.NOT_NULL_AND_NOT_EMPTY,
                "listName"), requestParams.getAction(), HttpStatus.BAD_REQUEST);
        return false;
      }
    }

    return true;
  }

  private boolean validateItemName(ValidationResult validationResult,
      HttpServletRequest request) {
    NameValueListItemRequestParameters requestParams = (NameValueListItemRequestParameters) validationResult
        .getValidatedRequest();
    String action = requestParams.getAction();

    if (!action.equals(NameValueListItemsController.GET_LIST_NAMES_ACTION)
        && !action.equals(NameValueListItemsController.GET_LIST_ACTION)
        && !action.equals(NameValueListItemsController.DELETE_LIST_ACTION)) {
      if (StringUtils.isNullOrEmtpy(requestParams.getItemName())) {
        ValidationUtils.setValidationError(validationResult,
            ArchitectureErrorCodeConstants.MISSING_PARAMETER, String.format(
                ArchitectureErrorMessageConstants.NOT_NULL_AND_NOT_EMPTY,
                "itemName"), requestParams.getAction(), HttpStatus.BAD_REQUEST);
      }
    }

    return true;
  }

  private boolean validateItemValue(ValidationResult validationResult,
      HttpServletRequest request) {
    NameValueListItemRequestParameters requestParams = (NameValueListItemRequestParameters) validationResult
        .getValidatedRequest();
    String action = requestParams.getAction();

    if (!action.equals(NameValueListItemsController.GET_LIST_NAMES_ACTION)
        && !action.equals(NameValueListItemsController.GET_LIST_ACTION)
        && !action.equals(NameValueListItemsController.GET_LIST_ITEM_ACTION)
        && !action.equals(NameValueListItemsController.DELETE_LIST_ACTION)
        && !action.equals(NameValueListItemsController.DELETE_ITEM_ACTION)) {
      if (StringUtils.isNullOrEmtpy(requestParams.getItemName())) {
        ValidationUtils.setValidationError(validationResult,
            ArchitectureErrorCodeConstants.MISSING_PARAMETER, String.format(
                ArchitectureErrorMessageConstants.NOT_NULL_AND_NOT_EMPTY,
                "itemValue"), requestParams.getAction(), HttpStatus.BAD_REQUEST);
      }
    }

    return true;
  }

}
