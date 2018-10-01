package gov.nara.opa.api.services.content;

public interface ImageTileRetrievalService {
  String getImageTilesFilePath(String naId, String objectId);
  
  void deleteCompressedFile(String filePath);
}
