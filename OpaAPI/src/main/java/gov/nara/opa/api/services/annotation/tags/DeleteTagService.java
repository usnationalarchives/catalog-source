package gov.nara.opa.api.services.annotation.tags;

import java.io.UnsupportedEncodingException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;

import gov.nara.opa.common.valueobject.annotation.tags.TagValueObject;

public interface DeleteTagService {
  void deleteTag(TagValueObject tag, String sessionId) throws BadSqlGrammarException, DataAccessException, UnsupportedEncodingException;
}
