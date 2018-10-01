package gov.nara.opa.api.valueobject.user.lists;

import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class UserListItemValueObject extends AbstractWebEntityValueObject
    implements UserListItemValueObjectConstants {

  private Integer listItemId;
  private Integer listId;
  private String naId;
  private String objectId;
  private String opaId;
  private Timestamp itemTs;

  @Override
  public Map<String, Object> getDatabaseContent() {
    Map<String, Object> databaseContent = new HashMap<String, Object>();
    databaseContent.put(LIST_ID_DB, getListId());
    databaseContent.put(LIST_ITEM_ID_DB, getListItemId());
    databaseContent.put(NA_ID_DB, getNaId());
    databaseContent.put(OBJECT_ID_DB, getObjectId());
    databaseContent.put(OPA_ID_DB, getOpaId());
    databaseContent.put(ITEM_TS_DB, getItemTs());
    return databaseContent;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    // TODO Auto-generated method stub
    return null;
  }

  public Integer getListItemId() {
    return listItemId;
  }

  public void setListItemId(Integer listItemId) {
    this.listItemId = listItemId;
  }

  public Integer getListId() {
    return listId;
  }

  public void setListId(Integer listId) {
    this.listId = listId;
  }

  public String getNaId() {
    return naId;
  }

  public void setNaId(String naId) {
    this.naId = naId;
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  public String getOpaId() {
    return opaId;
  }

  public void setOpaId(String opaId) {
    this.opaId = opaId;
  }

  public Timestamp getItemTs() {
    return itemTs;
  }

  public void setItemTs(Timestamp itemTs) {
    this.itemTs = itemTs;
  }
}
