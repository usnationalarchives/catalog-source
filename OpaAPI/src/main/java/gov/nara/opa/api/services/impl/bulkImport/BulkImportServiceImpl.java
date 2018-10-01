package gov.nara.opa.api.services.impl.bulkImport;

import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import gov.nara.opa.api.annotation.TagErrorCode;
import gov.nara.opa.api.annotation.transcriptions.Transcription;
import gov.nara.opa.api.annotation.transcriptions.TranscriptionErrorCode;
import gov.nara.opa.api.dataaccess.annotation.transcriptions.TranscriptionDao;
import gov.nara.opa.api.security.OPAAuthenticationProvider;
import gov.nara.opa.api.services.ServiceResponseObject;
import gov.nara.opa.api.services.annotation.comments.CommentService;
import gov.nara.opa.api.services.annotation.tags.CreateTagsService;
import gov.nara.opa.api.services.annotation.transcriptions.CreateTranscriptionService;
import gov.nara.opa.api.services.bulkImport.BulkImportService;
import gov.nara.opa.api.services.impl.annotation.comments.CommentsHelper;
import gov.nara.opa.api.services.impl.annotation.transcriptions.TranscriptionHelper;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.utils.PageNumberUtils;
import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.valueobject.annotation.comments.CommentValueObject;
import gov.nara.opa.common.valueobject.annotation.tags.TagsCollectionValueObject;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;

@Component
@Transactional
public class BulkImportServiceImpl implements BulkImportService {

	private static OpaLogger log = OpaLogger
			.getLogger(BulkImportServiceImpl.class);

	@Value("${import.input.location}")
	private String importFilePath;

	@Autowired
	private CreateTagsService createTagService;

	@Autowired
	private CreateTranscriptionService createTranscriptionService;

	@Autowired
	private CommentService commentService;

	@Autowired
	private TranscriptionDao transcriptionDao;

	@Autowired
	private PageNumberUtils pageNumberUtils;

	@Autowired
	private TranscriptionHelper transcriptionHelper;

	@Autowired
	private CommentsHelper commentsHelper;

	@Override
	public void importTagsFromFile(String apiType, String sessionId,
			String fileName, ValidationResult validationResult) {
		String filePath = importFilePath + fileName;
		FileReader reader = null;

		try {
			reader = new FileReader(filePath);
		} catch (Exception e) {
			throw new OpaRuntimeException(e);
		}

		importTags(apiType, sessionId, reader, validationResult);
	}

	@Override
	public void importTags(String apiType, String sessionId, String bulkJson,
			ValidationResult validationResult) {
		// Load json into aspire object
		StringReader reader = new StringReader(bulkJson);

		importTags(apiType, sessionId, reader, validationResult);
	}

	@Override
	public void importTags(String apiType, String sessionId, Reader reader,
			ValidationResult validationResult) {
		log.trace("Starting tag import");

		try {
			AspireObject importAspireObject = AspireObject.createFromJson("",
					reader);

			List<AspireObject> tagsList = importAspireObject.getAll("tags");

			if (tagsList == null || tagsList.isEmpty()) {
				ValidationError error = new ValidationError();
				error.setErrorCode(TagErrorCode.CREATE_TAG_FAILED.toString());
				error.setErrorMessage(ErrorConstants.INVALID_TAG_JSON);

				validationResult.addCustomValidationError(error);
			}

			if (validationResult.isValid()) {
				TagsCollectionValueObject tagValueObjects = createTagService
						.createTags(apiType, sessionId, validationResult,
								tagsList);

				if (validationResult.isValid()
						&& (tagValueObjects == null || tagValueObjects
								.getTotalTags() == 0)) {
					ValidationError error = new ValidationError();
					error.setErrorCode(TagErrorCode.CREATE_TAG_FAILED
							.toString());
					error.setErrorMessage(ErrorConstants.EMPTY_TAG_JSON);

					validationResult.addCustomValidationError(error);
				}
			}
		} catch (AspireException e) {
			throw new OpaRuntimeException(e);
		}
	}

	@Override
	public void importTranscriptionsFromFile(String apiType, String sessionId,
			String fileName, String requestPath,
			ValidationResult validationResult) {
		String filePath = importFilePath + fileName;
		FileReader reader = null;

		try {
			reader = new FileReader(filePath);

		} catch (Exception e) {
			throw new OpaRuntimeException(e);
		}

		importTranscriptions(apiType, sessionId, reader, requestPath,
				validationResult);
	}

	@Override
	public void importTranscriptions(String apiType, String sessionId,
			String bulkJson, String requestPath,
			ValidationResult validationResult) {

		// Load json into aspire object
		StringReader reader = new StringReader(bulkJson);

		importTranscriptions(apiType, sessionId, reader, requestPath,
				validationResult);
	}

