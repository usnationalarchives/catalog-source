package gov.nara.opa.api.validation.administrator;

import gov.nara.opa.api.valueobject.administrator.AccountReasonValueObjectConstants;
import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;
import gov.nara.opa.architecture.web.validation.constraint.OpaSize;

import java.util.LinkedHashMap;

public class AddAccountReasonRequestParameters extends
    AbstractRequestParameters implements AccountReasonValueObjectConstants {

  @OpaNotNullAndNotEmpty
  @OpaSize(min = 2, max = 50)
  private String text;

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    initRequestParamsMap();
    if (getText() != null) {
      requestParams.put(TEXT_REQ_ASP, getText());
    }
    return requestParams;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

}
