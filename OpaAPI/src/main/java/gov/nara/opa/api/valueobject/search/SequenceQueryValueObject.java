package gov.nara.opa.api.valueobject.search;

public class SequenceQueryValueObject {
  private String sequenceQuery;
  private int rows;
  private int originalOffset;
  private int offset;
  
  public String getSequenceQuery() {
    return sequenceQuery;
  }
  public void setSequenceQuery(String sequenceQuery) {
    this.sequenceQuery = sequenceQuery;
  }
  public int getRows() {
    return rows;
  }
  public void setRows(int rows) {
    this.rows = rows;
  }
  public int getOriginalOffset() {
    return originalOffset;
  }
  public void setOriginalOffset(int originalOffset) {
    this.originalOffset = originalOffset;
  }
  public int getOffset() {
    return offset;
  }
  public void setOffset(int offset) {
    this.offset = offset;
  }
  
  
}
