package gov.nara.opa.api.services.impl.user.contributions;

import gov.nara.opa.api.annotation.TagErrorCode;
import gov.nara.opa.api.annotation.TranscriptedOpaTitle;
import gov.nara.opa.api.dataaccess.annotation.logs.AnnotationLogDao;
import gov.nara.opa.api.dataaccess.annotation.transcriptions.TranscriptionDao;
import gov.nara.opa.api.dataaccess.user.UserAccountDao;
import gov.nara.opa.api.services.ServiceResponseObject;
import gov.nara.opa.api.services.user.contributions.ViewOpaTitleService;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.common.dataaccess.annotation.tags.TagDao;
import gov.nara.opa.common.valueobject.annotation.tags.OpaTitle;

import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class ViewOpaTitleServiceImpl implements ViewOpaTitleService {

  private static OpaLogger logger = OpaLogger
      .getLogger(ViewOpaTitleServiceImpl.class);

  @Autowired
  private TagDao tagDao;

  @Autowired
  private TranscriptionDao transcriptionDao;

  @Autowired
  private AnnotationLogDao logDao;

  @Autowired
  private UserAccountDao administratorUserAccountDao;

  @Override
  public boolean isValidUserName(String userName) {
    return administratorUserAccountDao.verifyIfUserNameExists(userName);
  }
  
  @Override
  public ServiceResponseObject viewTaggedTitles(String tagText, String title,
      String userName, int offset, int rows) {
    return viewTaggedTitles(tagText, title, userName, offset, rows, true);
  }

  @Override
  public ServiceResponseObject viewTaggedTitles(String tagText, String title,
      String userName, int offset, int rows, boolean descOrder) {
    ServiceResponseObject results = new ServiceResponseObject();
    
    
    TagErrorCode errorCode = TagErrorCode.NONE;
    try {
      List<OpaTitle> opaTitles = tagDao.selectTaggedTitles(tagText, title,
          userName, offset, rows, descOrder);
      
      if (opaTitles == null) {
        errorCode = TagErrorCode.NO_TAGS_FOUND;
        errorCode.setErrorMessage("Tag not found");
      } else {
        //Get total count
        int tagCount = tagDao.selectTaggedTitleCount(tagText, title, userName);
        
        results.setErrorCode(errorCode);
        results.getContentMap().put("OpaTitles", opaTitles);
        results.getContentMap().put("TagCount", tagCount);
        
        return results;
      }

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      errorCode = TagErrorCode.INTERNAL_ERROR;
    }

    return new ServiceResponseObject(errorCode, null);
  }

  @Override
  public List<OpaTitle> getTitlesByNaIds(String[] naIdsList) {
    TagErrorCode errorCode = TagErrorCode.NONE;
    try {
      List<OpaTitle> opaTitles = tagDao.getTitlesByNaIds(naIdsList);
      if (opaTitles == null) {
        errorCode = TagErrorCode.NO_TAGS_FOUND;
        errorCode.setErrorMessage("Tag not found");
      } else {
        return opaTitles;
      }

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      errorCode = TagErrorCode.INTERNAL_ERROR;
    }

    return null;
  }

  @Override
  public ServiceResponseObject viewTranscriptedTitles(String title,
      String userName, int offset, int rows) {
    return viewTranscriptedTitles(title, userName, offset, rows, true);
  }
  
  
  @Override
  public ServiceResponseObject viewTranscriptedTitles(String title,
      String userName, int offset, int rows, boolean descOrder) {
    TagErrorCode errorCode = TagErrorCode.NONE;
    LinkedHashMap<String, Object> contentHashMap = new LinkedHashMap<String, Object>();
    ServiceResponseObject result = new ServiceResponseObject();

    
    try {
      int totalOpaTitles = transcriptionDao.selectTranscriptedTitleCount(title, userName);
      
      List<TranscriptedOpaTitle> opaTitles = transcriptionDao
          .selectTranscriptedTitles(title, userName, offset, rows, descOrder);
      if (opaTitles == null) {
        errorCode = TagErrorCode.NO_TAGS_FOUND;
        errorCode.setErrorMessage("Transcriptions ");
      } else {
        contentHashMap.put("Total", totalOpaTitles);
        contentHashMap.put("OpaTitles", opaTitles);
        result.setContentMap(contentHashMap);
        
      }

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      errorCode = TagErrorCode.INTERNAL_ERROR;
    }
    
    result.setErrorCode(errorCode);

    return result;
  }
}
