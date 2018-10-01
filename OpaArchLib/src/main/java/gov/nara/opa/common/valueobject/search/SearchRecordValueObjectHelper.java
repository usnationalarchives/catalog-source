package gov.nara.opa.common.valueobject.search;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.sax.SAXSource;

import org.apache.solr.common.SolrDocument;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.exception.OpaSkipRecordException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.utils.TimestampUtils;
import gov.nara.opa.common.dataaccess.annotation.tags.TagDao;
import gov.nara.opa.common.dataaccess.annotation.transcriptions.TranscriptionDao;
import gov.nara.opa.common.services.SingletonServices;
import gov.nara.opa.common.services.docstransforms.Constants;
import gov.nara.opa.common.services.docstransforms.impl.normalize.AbstractXslValueExtractor;
import gov.nara.opa.common.validation.search.ResultTypesValidator;
import gov.nara.opa.common.valueobject.annotation.tags.TagValueObject;
import gov.nara.opa.common.valueobject.annotation.transcriptions.TranscriptionValueObject;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.common.valueobject.export.DigitalObjectValueObject;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltExecutable;

@Component
public class SearchRecordValueObjectHelper implements Constants, InitializingBean {

	private static long LONG_TIMED_PROCESS_MILLIS = 5000;
	
	XsltExecutable getObjectsMetadataXslt;

	static OpaLogger logger = OpaLogger.getLogger(SearchRecordValueObjectHelper.class);

	@Autowired
	TagDao tagDao;

	@Autowired
	TranscriptionDao transcriptionDao;

	public static List<String> LEVELS = new ArrayList<String>();

	static {
		LEVELS.add(RESULT_TYPE_RECORD_GROUP);
		LEVELS.add(RESULT_TYPE_COLLECTION);
		LEVELS.add(RESULT_TYPE_SERIES);
		LEVELS.add(RESULT_TYPE_FILE_UNIT);
		LEVELS.add(RESULT_TYPE_ITEM);
		LEVELS.add(RESULT_TYPE_ITEM_AV);
		LEVELS.add(RESULT_TYPE_OBJECT);

	}

	public static List<String> TYPES = new ArrayList<String>();

	static {
		TYPES.add(RESULT_TYPE_PERSON);
		TYPES.add(RESULT_TYPE_ORGANIZATION);
		TYPES.add(RESULT_TYPE_GEOGRAPHIC_REFERENCE);
		TYPES.add(RESULT_TYPE_TOPICAL_SUBJECT);
		TYPES.add(RESULT_TYPE_SPECIFIC_RECORDS_TYPE);
		TYPES.add(RESULT_TYPE_ARCHIVES_WEB);
		TYPES.add(RESULT_TYPE_PRESIDENTIAL_WEB);
	}

	public final SearchRecordValueObject createSolrRecord(SolrDocument solrDocument,
			AccountExportValueObject accountExport) {

		SearchRecordValueObject searchRecord = new SearchRecordValueObject();
		searchRecord.setSolrDocument(solrDocument);
		searchRecord.setResultType(getResultType(searchRecord));

		String opaId = (String) solrDocument.get("opaId");
		searchRecord.setOpaId(opaId);

		String naId = (String) solrDocument.get("naId");
		if (naId != null) {
			searchRecord.setNaId(naId.replaceAll("\\{.*\\}", ""));
		}
		
		// parentDescriptionNaId - NARA-2098
		String parentDescriptionNaId = (String) solrDocument.get("parentDescriptionNaId");
		if (!StringUtils.isNullOrEmtpy(parentDescriptionNaId)) {
			searchRecord.setParentDescriptionNaId(parentDescriptionNaId);
		} else {
			logger.debug("Parent desc not found for record: naId="+naId +", opaId="+opaId);
		}
		

//		if (searchRecord.getResultType().equals(ResultTypesValidator.RESULT_TYPE_OBJECT) ||
//				StringUtils.isNullOrEmtpy(searchRecord.getNaId())) {
//			// parentDescriptionNaId - NARA-2098
//			String parentDescriptionNaId = (String) solrDocument.get("parentDescriptionNaId");
//			if (!StringUtils.isNullOrEmtpy(parentDescriptionNaId)) {
//				searchRecord.setParentDescriptionNaId(parentDescriptionNaId);
//			} else {
//				logger.debug(String.format("Parent desc not found for record: %1$s", solrDocument.toString()));
//			}
//		}

		String authorityXml = (String) solrDocument.get("authority");
		if (authorityXml != null && !authorityXml.equals("")) {
			authorityXml = StringUtils.replaceString(StringUtils.CUSTOM_9999_YEAR_PATTERN, authorityXml, "");
			searchRecord.setAuthorityXml(authorityXml);
			searchRecord.setCompiledAuthorityXml(createCompiledXml(authorityXml));
		}
		String descriptionXml = (String) solrDocument.get("description");
		if (descriptionXml != null && !descriptionXml.equals("")) {
			descriptionXml = StringUtils.replaceString(StringUtils.CUSTOM_9999_YEAR_PATTERN, descriptionXml, "");
			searchRecord.setDescriptionXml(descriptionXml);
			searchRecord.setCompiledDescriptionXml(createCompiledXml(descriptionXml));
		}

		String publicContributionsXml = (String) solrDocument.get("publicContributions");
		if (publicContributionsXml != null && !publicContributionsXml.equals("")) {
			searchRecord.setPublicContributionsXml(publicContributionsXml);
			searchRecord.setCompiledPublicContributionsXml(createCompiledXml(publicContributionsXml));
		}

		try {
			setObjectsData(searchRecord, solrDocument, accountExport);
		} catch (AspireException | DataAccessException | UnsupportedEncodingException e) {
			throw new OpaRuntimeException(e);
		}
		return searchRecord;
	}

