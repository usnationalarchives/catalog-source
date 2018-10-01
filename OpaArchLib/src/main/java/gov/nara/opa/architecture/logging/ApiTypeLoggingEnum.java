package gov.nara.opa.architecture.logging;

import gov.nara.opa.architecture.web.valueobject.CommonValueObjectConstants;

public enum ApiTypeLoggingEnum {

  API_TYPE_PUBLIC("API"), API_TYPE_INTERNAL("WebApp");

  private String value;

  ApiTypeLoggingEnum(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }

  public static ApiTypeLoggingEnum toApiTypeLoggingEnum(String urlApiType) {
    if (CommonValueObjectConstants.PUBLIC_API_PATH.equals(urlApiType)) {
      return ApiTypeLoggingEnum.API_TYPE_PUBLIC;
    } else if (CommonValueObjectConstants.INTERNAL_API_PATH.equals(urlApiType)) {
      return ApiTypeLoggingEnum.API_TYPE_INTERNAL;
    }
    return null;
  }
}
