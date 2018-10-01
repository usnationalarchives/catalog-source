package gov.nara.opa.common.services.docstransforms.impl.doccreators;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.utils.TimestampUtils;
import gov.nara.opa.architecture.web.controller.aspirehelper.HeaderAspireObjectContentHolder;
import gov.nara.opa.architecture.web.controller.aspirehelper.SuccessResponseAspireObjectContentHolder;
import gov.nara.opa.common.dataaccess.annotation.comments.CommentsDao;
import gov.nara.opa.common.dataaccess.annotation.tags.TagDao;
import gov.nara.opa.common.services.docstransforms.Constants;
import gov.nara.opa.common.validation.search.ResultTypesValidator;
import gov.nara.opa.common.storage.OpaStorage;
import gov.nara.opa.common.storage.OpaStorageFactory;
import gov.nara.opa.common.storage.StorageUtils;
import gov.nara.opa.common.valueobject.annotation.comments.CommentValueObject;
import gov.nara.opa.common.valueobject.annotation.comments.CommentsCollectionValueObject;
import gov.nara.opa.common.valueobject.annotation.tags.TagValueObject;
import gov.nara.opa.common.valueobject.annotation.transcriptions.TranscriptionValueObject;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.common.valueobject.export.AccountExportValueObjectHelper;
import gov.nara.opa.common.valueobject.export.DigitalObjectValueObject;
import gov.nara.opa.common.valueobject.export.ValueHolderValueObject;
import gov.nara.opa.common.valueobject.search.SearchRecordValueObject;
import gov.nara.opa.common.valueobject.search.SolrQueryResponseValueObject;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;

