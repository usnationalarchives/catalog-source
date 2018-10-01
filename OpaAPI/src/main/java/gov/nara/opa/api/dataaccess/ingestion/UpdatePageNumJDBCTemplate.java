package gov.nara.opa.api.dataaccess.ingestion;

import gov.nara.opa.architecture.dataaccess.AbstractOpaDbJDBCTemplate;

import java.io.UnsupportedEncodingException;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class UpdatePageNumJDBCTemplate extends AbstractOpaDbJDBCTemplate
    implements UpdatePageNumDao {

  @Override
  public String update(String naId, String objectId, int pageNum)
      throws UnsupportedEncodingException {
    String response = "";
    response += updateAnnotationLog(naId, objectId, pageNum);
    response += "," + updateTranscriptions(naId, objectId, pageNum);
    response += "," + updateTags(naId, objectId, pageNum);
    return response;
  }

  public int updateTranscriptions(String naId, String objectId, int pageNum)
      throws UnsupportedEncodingException {
    // Create and fill the query with the specific parameters
    String sql = "UPDATE annotation_transcriptions SET page_num = ? WHERE na_id= ? AND object_id = ?";

    // the update() will return the number of rows modified, so greater than
    // 0 means that the query was executed succesful
    return getJdbcTemplate().update(sql,
        new Object[] { pageNum, naId, objectId });

  }

  public int updateTags(String naId, String objectId, int pageNum)
      throws UnsupportedEncodingException {

    // Create and fill the query with the specific parameters
    String sql = "UPDATE annotation_tags SET page_num = ? WHERE na_id= ? AND object_id = ?";

    // the update() will return the number of rows modified, so greater than
    // 0 means that the query was executed succesful
    return getJdbcTemplate().update(sql,
        new Object[] { pageNum, naId, objectId });

  }

  public int updateAnnotationLog(String naId, String objectId, int pageNum)
      throws UnsupportedEncodingException {

    // Create and fill the query with the specific parameters
    String sql = "UPDATE annotation_log SET page_num = ? WHERE na_id= ? AND object_id = ?";

    // the update() will return the number of rows modified, so greater than
    // 0 means that the query was executed succesful
    return getJdbcTemplate().update(sql,
        new Object[] { pageNum, naId, objectId });

  }

  @Override
  public String delete(String naId, String objectId)
      throws UnsupportedEncodingException {
    String response = "";
    response += deleteAnnotationLog(naId, objectId);
    response += "," + deleteTranscriptions(naId, objectId);
    response += "," + deleteTags(naId, objectId);
    return response;
  }

  public int deleteTranscriptions(String naId, String objectId)
      throws UnsupportedEncodingException {

    // Create and fill the query with the specific parameters
    String sql = "UPDATE annotation_transcriptions SET status = -1 WHERE na_id= ? AND object_id = ?";

    // the update() will return the number of rows modified, so greater than
    // 0 means that the query was executed succesful
    return getJdbcTemplate().update(sql, new Object[] { naId, objectId });

  }

  public int deleteTags(String naId, String objectId)
      throws UnsupportedEncodingException {

    // Create and fill the query with the specific parameters
    String sql = "UPDATE annotation_tags SET status = -1 WHERE na_id= ? AND object_id = ?";

    // the update() will return the number of rows modified, so greater than
    // 0 means that the query was executed succesful
    return getJdbcTemplate().update(sql, new Object[] { naId, objectId });

  }

  public int deleteAnnotationLog(String naId, String objectId)
      throws UnsupportedEncodingException {

    // Create and fill the query with the specific parameters
    String sql = "UPDATE annotation_log SET status = -1, action = 'VOIDED' WHERE na_id= ? AND object_id = ?";

    // the update() will return the number of rows modified, so greater than
    // 0 means that the query was executed succesful
    return getJdbcTemplate().update(sql, new Object[] { naId, objectId });

  }
}
