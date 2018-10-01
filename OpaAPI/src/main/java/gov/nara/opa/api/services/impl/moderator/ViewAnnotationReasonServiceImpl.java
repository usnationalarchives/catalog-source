package gov.nara.opa.api.services.impl.moderator;

import gov.nara.opa.api.dataaccess.moderator.ModeratorDao;
import gov.nara.opa.api.moderator.AnnotationReason;
import gov.nara.opa.api.services.moderator.ViewAnnotationReasonService;
import gov.nara.opa.api.services.system.ConnectionManager;
import gov.nara.opa.api.services.user.lists.ViewUserListService;
import gov.nara.opa.architecture.logging.OpaLogger;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Component
public class ViewAnnotationReasonServiceImpl implements
    ViewAnnotationReasonService {

  private static OpaLogger logger = OpaLogger
      .getLogger(ViewAnnotationReasonServiceImpl.class);

  SecureRandom random;

  @Autowired
  private ConnectionManager connectionManager;

  @Autowired
  private ModeratorDao moderatorDao;

  @Autowired
  private ViewUserListService viewUserListService;

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
  public List<AnnotationReason> viewAnnotationReasons(int accountId,
      String reason) {

    List<AnnotationReason> list = null;

    try {
      // Retrieve the AnnotationReasons object
      list = moderatorDao.getAnnotationReasons(accountId, reason);

    } catch (UnsupportedEncodingException ex) {
      logger.error(ex.getMessage(), ex);
      //throw new OpaRuntimeException(ex);
    }

    return list;
  }

}
