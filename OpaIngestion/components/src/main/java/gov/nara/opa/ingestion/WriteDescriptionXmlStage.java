package gov.nara.opa.ingestion;

import java.io.File;
import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;
import com.searchtechnologies.aspire.services.Job;
import java.io.IOException;
import java.util.UUID;

/**
 * Persists job record to the content directory in Opa Storage.
 */
public class WriteDescriptionXmlStage extends IngestionStage {
	
  @Override
  public void process(Job job) throws AspireException {
    try{
      save(job);
    } catch(Throwable e){
      throw new AspireException("save description xml", e);
    }
  }
  
  private void save(Job job) throws IOException, AspireException{
    JobInfo info = Jobs.getJobInfo(job);		
    
    if (info.isProcessingAnnotations()){
      return;
    }      
    
    AspireObject record = new AspireObject(info.getRecord());
    AspireObject digitalObjectArray = record.get("digitalObjectArray");

    if (digitalObjectArray != null){
      record.removeChildren("digitalObjectArray");
    }

    File description = File.createTempFile("description", ".xml");
    record.writeXml(description, AspireObject.PRETTY);

    OpaStorage opaStorage = info.getOpaStorage();

    String pathToDescriptionXml = info.getPathToDescriptionXml();

    boolean fileHasChanged = !Digests.md5Hex(description).equals(opaStorage.md5Hex(pathToDescriptionXml));

    if (fileHasChanged){
      opaStorage.saveFile(description, pathToDescriptionXml);
    }

    description.delete();

    // delete the description.xml file if it's in the deprecated "TO4" area
    String deprecatedPath = opaStorage.getFullNaIdPathInLive(opaStorage.DESCRIPTION_XML);
    if ( opaStorage.exists(deprecatedPath) ) {
      opaStorage.deleteFiles(deprecatedPath);
    }
  }

}