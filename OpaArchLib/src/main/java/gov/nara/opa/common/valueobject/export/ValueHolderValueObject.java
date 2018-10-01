package gov.nara.opa.common.valueobject.export;

import java.util.LinkedList;

public class ValueHolderValueObject {

  private String name;
  private Object value;
  private String section;
  private String type;
  private String label;
  private LinkedList<ValueHolderValueObject> arrayOfObjectsValue;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object object) {
    this.value = object;
  }

  public String getSection() {
    return section;
  }

  public void setSection(String section) {
    this.section = section;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public LinkedList<ValueHolderValueObject> getArrayOfObjectsValue() {
    return arrayOfObjectsValue;
  }

  public void setArrayOfObjectsValue(
      LinkedList<ValueHolderValueObject> arrayOfObjectsValue) {
    this.arrayOfObjectsValue = arrayOfObjectsValue;
  }

  public Class<? extends Object> getObjectType() {
    return value.getClass();
  }
  
  @Override
  public String toString() {
    return String.format("name:%1$s,value:%2$s,section:%3$s,type:%4$s,label:%5$s,objects:[%6$s]", name, (value != null ? value.toString() : null), section, type, label, (arrayOfObjectsValue != null ? arrayOfObjectsValue.toString() : null));
  }
}
