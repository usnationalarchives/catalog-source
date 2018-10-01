package gov.nara.opa.architecture.web.controller;
 
import gov.nara.opa.architecture.exception.OpaApiResponseRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorMessageConstants;
import gov.nara.opa.architecture.web.validation.SimpleRequestParameters;
 
import java.io.IOException;
import java.util.UUID;
 
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;
 
/**
* @author aolaru
* @date Jun 3, 2014 This resolver will capture uncaught exceptions, log them
*       and then return to the API caller an error response that will look very
*       similar in structure to other error messages
* ticket date       contributor           comments
* 84300  2017-08-11 Matthew Mariano       Fixed logger init class to use correct class, DefaultHandlerExceptionResolver
*/
 
@Component
public class DefaultHandlerExceptionResolver extends
    AbstractHandlerExceptionResolver {
 
  static OpaLogger log = OpaLogger.getLogger(DefaultHandlerExceptionResolver.class);
 
  /**
   * This method will be called by the Spring framework to allow handling of
   * exceptions uncaught by the application code. The returned ModelView will be
   * null because the error json/xml will be written directly to the
   * HttpResponse body
   */
  @Override
  protected ModelAndView doResolveException(HttpServletRequest request,
      HttpServletResponse response, Object handler, Exception ex) {
 
    String errorId = UUID.randomUUID().toString();
    log.error(
        "Exception caught by the Default Exception Handler. Unique error id: "
            + errorId + ". " + ex.getMessage(), ex);
 
    // Set TRACE level in log configuration to register trace calls
    // log.trace("Exception caught by the Default Exception Handler", ex);
 
    SimpleRequestParameters requestParameters = new SimpleRequestParameters(
        request);
 
    String errorMessage = null;
    String errorCode = null;
    String action = null;
    HttpStatus httpStatus = null;
    if (ex instanceof OpaApiResponseRuntimeException) {
      OpaApiResponseRuntimeException apiException = (OpaApiResponseRuntimeException) ex;
      errorMessage = apiException.getErrorMessage();
      errorCode = apiException.getErrorCode();
      action = apiException.getAction();
      httpStatus = apiException.getHttpStatus();
    } else {
      errorMessage = "Internal error. exception="+ex+"Error id: " + errorId +" cause: "+ex.getCause();
      errorCode = ArchitectureErrorCodeConstants.INTERNAL_EXCEPTION;
      httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
      action = ArchitectureErrorMessageConstants.UNKOWN_ACTION;
    }
    ResponseEntity<String> responseEntity = AbstractBaseController
        .createErrorResponseEntity(errorMessage, errorCode, requestParameters,
            httpStatus, request, action);
    response.setStatus(responseEntity.getStatusCode().value());
    try {
      response.getOutputStream().print(responseEntity.getBody());
    } catch (IOException e) {
      log.error("Error fetching the response content", e);
    }
    return null;
  }
  /**
   * Gets the request paraments from the HtttServletRequest in a format that can
   * be used with the AspireObject
   *
   * @param request
   *          HttpServletRequest where parameters are retrieved from
   * @return Map of parameters names/values in a format useful for AspireObject
   *         population
   */
 
}