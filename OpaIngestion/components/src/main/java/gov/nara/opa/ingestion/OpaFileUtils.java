package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import org.apache.commons.io.FileUtils;

public final class OpaFileUtils {
    
  public static File createTempFile() throws AspireException{
    try {
      return File.createTempFile("tmp", null);
    } catch (IOException ex) {
      throw Exceptions.createAspireException("could-not-create-temp-file", ex);
    }
  }
  
  public static File getTempFile(String name) {
    return FileUtils.getFile(FileUtils.getTempDirectory(), name);
  }
  
  public static Path createDirectories(Path dir) throws AspireException{
    try {
      return Files.createDirectories(dir);
    } catch (IOException ex) {
      throw new AspireException("Files.createDirectories-failed", ex);
    }    
  }
  
  public static void delete(File file) throws AspireException{
    try {
      FileUtils.forceDelete(file);
    } catch (IOException ex) {
      throw new AspireException("force deleting", ex);
    }
    
  }
  
  public static void delete(Path path) throws AspireException{
    delete(path.toFile());
  }
  
  /**
   * Deletes a directories and its subdirectories.
   * @param dir
   * @throws AspireException 
   */
  public static void deleteDirectory(Path dir) throws AspireException{
    try {
      Files.walkFileTree(dir, 
        new SimpleFileVisitor<Path>() {        
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {

            if (exc == null) {
              Files.delete(dir);
              return FileVisitResult.CONTINUE;
            } else {
              throw exc;
            }
          }        
      });
    } catch (IOException ex) {
      Exceptions.throwAspireException(String.format("failed-to-delete-dir-%s", dir), ex);
    }
  }
  
  public static void moveFileWithReplaceExisting(Path source, Path target) throws AspireException {       
    try {
      Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException ex) {
      throw new AspireException("Files.move", ex);
    }
  }
  
  public static boolean isEmptyDir(Path dir) {
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
      Iterator<Path> iterator = stream.iterator();
      return iterator.hasNext() == false;
    } catch(IOException ex){
      return false;
    }
  }
  
  public static void deleteEmptyDirectories(Path dir) throws AspireException{
    try {
      Files.walkFileTree(dir, new DeletingEmptyDirectoriesFileVisitor());
    } catch (IOException ex) {
      throw new AspireException("deleteEmptyDirectories", ex);
    }
  }

  static void copyFile(File source, File target) throws AspireException {
    try {
      FileUtils.copyFile(source, target);
    } catch (IOException ex) {
      throw new AspireException("copyFile", ex);
    }
  }
  
  public static boolean isTempFile(File file){
    return file.getAbsolutePath().startsWith(FileUtils.getTempDirectoryPath());
  }
  
}
