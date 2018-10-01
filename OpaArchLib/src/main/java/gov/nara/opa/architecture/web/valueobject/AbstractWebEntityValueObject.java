package gov.nara.opa.architecture.web.valueobject;

import java.util.Map;

/**
 * Super class for all value objects that are supposed to use an entities
 * represented in the Opa DB or search results: e.g. User, Tag, Transcription,
 * etc.
 * 
 * @author aolaru
 * @date Jun 9, 2014
 * 
 */
public abstract class AbstractWebEntityValueObject extends
    AbstractWebValueObject {

  public abstract Map<String, Object> getDatabaseContent();

}
