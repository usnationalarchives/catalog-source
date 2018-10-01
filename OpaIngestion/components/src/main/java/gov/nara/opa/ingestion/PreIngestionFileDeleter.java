package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.Component;
import java.io.File;
import java.nio.file.Path;

public class PreIngestionFileDeleter {
  private Path preIngestionDir;
    
  public PreIngestionFileDeleter(Component component) throws AspireException{
  }

  public void deleteFile(File file) throws AspireException{
    Path path = file.toPath();
    OpaFileUtils.delete(path);
    deleteParentDirectories(path);
  }

  private void deleteParentDirectories(Path file) throws AspireException{
    deleteParentDirectory(file.getParent());
  }

  private void deleteParentDirectory(Path dir) throws AspireException {
    if (dir.equals(preIngestionDir)){
      return;
    }

    if (OpaFileUtils.isEmptyDir(dir)){
      OpaFileUtils.delete(dir);
      deleteParentDirectory(dir.getParent());
    }
  }   
}
