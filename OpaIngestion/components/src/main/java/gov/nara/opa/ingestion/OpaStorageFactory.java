package gov.nara.opa.ingestion;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.searchtechnologies.aspire.framework.ComponentImpl;
import com.searchtechnologies.aspire.services.AspireException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.w3c.dom.Element;

public class OpaStorageFactory extends ComponentImpl {
  private Settings settings;
  private Path baseDir;
  private AWSCredentials awsCredentials;
  
  public OpaStorage createOpaStorage(Integer naid){
    return settings.isS3StorageEnabled() ? 
            createS3OpaStorage(naid) : createFileSystemOpaStorage(naid);
  }
  
  private OpaStorage createFileSystemOpaStorage(Integer naid){
    return new FileSystemOpaStorageImpl(baseDir, naid, settings);
  }
  
  private OpaStorage createS3OpaStorage(Integer naid){
    return new S3OpaStorageImpl(settings.getS3StorageBucketName(), awsCredentials, naid);
  }

  @Override
  public void initialize(Element config) throws AspireException {
    this.settings = Components.getSettings(this);
    this.awsCredentials = new BasicAWSCredentials(
        settings.getS3StorageAccessKeyId(),
        settings.getS3StorageSecretKey()
    );
    this.baseDir = Paths.get(settings.getOpaStorage());
  }

  @Override
  public void close() {
  }
}
