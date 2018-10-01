package gov.nara.opa.ingestion;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.searchtechnologies.aspire.framework.ComponentImpl;
import com.searchtechnologies.aspire.services.AspireException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Element;

/**
 * Opa Storage filesystem implementation.
 */
public class FileSystemOpaStorageImpl extends ComponentImpl implements OpaStorage{
  static final String ROOT_DIR_TAG = "rootDir";  

  static final String LIVE = "live";
  static final String PREINGESTION = "pre-ingestion";
  static final String QUARANTINE = "quarantine";
  static final String FUTURE = "future";  
  static final String DELETED = "deleted";  
  
  private Path baseDir;
  private final Integer naid;
  private OpaStorageArea live;
  private OpaStorageDirectory preIngestion;  
  private final Path xmlStore;
  private final Settings settings;
  
  public FileSystemOpaStorageImpl(Path baseDir, Integer naid, Settings settings){
    this.baseDir = baseDir;
    this.naid = naid;
    this.settings = settings;
    this.xmlStore = Paths.get(settings.getXmlStore());
  }
  
  @Override
  public void initialize(Element config) throws AspireException {
    String rootDirValue = getStringFromConfig(config, ROOT_DIR_TAG, null);
    baseDir = Paths.get(rootDirValue);    
    ensureBaseDirExists();  
  }
  
  private void ensureBaseDirExists() throws AspireException{
    try {
      Files.createDirectories(baseDir);
    } catch (IOException ex) {
      error("Opa Storage root dir '%s' could not be created", baseDir);
      throw new AspireException("Opa Storage base dir", ex);
    } 
    
    File dir = baseDir.toFile();
    
    // Check that OpaStorage is a directory
    if (!dir.isDirectory()) {
      throw new AspireException(this, "opastorageroot-is-not-a-directory", "The OpaStorage root directory is not a directory.");
    }

    // Check that OpaStorage is readable
    if (!dir.canRead()) {
      throw new AspireException(this, "opastorageroot-is-not-readable", "The OpaStorage root directory is not readable.");
    }

    // Check that OpaStorage is writable
    if (!dir.canWrite()) {
      throw new AspireException(this, "opastorageroot-is-not-writable", "The OpaStorage root directory is not writable.");
    }
  }

  @Override
  public void close() {    
  }

  public OpaStorageArea getLive() throws AspireException{    
    return live != null ? live : (live = createLive());
  }
  
  private OpaStorageArea createLive() throws AspireException{
    OpaStorageArea area = new OpaStorageArea(getBaseAreaDir(LIVE));
    area.create();
    return area;
  }

  private OpaStorageDirectory getPreIngestion() throws AspireException{
    return preIngestion != null ? preIngestion : (preIngestion = createPreIngestion());
  }
  
  private OpaStorageDirectory createPreIngestion() throws AspireException{
    OpaStorageDirectory area = new OpaStorageDirectory(baseDir.resolve(PREINGESTION));
    area.create();
    return area;
  }
  
  private Path getBaseAreaDir(String area){
    Path areaDir = baseDir.resolve(area);
    return NAIDDirectories.getThirdLevelDir(areaDir, naid);
  }
    
  @Override
  public void deleteObjectAndOpaRenditions(String path) throws AspireException{
    
    getFile(path).delete();
    
    String name = FilenameUtils.getName(path);
    
    getLive().getExtractedTextDir().resolve(name + ".txt").delete();
    
    getLive().getThumbnailsDir().resolve(name + "-thumb.jpg").delete();
    
    getLive().getImageTilesDir().resolve(name + ".dzi").delete();
    
    File deepZoomFilesDir = getLive().getImageTilesDir().resolve(name + "_files");
    deleteDirectory(deepZoomFilesDir);
    
  }

  @Override
  public void deleteLegacyObjects(String key) throws AspireException {

  }

  private void deleteDirectory(File directory) throws AspireException{
    try {
      FileUtils.deleteDirectory(directory);
    } catch (IOException ex) {
      throw new AspireException("FileUtils.deleteDirectory", ex.getMessage());
    }
  }

  @Override
  public boolean isFileNewer(String filePath, String referencePath) throws AspireException{
    File file = new File(filePath);
    File reference = new File(referencePath);
    return file.exists() 
            && (
              !reference.exists() 
              || FileUtils.isFileNewer(file, reference)
            );
  }

  @Override
  public File getFile(String path) {
    return new File(path);
  }

  @Override
  public void saveFile(File src, String destPath) throws AspireException {
    File dest = new File(destPath);
    OpaFileUtils.copyFile(src, dest);
  }

  @Override
  public void deleteFiles(String... paths) throws AspireException {
    for (String path : paths){
      File file = getFile(path);
      
      if (file.isDirectory()){
        try {
          FileUtils.deleteDirectory(file);
        } catch (IOException ex) {
          throw new AspireException("deleteDirectory", ex);
        }
      } else {
        file.delete();
      }
    }
  }

  @Override
  public String getFullPathInPreIngestion(String path) throws AspireException{
    return getPreIngestion().resolve(path).toString();
  }

  @Override
  public String getFullPathInLive(String path) throws AspireException {
    return getLive().resolve(path).toString();
  }

  @Override
  public String getFullNaIdPathInLive(String relativePath) throws AspireException {
    return null;
  }

  @Override
  public void copyFileAsPublic(String srcPath, String destPath) throws AspireException, IOException {
    FileUtils.copyFile(getFile(srcPath), getFile(destPath));
  }

  @Override
  public boolean exists(String path) {
    return getFile(path).exists();
  }

  @Override
  public String getFullPathInXmlStore(String relativePath) throws AspireException {
    return xmlStore.resolve(relativePath).toString();
  }

  @Override
  public String getLegacyLiveBaseKey() throws AspireException {
    return null;
  }

  @Override
  public void deleteOpaIP() throws AspireException {
    File dir = getLive().getDir().toFile();
    try {    
      FileUtils.deleteDirectory(dir);
    } catch (IOException ex) {
      throw new AspireException("deleteDirectory", ex);
    }
  }

  @Override
  public String md5Hex(String key) throws AspireException {
    return Digests.md5Hex(getFile(key));
  }

  @Override
  public void copyFileFromS3(String bucketName, String key, String destPath) throws AspireException, IOException{
    BasicAWSCredentials credentials = new BasicAWSCredentials(settings.getAwsAccessKeyId(), settings.getAwsSecretKey());
    GetObjectRequest request = new GetObjectRequest(bucketName, key);	
    AmazonS3 s3 = new AmazonS3Client(credentials);
    
    File dest = File.createTempFile("fromS3", null);
    
    s3.getObject(request, dest);
    
    saveFile(dest, destPath);
    
    dest.delete();
  }

  @Override
  public void saveFileAsPublic(File src, String destPath) throws AspireException {
    saveFile(src, destPath);
  }

  @Override
  public void saveFilesAsPublic(Map<String, File> files) throws AspireException {
    throw new UnsupportedOperationException();
  }
}
