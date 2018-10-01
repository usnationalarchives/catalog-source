package gov.nara.opa.server.export.valueobject;

import org.apache.solr.common.SolrDocument;

public class SolrDocumentUnitOfWork {

  private Integer searcherId;
  private Integer documentIndex;
  private Integer batchIndex;
  private SolrDocument document;
  private Integer totalRecords;

  private String contentToWrite;

  public SolrDocumentUnitOfWork(SolrDocument document, int searcherId,
      int documentIndex, int batchIndex, int totalRecords) {
    this.document = document;
    this.searcherId = searcherId;
    this.documentIndex = documentIndex;
    this.batchIndex = batchIndex;
    this.totalRecords = totalRecords;
  }

  public Integer getSearcherId() {
    return searcherId;
  }

  public Integer getDocumentIndex() {
    return documentIndex;
  }

  public Integer getBatchIndex() {
    return batchIndex;
  }

  public SolrDocument getDocument() {
    return document;
  }

  public String getContentToWrite() {
    return contentToWrite;
  }

  public void setContentToWrite(String contentToWrite) {
    this.contentToWrite = contentToWrite;
  }

  public Integer getTotalRecords() {
    return totalRecords;
  }

  @Override
  public String toString() {
    return String.format(
        "searcherId=%1$s, batchIndex=%2$s, documentIndex=%3$s",
        getSearcherId(), getBatchIndex(), getDocumentIndex());
  }
}
