package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * OPA Storage holds OPA Information Packages (OPA-IPs) that contain and 
 * structure digital objects (media files) associated with some ADs.
 * 
 * OPA Storage is divided into areas, each of which holds OPA-IPs. 
 * 
 * The areas are: 
 * 
 *  live – The live area contains OPA-IPs that have been processed and are 
 *  available to the front end for serving images and other data they contain.
 * 
 *  quarantine – The quarantine area contains OPA-IPs that have had something 
 *  wrong detected in them and need to be separated out so they are not served. 
 *  Packages in this area will need to be inspected, repaired, and moved to 
 *  another area by the OPA System Administrator.
 * 
 *  pre-ingestion – The pre-ingestion area contains OPA-IPs that have been 
 *  submitted to OPA for processing but have not yet been processed.
 * 
 *  future – The future area in a staging area that data preparers can use to 
 *  get data ready for submission to OPA.
 */
public interface OpaStorage {
  static final String DESCRIPTION_XML = "description.xml";
  static final String OBJECTS_XML = "objects.xml";
  static final String CONTENT = "content";
  static final String OPA_RENDITIONS = "opa-renditions";
  static final String EXTRACTED_TEXT = "extracted-text";
  static final String IMAGE_TILES = "image-tiles";
  static final String TECHNICAL_METADATA = "technical-metadata";
  static final String THUMBNAILS = "thumbnails";
  static final String LIVE_PREFIX = "live";
  static final String LANDING_ZONE = "lz";

  public void deleteObjectAndOpaRenditions(String path) throws AspireException;

  public void deleteLegacyObjects(String path) throws AspireException;

  public boolean isFileNewer(String filePath, String referencePath) throws AspireException;

  public File getFile(String path) throws AspireException;

  public void saveFile(File src, String destPath) throws AspireException;

  public void saveFileAsPublic(File src, String destPath) throws AspireException;

  void saveFilesAsPublic(Map<String, File> files) throws AspireException;

  public void deleteFiles(String ... paths) throws AspireException;
  
  public String getFullPathInPreIngestion(String relativePath) throws AspireException;

  public String getFullPathInLive(String relativePath) throws AspireException;

  public String getFullNaIdPathInLive(String relativePath) throws AspireException;
  
  public String getFullPathInXmlStore(String relativePath) throws AspireException;

  public String getLegacyLiveBaseKey() throws AspireException;

  public void copyFileAsPublic(String src, String destPath) throws AspireException, IOException;
  
  public void copyFileFromS3(String bucketName, String key, String destPath)throws AspireException, IOException;

  public boolean exists(String path);

  public void deleteOpaIP() throws AspireException;

  String md5Hex(String key) throws AspireException;

  void close();
}