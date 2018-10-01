package gov.nara.opa.ingestion;

public interface IngestionDb {
  void removeRecord(Integer naId);
  
  String getFirstIngestDate(Integer naId);
  void setFirstIngestDate(Integer naId, String date);
  
  String getMD5(Integer naid);
  void setMD5(Integer naid, String md5);
}