	public final SearchRecordValueObject createSolrRecord(SolrDocument solrDocument) {

		SearchRecordValueObject searchRecord = new SearchRecordValueObject();

		String opaId = (String) solrDocument.get("opaId");
		searchRecord.setOpaId(opaId);

		String naId = (String) solrDocument.get("naId");
		logger.trace("Creating document for naId: " + naId);
		if (!StringUtils.isNullOrEmtpy(naId)) {
			searchRecord.setNaId(naId.replaceAll("\\{.*\\}", ""));
		}

		searchRecord.setSolrDocument(solrDocument);
		searchRecord.setResultType(getResultType(searchRecord));

		// parentDescriptionNaId - NARA-2098
		logger.debug("Getting parent desc for record");
		String parentDescriptionNaId = (String) solrDocument.get("parentDescriptionNaId");
		if (!StringUtils.isNullOrEmtpy(parentDescriptionNaId)) {
			searchRecord.setParentDescriptionNaId(parentDescriptionNaId);
		} else {
			logger.debug("Parent desc not found for record: naId="+naId +", opaId="+opaId);
		}
		
		if (searchRecord.getResultType().equals(ResultTypesValidator.RESULT_TYPE_OBJECT) ||
				StringUtils.isNullOrEmtpy(searchRecord.getNaId())) {
			if(!StringUtils.isNullOrEmtpy(opaId)) {
				String[] tokens = opaId.split("-");
				searchRecord.setObjectId(tokens[2]);
				searchRecord.setObjectsXml(solrDocument.get("objects").toString());
			}

//			// parentDescriptionNaId - NARA-2098
//			String parentDescriptionNaId = (String) solrDocument.get("parentDescriptionNaId");
//			if (!StringUtils.isNullOrEmtpy(parentDescriptionNaId)) {
//				searchRecord.setParentDescriptionNaId(parentDescriptionNaId);
//			} else {
//				logger.debug(String.format("Parent desc not found for record: %1$s", solrDocument.toString()));
//			}

		}
		return searchRecord;
	}

	private void setTagsData(SearchRecordValueObject searchRecord, AccountExportValueObject accountExport,
			XdmNode publicContributionsNode, XdmNode objectsNode)
					throws DataAccessException, UnsupportedEncodingException, AspireException {
		if (!accountExport.getIncludeTags()) {
			return;
		}
		List<TagValueObject> tags = getTagsFromSolrXml(publicContributionsNode, objectsNode, searchRecord.getNaId());
		for (TagValueObject tag : tags) {
			if (tag.getObjectId() == null) {
				searchRecord.getTags().add(tag);
			} else {
				DigitalObjectValueObject digitatlObject = searchRecord.getObjects().get(tag.getObjectId());
				if (digitatlObject == null) {
					logger.error(String.format(
							"Tag (%1$s) with object id (%2$s) does not have an associated object in the objects xml for opaId: %3$s",
							tag.getAnnotation(), tag.getObjectId(), searchRecord.getOpaId()));
				} else {
					digitatlObject.getTags().add(tag);
				}
			}
		}
	}

