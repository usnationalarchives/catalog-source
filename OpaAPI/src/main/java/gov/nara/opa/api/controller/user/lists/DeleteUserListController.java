package gov.nara.opa.api.controller.user.lists;

import gov.nara.opa.api.response.APIResponse;
import gov.nara.opa.api.services.ServiceResponseObject;
import gov.nara.opa.api.services.user.lists.DeleteUserListService;
import gov.nara.opa.api.services.user.lists.ModifyUserListService;
import gov.nara.opa.api.services.user.lists.ViewUserListService;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.user.accounts.UserAccount;
import gov.nara.opa.api.user.lists.UserList;
import gov.nara.opa.api.user.lists.UserListErrorCode;
import gov.nara.opa.api.user.lists.UserListItem;
import gov.nara.opa.api.utils.PathUtils;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.api.validation.user.lists.UserListValidator;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.utils.TimestampUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;

@Controller
public class DeleteUserListController {

  private static OpaLogger logger = OpaLogger
      .getLogger(DeleteUserListController.class);

  @Autowired
  private APIResponse apiResponse;

  @Autowired
  private UserListValidator userListValidator;

  @Autowired
  private DeleteUserListService deleteUserListService;

  @Autowired
  private ViewUserListService viewUserListService;

  @Autowired
  private ModifyUserListService modifyUserListService;

