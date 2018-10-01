package gov.nara.opa.api.controller.user.contributions;

import gov.nara.opa.api.annotation.TagErrorCode;
import gov.nara.opa.api.response.APIResponse;
import gov.nara.opa.api.services.ServiceResponseObject;
import gov.nara.opa.api.services.user.contributions.ViewOpaTitleService;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.user.contributions.UserContributionsErrorCode;
import gov.nara.opa.api.utils.PathUtils;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.api.validation.user.contributions.OpaTitlesValidator;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.utils.TimestampUtils;
import gov.nara.opa.common.valueobject.annotation.tags.OpaTitle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ViewTaggedTitlesController {
  private static OpaLogger logger = OpaLogger
      .getLogger(ViewTaggedTitlesController.class);

  @Autowired
  private APIResponse apiResponse;

  @Autowired
  private OpaTitlesValidator taggedTitlesValidator;

  @Autowired
  private ViewOpaTitleService viewOpaTitleService;

  /**
   * Get the list of titles that have been tagged with a specified tag
   * 
   * @param request
   *          The HttpServletRequest instance
   * @param tagText
   *          The tag text that we will look for
   * @param format
   *          The output format. Either json or xml. Default is xml
   * @param pretty
   *          Specifies if the output should be pretty printed. Default is true
   * @return Response Message with the collection of titles.
   */
  @SuppressWarnings("unchecked")
  @RequestMapping(value = "/iapi/v1/contributions/tags/titles", method = RequestMethod.GET)
  public ResponseEntity<String> viewTaggedTitles(
      HttpServletRequest request,
      @RequestParam(value = "tagtext", required = false, defaultValue = "") String tagText,
      @RequestParam(value = "username", required = false, defaultValue = "") String userName,
      @RequestParam(value = "title", required = false, defaultValue = "") String title,
      @RequestParam(value = "descOrder", required = false, defaultValue = "true") boolean descOrder,
      @RequestParam(value = "format", required = false, defaultValue = "json") String format,
      @RequestParam(value = "pretty", required = false, defaultValue = "true") boolean pretty,
      @RequestParam(value = "rows", required = false, defaultValue = "20") int rows,
      @RequestParam(value = "offset", required = false, defaultValue = "0") int offset) {

    String message = "";
    HttpStatus status = HttpStatus.OK;
    String action = "viewTaggedTitles";
    String resultType = "title";
    String resultsType = "titles";

    // Build the Aspire OPA response object
    AspireObject responseObj = new AspireObject("opaResponse");

    // Get the request path
    String requestPath = PathUtils.getServeletPath(request);

    // Set request params
    LinkedHashMap<String, Object> requestParams = new LinkedHashMap<String, Object>();
    requestParams.put("@path", requestPath);
    requestParams.put("tagtext", tagText);
    requestParams.put("username", userName);
    requestParams.put("title", title);
    requestParams.put("descOrder", descOrder);
    requestParams.put("action", action);
    requestParams.put("format", format);
    requestParams.put("pretty", pretty);
    requestParams.put("offset", offset);
    requestParams.put("rows", rows);

    try {
      
      // Validate parameters
      String[] paramNamesStringArray = { "tagtext", "username", "rows", "offset",
          "title", "descOrder", "format", "pretty" };
      LinkedHashMap<String, String> validRequestParameterNames = StringUtils
          .convertStringArrayToLinkedHashMap(paramNamesStringArray);
      if (!ValidationUtils.validateRequestParameterNames(
          validRequestParameterNames, request.getQueryString())) {
        status = HttpStatus.BAD_REQUEST;
        taggedTitlesValidator.setStandardError(UserContributionsErrorCode.INVALID_PARAMETER);
        taggedTitlesValidator.setMessage(ErrorConstants.invalidParameterName);
        taggedTitlesValidator.setIsValid(false);
      } else {
        taggedTitlesValidator.validateParameters(requestParams);
      }
      

      if (taggedTitlesValidator.getIsValid()) {
        if (!viewOpaTitleService.isValidUserName(userName)) {
          status = HttpStatus.NOT_FOUND;
          taggedTitlesValidator
              .setStandardError(UserContributionsErrorCode.USER_NOT_FOUND);
        } else {
          // Retrieve the collection of titles belonging to specified tag
          ServiceResponseObject response = viewOpaTitleService
              .viewTaggedTitles(tagText, title, userName, offset, rows,
                  descOrder);

          TagErrorCode errorCode = (TagErrorCode) response.getErrorCode();

          if (errorCode != TagErrorCode.NONE) {

            if (errorCode == TagErrorCode.NO_TAGS_FOUND) {
              status = HttpStatus.NOT_FOUND;
              taggedTitlesValidator
                  .setStandardError(UserContributionsErrorCode.TITLES_NOT_FOUND);
            } else {
              status = HttpStatus.BAD_REQUEST;
              taggedTitlesValidator
                  .setStandardError(UserContributionsErrorCode.INTERNAL_ERROR);
            }
          } else {

            List<OpaTitle> opaTitles = (List<OpaTitle>) response
                .getContentMap().get("OpaTitles");
            int totalCount = (int) response.getContentMap().get("TagCount");

            // Set header params
            LinkedHashMap<String, Object> headerParams = new LinkedHashMap<String, Object>();
            headerParams.put("@status", String.valueOf(status));
            headerParams.put("time", TimestampUtils.getUtcTimestampString());

            // Add all the lists found to the response object
            LinkedHashMap<String, Object> resultsMap = new LinkedHashMap<String, Object>();
            ArrayList<LinkedHashMap<String, Object>> resultsValueList = new ArrayList<LinkedHashMap<String, Object>>();

            for (OpaTitle opaTitle : opaTitles) {

              LinkedHashMap<String, Object> resultValuesMap = new LinkedHashMap<String, Object>();

              resultValuesMap.put("naId", opaTitle.getNaId());
              resultValuesMap.put("opaTitle", opaTitle.getOpaTitle());
              resultValuesMap.put("opaType", opaTitle.getOpaType());
              resultValuesMap.put("objectId", opaTitle.getObjectId());
              resultValuesMap.put("pageNum",
                  String.valueOf(opaTitle.getPageNum()));

              resultValuesMap.put("totalPages",
                  String.valueOf(opaTitle.getTotalPages()));
              resultValuesMap.put("addedTs",
                  TimestampUtils.getUtcString(opaTitle.getAddedTs()));

              resultsValueList.add(resultValuesMap);
            }

            resultsMap.put("total", totalCount);
            resultsMap.put(resultType, resultsValueList);
            requestParams.put("tagText", tagText);

            // Add request params to response
            headerParams.put("request", requestParams);

            // Set response header
            apiResponse.setResponse(responseObj, "header", headerParams);

            // Set response results
            apiResponse.setResponse(responseObj, resultsType, resultsMap);
          }
        }
      }

      if (!taggedTitlesValidator.getIsValid()) {

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
        errorValuesMap.put("@code", taggedTitlesValidator.getErrorCode()
            .toString());
        errorValuesMap.put("description", taggedTitlesValidator.getErrorCode()
            .getErrorMessage());
        errorValuesMap.put("username", userName);
        errorValuesMap.put("tagtext", tagText);
        errorValuesMap.put("title", title);
        errorValuesMap.put("format", format);
        errorValuesMap.put("pretty", pretty);
        requestParams.put("offset", offset);
        requestParams.put("rows", rows);

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
}
