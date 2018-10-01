package gov.nara.opa.ingestion.dasexportutility;

import org.jdom2.JDOMException;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Queue;

public class Main {

  public static void main(String[] args) throws IOException, JDOMException, XMLStreamException {
    String command = args[0];

    File export;
    int numberOfThreads;

    switch (command){
      case "extract":
        export = new File(args[1]);
        File baseDir = new File(args[2]);
        new DasExportExtractor(export, baseDir).extract();
        break;
      case "all-objects":
        export = new File(args[1]);
        numberOfThreads = Integer.parseInt(args[2]);
        new DigitalObjectsPrinter(export, numberOfThreads).execute();
        break;
    }

//    String filename = args[1];
//    String maxThreadsArg = args[2];
//    int maxThreads = Integer.parseInt(maxThreadsArg);
//
//    ConcurrentExecutor concurrentExecutor = new ConcurrentExecutor();
//    FileProcessor fileProcessor = getFileProcessor(command, filename);
//    concurrentExecutor.execute(fileProcessor, maxThreads);
  }

  private static FileProcessor getFileProcessor(String command, String filename) {
    switch(command){
      case "all-records":
        return new DasExportFileProcessor(filename, new DasRecordWorkerFactory() {
          @Override
          public DasRecordWorker createWorker(Path file, Queue<String> results) {
            return new RecordInfoProducer(file, results);
          }
        });
      case "all-objects":
        return new DasExportFileProcessor(filename, new DasRecordWorkerFactory() {
          @Override
          public DasRecordWorker createWorker(Path file, Queue<String> results) {
            return new ObjectInfoProducer(file, results);
          }
        });
      case "check-content-files":
        return new TextFileProcessor(filename, new LineWorkerFactory() {
          @Override
          public LineWorker createWorker(String line, Queue<String> results) {
            return new CheckContentFilesWorker(line, results);
          }
        });
      default:
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }    
  }
  
}
