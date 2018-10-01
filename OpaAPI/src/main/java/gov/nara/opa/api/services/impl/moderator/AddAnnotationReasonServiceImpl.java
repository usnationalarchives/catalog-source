package gov.nara.opa.api.services.impl.moderator;

import gov.nara.opa.api.dataaccess.moderator.ModeratorDao;
import gov.nara.opa.api.moderator.AnnotationReason;
import gov.nara.opa.api.services.moderator.AddAnnotationReasonService;
import gov.nara.opa.api.services.system.ConnectionManager;
import gov.nara.opa.api.services.user.lists.ViewUserListService;
import gov.nara.opa.architecture.logging.OpaLogger;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class AddAnnotationReasonServiceImpl implements
    AddAnnotationReasonService {

  private static OpaLogger logger = OpaLogger
      .getLogger(AddAnnotationReasonServiceImpl.class);

  SecureRandom random;

  @Autowired
  private ConnectionManager connectionManager;

  @Autowired
  private ModeratorDao moderatorDao;

  @Autowired
  private ViewUserListService viewUserListService;

  /**
   * Initialize the Datasource
   */

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
  public AnnotationReason createReason(int accountId, String reason) {

    AnnotationReason annotationReason = new AnnotationReason();

    try {
      // Execute the insert
      moderatorDao.create(accountId, reason);

      // Retrieve the created AnnotationReason object
      List<AnnotationReason> list;
      list = moderatorDao.getAnnotationReasons(accountId, reason);
      if (list != null && list.size() > 0) {
        annotationReason = list.get(0);
      }

    } catch (UnsupportedEncodingException ex) {
      logger.error(ex.getMessage(), ex);
      //throw new OpaRuntimeException(ex);
    }

    return annotationReason;
  }

  /**
   * Duplicate Reason value check
   * 
   * @param type
   *          Reason's type
   * @param event
   *          Reason's event
   * @param reason
   *          Reason's reason
   * @return isDuplicate (true/false)
   */
  @Override
  public boolean isDuplicateReason(int accountId, String reason) {

    try {
      // Retrieve the duplicate AnnotationReason object
      List<AnnotationReason> list;
      list = moderatorDao.getAnnotationReasons(accountId, reason);
      if (list != null && list.size() > 0) {
        return true;
      }

    } catch (UnsupportedEncodingException ex) {
      logger.error(ex.getMessage(), ex);
      //throw new OpaRuntimeException(ex);
    }

    return false;
  }

}
