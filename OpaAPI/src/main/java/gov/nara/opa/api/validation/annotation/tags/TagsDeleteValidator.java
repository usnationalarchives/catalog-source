package gov.nara.opa.api.validation.annotation.tags;

import gov.nara.opa.api.security.OPAAuthenticationProvider;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.api.validation.OpaApiAbstractValidator;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.dataaccess.annotation.tags.TagDao;
import gov.nara.opa.common.valueobject.annotation.tags.TagValueObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class TagsDeleteValidator extends OpaApiAbstractValidator {

	@Autowired
	TagDao tagDao;

	public static final String TAG_VALUE_OBJECT_KEY = "tag";

	@Override
	public void performCustomValidation(ValidationResult validationResult,
			HttpServletRequest request) {
		try {
			TagsDeleteRequestParameters requestParameters = (TagsDeleteRequestParameters) validationResult
					.getValidatedRequest();

			List<TagValueObject> tags = tagDao.selectAllTags(
					requestParameters.getNaId(),
					requestParameters.getObjectId(),
					URLDecoder.decode(requestParameters.getText(), "UTF-8"),
					true);
			validateTagExists(tags, validationResult);
			if (!validationResult.isValid()) {
				return;
			}
			TagValueObject foundTag = tags.get(0);
			validationResult.addContextObject(TAG_VALUE_OBJECT_KEY, foundTag);
			validateOwner(foundTag, validationResult);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void validateTagExists(List<TagValueObject> tags,
			ValidationResult validationResult) {
		if (tags == null || tags.size() == 0) {
			ValidationError error = new ValidationError();
			error.setErrorCode(ErrorCodeConstants.TAG_NOT_FOUND);
			error.setErrorMessage(ErrorConstants.ACTIVE_TAG_NOT_FOUND);
			validationResult.setHttpStatus(HttpStatus.NOT_FOUND);
			validationResult.addCustomValidationError(error);
		}
	}

	private void validateOwner(TagValueObject tag,
			ValidationResult validationResult) {
		Integer currentUserId = OPAAuthenticationProvider
				.getAccountIdForLoggedInUser();
		if (!currentUserId.equals(tag.getAccountId())) {
			ValidationError error = new ValidationError();
			error.setErrorCode(ErrorCodeConstants.NOT_OWNER);
			error.setErrorMessage(ErrorConstants.TAG_NOT_OWNER);
			validationResult.addCustomValidationError(error);
		}
	}

}
