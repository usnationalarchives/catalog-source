package gov.nara.opa.architecture.web.validation;

import java.util.Enumeration;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;

/**
 * Simple class that will extract parameters from an http servler request and
 * put them in a LinkedMap. The format and pretty parameter will be put first on
 * the map. If the format/pretty params don't exist or can't be mapped to their
 * appropriate values they will be mapped to their default values
 * 
 * @author aolaru
 * @data Jun 17, 2014
 */
public class SimpleRequestParameters extends AbstractRequestParameters {

  private LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();

  public SimpleRequestParameters(HttpServletRequest request) {
    setFormatParamValue(request);
    setPrettyParamValue(request);
    parameters = getRequestParameters(request);
  }

  public SimpleRequestParameters(HttpServletRequest request,
      boolean addRequestParams) {
    setFormatParamValue(request);
    setPrettyParamValue(request);
    if (addRequestParams) {
      parameters = getRequestParameters(request);
    }
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    initRequestParamsMap();
    requestParams.putAll(parameters);
    return requestParams;
  }

  private void setFormatParamValue(HttpServletRequest request) {
    String format = request
        .getParameter(AbstractRequestParameters.PARAM_NAME_FORMAT);
    if (format == null
        || !((format.equals(AbstractRequestParameters.JSON_FORMAT) || format
            .equals(AbstractRequestParameters.XML_FORMAT)))) {
      format = AbstractRequestParameters.JSON_FORMAT;
    }
    setFormat(format);
  }

  private void setPrettyParamValue(HttpServletRequest request) {
    boolean pretty = true;
    String prettyString = request
        .getParameter(AbstractRequestParameters.PARAM_NAME_PRETTY);
    if (prettyString == null) {
      pretty = true;
    } else if (prettyString.toLowerCase().equals("true")) {
      pretty = true;
    } else if (prettyString.toLowerCase().equals("false")) {
      pretty = false;
    }
    setPretty(pretty);
    return;
  }

  private LinkedHashMap<String, Object> getRequestParameters(
      HttpServletRequest request) {

    LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();

    Enumeration<String> requestParamsNames = request.getParameterNames();
    if (requestParamsNames == null) {
      return parameters;
    }
    while (requestParamsNames.hasMoreElements()) {
      String requestParamName = requestParamsNames.nextElement();
      if (requestParamName.equals(AbstractRequestParameters.PARAM_NAME_PRETTY)
          || requestParamName
              .equals(AbstractRequestParameters.PARAM_NAME_FORMAT)
          || requestParamName.equals("password")) {
        continue;
      }
      String[] values = request.getParameterValues(requestParamName);
      String value = null;
      if (values != null && values.length > 0) {
        if (values.length == 1) {
          value = values[0];
        } else {
          value = values.toString();
        }
      }
      parameters.put(requestParamName, value);
    }

    return parameters;
  }

}
