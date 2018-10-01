package gov.nara.opa.architecture.web.validation.annotation;

import java.util.Map;

import javax.servlet.ServletRequest;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.web.servlet.mvc.method.annotation.ExtendedServletRequestDataBinder;

public class HttpParameterNameDataBinder extends ExtendedServletRequestDataBinder {
  private final Map<String, String> renameMapping;

  public HttpParameterNameDataBinder(Object target, String objectName,
      Map<String, String> renameMapping) {
    super(target, objectName);
    this.renameMapping = renameMapping;
  }

  @Override
  protected void addBindValues(MutablePropertyValues mpvs,
      ServletRequest request) {
    super.addBindValues(mpvs, request);
    for (Map.Entry<String, String> entry : renameMapping.entrySet()) {
      String from = entry.getKey();
      String to = entry.getValue();
      if (mpvs.contains(from)) {
        mpvs.add(to, mpvs.getPropertyValue(from).getValue());
      }
    }
  }
}
