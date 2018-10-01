package gov.nara.opa.api.services.annotation.locks;

import gov.nara.opa.api.validation.annotation.locks.AnnotationLockLanguageRequestParameters;
import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.common.valueobject.annotation.lock.AnnotationLockValueObject;

public interface AnnotationLockService {

	public AnnotationLockValueObject create(AnnotationLockLanguageRequestParameters annotationLock) 
			throws OpaRuntimeException;

	public AnnotationLockValueObject getLock(String naId, String objectId,
		      String languageISO);

	public boolean delete(AnnotationLockLanguageRequestParameters annotationLock) 
			throws OpaRuntimeException;

	public boolean delete(String naId, String objectId,
			String languageISO, int accountId);

	public boolean validateLock(int accountId, String naId, 
			String objectId, String languageISO);

	 public boolean canLock(String naId, String objectId,
			 String languageISO); 
}