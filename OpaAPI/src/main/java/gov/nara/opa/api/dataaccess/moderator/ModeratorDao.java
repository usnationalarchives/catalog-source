package gov.nara.opa.api.dataaccess.moderator;

import gov.nara.opa.api.moderator.AnnotationReason;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.springframework.dao.DataAccessException;

public interface ModeratorDao {

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
  boolean create(int accountId, String reason) throws DataAccessException,
      UnsupportedEncodingException;

  /**
   * Select the annotation_reason that fullfill the paramters specified
   * 
   * @param accountId
   *          accountId of the reason we are creating
   * @param reason
   *          reason of the reason we are creating
   * @return Collection of the reasons found that fullfill the specified
   *         parameters
   * @throws DataAccessException
   * @throws UnsupportedEncodingException
   */
  List<AnnotationReason> getAnnotationReasons(int accountId, String reason)
      throws DataAccessException, UnsupportedEncodingException;

}