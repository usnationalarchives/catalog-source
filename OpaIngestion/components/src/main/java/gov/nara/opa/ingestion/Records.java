package gov.nara.opa.ingestion;

import com.google.common.collect.ImmutableList;
import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;

public class Records {  
  public static final String COLLECTION_TAG = "collection";
  public static final String SERIES_TAG = "series";
  public static final String RECORD_GROUP_TAG = "recordGroup";
  public static final String FILE_UNIT_TAG = "fileUnit";
  public static final String ITEM_TAG = "item";
  public static final String ITEM_AV_TAG = "itemAv";
  public static final String ORGANIZATION_TAG = "organization";
  public static final String PERSON_TAG = "person";
  public static final String GEOGRAPHIC_PLACE_NAME_TAG = "geographicPlaceName";
  public static final String SPECIFIC_RECORDS_TYPE_TAG = "specificRecordsType";
  public static final String TOPICAL_SUBJECT_TAG = "topicalSubject";
  public static final String NAID_TAG = "naId";
  public static final String DAS_ITEMS_TAG = "das_items";
  
  public static final ImmutableList<String> ARCHIVAL_DESCRIPTION_TAGS = 
    ImmutableList.of(COLLECTION_TAG, SERIES_TAG, RECORD_GROUP_TAG, FILE_UNIT_TAG, ITEM_TAG, ITEM_AV_TAG);
  
  public static final ImmutableList<String> AUTHORITY_RECORD_TAGS = 
    ImmutableList.of(ORGANIZATION_TAG, PERSON_TAG, GEOGRAPHIC_PLACE_NAME_TAG, SPECIFIC_RECORDS_TYPE_TAG, TOPICAL_SUBJECT_TAG);
  
  public static final ImmutableList<String> RECORD_TAGS = 
    new ImmutableList.Builder<String>()
    .addAll(ARCHIVAL_DESCRIPTION_TAGS)
    .addAll(AUTHORITY_RECORD_TAGS)
    .build();
    
  public static String findRecordType(final AspireObject doc) throws AspireException {
    for (String tag : RECORD_TAGS){
      if (doc.get(tag) != null){
        return tag;
      }
    }    
    return null; 
  }
  
  /**
   * Tests whether the record within the <code>doc</code> parameter contains
   * a child whose name matches the value of the <code>elementName</code> 
   * parameter.
   * @param elementName
   * @param doc
   * @return
   * @throws AspireException
   */
  public static boolean hasChild(String elementName, AspireObject doc) throws AspireException{
    return doc.get(elementName) != null;
  }
}