	private List<TagValueObject> getTagsFromSolrXml(XdmNode publicContributionsNode, XdmNode objectsNode, String naId)
			throws AspireException {

		// logger.debug("PUBLIC CONTRIBUTIONS FIELD:\n" +
		// publicContributionsNode);
		// logger.debug("OBJECTS FIELD:\n" + objectsNode);
		List<TagValueObject> tags = new ArrayList<TagValueObject>();
		if (publicContributionsNode != null) {
			addDescriptionLevelTagsFromSolrXml(tags, publicContributionsNode, naId);
		}
		if (objectsNode != null) {
			addObjectLevelTagsFromSolrXml(tags, objectsNode, naId);
		}
		return tags;
	}

	private void addDescriptionLevelTagsFromSolrXml(List<TagValueObject> tags, XdmNode publicContributionsNode,
			String naId) throws AspireException {
		@SuppressWarnings("resource")
		AspireObject obj = new AspireObject("root");
		String xml = publicContributionsNode.toString();
		if (xml == null || xml.trim().equals("")) {
			return;
		}
		obj.loadXML(new StringReader(xml));
		AspireObject publicContributionsAo = obj.getChildren().get(0);

		if (publicContributionsAo != null) {
			AspireObject tagsAo = publicContributionsAo.get("tags");
			if (tagsAo != null) {
				addTagsFromXmlAspireOject(tagsAo, naId, null, tags);
			}
		}
	}

	private void addObjectLevelTagsFromSolrXml(List<TagValueObject> tags, XdmNode objectsNode, String naId)
			throws AspireException {
		@SuppressWarnings("resource")
		AspireObject obj = new AspireObject("root");
		String xml = objectsNode.toString();
		if (xml == null || xml.trim().equals("")) {
			return;
		}
		obj.loadXML(new StringReader(xml));
		AspireObject rootXml = obj.getChildren().get(0);
		for (AspireObject ao : rootXml.getChildren()) {
			if (ao.getName().equals("object")) {
				AspireObject publicContributionsAo = ao.get("publicContributions");
				if (publicContributionsAo != null) {
					AspireObject tagsAo = publicContributionsAo.get("tags");
					addTagsFromXmlAspireOject(tagsAo, naId, ao.getAttribute("id"), tags);
				}
			}
		}
	}

	private void addTagsFromXmlAspireOject(AspireObject tagsAo, String naId, String objectId,
			List<TagValueObject> tags) {
		if (tagsAo == null || tagsAo.getChildren() == null) {
			return;
		}
		for (AspireObject ao : tagsAo.getChildren()) {
			TagValueObject tag = new TagValueObject();
			tag.setNaId(naId);
			tag.setObjectId(objectId);
			tag.setAnnotation((String) ao.getContent());
			tag.setUserName(ao.getAttribute("user"));
			if (ao.getAttribute("created") != null) {
				tag.setAnnotationTS(TimestampUtils.toUtcTimestamp(ao.getAttribute("created")));
			}

			if (ao.getAttribute("isNaraStaff") != null) {
				tag.setIsNaraStaff(new Boolean(ao.getAttribute("isNaraStaff")));
			} else {
				tag.setIsNaraStaff(new Boolean(false));
			}
			tag.setFullName(ao.getAttribute("fullName"));
			if (tag.getFullName() != null) {
				tag.setDisplayNameFlag(true);
			}
			tags.add(tag);
		}
	}

	// private List<TranscriptionValueObject> get

	private void setTranscriptionsData(SearchRecordValueObject searchRecord, AccountExportValueObject accountExport,
			XdmNode objectsNode) throws DataAccessException, UnsupportedEncodingException, AspireException {
		if (!accountExport.getIncludeTranscriptions()) {
			return;
		}
		// commented out because public contributions should come the the solr
		// xml
		// now
		// List<TranscriptionValueObject> transcriptions = transcriptionDao
		// .selectTranscriptionsByNaid(searchRecord.getNaId());
		List<TranscriptionValueObject> transcriptions = getTranscriptionsFromSolrXml(objectsNode,
				searchRecord.getNaId());

		for (TranscriptionValueObject transcription : transcriptions) {
			if (transcription.getObjectId() == null) {
				logger.error(String.format("This transcription (annotation id: %1$s) does not have an object id set: ",
						transcription.getAnnotationId()));
			} else {
				DigitalObjectValueObject digitatlObject = searchRecord.getObjects().get(transcription.getObjectId());
				if (digitatlObject == null) {
					logger.error(String.format(
							"Transcription (%1$s) with object id (%2$s) does not have an associated object in the objects xml for opaId: %3$s",
							transcription.getAnnotationId(), transcription.getObjectId(), searchRecord.getOpaId()));
				} else {
					digitatlObject.getTranscriptions().add(transcription);
				}
			}
		}
	}

