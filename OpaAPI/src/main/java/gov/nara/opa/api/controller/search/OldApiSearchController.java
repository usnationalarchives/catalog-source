package gov.nara.opa.api.controller.search;

import gov.nara.opa.api.annotation.locks.AnnotationLock;
import gov.nara.opa.api.annotation.transcriptions.Transcription;
import gov.nara.opa.api.annotation.transcriptions.TranscriptionResponseValuesHelper;
import gov.nara.opa.api.dataaccess.user.UserAccountDao;
import gov.nara.opa.api.response.APIResponse;
import gov.nara.opa.api.search.FullResults;
import gov.nara.opa.api.search.SearchErrorCode;
import gov.nara.opa.api.services.ServiceResponseObject;
import gov.nara.opa.api.services.annotation.tags.ViewTagService;
import gov.nara.opa.api.services.annotation.transcriptions.ViewTranscriptionService;
import gov.nara.opa.api.services.search.OldApiSearchService;
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.services.user.contributions.UserContributionsService;
import gov.nara.opa.api.services.user.contributions.ViewOpaTitleService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.user.accounts.UserAccount;
import gov.nara.opa.api.utils.PathUtils;
import gov.nara.opa.api.utils.SessionUtils;
import gov.nara.opa.api.utils.XmlParser;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.api.validation.search.SearchValidator;
import gov.nara.opa.api.validation.user.contributions.UserContributionsValidator;
import gov.nara.opa.architecture.logging.ApiTypeLoggingEnum;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.logging.usage.UsageLogCode;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.utils.TimestampUtils;
import gov.nara.opa.common.valueobject.annotation.tags.TagValueObject;
import gov.nara.opa.common.valueobject.annotation.tags.TagsCollectionValueObject;

import java.io.UnsupportedEncodingException;
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
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;

@Controller
public class OldApiSearchController {
  private static OpaLogger logger = OpaLogger
      .getLogger(OldApiSearchController.class);

  @Autowired
  private UserContributionsValidator userContributionsValidator;

  @Autowired
  private TranscriptionResponseValuesHelper responseValueHelper;

  @Autowired
  private ViewTagService viewTagService;

  @Autowired
  private UserAccountDao userAccountDao;

  @Autowired
  private ViewOpaTitleService viewOpaTitleService;

  @Autowired
  private UserContributionsService userContributionsService;

  @Autowired
  private ViewTranscriptionService viewTranscriptionService;

  @Autowired
  private OldApiSearchService apiSearchService;

  @Autowired
  private SearchValidator searchValidator;

  @Autowired
  APIResponse apiResponse;

  @Autowired
  private ConfigurationService configurationService;

