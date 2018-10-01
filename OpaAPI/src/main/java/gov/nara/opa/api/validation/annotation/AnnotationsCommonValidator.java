package gov.nara.opa.api.validation.annotation;

import gov.nara.opa.api.constants.AnnotationsConstants;
import gov.nara.opa.api.services.search.SingleSearchRecordRetrieval;
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.system.Config;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.utils.PageNumberUtils;
import gov.nara.opa.api.validation.OpaApiAbstractValidator;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.ObjectsXmlUtils;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.AnnotationConstants;
import gov.nara.opa.common.NumericConstants;
import gov.nara.opa.common.storage.OpaStorage;
import gov.nara.opa.common.storage.OpaStorageFactory;
import gov.nara.opa.common.storage.StorageUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import gov.nara.opa.common.valueobject.search.SearchRecordValueObject;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public abstract class AnnotationsCommonValidator extends OpaApiAbstractValidator {

	private static OpaLogger logger = OpaLogger
			.getLogger(AnnotationsCommonValidator.class);

	protected String ANNOTATION_TEXT = "ANNOTATION";
	protected int annotationMaxSizeLength = 0;

	@Value("${configFilePath}")
	protected String configFilePath;

	@Autowired
	private PageNumberUtils pageNumberUtils;

	@Autowired
	protected ConfigurationService configService;

	@Autowired
	OpaStorageFactory opaStorageFactory;

	@Autowired
	SingleSearchRecordRetrieval searchRecordRetrieval;

	protected Config config;

	/**
	 * Validates if the naId and objectId have a valid value
	 * 
	 * @param annotationParameters
	 *            annotation parameters sent
	 * @return True if the validation process was successful, false otherwise
	 */
	protected boolean validateIds(
			AnnotationsViewRequestParameters annotationParameters,
			ValidationResult validationResult) {

		if (annotationParameters.getNaId() != null) {
			/*
			 * Validate naId
			 */
			boolean validNaId = validateNaId(annotationParameters.getNaId());
			if (!validNaId) {
				ValidationUtils.setValidationError(validationResult,
						ArchitectureErrorCodeConstants.INVALID_ID_VALUE,
						ErrorConstants.INVALID_NA_ID,
						AnnotationsConstants.NAID_FIELD_NAME);
				return false;
			}
		}

		if (annotationParameters.getNaId() != null
				&& (annotationParameters.getObjectId() != null)) {

			/*
			 * Validate naId AND objectId
			 */
			boolean validObjectId = validateObjectId(
					annotationParameters.getObjectId(),
					annotationParameters.getNaId());
			if (!validObjectId) {
				ValidationUtils.setValidationError(validationResult,
						ArchitectureErrorCodeConstants.INVALID_ID_VALUE,
						ErrorConstants.INVALID_OBJECT_ID,
						AnnotationsConstants.OBJECTID_FIELD_NAME);
				return false;
			}
		}

		return true;
	}

	/**
	 * Validates if the page number has a valid value
	 * 
	 * @param annotationParameters
	 *            annotation parameters sent
	 * @return True if the validation process was successful, false otherwise
	 */
	protected boolean validatePageNum(
			AnnotationsViewRequestParameters annotationParameters,
			ValidationResult validationResult) {
		String objectId = annotationParameters.getObjectId();
		int pageNum = annotationParameters.getPageNum();

		if (annotationParameters.getApiType().equals(Constants.PUBLIC_API_PATH)
				&& pageNum <= 0) {
			pageNum = pageNumberUtils.getPageNumber(
					annotationParameters.getApiType(), 
					annotationParameters.getNaId(),
					objectId);
		}

		if (objectId != null && !objectId.isEmpty() && pageNum <= 0) {
			ValidationUtils.setValidationError(validationResult,
					ArchitectureErrorCodeConstants.INVALID_PARAMETER_VALUE,
					ErrorConstants.PAGE_NUMBER_NOT_FOUND,
					AnnotationsConstants.PAGE_NUMBER_FIELD_NAME);
			return false;
		}
		return true;
	}

	/**
	 * Validates if the annotation text is valid
	 * 
	 * @param annotationText
	 *            annotation text
	 * @return True if the validation process was successful, false otherwise
	 */
	protected void validateAnnotationText(String annotationText,
			ValidationResult validationResult) {
		if (!(validateAnnotationTextLength(annotationText, validationResult))) {
			return;
		}
		if (!(validateAnnotationContent(annotationText, validationResult))) {
			return;
		}
	}

	/**
	 * Validates if the annotation text length is valid
	 * 
	 * @param annotationText
	 *            annotation text
	 * @return True if the validation process was successful, false otherwise
	 */
	protected boolean validateAnnotationTextLength(String annotationText,
			ValidationResult validationResult) {
		int annotationMaxSizeLength = getAnnotationTextMaxLength(validationResult);
		if (annotationMaxSizeLength < 0) {
			ValidationUtils.setValidationError(validationResult,
					ArchitectureErrorCodeConstants.CONFIG_FILE_NOT_FOUND,
					ErrorConstants.CONFIG_FILE_NOT_FOUND, AnnotationsConstants.TEXT_FIELD_NAME);

			return false;
		}
		else if (annotationText.length() > annotationMaxSizeLength) {
			ValidationUtils.setValidationError(validationResult,
					ArchitectureErrorCodeConstants.INVALID_PARAMETER_VALUE,
					String.format(ErrorConstants.INVALID_ANNOTATION_SIZE,
							AnnotationsConstants.TEXT_FIELD_NAME,
							annotationText.length(), annotationMaxSizeLength),
							AnnotationsConstants.TEXT_FIELD_NAME);
			return false;
		}
		return true;
	}

	/**
	 * Validates if the annotation text content is encoded in UTF-8
	 * 
	 * @param text
	 *            annotation text content
	 * @return True if the validation process was successful, false otherwise
	 */
	protected boolean validateAnnotationContent(String text,
			ValidationResult validationResult) {
		try {
			text.getBytes(AnnotationConstants.UTF8_ENCODING);
		} catch (UnsupportedEncodingException e) {
			ValidationUtils.setValidationError(validationResult,
					ArchitectureErrorCodeConstants.ILLEGAL_CHARACTERS, String
					.format(ErrorConstants.ILLEGAL_CHARACTERS,
							ANNOTATION_TEXT),
							AnnotationsConstants.TEXT_FIELD_NAME);
			return false;
		}

		return true;
	}

	/**
	 * Validates if the request parameter names are valid
	 * 
	 * @param annotationText
	 *            annotation text
	 * @return True if the validation process was successful, false otherwise
	 */
	protected boolean validRequestParameterNames(String queryString,
			ValidationResult validationResult) {
		LinkedHashMap<String, String> validRequestParameterNames = StringUtils
				.convertStringArrayToLinkedHashMap(AnnotationsConstants.ANNOTATION_VALID_PARAMETERS);
		if (!ValidationUtils.validateRequestParameterNames(
				validRequestParameterNames, queryString)) {
			ValidationUtils.setValidationError(validationResult,
					ArchitectureErrorCodeConstants.INVALID_PARAMETER,
					ErrorConstants.invalidParameterName,
					AnnotationsConstants.ANNOTATION_VALID_PARAMETERS.toString());
			return false;
		}
		return true;
	}

	protected int getAnnotationTextMaxLength(ValidationResult validationResult) {
		return annotationMaxSizeLength;
	}

	public boolean validateNaId(String naId) {
		logger.debug(String.format("Validating naId:%1$s", naId));

		if (validateStringField("NA ID", naId, NumericConstants.NA_ID_LENGTH)) {

			OpaStorage storage = opaStorageFactory.createOpaStorage();
			String basePath = storage.getFullPathInLive("",
					Integer.parseInt(naId));
			String descriptionXmlPath = basePath
					+ StorageUtils.DESCRIPTION_XML_FILE_NAME;

			if (storage.exists(descriptionXmlPath)) {
				return true;
			}
			descriptionXmlPath = storage.getFullPathInXmlStore("",
					Integer.parseInt(naId));
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
			logger.debug(String.format("Base path is %1$s", basePath));
			String objectXmlPath = basePath + StorageUtils.OBJECT_XML_FILE_NAME;

			if (storage.exists(objectXmlPath)) {
				String objectsXmlContents = "";
				try {
					byte[] bytes = storage.getFileContent(objectXmlPath);
					objectsXmlContents = new String(bytes, "UTF-8");
					if(StringUtils.isNullOrEmtpy(objectsXmlContents)) {
						logger.debug("ObjectXmlContents is null");
					}

					if (ObjectsXmlUtils.objectIdExists(objectsXmlContents,
							objectId)) {
						return true;
					} else {
						logger.debug(String.format("ObjectId '%1$s' doesn't exist", objectId));
					}
				} catch (IOException e) {
					throw new OpaRuntimeException(e);
				}
			} else {
				try {
					String objectsXml = getObjectsXMLFromSolr(naId, objectId);
					if(StringUtils.isNullOrEmtpy(objectsXml)) {
						logger.debug("ObjectXml is null");
					}
					if (ObjectsXmlUtils.objectIdExists(objectsXml,
							objectId)) {
						return true;
					} else {
						logger.debug(String.format("ObjectId '%1$s' doesn't exist", objectId));
					}
				} catch(Exception e) {
					throw new OpaRuntimeException(e);
				}

				logger.debug(String.format("ObjectXml could not be found"));
			}
		}

		return false;
	}

	private String getObjectsXMLFromSolr(String naId, String objectId) throws SolrServerException {
		String opaId = "obj-"+naId+"-"+objectId;
		String q = "opaId:\""+opaId+"\"";
		Map<String,String[]> queryParams = new HashMap<>();
		queryParams.put("q",new String[]{q});
		SearchRecordValueObject solrRes = searchRecordRetrieval.getSearchRecord(queryParams);
		if (solrRes == null) {
			logger.debug("Got no result for q="+q);
			return null;
		}
		return solrRes.getObjectsXml();
	}

	public boolean validateStringField(String fieldName, String value,
			int fieldSize) {
		if (StringUtils.isNullOrEmtpy(value) || value.length() > fieldSize) {
			return false;
		}
		return true;
	}

	/**
	 * Validates if the action has a valid value
	 * @param action
	 * 		action parameter sent
	 * @return 
	 * 		True if the validation process was successful, false otherwise
	 */
	protected boolean validateAction (String action, 
			ValidationResult validationResult) {
		if (!action.equals(AnnotationsConstants.SAVE_AND_RELOCK_ACTION)  && 
				!action.equals(AnnotationsConstants.SAVE_AND_UNLOCK_ACTION)) {
			ValidationError validationError = new ValidationError();
			validationError.setErrorCode(
					ArchitectureErrorCodeConstants.INVALID_PARAMETER_VALUE);
			validationError.setErrorMessage(
					ErrorConstants.INVALID_ACTION_VALUE);
			validationError.setFieldValidationError(true);
			validationError.setValidatedItemCode(AnnotationsConstants.ACTION_FIELD_NAME);
			validationResult.addCustomValidationError(validationError);
			return false;
		}
		return true;
	}

	@Override
	protected void performCustomValidation(ValidationResult validationResult,
			HttpServletRequest request) {
	}
}
