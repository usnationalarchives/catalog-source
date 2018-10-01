package gov.nara.opa.api.controller.user.lists;

import gov.nara.opa.api.response.APIResponse;
import gov.nara.opa.api.services.search.OpaSearchService;
import gov.nara.opa.api.services.user.lists.ViewUserListService;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.user.accounts.UserAccount;
import gov.nara.opa.api.user.lists.UserList;
import gov.nara.opa.api.user.lists.UserListErrorCode;
import gov.nara.opa.api.utils.PathUtils;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.api.validation.user.lists.UserListValidator;
import gov.nara.opa.api.valueobject.user.lists.UserListCollectionValueObject;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.utils.TimestampUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

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
public class ViewUserListsController {

  private static OpaLogger logger = OpaLogger
      .getLogger(ViewUserListsController.class);

  @Autowired
  private ViewUserListService viewUserListService;

  @Autowired
  private UserListValidator userListValidator;

  @Autowired
  private OpaSearchService opaSearchService;

  @Autowired
  APIResponse apiResponse;

  /**
   * Show the lists that belong to the specified user
   * 
   * @param webRequest
   *          The web request instance
   * @param userName
   *          The useraccount owner of the lists
   * @param format
   *          The output format. Either json or xml. Default is xml
   * @param pretty
   *          Specifies if output should be printed formatted. Default is true
   * @return ResponseEntity with the json/xml representation of the entries
   *         found.
   */
  @RequestMapping(value = "/iapi/v1/lists/view", method = RequestMethod.GET)
  public ResponseEntity<String> viewUserLists(
      WebRequest webRequest,
      @RequestParam(value = "rows", required = false, defaultValue = "20") int rows,
      @RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(value = "format", required = false, defaultValue = "json") String format,
      @RequestParam(value = "pretty", required = false, defaultValue = "true") boolean pretty) {

    HttpStatus status = HttpStatus.OK;
    String action = "viewUserLists";
    String resultsType = "userLists";
    String resultType = "userList";
    String responseMessage = "";

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
      requestParams.put("format", format);
      requestParams.put("pretty", pretty);
      requestParams.put("offset", offset);
      requestParams.put("rows", rows);

      // Validate parameters
      String[] paramNamesStringArray = { "rows", "offset", "format", "pretty" };
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
          // If parameter listname is present display a single list
        } else {
          // Retrieve the collection of lists filtering by accountId
          UserListCollectionValueObject resultObject = viewUserListService.viewMyLists(
              userAccount.getAccountId(), offset, rows);
          
          List<UserList> myLists = resultObject.getListCollection();
          Integer listCount = resultObject.getTotalLists();

          if (myLists == null || myLists.size() == 0) {
            status = HttpStatus.NOT_FOUND;
            userListValidator
                .setStandardError(UserListErrorCode.LISTS_NOT_FOUND);
          } else {

            // Set header params
            LinkedHashMap<String, Object> headerParams = new LinkedHashMap<String, Object>();
            headerParams.put("@status", String.valueOf(status));
            headerParams.put("time", TimestampUtils.getUtcTimestampString());

            // Add all the lists found to the response object
            LinkedHashMap<String, Object> resultsMap = new LinkedHashMap<String, Object>();
            ArrayList<LinkedHashMap<String, Object>> resultsValueList = new ArrayList<LinkedHashMap<String, Object>>();
            resultsMap.put("total", listCount);

            for (UserList list : myLists) {
              LinkedHashMap<String, Object> resultValuesMap = new LinkedHashMap<String, Object>();
              resultValuesMap.put("@name", list.getListName());
              resultValuesMap.put("total", list.getTotal());
              if (list.getLastModifiedTs() != null) {
                resultValuesMap.put("@lastModified", list.getLastModifiedTs()
                    .toString());
              } else {
                resultValuesMap.put("@lastModified", "");
              }
              if (list.getCreatedTs() != null) {
                resultValuesMap.put("@created", list.getCreatedTs().toString());
              } else {
                resultValuesMap.put("@created", "");
              }
              resultsValueList.add(resultValuesMap);
            }
            resultsMap.put(resultType, resultsValueList);

            // Add request params to response
            headerParams.put("request", requestParams);

            // Set response header
            apiResponse.setResponse(responseObj, "header", headerParams);

            // Set response results
            apiResponse.setResponse(responseObj, resultsType, resultsMap);
          }
        }
      }

      // Build response object error if the request results invalid
      if (!userListValidator.getIsValid()) {

        // Set error code
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
        LinkedHashMap<String, Object> errorValuesMap = new LinkedHashMap<String, Object>();
        errorValuesMap
            .put("@code", userListValidator.getErrorCode().toString());
        errorValuesMap.put("description", userListValidator.getErrorCode()
            .getErrorMessage());
        errorValuesMap.put("userName", userName);
        errorValuesMap.put("format", format);
        errorValuesMap.put("pretty", pretty);

        // Set response header
        apiResponse.setResponse(responseObj, "header", headerParams);

        // Set response error
        apiResponse.setResponse(responseObj, "error", errorValuesMap);
      }

      // Build response string using the response object
      responseMessage = apiResponse.getResponseOutputString(responseObj,
          format, pretty);

      // Close Aspire response object
      responseObj.close();

    } catch (Exception ae) {
      logger.error(ae.getMessage(), ae);
      //throw new OpaRuntimeException(ae);
    }

    ResponseEntity<String> entity = new ResponseEntity<String>(responseMessage,
        status);

    return entity;
  }

}
