package gov.nara.opa.api.controller.system;

import gov.nara.opa.api.response.APIResponse;
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.system.Config;
import gov.nara.opa.api.system.ConfigurationErrorCode;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.utils.PathUtils;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.api.validation.system.ConfigurationValidator;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.utils.TimestampUtils;

import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;

@Controller
public class ConfigurationController {
  private static OpaLogger logger = OpaLogger
      .getLogger(ConfigurationController.class);

  @Value("${configFilePath}")
  private String configFilePath;

  @Autowired
  private ConfigurationService configurationService;

  @Autowired
  private ConfigurationValidator configurationValidator;

  @Autowired
  APIResponse apiResponse;

  static Logger log = Logger.getLogger(ConfigurationController.class);

  /**
   * Retrieves the public user configuration info
   * 
   * @param format
   *          OPA Response Object Format
   * @return Aspire JSON/XML Response Object
   */
  @RequestMapping(value = {
      "/" + Constants.INTERNAL_API_PATH + "/" + Constants.API_VERS_NUM
          + "/public/configuration"}, method = RequestMethod.GET)
  public ResponseEntity<String> getPublicConfig(
      WebRequest webRequest,
      @RequestParam(value = "format", required = false, defaultValue = "json") String format,
      @RequestParam(value = "pretty", required = false, defaultValue = "true") boolean pretty) {
    
    HttpStatus status = HttpStatus.OK;
    String action = "getPublicConfig";
    String resultType = "configuration";
    String responseMessage = "";

    try {
      // Build the Aspire OPA response object
      AspireObject responseObj = new AspireObject("opaResponse");

      // Get the request path
      String requestPath = PathUtils.getPathFromWebRequest(webRequest);

      // Validate the input parameters

      String[] paramNamesStringArray = { "format", "pretty" };
      LinkedHashMap<String, String> validRequestParameterNames = StringUtils
          .convertStringArrayToLinkedHashMap(paramNamesStringArray);
      if (!ValidationUtils.validateRequestParameterNames(
          validRequestParameterNames, webRequest.getParameterNames())) {
        status = HttpStatus.BAD_REQUEST;
        configurationValidator
            .setStandardError(ConfigurationErrorCode.INVALID_PARAMETER);
        configurationValidator.getErrorCode().setErrorMessage(
            ErrorConstants.invalidParameterName);
        configurationValidator.setIsValid(false);
      } else {
        configurationValidator.validate(format);
      }

      // If the input values are valid - get the configuration information
      if (configurationValidator.getIsValid()) {

        // Get the config.xml file values
        Config configFileValues = configurationService
            .getConfig(configFilePath);

        // If the config.xml file returns successfully
        if (configFileValues != null) {

          // Set header params
          LinkedHashMap<String, Object> headerParams = new LinkedHashMap<String, Object>();
          headerParams.put("@status", String.valueOf(status));
          headerParams.put("time", TimestampUtils.getUtcTimestampString());

          // Set request params
          LinkedHashMap<String, Object> requestParams = new LinkedHashMap<String, Object>();
          requestParams.put("@path", requestPath);
          requestParams.put("action", action);
          requestParams.put("format", format);
          requestParams.put("pretty", pretty);
          headerParams.put("request", requestParams);

          // Set result values
          LinkedHashMap<String, Object> resultValues = new LinkedHashMap<String, Object>();

          // SESSION SETTINGS
          resultValues.put("sessionTimeout",
              configFileValues.getSessionTimeout());

          // PRINT SETTINGS
          resultValues.put("maxPrintResults",
              configFileValues.getMaxPrintResults());

          // ROW RETRIEVAL SETTINGS
          resultValues.put("maxContributionRows",
              configFileValues.getMaxContributionRows());

          // SEARCH SETTINGS
          resultValues.put("maxSearchResultsPublic",
              configFileValues.getMaxSearchResultsPublic());

          // TRANSCRIPTION INACTIVITY TIME SETTINGS
          resultValues.put("transcriptionInactivityTime",
              configFileValues.getTranscriptionInactivityTime());

          // Set response header
          apiResponse.setResponse(responseObj, "header", headerParams);

          // Set response results
          apiResponse.setResponse(responseObj, resultType, resultValues);

        } else {
          configurationValidator
              .setStandardError(ConfigurationErrorCode.CONFIG_FILE_NOT_FOUND);
        }
      }

      // Build response object error
      if (!configurationValidator.getIsValid()) {

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
        errorParams.put("@code", configurationValidator.getErrorCode()
            .toString());
        errorParams.put("description", configurationValidator.getErrorCode()
            .getErrorMessage());
        errorParams.put("format", format);
        errorParams.put("pretty", pretty);

        // Set response header
        apiResponse.setResponse(responseObj, "header", headerParams);

        // Set response error
        apiResponse.setResponse(responseObj, "error", errorParams);

      }
      responseObj.close();

      // Build response string using the response object
      responseMessage = apiResponse.getResponseOutputString(responseObj,
          format, pretty);

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      // throw new OpaRuntimeException(e);
    }

    ResponseEntity<String> entity = new ResponseEntity<String>(responseMessage,
        status);

    return entity;

  }
  

