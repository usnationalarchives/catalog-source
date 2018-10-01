package gov.nara.opa.api.dataaccess.ingestion;

import java.io.UnsupportedEncodingException;

import org.springframework.dao.DataAccessException;

public interface UpdatePageNumDao {

  String update(String naId, String objectId, int pageNum)
      throws DataAccessException, UnsupportedEncodingException;

  String delete(String naId, String objectId)
      throws UnsupportedEncodingException;

}