public abstract class AbstractAspireObjectDocumentCreator extends AbstractDocumentCreator
		implements DocumentCreator, Constants {

	static OpaLogger logger = OpaLogger.getLogger(AbstractAspireObjectDocumentCreator.class);

	@Autowired
	private CommentsDao commentsDao;

	@Autowired
	private TagDao tagsDao;

	@Autowired
	OpaStorageFactory opaStorageFactory;

	public static final String AO_PADDING_KEYWORD = "paddingLevel";

	public String createDocument(LinkedList<ValueHolderValueObject> records, OutputStream outputStream,
			int documentIndex, String format, AccountExportValueObject accountExport, SearchRecordValueObject document)
					throws IOException {
		try {
			AspireObject ao = new AspireObject("naraitem");
			ao.add("documentIndex", new Integer(documentIndex));
			for (ValueHolderValueObject record : records) {
				if (record.getValue() != null) {
					addFieldToAspireObject(ao, record.getName(), record.getValue(), format);
				} else if (record.getArrayOfObjectsValue() != null) {
					complexListOfValueHoders(ao, record, record.getArrayOfObjectsValue(), format);
				}

			}
			addObjectsContent(ao, accountExport, document);
			populateDocPublicContributions(document, accountExport, ao);

			if (EXPORT_FORMAT_JSON.equals(format)) {
				return ao.toJsonString(false);
			} else if (EXPORT_FORMAT_XML.equals(format)) {
				return ao.toXmlString(false);
			} else {
				throw new OpaRuntimeException(
						"Invalid format requested to be handled by AspireObjectDocumentCreator: " + format);
			}
		} catch (AspireException e) {
			throw new OpaRuntimeException(e);
		} catch (NullPointerException e) {
			throw e;
		}
	}

	private void addFieldToAspireObject(AspireObject ao, String name, Object value, String format)
			throws AspireException {
		if (EXPORT_FORMAT_JSON.equals(format)) {
			ao.add(name, value);
			return;
		}
		if (value instanceof List) {
			List<?> valueList = (List<?>) value;
			List<AspireObject> children = new ArrayList<AspireObject>();
			for (Object objectValue : valueList) {
				AspireObject child = new AspireObject(name, objectValue);
				children.add(child);
			}
			ao.add(children);
		} else {
			ao.add(name, value);
		}
	}

	private void complexListOfValueHoders(AspireObject ao, ValueHolderValueObject parent,
			LinkedList<ValueHolderValueObject> children, String format) throws AspireException {
		AspireObject parentAo = new AspireObject(parent.getName());
		for (ValueHolderValueObject child : children) {
			addFieldToAspireObject(parentAo, child.getName(), child.getValue(), format);
			// parentAo.add(child.getName(), );
		}
		ao.add(parentAo);
	}

	private void addObjectsContent(AspireObject aspireObject, AccountExportValueObject accountExport,
			SearchRecordValueObject document) throws AspireException {
		if (document.getObjects().values().size() == 0) {
			return;
		}

		AspireObject objectsAO = new AspireObject("objects");
		objectsAO.setAttribute("@version", document.getObjectVersion());
		objectsAO.setAttribute("@count", new Integer(document.getObjects().size()).toString());
		for (DigitalObjectValueObject digitalObject : document.getObjects().values()) {
			AspireObject objectAO = new AspireObject("object");
			if (digitalObject.getFilePath() != null) {
				AspireObject fileAO = new AspireObject("file");
				fileAO.setAttribute("@path", digitalObject.getFilePath());
				fileAO.setAttribute("@url", getFullUrl(digitalObject.getFilePath(), document, digitalObject.getFilePath()));
				objectAO.add(fileAO);

			}

			if (accountExport.getIncludeThumbnails() && digitalObject.getThumbnailPath() != null) {
				AspireObject thumbnailAO = new AspireObject("thumbnail");
				thumbnailAO.setAttribute("@path", digitalObject.getThumbnailPath());
				thumbnailAO.setAttribute("@url", getFullUrl(digitalObject.getThumbnailPath(), document, digitalObject.getFilePath()));
				thumbnailAO.setAttribute("@mime", digitalObject.getThumbnailMimeType());
				String caption = null;
				if (digitalObject.getDesignator() != null && digitalObject.getDescription() != null) {
					caption = digitalObject.getDesignator() + ", " + digitalObject.getDescription();
				} else {
					caption = (digitalObject.getDesignator() != null) ? digitalObject.getDesignator()
							: digitalObject.getDescription();
				}
				thumbnailAO.setAttribute("@caption", caption);
				objectAO.add(thumbnailAO);
			}
			objectsAO.add(objectAO);
			populateObjectPublicContributions(objectAO, digitalObject, accountExport, document);
		}
		aspireObject.add(objectsAO);
	}

	private void populateDocPublicContributions(SearchRecordValueObject document,
			AccountExportValueObject accountExport, AspireObject aspireObject) throws AspireException {
		if (!accountExport.getIncludeTags() && !accountExport.getIncludeComments()) {
			return;
		}
		List<TagValueObject> tags = null;
		if (accountExport.getIncludeTags()) {
			try {
				if (document.getNaId() != null) {
					tags = tagsDao.selectAllTags(document.getNaId(), document.getObjectId(), null, true);
				}
			} catch (DataAccessException e) {
				logger.error(String.format("Error while retrieving tags for naId: %s. Reason: %s", document.getNaId(),
						e.getMessage()));
			}
		}
		CommentsCollectionValueObject comments = null;
		if (accountExport.getIncludeComments()) {
			comments = commentsDao.selectAllCommentsAndReplies(document.getNaId(), null);
		}

		if ((tags == null || tags.size() == 0)
				&& (comments == null || comments.getTotalCommentsIncludingRemoves() == 0)) {
			return;
		}

		AspireObject publicContributionsAO = new AspireObject("publicContributions");
		aspireObject.add(publicContributionsAO);
		if (tags != null && tags.size() > 0) {
			publicContributionsAO.add(createAspireObjectFromTags(tags));
		}
		if (comments != null && comments.getTotalCommentsIncludingRemoves() > 0) {
			publicContributionsAO.add(createAspireObjectFromComments(comments));
		}
	}

	private void populateObjectPublicContributions(AspireObject objectAO, DigitalObjectValueObject digitalObject,
			AccountExportValueObject accountExport, SearchRecordValueObject document) throws AspireException {

		if (!accountExport.getIncludeTags() && !accountExport.getIncludeTranscriptions()
				&& !accountExport.getIncludeComments()) {
			return;
		}

		CommentsCollectionValueObject objectComments = commentsDao.selectAllCommentsAndReplies(document.getNaId(),
				digitalObject.getId());

		if (digitalObject.getTags().size() == 0 && digitalObject.getTranscriptions().size() == 0
				&& (objectComments == null || objectComments.getTotalCommentsIncludingRemoves() == 0)) {
			return;
		}

		AspireObject publicContributionsAO = new AspireObject("publicContributions");
		objectAO.add(publicContributionsAO);
		populateTags(digitalObject, publicContributionsAO);
		populateTranscriptions(digitalObject, publicContributionsAO);
		// populateComments
		publicContributionsAO.add(createAspireObjectFromComments(objectComments));
	}

	private void populateTags(DigitalObjectValueObject digitalObject, AspireObject publicContributionsAO)
			throws AspireException {
		List<TagValueObject> tags = digitalObject.getTags();
		if (tags == null || tags.size() == 0) {
			return;
		}
		publicContributionsAO.add(createAspireObjectFromTags(tags));
	}

	private AspireObject createAspireObjectFromTags(List<TagValueObject> tags) throws AspireException {
		int noOfTags = tags.size();
		AspireObject tagsAO = new AspireObject("tags");

		tagsAO.setAttribute("@total", new Integer(noOfTags).toString());
		for (TagValueObject tag : tags) {
			tagsAO.add(createAspireObjectFromTag(tag));
		}
		return tagsAO;
	}

	private AspireObject createAspireObjectFromTag(TagValueObject tag) throws AspireException {
		AspireObject tagAO = new AspireObject("tag");
		String user = null;
		if (tag.getDisplayNameFlag() != null && tag.getDisplayNameFlag()) {
			user = tag.getFullName();
		} else {
			user = tag.getUserName();
		}
		tagAO.setAttribute("@user", user);
		tagAO.setAttribute("@created", TimestampUtils.getUtcString(tag.getAnnotationTS()));
		tagAO.addContent(tag.getAnnotation());
		return tagAO;
	}

	private void populateTranscriptions(DigitalObjectValueObject digitalObject, AspireObject publicContributionsAO)
			throws AspireException {

		List<TranscriptionValueObject> transcriptions = digitalObject.getTranscriptions();
		if (transcriptions == null || transcriptions.size() == 0) {
			return;
		}
		AspireObject transcriptionAO = new AspireObject("transcription");
		TranscriptionValueObject lastTranscription = transcriptions.get(transcriptions.size() - 1);
		transcriptionAO.setAttribute("@lastModified", TimestampUtils.getUtcString(lastTranscription.getAnnotationTS()));
		transcriptionAO.setAttribute("@version", new Integer(lastTranscription.getSavedVersionNumber()).toString());
		AspireObject transcriptionUsersAO = new AspireObject("users");
		transcriptionUsersAO.setAttribute("@total", new Integer(transcriptions.size()).toString());
		addTranscriptionUsers(transcriptionUsersAO, transcriptions);
		transcriptionAO.add(transcriptionUsersAO);
		transcriptionAO.add("text", lastTranscription.getAnnotation());
		publicContributionsAO.add(transcriptionAO);
	}

	private void addTranscriptionUsers(AspireObject transcriptionUsersAO, List<TranscriptionValueObject> transcriptions)
			throws AspireException {
		for (TranscriptionValueObject transcription : transcriptions) {
			AspireObject transcriptionUserAO = new AspireObject("user");
			UserAccountValueObject userValueObject = transcription.getUser();
			transcriptionUserAO.setAttribute("@user", userValueObject.getUserName());
			transcriptionUserAO.setAttribute("@fullName", userValueObject.getFullName());
			transcriptionUserAO.setAttribute("@isNaraStaff", userValueObject.isNaraStaff().toString());
			transcriptionUserAO.setAttribute("@lastModified",
					TimestampUtils.getUtcString(transcription.getAnnotationTS()));
			transcriptionUserAO.setAttribute("@version", new Integer(transcription.getSavedVersionNumber()).toString());
			transcriptionUsersAO.add(transcriptionUserAO);
		}
	}

	private AspireObject createAspireObjectFromComments(CommentsCollectionValueObject comments) throws AspireException {
		int noOfComments = comments.getTotalCommentsIncludingRemoves();
		AspireObject commentsAO = new AspireObject("comments");

		commentsAO.setAttribute("@total", new Integer(noOfComments).toString());
		for (CommentValueObject comment : comments.getComments()) {
//			if (comment.getStatus() || comment.getReplies().size() > 0) {
//				commentsAO.add(createAspireObjectFromComment(comment));
//			}
			commentsAO.add(createAspireObjectFromComment(comment));
		}
		return commentsAO;
	}

	private AspireObject createAspireObjectFromComment(CommentValueObject comment) throws AspireException {
		AspireObject commentAO = new AspireObject("comment");
		createAspireObjectComments(commentAO, comment);

		if (comment.getReplies() != null && comment.getReplies().size() > 0) {
			AspireObject repliesAO = new AspireObject("replies");
			for (CommentValueObject reply : comment.getReplies()) {
				AspireObject replyAO = new AspireObject("reply");
				createAspireObjectComments(replyAO, reply);
				repliesAO.add(replyAO);
			}
			commentAO.add(repliesAO);
		}

		return commentAO;
	}

	private void createAspireObjectComments(AspireObject commentAO, CommentValueObject comment) {
		String user = null;
		if (comment.getDisplayNameFlag() != null && comment.getDisplayNameFlag()) {
			user = comment.getFullName();
			if (comment.getIsNaraStaff() != null && comment.getIsNaraStaff()) {
				user += " (NARA Staff)";
			}
		} else {
			user = comment.getUserName();
		}
		commentAO.setAttribute("@text", comment.getAnnotation());
		commentAO.setAttribute("@user", user);
		commentAO.setAttribute("@created", TimestampUtils.getUtcString(comment.getAnnotationTS()));
	}

	@SuppressWarnings("resource")
	public AspireObject createAspireObjectPublicSearchResult(SearchRecordValueObject document,
			AccountExportValueObject accountExport, int documentIndex) throws AspireException {
		if (document == null) {
			return null;
		}
		// List<String> resultFields = new ArrayList<String>();
		// String[] resultFields =
		// accountExport.getQueryParameters().get(CreateAccountExportRequestParameters.RESULT_FIELDS_HTTP_PARAM_NAME);
		//
		AspireObject resultAspireObject = new AspireObject("result");
		SolrDocument solrDoc = document.getSolrDocument();

		int offset = accountExport.getCursorMarkOffset() != null ? accountExport.getCursorMarkOffset() :
				accountExport.getOffset() != null ? accountExport.getOffset() : 0;

		List<String> resultFields = AccountExportValueObjectHelper.getResultFields(accountExport);
		if (resultFields == null || accountExport.isDefaultResultFieldsSet() || resultFields.contains("num")) {
			resultAspireObject.add("num", Integer.valueOf(documentIndex + offset).toString());
		}

		if (solrDoc.getFieldValue("score") != null && accountExport.getQueryParameters().containsKey("q")
				&& (resultFields == null || accountExport.isDefaultResultFieldsSet()
						|| resultFields.contains("score"))) {
			resultAspireObject.add("score", solrDoc.getFieldValue("score"));
		}

		String resultType = null;
		if (solrDoc.getFieldValue("type") != null
				&& !(StringUtils.isNullOrEmtpy(((String) solrDoc.getFieldValue("type")).trim()))) {

			resultType = (String) solrDoc.getFieldValue("type");
			if (resultFields == null || resultFields.contains("type")) {
				resultAspireObject.add("type", resultType);
			}
		}

		// Sets naId only if not object type
		if (solrDoc.getFieldValue("naId") != null
				&& !(StringUtils.isNullOrEmtpy(((String) solrDoc.getFieldValue("naId")).trim()))
				&& accountExport.getIncludeMetadata()
				&& (resultType == null || !resultType.equals(ResultTypesValidator.RESULT_TYPE_OBJECT))) {
			resultAspireObject.add("naId", StringUtils.removeMarkUps((String) solrDoc.getFieldValue("naId")));
		}

		// Sets parentDescriptionNaId if object type
		// if (solrDoc.getFieldValue("parentDescriptionNaId") != null
		// && !(StringUtils.isNullOrEmtpy(((String)
		// solrDoc.getFieldValue("parentDescriptionNaId")).trim()))
		// && (resultType != null &&
		// resultType.equals(ResultTypesValidator.RESULT_TYPE_OBJECT))) {
		// resultAspireObject.add("parentDescriptionNaId",
		// StringUtils.removeMarkUps((String)
		// solrDoc.getFieldValue("parentDescriptionNaId")));
		// }
		if (solrDoc.getFieldValue("parentDescriptionNaId") != null
				&& !(StringUtils.isNullOrEmtpy(((String) solrDoc.getFieldValue("parentDescriptionNaId")).trim()))) {
			resultAspireObject.add("parentDescriptionNaId",
					StringUtils.removeMarkUps((String) solrDoc.getFieldValue("parentDescriptionNaId")));
		}

		if (Constants.RESULT_TYPE_ARCHIVES_WEB.equals(resultType)
				|| Constants.RESULT_TYPE_PRESIDENTIAL_WEB.equals(resultType)) {
			if (solrDoc.getFieldValue("title") != null) {
				resultAspireObject.add("title", solrDoc.getFieldValue("title"));
			}

			if (solrDoc.getFieldValue("url") != null) {
				resultAspireObject.add("url", solrDoc.getFieldValue("url"));
			}

			if (solrDoc.getFieldValue("teaser") != null) {
				resultAspireObject.add("teaser", solrDoc.getFieldValue("teaser"));
			}

			if (solrDoc.getFieldValue("webArea") != null) {
				resultAspireObject.add("webArea", solrDoc.getFieldValue("webArea"));
			}

			if (solrDoc.getFieldValue("webAreaUrl") != null) {
				resultAspireObject.add("webAreaUrl", solrDoc.getFieldValue("webAreaUrl"));
			}
		}
		if (solrDoc.getFieldValue("description") != null
				&& !(StringUtils.isNullOrEmtpy(((String) solrDoc.getFieldValue("description")).trim()))
				&& (resultType == null || !resultType.equals("object"))) {
			String description = (String) solrDoc.getFieldValue("description");
			description = StringUtils.replaceString(StringUtils.CUSTOM_9999_YEAR_PATTERN, description, "");
			AspireObject object = new AspireObject("description");
			object.add(AspireObject.createFromXML(new StringReader(description)));
			resultAspireObject.add(object);
		}

		if (solrDoc.getFieldValue("authority") != null
				&& !(StringUtils.isNullOrEmtpy(((String) solrDoc.getFieldValue("authority")).trim()))) {
			String authority = (String) solrDoc.getFieldValue("authority");
			authority = StringUtils.replaceString(StringUtils.CUSTOM_9999_YEAR_PATTERN, authority, "");
			AspireObject object = new AspireObject("authority");
			object.add(AspireObject.createFromXML(new StringReader(authority)));
			resultAspireObject.add(object);
		}

		if (solrDoc.getFieldValue("objects") != null
				&& !(StringUtils.isNullOrEmtpy(((String) solrDoc.getFieldValue("objects")).trim()))) {
			String objects = (String) solrDoc.getFieldValue("objects");
			AspireObject aspireObjects = AspireObject.createFromXML(new StringReader(objects));
			enhanceObjectsDataPublicApis(aspireObjects, document, accountExport, solrDoc);
			resultAspireObject.add(aspireObjects);
		}

		if (solrDoc.getFieldValue("publicContributions") != null
				&& !(StringUtils.isNullOrEmtpy(((String) solrDoc.getFieldValue("publicContributions")).trim()))) {
			String publicContributions = (String) solrDoc.getFieldValue("publicContributions");
			AspireObject object = new AspireObject("publicContributions");
			object.add(AspireObject.createFromXML(new StringReader(publicContributions)));
			resultAspireObject.add(object.get("publicContributions"));
		}
		return resultAspireObject;
	}

	private void enhanceObjectsDataPublicApis(AspireObject aspireObjects, SearchRecordValueObject document,
			AccountExportValueObject accountExport, SolrDocument solrDoc) throws AspireException {
		for (AspireObject aspireObject : aspireObjects.getChildren()) {
			populateObjectFieldsPublicApi(aspireObject, document, solrDoc);
		}
	}

	private void populateObjectFieldsPublicApi(AspireObject aspireObject, SearchRecordValueObject document,
			SolrDocument solrDoc) {
		String baseObjectKey = "";
		for (AspireObject element : aspireObject.getChildren()) {
			String name = element.getName();
			if (name.equals("file") || name.equals("thumbnail") || name.equals("imageTiles") || name.equals("seData")) {
				String pathString = element.getAttribute("@path");
				if(name.equals("file")){
					baseObjectKey = pathString;
				}
				if (pathString != null) {
					element.setAttribute("@url", getFullUrl(pathString, document, baseObjectKey));
				}
				if (element.getAttribute("@mime") != null && element.getAttribute("@mime").toLowerCase().contains("pdf")
						&& element.getAttribute("@name").toLowerCase().contains("pdf")) {
					String naId = null;
					if (solrDoc.getFieldValue("naId") != null && !solrDoc.getFieldValue("naId").toString().isEmpty()) {
						naId = (String) solrDoc.getFieldValue("naId");
					}
					if (solrDoc.getFieldValue("parentDescriptionNaId") != null
							&& !solrDoc.getFieldValue("parentDescriptionNaId").toString().isEmpty()) {
						naId = (String) solrDoc.getFieldValue("parentDescriptionNaId");
					}
					if (naId == null) {
						continue;
					}

					String shortContent = getExtractedTextFromStorage(StringUtils.removeMarkUps(naId),
							element.getAttribute("@name"));
					shortContent = shortContent.replaceAll("[\\u0000-\\u0008]|[\\u000E-\\u001F]", "");

					if (!shortContent.isEmpty())
						element.setAttribute("@extractedText", shortContent);

				}
			}
		}
	}

	public String getExtractedTextFromStorage(String naId, String fileName) {

		OpaStorage storage = opaStorageFactory.createOpaStorage();
		String basePath = storage.getFullPathInLive("", Integer.parseInt(naId));
		String extractedPdfPath = basePath + StorageUtils.PDF_EXTRACTED_TEXT_PATH + fileName + ".paginated.txt";

		if (storage.exists(extractedPdfPath)) {
			String extractedPdfContents = "";
			try {
				byte[] bytes = storage.getFileContent(extractedPdfPath);
				extractedPdfContents = new String(bytes, "UTF-8");
				return extractedPdfContents;
			} catch (IOException e) {
				throw new OpaRuntimeException(e);
			}
		}

		return "";
	}

	protected String getJsonRecord(AspireObject aspireObject, AccountExportValueObject accountExport)
			throws AspireException {
		boolean prettyPrint = accountExport.isPrettyPrint();

		if (prettyPrint) {
			if (accountExport.getIncludeOpaResponseWrapper()) {
				aspireObject = padAspireObject(aspireObject, 2);
			}
		}

		String jsonRecord = aspireObject.toJsonString(prettyPrint);

		if (accountExport.getIncludeOpaResponseWrapper()) {
			if (prettyPrint) {
				jsonRecord = jsonRecord.replaceAll(
						"^\\{\n  \"paddingLevel1\":\\{\n    \"paddingLevel2\":\\{\n      \"result\":", "      ");
				jsonRecord = jsonRecord.replaceAll("\n    }\n  }\n}$", "");
			} else {
				jsonRecord = jsonRecord.replaceAll("^\\{\"result\":", "");
				jsonRecord = jsonRecord.replaceAll("\\}$", "");
			}
		} else {
			if (prettyPrint) {
				jsonRecord = jsonRecord.replaceAll("\\{\n  \"result\":", "  ");
				jsonRecord = jsonRecord.replaceAll("\n\\}$", "");
			} else {
				jsonRecord = jsonRecord.replaceAll("^\\{.*\"result\"\\:", "");
				jsonRecord = jsonRecord.replaceAll("\\}$", "");
			}

		}
		return jsonRecord;
	}

	protected String getXmlRecord(AspireObject aspireObject, AccountExportValueObject accountExport)
			throws AspireException {
		boolean prettyPrint = accountExport.isPrettyPrint();

		if (prettyPrint) {
			if (accountExport.getIncludeOpaResponseWrapper()) {
				aspireObject = padAspireObject(aspireObject, 2);
			}
		}

		String xmlRecord = aspireObject.toXmlString(prettyPrint);

		if (accountExport.getIncludeOpaResponseWrapper()) {
			if (prettyPrint) {
				xmlRecord = xmlRecord.replaceAll("^<paddingLevel1>\n   <paddingLevel2>\n", "");
				xmlRecord = xmlRecord.replaceAll("\n   </paddingLevel2>\n</paddingLevel1>$", "");
			}
		}

		return xmlRecord;
	}

	private AspireObject padAspireObject(AspireObject aspireObject, int level) {
		AspireObject returnValue = aspireObject;
		for (int i = level; i > 0; i--) {
			@SuppressWarnings("resource")
			AspireObject newObject = new AspireObject(AO_PADDING_KEYWORD + i);
			newObject.add(returnValue);
			returnValue = newObject;
		}
		return returnValue;
	}

	protected String getXmlBeginning(AccountExportValueObject accountExport,
			SolrQueryResponseValueObject queryResponse) {
		try {
			if (accountExport.getIncludeOpaResponseWrapper()) {
				String opaResponseWrapper = opaResponseWrapperBeginning(accountExport, queryResponse);
				if (!accountExport.getPrintingFormat().equals(PRINTING_FORMAT_PRETTY_FALSE)) {
					opaResponseWrapper = opaResponseWrapper + "\n";
				}
				return opaResponseWrapper;
			} else {
				String retValue = "<naraitems>";
				if (accountExport.getPrintingFormat().equals(PRINTING_FORMAT_PRETTY_FALSE)) {
					return retValue;
				} else {
					return retValue + "\n";
				}
			}
		} catch (AspireException e) {
			throw new OpaRuntimeException(e);
		}
	}

	protected String getJsonBeginning(AccountExportValueObject accountExport,
			SolrQueryResponseValueObject queryResponse) {
		try {
			if (accountExport.getIncludeOpaResponseWrapper()) {
				String opaResponseWrapper = opaResponseWrapperBeginning(accountExport, queryResponse);
				if (accountExport.getPrintingFormat().equals(PRINTING_FORMAT_PRETTY_FALSE)) {
					opaResponseWrapper = opaResponseWrapper + "[";
				} else {
					opaResponseWrapper = opaResponseWrapper + "[\n";
				}
				return opaResponseWrapper;
			} else {
				if (accountExport.getPrintingFormat().equals(PRINTING_FORMAT_PRETTY_FALSE)) {
					return "[";
				} else {
					return "[\n";
				}
			}
		} catch (AspireException e) {
			throw new OpaRuntimeException(e);
		}
	}

	protected String getJsonEnding(AccountExportValueObject accountExport, String jsonRecord) {
		if (accountExport.getIncludeOpaResponseWrapper()) {
			if (accountExport.getPrintingFormat().equals(PRINTING_FORMAT_PRETTY_FALSE)) {
				return "]}}}";
			} else {
				if (jsonRecord.endsWith("{}")) {
					return "\n      ]\n    }\n  }\n}";
				} else {
					return "      ]\n    }\n  }\n}";
				}
			}
		} else {
			if (accountExport.getPrintingFormat().equals(PRINTING_FORMAT_PRETTY_FALSE)) {
				return "]";
			} else {
				return "\n]";
			}
		}
	}

	protected String getXmlEnding(AccountExportValueObject accountExport) {
		if (accountExport.getIncludeOpaResponseWrapper()) {
			if (accountExport.getPrintingFormat().equals(PRINTING_FORMAT_PRETTY_FALSE)) {
				return "</results></opaResponse>";
			} else if (accountExport.getPrintingFormat().equals(PRINTING_RECORD_LINE)) {
				return "\n</results></opaResponse>";
			} else {
				return "   </results>\n</opaResponse>";
			}
		} else {
			if (accountExport.getPrintingFormat().equals(PRINTING_FORMAT_PRETTY_FALSE)
					|| accountExport.getPrintingFormat().equals(PRINTING_FORMAT_PRETTY_TRUE)) {
				return "</naraitems>";
			} else {
				return "\n</naraitems>";
			}
		}
	}

	protected String opaResponseWrapperBeginning(AccountExportValueObject accountExport,
			SolrQueryResponseValueObject queryResponse) throws AspireException {
		LinkedHashMap<String, Object> requestParameters = accountExport.getApiRequestParams();
		DateTime dt = new DateTime();

		String currentTimeStampString = dt.toDateTime(DateTimeZone.UTC).toString();

		AspireObject aspireObject = new AspireObject(SuccessResponseAspireObjectContentHolder.OPA_RESPONSE);
		aspireObject.add(HeaderAspireObjectContentHolder.HEADER_STATUS_PARAMETER_NAME, HttpStatus.OK.toString());
		aspireObject.add(HeaderAspireObjectContentHolder.HEADER_TIME_PARAMETER_NAME, currentTimeStampString);
		AspireObject request = new AspireObject(HeaderAspireObjectContentHolder.REQUEST_HEADER_NAME);
		if (requestParameters != null) {
			for (String name : requestParameters.keySet()) {
				request.add(name, requestParameters.get(name));
			}
		}
		aspireObject.add(request);
		addQueryResponse(aspireObject, accountExport, queryResponse);
		return getBeginningString(aspireObject, accountExport);
	}

	private void addQueryResponse(AspireObject aspireObject, AccountExportValueObject accountExport,
			SolrQueryResponseValueObject queryResponse) throws AspireException {
		AspireObject results = new AspireObject("results");
		results.add("queryTime", queryResponse.getQueryTime());
		results.add("total", queryResponse.getTotalResults());
		int offset = accountExport.getCursorMarkOffset() != null ? accountExport.getCursorMarkOffset() :
				accountExport.getOffset() != null ? accountExport.getOffset() : 0;
		results.add("offset", offset);

		if (queryResponse.getNextCursorMark() != null
        	&& (accountExport.getRows() + offset) < queryResponse.getTotalResults()) {
			if (!accountExport.getCursorMark().equalsIgnoreCase(queryResponse.getNextCursorMark())) {
				results.add("nextCursorMark", queryResponse.getNextCursorMark() + "-" + (offset + accountExport.getRows()));
			}
		}				
		results.add("rows", accountExport.getRows());
		results.add("result", "");
		aspireObject.add(results);
	}

	private String getBeginningString(AspireObject aspireObject, AccountExportValueObject accountExport)
			throws AspireException {
		boolean prettyPrint = accountExport.isPrettyPrint();
		String result = null;
		if (accountExport.getExportFormat().equals(EXPORT_FORMAT_JSON)) {
			result = aspireObject.toJsonString(prettyPrint);
			int indexOfResult = result.indexOf("\"result\":");
			result = result.substring(0, indexOfResult + 9);
		} else if (accountExport.getExportFormat().equals(EXPORT_FORMAT_XML)) {
			result = aspireObject.toXmlString(prettyPrint);
			int indexOfResult = result.lastIndexOf("</rows>", result.length() - 1);
			result = result.substring(0, indexOfResult + 7);
		} else {
			throw new OpaRuntimeException(
					"Invalid format to inculde an opa response: " + accountExport.getExportFormat());
		}
		return result;
	}
}
