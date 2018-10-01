package gov.nara.opa.architecture.web.valueobject;

import java.util.LinkedHashMap;
import java.util.Map;

public class SimpleWebEntityValueObject extends AbstractWebEntityValueObject {

  LinkedHashMap<String, Object> aspireContent;
  Map<String, Object> databaseContent;

  public SimpleWebEntityValueObject(LinkedHashMap<String, Object> aspireContent) {
    this(aspireContent, null);
  }

  public SimpleWebEntityValueObject(
      LinkedHashMap<String, Object> aspireContent,
      Map<String, Object> databaseContent) {
    this.aspireContent = aspireContent;
    this.databaseContent = databaseContent;
  }

  @Override
  public Map<String, Object> getDatabaseContent() {
    return databaseContent;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    return aspireContent;
  }

}
