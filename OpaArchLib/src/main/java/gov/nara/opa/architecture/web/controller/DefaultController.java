package gov.nara.opa.architecture.web.controller;

import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorMessageConstants;
import gov.nara.opa.architecture.web.validation.SimpleRequestParameters;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author gsofizade
 * @date Apr 3, 2014
 * 
 */

// @Controller
public class DefaultController {

  /**
   * Default controller that exists to return a proper REST response for
   * unmapped requests.
   */
  @RequestMapping("/**")
  public ResponseEntity<String> unmappedRequest(HttpServletRequest request,
      HttpStatus status) throws IOException {
    SimpleRequestParameters requestParameters = new SimpleRequestParameters(
        request);

    ResponseEntity<String> responseEntity = AbstractBaseController
        .createErrorResponseEntity(
            ArchitectureErrorMessageConstants.NO_ASSOCIATED_RESOURCE,
            ArchitectureErrorCodeConstants.NO_RESOURCE_PATH, requestParameters,
            HttpStatus.BAD_REQUEST, request,
            ArchitectureErrorMessageConstants.UNKOWN_ACTION);
    return responseEntity;

  }
}
