package gov.nara.opa.ingestion.dasexportutility;

import java.nio.file.Path;
import java.util.Queue;
import org.jdom2.Element;
import org.jdom2.Namespace;

public class ObjectInfoProducer extends DasRecordWorker{
  public ObjectInfoProducer(Path file, Queue<String> results) {
    super(file, results);
  }

  @Override
  public void process() throws Exception {    
    Element rootElement = getDocument().getRootElement();
    Namespace namespace = rootElement.getNamespace();

    String type = rootElement.getName();
    if (!Records.RECORD_TAGS.contains(type)) {
      return;
    }
        
    String naid = rootElement.getChildText("naId", namespace);
   
    Element digitalObjectArray = rootElement.getChild("digitalObjectArray", namespace);

    if (digitalObjectArray == null){
      return;
    }
    
    for (Element digitalObject : digitalObjectArray.getChildren("digitalObject", namespace)){
      String objectIdentifier = digitalObject.getChildText("objectIdentifier", namespace);
      
      String accessFileSizeText = digitalObject.getChildText("accessFileSize", namespace);
      String accessFileSize = accessFileSizeText != null ? accessFileSizeText : "0";
      
      String termName = digitalObject.getChild("objectType", namespace).getChildText("termName", namespace);
      
      String accessFilenameText = digitalObject.getChildText("accessFilename", namespace);
      String accessFilename = accessFilenameText != null ? accessFilenameText.replace('\n', ' ') : "";
      
      String line = String.format("%s\t%s\t%s\t%s\t%s\n", naid, objectIdentifier,
              accessFileSize, termName, accessFilename);
      
      getResults().add(line);
    }    
  }	  
}
