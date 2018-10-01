package gov.nara.opa.api.valueobject.migration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

public class ListItemsMigrationValueObject extends AbstractWebEntityValueObject
    implements ListItemsMigrationObjectConstants {
  
  private Integer listItemsRead;
  
  private Integer listItemsWritten;
  
  private Boolean fullDetail = false;
  
  private List<Integer> notInAssetRecord;
  
  
  public Boolean getFullDetail() {
    return fullDetail;
  }

  public void setFullDetail(Boolean fullDetail) {
    this.fullDetail = fullDetail;
  }

  public List<Integer> getNotInAssetRecord() {
    return notInAssetRecord;
  }

  public void setNotInAssetRecord(List<Integer> notInAssetRecord) {
    this.notInAssetRecord = notInAssetRecord;
  }

  public Integer getListItemsRead() {
    return listItemsRead;
  }

  public void setListItemsRead(Integer listItemsRead) {
    this.listItemsRead = listItemsRead;
  }

  public Integer getListItemsWritten() {
    return listItemsWritten;
  }

  public void setListItemsWritten(Integer listItemsWritten) {
    this.listItemsWritten = listItemsWritten;
  }

  @Override
  public Map<String, Object> getDatabaseContent() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
    
    result.put(LIST_ITEMS_READ, getListItemsRead());
    result.put(LIST_ITEMS_WRITTEN, getListItemsWritten());
    if(fullDetail) {
      result.put(NOT_IN_ASSET_RECORD, getNotInAssetRecord());
    }
    
    return result;
  }

}
