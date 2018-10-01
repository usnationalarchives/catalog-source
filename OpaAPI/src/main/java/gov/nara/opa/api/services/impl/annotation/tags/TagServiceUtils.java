package gov.nara.opa.api.services.impl.annotation.tags;

import gov.nara.opa.common.valueobject.annotation.logs.AnnotationLogValueObject;
import gov.nara.opa.common.valueobject.annotation.tags.Tag;

public class TagServiceUtils {

	public static AnnotationLogValueObject getNewAnnotationLogEntry(Tag tag,
			String action, String sessionId, boolean status) {
		AnnotationLogValueObject newLogEntry = new AnnotationLogValueObject();
		newLogEntry.setAnnotationType(Tag.ANNOTATION_TYPE);
		newLogEntry.setAnnotationId(tag.getAnnotationId());
		newLogEntry.setLanguageISO("");
		newLogEntry.setAnnotationMD5(tag.getAnnotationMD5());
		newLogEntry.setStatus(status);
		newLogEntry.setAccountId(tag.getAccountId());
		newLogEntry.setSessionId(sessionId);
		newLogEntry.setAction(action);
		newLogEntry.setNaId(tag.getNaId());
		newLogEntry.setObjectId(tag.getObjectId());
		// TODO: opa_id
		// TODO: notes

		return newLogEntry;
	}
}
