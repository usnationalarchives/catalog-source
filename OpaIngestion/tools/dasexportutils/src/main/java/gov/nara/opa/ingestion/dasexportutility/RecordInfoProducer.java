package gov.nara.opa.ingestion.dasexportutility;

import java.nio.file.Path;
import java.util.Queue;
import org.jdom2.Element;


public class RecordInfoProducer extends DasRecordWorker{

  public RecordInfoProducer(Path file, Queue<String> results) {
    super(file, results);
  }

  @Override
  public void process() throws Exception {    
    Element rootElement = getDocument().getRootElement();

    String type = rootElement.getName();
    if (!Records.RECORD_TAGS.contains(type)) {
      return;
    }
        
    String naid = rootElement.getChild("naId", rootElement.getNamespace()).getText();

    int digitalObjectCount = 0;
    
    Element digitalObjectArray = rootElement.getChild("digitalObjectArray", rootElement.getNamespace());
    
    if (digitalObjectArray != null){
      digitalObjectCount = digitalObjectArray.getChildren("digitalObject", rootElement.getNamespace()).size();
    }
      
    String line = String.format("%s,%s,%d\n", naid, type, digitalObjectCount);
    getResults().add(line);
  }	  
}