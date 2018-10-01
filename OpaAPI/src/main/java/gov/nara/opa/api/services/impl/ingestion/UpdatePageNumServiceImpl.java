package gov.nara.opa.api.services.impl.ingestion;

import gov.nara.opa.api.dataaccess.ingestion.UpdatePageNumDao;
import gov.nara.opa.api.services.ingestion.UpdatePageNumService;
import gov.nara.opa.architecture.logging.OpaLogger;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class UpdatePageNumServiceImpl implements UpdatePageNumService {

  private static OpaLogger logger = OpaLogger
      .getLogger(UpdatePageNumServiceImpl.class);

  SecureRandom random;

  @Autowired
  private UpdatePageNumDao updatePageNumDao;

  @Override
  public String updatePageNum(String naId, String objectId, int pageNum) {
    try {
      // if pageNum is -1, we delete the rows from the tables.
      if (pageNum == -1)
        return updatePageNumDao.delete(naId, objectId);
      else
        return updatePageNumDao.update(naId, objectId, pageNum);

    } catch (UnsupportedEncodingException ex) {
      logger.error(ex.getMessage(), ex);
    }
    return "";
  }

}
