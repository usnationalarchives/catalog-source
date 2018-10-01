package gov.nara.opa.api.controller.search;

import gov.nara.opa.api.response.APIResponse;
import gov.nara.opa.api.search.BriefResults;
import gov.nara.opa.api.search.SearchErrorCode;
import gov.nara.opa.api.search.WebResults;
import gov.nara.opa.api.services.search.OpaSearchService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.user.accounts.UserAccount;
import gov.nara.opa.api.utils.PathUtils;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.api.validation.search.SearchValidator;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.utils.TimestampUtils;

import java.io.StringReader;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Controller
public class OpaSearchController {
  private static OpaLogger logger = OpaLogger
      .getLogger(OpaSearchController.class);

  @Autowired
  private OpaSearchService opaSearchService;

  @Autowired
  private SearchValidator searchValidator;

  @Autowired
  APIResponse apiResponse;

  /**
   * Method to proxy a search request to the Solr search engine
   * 
   * @param webRequest
   *          Web request instance
   * @param request
   *          Http request
   * @param action
   *          Search request type (search, addList, newList, export)
   * @param format
   *          OPA Response Object Format
   * @param pretty
   *          Pretty Print
   * @param q
   *          query Solr to execute
   * @param rows
   *          number of rows to return
   * @param offset
   *          offset value
   * @param listName
   *          name of list to add search results to
   * @return ResponseEntity with the json/xml representation of either the
   *         search results or any encountered error.
   */
  @SuppressWarnings("unchecked")
  @RequestMapping(value = { "/rf/iapi/" + Constants.API_VERS_NUM }, method = RequestMethod.GET)
  public ResponseEntity<String> search(
      WebRequest webRequest,
      HttpServletRequest request,

      // Search Parameters
      @RequestParam(value = "action", required = false) String action,
      @RequestParam(value = "format", required = false, defaultValue = "json") String format,
      @RequestParam(value = "pretty", required = false, defaultValue = "true") boolean pretty,

      // Simple Keyword Queries
      @RequestParam(value = "q", required = false, defaultValue = "") String q,

      // Controlling the Output
      @RequestParam(value = "rows", required = false, defaultValue = "10") int rows,
      @RequestParam(value = "offset", required = false, defaultValue = "0") int offset,

      // Web Search
      @RequestParam(value = "tabType", required = false, defaultValue = "") String queryTabType,

      // Saved Result Lists (logged-in users only)
      @RequestParam(value = "list", required = false, defaultValue = "") String listName) {

    HttpStatus status = HttpStatus.OK;
    TreeMap<String, Object> briefResults = new TreeMap<String, Object>();
    TreeMap<Integer, BriefResults> documentBriefResults = new TreeMap<Integer, BriefResults>();
    TreeMap<Integer, WebResults> documentWebResults = new TreeMap<Integer, WebResults>();
    UserAccount userAccount = new UserAccount();
    String resultsType = "results";
    String resultType = "result";
    String responseMessage = "";
    String opaPath = "";
    String query = "";
    boolean webGrouping = false;

    // Get the session user account object (If the user is logged-in)
    Authentication auth = SecurityContextHolder.getContext()
        .getAuthentication();
    if (!auth.getName().equals("anonymousUser")) {
      userAccount = (UserAccount) auth.getDetails();
    }

    // Get the list of naIds selected by the user
    // HashMap<String,String> selectedIdsMap = new HashMap<String,String>();
    // if (request.getParameter("abc") != null)
    // selectedIdsMap.put(request.getParameter("abc"), "true");

    try {
      // Build the Aspire OPA response object
      AspireObject responseObj = new AspireObject("opaResponse");

      // Get the request path
      String requestPath = PathUtils.getPathFromWebRequest(webRequest);

      // Set request params
      // requestParams.put("query", request.getQueryString());

      // Validate the input parameters
      String[] paramNamesStringArray = { "action", "rows", "offset",
          "q", "tabType", "list", "format", "pretty" };
      LinkedHashMap<String, String> validRequestParameterNames = StringUtils
          .convertStringArrayToLinkedHashMap(paramNamesStringArray);
      if (!ValidationUtils.validateRequestParameterNames(
          validRequestParameterNames, request.getQueryString())) {
        status = HttpStatus.BAD_REQUEST;
        searchValidator.setStandardError(SearchErrorCode.INVALID_PARAMETER);
        searchValidator.getErrorCode().setErrorMessage(ErrorConstants.invalidParameterName);
        searchValidator.setIsValid(false);
      } else {
        searchValidator.validate(action, format);
      }
      

      // Validate q paramter
      // Cannot be too long or contain embedded HTML

      // Validate rows and offset based on user account type
      if (rows > 100) {
        searchValidator.setStandardError(SearchErrorCode.ROWS_LIMIT_EXCEEDED);
      } else if (offset < 0) {
        searchValidator.setStandardError(SearchErrorCode.INVALID_OFFSET_LIMIT);
      }

      // Validate action/user privileges
      if (searchValidator.getIsValid()) {

        // Process List Management or Export actions
        if (action.equals("addList") || action.equals("newList")
            || action.equals("export")) {
          if (userAccount == null) {
            searchValidator.setStandardError(SearchErrorCode.NOT_API_LOGGED_IN);
          } else {
            // int accountId = userAccount.getAccountId();
            // String accountRights =
            // userAccount.getAccountRights();

            // if (action.equals("addList") ||
            // action.equals("newList")) {

            // } else if (action.equals("export")) {

            // }
          }
        }
      }

      // Execute search if parameters are valid
      if (searchValidator.getIsValid()) {

        // See of the web group results are required
        if (offset == 0 && queryTabType.equals("all")) {
          webGrouping = true;
        }

        // Construct the opaPath and query variables
        opaPath = request.getServletPath().substring(1,
            request.getServletPath().length());

        /**********************************************/
        opaPath = "iapi/v1";
        /**********************************************/

        query = "?" + request.getQueryString();
        query = query + "&facet=true&facet.fields=tabType";

        // Call the search service - get the brief results
        briefResults = opaSearchService.getBriefResults(opaPath, query,
            webGrouping);

        int totalSearchResults = (int) briefResults.get("@total");
        double queryTime = (double) briefResults.get("queryTime");
        documentBriefResults = (TreeMap<Integer, BriefResults>) briefResults
            .get("documentBriefResults");

        // If the search returns brief results
        if (documentBriefResults != null) {

          // Set response header information
          LinkedHashMap<String, Object> headerParams = new LinkedHashMap<String, Object>();
          headerParams.put("@status", String.valueOf(status));
          headerParams.put("time", TimestampUtils.getUtcTimestampString());

          // Set request params
          LinkedHashMap<String, Object> requestParams = new LinkedHashMap<String, Object>();
          requestParams.put("@path", requestPath);
          requestParams.put("action", action);
          requestParams.put("query", request.getQueryString());
          requestParams.put("format", format);
          requestParams.put("pretty", pretty);
          headerParams.put("request", requestParams);

          // Set result values
          LinkedHashMap<String, Object> resultsMap = new LinkedHashMap<String, Object>();
          ArrayList<LinkedHashMap<String, Object>> resultsValueList = new ArrayList<LinkedHashMap<String, Object>>();

          resultsMap.put("@queryTime", Double.toString(queryTime));
          resultsMap.put("@total", Integer.toString(totalSearchResults));
          resultsMap.put("@offset", Integer.toString(offset));
          resultsMap.put("rows", Integer.toString(rows));

          // Process the brief results
          Set<Integer> s1 = documentBriefResults.keySet();
          Iterator<Integer> iter1 = s1.iterator();
          while (iter1.hasNext()) {
            int docNumber = (Integer) iter1.next();
            BriefResults briefResultsObj = documentBriefResults.get(docNumber);
            String contentDetailUrl = briefResultsObj.getUrl();
            String iconType = briefResultsObj.getIconType();
            String thumbnailFile = briefResultsObj.getThumbnailFile();
            boolean hasOnline = briefResultsObj.getHasOnline();
            List<String> tabType = briefResultsObj.getTabType();
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
            // if (selectedIdsMap.containsKey(naId)){
            // resultValuesMap.put("seletedNaId", true);
            // } else {
            // resultValuesMap.put("seletedNaId", false);
            // }
            resultValuesMap.put("contentDetailUrl", contentDetailUrl);
            resultValuesMap.put("iconType", iconType);
            resultValuesMap.put("thumbnailFile", thumbnailFile);
            resultValuesMap.put("hasOnline", hasOnline);
            resultValuesMap.put("teaser", teaser);
            resultValuesMap.put("tabType", tabType);
            resultValuesMap.put("briefResults", briefResultsValues);
            resultsValueList.add(resultValuesMap);
          }
          resultsMap.put(resultType, resultsValueList);

          // Set response header
          apiResponse.setResponse(responseObj, "header", headerParams);

          // Set response results
          apiResponse.setResponse(responseObj, resultsType, resultsMap);

          // Process the facet results
          ArrayList<LinkedHashMap<String, Object>> facetResultsList = new ArrayList<LinkedHashMap<String, Object>>();
          facetResultsList = (ArrayList<LinkedHashMap<String, Object>>) briefResults
              .get("facetResults");
          responseObj.push("facets");
          GsonBuilder builder = new GsonBuilder().disableHtmlEscaping();
          Gson gson = builder.create();
          for (int x = 0; x < facetResultsList.size(); x++) {
            LinkedHashMap<String, Object> facetResults = new LinkedHashMap<String, Object>();
            facetResults = (LinkedHashMap<String, Object>) facetResultsList
                .get(x);

            String jsonResults = gson.toJson(facetResults);
            StringReader sr = new StringReader(jsonResults);
            responseObj.loadJson("field", sr);
          }
          responseObj.pop();

          // Process the web results
          documentWebResults = (TreeMap<Integer, WebResults>) briefResults
              .get("webResults");

          // If the search returns web results
          if (documentWebResults != null) {

            // Set result values
            LinkedHashMap<String, Object> webResultsMap = new LinkedHashMap<String, Object>();
            ArrayList<LinkedHashMap<String, Object>> webResultsValueList = new ArrayList<LinkedHashMap<String, Object>>();

            webResultsMap.put("@queryTime", Double.toString(queryTime));
            webResultsMap.put("@total", Integer.toString(123));
            webResultsMap.put("@offset", Integer.toString(0));
            webResultsMap.put("rows", Integer.toString(3));

            // Iterate through the search results / web results
            Set<Integer> s2 = documentWebResults.keySet();
            Iterator<Integer> iter2 = s2.iterator();
            while (iter2.hasNext()) {
              int docNumber = (Integer) iter2.next();
              WebResults webResultsObj = documentWebResults.get(docNumber);
              Float score = webResultsObj.getScore();
              // String naId = webResultsObj.getNaId();
              String opaId = webResultsObj.getOpaId();
              String title = webResultsObj.getTitle();
              String webArea = webResultsObj.getWebArea();
              String webAreaUrl = webResultsObj.getWebAreaUrl();
              String url = webResultsObj.getUrl();
              String iconType = webResultsObj.getIconType();
              String teaser = webResultsObj.getTeaser();
              LinkedHashMap<String, Object> webResultValuesMap = new LinkedHashMap<String, Object>();
              webResultValuesMap.put("@num", Integer.toString(docNumber));
              webResultValuesMap.put("@score", Float.toString(score));
              webResultValuesMap.put("opaId", opaId);
              webResultValuesMap.put("title", title);
              webResultValuesMap.put("webArea", webArea);
              webResultValuesMap.put("webAreaUrl", webAreaUrl);
              webResultValuesMap.put("url", url);
              webResultValuesMap.put("iconType", iconType);
              webResultValuesMap.put("teaser", teaser);
              webResultsValueList.add(webResultValuesMap);
            }
            webResultsMap.put("result", webResultsValueList);
            responseObj.push("webPages");
            String jsonResults = gson.toJson(webResultsMap);
            StringReader sr = new StringReader(jsonResults);
            responseObj.loadJson("field", sr);
            responseObj.pop();
          }

        } else {
          // Return an error if the search method returns null
          searchValidator.setStandardError(SearchErrorCode.SYSTEM_ERROR);
        }
      }

      // If NOT valid - Set response error information
      if (!searchValidator.getIsValid()) {

        // Set error status
        status = HttpStatus.BAD_REQUEST;

        // Set header params
        LinkedHashMap<String, Object> headerParams = new LinkedHashMap<String, Object>();
        headerParams.put("@status", String.valueOf(status));
        headerParams.put("time", TimestampUtils.getUtcTimestampString());

        // Set request params
        LinkedHashMap<String, Object> requestParams = new LinkedHashMap<String, Object>();
        requestParams.put("@path", requestPath);
        requestParams.put("action", action);
        headerParams.put("request", requestParams);

        // Set error params
        LinkedHashMap<String, Object> errorParams = new LinkedHashMap<String, Object>();
        errorParams.put("@code", searchValidator.getErrorCode().toString());
        errorParams.put("description", searchValidator.getErrorCode()
            .getErrorMessage());
        errorParams.put("query", request.getQueryString());
        errorParams.put("format", format);
        errorParams.put("pretty", pretty);

        // Set response header
        apiResponse.setResponse(responseObj, "header", headerParams);

        // Set response error
        apiResponse.setResponse(responseObj, "error", errorParams);
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
