package gov.nara.opa.api.valueobject.user.lists;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gov.nara.opa.api.user.lists.UserList;
import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

public class UserListCollectionValueObject extends AbstractWebEntityValueObject {

  private Integer totalLists;
  private List<UserList> listCollection;
  
  
  public Integer getTotalLists() {
    return totalLists;
  }

  public void setTotalLists(Integer totalLists) {
    this.totalLists = totalLists;
  }

  public List<UserList> getListCollection() {
    return listCollection;
  }

  public void setListCollection(List<UserList> listCollection) {
    this.listCollection = listCollection;
  }

  @Override
  public Map<String, Object> getDatabaseContent() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    // TODO Auto-generated method stub
    return null;
  }

}