  /**
   * Method to retrieve the system config.xml file
   * 
   * @param format
   *          OPA Response Object Format
   * @return Aspire JSON/XML Response Object
   */
  @RequestMapping(value = {
      "/" + Constants.INTERNAL_API_PATH + "/" + Constants.API_VERS_NUM
          + "/administrator/configuration",
      "/" + Constants.PUBLIC_API_PATH + "/" + Constants.API_VERS_NUM
          + "/system/configuration" }, method = RequestMethod.GET)
  public ResponseEntity<String> get(
      WebRequest webRequest,
      @RequestParam(value = "format", required = false, defaultValue = "json") String format,
      @RequestParam(value = "pretty", required = false, defaultValue = "true") boolean pretty) {

    // Log File Write
    log.info("start get(): format=" + format + ", pretty=" + pretty);

    HttpStatus status = HttpStatus.OK;
    String action = "getConfig";
    String resultType = "configuration";
    String responseMessage = "";

    try {
      // Build the Aspire OPA response object
      AspireObject responseObj = new AspireObject("opaResponse");

      // Get the request path
      String requestPath = PathUtils.getPathFromWebRequest(webRequest);

      // Validate the input parameters

      String[] paramNamesStringArray = { "format", "pretty" };
      LinkedHashMap<String, String> validRequestParameterNames = StringUtils
          .convertStringArrayToLinkedHashMap(paramNamesStringArray);
      if (!ValidationUtils.validateRequestParameterNames(
          validRequestParameterNames, webRequest.getParameterNames())) {
        status = HttpStatus.BAD_REQUEST;
        configurationValidator
            .setStandardError(ConfigurationErrorCode.INVALID_PARAMETER);
        configurationValidator.getErrorCode().setErrorMessage(
            ErrorConstants.invalidParameterName);
        configurationValidator.setIsValid(false);
      } else {
        configurationValidator.validate(format);
      }

      // If the input values are valid - get the configuration information
      if (configurationValidator.getIsValid()) {

        // Get the config.xml file values
        Config configFileValues = configurationService
            .getConfig(configFilePath);

        // If the config.xml file returns successfully
        if (configFileValues != null) {

          // Set header params
          LinkedHashMap<String, Object> headerParams = new LinkedHashMap<String, Object>();
          headerParams.put("@status", String.valueOf(status));
          headerParams.put("time", TimestampUtils.getUtcTimestampString());

          // Set request params
          LinkedHashMap<String, Object> requestParams = new LinkedHashMap<String, Object>();
          requestParams.put("@path", requestPath);
          requestParams.put("action", action);
          requestParams.put("format", format);
          requestParams.put("pretty", pretty);
          headerParams.put("request", requestParams);

          // Set result values
          LinkedHashMap<String, Object> resultValues = new LinkedHashMap<String, Object>();

          // UI SETTINGS
          resultValues
              .put("searchRunTime", configFileValues.getSearchRunTime());

          // SESSION SETTINGS
          resultValues.put("sessionTimeout",
              configFileValues.getSessionTimeout());

          // FAILED LOGIN ATTEMPT SETTINGS
          resultValues
              .put("loginAttempts", configFileValues.getLoginAttempts());
          resultValues.put("timeLocked", configFileValues.getTimeLocked());
          resultValues.put("attemptsTime", configFileValues.getAttemptsTime());

          // SEARCH SETTINGS
          resultValues.put("maxSearchResultsPublic",
              configFileValues.getMaxSearchResultsPublic());
          resultValues.put("maxSearchResultsStandard",
              configFileValues.getMaxSearchResultsStandard());
          resultValues.put("maxSearchResultsPower",
              configFileValues.getMaxSearchResultsPower());

          // PUBLIC API SEARCH SETTINGS
          resultValues.put("maxApiSearchResults",
              configFileValues.getMaxApiSearchResults());

          // USER LIST SETTINGS
          resultValues.put("maxResultsPerListPublic",
              configFileValues.getMaxResultsPerListPublic());
          resultValues.put("maxResultsPerListStandard",
              configFileValues.getMaxResultsPerListStandard());
          resultValues.put("maxResultsPerListPower",
              configFileValues.getMaxResultsPerListPower());
          resultValues.put("maxListsPerUser",
              configFileValues.getMaxListsPerUser());

          // PRINT SETTINGS
          resultValues.put("maxPrintResults",
              configFileValues.getMaxPrintResults());

          // BULK EXPORT SETTINGS
          resultValues.put("maxNonBulkTimer",
              configFileValues.getMaxNonBulkTimer());
          resultValues.put("maxNonBulkFileSizeLimit",
              configFileValues.getMaxNonBulkFileSizeLimit());
          resultValues.put("maxBulkExportFileSize",
              configFileValues.getMaxBulkExportFileSize());
          resultValues.put("bulkExpDays", configFileValues.getBulkExpDays());

          // ROW RETRIEVAL SETTINGS
          resultValues.put("maxNotificationRows",
              configFileValues.getMaxNotificationRows());
          resultValues.put("maxOpaTitlesRows",
              configFileValues.getMaxOpaTitlesRows());
          resultValues.put("maxSummaryRows",
              configFileValues.getMaxSummaryRows());
          resultValues.put("maxContributionRows",
              configFileValues.getMaxContributionRows());

          // ANNOTATION SETTINGS
          resultValues.put("tagsLength", configFileValues.getTagsLength());
          resultValues.put("commentsLength",
              configFileValues.getCommentsLength());
          resultValues.put("commentsFormat",
              configFileValues.getCommentsFormat());

          // TRANSCRIPTION INACTIVITY TIME SETTINGS
          resultValues.put("transcriptionInactivityTime",
              configFileValues.getTranscriptionInactivityTime());

          // CONTRIBUTION DISPLAY TIME SETTINGS
          resultValues.put("transcriptionsDisplayTime",
              configFileValues.getTranscriptionsDisplayTime());
          resultValues.put("tagsDisplayTime",
              configFileValues.getTagsDisplayTime());

          // GENERAL SETTINGS
          resultValues.put("naraEmail", configFileValues.getNaraEmail());

          // BANNER LINK SETTINGS
          resultValues.put("bannerLinkHomeDisplay",
              configFileValues.getBannerLinkHomeDisplay());
          resultValues.put("bannerLinkHome",
              configFileValues.getBannerLinkHome());
          resultValues.put("bannerLinkResearchRoomDisplay",
              configFileValues.getBannerLinkResearchRoomDisplay());
          resultValues.put("bannerLinkResearchRoom",
              configFileValues.getBannerLinkResearchRoom());
          resultValues.put("bannerLinkContactUsDisplay",
              configFileValues.getBannerLinkContactUsDisplay());
          resultValues.put("bannerLinkContactUs",
              configFileValues.getBannerLinkContactUs());
          resultValues.put("bannerLinkStatisticsDisplay",
              configFileValues.getBannerLinkStatisticsDisplay());
          resultValues.put("bannerLinkStatistics",
              configFileValues.getBannerLinkStatistics());
          resultValues.put("bannerLinkHelpDisplay",
              configFileValues.getBannerLinkHelpDisplay());
          resultValues.put("bannerLinkHelp",
              configFileValues.getBannerLinkHelp());

          // Set response header
          apiResponse.setResponse(responseObj, "header", headerParams);

          // Set response results
          apiResponse.setResponse(responseObj, resultType, resultValues);

        } else {
          configurationValidator
              .setStandardError(ConfigurationErrorCode.CONFIG_FILE_NOT_FOUND);
        }
      }

      // Build response object error
      if (!configurationValidator.getIsValid()) {

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
        errorParams.put("@code", configurationValidator.getErrorCode()
            .toString());
        errorParams.put("description", configurationValidator.getErrorCode()
            .getErrorMessage());
        errorParams.put("format", format);
        errorParams.put("pretty", pretty);

        // Set response header
        apiResponse.setResponse(responseObj, "header", headerParams);

        // Set response error
        apiResponse.setResponse(responseObj, "error", errorParams);

      }
      responseObj.close();

      // Build response string using the response object
      responseMessage = apiResponse.getResponseOutputString(responseObj,
          format, pretty);

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      // throw new OpaRuntimeException(e);
    }

    ResponseEntity<String> entity = new ResponseEntity<String>(responseMessage,
        status);

    return entity;
  }
}
