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

@WebServlet("/ForbiddenAccess")
public class ForbiddenAccessHandler extends HttpServlet {
  /**
   * 
   */
  private static final long serialVersionUID = 121123242L;

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
            ArchitectureErrorMessageConstants.FORBIDDEN_ACCESS,
            ArchitectureErrorCodeConstants.FORBIDDEN, requestParameters,
            HttpStatus.FORBIDDEN, request,
            ArchitectureErrorMessageConstants.UNKOWN_ACTION);
    response.setStatus(HttpStatus.FORBIDDEN.value());
    response.getOutputStream().print(responseEntity.getBody());
  }
}
