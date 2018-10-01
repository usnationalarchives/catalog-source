package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import java.nio.file.Path;

/**
 * Represents an area within Opa Storage for a given NAID, which be either one
 * of the following: live, quarantine, pre-ingestion or future.
 * @author caraya
 */
public class OpaStorageArea extends OpaStorageDirectory {
  static final String CONTENT = "content";
  static final String OPA_RENDITIONS = "opa-renditions";
  static final String EXTRACTED_TEXT = "extracted-text";
  static final String IMAGE_TILES = "image-tiles";
  static final String TECHNICAL_METADATA = "technical-metadata";
  static final String THUMBNAILS = "thumbnails";
  
  private OpaStorageDirectory contentDir;
  private OpaStorageDirectory opaRenditionsDir;
  private OpaStorageDirectory extractedTextDir;
  private OpaStorageDirectory imageTilesDir;
  private OpaStorageDirectory technicalMetadataDir;
  private OpaStorageDirectory thumbnailsDir;
  
  public OpaStorageArea(Path dir){
    super(dir);    
  }

  public OpaStorageDirectory getContentDir() throws AspireException {
    return contentDir != null ? contentDir : (contentDir = createContentDir());
  }
  
  private OpaStorageDirectory createContentDir() throws AspireException{
    return createOpaStorageDirectory(getDir().resolve(CONTENT));
  }

  public OpaStorageDirectory getOpaRenditionsDir() throws AspireException {
    return opaRenditionsDir != null ? opaRenditionsDir : (opaRenditionsDir = createOpaRenditionsDir());
  }
  
  private OpaStorageDirectory createOpaRenditionsDir() throws AspireException{
    return createOpaStorageDirectory(getDir().resolve(OPA_RENDITIONS));
  }

  public OpaStorageDirectory getExtractedTextDir() throws AspireException {
    return extractedTextDir != null ? extractedTextDir : (extractedTextDir = createExtractedTextDir());
  }
  
  private OpaStorageDirectory createExtractedTextDir() throws AspireException{
    return createOpaStorageDirectory(getOpaRenditionsDir().resolveAsPath(EXTRACTED_TEXT));
  }

  public OpaStorageDirectory getImageTilesDir() throws AspireException {
    return imageTilesDir != null ? imageTilesDir : (imageTilesDir = createImageTilesDir());
  }
  
  private OpaStorageDirectory createImageTilesDir() throws AspireException{
    return createOpaStorageDirectory(getOpaRenditionsDir().resolveAsPath(IMAGE_TILES));
  }

  public OpaStorageDirectory getTechnicalMetadataDir() throws AspireException {
    return technicalMetadataDir != null ? technicalMetadataDir : (technicalMetadataDir = createTechnicalMetadataDir());
  }
  
  private OpaStorageDirectory createTechnicalMetadataDir() throws AspireException{
    return createOpaStorageDirectory(getOpaRenditionsDir().resolveAsPath(TECHNICAL_METADATA));
  }

  public OpaStorageDirectory getThumbnailsDir() throws AspireException {
    return thumbnailsDir != null ? thumbnailsDir : (thumbnailsDir = createThumbnailsDir());
  }
  
  private OpaStorageDirectory createThumbnailsDir() throws AspireException{
    return createOpaStorageDirectory(getOpaRenditionsDir().resolveAsPath(THUMBNAILS));
  }
  
  private OpaStorageDirectory createOpaStorageDirectory(Path dir){
    ensureDirectoryExists(dir);
    return new OpaStorageDirectory(dir);
  }

  private void ensureDirectoryExists(Path dir) {
    dir.toFile().mkdir();
  }
}
