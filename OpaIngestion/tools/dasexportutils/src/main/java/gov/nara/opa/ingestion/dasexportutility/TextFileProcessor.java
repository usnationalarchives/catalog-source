package gov.nara.opa.ingestion.dasexportutility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

public class TextFileProcessor implements FileProcessor{
  private final LineWorkerFactory factory;
  private final String filename;

  public TextFileProcessor(String filename, LineWorkerFactory factory){
    this.factory = factory;
    this.filename = filename;    
  }
  
  @Override
  public void execute(ExecutorService executorService, BlockingQueue<String> results) {
    File file = new File(filename);
    
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      String line;
      while ((line = br.readLine()) != null) {
        executorService.execute(factory.createWorker(line, results));
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }
}
