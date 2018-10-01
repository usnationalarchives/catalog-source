package gov.nara.opa.api.services.moderator;

import gov.nara.opa.api.moderator.AnnotationReason;

import java.io.UnsupportedEncodingException;

import org.springframework.dao.DataAccessException;

/**
 * Interface for the AnnotationReason service that handles requests from the
 * controller
 */
public interface AddAnnotationReasonService {

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
  public AnnotationReason createReason(int accountId, String reason);

  /**
   * Duplicate Reason value check
   * 
   * @param reason
   *          Reason's text
   * @return isDuplicate (true/false)
   */
  public boolean isDuplicateReason(int accountId, String reason);
}
