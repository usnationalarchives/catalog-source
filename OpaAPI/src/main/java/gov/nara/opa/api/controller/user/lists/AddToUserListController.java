package gov.nara.opa.api.controller.user.lists;

import gov.nara.opa.api.response.APIResponse;
import gov.nara.opa.api.search.BriefResults;
import gov.nara.opa.api.services.search.OpaSearchService;
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.services.user.lists.AddToUserListService;
import gov.nara.opa.api.services.user.lists.ViewUserListService;
import gov.nara.opa.api.system.Constants;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

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

@Controller
public class AddToUserListController {

  private static OpaLogger logger = OpaLogger
      .getLogger(AddToUserListController.class);

  @Autowired
  private APIResponse apiResponse;

  @Autowired
  private UserListValidator userListValidator;

  @Autowired
  private AddToUserListService addToUserListService;

  @Autowired
  private ViewUserListService viewUserListService;

  @Autowired
  private OpaSearchService opaSearchService;

  @Autowired
  private ConfigurationService configurationService;

  /**
   * Capability for a registered user to save an individual search result to a
   * user-specific search results list.
   * 
   * @param webRequest
   *          The web request instance
   * @param userName
   *          The useraccount that is creating the item
   * @param listName
   *          The name of the list to create
   * @param what
   *          Collection of opaIds separated by commas of the items to be
   *          created
   * @param format
   *          The output format. Either json or xml. Default is xml
   * @param pretty
   *          Specifies if the output should be pretty printed. Default is true
   * @return ResponseEntity with the json/xml representation of either the
   *         registered item or any encountered error.
   */
  @SuppressWarnings("unchecked")
  @RequestMapping(value = "/iapi/v1/lists/add/{listName:.+}", method = RequestMethod.POST)
  public ResponseEntity<String> addToUserList(
      HttpServletRequest request,
      // WebRequest webRequest,
      @PathVariable String listName,
      @RequestParam(value = "what", required = false) String what,
      @RequestParam(value = "rows", required = false, defaultValue = "20") int rows,
      @RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(value = "action", required = false, defaultValue = "addtolist") String action,
      @RequestParam(value = "format", required = false, defaultValue = "json") String format,
      @RequestParam(value = "pretty", required = false, defaultValue = "true") boolean pretty) {

    String message = "";
    HttpStatus status = HttpStatus.OK;
    String resultType = "userList";
    String resultsType = "ListItems";
    // UserListErrorCode errorCode = UserListErrorCode.NONE;
    int totalItemsAddedToList = 0;

    try {
      // Build the Aspire OPA response object
      AspireObject responseObj = new AspireObject("opaResponse");

      // Get the request path
      String requestPath = PathUtils.getServeletPath(request);

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
      if (action.equals("addToListFromSearch")) {
        requestParams.put("what", request.getQueryString());
      } else {
        requestParams.put("what", what);
      }
      requestParams.put("rows", rows);
      requestParams.put("offset", offset);
      requestParams.put("format", format);
      requestParams.put("pretty", pretty);

      // Validate parameters
      //Params 'q', facet values and 'tabType' might be provided by a search
      String[] paramNamesStringArray = { "q", "tabType", "what", "rows", "offset",
          "action", "format", "pretty", "f.level", "f.oldScope", "f.materialsType",
          "f.locationIds", "f.dateRangeFacet" };
      LinkedHashMap<String, String> validRequestParameterNames = StringUtils
          .convertStringArrayToLinkedHashMap(paramNamesStringArray);
      if (!ValidationUtils.validateRequestParameterNames(
          validRequestParameterNames, request.getQueryString())) {
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
          // We get the list from the database to add the item
          UserList userList = viewUserListService.getList(listName,
              userAccount.getAccountId());

          // Validate target list exists.
          if (userList == null) {
            status = HttpStatus.NOT_FOUND;
            userListValidator.setStandardError(UserListErrorCode.INVALID_LIST);
            // Validate the list already not contains the item
          } else {
            if (action.equals("addToListFromSearch")) {

              // Set opaPath Variable
              String opaPath = "iapi/" + Constants.API_VERS_NUM;
              String query = "?" + request.getQueryString() + "&apiType=iapi";

              // Call the search service - get the brief results
              TreeMap<String, Object> listSearchResults = new TreeMap<String, Object>();
              TreeMap<Integer, BriefResults> documentBriefResults = new TreeMap<Integer, BriefResults>();

              listSearchResults = opaSearchService.getBriefResults(opaPath,
                  query, false);

              documentBriefResults = (TreeMap<Integer, BriefResults>) listSearchResults
                  .get("documentBriefResults");

              // Build a string of comma delimited opaIds returned by the search
              what = "";
              for (int x = 0; x < documentBriefResults.size(); x++) {
                BriefResults briefResultsObj = new BriefResults();
                briefResultsObj = (BriefResults) documentBriefResults.get(x);
                what = what + briefResultsObj.getOpaId() + ",";
              }
              what = what.substring(0, what.length() - 1);
            }

            String[] opaIds = what.split(",");

            logger.debug(" Total OpaIds to add to list (" + listName + ") == "
                + opaIds.length);

            List<String> opaIdsToAddToList = new ArrayList<String>();
            opaIdsToAddToList = addToUserListService.getOpaIdsToAddToList(
                userList.getListId(), opaIds);

            logger.debug(" Total OpaIds to add to list (" + listName + ") == "
                + opaIdsToAddToList.size());

            int resultsLimitForUserType = getLimitForUser(userAccount
                .getAccountType());

            if (opaIdsToAddToList.size() == 0) {

              // If every opaId to add is a duplicate - RETURN AN ERROR
              userListValidator
                  .setStandardError(UserListErrorCode.DUPLICATE_LIST_ITEM);
              userListValidator.setIsValid(false);
            } else if ((opaIdsToAddToList.size() + userList.getTotal()) > resultsLimitForUserType) {

              // If the (total opaIds to add) + (the current list items total) >
              // total allowed for a user - RETURN AN ERROR
              userListValidator
                  .setStandardError(UserListErrorCode.MAX_ITEM_NUMBER_REACHED);
              userListValidator.setIsValid(false);
            } else {

              // Add items to the list
              totalItemsAddedToList = addToUserListService
                  .batchAddOpaIdsToList(userList.getListId(), opaIdsToAddToList);

              if (totalItemsAddedToList != opaIdsToAddToList.size()) {
                userListValidator
                    .setStandardError(UserListErrorCode.INTERNAL_ERROR);
                userListValidator.setIsValid(false);
              } else {

                LinkedHashMap<String, Object> resultsMap = new LinkedHashMap<String, Object>();
                if (totalItemsAddedToList > 0) {

                  logger.debug(" Added " + totalItemsAddedToList + " to list "
                      + listName);

                  resultsMap.put(resultType, totalItemsAddedToList);

                  // Set header params
                  LinkedHashMap<String, Object> headerParams = new LinkedHashMap<String, Object>();
                  headerParams.put("@status", String.valueOf(status));
                  headerParams.put("time",
                      TimestampUtils.getUtcTimestampString());

                  // Add request params to the response
                  headerParams.put("request", requestParams);

                  // Set response header
                  apiResponse.setResponse(responseObj, "header", headerParams);

                  // Set response results
                  apiResponse.setResponse(responseObj, resultsType, resultsMap);
                }
              }
            }
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
        LinkedHashMap<String, Object> errorValuesMap = new LinkedHashMap<String, Object>();
        errorValuesMap
            .put("@code", userListValidator.getErrorCode().toString());
        errorValuesMap.put("description", userListValidator.getErrorCode()
            .getErrorMessage());
        errorValuesMap.put("userName", userName);
        errorValuesMap.put("listName", listName);
        if (action.equals("addToListFromSearch")) {
          errorValuesMap.put("what", request.getQueryString());
        } else {
          errorValuesMap.put("what", what);
        }
        errorValuesMap.put("format", format);
        errorValuesMap.put("pretty", pretty);

        // Set response header
        apiResponse.setResponse(responseObj, "header", headerParams);

        // Set response error
        apiResponse.setResponse(responseObj, "error", errorValuesMap);
      }

      // Build response string using the response object
      message = apiResponse
          .getResponseOutputString(responseObj, format, pretty);

      // Close Aspire response object
      responseObj.close();

    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      // throw new OpaRuntimeException(e);
    }

    ResponseEntity<String> entity = new ResponseEntity<String>(message, status);
    return entity;
  }

  private int getLimitForUser(String accountType) {

    if (accountType != null) {
      switch (accountType.toLowerCase()) {
        case "standard":
          return configurationService.getConfig().getMaxSearchResultsStandard();
        case "power":
          return configurationService.getConfig().getMaxSearchResultsPower();
      }
    } else {
      return configurationService.getConfig().getMaxSearchResultsPublic();
    }

    return 0;
  }
}
