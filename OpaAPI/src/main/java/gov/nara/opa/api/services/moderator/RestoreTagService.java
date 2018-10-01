package gov.nara.opa.api.services.moderator;

import gov.nara.opa.common.validation.moderator.TagsModeratorRequestParameters;
import gov.nara.opa.common.valueobject.annotation.tags.TagValueObject;

import java.io.UnsupportedEncodingException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;

public interface RestoreTagService {

  public void restoreTag(TagsModeratorRequestParameters requestParameters,
      TagValueObject tag, String sessionId) throws BadSqlGrammarException, DataAccessException, UnsupportedEncodingException;
}
