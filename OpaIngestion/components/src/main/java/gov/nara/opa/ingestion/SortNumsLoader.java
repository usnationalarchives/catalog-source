package gov.nara.opa.ingestion;

import java.util.Map;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;
import com.searchtechnologies.aspire.services.Component;
import com.searchtechnologies.aspire.services.Job;
import com.searchtechnologies.aspire.services.logging.ALogger;

public class SortNumsLoader {
  private final ALogger logger;
  private final Job job;
  private final JobInfo jobInfo;
  private final Component component;


  public SortNumsLoader(Component component, Job job) throws AspireException{
    this.logger = (ALogger)component;
    this.job = job;
    this.jobInfo = Jobs.getJobInfo(this.job);
    this.component = component;
  }

  public void execute() throws AspireException{
    loadSortNums();
    loadPrevSortNums();
  }

  private void loadPrevSortNums() throws AspireException{

    logger.debug("Loading previous sort numbers for job %s", job.getJobId());
    
    AspireObject recordFromXmlStore = jobInfo.getRecordFromXmlStore();

    if (recordFromXmlStore != null){
      AspireObject objects = getObjects(recordFromXmlStore);
      
      Map<String, Integer> prevSortNums = jobInfo.createPrevSortNums();
      
      Objects.getSortNums(objects, prevSortNums);      
    }

  }

  private void loadSortNums() throws AspireException{

    logger.debug("Loading sort numbers for job %s", job.getJobId());
    
    AspireObject record = jobInfo.getRecord();

    if (record != null){
      AspireObject objects = getObjects(record);
      
      Map<String, Integer> sortNums = jobInfo.createSortNums();
      
      Objects.getSortNums(objects, sortNums);      
    }

  }

  private AspireObject getObjects(AspireObject record) throws AspireException {
    AspireObject objects = null;

    // Add object elements
    AspireObject dasDigitalObjectArray = record.get("digitalObjectArray");

    if (dasDigitalObjectArray != null &&
        dasDigitalObjectArray.hasChildren()){
      // Start with objects.xml template (empty)
      objects = ObjectsXml.startNewObjects();
      ObjectsXml.addObjectArray(component, objects, record, dasDigitalObjectArray, null);
    }

    return objects;
  }
  
}
