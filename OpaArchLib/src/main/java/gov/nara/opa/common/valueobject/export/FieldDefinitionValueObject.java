package gov.nara.opa.common.valueobject.export;

import gov.nara.opa.common.services.docstransforms.impl.normalize.AbstractValueExtractor;

public class FieldDefinitionValueObject {

  private Integer id;
  private String name;
  private String label;
  private String source;
  private String section;
  private String type;
  private String valueGenerationInstruction;
  private AbstractValueExtractor valueExtractor;

  public String getValueGenerationInstruction() {
    return valueGenerationInstruction;
  }

  public void setValueGenerationInstruction(String valueGenerationInstruction) {
    this.valueGenerationInstruction = valueGenerationInstruction;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
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

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public AbstractValueExtractor getValueExtractor() {
    return valueExtractor;
  }

  public void setValueExtractor(AbstractValueExtractor valueExtractor) {
    this.valueExtractor = valueExtractor;
  }
}
