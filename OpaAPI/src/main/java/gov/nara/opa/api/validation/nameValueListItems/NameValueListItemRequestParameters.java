package gov.nara.opa.api.validation.nameValueListItems;

import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;
import gov.nara.opa.architecture.web.validation.constraint.OpaPattern;
import gov.nara.opa.architecture.web.validation.constraint.OpaSize;

import java.util.LinkedHashMap;

public class NameValueListItemRequestParameters extends
    AbstractRequestParameters {
  
  private static final String ACTION_ERROR_MESSAGE = "Invalid action. Must be one of the following: getListNames, getList, getListItem, insertItem, updateItem, deleteList, deleteItem";

  
  @OpaNotNullAndNotEmpty
  @OpaPattern(regexp = "(^getListNames$)|(^getList$)|(^getListItem$)|(^insertItem$)|(^updateItem$)|(^deleteList$)|(^deleteItem$)|", message = ACTION_ERROR_MESSAGE)
  private String action;
  
  @OpaSize(max=100)
  private String listName;
  
  @OpaSize(max=100)
  private String itemName;
  
  @OpaSize(max=100)
  private String itemValue;
  
  @OpaSize(max=255)
  private String additionalContent;
  
  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getListName() {
    return listName;
  }

  public void setListName(String listName) {
    this.listName = listName;
  }

  public String getItemName() {
    return itemName;
  }

  public void setItemName(String itemName) {
    this.itemName = itemName;
  }

  public String getItemValue() {
    return itemValue;
  }

  public void setItemValue(String itemValue) {
    this.itemValue = itemValue;
  }

  public String getAdditionalContent() {
    return additionalContent;
  }

  public void setAdditionalContent(String additionalContent) {
    this.additionalContent = additionalContent;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();

    result.put("action", action);
    
    if(!StringUtils.isNullOrEmtpy(listName)) {
      result.put("listName", listName);
    }

    if(!StringUtils.isNullOrEmtpy(itemName)) {
      result.put("itemName", itemName);
    }
    
    if(!StringUtils.isNullOrEmtpy(itemValue)) {
      result.put("itemValue", itemValue);
    }
    
    if(!StringUtils.isNullOrEmtpy(additionalContent)) {
      result.put("additionalContent", additionalContent);
    }
    
    
    return result;
  }

}
