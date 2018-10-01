package gov.nara.opa.ingestion.dasexportutility;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Queue;

public class CheckContentFilesWorker extends LineWorker{
  public CheckContentFilesWorker(String line, Queue<String> results){
    super(line, results);
  }
  
  @Override
  public void process() {
    String[] parts = getLine().split("\t");
    
    String naid = parts[0];
    String objectIdentifier = parts[1];
    String accessFilename =  parts[4];
    
    URI uri = getURI(accessFilename);
    
    if (uri == null){
      return;
    }
    
    OpaStorage opaStorage = new OpaStorage(naid);
    
    String contentFilename = opaStorage.getContentFilename(uri);
    File contentFile = new File(contentFilename);
    
    String fileSize = contentFile.exists() ? String.valueOf(contentFile.length()) : "";

    getResults().add(String.format("%s,%s,%s\n", naid, objectIdentifier, fileSize));
  }
  
  private URI getURI(String uri){
    try {
      return new URI(uri);
    } catch (URISyntaxException ex) {
      return null;
    }
  }
  
}
