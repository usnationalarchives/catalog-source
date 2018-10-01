package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;
import com.searchtechnologies.aspire.services.Component;
import com.searchtechnologies.aspire.services.Job;
import java.util.List;

public class Jobs {

  static final String JOB_INFO_VAR = "job-info";
  
  public static boolean hasErrors(Job job) throws AspireException{
    List<AspireObject> errors = job.getResultForUse().getAll("error");
    return errors != null && errors.size() > 0;
  }  
  
  public static void validateMandatoryField(String fieldName, Job job, Component component) throws AspireException{
    JobInfo info = Jobs.getJobInfo(job);
    AspireObject record = info.getRecord();
    if (!Records.hasChild(fieldName, record)){
      addMissingFieldError(fieldName, job, component);
    }
  }
  
  public static void addMissingFieldError(String fieldName, Job job, Component component) {
    job.addErrorMessage(component, "Mandatory field '%s' is missing", fieldName);
  }
          
  public static JobInfo addJobInfo(Job job){
    JobInfo jobInfo = new JobInfo();
    job.putVariable(JOB_INFO_VAR, jobInfo);
    return jobInfo;
  }
  
  public static void setJobInfo(Job job, JobInfo jobInfo){
    job.putVariable(JOB_INFO_VAR, jobInfo);
  }
  
  public static JobInfo getJobInfo(Job job){
    return (JobInfo)job.getVariable(JOB_INFO_VAR);
  }
}
