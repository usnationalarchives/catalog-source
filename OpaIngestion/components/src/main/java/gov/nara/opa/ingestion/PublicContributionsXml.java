/**
 * Copyright Search Technologies 2014
 * for NARA OPA
 */
package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;

/**
 * Creates and updates a PublicContributions XML field as an AspireObject.
 *
 * @author OPA Ingestion Team
 */
public class PublicContributionsXml {

	AspireObject contributions;
	int tagCount = 0;
	int commentCount = 0;
	int transcriptionUserCount = 0;
	int translationUserCount = 0;

	public PublicContributionsXml() {
		contributions = new AspireObject("publicContributions");
	}

	public AspireObject getContent() {
		return contributions;
	}

	/**
	 * Add a comment to the public contributions xml.
	 */
	public void addComment(String userName, String fullName,
			boolean isNaraStaff, String createdTs, String commentText, int commentId)
			throws AspireException {

		AspireObject comments = contributions.get("comments");
		if (comments == null) {
			comments = new AspireObject("comments");
			contributions.set(comments);
		}

		AspireObject commentObject = new AspireObject("comment");
		commentObject.setAttribute("user", userName);
		commentObject.setAttribute("id", commentId+"");
		if (fullName != null && !fullName.equals("")) {
			commentObject.setAttribute("fullName", fullName);
		}
		commentObject.setAttribute("isNaraStaff", "" + isNaraStaff);
		commentObject.setAttribute("created", createdTs);
		commentObject.setContent(commentText);
		comments.add(commentObject);

		commentCount++;
		comments.setAttribute("total", "" + commentCount);

	}

	/**
	 * Add a tag to the public contributions xml.
	 */
	public void addTag(String userName, String fullName, boolean isNaraStaff,
			String createdTs, String tagText) throws AspireException {

		AspireObject tags = contributions.get("tags");
		if (tags == null) {
			tags = new AspireObject("tags");
			contributions.set(tags);
		}

		AspireObject tagObject = new AspireObject("tag");
		tagObject.setAttribute("user", userName);
		if (fullName != null && !fullName.equals("")) {
			tagObject.setAttribute("fullName", fullName);
		}
		tagObject.setAttribute("isNaraStaff", "" + isNaraStaff);
		tagObject.setAttribute("created", createdTs);
		tagObject.setContent(tagText);
		tags.add(tagObject);

		tagCount++;
		tags.setAttribute("total", "" + tagCount);

	}

	/**
	 * Add a transcription to the public contributions xml.
	 */
	public void addTranscription(String lastModifiedTs, String version,
			String transcriptionText) throws AspireException {
		AspireObject transcription = contributions.get("transcription");
		if (transcription == null) {
			transcription = new AspireObject("transcription");
			contributions.set(transcription);
		}

		transcription.setAttribute("lastModified", lastModifiedTs);
		transcription.setAttribute("version", version);
		transcription.add("text", transcriptionText);
	}

	/**
	 * Add a transcription user to the public contributions xml.
	 */
	public void addTranscriptionUsers(String userName, String fullName,
			boolean isNaraStaff, String lastModifiedTs, String version)
			throws AspireException {
		AspireObject transcription = contributions.get("transcription");
		if (transcription == null) {
			transcription = new AspireObject("transcription");
			contributions.set(transcription);
		}
		AspireObject users = transcription.get("users");
		if (users == null) {
			users = new AspireObject("users");
			transcription.set(users);
		}

		AspireObject userObject = new AspireObject("user");
		userObject.setAttribute("user", userName);
		if (fullName != null && !fullName.equals("")) {
			userObject.setAttribute("fullName", fullName);
		}
		userObject.setAttribute("isNaraStaff", "" + isNaraStaff);
		userObject.setAttribute("lastModified", lastModifiedTs);
		userObject.setAttribute("version", version);
		users.add(userObject);

		transcriptionUserCount++;
		users.setAttribute("total", "" + transcriptionUserCount);
	}

	/**
	 * Add a translation to the public contributions xml.
	 */
	public void addTranslation(String lastModifiedTs, String version,
			String languageIso, String translationText) throws AspireException {
		AspireObject translation = contributions.get("translation");
		if (translation == null) {
			translation = new AspireObject("translation");
			contributions.set(translation);
		}

		translation.setAttribute("lastModified", lastModifiedTs);
		translation.setAttribute("version", version);
		translation.setAttribute("languageIso", languageIso);
		translation.add("text", translationText);
	}

	/**
	 * Add a translation user to the public contributions xml.
	 */
	public void addTranslationUsers(String userName, String fullName,
			boolean isNaraStaff, String lastModifiedTs, String version)
			throws AspireException {
		AspireObject translation = contributions.get("translation");
		if (translation == null) {
			translation = new AspireObject("translation");
			contributions.set(translation);
		}
		AspireObject users = translation.get("users");
		if (users == null) {
			users = new AspireObject("users");
			translation.set(users);
		}

		AspireObject userObject = new AspireObject("user");
		userObject.setAttribute("user", userName);
		if (fullName != null && !fullName.equals("")) {
			userObject.setAttribute("fullName", fullName);
		}
		userObject.setAttribute("isNaraStaff", "" + isNaraStaff);
		userObject.setAttribute("lastModified", lastModifiedTs);
		userObject.setAttribute("version", version);
		users.add(userObject);

		translationUserCount++;
		users.setAttribute("total", "" + translationUserCount);
	}
}
