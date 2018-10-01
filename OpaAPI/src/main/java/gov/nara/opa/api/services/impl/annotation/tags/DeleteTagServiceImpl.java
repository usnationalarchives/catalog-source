package gov.nara.opa.api.services.impl.annotation.tags;

import gov.nara.opa.api.dataaccess.annotation.logs.AnnotationLogDao;
import gov.nara.opa.api.services.annotation.tags.DeleteTagService;
import gov.nara.opa.architecture.web.valueobject.CommonValueObjectConstants;
import gov.nara.opa.common.dataaccess.annotation.tags.TagDao;
import gov.nara.opa.common.valueobject.annotation.logs.AnnotationLogValueObject;
import gov.nara.opa.common.valueobject.annotation.logs.AnnotationLogValueObjectHelper;
import gov.nara.opa.common.valueobject.annotation.tags.TagValueObject;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class DeleteTagServiceImpl implements DeleteTagService {

  @Autowired
  TagDao tagDao;

  @Autowired
  AnnotationLogDao annotationLogDao;

  @Override
  public void deleteTag(TagValueObject tag, String sessionId) throws BadSqlGrammarException, DataAccessException, UnsupportedEncodingException {
    tagDao.updateTagStatus(tag.getAnnotationId(), false);
    tag.setStatus(false);

    // Deactivate existing logs
    // List<AnnotationLogValueObject> activeLogs = annotationLogDao
    // .getAnnotationLogs("TG", tag.getNaId(), tag.getObjectId(), true);
    // for (AnnotationLogValueObject log : activeLogs) {
    // annotationLogDao.disableByAnnotationId(log.getAnnotationId(), "TG");
    // }

    try {
		annotationLogDao.disableByAnnotationId(tag.getAnnotationId(), "TG");
	} catch (Exception e) {
		e.printStackTrace();
	}

    AnnotationLogValueObject annotationLog = AnnotationLogValueObjectHelper
        .createAnnotationLogForInsert(tag, sessionId,
            CommonValueObjectConstants.ACTION_DELETE);
    annotationLogDao.insert(annotationLog);
  }

}