	@Override
	public void importTranscriptions(String apiType, String sessionId,
			Reader reader, String requestPath, ValidationResult validationResult) {
		log.trace("Starting transcription import");

		List<AspireObject> errorTranscriptions = new ArrayList<AspireObject>();
		LinkedHashMap<String, Transcription> transcriptionsToAdd = new LinkedHashMap<String, Transcription>();

		try {
			AspireObject importAspireObject = AspireObject.createFromJson("",
					reader);

			List<AspireObject> transcriptionList = importAspireObject
					.getAll("transcriptions");

			if (transcriptionList == null || transcriptionList.isEmpty()) {
				ValidationError error = new ValidationError();
				error.setErrorCode(TranscriptionErrorCode.CREATE_TRANSCRIPTION_FAILED
						.toString());
				error.setErrorMessage(ErrorConstants.INVALID_TRANSCRIPTION_JSON);

				validationResult.addCustomValidationError(error);
			}

			if (validationResult.isValid()) {
				int accountId = OPAAuthenticationProvider
						.getAccountIdForLoggedInUser();

				// Create transcription list
				for (AspireObject transcriptionAspireObject : transcriptionList) {
					// Get values
					String text = transcriptionAspireObject.getText("text");
					String naId = transcriptionAspireObject.getText("naId");
					String objectId = transcriptionAspireObject
							.getText("objectId");
					boolean validTranscription = true;

					if (StringUtils.isNullOrEmtpy(naId)
							|| StringUtils.isNullOrEmtpy(text)
							|| StringUtils.isNullOrEmtpy(objectId)) {
						errorTranscriptions.add(transcriptionAspireObject);
					} else {

						// Validate naId
						validTranscription = pageNumberUtils.isValidNaId(
								apiType, naId);
						if (!validTranscription) {
							ValidationError validationError = new ValidationError();
							validationError
									.setErrorCode(ArchitectureErrorCodeConstants.INVALID_ID_VALUE);
							validationError.setErrorMessage(String.format(
									ErrorConstants.INVALID_ID_VALUE_IMPORT,
									naId));
							validationError.setFieldValidationError(true);
							validationResult
									.addCustomValidationError(validationError);
						}

						// Get page number
						Integer pageNumber = 0;
						if (validTranscription) {
							pageNumber = pageNumberUtils.getPageNumber(apiType,
									naId, objectId);

							if (pageNumber == 0) {
								errorTranscriptions
										.add(transcriptionAspireObject);

								ValidationError error = new ValidationError();
								error.setErrorCode(TranscriptionErrorCode.CREATE_TRANSCRIPTION_FAILED
										.toString());
								error.setErrorMessage(String
										.format(ErrorConstants.INVALID_TRANSCRIPTION_PAGE_NUMBER_NAID_OBJECTID,
												naId, objectId));

								validationResult
										.addCustomValidationError(error);
							}
						}

						if (validTranscription) {
							// Create transcription
							Transcription transcription = transcriptionHelper
									.createTranscription(accountId, naId,
											objectId, text,
											pageNumber.toString(), requestPath);

							// Validate transcription text is not duplicate
							if (transcriptionDao.isTranscriptionDuplicate(naId,
									objectId, transcription.getAnnotationMD5())) {
								errorTranscriptions
										.add(transcriptionAspireObject);
							} else {
								String transcriptionKey = String.format(
										"%1$s-%2$s-%3$s", naId, objectId,
										transcription.getAnnotationMD5());
								if (transcriptionsToAdd
										.containsKey(transcriptionKey)) {
									errorTranscriptions
											.add(transcriptionAspireObject);
								} else {
									transcriptionsToAdd.put(transcriptionKey,
											transcription);
								}
							}
						}

					} // Create transcription list

				} // For each object in list

				// If there were no errors create transcriptions
				if (errorTranscriptions.size() == 0
						&& validationResult.isValid()) {

					// Cycle through transcriptions
					for (Transcription transcription : transcriptionsToAdd
							.values()) {

						ServiceResponseObject serviceResponse;

						// Lock naId
						serviceResponse = createTranscriptionService.lock(
								accountId, transcription.getNaId(),
								transcription.getObjectId());
						if (!serviceResponse.getErrorCode().toString()
								.equals("NONE")) {
							throw new Exception(serviceResponse.getErrorCode()
									.getErrorMessage());
						}

						// Save and unlock transcription
						serviceResponse = createTranscriptionService
								.saveAndUnlock(transcription, sessionId,
										accountId);
						if (!serviceResponse.getErrorCode().toString()
								.equals("NONE")) {
							throw new Exception(serviceResponse.getErrorCode()
									.getErrorMessage());
						}

					} // Transcription insertion

				} else if (validationResult.isValid()) {
					ValidationError error = new ValidationError();
					error.setErrorCode(TranscriptionErrorCode.CREATE_TRANSCRIPTION_FAILED
							.toString());
					error.setErrorMessage(ErrorConstants.INVALID_TRANSCRIPTIONS);
					validationResult.addCustomValidationError(error);
				}

			} // Initial validation

		} catch (Exception e) {
			throw new OpaRuntimeException(e);
		}
	}

