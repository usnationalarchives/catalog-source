package gov.nara.opa.api.services.moderator;

import gov.nara.opa.api.moderator.AnnotationReason;

import java.util.List;

/**
 * Interface for the AnnotationReason service that handles requests from the
 * controller
 */
public interface ViewAnnotationReasonService {

  public List<AnnotationReason> viewAnnotationReasons(int accountId,
      String reason);

}
