package gov.nara.opa.api.services.impl.nameValueListItems;

import gov.nara.opa.api.dataaccess.nameValueLists.NameValueListsDao;
import gov.nara.opa.api.services.nameValueListItems.NameValueListItemsService;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.api.validation.nameValueListItems.NameValueListItemRequestParameters;
import gov.nara.opa.api.valueobject.nameValueLists.NameValueItemCollectionValueObject;
import gov.nara.opa.api.valueobject.nameValueLists.NameValueListItemValueObject;
import gov.nara.opa.api.valueobject.nameValueLists.NameValueListItemsHelper;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorMessageConstants;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.valueobject.utils.StringListValueObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class NameValueListItemsServiceImpl implements NameValueListItemsService {

  @Autowired
  private NameValueListsDao dao;

  @Override
  public StringListValueObject getListNames(
      NameValueListItemRequestParameters requestParams,
      ValidationResult validationResult) {

    StringListValueObject result = new StringListValueObject(
        "nameValueListNames", dao.getListNames());

    if (result == null || result.getValues() == null
        || result.getValues().size() == 0) {
      ValidationUtils.setValidationError(validationResult,
          ArchitectureErrorCodeConstants.NOT_FOUND,
          ArchitectureErrorMessageConstants.NOT_FOUND,
          requestParams.getAction(), HttpStatus.NOT_FOUND);
    }

    return result;
  }

  @Override
  public NameValueItemCollectionValueObject getListItemsByListName(
      NameValueListItemRequestParameters requestParams,
      ValidationResult validationResult) {

    NameValueItemCollectionValueObject result = dao
        .getListItemsByListName(requestParams.getListName());

    if (result == null || result.getListItems() == null
        || result.getListItems().size() == 0) {
      ValidationUtils.setValidationError(validationResult,
          ArchitectureErrorCodeConstants.NOT_FOUND,
          ArchitectureErrorMessageConstants.NOT_FOUND,
          requestParams.getAction(), HttpStatus.NOT_FOUND);
    }

    return result;
  }

  @Override
  public NameValueListItemValueObject getListItem(
      NameValueListItemRequestParameters requestParams,
      ValidationResult validationResult) {

    NameValueListItemValueObject result = dao.getListItem(
        requestParams.getListName(), requestParams.getItemName());

    if (result == null) {
      ValidationUtils.setValidationError(validationResult,
          ArchitectureErrorCodeConstants.NOT_FOUND,
          ArchitectureErrorMessageConstants.NOT_FOUND,
          requestParams.getAction(), HttpStatus.NOT_FOUND);
    }

    return result;
  }

  @Override
  public NameValueListItemValueObject insertListItem(NameValueListItemRequestParameters requestParams,
      ValidationResult validationResult) {

    if(dao.createListItem(NameValueListItemsHelper.fromRequestParams(requestParams))) {
      return dao.getListItem(
          requestParams.getListName(), requestParams.getItemName());
    } else {
      ValidationUtils.setValidationError(validationResult,
          ArchitectureErrorCodeConstants.DUPLICATE_RECORD,
          ArchitectureErrorMessageConstants.DUPLICATE_RECORD,
          requestParams.getAction(), HttpStatus.BAD_REQUEST);
    }

    return null;
  }

  @Override
  public NameValueListItemValueObject updateListItem(NameValueListItemRequestParameters requestParams,
      ValidationResult validationResult) {

    if(dao.updateListItem(NameValueListItemsHelper.fromRequestParams(requestParams))) {
      return dao.getListItem(
          requestParams.getListName(), requestParams.getItemName());
    }
    
    return null;
  }

  @Override
  public void deleteList(NameValueListItemRequestParameters requestParams,
      ValidationResult validationResult) {

    dao.deleteListByName(requestParams.getListName());

  }

  @Override
  public void deleteListItem(NameValueListItemRequestParameters requestParams,
      ValidationResult validationResult) {
    
    dao.deleteListItem(NameValueListItemsHelper.fromRequestParams(requestParams));
    

  }

}
