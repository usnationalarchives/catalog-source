package gov.nara.opa.api.services.annotation.tags;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;

import com.searchtechnologies.aspire.services.AspireObject;

import gov.nara.opa.api.validation.annotation.tags.TagsCreateRequestParameters;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.valueobject.annotation.tags.TagsCollectionValueObject;

public interface CreateTagsService {

  public TagsCollectionValueObject createTags(
      TagsCreateRequestParameters tagsRequest) throws BadSqlGrammarException, DataAccessException, UnsupportedEncodingException;
  
  public TagsCollectionValueObject createTags(String apiType, String sessionId, ValidationResult validationResult,
      List<AspireObject> aspireObjectList);

}
