package gov.nara.opa.architecture.utils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.searchtechnologies.aspire.services.AspireObject;

public class DASField {

  Map<String, Set<String>> childrenByElement = new ConcurrentHashMap<String, Set<String>>();

  AspireObject content;
  String name;

  public AspireObject getContent() {
    return content;
  }

  public void setContent(AspireObject content) {
    this.content = content;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Set<String> getChildFields(String elementName) {
    if (elementName == null) {
      return childrenByElement.get(getName());
    }
    return childrenByElement.get(elementName);
  }

  public void setChildFields(Set<String> childFields, String elementName) {
    if (elementName == null) {
      childrenByElement.put(getName(), childFields);
    } else {
      childrenByElement.put(elementName, childFields);
    }
  }

  public void clearChidlFields() {
    childrenByElement = new ConcurrentHashMap<String, Set<String>>();
  }

}
