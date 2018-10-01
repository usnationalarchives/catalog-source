package gov.nara.opa.api.validation.moderator;

import java.util.LinkedHashSet;

import javax.servlet.http.HttpServletRequest;

import gov.nara.opa.api.services.moderator.OnlineAvailabilityHeaderService;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.api.validation.OpaApiAbstractValidator;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.api.valueobject.moderator.OnlineAvailabilityHeaderValueObject;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorMessageConstants;
import gov.nara.opa.architecture.web.validation.ValidationResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

@Component
public class OnlineAvailabilityHeaderValidator extends OpaApiAbstractValidator {

	@Autowired
	private OnlineAvailabilityHeaderService onlineAvailabilityHeaderService;

	public static final String NA_ID_FIELD_NAME = "naId";

	@Override
	public void performCustomValidation(ValidationResult validationResult,
			HttpServletRequest request) {
		if (validationResult.isValid()) {
			OnlineAvailabilityHeaderRequestParameters requestParameters = (OnlineAvailabilityHeaderRequestParameters) validationResult
					.getValidatedRequest();

			if (requestParameters.getNaId() == null) {
				ValidationUtils
						.setValidationError(
								validationResult,
								ErrorCodeConstants.MISSING_VALUE,
								String.format(
										ArchitectureErrorMessageConstants.INVALID_INTEGER,
										NA_ID_FIELD_NAME), NA_ID_FIELD_NAME);
			}

			if (!validateNaId(requestParameters.getNaId())) {
				ValidationUtils.setValidationError(validationResult,
						ArchitectureErrorCodeConstants.INVALID_ID_VALUE,
						ErrorConstants.INVALID_NA_ID, NA_ID_FIELD_NAME);
			}

			if (request.getMethod().equals(RequestMethod.PUT)) {
				OnlineAvailabilityHeaderValueObject onlineAvailabilityHeader = onlineAvailabilityHeaderService
						.getOnlineAvailabilityHeaderByNaId(requestParameters
								.getNaId());
				if (onlineAvailabilityHeader.getHeader() == null
						&& onlineAvailabilityHeader.getAvailabilityTS() == null) {
					validateUniqueness(requestParameters, validationResult);
				}
			}
		}
	}

	@Override
	protected LinkedHashSet<String> getOrderedValidatedItemCodes() {
		// TODO Auto-generated method stub
		return null;
	}

	protected void validateUniqueness(
			OnlineAvailabilityHeaderRequestParameters requestParameters,
			ValidationResult validationResult) {

		if (requestParameters.getNaId() != null) {
			OnlineAvailabilityHeaderValueObject onlineAvailabilityHeader = onlineAvailabilityHeaderService
					.getOnlineAvailabilityHeaderByNaId(requestParameters
							.getNaId());
			// if already exists
			if (onlineAvailabilityHeader != null) {
				ValidationUtils
						.setValidationError(
								validationResult,
								ErrorCodeConstants.DUPLICATE_ONLINE_AVAILABILITY_HEADER,
								ErrorConstants.INVALID_ONLINE_AVAILABILITY_HEADER_ALREADY_EXISTS,
								"naId");
			}
		}
	}
}
