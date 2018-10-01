package gov.nara.opa.ingestion.dasexportutility;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.io.input.CloseShieldInputStream;

public class DasExportFileIterator{
  private final Archive archive;
  private DasXmlFileIterator iterator;
  private Reader reader;
  
  public DasExportFileIterator(Path file) throws ArchiveException, CompressorException, FileNotFoundException{
    this.archive = new Archive(file.toFile());
  }
  
  public void close() throws IOException  {
    archive.close();
  }

  public boolean hasNext() throws IOException, XMLStreamException {    
      
    if (iterator == null){
          boolean hasNextFile = archive.next();
          if (!hasNextFile){
            return false;
          }

      reader = createReader();
      iterator = createDasXmlFileIterator();
    }

    return iterator.hasNext();    
  }
  
  private DasXmlFileIterator createDasXmlFileIterator() throws XMLStreamException{
    DasXmlFileIterator fileIterator = null;//new DasXmlFileIterator(reader);
    //fileIterator.initialize();
    return fileIterator;
  }
  
  private Reader createReader(){
    return new BufferedReader(new InputStreamReader(new CloseShieldInputStream(archive.getInputStream())));
  }

  public Path next() throws IOException, XMLStreamException {
    Path file = null;//iterator.next();

    if (reachedEndOfFile()){
    	closeReader();
    	closeIterator();    
    }
    
    return file;
  }
  
  private boolean reachedEndOfFile(){
	  return !iterator.hasNext();
  }
  
  private void closeReader() throws IOException {
    reader.close();
  }
  
  private void closeIterator() throws XMLStreamException {
    iterator.close();
    iterator = null;
  }  
}
