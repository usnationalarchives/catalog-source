package gov.nara.opa.api.controller.user.contributions;

import gov.nara.opa.api.annotation.locks.AnnotationLock;
import gov.nara.opa.api.annotation.transcriptions.Transcription;
import gov.nara.opa.api.annotation.transcriptions.TranscriptionResponseValuesHelper;
import gov.nara.opa.api.dataaccess.user.UserAccountDao;
import gov.nara.opa.api.response.APIResponse;
import gov.nara.opa.api.services.ServiceResponseObject;
import gov.nara.opa.api.services.annotation.tags.ViewTagService;
import gov.nara.opa.api.services.annotation.transcriptions.ViewTranscriptionService;
import gov.nara.opa.api.services.user.contributions.UserContributionsService;
import gov.nara.opa.api.services.user.contributions.ViewOpaTitleService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.user.accounts.UserAccount;
import gov.nara.opa.api.user.contributions.UserContributionsErrorCode;
import gov.nara.opa.api.utils.PathUtils;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.api.validation.user.contributions.UserContributionsValidator;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.utils.TimestampUtils;
import gov.nara.opa.common.valueobject.annotation.tags.OpaTitle;
import gov.nara.opa.common.valueobject.annotation.tags.TagValueObject;
import gov.nara.opa.common.valueobject.annotation.tags.TagsCollectionValueObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
public class ViewContributionsByNaidsController {

  private static OpaLogger logger = OpaLogger
      .getLogger(ViewContributionsByNaidsController.class);

  @Autowired
  private APIResponse apiResponse;

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

