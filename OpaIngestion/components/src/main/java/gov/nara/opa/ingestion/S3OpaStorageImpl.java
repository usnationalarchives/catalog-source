/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nara.opa.ingestion;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.searchtechnologies.aspire.services.AspireException;

import java.io.*;
import java.util.*;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public class S3OpaStorageImpl implements OpaStorage {
  private static final String OPA_STORAGE = "opastorage";
  private static final String LIVE = "live";
  private static final String PREINGESTION = "pre-ingestion";
  private static final String XML_STORE = "xmlstore";
  private static final String XML_STORE_FULL = "xmlstore/full";
  
  private String recordKey;
  private final String bucketName;
  private final AmazonS3Client amazonS3Client;
  private final Integer naid;
  private String liveBaseKey;
  private final String preIngestionBaseKey = PREINGESTION;
  private final AWSCredentials awsCredentials;
  private final TransferManager transferManager;

  public S3OpaStorageImpl(String bucketName, AWSCredentials awsCredentials, Integer naid){
    this.bucketName = bucketName;
    this.awsCredentials = awsCredentials;
    this.amazonS3Client = new AmazonS3Client(awsCredentials);
    this.transferManager = new TransferManager(awsCredentials);
    this.naid = naid;
  }

  public String md5Hex(String key) throws AspireException {
    ObjectMetadata md = getObjectMetadata(key);
    if(md != null) {
      return md.getETag();
    }
    else return null;
  }

  public Long getObjectFileSize(String key) throws AspireException {
    ObjectMetadata md = getObjectMetadata(key);
    if (md != null){
      return md.getContentLength();
    }
    else return null;
  }

  @Override
  public void close() {
    amazonS3Client.shutdown();
    transferManager.shutdownNow();
  }

  private String getRecordKey(){
    return recordKey != null ? recordKey : (recordKey = buildRecordKey());
  }

  private String buildRecordKey() {
    return String.format("%s/%d/%d/%d.xml", 
            XML_STORE_FULL, 
            NAIDDirectories.getLevel1(naid),
            NAIDDirectories.getLevel2(naid),
            naid);
  }

  /* uses much less expensive API call - jdh */
  private ObjectMetadata getObjectMetadata(String key){
    ObjectMetadata md;
    key = StringUtils.replace(key, "+", " ");
    key = StringUtils.replace(key,"%20", " ");
    try {
      md = amazonS3Client.getObjectMetadata(bucketName, key);
    } catch (AmazonS3Exception e) {
      md = null;
    }
    return md;
  }

  private ObjectListing listObject(String key) {
    ListObjectsRequest request = new ListObjectsRequest()
      .withBucketName(bucketName)
      .withPrefix(key)
      .withMaxKeys(1);
    return amazonS3Client.listObjects(request);
  }

  private String getPreIngestionBaseKey(){
    return preIngestionBaseKey;
  }
  
  private String getLiveBaseKey(String relativeObjectPath) {
    return createOpaStorageBaseKey(relativeObjectPath);
  }

  @Override
  public String getLegacyLiveBaseKey() {
    return createLegacyOpaStorageBaseKey();
  }

  private String getLiveNaIdBaseKey() {
    return String.format("%s/%d/%d/%d",
            XML_STORE_FULL,
            NAIDDirectories.getLevel1(naid),
            NAIDDirectories.getLevel2(naid),
            naid);
  }

  @Override
  public String getFullNaIdPathInLive(String relativePath) {
    return getLiveNaIdBaseKey() + "/" + relativePath;
  }

  private String createLegacyOpaStorageBaseKey() {
    return String.format("%s/%s/%d/%d/%d", 
            OPA_STORAGE,
            LIVE,
            NAIDDirectories.getLevel1(naid),
            NAIDDirectories.getLevel2(naid),
            naid);
  }

  private String createOpaStorageBaseKey(String objectRelativePath) {
    if (objectRelativePath.startsWith("/")) {
      objectRelativePath = objectRelativePath.substring(1);
    }
    return String.format("%s/%s", LIVE_PREFIX, objectRelativePath.replaceFirst(LANDING_ZONE+"/",""));
  }

  private boolean isEmpty(ObjectListing objectListing) {
    return objectListing.getObjectSummaries().isEmpty();
  }

  private String getETag(ObjectListing objectListing) {
    return objectListing.getObjectSummaries().get(0).getETag();
  }
  
  private S3ObjectSummary getFirstObjectSummary(ObjectListing objectListing){
    return objectListing.getObjectSummaries().get(0);
  }
  
  private Date getLastModified(ObjectListing objectListing){
    return getFirstObjectSummary(objectListing).getLastModified();
  }
  
  private void upload(File file, String key) throws AspireException {
    try {
      amazonS3Client.putObject(bucketName, key, file);
    } catch (AmazonClientException e) {
      throw new AspireException("S3 Upload", e.getMessage());
    }
  }
/***
  This method will remove all digital objects associated with the provided key:
  The original at the LANDING_ZONE prefix

  Update 2017-06-19 JDH:
       decided to not remove original LZ image in case for some
       reason we need it later

  And the renditions at the live prefix (i.e. live/<object key/*)
 */
  @Override
  public void deleteObjectAndOpaRenditions(String key) throws AspireException {
    /*LinkedList<KeyVersion> keys = new LinkedList<>();
    keys.add(new KeyVersion(key));

    amazonS3Client.deleteObjects(
      new DeleteObjectsRequest(bucketName)
          .withKeys(keys)
    );*/
    // delete rendition objects in new S3 storage area
    deleteObjects(getLiveBaseKey(key));
  }

  @Override
  public void deleteLegacyObjects(String key) throws AspireException {
    LinkedList<KeyVersion> keys = new LinkedList<>();
    keys.add(new KeyVersion(key));

    String name = FilenameUtils.getName(key);

    String extractedTextKey = String.format("%s/%s/%s/%s.txt",
            getLegacyLiveBaseKey(), OPA_RENDITIONS, EXTRACTED_TEXT, name);
    keys.add(new KeyVersion(extractedTextKey));

    String thumbnailKey = String.format("%s/%s/%s/%s-thumb.jpg",
            getLegacyLiveBaseKey(), OPA_RENDITIONS, THUMBNAILS, name);
    keys.add(new KeyVersion(thumbnailKey));

    String deepZoomImageKey = String.format("%s/%s/%s/%s.dzi",
            getLegacyLiveBaseKey(), OPA_RENDITIONS, IMAGE_TILES, name);
    keys.add(new KeyVersion(deepZoomImageKey));

    amazonS3Client.deleteObjects(
            new DeleteObjectsRequest(bucketName)
                    .withKeys(keys)
    );

    String deepZoomImageFilesPrefix = String.format("%s/%s/%s/%s_files",
            getLegacyLiveBaseKey(), OPA_RENDITIONS, IMAGE_TILES, name);
    deleteObjects(deepZoomImageFilesPrefix);

  }

  @Override
  public boolean isFileNewer(String filePath, String referencePath) throws AspireException {
    ObjectMetadata file = getObjectMetadata(filePath);
    if(file == null){
      return false;
    }

    ObjectMetadata reference = getObjectMetadata(referencePath);

    return (reference == null) || file.getLastModified().after(reference.getLastModified());
    /*ObjectListing file = listObject(filePath);
    
    if (isEmpty(file)){
      return false;
    }
    
    ObjectListing reference = listObject(referencePath);
    
    return isEmpty(reference) || 
            getLastModified(file).after(getLastModified(reference));
            */
  }

  @Override
  public File getFile(String key) throws AspireException{
    key = StringUtils.replace(key, "+", " ");
    key = StringUtils.replace(key,"%20", " ");
    if (!exists(key)){
      return null;
    }

    try {
      File file = File.createTempFile("s3-", "." + FilenameUtils.getExtension(key));

      Download download = transferManager.download(bucketName, key, file);
      download.waitForCompletion();

      return file;
    } catch (AmazonClientException | IOException | InterruptedException e) {
      throw new AspireException("download.waitForCompletion", e, "Key: %s", key);
    }
  }

  @Override
  public void saveFile(File file, String key) throws AspireException {
    key = StringUtils.replace(key, "+", " ");
    key = StringUtils.replace(key,"%20", " ");
    upload(file, key);
  }

  @Override
  public void deleteFiles(String... keys) throws AspireException {
    try{
      DeleteObjectsRequest deleteObjectsRequest = 
              new DeleteObjectsRequest(bucketName)
              .withKeys(keys);
      amazonS3Client.deleteObjects(deleteObjectsRequest);
    } catch(MultiObjectDeleteException e){      
    }
  }

  @Override
  public String getFullPathInPreIngestion(String relativePath) {
    return getFullPath(getPreIngestionBaseKey(), relativePath);
  }

  @Override
  public String getFullPathInLive(String relativePath) {
    if(relativePath.startsWith("/")){
      relativePath = relativePath.substring(1);
    }
    if(relativePath.startsWith(LANDING_ZONE)){
      return relativePath;
    }
    return getFullPath(getLegacyLiveBaseKey(), relativePath);
  }
  
  private String getFullPath(String basePath, String relativePath){            
    return FilenameUtils.separatorsToUnix(
          basePath 
          + File.separator 
          + relativePath
    );
  }

  @Override
  public void copyFileAsPublic(String sourceKey, String destinationKey) throws AspireException {
    copyObjectAsPublic(bucketName, sourceKey, bucketName, destinationKey);
  }
  
  private void copyObjectAsPublic(String sourceBucketName, String sourceKey, 
          String destinationBucketName, String destinationKey) throws AspireException {
    try {
      amazonS3Client.copyObject(
              new CopyObjectRequest(sourceBucketName, sourceKey,
                      destinationBucketName, destinationKey)
                      .withCannedAccessControlList(CannedAccessControlList.PublicRead)
      );
    } catch (AmazonClientException e) {
      throw new AspireException("copy", e);
    }
  }

  @Override
  public boolean exists(String key) {
    return getObjectMetadata(key)!=null;
  }

  @Override
  public String getFullPathInXmlStore(String relativePath) throws AspireException {
    return FilenameUtils.separatorsToUnix(
            XML_STORE + File.separator + relativePath);
  }

  @Override
  public void deleteOpaIP() {
     //deleteObjects(getLiveBaseKey(baseObjectFullPath));
  }
  
  private void deleteObjects(String prefix){
    ListObjectsV2Request listObjectsRequest = new ListObjectsV2Request()
      .withBucketName(bucketName)
      .withPrefix(prefix);

     ListObjectsV2Result v2ObjectListing;
     int iterCount = 0;
     do{
       LinkedList<KeyVersion> keys = new LinkedList<>();
       v2ObjectListing = amazonS3Client.listObjectsV2(listObjectsRequest);
       for (S3ObjectSummary objectSummary : v2ObjectListing.getObjectSummaries()) {
         keys.add(new KeyVersion(objectSummary.getKey()));
       }
       if (keys.size() > 0) {
         iterCount++;
         try {
           DeleteObjectsRequest deleteObjectsRequest =
                   new DeleteObjectsRequest(bucketName)
                           .withKeys(keys);
           amazonS3Client.deleteObjects(deleteObjectsRequest);
         }catch(MultiObjectDeleteException mode) {
             System.out.println("Error deleting '" + prefix + "' after " + iterCount + " calls to listObjectsV2.");
             System.out.format("%s \n", mode.getMessage());
             System.out.format("No. of objects successfully deleted = %s\n", mode.getDeletedObjects().size());
             System.out.format("No. of objects failed to delete = %s\n", mode.getErrors().size());
             System.out.format("Printing error data...\n");
             for (MultiObjectDeleteException.DeleteError deleteError : mode.getErrors()){
                 System.out.format("Object Key: %s\t%s\t%s\n",
                         deleteError.getKey(), deleteError.getCode(), deleteError.getMessage());
             }
         }catch(Exception e){
             System.out.println("Error deleting '" + prefix + "' after " + iterCount + " calls to listObjectsV2.");
             System.out.println("Delete exception: "+e.getMessage());
             e.printStackTrace();
           break;
         }
       }
       listObjectsRequest.setContinuationToken(v2ObjectListing.getContinuationToken());
     } while (v2ObjectListing.isTruncated());
  }

  @Override
  public void copyFileFromS3(String sourceBucketName, String sourceKey, String destinationKey) throws AspireException, IOException {
    copyObjectAsPublic(sourceBucketName, sourceKey, bucketName, destinationKey);
  }

  @Override
  public void saveFileAsPublic(File file, String key) throws AspireException {
    try {
      amazonS3Client.putObject(
              new PutObjectRequest(bucketName, key, file)
                      .withCannedAcl(CannedAccessControlList.PublicRead)
      );
    } catch (AmazonClientException e) {
      throw new AspireException("Upload as public", e.getMessage());
    }
  }

  @Override
  public void saveFilesAsPublic(Map<String, File> files) throws AspireException {

    try {

      ArrayList<Upload> uploads = new ArrayList<>(files.size());

      for (Map.Entry<String, File> entry : files.entrySet()) {

        PutObjectRequest request =
            new PutObjectRequest(bucketName, entry.getKey(), entry.getValue())
                .withCannedAcl(CannedAccessControlList.PublicRead);

        uploads.add(transferManager.upload(request));
      }

      for (Upload upload : uploads) {
        upload.waitForCompletion();
      }

    } catch (InterruptedException e) {
      throw new AspireException("S3 TransferManager Uploads", e);
    }
  }

}
