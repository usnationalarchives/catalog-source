package gov.nara.opa.common.valueobject.export;

import gov.nara.opa.architecture.web.valueobject.CommonValueObjectConstants;

public interface AccountExportValueObjectConstants extends
    CommonValueObjectConstants {

  // ACCOUNTS_EXPORTS columns
  public static final String EXPORT_ID_DB = "EXPORT_ID";
  public static final String ACCOUNT_ID_DB = "ACCOUNT_ID";
  public static final String EXPORT_TYPE_DB = "EXPORT_TYPE";
  public static final String EXPORT_NAME_DB = "EXPORT_NAME";
  public static final String LIST_NAME_DB = "LIST_NAME";
  public static final String BULK_EXPORT_DB = "BULK_EXPORT";
  public static final String BULK_EXPORT_CONTENT_DB = "BULK_EXPORT_CONTENT";
  public static final String INCLUDE_THUMBNAILS_DB = "INCLUDE_THUMBNAILS";
  public static final String INCLUDE_EMBEDDED_THUMBNAILS_DB = "INCLUDE_EMBEDDED_THUMBNAILS";
  public static final String INCLUDE_COMMENTS_DB = "INCLUDE_COMMENTS";
  public static final String INCLUDE_METADATA_DB = "INCLUDE_METADATA";
  public static final String INCLUDE_TAGS_DB = "INCLUDE_TAGS";
  public static final String INCLUDE_TRANSCRIPTIONS_DB = "INCLUDE_TRANSCRIPTIONS";
  public static final String INCLUDE_TRANSLATIONS_DB = "INCLUDE_TRANSLATIONS";
  public static final String INCLUDE_CONTENT_DB = "INCLUDE_CONTENT";
  public static final String EXPORT_FORMAT_DB = "EXPORT_FORMAT";
  public static final String API_TYPE_DB = "API_TYPE";
  public static final String URL_DB = "URL";
  public static final String QUERY_PARAMETERS_DB = "QUERY_PARAMETERS";
  public static final String REQUEST_STATUS_DB = "REQUEST_STATUS";
  public static final String ERROR_MESSAGE_DB = "ERROR_MESSAGE";
  public static final String PROCESSING_HINT_DB = "PROCESSING_HINT";
  public static final String REQUEST_TS_DB = "REQUEST_TS";
  public static final String COMPLETED_TS_DB = "COMPLETED_TS";
  public static final String LAST_ACTION_TS_DB = "LAST_ACTION_TS";
  public static final String SERVER_INSTANCE_ID_DB = "SERVER_INSTANCE_ID";
  public static final String SPRING_JOB_EXECUTION_ID_DB = "SPRING_JOB_EXECUTION_ID";
  public static final String OFFSET_DB = "OFFSET";
  public static final String ROWS_DB = "ROWS";
  public static final String TOTAL_RECS_PROCESSED_DB = "TOTAL_RECS_PROCESSED";
  public static final String TOTAL_ERRORS_DB = "TOTAL_ERRORS";
  public static final String TOTAL_SKIPPED_DB = "TOTAL_SKIPPED";
  public static final String TOTAL_RECS_TO_BE_PROCESSED_DB = "TOTAL_RECS_TO_BE_PROCESSED";
  public static final String SORT_DB = "SORT";
  public static final String FILE_SIZE_DB = "FILE_SIZE";
  public static final String EXPIRES_TS_DB = "EXPIRES_TS";

  public static final String EXPORT_IDS_HTTP_PARAM_NAME = "export.ids";
  public static final String LIST_NAME_HTTP_PARAM_NAME = "listName";
  public static final String USER_NAME_HTTP_PARAM_NAME = "userName";
  public static final String NAIDS_HTTP_PARAM_NAME = "naIds";
  public static final String SORT_HTTP_PARAM_NAME = "sort";
  public static final String EXCLUDE_RESULT_TYPES_HTTP_PARAM_NAME = "excludeResultTypes";
  public static final String ACTION_HTTP_PARAM_NAME = "action";
  public static final String LANGUAGES_HTTP_PARAM_NAME = "export.languages";
  public static final String LANGUAGES_ALL_HTTP_PARAM_NAME = "export.languages.all";

  public static final String EXPORT_ID_ASP = "exportId";
  public static final String EXPORT_NAME_ASP = "exportName";
  public static final String EXPORT_STATUS_ASP = "status";
  public static final String EXPORT_FORMAT_ASP = "exportFormat";
  public static final String EXPORT_PERCENTAGE_COMPLETE_ASP = "percentageComplete";
  public static final String EXPORT_REQUEST_TS_ASP = "requestTs";
  public static final String EXPORT_DOWNLOAD_URL_ASP = "downloadUrl";
  public static final String EXPORT_BULK_EXPORT_ASP = "bulkExport";
  public static final String EXPORT_BULK_EXPIRES_ASP = "expiresTs";
  public static final String EXPORT_FILE_SIZE_ASP = "fileSize";
  public static final String EXPORT_ERROR_MESSAGE_ASP = "errorMessage";
  public static final String EXPORT_TOTAL_PROCESSED_RECORDS_ASP = "totalProcesedRecords";

  public static final int CALLER_TYPE_EXPORT = 1;
  public static final int CALLER_TYPE_PRINT = 2;
  public static final int CALLER_TYPE_SEARCH = 3;
}
