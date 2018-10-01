package gov.nara.opa.api.services.impl.annotation.tags;

import gov.nara.opa.api.annotation.TagErrorCode;
import gov.nara.opa.api.dataaccess.annotation.logs.AnnotationLogDao;
import gov.nara.opa.api.dataaccess.user.UserAccountDao;
import gov.nara.opa.api.security.OPAAuthenticationProvider;
import gov.nara.opa.api.services.annotation.tags.CreateTagsService;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.utils.PageNumberUtils;
import gov.nara.opa.api.validation.annotation.tags.TagsCreateRequestParameters;
import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.architecture.web.valueobject.CommonValueObjectConstants;
import gov.nara.opa.common.dataaccess.annotation.tags.TagDao;
import gov.nara.opa.common.valueobject.annotation.logs.AnnotationLogValueObject;
import gov.nara.opa.common.valueobject.annotation.logs.AnnotationLogValueObjectHelper;
import gov.nara.opa.common.valueobject.annotation.tags.TagValueObject;
import gov.nara.opa.common.valueobject.annotation.tags.TagValueObjectHelper;
import gov.nara.opa.common.valueobject.annotation.tags.TagsCollectionValueObject;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.searchtechnologies.aspire.services.AspireObject;

@Component
public class CreateTagsServiceImpl implements CreateTagsService {

	private static OpaLogger log = OpaLogger
			.getLogger(CreateTagsServiceImpl.class);

	@Autowired
	private TagDao tagDao;

	@Autowired
	private AnnotationLogDao annotationLogDao;

	// to be replaced later once User account refactoring is completed
	@Autowired
	private UserAccountDao userAccountDao;

	@Autowired
	private PageNumberUtils pageNumberUtils;

