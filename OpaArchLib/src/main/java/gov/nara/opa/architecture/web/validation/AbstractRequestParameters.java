package gov.nara.opa.architecture.web.validation;

import gov.nara.opa.architecture.web.validation.constraint.OpaPattern;
import gov.nara.opa.architecture.web.valueobject.AbstractWebValueObject;

import java.util.LinkedHashMap;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Abstract superclass from which all Request POJOs should extend. It provides a
 * blueprint for some methods that will be reused during processing as well data
 * holders for the common format and pretty printing parameters. Property values
 * will be injected by the Spring framework while binding http request
 * parameters
 * 
 * @author aolaru
 * @date Jun 4, 2014
 * 
 */
public abstract class AbstractRequestParameters extends AbstractWebValueObject {

  public static final String PARAM_NAME_FORMAT = "format";
  public static final String PARAM_NAME_PRETTY = "pretty";

  public static final String JSON_FORMAT = "json";
  public static final String XML_FORMAT = "xml";

  public static final String INTERNAL_API_TYPE = "iapi";
  public static final String PUBLIC_API_TYPE = "api";

  @OpaPattern(regexp = "(^" + JSON_FORMAT + "$)|(^" + XML_FORMAT + "$)", message = ArchitectureErrorMessageConstants.INVALID_FORMAT)
  private String format = JSON_FORMAT;

  protected LinkedHashMap<String, Object> requestParams = new LinkedHashMap<String, Object>();

  private Boolean pretty = true;

  @OpaPattern(regexp = "(^" + INTERNAL_API_TYPE + "$)|(^" + PUBLIC_API_TYPE
      + "$)", errorCode = ArchitectureErrorCodeConstants.INVALID_API_TYPE, message = ArchitectureErrorMessageConstants.INVALID_API_TYPE)
  protected String apiType;

  private String httpSessionId;

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public Boolean isPretty() {
    return pretty;
  }

  public void setPretty(Boolean pretty) {
    this.pretty = pretty;
  }

  public String getHttpSessionId() {
    return httpSessionId;
  }

  public void setHttpSessionId(String httpSessionId) {
    this.httpSessionId = httpSessionId;
  }

  public String getApiType() {
    return apiType;
  }

  public void setApiType(String apiType) {
    this.apiType = apiType;
  }

  /**
   * Method to be called by subclasses which will initialize the parameters map
   * with the format and pretty printing flag
   */
  protected void initRequestParamsMap() {
    requestParams.clear();
    requestParams.put(PARAM_NAME_FORMAT, getFormat());
    requestParams.put(PARAM_NAME_PRETTY, isPretty());
  }

  @JsonIgnore
  public boolean isPublicApi() {
    return PUBLIC_API_TYPE.equals(getApiType());
  }

  protected void addParameterToMapIfNotNull(String parameterName,
      Object parameterValue) {
    if (parameterValue != null) {
      requestParams.put(parameterName, parameterValue);
    }
  }

  protected void addParameterToMap(String parameterName, Object parameterValue) {
    requestParams.put(parameterName, parameterValue);
  }

  public boolean bypassExtraneousHttpParametersValidation() {
    return false;
  }

  public boolean isInWhiteList(String parameterName) {
    return false;
  }

}
