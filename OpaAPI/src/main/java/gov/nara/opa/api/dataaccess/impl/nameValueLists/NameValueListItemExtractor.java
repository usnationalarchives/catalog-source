package gov.nara.opa.api.dataaccess.impl.nameValueLists;

import java.sql.ResultSet;
import java.sql.SQLException;

import gov.nara.opa.api.valueobject.nameValueLists.NameValueListItemValueObject;
import gov.nara.opa.api.valueobject.nameValueLists.NameValueListItemValueObjectConstants;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

public class NameValueListItemExtractor implements ResultSetExtractor<NameValueListItemValueObject>, NameValueListItemValueObjectConstants {

  @Override
  public NameValueListItemValueObject extractData(ResultSet rs)
      throws SQLException, DataAccessException {
    NameValueListItemValueObject result = new NameValueListItemValueObject();
    
    result.setListName(rs.getString(LIST_NAME_DB));
    result.setItemName(rs.getString(ITEM_NAME_DB));
    result.setItemValue(rs.getString(ITEM_VALUE_DB));
    result.setAdditionalContent(rs.getString(ADDITIONAL_CONTENT_DB));
    
    return result;
  }

}