	private List<TranscriptionValueObject> getTranscriptionsFromSolrXml(XdmNode objectsNode, String naId)
			throws AspireException {
		List<TranscriptionValueObject> transcriptions = new ArrayList<TranscriptionValueObject>();
		if (objectsNode == null) {
			return transcriptions;
		}

		@SuppressWarnings("resource")
		AspireObject obj = new AspireObject("root");
		String xml = objectsNode.toString();
		if (xml == null || xml.trim().equals("")) {
			return transcriptions;
		}
		obj.loadXML(new StringReader(xml));
		AspireObject rootXml = obj.getChildren().get(0);
		for (AspireObject ao : rootXml.getChildren()) {
			if (ao.getName().equals("object")) {
				AspireObject publicContributionsAo = ao.get("publicContributions");
				if (publicContributionsAo != null) {
					AspireObject transcriptionAo = publicContributionsAo.get("transcription");
					if (transcriptionAo != null) {
						for (AspireObject userAo : transcriptionAo.get("users").getChildren()) {
							TranscriptionValueObject transcription = new TranscriptionValueObject();
							if (transcriptionAo.get("text") != null) {
								transcription.setAnnotation((String) transcriptionAo.get("text").getContent());
							} else {
								logger.debug("Encountered a transcription with no text set for it. Naid=" + naId);
							}
							transcription.setNaId(naId);
							transcription.setObjectId(ao.getAttribute("id"));
							if (userAo.getAttribute("version") != null) {
								transcription.setSavedVersionNumber(Integer.valueOf(userAo.getAttribute("version")));
							}
							UserAccountValueObject user = new UserAccountValueObject();
							user.setFullName(userAo.getAttribute("fullName"));
							if (user.getFullName() != null) {
								user.setDisplayFullName(true);
							}
							if (userAo.getAttribute("isNaraStaff") != null) {
								user.setNaraStaff(new Boolean(userAo.getAttribute("isNaraStaff")));
							} else {
								user.setNaraStaff(new Boolean(false));
							}

							user.setUserName(userAo.getAttribute("user"));
							if (userAo.getAttribute("lastModified") != null) {
								transcription.setAnnotationTS(
										TimestampUtils.toUtcTimestamp(userAo.getAttribute("lastModified")));
							}
							transcription.setUser(user);

							transcriptions.add(transcription);
						}
					}

				}
			}
		}

		return transcriptions;
	}

	private void setObjectsData(SearchRecordValueObject searchRecord, SolrDocument solrDocument,
			AccountExportValueObject accountExport)
					throws AspireException, DataAccessException, UnsupportedEncodingException {
		if (accountExport.getIncludeThumbnails() || accountExport.getIncludeTranscriptions()
				|| accountExport.getIncludeTranslations() || accountExport.getIncludeContent()
				|| accountExport.getIncludeEmbeddedThumbnails() || accountExport.getIncludeComments()
				|| accountExport.getIncludeTags()) {
			long startTime = new Date().getTime();
			
			String objectsXml = (String) solrDocument.get("objects");

			XdmNode objectsNode = null;
			if (objectsXml != null && !objectsXml.equals("")) {
				searchRecord.setObjectsXml(objectsXml);
				XdmNode compiledXml = createCompiledXml(objectsXml);
				searchRecord.setCompiledObjectsXml(compiledXml);
				
				
				if (getObjectsMetadataXslt == null) {
				     getObjectsMetadataXslt = AbstractXslValueExtractor
				       .createXPathExecutable(AbstractXslValueExtractor.XSL_FILE_PATH + "objectsMetadata.xsl");
				}
				
				objectsNode = AbstractXslValueExtractor.transform(compiledXml, getObjectsMetadataXslt);
				if (objectsNode != null) {
					populateObjects(objectsNode, searchRecord);
				}
			}
			setTagsData(searchRecord, accountExport, searchRecord.getCompiledPublicContributionsXml(),
					searchRecord.getCompiledObjectsXml());
			if (objectsXml != null && !objectsXml.equals("")) {
				setTranscriptionsData(searchRecord, accountExport, searchRecord.getCompiledObjectsXml());
			}
			
			long endTime = new Date().getTime();
			if(endTime - startTime > LONG_TIMED_PROCESS_MILLIS) {
				logger.info(String.format("ExportId:[%1$d] Process took more than [%2$d] secs", accountExport.getExportId(), LONG_TIMED_PROCESS_MILLIS));
			}
		}
	}

