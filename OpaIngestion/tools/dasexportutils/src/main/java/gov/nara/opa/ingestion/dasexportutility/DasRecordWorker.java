package gov.nara.opa.ingestion.dasexportutility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Queue;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;


abstract class DasRecordWorker implements Runnable{
  private final Path file;
  private final Queue<String> results;
  private Document document;

  public DasRecordWorker(Path file, Queue<String> results) {
    this.file = file;
    this.results = results;
  }

  public Queue<String> getResults() {
    return results;
  }

  public Path getFile() {
    return file;
  }

  public Document getDocument() {
    return document;
  }

  @Override
  public void run() {
    try {
      loadXmlDocument();
      process();
      deleteFile();
    } catch (Throwable ex) {
      ex.printStackTrace();
    }
  }
  
  private void loadXmlDocument() throws Exception{
    SAXBuilder builder = new SAXBuilder();
    document = builder.build(getFile().toFile());
  }

  public abstract void process() throws Exception;

  private void deleteFile() {
    try {
      Files.delete(getFile());
    } catch (IOException e) {
    }
  }
}
