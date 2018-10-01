package gov.nara.opa.api.valueobject.migration;

import java.util.LinkedHashMap;
import java.util.Map;

import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

public class ListsMigrationValueObject extends AbstractWebEntityValueObject
    implements ListsMigrationObjectConstants {
  
  private Integer listsRead;
  
  private Integer listsWritten;
  
  private ListItemsMigrationValueObject itemsValueObject;
  
  private Boolean fullDetail = false;
  
  public Boolean getFullDetail() {
    return fullDetail;
  }

  public void setFullDetail(Boolean fullDetail) {
    this.fullDetail = fullDetail;
  }

  public ListItemsMigrationValueObject getItemsValueObject() {
    return itemsValueObject;
  }

  public void setItemsValueObject(ListItemsMigrationValueObject itemsValueObject) {
    this.itemsValueObject = itemsValueObject;
  }

  public Integer getListsRead() {
    return listsRead;
  }

  public void setListsRead(Integer listsRead) {
    this.listsRead = listsRead;
  }

  public Integer getListsWritten() {
    return listsWritten;
  }

  public void setListsWritten(Integer listsWritten) {
    this.listsWritten = listsWritten;
  }

  @Override
  public Map<String, Object> getDatabaseContent() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
    
    result.put("@action", action);
    result.put(LISTS_READ, getListsRead());
    result.put(LISTS_WRITTEN, getListsWritten());
    result.put(ITEM_STATS, getItemsValueObject());
    
    return result;
  }

}
