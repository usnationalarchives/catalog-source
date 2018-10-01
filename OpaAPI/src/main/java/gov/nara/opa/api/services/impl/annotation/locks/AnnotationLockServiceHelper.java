package gov.nara.opa.api.services.impl.annotation.locks;

import gov.nara.opa.common.valueobject.annotation.lock.AnnotationLockValueObject;

import java.sql.Timestamp;
import java.util.Date;

import org.springframework.stereotype.Component;

@Component
public class AnnotationLockServiceHelper {

	public AnnotationLockValueObject createAnnotationLockValueObject(int accountId, 
			String naId, String objectId, String languageISO) {
		AnnotationLockValueObject lock = new AnnotationLockValueObject();
		lock.setAccountId(accountId);
		lock.setNaId(naId);
		lock.setObjectId(objectId);
		lock.setLanguageISO(languageISO);
		lock.setLockTS(new Timestamp(new Date().getTime()));
		return lock;
	}
}
