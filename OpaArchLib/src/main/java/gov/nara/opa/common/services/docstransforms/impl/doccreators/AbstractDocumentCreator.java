package gov.nara.opa.common.services.docstransforms.impl.doccreators;

import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.utils.TimestampUtils;
import gov.nara.opa.common.dataaccess.annotation.comments.CommentsDao;
import gov.nara.opa.common.storage.OpaStorage;
import gov.nara.opa.common.storage.OpaStorageFactory;
import gov.nara.opa.common.valueobject.annotation.comments.CommentValueObject;
import gov.nara.opa.common.valueobject.annotation.comments.CommentsCollectionValueObject;
import gov.nara.opa.common.valueobject.annotation.tags.TagValueObject;
import gov.nara.opa.common.valueobject.annotation.transcriptions.TranscriptionValueObject;
import gov.nara.opa.common.valueobject.export.AccountExportStatusEnum;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.common.valueobject.export.DigitalObjectValueObject;
import gov.nara.opa.common.valueobject.export.ValueHolderValueObject;
import gov.nara.opa.common.valueobject.search.SearchRecordValueObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.text.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public abstract class AbstractDocumentCreator implements DocumentCreator {
	private static OpaLogger logger = OpaLogger.getLogger(AbstractDocumentCreator.class);

	private static final Map<String, String> TRANSFORMED_LABELS = new ConcurrentHashMap<String, String>();

	public static final String PADDING_TO_LEFT_STRING = "            "; // 12
	// chars
	public static final int CHARS_PADDING_TO_LEFT = PADDING_TO_LEFT_STRING.length();

	@Value("${naraBaseUrl}")
	String naraBaseUrl;

	@Autowired
	private CommentsDao commentsDao;

	@Autowired
	private OpaStorageFactory opaStorageFactory;

	protected int getMaxLineLength() {
		return 0;
	}

	protected String getSeparator(int documentIndex) {
		return String.format("=============================== Result %1$d ===============================\n\n",
				documentIndex);
	}

	protected LinkedList<ValueHolderValueObject> flattenValue(LinkedList<ValueHolderValueObject> values) {
		// TEST LOG
		logger.debug("Flattening values");

		LinkedList<ValueHolderValueObject> flattenedValues = new LinkedList<ValueHolderValueObject>();
		populateFlattenedValues(values, flattenedValues);
		return flattenedValues;
	}

	private void populateFlattenedValues(List<ValueHolderValueObject> values, List<ValueHolderValueObject> newValues) {
		for (ValueHolderValueObject value : values) {
			if (value.getValue() != null) {
				newValues.add(value);
			} else if (value.getArrayOfObjectsValue() != null) {
				populateFlattenedValues(value.getArrayOfObjectsValue(), newValues);
			}
		}
	}

	protected String getLabel(String labelName) {
		String transformedLabel = TRANSFORMED_LABELS.get(labelName);
		if (transformedLabel != null) {
			return transformedLabel;
		}
		if (labelName.equals("")) {
			transformedLabel = "";
		} else {
			transformedLabel = labelName + ": ";
			if (transformedLabel.length() < CHARS_PADDING_TO_LEFT) {
				int origTransformedLabelLength = transformedLabel.length();
				for (int i = 0; i < CHARS_PADDING_TO_LEFT - origTransformedLabelLength; i++) {
					transformedLabel = transformedLabel + " ";
				}
			}
		}
		TRANSFORMED_LABELS.put(labelName, transformedLabel);
		return transformedLabel;
	}

	protected void populatePublicContributions(List<ValueHolderValueObject> allValues, SearchRecordValueObject document,
			AccountExportValueObject accountExport) {
		
		long LONG_PROCESS_MILLIS = 5000;
		long LONGER_PROCESS_MILLIS = 7000;
		long startTime = 0;
		long endTime = 0;
		
		int total = 0;
		
		if (accountExport.getRequestStatus() == AccountExportStatusEnum.TIMEDOUT ) {
			return;
		}
		
		if (accountExport.getIncludeTags()) {
			// TEST LOG
			logger.debug(String.format("Adding tags to record %1$s", document.getOpaId()));

			startTime = new Date().getTime();
			populateTags(allValues, document);
			endTime = new Date().getTime();
			if(endTime - startTime > LONG_PROCESS_MILLIS) {
				logger.info(String.format("ExportId:[%1$d] Tag inclusion took more than [%2$s] millisecs: [%3$d]", accountExport.getExportId(), LONG_PROCESS_MILLIS, endTime - startTime));
			}
		}

		
		if (accountExport.getRequestStatus() == AccountExportStatusEnum.TIMEDOUT ) {
			return;
		}
		
		
		if (accountExport.getIncludeTranscriptions()) {
			// TEST LOG
			logger.debug(String.format("Adding transcriptions to record %1$s", document.getOpaId()));

			startTime = new Date().getTime();
			populateTranscriptions(allValues, document);
			endTime = new Date().getTime();
			if(endTime - startTime > LONG_PROCESS_MILLIS) {
				logger.info(String.format("ExportId:[%1$d] Transcription inclusion took more than [%2$s] millisecs: [%3$d]", accountExport.getExportId(), LONG_PROCESS_MILLIS, endTime - startTime));
			}
		}

		if (accountExport.getRequestStatus() == AccountExportStatusEnum.TIMEDOUT ) {
			return;
		}
		
		if (accountExport.getIncludeComments()) {
			// TEST LOG
			logger.debug(String.format("Adding comments to record %1$s", document.getOpaId()));

			startTime = new Date().getTime();
			total = populateComments(allValues, document);
			endTime = new Date().getTime();
			if(endTime - startTime > LONG_PROCESS_MILLIS) {
				logger.info(String.format("ExportId:[%1$d] Comment inclusion took more than [%2$s] millisecs: [%3$d] - total: [%4$d]", accountExport.getExportId(), LONGER_PROCESS_MILLIS, endTime - startTime, total));
			}
		}
		logger.debug(String.format("Finished annotations for record %1$s", document.getOpaId()));
	}

	protected void populateThumbnails(List<ValueHolderValueObject> allValues, SearchRecordValueObject document,
			AccountExportValueObject accountExport) {

		if (!accountExport.getIncludeThumbnails() || document.getObjects() == null
				|| document.getObjects().size() == 0) {
			return;
		}

		ValueHolderValueObject tagsValueHolder = new ValueHolderValueObject();

		tagsValueHolder.setLabel("Thumbnails");

		ArrayList<AspireObject> thumbnails = new ArrayList<AspireObject>();

		for (DigitalObjectValueObject digitalObject : document.getObjects().values()) {
			if (digitalObject.getThumbnailPath() != null) {
				String thumbnailPath = getFullUrl(digitalObject.getThumbnailPath(), document, null);

				AspireObject thumbnail = new AspireObject("object");
				String caption = null;
				if (digitalObject.getDesignator() != null && digitalObject.getDescription() != null) {
					caption = digitalObject.getDesignator() + ", " + digitalObject.getDescription();
				} else {
					caption = (digitalObject.getDesignator() != null) ? digitalObject.getDesignator()
							: digitalObject.getDescription();
				}
				try {
					thumbnail.add("url", thumbnailPath);
					thumbnail.add("caption", caption);
				} catch (AspireException ae) {
					logger.error("Cannot add thumbnail to export response. Caused by " + ae);
				}
				thumbnails.add(thumbnail);
			}
		}
		tagsValueHolder.setValue(thumbnails);
		allValues.add(tagsValueHolder);
	}

	protected String getFullUrl(String partialThumbnailPath, SearchRecordValueObject document, String baseKey) {
		logger.debug("in getFullUrl(); \n\tpartialThumbnailPath: "+partialThumbnailPath+"\n\tbaseKey: "+baseKey);
		String naId = document.getNaId();
		if(StringUtils.isNullOrEmtpy(naId)) {
			naId = document.getParentDescriptionNaId();
		}
		OpaStorage storage = opaStorageFactory.createOpaStorage();
		String legacyObjectKey = naraBaseUrl + "OpaAPI/media/" + naId + "/" + partialThumbnailPath;
		logger.debug("\tLegacy Object Key: "+legacyObjectKey);
		if(baseKey != null){
			String newObjectKey = baseKey;

			if (newObjectKey.startsWith("/")) {
				newObjectKey = newObjectKey.substring(1);
			}

			if (newObjectKey.startsWith("content")) {
				newObjectKey = newObjectKey.replaceFirst("content", "lz");
			}
			// if this is the primary file, ignore the partial path
			// renditions use primary file key as base, and prefix of live/ in place
			// of lz/
			if (!baseKey.isEmpty() && !baseKey.equals(partialThumbnailPath)) {
				newObjectKey = newObjectKey.replaceFirst("lz","live") + "/" + partialThumbnailPath;
			}

			logger.debug("\tCHECKING key: "+newObjectKey);
			if (storage.exists(newObjectKey)) {
				logger.debug("\t\tfound " + newObjectKey + " in ");
				newObjectKey = naraBaseUrl + "catalogmedia/" + newObjectKey;
				logger.debug("\tNew Object Key: "+newObjectKey);
			}
		}
		return legacyObjectKey;

	}

	protected void populateTranscriptions(List<ValueHolderValueObject> allValues, SearchRecordValueObject document) {

		int noOfObjects = document.getObjects().size(), index = 1;
		for (DigitalObjectValueObject digitalObject : document.getObjects().values()) {

			List<TranscriptionValueObject> objectTranscriptions = digitalObject.getTranscriptions();
			if (objectTranscriptions != null && objectTranscriptions.size() > 0) {
				ValueHolderValueObject transcriptionValueHolder = new ValueHolderValueObject();
				transcriptionValueHolder
						.setLabel(String.format("Transcription for Image %1$d out of %2$d", index, noOfObjects));
				transcriptionValueHolder.setValue(getTranscriptionText(objectTranscriptions));

				// TEST LOG
				try {
					String transcriptionText = ((String) transcriptionValueHolder.getValue());
					if(transcriptionText != null && transcriptionText.length() > 10) {
						transcriptionText = transcriptionText.substring(0, 10);
					}
					logger.debug(String.format("Adding transcription to %1$s with data %2$s", document.getOpaId(),
							transcriptionText));
				} catch(Exception ex) {
					logger.error(ex);
				}

				allValues.add(transcriptionValueHolder);
				index++;
			}
		}
	}

	private String getTranscriptionText(List<TranscriptionValueObject> transcriptions) {
		TranscriptionValueObject transcription = transcriptions.get(transcriptions.size() - 1);
		return transcription.getAnnotation();
	}

	protected synchronized int populateComments(List<ValueHolderValueObject> allValues,
			SearchRecordValueObject document) {

		int totalComments = 0;
		
		logger.debug(String.format("Size of allValues before executing populate comments %1$d", allValues.size()));
		logger.debug(String.format("Getting comments and replies %1$s for naId", document.getNaId()));
		CommentsCollectionValueObject descriptionComments = commentsDao.selectAllCommentsAndReplies(document.getNaId(),
				null);
		if (descriptionComments != null && descriptionComments.getTotalCommentsIncludingRemoves() > 0) {
			ValueHolderValueObject baseCommentsValueHolder = new ValueHolderValueObject();
			baseCommentsValueHolder.setLabel("Comments(s)");
			String comments = getCommentText(descriptionComments);
			baseCommentsValueHolder.setValue(comments);

			if (!comments.trim().isEmpty()) {
				allValues.add(baseCommentsValueHolder);
			}
		}

		int noOfObjects = document.getObjects().size(), index = 1;

		List<DigitalObjectValueObject> objects = new ArrayList<DigitalObjectValueObject>(
				document.getObjects().values());
		/* Send non jpeg images to the end */
		List<DigitalObjectValueObject> objectsWithDocs = new ArrayList<DigitalObjectValueObject>();
		List<DigitalObjectValueObject> removedDocs = new ArrayList<DigitalObjectValueObject>();
		for (DigitalObjectValueObject digitalObject : objects) {
			//Fix for NARA-2687 - the same logic should apply for all digital objects
			//regardlesss if thet are jpg or not. The filePath is not retrieved from solr 
			//when no metadata is exported - as such the the next line failed with a NullPointerException.
			if ((digitalObject.getFilePath() != null && !digitalObject.getFilePath().endsWith("jpg")) || digitalObject.getFilePath() == null ) {
				objectsWithDocs.add(digitalObject);
				removedDocs.add(digitalObject);
			}
		}
		logger.debug(String.format("Comments: updating objects %1$s", document.getOpaId()));
		objects.removeAll(removedDocs);
		objects.addAll(objectsWithDocs);

		logger.debug(String.format("Getting comments for objects %1$s, total: %2$d", document.getOpaId(), objects.size()));
		totalComments = objects.size();
		for (DigitalObjectValueObject digitalObject : objects) {
			CommentsCollectionValueObject objectComments = commentsDao.selectAllCommentsAndReplies(document.getNaId(),
					digitalObject.getId());
			logger.debug(String.format("Comments retrieved %1$s - %3$s total: %2$d", document.getOpaId(), objectComments.getComments().size(), digitalObject.getId()));
			if (objectComments != null && objectComments.getTotalCommentsIncludingRemoves() > 0) {
				ValueHolderValueObject commentsValueHolder = new ValueHolderValueObject();
				commentsValueHolder.setLabel(String.format("Comments for Image %1$d out of %2$d", index, noOfObjects));

				String comments = getCommentText(objectComments);
				if (comments.trim().isEmpty()) {
					continue;
				}
				commentsValueHolder.setValue(comments);
				allValues.add(commentsValueHolder);
				index++;
			}
		}
		logger.debug(String.format("Finished comments for objects %1$s", document.getOpaId()));
		logger.debug(String.format("Size of allValues after executing populate comments %1$d", allValues.size()));
		
		return totalComments;
		
	}

	private String getCommentText(CommentsCollectionValueObject objectComments) {
		StringBuilder tmp = new StringBuilder();
		tmp.append("\n");
		int index = 1;
		String user = null;
		for (CommentValueObject cmm : objectComments.getComments()) {
			if (cmm.getDisplayNameFlag() != null && cmm.getDisplayNameFlag()) {
				user = cmm.getFullName();
				if (cmm.getIsNaraStaff() != null && cmm.getIsNaraStaff()) {
					user += " (NARA Staff)";
				}
			} else {
				user = cmm.getUserName();
			}
//			if (!cmm.getStatus() && cmm.getReplies().size() == 0) {
//				continue;
//			}
			tmp.append(index + ". " + cmm.getAnnotation() + " - " + user + " - "
					+ TimestampUtils.getUtcString(cmm.getAnnotationTS()));
			tmp.append("\n");
			List<CommentValueObject> replies = cmm.getReplies();
			if (replies != null && replies.size() > 0) {
				for (int i = 0; i < replies.size(); ++i) {
					if (replies.get(i).getDisplayNameFlag() != null && replies.get(i).getDisplayNameFlag()) {
						user = replies.get(i).getFullName();
					} else {
						user = replies.get(i).getUserName();
					}
					tmp.append(PADDING_TO_LEFT_STRING + PADDING_TO_LEFT_STRING + index + "." + (i + 1) + ". "
							+ replies.get(i).getAnnotation() + " - " + user + " - "
							+ TimestampUtils.getUtcString(replies.get(i).getAnnotationTS()));
					tmp.append("\n");
				}
				tmp.append("\n");
			}
			index++;
		}

		tmp.append("\n");
		return tmp.toString();
	}

	protected void populateTags(List<ValueHolderValueObject> allValues, SearchRecordValueObject document) {
		List<TagValueObject> descriptionTags = document.getTags();

		if (descriptionTags != null && descriptionTags.size() > 0) {
			// TEST LOG
			logger.debug(String.format("Adding description level tags to %1$s", document.getOpaId()));

			ValueHolderValueObject tagsValueHolder = new ValueHolderValueObject();
			tagsValueHolder.setLabel("Tag(s)");
			tagsValueHolder.setValue(getTagsText(descriptionTags));
			allValues.add(tagsValueHolder);
		}

		int noOfObjects = document.getObjects().size(), index = 1;
		for (DigitalObjectValueObject digitalObject : document.getObjects().values()) {
			List<TagValueObject> objectTags = digitalObject.getTags();
			if (objectTags != null && objectTags.size() > 0) {
				ValueHolderValueObject tagsValueHolder = new ValueHolderValueObject();
				tagsValueHolder.setLabel(String.format("Tag(s) for Image %1$d out of %2$d", index, noOfObjects));
				tagsValueHolder.setValue(getTagsText(objectTags));
				allValues.add(tagsValueHolder);
				index++;
			}
		}
	}

	private String getTagsText(List<TagValueObject> tags) {
		StringBuilder sb = new StringBuilder();
		int index = 1, size = tags.size();
		for (TagValueObject tag : tags) {
			sb.append(tag.getAnnotation());
			if (index < size) {
				sb.append(", ");
			}
			index++;
		}
		return sb.toString();
	}

	protected void appendRecord(ValueHolderValueObject record, StringBuilder sb) {
		if (record.getLabel() != null) {
			sb.append(getLabel(record.getLabel()));
			appendTextValue(record.getValue(), sb, true, getLabel(record.getLabel()).length());
		} else {
			logger.info(String.format("Invalid label in record: %1$s", record.toString()));
		}
	}

	private void appendTextValue(Object value, StringBuilder sb, boolean afterTag, int labelLength) {
		if (value == null || value.equals("")) {
			sb.append(makeLine("", afterTag));
			return;
		}
		if (value instanceof List) {
			appendListTextValue((List<?>) value, sb, afterTag);
			return;
		}
		String[] lines = (value.toString()).split("\n");
		if (lines.length > 1) {
			appendArrayTextValue(lines, sb, afterTag);
			return;
		}
		String[] wrappedLines = WordUtils.wrap(value.toString(), getMaxLineLength() - labelLength, "\n", false)
				.split("\n");
		if (wrappedLines.length > 1) {
			appendArrayTextValue(wrappedLines, sb, afterTag);
		} else {
			sb.append(makeLine(wrappedLines[0], afterTag));
		}
	}

	private String makeLine(String line, boolean afterTag) {
		if (afterTag) {
			return line + "\n";
		} else {
			return PADDING_TO_LEFT_STRING + line + "\n";
		}
	}

	@SuppressWarnings("unchecked")
	private void appendListTextValue(List<?> values, StringBuilder sb, boolean afterTag) {
		boolean afterTagLocal = afterTag;
		try {
			for (String stringValue : (List<String>) values) {
				appendTextValue(stringValue, sb, afterTagLocal, 0);
				afterTagLocal = false;
			}
			return;
		} catch (ClassCastException cce) {
		}
		try {
			for (AspireObject ao : (ArrayList<AspireObject>) values) {
				appendTextValue(ao.getContent("url") + "\n" + ao.getContent("caption"), sb, afterTagLocal, 0);
				afterTagLocal = false;
			}
		} catch (AspireException ae) {
			logger.error("An error occurred creating text export file. Caused by " + ae);
		}
	}

	private void appendArrayTextValue(String[] stringValues, StringBuilder sb, boolean afterTag) {
		boolean afterTagLocal = afterTag;
		for (String stringValue : stringValues) {
			appendTextValue(stringValue, sb, afterTagLocal, 0);
			afterTagLocal = false;
		}
	}
}