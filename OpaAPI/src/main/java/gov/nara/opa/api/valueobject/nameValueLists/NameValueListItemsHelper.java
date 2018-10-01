package gov.nara.opa.api.valueobject.nameValueLists;

import gov.nara.opa.api.validation.nameValueListItems.NameValueListItemRequestParameters;

public class NameValueListItemsHelper {

  public static NameValueListItemValueObject fromRequestParams(NameValueListItemRequestParameters requestParams) {
    NameValueListItemValueObject result = new NameValueListItemValueObject();
    
    result.setListName(requestParams.getListName());
    result.setItemName(requestParams.getItemName());
    result.setItemValue(requestParams.getItemValue());
    result.setAdditionalContent(requestParams.getAdditionalContent());
    
    return result;
  }
  
}
