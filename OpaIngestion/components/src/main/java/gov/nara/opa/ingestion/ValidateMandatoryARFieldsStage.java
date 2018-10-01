package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.Job;

public class ValidateMandatoryARFieldsStage extends IngestionStage{

  @Override
  public void process(Job job) throws AspireException {
    JobInfo info = Jobs.getJobInfo(job);
    
    if (info.isAuthorityRecord()){
      String recordType = info.getRecordType();
      
      switch(recordType){
        case Records.PERSON_TAG:
          validatePerson(job);
          break;
        case Records.ORGANIZATION_TAG:
          validateOrganization(job);
          break;
      }
      
      if (Jobs.hasErrors(job)){
        job.setBranch("onError");
      }
    }
  }

  void validatePerson(Job job) throws AspireException {
    Jobs.validateMandatoryField(ARFields.NAME, job, this); 
  }

  void validateOrganization(Job job) throws AspireException {
    Jobs.validateMandatoryField(ARFields.ORGANIZATION_NAME_ARRAY, job, this); 
  }
  
}
