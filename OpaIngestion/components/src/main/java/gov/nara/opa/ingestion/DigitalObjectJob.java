package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireObject;
import com.searchtechnologies.aspire.services.Job;

public class DigitalObjectJob {
  private Job job;
  private JobInfo jobInfo;
  private OpaStorage opaStorage;
  private AspireObject digitalObject;
  private String objectSortNumber;
  private String eventLabel;
  
  public void close(){
      job = null;
      jobInfo = null;
      opaStorage = null;
      digitalObject = null;
  }

  public Job getJob() {
    return job;
  }

  public void setJob(Job job) {
    this.job = job;
  }

  public JobInfo getJobInfo() {
    return jobInfo;
  }

  public void setJobInfo(JobInfo jobInfo) {
    this.jobInfo = jobInfo;
  }

  public OpaStorage getOpaStorage() {
    return opaStorage;
  }

  public void setOpaStorage(OpaStorage opaStorage) {
    this.opaStorage = opaStorage;
  }

  public AspireObject getDigitalObject() {
    return digitalObject;
  }

  public void setDigitalObject(AspireObject digitalObject) {
    this.digitalObject = digitalObject;
  }

  public String getObjectSortNumber() {
    return objectSortNumber;
  }

  public void setObjectSortNumber(String objectSortNumber) {
    this.objectSortNumber = objectSortNumber;
  }

  public String getEventLabel() {
    return eventLabel;
  }

  public void setEventLabel(String eventLabel) {
    this.eventLabel = eventLabel;
  }
}
