/**
 * Copyright Search Technologies 2014
 * for NARA OPA
 */
package gov.nara.opa.ingestion;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.Lock;

import org.w3c.dom.Element;

import com.searchtechnologies.aspire.framework.StageImpl;
import com.searchtechnologies.aspire.rdb.RDBMSConnectionPool;
import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;
import com.searchtechnologies.aspire.services.ComponentUnavailableException;
import com.searchtechnologies.aspire.services.Job;

/**
 * Adds Annotations to the document, based on NAID/ObjectId.
 *
 * @author OPA Ingestion Team
 */
public class ProcessAnnotationsStage extends StageImpl {
	private static final String TRANSCRIPTION_KEYWORD_ELEMENT = "objectTranscriptionText";
	private static final String TRANSCRIPTION_FIRST_ELEMENT = "transcriptionFirstDateTime";
	private static final String TRANSCRIPTION_LAST_ELEMENT = "transcriptionLatestDateTime";
	private static final String TRANSLATION_KEYWORD_ELEMENT = "objectTranslationText";
	private static final String TRANSLATION_FIRST_ELEMENT = "translationFirstDateTime";
	private static final String TRANSLATION_LAST_ELEMENT = "translationLatestDateTime";
	private static final String TAG_KEYWORD_ELEMENT = "tagsKeywords";
	private static final String TAG_OBJECT_KEYWORD_ELEMENT = "objectTagsKeywords";
	private static final String TAG_FIRST_ELEMENT = "tagFirstDateTime";
	private static final String TAG_LAST_ELEMENT = "tagLatestDateTime";
	private static final String COMMENT_FIRST_ELEMENT = "commentFirstDateTime";
	private static final String COMMENT_LAST_ELEMENT = "commentLatestDateTime";
	private static final String COMMENT_KEYWORD_ELEMENT = "commentsKeywords";
	private static final String COMMENT_OBJECT_KEYWORD_ELEMENT = "objectCommentsKeywords";
	private static final String SP_CHECK_ANNOTATIONS = "spIngestionCheckAnnotations";
	private static final String SP_GET_TAGS = "spIngestionGetTags";
	private static final String SP_GET_TRANSCRIPTIONS = "spIngestionGetTranscriptions";
	private static final String SP_GET_TRANSLATIONS = "spIngestionGetTranslations";
	private static final String SP_GET_COMMENTS = "spIngestionGetComments";

	private static final String ALL_CONTRIBUTORS_FIRST_ELEMENT = "allContributionsFirstDateTime";
	private static final String ALL_CONTRIBUTORS_LAST_ELEMENT = "allContributionsLatestDateTime";

	private Settings settings;

	/**
	 * The main entry point for processing a job. This will be called, sometimes
	 * at the same time by multiple threads (executing multiple jobs), whenever
	 * a job needs to be processed by your component.
	 * 
	 * @param j
	 *            The job to process.
	 */
	@Override
	public void process(Job j) throws AspireException {
		AspireObject doc = j.get();
		JobInfo info = Jobs.getJobInfo(j);
		Integer naid = info.getNAID();
		String objectId = info.getObjectId();
		boolean hasAnnotations;

		PublicContributionsXml publicContributionsXml = new PublicContributionsXml();

		debug("Processing Annotations for job: %s", j.getJobId());

		try (Connection connection = settings.getDbConnection()) {

			debug("CheckAnnotations: rdbConn %s,naid %s, objectId %s",
					connection, naid, objectId);
			hasAnnotations = checkAnnotations(connection, naid, objectId);

			if (hasAnnotations) {

				// Get all tags (and unique contributors + first/last date)
				getTags(connection, doc, naid, objectId, publicContributionsXml);

				// Get all transcriptions (and unique contributors + first/last
				// date) - only objects have transcriptions
				getTranscriptions(connection, doc, naid, objectId,
						publicContributionsXml);

				// Get all comments (and unique contributors + first/last date)
				getComments(connection, doc, naid, objectId, publicContributionsXml);

				// Get all translations
				getTranslations(connection, doc, naid, objectId,
						publicContributionsXml);

				// From tags & transcriptions (later others also) get unique
				// contributors with first/last date
				getContributions(doc);
				AspireObject publicContributions = publicContributionsXml.getContent();
				doc.set(publicContributions);

				if (objectId != null) {
					setObjectPublicContributions(publicContributions, info);

					if (info.getParent() != null) {
						setPublicContributionsInParentDescription(publicContributions, objectId, info);
					}
				}
			}
		} catch (Throwable e) {
				error(e, "%s", info.getDescription());
		}
	}

