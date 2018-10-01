package gov.nara.opa.api.validation.export;

import gov.nara.opa.api.dataaccess.user.UserAccountDao;
import gov.nara.opa.api.dataaccess.user.lists.ListDao;
import gov.nara.opa.api.security.OPAAuthenticationProvider;
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.api.valueobject.user.lists.UserListItemValueObject;
import gov.nara.opa.api.valueobject.user.lists.UserListValueObject;
import gov.nara.opa.architecture.web.validation.AbstractBaseValidator;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.services.docstransforms.Constants;
import gov.nara.opa.common.validation.export.CreateAccountExportRequestParameters;
import gov.nara.opa.common.valueobject.export.AccountExportValueObjectConstants;
import gov.nara.opa.common.valueobject.export.AccountExportValueObjectHelper;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateAccountExportValidator extends AbstractBaseValidator
    implements AccountExportValueObjectConstants {

  public static final String ACCOUNT_ID_OBJECT = "accountId";

  public static final String LIST_ID_OBJECT = "listId";

  public static final String LIST_EXISTS_OBJECT = "listExists";

  public static final String LIST_ITEMS_OBJECT = "listItems";

  public static final String LIST_USER_ID = "listUserId";

  public static final String EXPORT_IDS_EXIST_OBJECT = "exportIdsExist";

  public static final String GENERAL_QUERY_EXISTS_OBJECT = "generalQueryExists";

  public static final String TOTAL_NO_OF_ESTIMATED_RECORDS = "totalNoOfEstimatedRecords";

  @Autowired
  ConfigurationService configurationService;

  @Autowired
  ListDao listDao;

  @Autowired
  UserAccountDao userDao;

  @Override
  protected void performCustomValidation(ValidationResult validationResult,
      HttpServletRequest request) {
    CreateAccountExportRequestParameters requestParameters = (CreateAccountExportRequestParameters) validationResult
        .getValidatedRequest();
    requestParameters.setQueryParameters(AccountExportValueObjectHelper
        .scrubQueryParameters(request.getParameterMap(),
            requestParameters.getApiType()));
    validateFormat(requestParameters);
    // trick to perform the validations in a sequence and stop at the first
    // failure
    if (validateBulkExportCapbability(requestParameters, validationResult)
        && validateUserName(requestParameters, validationResult)
        && validateMutuallyExclusiveQueryCriteria(requestParameters,
            validationResult)
        && validateRows(requestParameters, validationResult)
        && validateList(requestParameters, validationResult)
        && validateExportLimit(requestParameters, validationResult)
        && validateBulkExportContent(requestParameters, validationResult)
        && validateMetadata(requestParameters, validationResult)) {
      return;
    }
  }

  private boolean validateMetadata(
      CreateAccountExportRequestParameters requestParameters,
      ValidationResult validationResult) {
    if(requestParameters.getExportWhat() != null) {
      if (requestParameters.getExportWhat().contains(
          AccountExportValueObjectHelper.METADATA)) {
        return true;
      }

      if (requestParameters.getQueryParameters().containsKey(
          CreateAccountExportRequestParameters.RESULT_FIELDS_HTTP_PARAM_NAME)) {
        ValidationError error = new ValidationError();
        error.setErrorCode(ArchitectureErrorCodeConstants.INVALID_PARAMETER);
        error.setErrorMessage(ErrorConstants.EXPORT_INVALID_RESULT_FIELDS);
        validationResult.addCustomValidationError(error);
        return false;
      }      
    } else {
      ValidationError error = new ValidationError();
      error.setErrorCode(ArchitectureErrorCodeConstants.MISSING_PARAMETER);
      error.setErrorMessage(ErrorConstants.EXPORT_MISSING_WHAT);
      validationResult.addCustomValidationError(error);
      return false;
    }
    


    return true;
  }

  private void validateFormat(
      CreateAccountExportRequestParameters requestParameters) {
    if (requestParameters.getExportFormat() == null) {
      requestParameters.setExportFormat(requestParameters.getFormat());
    }
  }

  public boolean validateUserName(
      CreateAccountExportRequestParameters requestParameters,
      ValidationResult validationResult) {
    String userName = requestParameters.getUserName();

    if (userName != null && requestParameters.getListName() == null) {
      ValidationError error = new ValidationError();
      error.setErrorCode(ArchitectureErrorCodeConstants.INVALID_PARAMETER);
      error.setErrorMessage(ErrorConstants.EXPORT_USER_NAME_WO_LIST);
      validationResult.addCustomValidationError(error);
      return false;
    }

    if (requestParameters.getListName() != null && userName == null) {
      ValidationError error = new ValidationError();
      error.setErrorCode(ArchitectureErrorCodeConstants.MISSING_PARAMETER);
      error.setErrorMessage(ErrorConstants.EXPORT_USER_NAME_NOT_PROVIDED);
      validationResult.addCustomValidationError(error);
      return false;
    }

    if (userName != null) {
      UserAccountValueObject userForList = userDao.selectByUserName(userName);
      if (userForList == null) {
        ValidationError error = new ValidationError();
        error
            .setErrorCode(ArchitectureErrorCodeConstants.INVALID_PARAMETER_VALUE);
        error.setErrorMessage(ErrorConstants.EXPORT_LIST_USER_NAME_NOT_EXISTS);
        validationResult.addCustomValidationError(error);
        return false;
      } else {
        validationResult.addContextObject(LIST_USER_ID,
            userForList.getAccountId());
      }
    }

    return true;
  }

  public boolean validateMutuallyExclusiveQueryCriteria(
      CreateAccountExportRequestParameters requestParameters,
      ValidationResult validationResult) {
    List<String> parameterNames = new ArrayList<String>();
    parameterNames.addAll(requestParameters.getQueryParameters().keySet());

    parameterNames.remove(SORT_HTTP_PARAM_NAME);
    parameterNames.remove(ACTION_HTTP_PARAM_NAME);
    parameterNames.remove(LANGUAGES_HTTP_PARAM_NAME);
    parameterNames.remove(LANGUAGES_ALL_HTTP_PARAM_NAME);
    parameterNames.remove(EXCLUDE_RESULT_TYPES_HTTP_PARAM_NAME);
    
    Boolean listNameExists = requestParameters.getListName() == null ? false
        : true;
    validationResult.addContextObject(LIST_EXISTS_OBJECT, listNameExists);
    
    parameterNames.remove(LIST_NAME_HTTP_PARAM_NAME);
    parameterNames.remove(USER_NAME_HTTP_PARAM_NAME);
    
    Boolean naIdsExist = parameterNames.remove(NAIDS_HTTP_PARAM_NAME); 
    
    Boolean exportIdsExist = parameterNames.remove(EXPORT_IDS_HTTP_PARAM_NAME);
    validationResult.addContextObject(EXPORT_IDS_EXIST_OBJECT, exportIdsExist);
    
    Boolean qExists = parameterNames.size() > 0 ? true : false;
    validationResult.addContextObject(GENERAL_QUERY_EXISTS_OBJECT, qExists);

    if ((listNameExists && exportIdsExist) || (listNameExists && qExists)
        || (exportIdsExist && qExists)) {
      ValidationError error = new ValidationError();
      error.setErrorCode(ErrorCodeConstants.INVALID_QUERY_COMBINATION);
      error.setErrorMessage(ErrorConstants.INVALID_QUERY_COMBINATION);
      validationResult.addCustomValidationError(error);
      return false;
    }

    if (!listNameExists && !exportIdsExist && !qExists && !naIdsExist) {
      ValidationError error = new ValidationError();
      error.setErrorCode(ArchitectureErrorCodeConstants.MISSING_PARAMETER);
      error.setErrorMessage(ErrorConstants.MISSING_QUERY);
      validationResult.addCustomValidationError(error);
      return false;
    }

    return true;
  }

  public boolean validateList(
      CreateAccountExportRequestParameters requestParameters,
      ValidationResult validationResult) {

    if (requestParameters.getListName() == null) {
      return true;
    }
    String listName = requestParameters.getListName();
    Integer accountId = (Integer) validationResult.getContextObjects().get(
        LIST_USER_ID);
    if (accountId == null) {
      ValidationError error = new ValidationError();
      error.setErrorCode(ArchitectureErrorCodeConstants.INVALID_PARAMETER);
      error.setErrorMessage(ErrorConstants.NO_LIST_FOR_ANONYMOUS);
      validationResult.addCustomValidationError(error);
      return false;
    }
    UserListValueObject userList = listDao.getList(listName, accountId);
    if (userList == null) {
      ValidationError error = new ValidationError();
      error.setErrorCode(ArchitectureErrorCodeConstants.INVALID_PARAMETER);
      error.setErrorMessage(String.format(ErrorConstants.INVALID_LIST_NAME,
          listName, requestParameters.getUserName()));
      validationResult.addCustomValidationError(error);
      return false;
    }
    validationResult.addContextObject(LIST_ID_OBJECT, userList.getListId());
    return true;
  }

  public boolean validateRows(
      CreateAccountExportRequestParameters requestParameters,
      ValidationResult validationResult) {
    boolean qExists = ((Boolean) validationResult.getContextObjects().get(
        GENERAL_QUERY_EXISTS_OBJECT)).booleanValue();
    if (qExists && requestParameters.getRows() == null) {
      ValidationError error = new ValidationError();
      error.setErrorCode(ArchitectureErrorCodeConstants.MISSING_PARAMETER);
      error.setErrorMessage(ErrorConstants.ROWS_MISSING);
      validationResult.addCustomValidationError(error);
      return false;
    }
    return true;
  }

  public boolean validateBulkExportCapbability(
      CreateAccountExportRequestParameters requestParameters,
      ValidationResult validationResult) {
    if (requestParameters.getBulkExport().booleanValue()
        && OPAAuthenticationProvider.getAccountIdForLoggedInUser() == null) {
      ValidationError error = new ValidationError();
      error.setErrorCode(ArchitectureErrorCodeConstants.INVALID_PARAMETER);
      error.setErrorMessage(ErrorConstants.UNAUTHORIZED_BULK_EXPORT_REQUEST);
      validationResult.addCustomValidationError(error);
      return false;
    }
    return true;
  }

  public boolean validateExportLimit(
      CreateAccountExportRequestParameters requestParameters,
      ValidationResult validationResult) {

    int totalNoOfEstimatedRecords = getTotalNoOfEstimatedRecords(
        requestParameters, validationResult);
    validationResult.addContextObject(TOTAL_NO_OF_ESTIMATED_RECORDS,
        new Integer(totalNoOfEstimatedRecords));

    if (requestParameters.getExportFormat().equals(
        Constants.EXPORT_FORMAT_PRINT)) {
      int maxPrintResults = configurationService.getConfig()
          .getMaxPrintResults();
      if (requestParameters.getRows() > maxPrintResults) {
        ValidationError validationError = new ValidationError();
        validationError.setErrorCode(ErrorCodeConstants.MAX_LIMIT);
        String errorMessage = ErrorConstants.MAX_LIMIT_PRINT;
        validationError.setErrorMessage(String.format(errorMessage,
            totalNoOfEstimatedRecords, maxPrintResults));
        validationResult.addCustomValidationError(validationError);
        return false;
      }
    }

    return true;
    // TODO to complete once we know the rules for exports and print limits
    // boolean bulkExport = requestParameters.getBulkExport();

    // int maxNoOfRecordsForExport =
    // getMaxExportRecords(OPAAuthenticationProvider
    // .getUserTypeForLoggedInUser());
    //
    // if (totalNoOfEstimatedRecords > maxNoOfRecordsForExport) {
    // ValidationError error = new ValidationError();
    // error.setErrorCode(ErrorCodeConstants.MAX_LIMIT);
    // error.setErrorMessage(String.format(ErrorConstants.MAX_LIMIT_EXPORTS,
    // totalNoOfEstimatedRecords, maxNoOfRecordsForExport));
    // validationResult.addCustomValidationError(error);
    // return false;
    // }
    //
    // if (!bulkExport) {
    // int maxNonBulkRecordsForExport = getMaxNonBulkExportRecords();
    // if (totalNoOfEstimatedRecords > maxNonBulkRecordsForExport) {
    // ValidationError error = new ValidationError();
    // error.setErrorCode(ErrorCodeConstants.MAX_LIMIT);
    // String errorMessage = Constants.EXPORT_FORMAT_PRINT
    // .equals(requestParameters.getExportFormat()) ?
    // ErrorConstants.MAX_LIMIT_PRINT
    // : ErrorConstants.MAX_LIMIT_NON_BULK_EXPORTS;
    // error.setErrorMessage(String.format(errorMessage,
    // totalNoOfEstimatedRecords, maxNonBulkRecordsForExport));
    // validationResult.addCustomValidationError(error);
    // return false;
    // }
    // }
    //
    // return true;
  }

  public int getTotalNoOfEstimatedRecords(
      CreateAccountExportRequestParameters requestParameters,
      ValidationResult validationResult) {
    if (((Boolean) validationResult.getContextObjects().get(
        EXPORT_IDS_EXIST_OBJECT)).booleanValue()) {
      String exportIds = requestParameters.getQueryParameters().get(
          EXPORT_IDS_HTTP_PARAM_NAME)[0];
      return exportIds.split(",").length;
    } else if (((Boolean) validationResult.getContextObjects().get(
        LIST_EXISTS_OBJECT)).booleanValue()) {
      List<UserListItemValueObject> listItems = listDao
          .getListItems((Integer) validationResult.getContextObjects().get(
              LIST_ID_OBJECT));
      validationResult.addContextObject(LIST_ITEMS_OBJECT, listItems);
      return listItems.size();
    } else if (((Boolean) validationResult.getContextObjects().get(
        GENERAL_QUERY_EXISTS_OBJECT)).booleanValue()) {
      return requestParameters.getRows();
    }
    return -1;
  }

  public boolean validateBulkExportContent(
      CreateAccountExportRequestParameters requestParameters,
      ValidationResult validationResult) {
    if (!requestParameters.getBulkExport()
        && requestParameters.getBulkExportContent() != null) {
      ValidationError error = new ValidationError();
      error.setErrorCode(ArchitectureErrorCodeConstants.INVALID_PARAMETER);
      error.setErrorMessage(ErrorConstants.BULK_EXPORT_CONTENT);
      validationResult.addCustomValidationError(error);
    }
    return true;
  }

  public void validateAccountId(ValidationResult validationResult,
      Integer accountId) {

  }

  /*
   * private int getMaxExportRecords(String userType) { if (userType == null) {
   * return configurationService.getConfig().getBulkMaxRecordsPublic(); } else
   * if (userType.equals("standard")) { return
   * configurationService.getConfig().getBulkMaxRecordsStandard(); } else if
   * (userType.equals("power")) { return
   * configurationService.getConfig().getBulkMaxRecordsPower(); } return
   * configurationService.getConfig().getBulkMaxRecordsPublic(); }
   */

  /*
   * private int getMaxNonBulkExportRecords() { return
   * configurationService.getConfig().getNonBulkMaxRecords(); }
   */

}
