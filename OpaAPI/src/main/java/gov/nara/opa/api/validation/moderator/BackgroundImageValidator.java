package gov.nara.opa.api.validation.moderator;

import java.util.LinkedHashSet;

import javax.servlet.http.HttpServletRequest;

import gov.nara.opa.api.dataaccess.moderator.BackgroundImageDao;
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.utils.PageNumberUtils;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.api.validation.OpaApiAbstractValidator;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.api.valueobject.moderator.BackgroundImageValueObject;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ValidationResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BackgroundImageValidator extends OpaApiAbstractValidator {

	@Value("${configFilePath}")
	private String configFilePath;

	@Autowired
	private PageNumberUtils pageNumberUtils;

	@Autowired
	private ConfigurationService configService;

	@Autowired
	private BackgroundImageDao backgroundImageDao;

	@Override
	public void performCustomValidation(ValidationResult validationResult,
			HttpServletRequest request) {
		if (validationResult.isValid()) {
			CreateDeleteBackgroundImageRequestParameters requestParameters = (CreateDeleteBackgroundImageRequestParameters) validationResult
					.getValidatedRequest();
			validateIds(requestParameters, validationResult);
			validateUniqueness(requestParameters, validationResult);
		}
	}

	@Override
	protected LinkedHashSet<String> getOrderedValidatedItemCodes() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Validates if the naId and objectId have a valid value
	 * 
	 * @param annotationParameters
	 *            annotation parameters sent
	 * @return True if the validation process was successful, false otherwise
	 */
	protected boolean validateIds(
			CreateDeleteBackgroundImageRequestParameters requestParameters,
			ValidationResult validationResult) {

		if (requestParameters.getNaId() != null) {
			/*
			 * Validate naId
			 */
			boolean validNaId = validateNaId(requestParameters.getNaId());
			if (!validNaId) {
				ValidationUtils.setValidationError(validationResult,
						ArchitectureErrorCodeConstants.INVALID_ID_VALUE,
						ErrorConstants.INVALID_NA_ID, "naId");
				return false;
			}
		}

		if (requestParameters.getNaId() != null
				&& (requestParameters.getObjectId() != null)) {

			/*
			 * Validate naId AND objectId
			 */
			boolean validObjectId = validateObjectId(
					requestParameters.getObjectId(),
					requestParameters.getNaId());
			if (!validObjectId) {
				ValidationUtils.setValidationError(validationResult,
						ArchitectureErrorCodeConstants.INVALID_ID_VALUE,
						ErrorConstants.INVALID_OBJECT_ID, "objectId");
				return false;
			}
		}

		return true;
	}

	protected void validateUniqueness(
			CreateDeleteBackgroundImageRequestParameters requestParameters,
			ValidationResult validationResult) {

		if (requestParameters.getNaId() != null
				&& requestParameters.getObjectId() != null) {
			BackgroundImageValueObject backgroundImage = backgroundImageDao
					.getBackgroundImage(requestParameters.getNaId(),
							requestParameters.getObjectId(), false);
			// if already exists
			if (backgroundImage != null) {
				ValidationUtils.setValidationError(validationResult,
						ErrorCodeConstants.DUPLICATE_BACKGROUND_IMAGE,
						ErrorConstants.INVALID_BACKGROUND_IMAGE_ALREADY_EXISTS,
						"naId");
			}
		}
	}
}