  @SuppressWarnings("unchecked")
  @RequestMapping(value = {
      "/" + Constants.PUBLIC_API_PATH + "/" + Constants.API_VERS_NUM
          + "/contributions/contributionsbynaids",
      "/" + Constants.INTERNAL_API_PATH + "/" + Constants.API_VERS_NUM
          + "/contributions/contributionsbynaids" }, method = RequestMethod.GET)
  public ResponseEntity<String> ViewContributionsByNaids(
      HttpServletRequest request,
      @RequestParam(value = "naids", required = false, defaultValue = "") String naIds,
      @RequestParam(value = "rows", required = false, defaultValue = "25") int rows,
      @RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
      @RequestParam(value = "format", required = false, defaultValue = "json") String format,
      @RequestParam(value = "pretty", required = false, defaultValue = "true") boolean pretty) {

    String message = "";
    HttpStatus status = HttpStatus.OK;
    String action = "viewContributionsByNaids";
    String resultsType = "Results";

    // Build the Aspire OPA response object
    AspireObject responseObj = new AspireObject("opaResponse");

    // Get the request path
    String requestPath = PathUtils.getServeletPath(request);

    // Set request params
    LinkedHashMap<String, Object> requestParams = new LinkedHashMap<String, Object>();
    requestParams.put("@path", requestPath);
    requestParams.put("naids", naIds);
    requestParams.put("action", action);
    requestParams.put("rows", rows);
    requestParams.put("offset", offset);
    requestParams.put("format", format);
    requestParams.put("pretty", pretty);

    try {

      // Validate parameters
      String[] paramNamesStringArray = { "naids", "rows", "offset",
          "format", "pretty" };
      LinkedHashMap<String, String> validRequestParameterNames = StringUtils
          .convertStringArrayToLinkedHashMap(paramNamesStringArray);
      if (!ValidationUtils.validateRequestParameterNames(
          validRequestParameterNames, request.getQueryString())) {
        status = HttpStatus.BAD_REQUEST;
        userContributionsValidator.setStandardError(UserContributionsErrorCode.INVALID_PARAMETER);
        userContributionsValidator.setMessage(ErrorConstants.invalidParameterName);
        userContributionsValidator.setIsValid(false);
      } else {
        userContributionsValidator.validateParameters(requestParams);
      }
      
      if (userContributionsValidator.getIsValid()) {

        String[] naIdsList = naIds.split(",");

        // Retrieve the collection of titles belonging to specified tag
        List<OpaTitle> opaTitles = viewOpaTitleService
            .getTitlesByNaIds(naIdsList);

        if (opaTitles == null || opaTitles.size() == 0) {
          status = HttpStatus.NOT_FOUND;
          userContributionsValidator
              .setStandardError(UserContributionsErrorCode.TITLES_NOT_FOUND);
        } else {

          TagsCollectionValueObject tags = viewTagService
              .getTagsByNaIds(naIdsList);

          ServiceResponseObject transcriptionsResponseObject = viewTranscriptionService
              .selectByNaIds(naIdsList);
          HashMap<String, Object> results = (HashMap<String, Object>) transcriptionsResponseObject
              .getContentMap();
          List<Transcription> transcriptions = (List<Transcription>) results
              .get("Transcriptions");

          // Set header params
          LinkedHashMap<String, Object> headerParams = new LinkedHashMap<String, Object>();
          headerParams.put("@status", String.valueOf(status));
          headerParams.put("time", TimestampUtils.getUtcTimestampString());

          // Add all the info found to the response object
          LinkedHashMap<String, Object> resultsMap = new LinkedHashMap<String, Object>();

          ArrayList<LinkedHashMap<String, Object>> resultsValueList = new ArrayList<LinkedHashMap<String, Object>>();

          for (OpaTitle opaTitle : opaTitles) {

            LinkedHashMap<String, Object> resultValuesMap = new LinkedHashMap<String, Object>();
            LinkedHashMap<String, Object> publicValuesMap = new LinkedHashMap<String, Object>();

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

            // Adding Tags:
            ArrayList<LinkedHashMap<String, Object>> tagsResultsValueList = new ArrayList<LinkedHashMap<String, Object>>();
            for (TagValueObject tag : tags.getTags()) {
              if (tag.getNaId().equals(opaTitle.getNaId())) {
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
            if (tagsResultsValueList.size() > 0)
              publicValuesMap.put("tags", tagsResultsValueList);

            // Adding transcriptions:
            ArrayList<LinkedHashMap<String, Object>> transcriptionsResultsValueList = new ArrayList<LinkedHashMap<String, Object>>();
            for (Transcription transcription : transcriptions) {
              if (transcription.getNaId().equals(opaTitle.getNaId())) {
                // Service call
                ServiceResponseObject responseObject = viewTranscriptionService
                    .getFullTranscription(transcription.getNaId(),
                        transcription.getObjectId());

                HashMap<String, Object> transcriptionResults = (HashMap<String, Object>) responseObject
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
                responseValueHelper
                    .setPreviousTranscriptions(previousTranscriptions);

                transcriptionsResultsValueList.add(responseValueHelper
                    .getResponseValues());
              }
            }
            if (transcriptionsResultsValueList.size() > 0)
              publicValuesMap.put("transcriptions",
                  transcriptionsResultsValueList);

            resultValuesMap.put("PublicContributions", publicValuesMap);

            resultsValueList.add(resultValuesMap);
          }

          resultsMap.put("total", opaTitles.size());
          resultsMap.put("Result", resultsValueList);

          // Add request params to response
          headerParams.put("request", requestParams);

          // Set response header
          apiResponse.setResponse(responseObj, "header", headerParams);

          // Set response results
          apiResponse.setResponse(responseObj, resultsType, resultsMap);
        }
      }
      if (!userContributionsValidator.getIsValid()) {

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
        errorValuesMap.put("@code", userContributionsValidator.getErrorCode()
            .toString());
        errorValuesMap.put("description", userContributionsValidator
            .getErrorCode().getErrorMessage());
        errorValuesMap.put("@path", requestPath);
        errorValuesMap.put("action", action);
        errorValuesMap.put("rows", rows);
        errorValuesMap.put("offset", offset);
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
}
