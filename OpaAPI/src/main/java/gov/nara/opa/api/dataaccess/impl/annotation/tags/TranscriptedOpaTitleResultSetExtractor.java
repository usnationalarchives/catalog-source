package gov.nara.opa.api.dataaccess.impl.annotation.tags;

import gov.nara.opa.api.annotation.TranscriptedOpaTitle;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.ResultSetExtractor;

public class TranscriptedOpaTitleResultSetExtractor implements
    ResultSetExtractor<TranscriptedOpaTitle> {

  @Override
  public TranscriptedOpaTitle extractData(ResultSet resultSet)
      throws SQLException {
    TranscriptedOpaTitle opaTitle = new TranscriptedOpaTitle();
    opaTitle.setNaId(resultSet.getString("na_id"));
    opaTitle.setOpaTitle(resultSet.getString("opa_title"));
    opaTitle.setFullName(resultSet.getString("author_full_name"));
    opaTitle.setCreatorFullName(resultSet.getString("creator_full_name"));
    opaTitle.setCreatorUserName(resultSet.getString("creator_user_name"));
    opaTitle.setOpaType(resultSet.getString("opa_type"));
    opaTitle.setObjectId(resultSet.getString("object_id"));
    opaTitle.setTotalPages(resultSet.getInt("total_pages"));
    opaTitle.setAnnotationId(resultSet.getInt("annotation_id"));
    opaTitle.setFirstAnnotationId(resultSet.getInt("first_annotation_id"));
    opaTitle.setAccountId(resultSet.getInt("account_id"));
    opaTitle.setPageNum(resultSet.getInt("page_num"));
    opaTitle.setAddedTs(resultSet.getTimestamp("annotation_ts"));
    opaTitle.setCreationTs(resultSet.getTimestamp("creation_ts"));

    return opaTitle;
  }

}