  /**
   * Delete a existing list
   * 
   * @param webRequest
   *          The web request instance
   * @param listName
   *          The name of the list to delete
   * @param format
   *          The output format. Either json or xml. Default is xml
   * @param pretty
   *          Specifies if the output should be pretty printed. Default is true
   * @return ResponseEntity with the json/xml representation of deleted list
   */
  @RequestMapping(value = "/iapi/v1/lists/delete/{listName:.+}", method = RequestMethod.DELETE)
  public ResponseEntity<String> deleteUserList(
      WebRequest webRequest,
      @PathVariable String listName,
      @RequestParam(value = "what", required = false, defaultValue = "") String what,
      @RequestParam(value = "format", required = false, defaultValue = "json") String format,
      @RequestParam(value = "pretty", required = false, defaultValue = "true") boolean pretty) {

    String message = "";
    HttpStatus status = HttpStatus.OK;
    String action = "delete";
    String resultType = "userList";
    String resultsType = "ListItems";

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
      requestParams.put("listName", listName);
      requestParams.put("format", format);
      requestParams.put("pretty", pretty);

      // Validate parameters
      String[] paramNamesStringArray = { "what", "format", "pretty" };
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
        } else {
          // Attempt to obtain the list from the database
          UserList userList = viewUserListService.getList(listName,
              userAccount.getAccountId());

          // Validate target list exists.
          if (userList == null) {
            status = HttpStatus.NOT_FOUND;
            userListValidator.setStandardError(UserListErrorCode.INVALID_LIST);
          } else {
            ServiceResponseObject responseObject = new ServiceResponseObject();

            switch (what) {
              case "":
                // Attempt to delete
                responseObject = deleteUserListService.deleteList(userList);

                // Validate that the update was executed succesful
                if (responseObject.getErrorCode() == UserListErrorCode.NONE) {

                  // Build and return the response object
                  UserList deletedUserList = (UserList) responseObject
                      .getContent();

                  // Set header params
                  LinkedHashMap<String, Object> headerParams = setHeaderValues(
                      status, requestParams);

                  // Set the required response values
                  LinkedHashMap<String, Object> resultValues = setListResultValues(deletedUserList);

                  // Set response header
                  apiResponse.setResponse(responseObj, "header", headerParams);

                  // Set response results
                  apiResponse
                      .setResponse(responseObj, resultType, resultValues);
                }
                break;
              case "all":
                if (userListValidator.validateWhat(what)) {
                  // Add parameter 'what' to the response
                  requestParams.put("what", what);

                  // Attempt to delete
                  responseObject = deleteUserListService
                      .removeAllFromList(userList);

                  // Validate that the update was executed
                  // succesful
                  if (responseObject.getErrorCode() == UserListErrorCode.NONE) {

                    // Build and return the response object
                    UserList deletedUserList = (UserList) responseObject
                        .getContent();
                    // Set header params
                    LinkedHashMap<String, Object> headerParams = setHeaderValues(
                        status, requestParams);

                    // Set the required response values
                    LinkedHashMap<String, Object> resultValues = setListResultValues(deletedUserList);

                    // Set response header
                    apiResponse
                        .setResponse(responseObj, "header", headerParams);

                    // Set response results
                    apiResponse.setResponse(responseObj, resultType,
                        resultValues);

                  } else {
                    userListValidator
                        .setStandardError(UserListErrorCode.EMPTY_LIST);
                  }
                }
                break;
              default:
                if (userListValidator.validateWhat(what)) {
                  // Add parameter 'what' to the response
                  requestParams.put("what", what);

                  String[] opaIds = what.split(",");

                  // Add all the lists found to the response
                  // object
                  LinkedHashMap<String, Object> resultsMap = new LinkedHashMap<String, Object>();
                  ArrayList<LinkedHashMap<String, Object>> resultsValueList = new ArrayList<LinkedHashMap<String, Object>>();

                  for (String opaId : opaIds) {
                    // Obtain the item from the database
                    UserListItem item = viewUserListService.getListItem(
                        userList.getListId(), opaId);
                    if (item != null) {
                      // Attempt to delete
                      responseObject = deleteUserListService.removeFromList(
                          userList, opaId);

                      if (responseObject.getErrorCode() == UserListErrorCode.NONE) {

                        // Set the required response values
                        LinkedHashMap<String, Object> resultValuesMap = setListItemResultValues(item);

                        // Add current item to the items
                        // subgroup
                        resultsValueList.add(resultValuesMap);
                      }
                    }
                  }
                  if (resultsValueList.size() > 0) {

                    // Add items subgroup to response map
                    resultsMap.put(resultType, resultsValueList);

                    // Set header params
                    LinkedHashMap<String, Object> headerParams = setHeaderValues(
                        status, requestParams);

                    // Set response header
                    apiResponse
                        .setResponse(responseObj, "header", headerParams);

                    // Set response results
                    apiResponse.setResponse(responseObj, resultsType,
                        resultsMap);
                  } else {
                    userListValidator
                        .setStandardError(UserListErrorCode.ENTRIES_NOT_FOUND);
                  }
                }
                break;
            }
          }
        }
      }

      if (!userListValidator.getIsValid()) {

        // Set error status if different from 404
        if (status != HttpStatus.NOT_FOUND)
          status = HttpStatus.BAD_REQUEST;

        // Set request params
        LinkedHashMap<String, Object> badRequestParams = new LinkedHashMap<String, Object>();
        badRequestParams.put("@path", requestPath);
        badRequestParams.put("action", action);

        // Set header params
        LinkedHashMap<String, Object> headerParams = setHeaderValues(status,
            badRequestParams);

        // Set error params
        LinkedHashMap<String, Object> errorParams = new LinkedHashMap<String, Object>();
        errorParams.put("@code", userListValidator.getErrorCode().toString());
        errorParams.put("description", userListValidator.getErrorCode()
            .getErrorMessage());
        errorParams.put("listName", listName);
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

  /**
   * Delete a existing list
   * 
   * @param webRequest
   *          The web request instance
   * @param listName
   *          The name of the list to delete
   * @param format
   *          The output format. Either json or xml. Default is xml
   * @param pretty
   *          Specifies if the output should be pretty printed. Default is true
   * @return ResponseEntity with the json/xml representation of deleted list
   */
  @SuppressWarnings("unchecked")
  @RequestMapping(value = "/iapi/v1/lists/deleteall", method = RequestMethod.DELETE)
  public ResponseEntity<String> deleteAllUserLists(
      WebRequest webRequest,
      @RequestParam(value = "format", required = false, defaultValue = "json") String format,
      @RequestParam(value = "pretty", required = false, defaultValue = "true") boolean pretty) {

    String message = "";
    HttpStatus status = HttpStatus.OK;
    String action = "delete";
    String resultType = "userList";
    String resultsType = "userLists";

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

      // Set request params
      LinkedHashMap<String, Object> requestParams = new LinkedHashMap<String, Object>();
      requestParams.put("@path", requestPath);
      requestParams.put("action", action);
      requestParams.put("userName", userName);
      requestParams.put("format", format);
      requestParams.put("pretty", pretty);

      // Validate parameters
      String[] paramNamesStringArray = { "format", "pretty" };
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
        } else {

          // Delete the user's lists
          ServiceResponseObject responseObject = deleteUserListService
              .deleteAllUserLists(userAccount.getAccountId());

          if (responseObject.getErrorCode() == UserListErrorCode.LISTS_NOT_FOUND) {
            userListValidator
                .setStandardError(UserListErrorCode.LISTS_NOT_FOUND);
          } else if (responseObject.getErrorCode() == UserListErrorCode.NONE) {
            HashMap<String, Object> responseMap = responseObject
                .getContentMap();
            List<UserList> userLists = (List<UserList>) responseMap
                .get("DeletedLists");

            // Add all the lists found to the response object
            LinkedHashMap<String, Object> resultsMap = new LinkedHashMap<String, Object>();
            ArrayList<LinkedHashMap<String, Object>> resultsValueList = new ArrayList<LinkedHashMap<String, Object>>();

            for (UserList userList : userLists) {
              // Set the required response values
              LinkedHashMap<String, Object> resultValuesMap = setListResultValues(userList);

              // Add current list to the lists subgroup
              resultsValueList.add(resultValuesMap);
            }

            if (resultsValueList.size() > 0) {

              // Add lists subgroup to response map
              resultsMap.put(resultType, resultsValueList);

              // Set header params
              LinkedHashMap<String, Object> headerParams = setHeaderValues(
                  status, requestParams);

              // Set response header
              apiResponse.setResponse(responseObj, "header", headerParams);

              // Set response results
              apiResponse.setResponse(responseObj, resultsType, resultsMap);
            }

          }

        }
      }

      if (!userListValidator.getIsValid()) {

        // Set error status
        status = HttpStatus.BAD_REQUEST;

        // Set request params
        LinkedHashMap<String, Object> badRequestParams = new LinkedHashMap<String, Object>();
        badRequestParams.put("@path", requestPath);
        badRequestParams.put("action", action);

        // Set header params
        LinkedHashMap<String, Object> headerParams = setHeaderValues(status,
            badRequestParams);

        // Set error params
        LinkedHashMap<String, Object> errorParams = new LinkedHashMap<String, Object>();
        errorParams.put("@code", userListValidator.getErrorCode().toString());
        errorParams.put("description", userListValidator.getErrorCode()
            .getErrorMessage());
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

  /**
   * Fill a map with the item info that the response message will return
   * 
   * @param item
   *          The object that contains the information
   * 
   * @return Map with the item info that the response message will return
   */
  private LinkedHashMap<String, Object> setListItemResultValues(
      UserListItem item) {
    LinkedHashMap<String, Object> resultValuesMap = new LinkedHashMap<String, Object>();
    // Set the required response values
    resultValuesMap.put("@listItemId", item.getUserListItemId());
    resultValuesMap.put("@listId", item.getListId());
    resultValuesMap.put("@opaId", item.getOpaId());
    resultValuesMap.put("@created",
        TimestampUtils.getUtcString(item.getItemTs()));
    return resultValuesMap;
  }

  /**
   * Fill a map with the list info that the response message will return
   * 
   * @param deletedUserList
   *          The object that contains the information
   * 
   * @return Map with the list info that the response message will return
   */
  private LinkedHashMap<String, Object> setListResultValues(
      UserList deletedUserList) {
    // Set result values
    LinkedHashMap<String, Object> resultValues = new LinkedHashMap<String, Object>();
    resultValues.put("@name", deletedUserList.getListName());
    resultValues.put("@total", deletedUserList.getTotal());
    if (deletedUserList.getLastModifiedTs() != null)
      resultValues.put("@lastModified",
          TimestampUtils.getUtcString(deletedUserList.getLastModifiedTs()));
    resultValues.put("@created",
        TimestampUtils.getUtcString(deletedUserList.getCreatedTs()));
    return resultValues;
  }

  /**
   * Fill a map with the info that the response message will return in the
   * header of the response message
   * 
   * @param status
   *          HTTP Status of the request
   * 
   * @param requestParams
   *          The params that will be returned as part of the header
   * @return Map with the info that the response will return as header
   */
  private LinkedHashMap<String, Object> setHeaderValues(HttpStatus status,
      LinkedHashMap<String, Object> requestParams) {

    // Set header params
    LinkedHashMap<String, Object> headerParams = new LinkedHashMap<String, Object>();
    headerParams.put("@status", String.valueOf(status));
    headerParams.put("time", TimestampUtils.getUtcTimestampString());
    headerParams.put("request", requestParams);
    return headerParams;
  }

}
