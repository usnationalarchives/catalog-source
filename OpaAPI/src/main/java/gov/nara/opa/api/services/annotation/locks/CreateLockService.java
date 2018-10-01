package gov.nara.opa.api.services.annotation.locks;

import gov.nara.opa.api.annotation.locks.AnnotationLock;
import gov.nara.opa.api.services.ServiceResponseObject;

public interface CreateLockService {

  /**
   * Creates a lock for the provided lock instance
   * 
   * @param annotationLock
   * @return A response object with a valid error if unable to comply or a
   *         LinkedHashMap with AnnotationLock as key for the lock instance,
   *         UserAccount as key for the locking user.
   */
  public ServiceResponseObject create(AnnotationLock annotationLock);
  

}
