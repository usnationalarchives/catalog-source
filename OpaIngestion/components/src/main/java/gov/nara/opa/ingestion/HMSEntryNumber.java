package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;
import com.searchtechnologies.aspire.services.Job;

import java.util.LinkedList;
import java.util.List;

public class HMSEntryNumber {
  private final JobInfo jobInfo;
  
  public HMSEntryNumber(Job job){
    this.jobInfo = Jobs.getJobInfo(job);
  }

  public List<AspireObject> getSeriesHMSEntryNumbers() throws AspireException {
     String seriesNaId = getParentSeriesNaId();
     return seriesNaId != null ? getHMSEntryNumbers(seriesNaId) : null;
  }
  
  private String getParentSeriesNaId() throws AspireException{
    AspireObject record = jobInfo.getRecord();
    // FileUnit, Item and ItemAv can have
    // 1. A <parentFileUnit> element and within that a <parentSeries> element, or
    // 2. A <parentSeries> element
    AspireObject parentFileUnit = record.get("parentFileUnit");
    return parentFileUnit != null ? getParentSeriesNaId(parentFileUnit) : getParentSeriesNaId(record);
  }

  private String getParentSeriesNaId(AspireObject child) throws AspireException{
    AspireObject parentSeries = child.get("parentSeries");
    return parentSeries != null ? parentSeries.getText("naId") : null;
  }

  private List<AspireObject> getHMSEntryNumbers(String naId) throws AspireException {
    Integer seriesNaId = Integer.parseInt(naId);

    AspireObject series = jobInfo.getRecordInXmlStore(seriesNaId);
    // couldn't find series XML for this series naid
    if( series == null ){
      return null;
    }
    AspireObject variantControlNumberArray = series.get("variantControlNumberArray");
    if (variantControlNumberArray == null){
      return null;
    }
    
    LinkedList<AspireObject> variantControlNumbers = new LinkedList<>();
    for (AspireObject item : variantControlNumberArray.getChildren(true)){
      if (isHmsEntryNumber(item)){
        variantControlNumbers.add(item.clone());
      }
    }
    
    return variantControlNumbers;
  }

  private boolean isHmsEntryNumber(AspireObject item) throws AspireException {
    AspireObject type;
    return
            "variantControlNumber".equals(item.getName())
            && (type = item.get("type")) != null
            && "HMS/MLR Entry Number".equals(type.getText("termName"));
  }
  
}
