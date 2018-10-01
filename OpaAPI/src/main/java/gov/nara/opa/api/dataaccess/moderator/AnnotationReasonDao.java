package gov.nara.opa.api.dataaccess.moderator;

import gov.nara.opa.api.valueobject.moderator.AnnotationReasonValueObject;

public interface AnnotationReasonDao {

  AnnotationReasonValueObject getAnnotationReasonById(Integer reasonId);

}
