package gov.nara.opa.api.services.bulkImport;

import gov.nara.opa.architecture.web.validation.ValidationResult;

import java.io.Reader;

public interface BulkImportService {

	// Tags

	void importTags(String apiType, String sessionId, String bulkJson,
			ValidationResult validationResult);

	void importTags(String apiType, String sessionId, Reader reader,
			ValidationResult validationResult);

	void importTagsFromFile(String apiType, String sessionId, String fileName,
			ValidationResult validationResult);

	// Transcriptions

	void importTranscriptions(String apiType, String sessionId,
			String bulkJson, String requestPath,
			ValidationResult validationResult);

	void importTranscriptions(String apiType, String sessionId, Reader reader,
			String requestPath, ValidationResult validationResult);

	void importTranscriptionsFromFile(String apiType, String sessionId,
			String fileName, String requestPath,
			ValidationResult validationResult);

	// Comments

	void importComments(String apiType, String sessionId, String bulkJson,
			String requestPath, ValidationResult validationResult);

	void importComments(String apiType, String sessionId, Reader reader,
			String requestPath, ValidationResult validationResult);

	void importCommentsFromFile(String apiType, String sessionId,
			String fileName, String requestPath,
			ValidationResult validationResult);
}
