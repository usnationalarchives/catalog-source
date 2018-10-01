package gov.nara.opa.api.controller.user.lists;

import gov.nara.opa.api.dataaccess.user.UserAccountDao;
import gov.nara.opa.api.response.APIResponse;
import gov.nara.opa.api.search.BriefResults;
import gov.nara.opa.api.services.search.OpaSearchService;
import gov.nara.opa.api.services.user.lists.ViewUserListService;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.user.lists.UserList;
import gov.nara.opa.api.user.lists.UserListErrorCode;
import gov.nara.opa.api.user.lists.UserListItem;
import gov.nara.opa.api.utils.PathUtils;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.api.validation.user.lists.UserListValidator;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.utils.TimestampUtils;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ViewUserListEntriesController {

  @Autowired
  APIResponse apiResponse;

  @Autowired
  private UserAccountDao userAccountDao;

  @Autowired
  private OpaSearchService opaSearchService;

  @Autowired
  private UserListValidator userListValidator;

  @Autowired
  private ViewUserListService viewUserListService;

  private static OpaLogger logger = OpaLogger
      .getLogger(ViewUserListEntriesController.class);

  /**
   * Show the entries contained on a specified list.
   * 
   * @param request
   *          Http request
   * @param userName
   *          The useraccount that is displaying the items
   * @param listName
   *          The name of the list to consult
   * @param format
   *          The output format. Either json or xml. Default is xml
   * @param pretty
   *          Specifies if output should be printed formatted. Default is true
   * @return ResponseEntity with the json/xml representation of the entries
   *         found.
   */
  @SuppressWarnings("unchecked")
  @RequestMapping(value = "/iapi/v1/lists/viewentries/{listName:.+}", method = RequestMethod.GET)
  public ResponseEntity<String> viewUserListEntries(
      HttpServletRequest request,
      @PathVariable String listName,
      @RequestParam(value = "rows", required = false, defaultValue = "20") int rows,
      @RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(value = "username", required = false, defaultValue = "") String userName,
      @RequestParam(value = "format", required = false, defaultValue = "json") String format,
      @RequestParam(value = "pretty", required = false, defaultValue = "true") boolean pretty) {
    HttpStatus status = HttpStatus.OK;
    TreeMap<String, Object> listSearchResults = new TreeMap<String, Object>();
    TreeMap<Integer, BriefResults> documentBriefResults = new TreeMap<Integer, BriefResults>();
    String resultsType = "results";
    String resultType = "result";
    String action = "viewUserListEntries";
    String responseMessage = "";
    UserList listObj = new UserList();
    int accountId = 0;

    try {
      // Build the Aspire OPA response object
      AspireObject responseObj = new AspireObject("opaResponse");

      // Get the request path
      String requestPath = PathUtils.getServeletPath(request);

      // Create map with parameters received on the request
      LinkedHashMap<String, Object> requestParams = new LinkedHashMap<String, Object>();
      requestParams.put("@path", requestPath);
      requestParams.put("action", action);
      if (userName != null && !userName.equals(""))
        requestParams.put("username", userName);
      requestParams.put("listName", listName);
      requestParams.put("format", format);
      requestParams.put("pretty", pretty);
      requestParams.put("offset", offset);
      requestParams.put("rows", rows);

      // Validate parameters
      String[] paramNamesStringArray = { "username", "rows", "offset",
          "format", "pretty" };
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

      // If request parameters are valid
      if (userListValidator.getIsValid()) {

        if (userAccountDao.verifyIfUserNameExists(userName)) {
          // Get the account user_name
          UserAccountValueObject user = userAccountDao
              .selectByUserName(userName);
          accountId = user.getAccountId();
          // Validate that the user account is inactive
          if (user.getAccountStatus() == false) {
            userListValidator
                .setStandardError(UserListErrorCode.ACCOUNT_INACTIVE);
          }

        } else {
          status = HttpStatus.NOT_FOUND;
          userListValidator.setStandardError(UserListErrorCode.USER_NOT_FOUND);
        }

        // }

        // If request parameters are valid
        if (userListValidator.getIsValid()) {

          // retieve the list's opaIds
          listObj = viewUserListService.getList(listName, accountId);

          if (listObj != null) {

            // Add the items information and fill the total of items
            List<UserListItem> itemsFullList = viewUserListService
                .getListItems(listObj.getListId());
            List<UserListItem> itemsList = new ArrayList<UserListItem>();
            if (itemsFullList != null && itemsFullList.size() > 0) {
              for (int x = offset; x < Math.min((offset + rows),
                  itemsFullList.size()); x++) {
                itemsList.add(itemsFullList.get(x));
              }
            }

            // If the search returns brief results
            if (itemsList != null && itemsList.size() > 0) {
              // Construct the opaPath and query variables
              String opaPath = request.getServletPath().substring(1,
                  request.getServletPath().length());

              // Build the query string using the list's opaIds
              StringBuilder sb = new StringBuilder("?q=");
              for (UserListItem item : itemsList) {
                sb.append("(opaId:%22" + item.getOpaId() + "%22)%20OR%20");
              }
              sb.replace(sb.length() - 8, sb.length(), "");
              if (rows > 10) {
                sb.append("&rows=" + rows);
              }
              sb.append("&sort=title+ASC");
              String query = sb.toString().trim();

              // Call the search service - get the brief results
              listSearchResults = opaSearchService.getBriefResults(opaPath,
                  query, false);

              int totalSearchResults = itemsFullList.size();
              double queryTime = (double) listSearchResults.get("queryTime");

              documentBriefResults = (TreeMap<Integer, BriefResults>) listSearchResults
                  .get("documentBriefResults");

              // If the search returns brief results
              if (documentBriefResults != null
                  && documentBriefResults.size() > 0) {

                // Set response header information
                LinkedHashMap<String, Object> headerParams = new LinkedHashMap<String, Object>();
                headerParams.put("@status", String.valueOf(status));
                headerParams
                    .put("time", TimestampUtils.getUtcTimestampString());

                // Set request params
                headerParams.put("request", requestParams);

                // Set result values
                LinkedHashMap<String, Object> resultsMap = new LinkedHashMap<String, Object>();
                ArrayList<LinkedHashMap<String, Object>> resultsValueList = new ArrayList<LinkedHashMap<String, Object>>();

                resultsMap.put("@queryTime", Double.toString(queryTime));
                resultsMap.put("@total", Integer.toString(totalSearchResults));
                resultsMap.put("@offset", Integer.toString(offset));
                resultsMap.put("rows", Integer.toString(rows));

                // Iterate through the brief results
                Set<Integer> s1 = documentBriefResults.keySet();
                Iterator<Integer> iter1 = s1.iterator();
                while (iter1.hasNext()) {
                  int docNumber = iter1.next();
                  BriefResults briefResultsObj = documentBriefResults
                      .get(docNumber);
                  String contentDetailUrl = briefResultsObj.getUrl();
                  String thumbnailFile = briefResultsObj.getThumbnailFile();
                  boolean hasOnline = briefResultsObj.getHasOnline();
                  String teaser = briefResultsObj.getTeaser();
                  Float score = briefResultsObj.getScore();
                  String naId = briefResultsObj.getNaId();
                  String opaId = briefResultsObj.getOpaId();
                  HashMap<String, ArrayList<Map<String, Object>>> briefResultsValues = briefResultsObj
                      .getDocumentBriefResults();
                  LinkedHashMap<String, Object> resultValuesMap = new LinkedHashMap<String, Object>();
                  resultValuesMap.put("@num", Integer.toString(docNumber));
                  resultValuesMap.put("@score", Float.toString(score));
                  resultValuesMap.put("naId", naId);
                  resultValuesMap.put("opaId", opaId);
                  resultValuesMap.put("contentDetailUrl", contentDetailUrl);
                  resultValuesMap.put("thumbnailFile", thumbnailFile);
                  resultValuesMap.put("hasOnline", hasOnline);
                  resultValuesMap.put("teaser", teaser);
                  resultValuesMap.put("briefResults", briefResultsValues);
                  resultsValueList.add(resultValuesMap);
                }
                resultsMap.put(resultType, resultsValueList);

                // Set response header
                apiResponse.setResponse(responseObj, "header", headerParams);

                // Set response results
                apiResponse.setResponse(responseObj, resultsType, resultsMap);
              } else {
                // Return an error if the search method returns
                // null
                userListValidator
                    .setStandardError(UserListErrorCode.EMPTY_LIST);
              }
            } else {
              // Return an error if the search method returns null
              userListValidator.setStandardError(UserListErrorCode.EMPTY_LIST);
            }

          } else {
            status = HttpStatus.NOT_FOUND;
            userListValidator.setStandardError(UserListErrorCode.INVALID_LIST);
          }

        }
      }

      // Build response object error if the request results invalid
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
      // throw new OpaRuntimeException(ae);
    }

    ResponseEntity<String> entity = new ResponseEntity<String>(responseMessage,
        status);

    return entity;
  }

}
