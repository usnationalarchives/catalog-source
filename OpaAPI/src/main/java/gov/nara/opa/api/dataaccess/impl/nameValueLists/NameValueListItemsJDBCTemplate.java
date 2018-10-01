package gov.nara.opa.api.dataaccess.impl.nameValueLists;

import gov.nara.opa.api.dataaccess.nameValueLists.NameValueListsDao;
import gov.nara.opa.api.valueobject.nameValueLists.NameValueItemCollectionValueObject;
import gov.nara.opa.api.valueobject.nameValueLists.NameValueListItemValueObject;
import gov.nara.opa.api.valueobject.nameValueLists.NameValueListItemValueObjectConstants;
import gov.nara.opa.architecture.dataaccess.AbstractOpaDbJDBCTemplate;
import gov.nara.opa.common.dataaccess.utils.GenericRowMapper;
import gov.nara.opa.common.dataaccess.utils.StoredProcedureDataAccessUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class NameValueListItemsJDBCTemplate extends AbstractOpaDbJDBCTemplate
    implements NameValueListsDao, NameValueListItemValueObjectConstants {

  @Override
  public List<String> getListNames() {

    List<Map<String, Object>> resultSet = StoredProcedureDataAccessUtils
        .executeWithListResults(getJdbcTemplate(), "spGetListNames", null);

    List<String> result = new ArrayList<String>();

    for (Map<String, Object> listName : resultSet) {
      String name = listName.get(LIST_NAME_DB).toString();
      result.add(name);
    }

    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public NameValueItemCollectionValueObject getListItemsByListName(
      String listName) {

    HashMap<String, Object> params = new LinkedHashMap<String, Object>();
    params.put("listName", listName);

    List<NameValueListItemValueObject> results = (List<NameValueListItemValueObject>) StoredProcedureDataAccessUtils
        .execute(getJdbcTemplate(), "spGetNameValueList",
            new GenericRowMapper<NameValueListItemValueObject>(
                new NameValueListItemExtractor()), params);

    return new NameValueItemCollectionValueObject(results);
  }

  @Override
  public NameValueListItemValueObject getListItem(String listName,
      String itemName) {
    HashMap<String, Object> params = new LinkedHashMap<String, Object>();
    params.put("listName", listName);
    params.put("itemName", itemName);

    List<NameValueListItemValueObject> result = StoredProcedureDataAccessUtils
        .executeGeneric(getJdbcTemplate(), "spGetNameValueListItem",
            new GenericRowMapper<NameValueListItemValueObject>(
                new NameValueListItemExtractor()), params);

    return (result != null && result.size() > 0 ? result.get(0) : null);
  }

  @Override
  public boolean createListItem(NameValueListItemValueObject listItem) {
    HashMap<String, Object> params = new LinkedHashMap<String, Object>();
    params.put("listName", listItem.getListName());
    params.put("itemName", listItem.getItemName());
    params.put("itemValue", listItem.getItemValue());
    params.put("additionalContent", listItem.getItemName());
    
    boolean result = false;
    
    try {
      result = StoredProcedureDataAccessUtils.execute(getJdbcTemplate(), "spInsertNameValueListItem", params);
    } catch(DuplicateKeyException e) {
      result = false;
    }
    
    return result;
  }

  @Override
  public boolean updateListItem(NameValueListItemValueObject listItem) {
    HashMap<String, Object> params = new LinkedHashMap<String, Object>();
    params.put("listName", listItem.getListName());
    params.put("itemName", listItem.getItemName());
    params.put("itemValue", listItem.getItemValue());
    params.put("additionalContent", listItem.getItemName());
    
    return StoredProcedureDataAccessUtils.execute(getJdbcTemplate(), "spUpdateNameValueListItem", params);
  }

  @Override
  public boolean deleteListItem(NameValueListItemValueObject listItem) {
    HashMap<String, Object> params = new LinkedHashMap<String, Object>();
    params.put("listName", listItem.getListName());
    params.put("itemName", listItem.getItemName());
    
    return StoredProcedureDataAccessUtils.execute(getJdbcTemplate(), "spDeleteNameValueListItem", params);
  }

  @Override
  public boolean deleteListByName(String listName) {
    HashMap<String, Object> params = new LinkedHashMap<String, Object>();
    params.put("listName", listName);
    
    return StoredProcedureDataAccessUtils.execute(getJdbcTemplate(), "spDeleteNameValueList", params);
  }

}