	@Override
	public void close() {
	}

	/**
	 * Initialize this component with the configuration data from the component
	 * manager configuration. NOTE: This method is *always* called, even if the
	 * component manager configuration is empty (in this situation, "config"
	 * will be null).
	 *
	 * @param config
	 *            The XML &lt;config&gt; DOM element which holds the custom
	 *            configuration for this component from the component manager
	 *            configuration file.
	 * @throws AspireException
	 */
	@Override
	public void initialize(Element config) throws AspireException {

		settings = Components.getSettings(this);
	}

	/**
	 * Check if two object ids match each other.
	 */
	private Boolean idsMatch(String objectId1, String objectId2) {
		if (objectId1 == null) {
			if (objectId2 == null) {
				return true;
			}
		} else {
			if (objectId1.equals(objectId2)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if the current record has annotations
	 * 
	 * @param conn
	 * @param naid
	 * @param objectid
	 * @return
	 * @throws Exception
	 */
	private boolean checkAnnotations(Connection conn, Integer naid, String objectid) throws SQLException {
		boolean annotationsFound = false;

		// Single query to all annotations tables, limiting to finding first
		// applicable row in any table
		try (CallableStatement statement = StoreProcedureDataAccessUtils.callProcedure(conn, SP_CHECK_ANNOTATIONS, naid, objectid)) {
			ResultSet resultSet = statement.getResultSet();
			// Only one row needs to be returned for annotations to be
			// processed.
			if (resultSet.first()) {
				annotationsFound = true;
			} else {
				debug("CheckForAnnotationsStage: No annotations found");
			}
		}

		return annotationsFound;
	}

	/**
	 * Get the tags and add the info to the doc.
	 */
	private void getTags(Connection conn, AspireObject doc, Integer naid, String objectid,
			PublicContributionsXml publicContributionsXml) throws SQLException, AspireException {
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

			// Execute the stored procedure.
			try (CallableStatement statement = StoreProcedureDataAccessUtils.callProcedure(conn, SP_GET_TAGS, naid, objectid)) {
				ResultSet resultSet = statement.getResultSet();

				boolean firstFound = false;
				String latestDateTime = null;

				while (resultSet.next()) {
					String user = resultSet.getString("user_name");
					String tagText = resultSet.getString("annotation");
					String annotationObjectId = resultSet.getString("object_id");
					String tagCreated = formatter.format(new Date(resultSet.getTimestamp("annotation_date").getTime()));
					Boolean isNaraStaff = resultSet.getBoolean("is_nara_staff");
					Boolean displayNameFlag = resultSet.getBoolean("display_name_flag");
					String fullName = "";
					if (isNaraStaff || displayNameFlag) {
						fullName = resultSet.getString("full_name");
					}

					// Add info to public contributions xml
					if (idsMatch(objectid, annotationObjectId)) {
						// N.B., comparing the record's object id with the
						// annotation's object id
						// Add public contributions field
						publicContributionsXml.addTag(user, fullName, isNaraStaff, tagCreated, tagText);
					}

					// Add info to search fields
					if (firstFound == false) { // Looking at first record, which was
						// sorted by date ascending
						doc.add(TAG_FIRST_ELEMENT, tagCreated);
						firstFound = true;
					}

					// Object annotation: annotation objectId has a value
					// Description annotation: job objectid is null and annotation
					// objectId must be null or empty
					if (annotationObjectId != null && !annotationObjectId.isEmpty()) {
						doc.add(TAG_OBJECT_KEYWORD_ELEMENT, tagText);
					} else {
						doc.add(TAG_KEYWORD_ELEMENT, tagText);
					}

					latestDateTime = tagCreated;
				}

				if (latestDateTime != null) {
					doc.add(TAG_LAST_ELEMENT, latestDateTime);
				}
			}
	}

	/**
	 * Get the transcriptions and add the info to the doc.
	 */
	private void getTranscriptions(Connection conn, AspireObject doc, Integer naid, String objectid,
			PublicContributionsXml publicContributionsXml) throws AspireException, SQLException {

		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

			// Execute the stored procedure.
			try (CallableStatement statement = StoreProcedureDataAccessUtils.callProcedure(conn, SP_GET_TRANSCRIPTIONS, naid, objectid)) {

				ResultSet resultSet = statement.getResultSet();

				boolean firstFound = false;
				String latestDateTime = null;

				while (resultSet.next()) {
					String user = resultSet.getString("user_name");
					String annotationObjectId = resultSet.getString("object_id");
					String transcriptionText = resultSet.getString("annotation");
					String transcriptionCreated = formatter
							.format(new Date(resultSet.getTimestamp("annotation_date").getTime()));
					String versionNum = resultSet.getString("version_num");
					Boolean isNaraStaff = resultSet.getBoolean("is_nara_staff");
					Boolean displayNameFlag = resultSet.getBoolean("display_name_flag");
					String fullName = "";
					if (isNaraStaff || displayNameFlag) {
						fullName = resultSet.getString("full_name");
					}
					String status = resultSet.getString("status");

					// Add info to public contributions xml
					if (idsMatch(objectid, annotationObjectId)) {

						// N.B., comparing the record's object id with the
						// annotation's object id
						if (status.equals("1")) {
							publicContributionsXml.addTranscription(transcriptionCreated, versionNum, transcriptionText);
						}
						publicContributionsXml.addTranscriptionUsers(user, fullName, isNaraStaff, transcriptionCreated,
								versionNum);
					}

					// Add info to search fields
					if (firstFound == false) { // Looking at first record, which was
						// sorted by date ascending
						doc.add(TRANSCRIPTION_FIRST_ELEMENT, transcriptionCreated);
						firstFound = true;
					}

					// annotation text in result set is null for any other than the
					// current transcription
					if (transcriptionText != null) {
						doc.add(TRANSCRIPTION_KEYWORD_ELEMENT, transcriptionText);
					}

					latestDateTime = transcriptionCreated;
				}

				if (latestDateTime != null) {
					doc.add(TRANSCRIPTION_LAST_ELEMENT, latestDateTime);
				}
			}
	}

	/**
	 * Get the translations and add the info to the doc.
	 */
	private void getTranslations(Connection conn, AspireObject doc, Integer naid, String objectid,
			PublicContributionsXml publicContributionsXml) throws AspireException, SQLException {

		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

			// Execute the stored procedure.
			try (CallableStatement statement = StoreProcedureDataAccessUtils.callProcedure(conn, SP_GET_TRANSLATIONS, naid, objectid)) {

				ResultSet resultSet = statement.getResultSet();

				boolean firstFound = false;
				String latestDateTime = null;

				while (resultSet.next()) {
					String user = resultSet.getString("user_name");
					String translationText = resultSet.getString("annotation");
					String translationCreated = formatter
							.format(new Date(resultSet.getTimestamp("annotation_date").getTime()));
					String versionNum = resultSet.getString("version_num");
					Boolean isNaraStaff = resultSet.getBoolean("is_nara_staff");
					String language_iso = resultSet.getString("language_iso");
					Boolean displayNameFlag = resultSet.getBoolean("display_name_flag");
					String fullName = "";
					if (isNaraStaff || displayNameFlag) {
						fullName = resultSet.getString("full_name");
					}
					String status = resultSet.getString("status");

					if (status.equals("1")) {
						publicContributionsXml.addTranslation(translationCreated, versionNum, language_iso,
								translationText);
					}

					publicContributionsXml.addTranslationUsers(user, fullName, isNaraStaff, translationCreated, versionNum);

					// Add info to search fields
					if (firstFound == false) { // Looking at first record, which was
						// sorted by date ascending
						doc.add(TRANSLATION_FIRST_ELEMENT, translationCreated);
						firstFound = true;
					}

					// annotation text in result set is null for any other than the
					// current translation
					if (translationText != null) {
						doc.add(TRANSLATION_KEYWORD_ELEMENT, translationText);
					}

					latestDateTime = translationCreated;
				}

				if (latestDateTime != null) {
					doc.add(TRANSLATION_LAST_ELEMENT, latestDateTime);
				}
			}
	}

	/**
	 * Get the tags and add the info to the doc.
	 */
	private void getComments(Connection conn, AspireObject doc, Integer naid, String objectid,
			PublicContributionsXml publicContributionsXml) throws AspireException, SQLException {
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

			// Execute the stored procedure.
			try (CallableStatement statement = StoreProcedureDataAccessUtils.callProcedure(conn, SP_GET_COMMENTS, naid, objectid)) {
				ResultSet resultSet = statement.getResultSet();

				boolean firstFound = false;
				String latestDateTime = null;

				while (resultSet.next()) {

					String user = resultSet.getString("user_name");
					String commentText = resultSet.getString("annotation");
					String annotationObjectId = resultSet.getString("object_id");
					String commentCreated = formatter.format(new Date(resultSet.getTimestamp("annotation_date").getTime()));
					Boolean isNaraStaff = resultSet.getBoolean("is_nara_staff");
					Boolean displayNameFlag = resultSet.getBoolean("display_name_flag");
					int commentId = resultSet.getInt("comment_id");
					String fullName = "";

					debug("Comment ingested for objectid %s : %s", annotationObjectId, commentText);

					if (isNaraStaff || displayNameFlag) {
						fullName = resultSet.getString("full_name");
					}

					// Add public contributions field
					publicContributionsXml.addComment(user, fullName, isNaraStaff, commentCreated, commentText, commentId);

					// Add info to search fields
					if (firstFound == false) { // Looking at first record, which was
						// sorted by date ascending
						doc.add(COMMENT_FIRST_ELEMENT, commentCreated);
						firstFound = true;
					}

					// Object annotation: annotation objectId has a value
					// Description annotation: job objectid is null and annotation
					// objectId must be null or empty
					if (annotationObjectId != null && !annotationObjectId.isEmpty()) {
						doc.add(COMMENT_OBJECT_KEYWORD_ELEMENT, commentText);
					} else {
						doc.add(COMMENT_KEYWORD_ELEMENT, commentText);
					}

					latestDateTime = commentCreated;
				}

				if (latestDateTime != null) {
					doc.add(COMMENT_LAST_ELEMENT, latestDateTime);
				}
			}
	}

	/**
	 * Add info about all contributors and contributions to doc.
	 */
	private void getContributions(AspireObject doc) throws Exception {

		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

		// Get first contribution date
		String allContributionsFirstDate = null;
		String tagFirstDateTime = doc.getText(TAG_FIRST_ELEMENT);
		String transcriptionFirstDateTime = doc.getText(TRANSCRIPTION_FIRST_ELEMENT);
		String commentFirstDateTime = doc.getText(COMMENT_FIRST_ELEMENT);
		String translationFirstDateTime = doc.getText(TRANSLATION_FIRST_ELEMENT);
		if (tagFirstDateTime != null && transcriptionFirstDateTime != null && commentFirstDateTime != null
				&& translationFirstDateTime != null) {
			Date tagsFirst = formatter.parse(tagFirstDateTime);
			Date transcriptionFirst = formatter.parse(transcriptionFirstDateTime);
			Date commentFirst = formatter.parse(commentFirstDateTime);
			Date translationFirst = formatter.parse(translationFirstDateTime);

			allContributionsFirstDate = (tagsFirst.before(transcriptionFirst) ? tagFirstDateTime
					: transcriptionFirstDateTime);

			Date allContributionsFirst = formatter.parse(allContributionsFirstDate);

			allContributionsFirstDate = (commentFirst.before(allContributionsFirst) ? commentFirstDateTime
					: allContributionsFirstDate);

			allContributionsFirst = formatter.parse(allContributionsFirstDate);

			allContributionsFirstDate = (translationFirst.before(allContributionsFirst) ? translationFirstDateTime
					: allContributionsFirstDate);

		} else if (tagFirstDateTime != null) {
			allContributionsFirstDate = tagFirstDateTime;
		} else if (transcriptionFirstDateTime != null) {
			allContributionsFirstDate = transcriptionFirstDateTime;
		} else if (commentFirstDateTime != null) {
			allContributionsFirstDate = commentFirstDateTime;
		} else if (translationFirstDateTime != null) {
			allContributionsFirstDate = translationFirstDateTime;
		}

		if (allContributionsFirstDate != null) {
			doc.add(ALL_CONTRIBUTORS_FIRST_ELEMENT, allContributionsFirstDate);
		}

		// Get last contribution date
		String allContributionsLastDate = null;
		String tagLastDateTime = doc.getText(TAG_LAST_ELEMENT);
		String transcriptionLastDateTime = doc.getText(TRANSCRIPTION_LAST_ELEMENT);
		String commentLastDateTime = doc.getText(COMMENT_LAST_ELEMENT);
		String translationLastDateTime = doc.getText(TRANSLATION_LAST_ELEMENT);
		if (tagLastDateTime != null && transcriptionLastDateTime != null && commentLastDateTime != null
				&& translationLastDateTime != null) {
			Date tagsLast = formatter.parse(tagLastDateTime);
			Date transcriptionLast = formatter.parse(transcriptionLastDateTime);
			Date commentLast = formatter.parse(commentLastDateTime);
			Date translationLast = formatter.parse(translationLastDateTime);

			allContributionsLastDate = (tagsLast.before(transcriptionLast) ? transcriptionLastDateTime
					: tagLastDateTime);

			Date allContributionsLast = formatter.parse(allContributionsLastDate);

			allContributionsLastDate = (commentLast.before(allContributionsLast) ? allContributionsLastDate
					: commentLastDateTime);

			allContributionsLast = formatter.parse(allContributionsLastDate);

			allContributionsLastDate = (translationLast.before(allContributionsLast) ? allContributionsLastDate
					: translationLastDateTime);

		} else if (tagLastDateTime != null) {
			allContributionsLastDate = tagLastDateTime;
		} else if (transcriptionLastDateTime != null) {
			allContributionsLastDate = transcriptionLastDateTime;
		} else if (commentLastDateTime != null) {
			allContributionsLastDate = commentLastDateTime;
		} else if (translationLastDateTime != null) {
			allContributionsLastDate = translationLastDateTime;
		}

		if (allContributionsLastDate != null) {
			doc.add(ALL_CONTRIBUTORS_LAST_ELEMENT, allContributionsLastDate);
		}

	}

	private void setObjectPublicContributions(AspireObject publicContributions, JobInfo info) throws AspireException {
		AspireObject object = info.getDigitalObject();
		if (object != null) {
			object.set(publicContributions.clone());
		}
	}

	private void setPublicContributionsInParentDescription(AspireObject publicContributions, String objectId,
			JobInfo info) throws AspireException {

		Lock lock = info.getObjectsXmlLock();
		lock.lock();
		try {
			AspireObject parentDoc = info.getParent().getJobData();
			AspireObject objects = parentDoc.get(ObjectsXml.OBJECTS_XML_TOP_LEVEL_ELEMENT);
			AspireObject object = ObjectsXml.findObject(objectId, objects);
			object.set(publicContributions.clone());
		} finally {
			lock.unlock();
		}
	}
}
