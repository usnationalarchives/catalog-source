package gov.nara.opa.api.dataaccess.nameValueLists;

import java.util.List;

import gov.nara.opa.api.valueobject.nameValueLists.NameValueItemCollectionValueObject;
import gov.nara.opa.api.valueobject.nameValueLists.NameValueListItemValueObject;

public interface NameValueListsDao {

  List<String> getListNames();

  NameValueItemCollectionValueObject getListItemsByListName(String listName);
  
  NameValueListItemValueObject getListItem(String listName, String itemName);
  
  boolean createListItem(NameValueListItemValueObject listItem);
  
  boolean updateListItem(NameValueListItemValueObject listItem);
  
  boolean deleteListItem(NameValueListItemValueObject listItem);
  
  boolean deleteListByName(String listName);
  
  
}
