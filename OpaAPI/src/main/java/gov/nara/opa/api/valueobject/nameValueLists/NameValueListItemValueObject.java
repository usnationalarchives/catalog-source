package gov.nara.opa.api.valueobject.nameValueLists;

import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class NameValueListItemValueObject extends AbstractWebEntityValueObject
    implements NameValueListItemValueObjectConstants {

  private String listName;
  private String itemName;
  private String itemValue;
  private String additionalContent;
  
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
  public Map<String, Object> getDatabaseContent() {
    Map<String, Object> databaseContent = new LinkedHashMap<String, Object>();
    databaseContent.put(LIST_NAME_DB, listName);
    databaseContent.put(ITEM_NAME_DB, itemName);
    databaseContent.put(ITEM_VALUE_DB, itemValue);
    databaseContent.put(ADDITIONAL_CONTENT_DB, additionalContent);
    return databaseContent;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    LinkedHashMap<String, Object> aspireContent = new LinkedHashMap<String, Object>();

    aspireContent.put(ITEM_NAME, itemName);
    aspireContent.put(ITEM_VALUE, itemValue);
    if(!StringUtils.isNullOrEmtpy(additionalContent)) {
      String[] additionalItems = additionalContent.split(",");
      for (String additionalItem : additionalItems) {
        String[] nameValuePair = additionalItem.split(":");
        String additionalItemName = nameValuePair[0];
        String additionalItemValue = (nameValuePair.length > 1 ? nameValuePair[1] : null);
        
        aspireContent.put(additionalItemName, additionalItemValue);
      }
    }
    

    return aspireContent;
  }

}
