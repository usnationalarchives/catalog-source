package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;
import com.searchtechnologies.aspire.services.Job;
import java.util.List;

/**
 * This stage applies to items and fileUnits.
 * These records do not inherit HMS entry number from its parent series, but we
 * need them to their parent series HMS entry number.
 * For these types of records, the HMS entry (a <variantControlNumber> element) 
 * in their parent series is found and copied to their <variantControlNumberArray>
 * element.
 */
public class PopulateHMSEntryNumberStage extends IngestionStage {

  @Override
  public void process(Job job) throws AspireException {
    try {
      addHMSEntryNumbers(job);
    }catch(Throwable error){
      JobInfo info = Jobs.getJobInfo(job);
      error(error, "Populate HMS entry number failed for %s", info.getDescription());
    }
  }  
  
  private void addHMSEntryNumbers(Job job) throws AspireException{
    JobInfo info = Jobs.getJobInfo(job);

    if (!isFileUnitOrItem(info.getRecordType())){
      return;
    }
    
    HMSEntryNumber hmsEntryNumber = new HMSEntryNumber(job);

    List<AspireObject> seriesHMSEntryNumbers = hmsEntryNumber.getSeriesHMSEntryNumbers();
    
    if (seriesHMSEntryNumbers == null || seriesHMSEntryNumbers.isEmpty()){
      return;
    }
    
    AspireObject record = info.getRecord();

    AspireObject variantControlNumberArray = record.get("variantControlNumberArray");
    if (variantControlNumberArray == null){
      variantControlNumberArray = record.add("variantControlNumberArray");
    }
    
    for (AspireObject variantControlNumber : seriesHMSEntryNumbers){
      variantControlNumberArray.add(variantControlNumber);
    }
  }

  private boolean isFileUnitOrItem(String type){
    return type.equals(Records.FILE_UNIT_TAG) ||
            type.equals(Records.ITEM_TAG) ||
            type.equals(Records.ITEM_AV_TAG);
  }
}