	@Override
	@Transactional
	public TagsCollectionValueObject createTags(
			TagsCreateRequestParameters tagsRequest)
			throws BadSqlGrammarException, DataAccessException,
			UnsupportedEncodingException {

		// Get session user account
		UserAccountValueObject userAccount = getSessionUserAccount();

		List<TagValueObject> tags = new ArrayList<TagValueObject>();

		for (String tagText : tagsRequest.getTextList()) {
			try {

				tagText = StringUtils
						.replaceMultipleWhiteSpaces(tagText.trim());

				log.info(String.format("Tag text in service: %1$s", tagText));

				TagValueObject tag = TagValueObjectHelper.createTagForInsert(
						userAccount.getAccountId(), tagsRequest.getNaId(),
						tagsRequest.getObjectId(), tagText,
						tagsRequest.getPageNum());
				tagDao.createTag(tag);
				tag.setUserName(userAccount.getUserName());
				tag.setFullName(userAccount.getFullName());
				tag.setIsNaraStaff(userAccount.isNaraStaff());
				tag.setDisplayNameFlag(userAccount.getDisplayFullName());
				tags.add(tag);

				AnnotationLogValueObject annotationLog = AnnotationLogValueObjectHelper
						.createAnnotationLogForInsert(tag,
								tagsRequest.getHttpSessionId(),
								CommonValueObjectConstants.ACTION_ADD);
				annotationLogDao.insert(annotationLog);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return new TagsCollectionValueObject(tags);
	}

	@Override
	public TagsCollectionValueObject createTags(String apiType,
			String sessionId, ValidationResult validationResult,
			List<AspireObject> aspireObjectList) {

		List<AspireObject> errorTags = new ArrayList<AspireObject>();
		LinkedHashMap<String, List<String>> tagSetsInDatabase = new LinkedHashMap<String, List<String>>();
		List<TagValueObject> tagsToAdd = new ArrayList<TagValueObject>();

		try {

			// Get session user account
			UserAccountValueObject userAccount = getSessionUserAccount();

			// Process each tag
			for (AspireObject tagInstance : aspireObjectList) {

				// Get values
				String tagText = tagInstance.getText("text");
				String naId = tagInstance.getText("naId");
				String objectId = tagInstance.getText("objectId");
				boolean tagIsValid = true;

				// Temporary validation
				// TODO: Implement validation consistent with normal tag input
				if (tagText == null || tagText.isEmpty() || naId == null
						|| naId.isEmpty()) {
					errorTags.add(tagInstance);
				} else {

					// Validate naId
					tagIsValid = pageNumberUtils.isValidNaId(apiType, naId);
					if (!tagIsValid) {
						ValidationError validationError = new ValidationError();
						validationError
								.setErrorCode(ArchitectureErrorCodeConstants.INVALID_ID_VALUE);
						validationError.setErrorMessage(String.format(
								ErrorConstants.INVALID_ID_VALUE_IMPORT, naId));
						validationError.setFieldValidationError(true);
						validationResult
								.addCustomValidationError(validationError);
					}

					int pageNumber = 0;

					if (tagIsValid && !StringUtils.isNullOrEmtpy(objectId)) {
						// If has object Id, retrieve page number
						pageNumber = pageNumberUtils.getPageNumber(apiType,
								naId, objectId);

						if (pageNumber == 0) {
							tagIsValid = false;
							errorTags.add(tagInstance);

							ValidationError error = new ValidationError();
							error.setErrorCode(TagErrorCode.CREATE_TAG_FAILED
									.toString());
							error.setErrorMessage(String
									.format(ErrorConstants.INVALID_TAG_PAGE_NUMBER_NAID_OBJECTID,
											naId, objectId));

							validationResult.addCustomValidationError(error);
						}
					}

					if (tagIsValid) {
						// Get tags in database for current tag set
						// Tag set key
						String tagSetKey = naId + "-"
								+ (objectId != null ? objectId : "");

						if (!tagSetsInDatabase.containsKey(tagSetKey)) {
							// insert tag set in hash map
							List<String> tagsInDatabase = tagDao
									.selectTagValuesByNaIdAndObjectId(naId,
											objectId, true);
							if (tagsInDatabase != null
									&& tagsInDatabase.size() > 0) {
								tagSetsInDatabase
										.put(tagSetKey, tagsInDatabase);
							} else {
								tagSetsInDatabase.put(tagSetKey,
										new ArrayList<String>());
							}
						}

						// Split tag text and process each tag
						// TODO: Implement for quoted tags like "City, State"
						String[] tags = tagText.split(",");
						for (String tag : tags) {
							// Verify if tag is in the database
							String cleanedTag = StringUtils
									.replaceMultipleWhiteSpaces(tag.trim());
							String lowerCaseTag = cleanedTag.toLowerCase();

							if (!isInArray(lowerCaseTag,
									tagSetsInDatabase.get(tagSetKey))) {
								// Add to array
								tagSetsInDatabase.get(tagSetKey)
										.add(cleanedTag);

								// Add value object to tags to add array
								TagValueObject tagValueObject = TagValueObjectHelper
										.createTagForInsert(
												userAccount.getAccountId(),
												naId, objectId, cleanedTag,
												pageNumber);

								tagsToAdd.add(tagValueObject);
							} else {
								// Add to error array
								errorTags.add(tagInstance);
							}
						}

					}

				}
			}

			// Put tags with errors in validation object
			if (validationResult.isValid() && errorTags.size() > 0) {
				ValidationError error = new ValidationError();
				error.setErrorCode(TagErrorCode.CREATE_TAG_FAILED.toString());
				error.setErrorMessage(ErrorConstants.TAG_IMPORT_ERRORS);

				validationResult.addCustomValidationError(error);
			}

			if (validationResult.isValid()) {
				// Insert valid tags into database
				for (TagValueObject tag : tagsToAdd) {
					tagDao.createTag(tag);
					tag.setUserName(userAccount.getUserName());
					tag.setFullName(userAccount.getFullName());
					tag.setIsNaraStaff(userAccount.isNaraStaff());
					tag.setDisplayNameFlag(userAccount.getDisplayFullName());

					AnnotationLogValueObject annotationLog = AnnotationLogValueObjectHelper
							.createAnnotationLogForInsert(tag, sessionId,
									CommonValueObjectConstants.ACTION_ADD);
					annotationLogDao.insert(annotationLog);
				}
			}

		} catch (Exception e) {
			log.error(e);
			throw new OpaRuntimeException(e);
		}

		return new TagsCollectionValueObject(tagsToAdd);
	}

	private boolean isInArray(String lowerCaseTag, List<String> list) {

		for (String tagInList : list) {
			if (tagInList.toLowerCase().equals(lowerCaseTag)) {
				return true;
			}
		}

		return false;
	}

	private UserAccountValueObject getSessionUserAccount() {
		Integer accountId = OPAAuthenticationProvider
				.getAccountIdForLoggedInUser();

		return userAccountDao.selectByAccountId(accountId);
	}

}
