package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.Component;
import com.searchtechnologies.aspire.services.Job;
import com.searchtechnologies.aspire.services.logging.ALogger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;

/**
 * Downloads digital object file to OpaIP/content/
 * Sources are:
 * 1. Pre-ingestion area.
 * 2. S3 bucket
 * 3. Internet
 */
public class DigitalObjectDownloader {

  private final ALogger logger;
  private final Job job;
  private final JobInfo jobInfo;
  private final URL sourceUrl;
  private final String fullPathInPreIngestion;
  private final String fullPathInLive;
  private final Settings settings;
  private final OpaStorage opaStorage;
  
  public DigitalObjectDownloader(Component component, Job job) throws AspireException{
    this.logger = (ALogger)component;
    this.job = job;
    this.jobInfo = Jobs.getJobInfo(job);
    this.opaStorage = jobInfo.getOpaStorage();
    this.settings = Components.getSettings(component);
    this.sourceUrl = getSourceUrl();
    this.fullPathInPreIngestion  = opaStorage.getFullPathInPreIngestion(pathToFileInPreIngestion());
    this.fullPathInLive = opaStorage.getFullPathInLive(getPathToFileInLive());
  }
  
  private URL getSourceUrl() throws AspireException{
    String url = job.get().getText("accessFilename");
    return URIUtilities.getUrlWithPathEncoded(url);
  }
  
  private String getPathToFileInLive(){
    return OpaStorage.CONTENT + sourceUrl.getPath();
  }
  
  private String pathToFileInPreIngestion(){
    String path = sourceUrl.getPath();
    return path.startsWith("/") ? path.substring(1) : path;
  }
  
  public void download() throws AspireException, IOException{        
    if (shouldCopyFromPreIngestion()){
      copyFromPreIngestion();
    } else if (shouldDownloadFileFromAmazonAws()){
      downloadFileFromAmazonS3();
    } else if (shouldDownloadFileFromInternet()) {
      downloadFile();
    } 
  }

  public void downloadExtractedText() throws AspireException, IOException {
    downloadExtractedTextFromAmazonS3();
  }

  public void downloadLegacyExtractedText() throws AspireException, IOException {
    downloadLegacyExtractedTextFromAmazonS3();
  }

  private boolean shouldCopyFromPreIngestion() throws AspireException{
    return opaStorage.isFileNewer(fullPathInPreIngestion, fullPathInLive);
  }
  
  private void copyFromPreIngestion() throws AspireException, IOException{    
    opaStorage.copyFileAsPublic(fullPathInPreIngestion, fullPathInLive);
    logger.info("Copied '%s' to '%s'", fullPathInPreIngestion, fullPathInLive);
  }
  
  private boolean shouldDownloadFileFromAmazonAws(){
    return settings.downloadDigitalObjectsIsEnabled() 
            && isResourceHostedInAmazonS3()
            && !opaStorage.exists(fullPathInLive);
  }
  
  private boolean isResourceHostedInAmazonS3(){
    String host = sourceUrl.getHost();
    return host != null && (host.startsWith("s3.amazonaws.com") || host.endsWith(".s3.amazonaws.com"));
  }
  
  private void downloadFileFromAmazonS3() throws AspireException, IOException{
    String bucketName = getS3BucketName();
    String accessFilename = job.get().getText("accessFilename");
    URL url = new URL(accessFilename);
    String key = url.getPath().substring(1);    
    opaStorage.copyFileFromS3(bucketName, key, fullPathInLive);
    logger.info("Copied '%s' to '%s'", accessFilename, fullPathInLive);
  }

  protected void downloadOriginalContentForTMDprocessing() throws AspireException, IOException {
    String accessFilename = job.get().getText("accessFilename");
    logger.debug("accessFilename: %s",accessFilename);
    URL url = URIUtilities.getUrlWithPathEncoded(accessFilename);
    String encodedAccessFilename = url.toString();

    logger.info("Downloading source original object.  URL: %s",encodedAccessFilename);

    String extension = "." + FilenameUtils.getExtension(url.getPath());
    File contentFile = OpaFileUtils.getTempFile(UUID.randomUUID() + extension);
    FileUtils.copyURLToFile(url, contentFile, 10000, 10000);
    if (contentFile != null) {
      jobInfo.setTMDContentFile(contentFile);
    } else {
      logger.debug("Couldn't find file for TMD regeneration. object: %s",encodedAccessFilename);
    }

  }

