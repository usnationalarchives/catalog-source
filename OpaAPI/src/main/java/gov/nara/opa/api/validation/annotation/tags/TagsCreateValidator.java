package gov.nara.opa.api.validation.annotation.tags;

import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.system.Config;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.utils.PageNumberUtils;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.api.validation.annotation.AnnotationsCommonValidator;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.dataaccess.annotation.tags.TagDao;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

//import gov.nara.opa.api.validation.FieldConstraintConstants;

@Component
public class TagsCreateValidator extends AnnotationsCommonValidator {

	@Value("${configFilePath}")
	private String configFilePath;

	public static final String TAGS_TEXT_FIELD_NAME = "text";
	public static final String PAGE_NUMBER_FIELD_NAME = "pageNum";
	public static final String NA_ID_FIELD_NAME = "naId";
	public static final String OBJECT_ID_FIELD_NAME = "objectId";

	@Autowired
	private TagDao tagDao;

	@Autowired
	private ConfigurationService configService;

	@Autowired
	private PageNumberUtils pageNumberUtils;

	@Override
	public void performCustomValidation(ValidationResult validationResult,
			HttpServletRequest request) {
		if (validationResult.isValid()) {
			TagsCreateRequestParameters tagsParameters = (TagsCreateRequestParameters) validationResult
					.getValidatedRequest();
			if (validateTagsText(tagsParameters.getTextList(), validationResult)
					&& validateIds(tagsParameters, validationResult)
					&& validateUniqueness(tagsParameters, validationResult,
							tagsParameters.getNaId(),
							tagsParameters.getObjectId())
							&& validatePageNum(tagsParameters, validationResult)) {

			}
		}
	}

	private boolean validateTagsText(List<String> tags,
			ValidationResult validationResult) {
		for (String tag : tags) {
			if (!(validateTagTextLength(tag, validationResult))) {
				return false;
			}
			if (!(validateTagContent(tag, validationResult))) {
				return false;
			}
		}

		return true;
	}

	private boolean validateTagContent(String tag,
			ValidationResult validationResult) {
		return true;
	}

	private boolean validateTagTextLength(String tag,
			ValidationResult validationResult) {

		Config config = configService.getConfig(configFilePath);

		// If the config.xml file returns successfully
		File file = new File(configFilePath);

		// If the config.xml file returns successfully
		if (config == null || !file.exists()) {
			ValidationUtils.setValidationError(validationResult,
					ArchitectureErrorCodeConstants.CONFIG_FILE_NOT_FOUND,
					ErrorConstants.CONFIG_FILE_NOT_FOUND, TAGS_TEXT_FIELD_NAME);

			return false;
		} else if (tag.length() > config.getTagsLength()) {
			ValidationUtils.setValidationError(validationResult,
					ArchitectureErrorCodeConstants.INVALID_PARAMETER_VALUE,
					String.format(ErrorConstants.INVALID_TAG_SIZE,
							tag.length(), config.getTagsLength()),
							TAGS_TEXT_FIELD_NAME);

			return false;
		}

		return true;
	}

	private boolean validateIds(TagsCreateRequestParameters tagsParameters,
			ValidationResult validationResult) {

		if (tagsParameters.getNaId() != null
				&& (tagsParameters.getObjectId() == null
				|| tagsParameters.getObjectId().equals("") || tagsParameters
				.getObjectId().equals("0"))) {
			// Validate naId
			boolean validNaId = validateNaId(tagsParameters.getNaId());
			if (!validNaId) {
				ValidationUtils.setValidationError(validationResult,
						ArchitectureErrorCodeConstants.INVALID_ID_VALUE,
						ErrorConstants.INVALID_NA_ID, NA_ID_FIELD_NAME);

				return false;
			}
		} else if (tagsParameters.getNaId() != null
				&& (tagsParameters.getObjectId() != null)) {

			// Validate objectId
			boolean validObjectId = validateObjectId(
					tagsParameters.getObjectId(), tagsParameters.getNaId());
			if (!validObjectId) {
				ValidationUtils.setValidationError(validationResult,
						ArchitectureErrorCodeConstants.INVALID_ID_VALUE,
						ErrorConstants.INVALID_OBJECT_ID, OBJECT_ID_FIELD_NAME);

				return false;
			}
		}

		return true;
	}

	private boolean validateUniqueness(
			TagsCreateRequestParameters tagsParameters,
			ValidationResult validationResult, String naId, String objectId) {
		List<String> tags = tagsParameters.getTextList();
		List<String> tagsInTheDatabase = tagDao
				.selectTagValuesByNaIdAndObjectId(naId, objectId, true);
		HashMap<String, String> tagsInDatabaseHashMap = new LinkedHashMap<String, String>();
		for (String tag : tagsInTheDatabase) {
			tagsInDatabaseHashMap.put(tag.toLowerCase(), tag);
		}

		ArrayList<String> tagsToAdd = new ArrayList<String>();
		StringBuilder tagsWithError = new StringBuilder();

		boolean tagsAreValid = true;

		for (String tag : tags) {
			String trimmedTag = StringUtils.replaceMultipleWhiteSpaces(tag
					.trim());
			String lowerCaseTag = trimmedTag.toLowerCase();
			boolean notDuplicate = true;
			boolean notAdded = true;

			// Validate tags are not duplicated in the input list
			for (String tagToAdd : tagsToAdd) {
				if (lowerCaseTag.equals(tagToAdd.toLowerCase())) {
					if (tagsWithError.length() > 0) {
						tagsWithError.append(", ");
					}
					tagsWithError.append(trimmedTag);
					notAdded = false;
					tagsAreValid = false;
					break;
				}
			}

			// Validate tag is not in the database
			if (tagsInDatabaseHashMap.containsKey(lowerCaseTag)) {
				if (tagsWithError.length() > 0) {
					tagsWithError.append(", ");
				}
				tagsWithError.append(trimmedTag);
				notDuplicate = false;
				tagsAreValid = false;
			}

			if (notDuplicate && notAdded) {
				tagsToAdd.add(trimmedTag);
			}

		}
		if (tagsAreValid) {
			tagsParameters.setTextList(tagsToAdd);
			return true;
		} else {
			addDuplicatedTagValidationError(tagsWithError, validationResult);
			return false;
		}

	}

	private boolean validatePageNum(TagsCreateRequestParameters tagsParameters,
			ValidationResult validationResult) {
		String objectId = tagsParameters.getObjectId();
		int pageNum = tagsParameters.getPageNum();

		if (tagsParameters.getApiType().equals(Constants.PUBLIC_API_PATH)
				&& pageNum <= 0) {
			pageNum = pageNumberUtils.getPageNumber(
					tagsParameters.getApiType(), 
					tagsParameters.getNaId(),
					objectId);
		}

		if (objectId != null && !objectId.isEmpty() && pageNum <= 0) {
			ValidationUtils.setValidationError(validationResult,
					ArchitectureErrorCodeConstants.INVALID_PARAMETER_VALUE,
					ErrorConstants.INVALID_TAG_PAGE_NUMBER,
					PAGE_NUMBER_FIELD_NAME);

			return false;
		}

		return true;
	}

	private void addDuplicatedTagValidationError(StringBuilder tags,
			ValidationResult validationResult) {
		ValidationUtils.setValidationError(
				validationResult,
				ErrorCodeConstants.DUPLICATE_ANNOTATION,
				String.format(ErrorConstants.INVALID_TAG_ALREADY_EXISTS,
						tags.toString()), TAGS_TEXT_FIELD_NAME);
	}

}
