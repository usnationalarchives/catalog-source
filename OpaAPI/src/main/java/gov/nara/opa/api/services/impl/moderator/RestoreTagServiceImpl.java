package gov.nara.opa.api.services.impl.moderator;

import gov.nara.opa.api.dataaccess.annotation.logs.AnnotationLogDao;
import gov.nara.opa.api.security.OPAAuthenticationProvider;
import gov.nara.opa.api.services.moderator.RestoreTagService;
import gov.nara.opa.architecture.web.valueobject.CommonValueObjectConstants;
import gov.nara.opa.common.dataaccess.annotation.tags.TagDao;
import gov.nara.opa.common.validation.moderator.TagsModeratorRequestParameters;
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
public class RestoreTagServiceImpl implements RestoreTagService {

  @Autowired
  TagDao tagDao;

  @Autowired
  AnnotationLogDao annotationLogDao;

  @Override
  public void restoreTag(TagsModeratorRequestParameters requestParameters,
      TagValueObject tag, String sessionId) throws BadSqlGrammarException, DataAccessException, UnsupportedEncodingException {

    tagDao.updateTagStatus(tag.getAnnotationId(), true);
    tag.setStatus(true);
    AnnotationLogValueObject annotationLog = null;
	try {
		annotationLog = AnnotationLogValueObjectHelper
		    .createAnnotationLogForInsert(tag, requestParameters, sessionId,
		        CommonValueObjectConstants.ACTION_RESTORE,
		        OPAAuthenticationProvider.getAccountIdForLoggedInUser());
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    annotationLogDao.insert(annotationLog);
  }
}
