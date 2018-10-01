package gov.nara.opa.ingestion.dasexportutility;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.compressors.CompressorException;

public class DasExportFileProcessor implements FileProcessor{
  private final DasRecordWorkerFactory factory;
  private final String filename;

  public DasExportFileProcessor(String filename, DasRecordWorkerFactory factory){
    this.factory = factory;
    this.filename = filename;    
  }
  
  @Override
  public void execute(ExecutorService executorService, BlockingQueue<String> results) {
    try {
      
      Path export = Paths.get(filename);
      
      DasExportFileIterator iter = new DasExportFileIterator(export);
      
      while (iter.hasNext()) {
        Path file = iter.next();
        executorService.execute(factory.createWorker(file, results));
      }
      
      iter.close();
      
    } catch (ArchiveException | CompressorException | FileNotFoundException ex) {
      Logger.getLogger(DasExportFileProcessor.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException | XMLStreamException ex) {
      Logger.getLogger(DasExportFileProcessor.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
}
