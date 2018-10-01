package gov.nara.opa.ingestion.dasexportutility;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.bzip2.BZip2Utils;
import org.apache.commons.compress.compressors.gzip.GzipUtils;
import org.apache.commons.io.FilenameUtils;

public class Archive implements AutoCloseable {
  final String name;
  final ArchiveInputStream archiveInputStream;
  String uncompressedFileName;
  
  ArchiveEntry entry;
  String entryName;
 
  public Archive(File file) throws ArchiveException, CompressorException, FileNotFoundException  {
    name = file.getName();
    archiveInputStream = 
      createArchiveInputStream(createCompressorInputStream(createBufferedInputStream(file)));
  }
  
  final InputStream createBufferedInputStream(File file) throws FileNotFoundException{        
    return new BufferedInputStream(new FileInputStream(file));
  }
  
  final InputStream createCompressorInputStream(InputStream inputStream) throws CompressorException{
    String compressorName = null;
    
    if (GzipUtils.isCompressedFilename(name)){
      compressorName = CompressorStreamFactory.GZIP;
      uncompressedFileName = GzipUtils.getUncompressedFilename(name);
    } else if (BZip2Utils.isCompressedFilename(name)){
      compressorName = CompressorStreamFactory.BZIP2;
      uncompressedFileName = BZip2Utils.getUncompressedFilename(name);
    }
        
    return compressorName == null 
        ? inputStream
        : new CompressorStreamFactory().createCompressorInputStream(compressorName, inputStream);
  }
  
  final ArchiveInputStream createArchiveInputStream(InputStream inputStream) throws ArchiveException{
    String archiverName = getArchiverName();
    
    return new ArchiveStreamFactory().createArchiveInputStream(archiverName, inputStream);
  }
  
  String getArchiverName(){
    String extension = uncompressedFileName != null 
      ? FilenameUtils.getExtension(uncompressedFileName)
      : FilenameUtils.getExtension(name);
        
    switch(extension){
      case ArchiveStreamFactory.TAR:
        return ArchiveStreamFactory.TAR;

      case ArchiveStreamFactory.ZIP:
        return ArchiveStreamFactory.ZIP;
        
      case ArchiveStreamFactory.SEVEN_Z:
        return ArchiveStreamFactory.SEVEN_Z;
        
      default:
        return null;
    }
  }
  
  /**
   * Gets the next entryFile entry in the archive.
   * Extracts entry into a temp entryFile.
   * @return True if there is an entry.
   * @throws AspireException
   */
  public boolean next() throws IOException{
    entry = getNextEntry();
    boolean hasNext = entry != null;
    
    if (hasNext){
      entryName = entry.getName();
    } else{
      clear();
    }
    
    return hasNext;
  }
  
  void clear(){
    entry = null;
    entryName = null;
  }
  
  public String getName(){
    return name;
  }
  
  public ArchiveEntry getEntry(){
    return entry;
  }
  
  public String getEntryName(){
    return entryName;
  }
  
  public InputStream getInputStream(){
    return archiveInputStream;
  }

  private ArchiveEntry getNextEntry() throws IOException {
    ArchiveEntry next = null;
    do{
      next = archiveInputStream.getNextEntry();
    } while(next != null && next.isDirectory());
    
    return next;
  }
  
  public void close() throws IOException {
    closeArchiveInputStream();    
  }
  
  private void closeArchiveInputStream() throws IOException {
    archiveInputStream.close(); 
  }
}