  private void downloadExtractedTextFromAmazonS3() throws AspireException, IOException {
    logger.debug("Downloading extracted text from AWS...");
    String bucketName = getS3BucketName();
    String accessFilename = job.get().getText("accessFilename");
    logger.debug("accessFilename: %s",accessFilename);
    URL url = URIUtilities.getUrlWithPathEncoded(accessFilename);
    String encodedAccessFileName = url.toString();

    logger.debug("encoded accessFilename: %s",encodedAccessFileName);

    String originalObjectKey = url.getPath().substring(1);
    String key = originalObjectKey;

    if (key.startsWith(bucketName)) {
      key = key.replace(bucketName+"/","");
    }

    // if it's a JPEG2000, BMP, or TIFF file, change the name.
    if( key.toLowerCase().endsWith(".jp2") ){
      key = key.replace(".jp2", ".jpg");
      key = key.replace(".JP2",".jpg");
    } else if ( key.toLowerCase().endsWith(".tif") ) {
      key = key.replace(".tif",".jpg");
      key = key.replace(".TIF",".jpg");
    } else if ( key.toLowerCase().endsWith(".tiff") ) {
      key = key.replace(".tiff", ".jpg");
      key = key.replace(".TIFF",".jpg");
    } else if ( key.toLowerCase().endsWith(".bmp") ) {
      key = key.replace(".bmp", ".jpg");
      key = key.replace(".BMP", ".jpg");
    }

    String[] keyParts = key.split("/");
    String fileName = keyParts[keyParts.length-1];

    String lzKey = key;
    key = key.replaceFirst("lz","live");

    String baseKey = key + "/" + "opa-renditions/extracted-text/" + fileName;
    String metadataKey = baseKey + ".technical-metadata-object";
    String extractedTextKey = baseKey + ".txt";
    String extractedPaginatedTextKey = baseKey + ".paginated.txt";

    logger.debug("metadata key: %s",metadataKey);
    logger.debug("extracted text key: %s",extractedTextKey);
    logger.debug("extracted paginated text key: %s",extractedPaginatedTextKey);

    if (opaStorage.exists(metadataKey)) {
      logger.debug("found metadata in s3");
      jobInfo.setMetaDataFile(opaStorage.getFile(metadataKey));
    }
    if (opaStorage.exists(extractedTextKey)) {
      logger.debug("found extracted text in s3");
      jobInfo.setExtractedTextFile(opaStorage.getFile(extractedTextKey));
    }
    if (opaStorage.exists(extractedPaginatedTextKey)) {
      logger.debug("found paginated text in s3");
      jobInfo.setExtractedPaginatedTextFile(opaStorage.getFile(extractedPaginatedTextKey));
    }

    logger.debug("Getting object file size for key: %s",lzKey);
    if( opaStorage instanceof S3OpaStorageImpl) {
      long fileSize = ((S3OpaStorageImpl) opaStorage).getObjectFileSize(lzKey);
      logger.debug("%s file size: %s", lzKey, fileSize);
      jobInfo.setObjectFileSize(fileSize);
    }
  }

  private void downloadLegacyExtractedTextFromAmazonS3() throws AspireException, IOException {
    logger.debug("Downloading legacy extracted text from AWS S3...");

    String extractedTextKey = jobInfo.getLegacyPathToExtractedText();
    String extractedPaginatedTextKey = jobInfo.getLegacyPathToPaginatedText();

    logger.debug("legacy extracted text key: %s",extractedTextKey);
    logger.debug("legacy extracted paginated text key: %s", extractedPaginatedTextKey);

    if (opaStorage.exists(extractedTextKey)) {
      logger.debug("found extracted text in s3");
      jobInfo.setExtractedTextFile(opaStorage.getFile(extractedTextKey));
    }
    if (opaStorage.exists(extractedPaginatedTextKey)) {
      logger.debug("found paginated text in s3");
      jobInfo.setExtractedPaginatedTextFile(opaStorage.getFile(extractedPaginatedTextKey));
    }
  }
  
  protected String getS3BucketName() {
    String host = sourceUrl.getHost();
    if (host.startsWith("s3.amazonaws.com")){
      return sourceUrl.getPath().replaceFirst("^/", "").split("/")[0];
    }else{
      int indexOf = host.indexOf(".s3.amazonaws.com");
      if (indexOf > 0){
        return host.substring(0, indexOf);
      }
    }
    return null;
  }
  
  private boolean shouldDownloadFileFromInternet() {
    return settings.downloadDigitalObjectsIsEnabled()
            && !opaStorage.exists(fullPathInLive);
  }  
    
  private void downloadFile() throws AspireException{
    try {
      String extension = "." + FilenameUtils.getExtension(sourceUrl.getPath());
      File contentFile = OpaFileUtils.getTempFile(UUID.randomUUID() + extension);
      
      FileUtils.copyURLToFile(sourceUrl, contentFile, 10000, 10000);

      opaStorage.saveFileAsPublic(contentFile, fullPathInLive);
      
      contentFile.delete();
      
      logger.info("Downloaded '%s' to '%s'", sourceUrl, fullPathInLive);
    } catch (IOException ex) {
      throw new AspireException("Failed to download from internet", ex);
    }
  }
}
