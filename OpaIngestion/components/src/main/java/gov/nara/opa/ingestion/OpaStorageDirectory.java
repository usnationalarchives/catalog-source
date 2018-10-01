package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Represents a directory within an Opa Storage area.
 */
public class OpaStorageDirectory {
  final Path dir;
  
  protected Path getDir(){
    return dir;
  }
  
  public OpaStorageDirectory(Path dir){
    this.dir = dir;
  }
    
  public OpaStorageDirectory create() throws AspireException{
    try {
      Files.createDirectories(dir);
      return this;
    } catch (IOException ex) {
      throw new AspireException("create", ex, "Opa Storage dir '%s' could not be created", dir);
    } 
  }
  
  public BufferedWriter createFileWriter(String fileName) throws AspireException{
    try {
      return Files.newBufferedWriter(dir.resolve(fileName), StandardCharsets.UTF_8);
    } catch (IOException ex) {
      throw new AspireException("createFileWriter", ex);
    }
  }
  
  public BufferedReader createFileReader(String fileName) throws AspireException{
    try {
      return Files.newBufferedReader(dir.resolve(fileName), StandardCharsets.UTF_8);
    } catch (IOException ex) {
      throw new AspireException("createFileReader", ex);
    }
  }
  
  public boolean exists(String fileName){
    return Files.exists(dir.resolve(fileName));
  }
  
  public Path resolveAsPath(String fileName){
    return dir.resolve(fileName);
  }
  
  public File resolve(String fileName){
    return resolveAsPath(fileName).toFile();
  }
  
  public Path resolve(Path file){
    return dir.resolve(file);
  }
}
