package gov.nara.opa.ingestion.dasexportutility;

import java.util.Arrays;
import java.util.List;

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
  
  public static final List<String> RECORD_TAGS = Arrays.asList(
          COLLECTION_TAG, SERIES_TAG, RECORD_GROUP_TAG, FILE_UNIT_TAG, ITEM_TAG, ITEM_AV_TAG,
          ORGANIZATION_TAG, PERSON_TAG, GEOGRAPHIC_PLACE_NAME_TAG, SPECIFIC_RECORDS_TYPE_TAG, TOPICAL_SUBJECT_TAG);
}
