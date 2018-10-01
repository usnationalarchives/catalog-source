package gov.nara.opa.api.security;

import gov.nara.opa.api.services.HttpHeaderHelper;
import gov.nara.opa.architecture.exception.OpaApiResponseRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorMessageConstants;
import gov.nara.opa.architecture.web.validation.SimpleRequestParameters;
import gov.nara.opa.architecture.web.validation.URLFilter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class CrossOriginRequestSharingFilter implements Filter {
  private static OpaLogger logger = OpaLogger
      .getLogger(CrossOriginRequestSharingFilter.class);

  String validOrigins;

  @Override
  public void destroy() {
  }

  public static String VALID_METHODS = "DELETE, HEAD, GET, OPTION, POST, PUT";

  @Override
  public void doFilter(ServletRequest req, ServletResponse resp,
      FilterChain chain) throws ServletException, IOException {
    HttpServletRequest httpReq = (HttpServletRequest) req;
    HttpServletResponse httpResp = (HttpServletResponse) resp;

    try {
      String origin = httpReq.getHeader("Origin");
      HttpHeaderHelper.setHeader(httpReq, httpResp, "Access-Control-Allow-Origin", origin);
      HttpHeaderHelper.setHeader(httpReq, httpResp, "Access-Control-Allow-Methods", VALID_METHODS);      
      HttpHeaderHelper.setHeader(httpReq, httpResp, "Access-Control-Expose-Headers", "JSESSIONID");
      
      String headers = httpReq.getHeader("Access-Control-Request-Headers");
      if (headers != null)
        HttpHeaderHelper.setHeader(httpReq, httpResp, "Access-Control-Allow-Headers", headers);
      
      HttpHeaderHelper.setHeader(httpReq, httpResp, "Access-Control-Max-Age", "3600");
      HttpHeaderHelper.setHeader(httpReq, httpResp, "Access-Control-Allow-Credentials", "true");
      HttpHeaderHelper.setHeader(httpReq, httpResp, "Pragma", "no-cache");
      HttpHeaderHelper.setHeader(httpReq, httpResp, "Cache-Control", "no-store, no-cache");
      
      httpResp.setCharacterEncoding("UTF-8");
      httpResp.setContentType("text/plain;charset=utf-8");
      
      httpReq.setAttribute("method", "POST");
      
      // Validate for cross-site scripting
      URLFilter urlF = new URLFilter();
      boolean isVulnerable = urlF.isXssVulnarable(httpReq);
      if (isVulnerable) {
        SimpleRequestParameters requestParameters = new SimpleRequestParameters(
            httpReq, false);
        ResponseEntity<String> responseEntity = AbstractBaseController
            .createErrorResponseEntity(
                String.format(ArchitectureErrorMessageConstants.XSS_VULNERABLE),
                ArchitectureErrorCodeConstants.INVALID_PARAMETER,
                requestParameters, HttpStatus.BAD_REQUEST, httpReq, "");
        httpResp.setStatus(responseEntity.getStatusCode().value());
        resp.getOutputStream().print(responseEntity.getBody());
        return;
      }

      chain.doFilter(httpReq, httpResp);

    } catch (Exception ex) {
      if (ex instanceof OpaApiResponseRuntimeException) {
        respondWithOpaApiException((OpaApiResponseRuntimeException) ex,
            httpResp, httpReq);
        return;
      }
      logger.error(ex.getMessage(), ex);
    }
  }

  private void respondWithOpaApiException(OpaApiResponseRuntimeException ex,
      HttpServletResponse response, HttpServletRequest request) {
    SimpleRequestParameters requestParameters = new SimpleRequestParameters(
        request);
    String errorMessage = ex.getErrorMessage();
    String errorCode = ex.getErrorCode();
    String action = ex.getAction();
    HttpStatus httpStatus = ex.getHttpStatus();
    ResponseEntity<String> responseEntity = AbstractBaseController
        .createErrorResponseEntity(errorMessage, errorCode, requestParameters,
            httpStatus, request, action);
    response.setStatus(responseEntity.getStatusCode().value());
    try {
      response.getOutputStream().print(responseEntity.getBody());
    } catch (IOException e) {
      logger.error("Error fetching the response content", e);
    }
  }

  @Override
  public void init(FilterConfig config) throws ServletException {
    validOrigins = config.getInitParameter("validOrigins");
  }

}
