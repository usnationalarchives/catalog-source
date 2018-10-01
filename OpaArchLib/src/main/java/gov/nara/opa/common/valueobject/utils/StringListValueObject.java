package gov.nara.opa.common.valueobject.utils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

public class StringListValueObject extends AbstractWebEntityValueObject {

  private String entityName;
  private List<String> values;
  
  public StringListValueObject(String entityName, List<String> stringList) {
    this.entityName = entityName;
    this.values = stringList;
  }
  
  public String getEntityName() {
    return entityName;
  }

  public void setEntityName(String entityName) {
    this.entityName = entityName;
  }

  public List<String> getValues() {
    return values;
  }

  public void setValues(List<String> values) {
    this.values = values;
  }

  @Override
  public Map<String, Object> getDatabaseContent() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
    
    result.put(entityName, values);
    
    return result;
  }

}
