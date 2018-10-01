package gov.nara.opa.common;

public class NumericConstants {

  public static final int REASON_TEXT_LENGTH = 50;
  public static final int NA_ID_LENGTH = 50;
  public static final int OBJECT_ID_LENGTH = 50;
  public static final int CONTRIBUTIONS_FILTER_LENGTH = 255;
  public static final int TAG_TEXT_LENGTH = 255;
  public static final int NOTES_LENGTH = 255;
  public static final int LANGUAGE_ISO_LENGTH = 3;
  public static final int OPA_ID_LENGTH = 100;
  /*
   * Due to NARA-1876, a new limit for opaId was set. It will only be used for validating lists creation
   */
  public static final int OPA_ID_LENGTH_FOR_LISTS = 255;
  public static final int LIST_NAME_LENGTH = 50; 
  
}