  @SuppressWarnings("unchecked")
  @RequestMapping(value = { "/apiold/" + Constants.API_VERS_NUM }, method = RequestMethod.GET)
  public ResponseEntity<String> search(
      WebRequest webRequest,
      HttpServletRequest request,

      // Search Parameters
      @RequestParam(value = "action", required = false, defaultValue = "search") String action,
      @RequestParam(value = "format", required = false, defaultValue = "json") String format,
      @RequestParam(value = "pretty", required = false, defaultValue = "true") boolean pretty,

      // Simple Keyword Queries
      @RequestParam(value = "q", required = false, defaultValue = "") String q,

      // Controlling the Output
      @RequestParam(value = "rows", required = false, defaultValue = "10") int rows,
      @RequestParam(value = "offset", required = false, defaultValue = "0") int offset) {

    HttpStatus status = HttpStatus.OK;
    TreeMap<String, Object> searchResults = new TreeMap<String, Object>();
    TreeMap<Integer, FullResults> documentFullResults = new TreeMap<Integer, FullResults>();
    String resultsType = "results";
    String resultType = "result";
    String responseMessage = "";
    String opaPath = "";
    String query = "";

    try {
      // Build the Aspire OPA response object
      AspireObject responseObj = new AspireObject("opaResponse");

      // Get the request path
      String requestPath = PathUtils.getPathFromWebRequest(webRequest);

      // Validate the input parameters
      String[] paramNamesStringArray = { "action", "rows", "offset",
          "q", "format", "pretty" };
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
      // Get value for user
      int maxRows = getLimitForUser();

      if (offset < 0) {
        searchValidator.setStandardError(SearchErrorCode.INVALID_OFFSET_LIMIT);
      } else if (rows + offset > maxRows) {
        searchValidator.setStandardError(SearchErrorCode.ROWS_LIMIT_EXCEEDED);
      }

      // Execute search if parameters are valid
      if (searchValidator.getIsValid()) {

        // Construct the opaPath and query variables
        opaPath = request.getServletPath().substring(1,
            request.getServletPath().length());
        String preQuery = request.getQueryString() + "&apiType=api";
        query = "?" + preQuery;

        // Call the search service - get the brief results
        searchResults = apiSearchService.getSearchResults(opaPath, query);

        int totalSearchResults = (int) searchResults.get("@total");
        double queryTime = (double) searchResults.get("queryTime");

        documentFullResults = (TreeMap<Integer, FullResults>) searchResults
            .get("documentFullResults");

        // If the search returns brief results
        if (documentFullResults != null) {

          // Record log entry
          String logMessage = String.format(
              " Query={%1$s}, queryTime=%2$f, totalResults=%3$d", preQuery,
              queryTime, totalSearchResults);

          logger.usage(this.getClass(), ApiTypeLoggingEnum.API_TYPE_PUBLIC,
              UsageLogCode.SEARCH, logMessage);
          logger.info(logMessage);

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

          resultsMap.put("queryTime", Double.toString(queryTime));
          resultsMap.put("total", Integer.toString(totalSearchResults));
          resultsMap.put("offset", Integer.toString(offset));
          resultsMap.put("rows", Integer.toString(rows));

          // Iterate through the search results / full results
          Set<Integer> s1 = documentFullResults.keySet();
          Iterator<Integer> iter1 = s1.iterator();
          while (iter1.hasNext()) {
            int docNumber = iter1.next();
            FullResults fullResultsObj = documentFullResults.get(docNumber);
            Float score = fullResultsObj.getScore();
            String type = fullResultsObj.getType();
            String naId = fullResultsObj.getNaId();
            String opaId = fullResultsObj.getOpaId();
            String url = fullResultsObj.getUrl();
            String description = fullResultsObj.getDescription();
            String authority = fullResultsObj.getAuthority();
            String objects = fullResultsObj.getObjects();
            LinkedHashMap<String, Object> resultValuesMap = new LinkedHashMap<String, Object>();
            resultValuesMap.put("num", Integer.toString(docNumber));
            resultValuesMap.put("score", Float.toString(score));
            resultValuesMap.put("type", type);
            resultValuesMap.put("naId", naId);
            resultValuesMap.put("opaId", opaId);
            resultValuesMap.put("url", url);
            XmlParser xmlParser = new XmlParser();
            Map<Object, Object> descriptionMap = new HashMap<Object, Object>();
            Map<Object, Object> authorityMap = new HashMap<Object, Object>();
            Map<Object, Object> objectsMap = new HashMap<Object, Object>();
            if (!description.equals("")) {
              descriptionMap = xmlParser.parseXML(description);
            }
            if (!authority.equals("")) {
              authorityMap = xmlParser.parseXML(authority);
            }
            if (!objects.equals("")) {
              objectsMap = xmlParser.parseXML(objects);
            }
            resultValuesMap.put("description", descriptionMap);
            resultValuesMap.put("authority", authorityMap);
            resultValuesMap.put("objects", objectsMap);

            LinkedHashMap<String, Object> publicContributions = getPublicContributions(naId);
            if (publicContributions != null && publicContributions.size() > 0)
              resultValuesMap.put("PublicContributions", publicContributions);

            resultsValueList.add(resultValuesMap);

          }
          resultsMap.put(resultType, resultsValueList);

          // Set response header
          apiResponse.setResponse(responseObj, "header", headerParams);

          // Set response results
          apiResponse.setResponse(responseObj, resultsType, resultsMap);

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

  private int getLimitForUser() {

    UserAccount sessionUser = SessionUtils.getSessionUser();

    if (sessionUser != null) {
      switch (sessionUser.getAccountType().toLowerCase()) {
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

  @SuppressWarnings("unchecked")
  public LinkedHashMap<String, Object> getPublicContributions(String naIds)
      throws DataAccessException, UnsupportedEncodingException {

    String[] naIdsList = naIds.split(",");

    TagsCollectionValueObject tags = viewTagService.getTagsByNaIds(naIdsList);

    ServiceResponseObject transcriptionsResponseObject = viewTranscriptionService
        .selectByNaIds(naIdsList);
    HashMap<String, Object> results = transcriptionsResponseObject
        .getContentMap();
    List<Transcription> transcriptions = (List<Transcription>) results
        .get("Transcriptions");

    LinkedHashMap<String, Object> tagsHashMap = new LinkedHashMap<String, Object>();
    LinkedHashMap<String, Object> publicValuesMap = new LinkedHashMap<String, Object>();
    LinkedHashMap<String, Object> transcriptionsHashMap = new LinkedHashMap<String, Object>();

    // Adding Tags:
    ArrayList<LinkedHashMap<String, Object>> tagsResultsValueList = new ArrayList<LinkedHashMap<String, Object>>();
    if (tags.getTags() != null) {
      for (TagValueObject tag : tags.getTags()) {
        if (tag.getNaId().equals(naIds)) {
          LinkedHashMap<String, Object> tagsResultsValue = new LinkedHashMap<String, Object>();
          tagsResultsValue.put("text", tag.getAnnotation());
          tagsResultsValue.put("user",
              userAccountDao.selectByAccountId(tag.getAccountId())
                  .getUserName());
          tagsResultsValue.put("created",
              TimestampUtils.getUtcString(tag.getAnnotationTS()));
          tagsResultsValueList.add(tagsResultsValue);
        }
      }
      if (tagsResultsValueList.size() > 0) {
        tagsHashMap.put("total", tagsResultsValueList.size());
        tagsHashMap.put("tag", tagsResultsValueList);
        publicValuesMap.put("tags", tagsHashMap);
      }
    }
    // Adding transcriptions:
    ArrayList<LinkedHashMap<String, Object>> transcriptionsResultsValueList = new ArrayList<LinkedHashMap<String, Object>>();
    if (transcriptions != null) {
      for (Transcription transcription : transcriptions) {
        if (transcription.getNaId().equals(naIds)) {
          // Service call
          ServiceResponseObject responseObject = viewTranscriptionService
              .getFullTranscription(transcription.getNaId(),
                  transcription.getObjectId());

          HashMap<String, Object> transcriptionResults = responseObject
              .getContentMap();

          AnnotationLock resultLock = (AnnotationLock) transcriptionResults
              .get("AnnotationLock");
          UserAccount resultUserAccount = (UserAccount) transcriptionResults
              .get("UserAccount");
          UserAccount lockUserAccount = (UserAccount) transcriptionResults
              .get("LockUserAccount");
          Transcription lastTranscription = (Transcription) transcriptionResults
              .get("Transcription");
          LinkedHashMap<Integer, Transcription> previousTranscriptions = (LinkedHashMap<Integer, Transcription>) transcriptionResults
              .get("TranscriptionsByUser");
          HashMap<Integer, UserAccount> previousContributors = (HashMap<Integer, UserAccount>) transcriptionResults
              .get("UserMap");

          // Initialize response value helper with retrieved items
          responseValueHelper.Init();
          responseValueHelper.setAnnotationLock(resultLock);
          responseValueHelper.setUserAccount(resultUserAccount);
          responseValueHelper.setLockUserAccount(lockUserAccount);
          responseValueHelper.setTranscription(lastTranscription);
          responseValueHelper.setContributorMap(previousContributors);
          responseValueHelper.setPreviousTranscriptions(previousTranscriptions);

          transcriptionsResultsValueList.add(responseValueHelper
              .getResponseValues());
        }
      }
      if (transcriptionsResultsValueList.size() > 0) {
        transcriptionsHashMap.put("total",
            transcriptionsResultsValueList.size());
        transcriptionsHashMap.put("transcription",
            transcriptionsResultsValueList);
        publicValuesMap.put("transcriptions", transcriptionsHashMap);
      }
    }

    return publicValuesMap;

  }

}
