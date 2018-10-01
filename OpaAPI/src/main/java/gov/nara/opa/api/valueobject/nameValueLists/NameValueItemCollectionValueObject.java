package gov.nara.opa.api.valueobject.nameValueLists;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

public class NameValueItemCollectionValueObject extends
    AbstractWebEntityValueObject {

  private String listName;
  private List<NameValueListItemValueObject> listItems;

  public NameValueItemCollectionValueObject(List<NameValueListItemValueObject> listItems) {
    if(listItems != null && listItems.size() > 0) {
      listName = listItems.get(0).getListName();
    }
    this.listItems = listItems;
  }
  
  public String getListName() {
    return listName;
  }

  public void setListName(String listName) {
    this.listName = listName;
  }

  public List<NameValueListItemValueObject> getListItems() {
    return listItems;
  }

  public void setListItems(List<NameValueListItemValueObject> listItems) {
    this.listItems = listItems;
  }

  @Override
  public Map<String, Object> getDatabaseContent() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    LinkedHashMap<String, Object> aspireContent = new LinkedHashMap<String, Object>();
    if (listItems != null && listItems.size() > 0) {
      aspireContent.put("listName", listName);
      aspireContent.put("total", listItems.size());
      aspireContent.put("items", listItems);
    }
    return aspireContent;

  }

}
