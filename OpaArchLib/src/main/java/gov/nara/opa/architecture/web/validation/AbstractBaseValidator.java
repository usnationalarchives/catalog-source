package gov.nara.opa.architecture.web.validation;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.ObjectsXmlUtils;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.web.validation.annotation.HttpParameterNameRenamingProcessor;
import gov.nara.opa.architecture.web.validation.constraint.messageinterpolator.MessageInterpolator;
import gov.nara.opa.common.NumericConstants;
import gov.nara.opa.common.storage.OpaStorage;
import gov.nara.opa.common.storage.OpaStorageFactory;
import gov.nara.opa.common.storage.StorageUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

/**
 * Abstract super class that should be extended by all application validators.
 * This class provides default implementation for the validate method performing
 * the bulk needed to map Spring validation errors to Opa validation errors that
 * contain error messages/codes specific to our application
 * 
 * @author aolaru
 * @date Jun 3, 2014
 * 
 */

public abstract class AbstractBaseValidator implements Validator {

	@Autowired
	OpaStorageFactory opaStorageFactory;


	/**
	 * Hook that allows subclasses to perform additional validations. This
	 * method will be called by the parent at the end of the validation message.
	 * The passed in validationResult will have all validation errors collected
	 * up to this point - these include field validation errors and global
	 * validation errors created as the result of the spring binding and the
	 * validation performed on the javax.validation field annotations.
	 * 
	 * @param validationResult
	 *            validationResult populated with all validation errors
	 *            collected by the AbstractBaseValidator. The subclasses are
	 *            supposed to add any additional validation errors generated as
	 *            a result of custom validations.
	 */
	protected abstract void performCustomValidation(
			ValidationResult validationResult, HttpServletRequest request);

	/**
	 * To be used by subclasses if developers intend to provide an ordered list
	 * of validatedItemCodes (these are typically field names) based on which
	 * the ordering of which validations should be returned first. If multiple
	 * validation errors are generated the AbstractBaseValidator will walk
	 * through the list returned by this method and send back to the client the
	 * validation error for the first field in the list that is found. If no
	 * codes in this list have a validation error associated with them then the
	 * first error in the validation errors list will be retuned
	 * 
	 * @return a list/set of validated item codes (e.g. field names)
	 */
	protected LinkedHashSet<String> getOrderedValidatedItemCodes() {
		return null;
	}

	/**
	 * To be overriden if subclasses want to provide their own message
	 * interpolator. If the return is null the abstract class will use the
	 * DefaultErrorMessagesInterpolator.
	 * 
	 * @return The MessageInterpolator to be used to map Spring error
	 *         messages/codes to Opa error messages/code
	 */

	protected MessageInterpolator getCustomMessageInterpolator() {
		return null;
	}

	private MessageInterpolator defaultMessageInterpolator = new DefaultErrorMessagesInterpolator();

	static OpaLogger logger = OpaLogger.getLogger(AbstractBaseValidator.class);

	@Override
	public ValidationResult validate(BindingResult bindingResult,
			HttpServletRequest request, String apiType) {

		ValidationResult validationResult = new ValidationResult();
		AbstractRequestParameters parameters = (AbstractRequestParameters) bindingResult
				.getTarget();

		if (logger.isTraceEnabled()) {
			logger.trace("Parameters being validated:\n" + parameters);
		}
		validationResult.setValidatedRequest(parameters);

		processGlobalErrors(bindingResult, validationResult);
		processFieldErrors(bindingResult, validationResult);
		validateApiType(validationResult, apiType, request);
		validateNoExtraParameters(parameters, request, validationResult);
		validateSingleQueryParameters(request, validationResult);
		validateTypes(parameters, request.getParameterMap(), validationResult);
		performCustomValidation(validationResult, request);
		if (validationResult.getHttpStatus() == null) {
			validationResult.setHttpStatus(HttpStatus.BAD_REQUEST);
		}
		validationResult
		.determineDefaultErrorMessage(getOrderedValidatedItemCodes());
		if (logger.isTraceEnabled()) {
			logger.trace("Result of validation: \n" + validationResult);
		}
		return validationResult;
	}

	private static void validateSingleQueryParameters(
			HttpServletRequest request, ValidationResult validationResult) {
		Map<String, String[]> queryParameters = request.getParameterMap();
		for (String parameterName : queryParameters.keySet()) {
			String[] parameterValue = queryParameters.get(parameterName);
			if (parameterValue != null && parameterValue.length > 1) {
				ValidationError error = new ValidationError();
				error.setErrorCode(ArchitectureErrorCodeConstants.INVALID_PARAMETER);
				error.setErrorMessage(String.format(
						ArchitectureErrorMessageConstants.MULTIPLE_PARAMETERS,
						parameterName));
				validationResult.addCustomValidationError(error);
			}
		}
	}

