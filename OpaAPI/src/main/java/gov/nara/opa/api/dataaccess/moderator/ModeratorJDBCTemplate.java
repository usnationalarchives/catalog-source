package gov.nara.opa.api.dataaccess.moderator;

import gov.nara.opa.api.dataaccess.impl.moderator.AnnotationReasonRowMapper;
import gov.nara.opa.api.moderator.AnnotationReason;
import gov.nara.opa.architecture.dataaccess.AbstractOpaDbJDBCTemplate;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class ModeratorJDBCTemplate extends AbstractOpaDbJDBCTemplate implements
    ModeratorDao {

  /**
   * Insert a new annotation_reason in the database
   * 
   * @param accountId
   *          accountId of the reason we are creating
   * @param reason
   *          reason of the reason we are creating
   * @return true if the reason was inserted, otherwise false
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   */
  @Override
  public boolean create(int accountId, String reason)
      throws DataAccessException, UnsupportedEncodingException {
    boolean result = false;

    // Create and fill the query with the specific parameters
    String sql = " INSERT INTO opadb.annotation_reasons "
        + " (account_id, reason, reason_status, reason_added_ts) "
        + " VALUES ( ?, ?, 1, now()) ";

    // the update() will return the number of rows modified, so greater than
    // 0 means that the query was executed succesful
    result = (getJdbcTemplate().update(sql,
        new Object[] { accountId, reason.getBytes("UTF-8") }) > 0);

    return result;
  }

  /**
   * Select the annotation_reason that fullfill the paramters specified
   * 
   * @param accountId
   *          accountId of the reason we are creating
   * @param reason
   *          reason of the reason w e are creating
   * @return Collection of the reasons found that fullfill the specified
   *         parameters
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   */
  @Override
  public List<AnnotationReason> getAnnotationReasons(int accountId,
      String reason) throws DataAccessException, UnsupportedEncodingException {

    ArrayList<Object> paramsList = new ArrayList<Object>();

    // Create and fill the query with the specific parameters
    String sql = "SELECT r.reason_id, r.account_id, "
        + "r.reason, r.reason_status, r.reason_added_ts "
        + "FROM opadb.annotation_reasons r WHERE 1=1 ";

    if (accountId != 0) {
      sql += " AND account_id = ? ";
      paramsList.add(accountId);
    }

    if (reason != null && !reason.equals("")) {
      sql += " AND reason = ? ";
      paramsList.add(reason);
    }

    return getJdbcTemplate().query(sql, paramsList.toArray(),
        new AnnotationReasonRowMapper());
  }

}