	@Override
	public void importComments(String apiType, String sessionId,
			String bulkJson, String requestPath,
			ValidationResult validationResult) {
		// Load json into aspire object
		StringReader reader = new StringReader(bulkJson);

		importComments(apiType, sessionId, reader, requestPath,
				validationResult);
	}

	@Override
	public void importComments(String apiType, String sessionId, Reader reader,
			String requestPath, ValidationResult validationResult) {
		log.trace("Starting comments import");

		List<AspireObject> errorComments = new ArrayList<AspireObject>();

		try {
			AspireObject importAspireObject = AspireObject.createFromJson("",
					reader);

			List<AspireObject> commentList = importAspireObject
					.getAll("comments");

			if (commentList == null || commentList.isEmpty()) {
				ValidationError error = new ValidationError();
				// error.setErrorCode(CommentErrorCode.CREATE_COMMENT_FAILED
				// .toString());
				error.setErrorMessage(ErrorConstants.INVALID_ANNOTATION_JSON);

				validationResult.addCustomValidationError(error);
			}

			if (validationResult.isValid()) {
				UserAccountValueObject userAccount = commentsHelper
						.getSessionUserAccount();

				// Create transcription list
				for (AspireObject commentAspireObject : commentList) {
					// Get values
					String text = commentAspireObject.getText("text");
					String naId = commentAspireObject.getText("naId");
					String objectId = commentAspireObject.getText("objectId");
					List<AspireObject> replies = commentAspireObject
							.getAll("replies");
					boolean validComment = true;

					if (StringUtils.isNullOrEmtpy(naId)
							|| StringUtils.isNullOrEmtpy(text)) {
						errorComments.add(commentAspireObject);
					} else {

						// Validate naId
						validComment = pageNumberUtils.isValidNaId(apiType,
								naId);
						if (!validComment) {
							ValidationError validationError = new ValidationError();
							validationError
									.setErrorCode(ArchitectureErrorCodeConstants.INVALID_ID_VALUE);
							validationError.setErrorMessage(String.format(
									ErrorConstants.INVALID_ID_VALUE_IMPORT,
									naId));
							validationError.setFieldValidationError(true);
							validationResult
									.addCustomValidationError(validationError);
						}

						// Get page number
						Integer pageNumber = null;
						if (validComment && !StringUtils.isNullOrEmtpy(objectId)) {
							pageNumber = pageNumberUtils.getPageNumber(apiType,
									naId, objectId);

							if (pageNumber == null || pageNumber == 0) {
								errorComments.add(commentAspireObject);

								ValidationError error = new ValidationError();
								// error.setErrorCode(TranscriptionErrorCode.CREATE_TRANSCRIPTION_FAILED
								// .toString());
								error.setErrorMessage(String
										.format(ErrorConstants.INVALID_PAGE_NUMBER_NAID_OBJECTID,
												naId, objectId));

								validationResult
										.addCustomValidationError(error);
							}
						}

						if (validComment) {
							// Create comment
							CommentValueObject comment = commentService
									.createComment(userAccount, naId, objectId,
											text, pageNumber, sessionId);

							if (comment != null) {
								// add replies
								for (AspireObject replyAspireObject : replies) {
									String replyText = replyAspireObject
											.getText("text");
									if (!StringUtils.isNullOrEmtpy(replyText)) {
										commentService.replyComment(comment,
												userAccount, naId, objectId,
												replyText, pageNumber,
												sessionId);
									}
								}
							}
						}

					} // Create comment list

				} // For each object in list

				if (!validationResult.isValid()) {
					ValidationError error = new ValidationError();
					// error.setErrorCode(TranscriptionErrorCode.CREATE_TRANSCRIPTION_FAILED
					// .toString());
					error.setErrorMessage(ErrorConstants.INVALID_ANNOTATION_JSON);
					validationResult.addCustomValidationError(error);
				}
			} // Initial validation

		} catch (Exception e) {
			throw new OpaRuntimeException(e);
		}
	}

	@Override
	public void importCommentsFromFile(String apiType, String sessionId,
			String fileName, String requestPath,
			ValidationResult validationResult) {
		String filePath = importFilePath + fileName;
		FileReader reader = null;

		try {
			reader = new FileReader(filePath);

		} catch (Exception e) {
			throw new OpaRuntimeException(e);
		}

		importComments(apiType, sessionId, reader, requestPath,
				validationResult);
	}
}