	private static void validateApiType(ValidationResult validationResult,
			String apiType, HttpServletRequest request) {
		if (apiType == null) {
			return;
		}
		if (request == null) {
			throw new OpaRuntimeException(
					"The http request needs to be passed as a parameter if a single api type value needs to be validated.");
		}
		String requestPath = request.getServletPath();
		if (!requestPath.contains("/" + apiType + "/")) {
			removePriorInvalidApyTypeError(validationResult.getErrors());
			validationResult.setValid(false);
			ValidationError error = new ValidationError();
			error.setErrorCode(ArchitectureErrorCodeConstants.INVALID_API_TYPE);
			error.setErrorMessage(String.format(
					ArchitectureErrorMessageConstants.INVALID_API_SINGLE,
					apiType));
			validationResult.getErrors().add(error);
		}
	}

	private static void removePriorInvalidApyTypeError(
			List<ValidationError> errors) {
		for (int i = 0; i < errors.size(); i++) {
			ValidationError error = errors.get(i);
			if (ArchitectureErrorCodeConstants.INVALID_API_TYPE.equals(error
					.getErrorCode())) {
				errors.remove(i);
			}
		}
	}

	@Override
	public ValidationResult validate(BindingResult bindingResult,
			HttpServletRequest request) {
		return validate(bindingResult, request, null);
	}

	/**
	 * Process the global Spring errors. If found they will be added to the
	 * validationResult
	 * 
	 * @param bindingResult
	 *            The bindingResult will contain the global errors, if any are
	 *            found
	 * @param validationResult
	 *            To be populated with global errors, if any are found
	 */
	private void processGlobalErrors(BindingResult bindingResult,
			ValidationResult validationResult) {
		if (!bindingResult.hasGlobalErrors()) {
			return;
		}
		validationResult.setValid(false);
		for (ObjectError globalError : bindingResult.getGlobalErrors()) {
			ValidationError globalValidationError = createOpaGlobalValidationError(globalError);
			validationResult.getErrors().add(globalValidationError);
		}
	}

	/**
	 * Process the Field Spring or Opa field errors. If found they will be added
	 * to the validationResult
	 * 
	 * @param bindingResult
	 *            The bindingResult will contain the field errors, if any are
	 *            found
	 * @param validationResult
	 *            To be populated with field errors, if any are found
	 */
	private void processFieldErrors(BindingResult bindingResult,
			ValidationResult validationResult) {
		if (!bindingResult.hasFieldErrors()) {
			return;
		}
		validationResult.setValid(false);
		for (FieldError fieldError : bindingResult.getFieldErrors()) {
			ValidationError fieldValidationError = createOpaFieldValidationError(fieldError);
			validationResult.getErrors().add(fieldValidationError);
			validationResult.getErrorsByValidatedItemCode().put(
					fieldValidationError.getValidatedItemCode(),
					fieldValidationError);
		}
	}

	/**
	 * Creates the Opa Validation error based on Spring/Opa javax.validation
	 * 
	 * @param bindingResultFieldError
	 *            The Spring/Opa javax.validation from which the Opa validation
	 *            error is to be created
	 * @return The newly created Opa validation error
	 */
	protected ValidationError createOpaFieldValidationError(
			FieldError bindingResultFieldError) {
		ValidationError validationError = new ValidationError();
		getMessageInterpolator().interpolate(bindingResultFieldError,
				validationError);
		validationError
		.setValidatedItemCode(bindingResultFieldError.getField());
		validationError.setFieldValidationError(true);
		return validationError;
	}

	/**
	 * Creates the Opa Validation error based on Spring global error
	 * 
	 * @param bindingResultGlobalError
	 *            The Spring global error from which the Opa validation error is
	 *            to be created
	 * @return The newly created Opa validation error
	 */
	protected ValidationError createOpaGlobalValidationError(
			ObjectError bindingResultGlobalError) {
		ValidationError validationError = new ValidationError();
		validationError.setErrorMessage(bindingResultGlobalError
				.getDefaultMessage());
		return validationError;
	}

	/**
	 * Populates the validationResult with the error message/code from the
	 * passed in validation error
	 * 
	 * @param validationResult
	 *            The validationResult that will have its error message/code
	 *            populated
	 * @param error
	 *            The ValidationError whose message/code will be copied into the
	 *            validationResult
	 */
	protected void populateErrorCodeAndMessage(
			ValidationResult validationResult, ValidationError error) {
		if (validationResult.isValid()) {
			validationResult.setErrorCode(error.getErrorCode());
			validationResult.setErrorMessage(error.getErrorMessage());
			validationResult.setHttpStatus(HttpStatus.BAD_REQUEST);
		}
	}

