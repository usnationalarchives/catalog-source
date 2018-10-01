package gov.nara.opa.ingestion;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;

public class DeletingEmptyDirectoriesFileVisitor extends SimpleFileVisitor<Path>{
    @Override
    public FileVisitResult postVisitDirectory(Path directory, IOException ioe) throws IOException {
      if (OpaFileUtils.isEmptyDir(directory)){
        Files.delete(directory); 
      }      
      return FileVisitResult.CONTINUE;
    }
}
