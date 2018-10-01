package gov.nara.opa.architecture.web.controller;

import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorMessageConstants;
import gov.nara.opa.architecture.web.validation.SimpleRequestParameters;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@WebServlet("/ResourceNotFound")
public class ResourceNotFoundHandler extends HttpServlet {
  /**
   * 
   */
  private static final long serialVersionUID = 121123241L;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    processError(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    processError(request, response);
  }

  @Override
  protected void doDelete(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    processError(request, response);
  }

  @Override
  protected void doPut(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    processError(request, response);
  }

  private void processError(HttpServletRequest request,
      HttpServletResponse response) throws IOException {
    SimpleRequestParameters requestParameters = new SimpleRequestParameters(
        request);

    ResponseEntity<String> responseEntity = AbstractBaseController
        .createErrorResponseEntity(
            ArchitectureErrorMessageConstants.NO_ASSOCIATED_RESOURCE,
            ArchitectureErrorCodeConstants.NO_RESOURCE_PATH, requestParameters,
            HttpStatus.NOT_FOUND, request,
            ArchitectureErrorMessageConstants.UNKOWN_ACTION);
    response.setStatus(HttpStatus.NOT_FOUND.value());
    response.getOutputStream().print(responseEntity.getBody());
  }
}
