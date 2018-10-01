package gov.nara.opa.api.services.nameValueListItems;

import gov.nara.opa.api.validation.nameValueListItems.NameValueListItemRequestParameters;
import gov.nara.opa.api.valueobject.nameValueLists.NameValueItemCollectionValueObject;
import gov.nara.opa.api.valueobject.nameValueLists.NameValueListItemValueObject;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.valueobject.utils.StringListValueObject;

public interface NameValueListItemsService {

  StringListValueObject getListNames(NameValueListItemRequestParameters requestParams, ValidationResult validationResult);
  
  NameValueItemCollectionValueObject getListItemsByListName(NameValueListItemRequestParameters requestParams, ValidationResult validationResult);
  
  NameValueListItemValueObject getListItem(NameValueListItemRequestParameters requestParams, ValidationResult validationResult);
  
  NameValueListItemValueObject insertListItem(NameValueListItemRequestParameters requestParams, ValidationResult validationResult);
  
  NameValueListItemValueObject updateListItem(NameValueListItemRequestParameters requestParams, ValidationResult validationResult);
  
  void deleteList(NameValueListItemRequestParameters requestParams, ValidationResult validationResult);
  
  void deleteListItem(NameValueListItemRequestParameters requestParams, ValidationResult validationResult);
  
}
