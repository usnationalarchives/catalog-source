package gov.nara.opa.api.services.impl.annotation.transcriptions;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.nara.opa.api.annotation.transcriptions.Transcription;
import gov.nara.opa.api.utils.PageNumberUtils;

@Component
public class TranscriptionHelper {

	@Autowired
	private PageNumberUtils pageNumberUtils;

	public Transcription createTranscription(int accountId, String naId,
			String objectId, String text, String pageNumber, String requestPath) {
		// Create transcription instance
		Transcription transcription = new Transcription();
		transcription.setAccountId(accountId);
		transcription.setNaId(naId);
		transcription.setObjectId(objectId);
		transcription.setAnnotation(text);
		if (pageNumber != null && !pageNumber.isEmpty()) {
			transcription.setPageNum(Integer.parseInt(pageNumber));
		} else {
			if (!requestPath.contains("iapi")) {
				int pageNum = getPageNum("api", naId, objectId);
				transcription.setPageNum(pageNum);
			}
		}
		transcription.setStatus(true);
		transcription.setAnnotationMD5(DigestUtils.md5Hex(text));

		return transcription;
	}

	public int getPageNum(String apiType, String naId, String objectId) {
		// Get page number

		return pageNumberUtils.getPageNumber(apiType, naId, objectId);
	}

}
