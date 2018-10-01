package gov.nara.opa.common.dataaccess.impl.annotation.tags;

import gov.nara.opa.common.valueobject.annotation.tags.OpaTitle;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.ResultSetExtractor;

public class OpaTitleResultSetExtractor implements ResultSetExtractor<OpaTitle> {

  @Override
  public OpaTitle extractData(ResultSet resultSet) throws SQLException {
    String naId = resultSet.getString("na_id");
    String opaTitleText = resultSet.getString("opa_title");
    if(opaTitleText == null || opaTitleText.isEmpty()) {
      opaTitleText = String.format("No title found for Nara ID '%1$s'", naId);
    }
    
    OpaTitle opaTitle = new OpaTitle();
    opaTitle.setNaId(naId);
    opaTitle.setOpaTitle(opaTitleText);
    opaTitle.setOpaType(resultSet.getString("opa_type"));
    opaTitle.setObjectId(resultSet.getString("object_id"));
    opaTitle.setTotalPages(resultSet.getInt("total_pages"));
    opaTitle.setPageNum(resultSet.getInt("page_num"));
    opaTitle.setAddedTs(resultSet.getTimestamp("annotation_ts"));

    return opaTitle;
  }

}
