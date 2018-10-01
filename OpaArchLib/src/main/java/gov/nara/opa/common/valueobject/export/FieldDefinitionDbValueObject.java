package gov.nara.opa.common.valueobject.export;

import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class FieldDefinitionDbValueObject extends AbstractWebEntityValueObject {

  private Integer fieldId;
  private String resultType;
  private String fieldName;
  private String fieldSource;
  private Integer orderNumber;
  private String fieldLabel;
  private Integer orderNumberBrief;
  private String fieldLabelBrief;
  private String fieldValueInstruction;
  private String section;
  private String fieldType;

  @Override
  public Map<String, Object> getDatabaseContent() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    // TODO Auto-generated method stub
    return null;
  }

  public Integer getFieldId() {
    return fieldId;
  }

  public void setFieldId(Integer fieldId) {
    this.fieldId = fieldId;
  }

  public String getResultType() {
    return resultType;
  }

  public void setResultType(String resultType) {
    this.resultType = resultType;
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public String getFieldSource() {
    return fieldSource;
  }

  public void setFieldSource(String fieldSource) {
    this.fieldSource = fieldSource;
  }

  public Integer getOrderNumber() {
    return orderNumber;
  }

  public void setOrderNumber(Integer orderNumber) {
    this.orderNumber = orderNumber;
  }

  public String getFieldLabel() {
    return fieldLabel;
  }

  public void setFieldLabel(String fieldLabel) {
    this.fieldLabel = fieldLabel;
  }

  public Integer getOrderNumberBrief() {
    return orderNumberBrief;
  }

  public void setOrderNumberBrief(Integer orderNumberBrief) {
    this.orderNumberBrief = orderNumberBrief;
  }

  public String getFieldLabelBrief() {
    return fieldLabelBrief;
  }

  public void setFieldLabelBrief(String fieldLabelBrief) {
    this.fieldLabelBrief = fieldLabelBrief;
  }

  public String getFieldValueInstruction() {
    return fieldValueInstruction;
  }

  public void setFieldValueInstruction(String fieldValueInstruction) {
    this.fieldValueInstruction = fieldValueInstruction;
  }

  public String getSection() {
    return section;
  }

  public void setSection(String section) {
    this.section = section;
  }

  public String getFieldType() {
    return fieldType;
  }

  public void setFieldType(String fieldType) {
    this.fieldType = fieldType;
  }

}
