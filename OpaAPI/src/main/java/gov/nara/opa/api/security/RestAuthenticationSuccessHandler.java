package gov.nara.opa.api.security;

import gov.nara.opa.api.dataaccess.user.UserAccountDao;
import gov.nara.opa.api.response.APIResponse;
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.user.accounts.UserAccount;
import gov.nara.opa.api.utils.PathUtils;
import gov.nara.opa.architecture.utils.TimestampUtils;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.LinkedHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

public class RestAuthenticationSuccessHandler extends
    SimpleUrlAuthenticationSuccessHandler {

  @Value("${configFilePath}")
  private String configFilePath;

  @Autowired
  private UserAccountDao userAccountDao;

  @Autowired
  APIResponse apiResponse;

  @Autowired
  ConfigurationService configService;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request,
      HttpServletResponse response, Authentication authentication)
      throws ServletException, IOException {
    logger.trace("RestAuthenticationSuccessHandler");
    response.setStatus(HttpServletResponse.SC_OK);
    clearAuthenticationAttributes(request);

    HttpHeaders responseHeaders = new HttpHeaders();
    String action = "login";
    String resultType = "user";
    String requestPath = PathUtils.getServeletPath(request);
    String responseMessage = "";
    String format = "json";
    boolean pretty = true;

    // Set response status
    HttpStatus status = HttpStatus.OK;

    // Assign format value
    if (request.getParameter("format") != null
        && request.getParameter("format").equals("xml")) {
      format = "xml";
    }

    // Assign pretty print value
    if (request.getParameter("pretty") != null
        && request.getParameter("pretty").equals("false")) {
      pretty = false;
    }

    try {
      // Build the Aspire OPA response object
      AspireObject responseObj = new AspireObject("opaResponse");

      // If the config.xml file returns successfully
      File file = new File(configFilePath);

      // If the config.xml file returns successfully
      if (file.exists()) {

        // Set response status
        response.setStatus(HttpServletResponse.SC_OK);

        // Get the session ID
        HttpSession session = request.getSession();
        String sessionId = session.getId();

        // Set the session ID in the response headers
        responseHeaders.set("JSESSIONID", sessionId);

        UserAccount userAccount = (UserAccount) authentication.getDetails();

        // Set header params
        LinkedHashMap<String, Object> headerParams = new LinkedHashMap<String, Object>();
        headerParams.put("@status", String.valueOf(status));
        headerParams.put("time", TimestampUtils.getUtcTimestampString());

        // Set request params
        LinkedHashMap<String, Object> requestParams = new LinkedHashMap<String, Object>();
        requestParams.put("@path", requestPath);
        requestParams.put("action", action);
        requestParams.put("userName", userAccount.getUserName());
        requestParams.put("format", format);
        requestParams.put("pretty", pretty);
        headerParams.put("request", requestParams);

        // Set the result information
        UserAccountValueObject user = userAccountDao
            .selectByUserName(userAccount.getUserName());
        LinkedHashMap<String, Object> resultValues = user
            .getAspireObjectContent("view");

        getEnvironmentSettings(userAccount.getAccountType(),
            userAccount.getAccountRights(), resultValues);

        // Set response header
        apiResponse.setResponse(responseObj, "header", headerParams);

        responseObj.add("credentials", request.getSession().getId());
        // Set response results
        apiResponse.setResponse(responseObj, resultType, resultValues);

        // update last_action_ts with NOW jdh 2016-09-01
        user.setLastActionTS(new Timestamp(System.currentTimeMillis()));
        userAccountDao.update(user);
      } else {
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
        errorParams.put("@code",
            ArchitectureErrorCodeConstants.CONFIG_FILE_NOT_FOUND.toString());
        errorParams.put("description", ErrorConstants.CONFIG_FILE_NOT_FOUND);
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

      // Close the Aspire OPA response object
      responseObj.close();

      // Write json to the response body
      response.getWriter().write(responseMessage);
      response.getWriter().flush();
      response.getWriter().close();
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      // throw new OpaRuntimeException(e);
    }
  }

  private LinkedHashMap<String, Object> getEnvironmentSettings(String userType,
      String userRights, LinkedHashMap<String, Object> results) {

    results.put("timeout", configService.getConfig().getSessionTimeout());

    int maxSearchRecords = 0;
    if (userType.equals("standard")) {
      maxSearchRecords = configService.getConfig()
          .getMaxSearchResultsStandard();
    } else {
      maxSearchRecords = configService.getConfig().getMaxSearchResultsPower();
    }

    results.put("searchMaxRecords", maxSearchRecords);
    
    //Maximum allowed for printing
    int maxPrintResults = configService.getConfig().getMaxPrintResults();
    results.put("printMaxRecords", maxPrintResults);

    return results;
  }

}
