package gov.nara.opa.api.dataaccess.impl.search;

import gov.nara.opa.common.valueobject.annotation.tags.OpaTitle;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.ResultSetExtractor;

public class OpaTitleResultSetExtractor implements ResultSetExtractor<OpaTitle> {

  @Override
  public OpaTitle extractData(ResultSet resultSet) throws SQLException {
    OpaTitle opaTitle = new OpaTitle();
    opaTitle.setNaId(resultSet.getString("na_id"));
    opaTitle.setOpaTitle(resultSet.getString("opa_title"));
    opaTitle.setOpaType(resultSet.getString("opa_type"));
    opaTitle.setTotalPages(resultSet.getInt("total_pages"));

    return opaTitle;
  }

}
