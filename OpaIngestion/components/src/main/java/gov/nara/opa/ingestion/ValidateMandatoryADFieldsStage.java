package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;
import com.searchtechnologies.aspire.services.Job;
import static gov.nara.opa.ingestion.Records.hasChild;

public class ValidateMandatoryADFieldsStage extends IngestionStage {

  @Override
  public void process(Job job) throws AspireException {
    JobInfo info = Jobs.getJobInfo(job);
    
    if (info.isArchivalDescription()){
      String recordType = info.getRecordType();

      switch(recordType){
        case Records.COLLECTION_TAG:
          validateCollection(job);
          break;
        case Records.RECORD_GROUP_TAG:
          validateRecordGroup(job);
          break;
        case Records.SERIES_TAG:
          validateSeries(job);
          break;
        case Records.FILE_UNIT_TAG:
        case Records.ITEM_TAG:
        case Records.ITEM_AV_TAG:
          validateFileUnit(job);
          break;
      }

      if (Jobs.hasErrors(job)){
        job.setBranch("onError");
      }
    }
  }
    
  void validateCollection(Job job) throws AspireException {    
    validateTitle(job);
    validateCollectionIdentifier(job);
    validateInclusiveDates(job);
  }

  void validateRecordGroup(Job job) throws AspireException {   
    validateTitle(job);
    validateRecordGroupNumber(job);    
    validateInclusiveDates(job);
  }

  void validateSeries(Job job) throws AspireException {        
    validateTitle(job);
    validateGeneralRecordsTypeArray(job);    
    validateInclusiveDates(job);
    validateAccessRestrictionStatus(job);
    validateUseRestrictionStatus(job);
    validateCreatingIndividualAndCreatingOrganization(job);    
    validatePhysicalOccurrenceElements(job);
  }
  
  void validateFileUnit(Job job) throws AspireException {
    validateTitle(job);
    validateGeneralRecordsTypeArray(job);
    validateAccessRestrictionStatus(job);
    validateUseRestrictionStatus(job);
    validateCopyStatusOfPhysicalOccurrenceElements(job);
  }
 
  void validateMandatory(String fieldName, Job job) throws AspireException {
    Jobs.validateMandatoryField(fieldName, job, this);
  }

  void validateTitle(Job job) throws AspireException {
    validateMandatory(ADFields.TITLE, job);
  }

  void validateCollectionIdentifier(Job job) throws AspireException {
    validateMandatory(ADFields.COLLECTION_IDENTIFIER, job);
  }

  void validateRecordGroupNumber(Job job) throws AspireException {
    validateMandatory(ADFields.RECORD_GROUP_NUMBER, job);
  }

  void validateGeneralRecordsTypeArray(Job job) throws AspireException {
    validateMandatory(ADFields.GENERAL_RECORDS_TYPE_ARRAY, job);
  }

  void validateInclusiveDates(Job job) throws AspireException {
    validateMandatory(ADFields.INCLUSIVE_DATES, job);
  }

  void validateAccessRestrictionStatus(Job job) throws AspireException {
    // Disabled for now, some ADs come with empty accessRestriction
    //validateRestrictionStatus(ADFields.ACCESS_RESTRICTION, job);
  }

  void validateUseRestrictionStatus(Job job) throws AspireException {
    validateRestrictionStatus(ADFields.USE_RESTRICTION, job);
  }
  
  void validateRestrictionStatus(String restrictionFieldName, Job job) throws AspireException {
    JobInfo info = Jobs.getJobInfo(job);
    AspireObject record = info.getRecord();
    AspireObject restriction = record.get(restrictionFieldName);
    
    if (restriction == null || 
        restriction.get(ADFields.STATUS) == null){
      Jobs.addMissingFieldError(String.format("%s/%s", restrictionFieldName, ADFields.STATUS), job, this);
    }    
  }
  
  void validatePhysicalOccurrenceElements(Job job) throws AspireException {
    JobInfo info = Jobs.getJobInfo(job);
    AspireObject record = info.getRecord();
    AspireObject physicalOccurrenceArray = record.get("physicalOccurrenceArray");
    
    if (physicalOccurrenceArray != null){
      for (AspireObject physicalOccurrence : physicalOccurrenceArray.getChildren()){
        if (isPhysicalOccurrence(physicalOccurrence)){          
          validateCopyStatus(job, physicalOccurrence);
          validateExtent(job, physicalOccurrence);
          validateHoldingsMeasurements(job, physicalOccurrence);
        }
      }
    }
  }
  
  boolean isPhysicalOccurrence(AspireObject doc) {
    return doc.getName().endsWith("PhysicalOccurrence");
  }
  
  void addMissingFieldErrorWithElementContent(String fieldName, Job job, AspireObject doc) throws AspireException {
    job.addErrorMessage(this, "Mandatory field '%s' missing in: %s", fieldName, doc.toXmlString(true));
  }

  void validateCopyStatus(Job job, AspireObject doc) throws AspireException {
    if (!hasChild(ADFields.COPY_STATUS, doc)){
      addMissingFieldErrorWithElementContent(ADFields.COPY_STATUS, job, doc);
    }
  }
  
  void validateExtent(Job job, AspireObject doc) throws AspireException {
    if (!hasChild(ADFields.EXTENT, doc)){
      addMissingFieldErrorWithElementContent(ADFields.EXTENT, job, doc);
    }
  }
  
  void validateHoldingsMeasurements(Job job, AspireObject doc) throws AspireException {
    AspireObject holdingsMeasurementArray = doc.get("holdingsMeasurementArray");    
    if (holdingsMeasurementArray != null){
      for (AspireObject holdingsMeasurement : holdingsMeasurementArray.getAll("holdingsMeasurement")){
        validateHoldingsMeasurement(job, holdingsMeasurement);
      }
    }
  }

  void validateHoldingsMeasurement(Job job, AspireObject doc) throws AspireException {
    if (!hasChild(ADFields.COUNT, doc)){
      addMissingFieldErrorWithElementContent(ADFields.COUNT, job, doc);
    }
    
    if (!hasChild(ADFields.TYPE, doc)){
      addMissingFieldErrorWithElementContent(ADFields.TYPE, job, doc);
    }   
  }
  
  private void validateCopyStatusOfPhysicalOccurrenceElements(Job job) throws AspireException {
    AspireObject doc = job.get();
    AspireObject physicalOccurrenceArray = doc.get("physicalOccurrenceArray");
    
    if (physicalOccurrenceArray != null){
      for (AspireObject physicalOccurrence : physicalOccurrenceArray.getChildren()){
        if (isPhysicalOccurrence(physicalOccurrence)){          
          validateCopyStatus(job, physicalOccurrence);
        }
      }
    }
  }
  
  void validateCreatingIndividualAndCreatingOrganization(Job job) throws AspireException{
    JobInfo info = Jobs.getJobInfo(job);
    AspireObject record = info.getRecord();

    if (!hasChild(ADFields.CREATING_INDIVIDUAL_ARRAY, record) && 
        !hasChild(ADFields.CREATING_ORGANIZATION_ARRAY, record)){
      job.addErrorMessage(this, "Mandatory fields missing. Either '%s' or '%s' must be specified.", 
        ADFields.CREATING_INDIVIDUAL_ARRAY, ADFields.CREATING_ORGANIZATION_ARRAY);
    }
  }
}
