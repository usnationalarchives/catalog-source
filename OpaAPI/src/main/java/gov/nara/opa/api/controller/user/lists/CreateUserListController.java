package gov.nara.opa.api.controller.user.lists;

import gov.nara.opa.api.response.APIResponse;
import gov.nara.opa.api.services.ServiceResponseObject;
import gov.nara.opa.api.services.user.lists.CreateUserListService;
import gov.nara.opa.api.services.user.lists.ViewUserListService;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.user.accounts.UserAccount;
import gov.nara.opa.api.user.lists.UserList;
import gov.nara.opa.api.user.lists.UserListErrorCode;
import gov.nara.opa.api.utils.PathUtils;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.api.validation.user.lists.UserListValidator;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.utils.TimestampUtils;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;

@Controller
public class CreateUserListController {
  
  private static OpaLogger logger = OpaLogger.getLogger(CreateUserListController.class);

  @Autowired
  private APIResponse apiResponse;

  @Autowired
  private UserListValidator userListValidator;

  @Autowired
  private CreateUserListService createUserListService;

  @Autowired
  private ViewUserListService viewUserListService;

  /**
   * Creates a new List
   * 
   * @param webRequest
   *          The web request instance
   * @param listName
   *          The name of the list to create
   * @param format
   *          The output format. Either json or xml. Default is xml
   * @param pretty
   *          Specifies if the output should be pretty printed. Default is true
   * @return ResponseEntity with the json/xml representation of either the
   *         created list or any encountered error.
   */
  @RequestMapping(value = "/iapi/v1/lists/create", method = RequestMethod.POST)
  public ResponseEntity<String> createUserList(
      WebRequest webRequest,
      @RequestParam(value = "listname", required = false) String listName,
      @RequestParam(value = "format", required = false, defaultValue = "json") String format,
      @RequestParam(value = "pretty", required = false, defaultValue = "true") boolean pretty) {

    String message = "";
    HttpStatus status = HttpStatus.OK;
    String action = "createList";
    String resultType = "userList";

    try {
      // Build the Aspire OPA response object
      AspireObject responseObj = new AspireObject("opaResponse");

      // Get the request path
      String requestPath = PathUtils.getPathFromWebRequest(webRequest);

      // Retrieve the user account object
      Authentication auth = SecurityContextHolder.getContext()
          .getAuthentication();
      UserAccount userAccount = (UserAccount) auth.getDetails();
      String userName = userAccount.getUserName();

      // Create map with parameters received on the request
      LinkedHashMap<String, Object> requestParams = new LinkedHashMap<String, Object>();
      requestParams.put("@path", requestPath);
      requestParams.put("action", action);
      requestParams.put("userName", userName);
      requestParams.put("listname", listName);
      requestParams.put("format", format);
      requestParams.put("pretty", pretty);

      // Validate parameters
      String[] paramNamesStringArray = { "listname", "format", "pretty" };
      LinkedHashMap<String, String> validRequestParameterNames = StringUtils
          .convertStringArrayToLinkedHashMap(paramNamesStringArray);
      if (!ValidationUtils.validateRequestParameterNames(
          validRequestParameterNames, webRequest.getParameterNames())) {
        status = HttpStatus.BAD_REQUEST;
        userListValidator.setStandardError(UserListErrorCode.INVALID_PARAMETER);
        userListValidator.setMessage(ErrorConstants.invalidParameterName);
        userListValidator.setIsValid(false);
      } else {
        userListValidator.validateParameters(requestParams); 
      }      

      // If the parameters are valid
      if (userListValidator.getIsValid()) {

        // Validate that the user account is inactive
        if (userAccount.getAccountStatus() == 0) {
          userListValidator
              .setStandardError(UserListErrorCode.ACCOUNT_INACTIVE);

          // Validate that there is not a duplicate list
        } else if (createUserListService.isDuplicateList(listName,
            userAccount.getAccountId())) {
          userListValidator.setDuplicateListError(listName);

        } else {
          // Call to the data access layer to insert the new list
          ServiceResponseObject responseObject = createUserListService
              .createList(listName, userAccount.getAccountId());

          UserListErrorCode errorCode = (UserListErrorCode)responseObject.getErrorCode();
          
          if(errorCode == UserListErrorCode.NONE) {
            // Build and return the response object
            UserList newUserList = (UserList) responseObject.getContent();
  
            // Set header params
            LinkedHashMap<String, Object> headerParams = new LinkedHashMap<String, Object>();
            headerParams.put("@status", String.valueOf(status));
            headerParams.put("time", TimestampUtils.getUtcTimestampString());
  
            // Add request params to the response
            headerParams.put("request", requestParams);
  
            // Set result values
            LinkedHashMap<String, Object> resultValues = new LinkedHashMap<String, Object>();
            resultValues.put("@name", newUserList.getListName());
            resultValues.put("@total", 0);
            resultValues.put("@lastModified",
                TimestampUtils.getUtcString(newUserList.getCreatedTs()));
            resultValues.put("@created",
                TimestampUtils.getUtcString(newUserList.getCreatedTs()));
  
            // Set response header
            apiResponse.setResponse(responseObj, "header", headerParams);
  
            // Set response results
            apiResponse.setResponse(responseObj, resultType, resultValues);
          
          } else {
            userListValidator.setIsValid(false);
            userListValidator.setErrorCode(errorCode);
          }

        }
      }

      if (!userListValidator.getIsValid()) {

        // Set error status if different from 404
        if (status != HttpStatus.NOT_FOUND)
          status = HttpStatus.BAD_REQUEST;

        // Set header params
        LinkedHashMap<String, Object> headerParams = new LinkedHashMap<String, Object>();
        headerParams.put("@status", String.valueOf(status));
        headerParams.put("time", TimestampUtils.getUtcTimestampString());

        // Set request params
        LinkedHashMap<String, Object> badRequestParams = new LinkedHashMap<String, Object>();
        badRequestParams.put("@path", requestPath);
        badRequestParams.put("action", action);
        headerParams.put("request", badRequestParams);

        // Set error params
        LinkedHashMap<String, Object> errorParams = new LinkedHashMap<String, Object>();
        errorParams.put("@code", userListValidator.getErrorCode().toString());
        errorParams.put("description", userListValidator.getErrorCode()
            .getErrorMessage());
        errorParams.put("userName", userName);
        errorParams.put("list", listName);
        errorParams.put("format", format);
        errorParams.put("pretty", pretty);

        // Set response header
        apiResponse.setResponse(responseObj, "header", headerParams);

        // Set response error
        apiResponse.setResponse(responseObj, "error", errorParams);
      }

      // Build response string using the response object
      message = apiResponse
          .getResponseOutputString(responseObj, format, pretty);

      // Close Aspire response object
      responseObj.close();

    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      //throw new OpaRuntimeException(e);
    }
    ResponseEntity<String> entity = new ResponseEntity<String>(message, status);
    return entity;
  }

}