	private MessageInterpolator getMessageInterpolator() {
		if (getCustomMessageInterpolator() == null) {
			return defaultMessageInterpolator;
		}
		return getCustomMessageInterpolator();
	}

	protected static boolean noRecordsFound(List<?> entities,
			ValidationResult validationResult, String errorCode,
			String entitiesName) {
		if (entities == null || entities.size() == 0) {
			ValidationError error = new ValidationError();
			error.setErrorCode(errorCode);
			error.setErrorMessage(String.format(
					ArchitectureErrorMessageConstants.NO_RECORDS_FOUND,
					entitiesName));
			validationResult.addCustomValidationError(error);
			validationResult.setHttpStatus(HttpStatus.NOT_FOUND);
			return true;
		}
		return false;
	}

	protected boolean validateSort(ValidationResult validationResult,
			AbstractSearchResquestParameters requestParameters) {
		String sortInstruction = requestParameters.getSort();
		if (sortInstruction == null) {
			return true;
		}
		String[] sortTokens = sortInstruction.trim().split(" ");
		if (sortTokens.length > 2) {
			ValidationError error = new ValidationError();
			error.setErrorCode(ArchitectureErrorCodeConstants.INVALID_SORT);
			error.setErrorMessage(ArchitectureErrorMessageConstants.INVALID_SORT_INSTRUCTION);
			validationResult.addCustomValidationError(error);
			return false;
		} else if (sortTokens.length == 2) {
			boolean fieldNameValidation = validateSortField(validationResult,
					requestParameters, sortTokens[0]);
			if (!fieldNameValidation) {
				return false;
			}
			return validateSortDirection(validationResult, requestParameters,
					sortTokens[1]);
		} else if (sortTokens.length == 1) {
			return validateSortField(validationResult, requestParameters,
					sortTokens[0]);
		}
		return true;
	}

	private boolean validateSortField(ValidationResult validationResult,
			AbstractSearchResquestParameters requestParameters,
			String requestSortFieldName) {
		Map<String, String> paramNamesToDbColumnsMap = requestParameters
				.getParamNamesToDbColumnsMap();
		if (paramNamesToDbColumnsMap == null) {
			throw new OpaRuntimeException(
					"No paramNamesToDbColumnsMap is set for the request parameters: "
							+ requestParameters.getClass().getName());
		}

		String sortField = paramNamesToDbColumnsMap.get(requestSortFieldName);
		if (sortField == null) {
			ValidationError error = new ValidationError();
			error.setErrorCode(ArchitectureErrorCodeConstants.INVALID_SORT);
			error.setErrorMessage(String.format(
					ArchitectureErrorMessageConstants.INVALID_SORT_FIELD_NAME,
					requestSortFieldName, paramNamesToDbColumnsMap.keySet()));
			validationResult.addCustomValidationError(error);
			return false;
		}
		requestParameters.setSortField(sortField);
		return true;
	}

	private boolean validateSortDirection(ValidationResult validationResult,
			AbstractSearchResquestParameters requestParameters,
			String requestSortDirection) {
		requestSortDirection = requestSortDirection.toLowerCase();
		if (requestSortDirection.equals("asc")) {
			requestParameters.setSortDirection("ASC");
		} else if (requestSortDirection.equals("desc")) {
			requestParameters.setSortDirection("DESC");
		} else {
			ValidationError error = new ValidationError();
			error.setErrorCode(ArchitectureErrorCodeConstants.INVALID_SORT);
			error.setErrorMessage(ArchitectureErrorMessageConstants.INVALID_SORT_DIRECTION);
			validationResult.addCustomValidationError(error);
			return false;
		}
		return true;
	}

	private static void validateNoExtraParameters(
			AbstractRequestParameters requestParameters,
			HttpServletRequest request, ValidationResult validationResult) {

		if (requestParameters.bypassExtraneousHttpParametersValidation()) {
			return;
		}

		List<String> beanPropertyNames = null;

		try {
			beanPropertyNames = getPropertiesNames(requestParameters);
		} catch (Exception e) {
			throw new OpaRuntimeException(e);
		}

		Map<String, String> renamedParameters = HttpParameterNameRenamingProcessor.RENAMED_PARAMETER_NAMES
				.get(requestParameters.getClass());

		Set<String> servletParameters = request.getParameterMap().keySet();
		for (String parameterName : servletParameters) {
			if (!beanPropertyNames.contains(parameterName)
					&& (renamedParameters == null || !renamedParameters
					.containsKey(parameterName))
					&& !requestParameters.isInWhiteList(parameterName)) {
				ValidationError error = new ValidationError();
				error.setErrorCode(ArchitectureErrorCodeConstants.INVALID_PARAMETER);
				error.setErrorMessage(String
						.format(ArchitectureErrorMessageConstants.PARAMETER_NOT_ALLOWED,
								parameterName));
				validationResult.addCustomValidationError(error);
				return;
			}
		}
	}

