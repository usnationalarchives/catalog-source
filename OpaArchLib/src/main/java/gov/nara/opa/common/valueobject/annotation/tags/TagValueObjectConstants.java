package gov.nara.opa.common.valueobject.annotation.tags;

import gov.nara.opa.architecture.web.valueobject.CommonValueObjectConstants;

public interface TagValueObjectConstants extends CommonValueObjectConstants {
  // Constants for the ANNOTATION_TAGS table
  public static final String ANNOTATION_ID_DB = "ANNOTATION_ID";
  public static final String ANNOTATION_DB = "ANNOTATION";
  public static final String ANNOTATION_MD5_DB = "ANNOTATION_MD5";
  public static final String STATUS_DB = "STATUS";
  public static final String NA_ID_DB = "NA_ID";
  public static final String OBJECT_ID_DB = "OBJECT_ID";
  public static final String PAGE_NUM_DB = "PAGE_NUM";
  public static final String OPA_ID_DB = "OPA_ID";
  public static final String ACCOUNT_ID_DB = "ACCOUNT_ID";
  public static final String ANNOTATION_TS_DB = "ANNOTATION_TS";

  public static final String ANNOTATION_ID_ASP = "@annotationId";
  public static final String ANNOTATION_TAG_ASP = "@text";
  public static final String STATUS_ASP = "@status";
  public static final String NA_ID_ASP = "@naId";
  public static final String OBJECT_ID_ASP = "@objectId";
  public static final String PAGE_NUM_ASP = "@pageNum";
  public static final String OPA_ID_ASP = "@opaId";
  public static final String ACCOUNT_ID_ASP = "@accountId";
  public static final String ANNOTATION_CREATED_TS_ASP = "@created";
  public static final String ANNOTATION_DELETED_TS_ASP = "@deleted";
  public static final String ANNOTATION_RESTORED_TS_ASP = "@restored";

  public static final String USER_NAME_ASP = "@user";
  public static final String FULL_NAME_ASP = "@fullName";
  public static final String DISPLAY_FULL_NAME = "@displayFullName";
  public static final String IS_NARA_STAFF_ASP = "@isNaraStaff";

  public static final String NA_ID_REQ_ASP = "naId";
  public static final String OBJECT_ID_REQ_ASP = "objectId";
  public static final String PAGE_NUM_REQ_ASP = "pageNum";
  public static final String TAG_TEXT_REQ_ASP = "tagText";

  public static final String CREATE_TAG_ACTION = "save";
  public static final String DELETE_TAG_ACTION = "deleteTag";
  public static final String VIEW_TAG_ACTION = "";
  public static final String REMOVE_TAG_ACTION = "removeTag";
  public static final String RESTORE_TAG_ACTION = "restoreTag";
}
