package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.framework.BranchHandlerFactory;
import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;
import com.searchtechnologies.aspire.services.BranchHandler;
import com.searchtechnologies.aspire.services.Job;
import java.util.concurrent.atomic.AtomicInteger;
import org.w3c.dom.Element;

public class DigitalObjectSubJobExtractorStage extends IngestionStage {
  BranchHandler branchHandler;
  
  @Override
  public void initialize(Element config) throws AspireException {
    branchHandler = BranchHandlerFactory.newInstance(config, this); 
  }
  
  @Override
  public void close() {
    if (branchHandler != null){
      branchHandler.close();
    }
  }

  @Override
  public void process(Job job) throws AspireException {
    JobInfo jobInfo = Jobs.getJobInfo(job);
    
    setPropertiesOfJobWithDigitalObjects(job, jobInfo);
    
    DigitalObjectJobPublisher publisher = new DigitalObjectJobPublisher(branchHandler, this, job);
    publisher.processObjects();
  }
  
  private boolean isObject(AspireObject node){
    return ObjectsXml.OBJECTS_XML_OBJECT_ELEMENT.equals(node.getName());
  }
  
  private void setPropertiesOfJobWithDigitalObjects(Job job, JobInfo jobInfo) throws AspireException {
    jobInfo.setSubJobOutstandingCount(new AtomicInteger(getObjectCount(job)));
    jobInfo.setSubJobFailedCount(new AtomicInteger());
    jobInfo.setJob(job);
    jobInfo.setJobData(job.get());
    jobInfo.setParentJob(job.getParentJob());
  }
  
  private int getObjectCount(Job job) throws AspireException{
    AspireObject objects = job.get().get(ObjectsXml.OBJECTS_XML_TOP_LEVEL_ELEMENT);
    int count = 0;    
    for (AspireObject object : objects.getChildren()) {
      if (isObject(object)) {   
        count++;
      }
    }
    return count;
  }
}