	private void populateObjects(XdmNode objectsNode, SearchRecordValueObject searchRecord) throws AspireException {
		@SuppressWarnings("resource")
		AspireObject obj = new AspireObject("root");
		String xml = null;
		try {
			xml = objectsNode.toString();
			if (xml == null || xml.trim().equals("")) {
				return;
			}
			obj.loadXML(new StringReader(xml));
			AspireObject rootXml = obj.getChildren().get(0);
			int index = 1;
			for (AspireObject ao : rootXml.getChildren()) {

				if (ao.getName().equals("object")) {
					DigitalObjectValueObject digitalObject = createDigitalObject(ao, index);
					searchRecord.getObjects().put(digitalObject.getId(), digitalObject);
				} else if (ao.getName().equals("version")) {
					searchRecord.setObjectVersion((String) ao.getContent());
				}
			}
		} catch (NullPointerException e) {
			logger.error(String.format("Objects XML: %1$s", xml));
			logger.error(e.getMessage(), e);
			return;
		}
	}

	private DigitalObjectValueObject createDigitalObject(AspireObject ao, int index) throws AspireException {
		DigitalObjectValueObject digitalObject = new DigitalObjectValueObject();
		String id = (String) ao.get("id").getContent();
		digitalObject.setId(id);
		digitalObject.setIndex(index);
		digitalObject.setThumbnailPath((String) ao.get("thumbnailpath").getContent());
		digitalObject.setThumbnailMimeType((String) ao.get("thumbnailmimetype").getContent());
		digitalObject.setFilePath((String) ao.get("filepath").getContent());
		digitalObject.setFileMimeType((String) ao.get("filemimetype").getContent());
		if (ao.get("designator") != null) {
			digitalObject.setDesignator((String) ao.get("designator").getContent());
		}
		if (ao.get("description") != null) {
			digitalObject.setDescription((String) ao.get("description").getContent());
		}
		index++;
		return digitalObject;
	}

	private XdmNode createCompiledXml(String xml) {
		if (xml == null) {
			return null;
		}
		Processor processor = SingletonServices.SAXON_PROCESSOR;
		DocumentBuilder documentBuilder = processor.newDocumentBuilder();
		InputSource source = new InputSource(new StringReader(xml));
		try {
			return documentBuilder.build(new SAXSource(source));
		} catch (SaxonApiException e) {
			if (e.getMessage().contains("xsi:xsi")) {
				logger.error("Skipping record with xsi parse exception: " + xml);
				throw new OpaSkipRecordException(e);
			} else {
				logger.error("Skipping record whose xml could not be parsed: " + xml);
				throw new OpaSkipRecordException(e);
			}
		}
	}

	public static String getResultType(SearchRecordValueObject document) {
		SolrDocument solrDocument = document.getSolrDocument();
		Object level = solrDocument.get("level");
		
		if (level != null && LEVELS.contains(level)) {
			if (level.equals("object") && solrDocument.get("description") != null) {
				String resultTypePattern = "^<(.*?)>";
				String description = (String)solrDocument.get("description");
				
				if(!StringUtils.isNullOrEmtpy(description)) {
					Pattern pattern = Pattern.compile(resultTypePattern);
					Matcher matcher = pattern.matcher(description);
					if (matcher.find())
					{
						level = matcher.group(1);
					}
				} else {
					String errorMessage = "Description is empty for document: " + solrDocument.get("opaId");
					logger.error(errorMessage);
					throw new OpaSkipRecordException(errorMessage);
				}
			}

			if (level.equals("itemAv")) {
				return "item";
			}
			return (String) level;
		}

		Object type = solrDocument.get("type");
		if (type != null && TYPES.contains(type)) {
			return (String) type;
		}

		throw new OpaSkipRecordException(
				"Could not determine the resultType for the document with opaId " + document.getOpaId());

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		getObjectsMetadataXslt = AbstractXslValueExtractor
				.createXPathExecutable(AbstractXslValueExtractor.XSL_FILE_PATH + "objectsMetadata.xsl");
	}
}
