package gov.nara.opa.common.validation.search;

import gov.nara.opa.common.ResultTypeConstants;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class ResultTypesValidator implements ResultTypeConstants {

  private Set<String> resultTypeSet;
  
  public ResultTypesValidator() {
    resultTypeSet = new HashSet<String>();
    
    resultTypeSet.add(RESULT_TYPE_HOLDING);
    resultTypeSet.add(RESULT_TYPE_RECORD_GROUP);
    resultTypeSet.add(RESULT_TYPE_COLLECTION);
    resultTypeSet.add(RESULT_TYPE_FILE_UNIT);
    resultTypeSet.add(RESULT_TYPE_ITEM);
    resultTypeSet.add(RESULT_TYPE_ITEM_AV);
    resultTypeSet.add(RESULT_TYPE_SERIES);
    resultTypeSet.add(RESULT_TYPE_OBJECT);
    resultTypeSet.add(RESULT_TYPE_AUTHORITY);
    resultTypeSet.add(RESULT_TYPE_PERSON);
    resultTypeSet.add(RESULT_TYPE_ORGANIZATION);
    resultTypeSet.add(RESULT_TYPE_TOPICAL_SUBJECT);
    resultTypeSet.add(RESULT_TYPE_GEOGRAPHIC_REFERENCE);
    resultTypeSet.add(RESULT_TYPE_SPECIFIC_RECORDS_TYPE);
    resultTypeSet.add(RESULT_TYPE_TOPICAL_SUBJECT_ALT);
    resultTypeSet.add(RESULT_TYPE_GEOGRAPHIC_REFERENCE_ALT);
    resultTypeSet.add(RESULT_TYPE_SPECIFIC_RECORDS_TYPE_ALT);
    resultTypeSet.add(RESULT_TYPE_ARCHIVES_WEB);
    resultTypeSet.add(RESULT_TYPE_PRESIDENTIAL_WEB);
  }
  
  public boolean isValid(String resultTypeValue) {
    return resultTypeSet.contains(resultTypeValue);
  }
  
}
