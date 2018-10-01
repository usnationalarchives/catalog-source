package gov.nara.opa.api.services.annotation.tags;

import gov.nara.opa.api.services.ServiceResponseObject;
import gov.nara.opa.api.validation.annotation.tags.TagsViewRequestParameters;
import gov.nara.opa.common.valueobject.annotation.tags.TagsCollectionValueObject;

import java.io.UnsupportedEncodingException;

import org.springframework.dao.DataAccessException;

public interface ViewTagService {

  public ServiceResponseObject viewTagById(int annotationId);

  public TagsCollectionValueObject getTags(
      TagsViewRequestParameters tagsParameters);

  public TagsCollectionValueObject getTagsByNaIds(String[] naIdsList)
      throws DataAccessException, UnsupportedEncodingException;
}