	private static List<String> getPropertiesNames(Object requestParams)
			throws IntrospectionException {
		BeanInfo beanInfo = Introspector.getBeanInfo(requestParams.getClass());
		PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();

		List<String> propNames = new ArrayList<String>();
		for (PropertyDescriptor pd : pds) {
			propNames.add(pd.getName());
		}
		return propNames;
	}

	private static void validateTypes(Object requestParameters,
			Map<String, String[]> httpParameters,
			ValidationResult validationResult) {
		List<String> propertiesNames;
		try {
			propertiesNames = getPropertiesNames(requestParameters);
		} catch (Exception e) {
			throw new OpaRuntimeException(e);
		}

		if (propertiesNames == null || httpParameters == null) {
			return;
		}

		for (String propertyName : propertiesNames) {
			String[] httpParamValue = httpParameters.get(propertyName);
			if (httpParamValue == null) {
				continue;
			}
			String inValue = httpParamValue[0];

			Field field = ReflectionUtils.findField(
					requestParameters.getClass(), propertyName);
			if (field != null) {
				Class<?> type = field.getType();

				if (inValue == null || inValue.trim().equals("")) {
					return;
				}
				inValue = inValue.trim();
				if (type.equals(Integer.class) || type.equals(int.class)) {
					if (!tryForInteger(propertyName, inValue, validationResult)) {
						return;
					}
				} else if (type.equals(Boolean.class)
						|| type.equals(boolean.class)) {
					if (!tryForBoolean(propertyName, inValue, validationResult)) {
						return;
					}
				}
			}
		}
	}

	private static boolean tryForInteger(String parameterName, String value,
			ValidationResult validationResult) {
		try {
			Integer.valueOf(value);
		} catch (NumberFormatException ex) {
			ValidationError error = new ValidationError();
			error.setErrorCode(ArchitectureErrorCodeConstants.INVALID_PARAMETER_VALUE);
			error.setErrorMessage(String.format(
					ArchitectureErrorMessageConstants.INVALID_INTEGER,
					parameterName));
			validationResult.addCustomValidationError(error);
			return false;
		}
		return true;
	}

	private static boolean tryForBoolean(String parameterName, String value,
			ValidationResult validationResult) {
		if (value.toLowerCase().equals("true")
				|| value.toLowerCase().equals("false")
				|| value.toLowerCase().equals("0")
				|| value.toLowerCase().equals("1")) {
			return true;
		} else {
			ValidationError error = new ValidationError();
			error.setErrorCode(ArchitectureErrorCodeConstants.INVALID_PARAMETER_VALUE);
			error.setErrorMessage(String.format(
					ArchitectureErrorMessageConstants.INVALID_BOOLEAN,
					parameterName));
			validationResult.addCustomValidationError(error);
			return false;
		}
	}

	public boolean validateNaId(String naId) {
		logger.debug(String.format("Validating naId:%1$s", naId));

		if (validateStringField("NA ID", naId, NumericConstants.NA_ID_LENGTH)) {

			OpaStorage storage = opaStorageFactory.createOpaStorage();
			String basePath = null;
			try {
				basePath = storage.getFullPathInLive("",
						Integer.parseInt(naId));
			} catch (NumberFormatException e) {
				return false;
			}
			String descriptionXmlPath = basePath
					+ StorageUtils.DESCRIPTION_XML_FILE_NAME;

			if (storage.exists(descriptionXmlPath)) {
				return true;
			}
		}

		return false;
	}

	public boolean validateObjectId(String objectId, String naId) {
		logger.debug(String.format("Validating naId:%1$s objectId:%2$s", naId,
				objectId));

		if (validateStringField("Object ID", objectId,
				NumericConstants.OBJECT_ID_LENGTH)) {

			OpaStorage storage = opaStorageFactory.createOpaStorage();
			String basePath = storage.getFullPathInLive("",
					Integer.parseInt(naId));
			String objectXmlPath = basePath + StorageUtils.OBJECT_XML_FILE_NAME;

			if (storage.exists(objectXmlPath)) {
				String objectsXmlContents = "";
				try {
					byte[] bytes = storage.getFileContent(objectXmlPath);
					objectsXmlContents = new String(bytes, "UTF-8");

					if (ObjectsXmlUtils.objectIdExists(objectsXmlContents,
							objectId)) {
						return true;
					}
					return true;
				} catch (IOException e) {
					throw new OpaRuntimeException(e);
				}
			}
		}

		return false;
	}

	public boolean validateStringField(String fieldName, String value,
			int fieldSize) {
		if (StringUtils.isNullOrEmtpy(value) || value.length() > fieldSize) {
			return false;
		}
		return true;
	}
}
