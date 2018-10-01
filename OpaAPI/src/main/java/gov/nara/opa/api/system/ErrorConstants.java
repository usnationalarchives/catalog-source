package gov.nara.opa.api.system;

/**
 * Added INVALID_SOLR_QUERY. Solr throws an exception when * is specified with a timeout.
 * This currently gets thrown using iapi like this /search?cursorMark=*
 */
public class ErrorConstants {
  public static final String INVALID_CURSOR_MARK="Invalid cursor mark. The cursor must either be '*' or the 'nextCursorMark' returned by a previous search"; 
  public static final String INVALID_SOLR_QUERY="Invalid Solr query. Can not search using both cursorMark and timeAllowed";
  public static final String fieldSizeExceeded = "Text size of field '%1$s' (%2$d) is greater than field size: %3$d";
  public static final String invalidFieldSize = "Text size of field '%1$s' must be %2$d";
  public static final String internalDatabaseError = "Internal database error";
  public static final String invalidAPICall = "The API call type is not valid";
  public static final String notLoggedIn = "You must be logged in to perform this action";
  public static final String invalidParameter = "Invalid parameter";
  public static final String invalidParameterName = "Invalid parameter name";
  public static final String valueNotAllowed = "The value of '%1$s' is not valid";
  public static final String emptyStringValue = "%1$s cannot be empty";
  public static final String internalError = "Internal error";
  public static final String missingParam = "Parameter not provided";
  public static final String invalidUser = "The user does not have rights for that operation";
  public static final String unexpectedParameters = "Unexpected parameters received: %1$s";
  public static final String moderatorRightsNeeded = "Moderator privileges are required to execute this operation";

  public static final String SEARCH_ERROR = "The search engine did not return any results for your specified query";
  public static final String SEARCH_INSERT_ERROR = "The search engine did not return any results for your specified query or the result cannot be annotated";
  public static final String INVALID_ID_VALUE = "Invalid ID value";

  public static final String INVALID_RECORD_TYPE = "The record type entered is not valid";

  public static final String INVALID_ACTION = "Invalid action value. Only 'search' or 'searchWithin' allowed";
  public static final String INVALID_ACTION_VALUE = "Invalid action value.";
  public static final String INVALID_ACTION_SEARCH_ONLY = "Invalid action value. Only 'search' is allowed";
  public static final String INVALID_ACTION_MODIFICATION = "Invalid action value. Only 'adminChange, resetPassword, reactivate' are allowed";
  public static final String INVALID_ACTION_ADMIN_CHANGE_ONLY = "Invalid action value. Only 'adminChange' is allowed";
  public static final String INVALID_ACTION_RESET_PASSWORD_ONLY = "Invalid action value. Only 'resetPassword' is allowed";
  public static final String INVALID_ACTION_REACTIVATE_ONLY = "Invalid action value. Only 'reactivate' is allowed";
  public static final String INVALID_OFFSET_LIMIT = "Invalid offset limit. Offset must be > 0";
  public static final String INVALID_OFFSET_UPPER_LIMIT = "Invalid offset limit. Offset must be < %1$d for this user";
  public static final String INVALID_OFFSET_CURSORMARK = "'offset' and 'cursorMark' are mutually exclusive. Please remove one and try again.";
  
  
  public static final String INVALID_PRINT_ACTION = "Invalid print action value. Only 'print' allowed";
  public static final String INVALID_PRINT_TYPE = "Invalid print type value. Only 'brief' or 'full' allowed";

  public static final String INVALID_USER_TYPE = "Invalid value provided for the user type parameter";
  public static final String INVALID_STATUS = "Invalid status. Only 'active' or 'inactive' allowed";
  public static final String INVALID_ACCOUNT_RIGHTS = "Invalid value provided for the account rights parameter";
  public static final String INVALID_URL = "Invalid Url value";

  public static final String USER_NAME_ALREADY_EXISTS = "The User Name: %1$s is already used for registration. Please choose another User Name.";
  public static final String USER_EMAIL_ALREADY_EXISTS = "The Email Address: %1$s is already used for registration. Please choose another Email Address.";
  public static final String USER_EMAIL_NOT_ALLOWED = "The email change you have requested is not permitted.";
  public static final String USER_NAME_DOES_NOT_EXIST = "The supplied user name does not exist";
  public static final String NO_USERS_FOUND = "No users found based on the specified search criteria.";
  public static final String ACCOUNT_REASON_ID_DOES_NOT_EXIST = "The supplied reason id does not exist";
  public static final String UNAUTHORIZED_VIEW_PROFILE = "Unauthorized to view this user profile (%1$s)";
  public static final String MISSING_REDIRECTION_VALUE = "If either returnUrl or returnText is provided, the other one must be provided as well.";

  public static final String CHANGE_PASSWORD_BOTH_OLD_AND_NEW_PRESENT = "Both the old and the new password need to be supplied to perform a password change";
  public static final String INVALID_OLD_PASSWORD = "The old password does not match the password stored in the system";

  public static final String LOCK_LIMIT_REACHED = "The total number of simultaneously locked files allowed for the user has been reached";
  public static final String LOCK_NOT_FOUND = "Lock record not found";
  public static final String ALREADY_LOCKED = "Record is locked by a different user";

  public static final String INVALID_TAG = "Invalid tag";
  public static final String INVALID_TAG_SIZE = "Tag length of %1$d characters is too long and exceeds the limit of %2$d characters";
  public static final String INVALID_TAG_ALREADY_EXISTS = "The supplied tag(s) already exist: %1$s";
  public static final String INVALID_TAG_EMTPY = "A value needs to be supplied for the text parameter which represents the tag(s) to be added";
  public static final String INVALID_TAG_PAGE_NUMBER = "Page number is required when an object Id is provided";
  public static final String INVALID_TAG_PAGE_NUMBER_NAID_OBJECTID = "Invalid page number value in tag for NaID '%1$s' and ObjectID '%2$s'";
  public static final String INVALID_TAG_JSON = "The provided Json is invalid";
  public static final String EMPTY_TAG_JSON = "The provided Json had no valid tags to add";
  public static final String TAG_IMPORT_ERRORS = "There where errors with some provided tags";

  public static final String INVALID_NOTES_SIZE = "Notes length of %1$d characters is too long and exceeds the limit of %2$d characters";
  
  public static final String INVALID_BACKGROUND_IMAGE_ALREADY_EXISTS = "The background image with the supplied naId and objectId already exist";
  public static final String INVALID_ONLINE_AVAILABILITY_HEADER_ALREADY_EXISTS = "The online availability header with the supplied naId already exist";
  
  public static final String ILLEGAL_CHARACTERS = "%1$s rejected because it contains illegal characters";
  public static final String PAGE_NUMBER_NOT_FOUND = "Page number is required when an objectId is provided";

  public static final String INVALID_ANNOTATION_SIZE = "%1$s length of %2$d characters is too long and exceeds the limit of %3$d characters";
  public static final String INVALID_ANNOTATION_JSON = "The provided Json is invalid";
  public static final String INVALID_PAGE_NUMBER_NAID_OBJECTID = "Invalid page number value for NaID '%1$s' and ObjectID '%2$s'";
  
  public static final String MAX_LIMIT_PRINT = "The estimated no. of records to be printed (%1$d) is larger then the maximum number of records allowed to be printed: %2$s. Please select fewer records to print.";

  public static final String ACTIVE_TAG_NOT_FOUND = "No active tag found based on the supplied parameters";
  public static final String ACTIVE_COMMENT_NOT_FOUND = "No active comment found based on the supplied parameters";
  public static final String COMMENT_NOT_FOUND = "No comment found based on the supplied parameters";
  public static final String ANNOUNCEMENT_NOT_FOUND = "No announcement(s) found";
  public static final String BACKGROUND_IMAGE_NOT_FOUND = "No background image(s) found";
  public static final String ONLINE_AVALIABILITY_HEADER_NOT_FOUND = "No online availability header(s) found";
  public static final String INACTIVE_TAG_NOT_FOUND = "No inactive tag found based on the supplied parameters";
  public static final String TAG_NOT_OWNER = "The requested operation cannot be performed as the current user is not the owner of the tag";
  public static final String COMMENT_NOT_OWNER = "The requested operation cannot be performed as the current user is not the owner of the comment";
  public static final String REASON_ID_DOES_NOT_EXIST = "No annotation reason could be found for the provided reason id (%1$d)";
  public static final String ACTIVE_TAG_ALREADY_EXISTS = "An active tag that matches the supplied parameters already exists";
  public static final String ACTIVE_ACCOUNT_REASON_ALREADY_EXISTS = "A reason you are trying to add already exists";

  public static final String INVALID_USER_ACTIVATION_CODE = "Invalid activation code";
  public static final String INVALID_USER_RESET_CODE = "Invalid reset code";
  public static final String EXPIRED_ACTIVATION_CODE_WINDOW = "Request outside of verification time window";

  public static final String EMAIL_NOT_EXISTS = "The email address submitted is not in the system.";
  public static final String USER_NAME_NOT_EXISTS = "The user name submitted is not in the system.";
  public static final String ACCOUNT_INACTIVE = "The account is inactive";
  public static final String INVALID_USERNAME = "Invalid Username.";
  public static final String CONFIG_FILE_NOT_FOUND = "The Configuration file was not found";
  public static final String INVALID_QUERY_COMBINATION = "Only one of these types of queries can be executed: query for a user list, query for a list"
      + " of export ids or a general query. You specified more then one type from these 3 types of queries.";
  public static final String MISSING_QUERY = "Parameter(s) for one of these types of queries need to be provided: query for a user list, query for a list"
      + " of export ids or a general query.";

  public static final String NO_LIST_FOR_ANONYMOUS = "The listName parameter can not be specified for an un-authenticated user session.";
  public static final String INVALID_LIST_NAME = "A list with the name - %1$s - does not exist for this user name: %2$s.";
  public static final String ROWS_MISSING = "The rows parameter needs to be provided for a general query.";
  public static final String MAX_ROWS_ALLOWED = " The maximum value for 'rows' is %1$d";
  public static final String MAX_LIMIT_SEARCH = "The estimated no. of records for the search is larger then the maximum number of records allowed to be returned for this user type: %1$s.";
  public static final String MAX_LIMIT_EXPORTS = "The estimated no. of records for the export (%1$d) is larger then the maximum number of records allowed to be exported: %2$s.";
  public static final String MAX_LIMIT_NON_BULK_EXPORTS = "The estimated no. of records for the export (%1$d) is larger then the maximum number of records allowed to be exported in a non-bulk export: %2$s. Please click continue if you would like to perform this export as a bulk export.";
  public static final String BULK_EXPORT_CONTENT = "The bulk export content parameter can not be specified if bulkExport=false";
  public static final String UNAUTHORIZED_BULK_EXPORT_REQUEST = "An un-authenticated user cannot perform bulk exports. Please login or create an account and try again.";
  public static final String EXPORT_EXCEEDED_NON_BULK_TIMEOUT = "The export request took longer than %1$d seconds to execute. This request will be executed as a bulk export. Please check the status of the request on the Bulk Downloads page using this export id: %2$d";
  public static final String EXPORT_EXCEEDED_NON_BULK_FILE_SIZE = "The export file size is: %1$s - it exceeds the non-bulk export limit of %2$s bytes. This request will be executed as a bulk export. Please check the status of the request on the Bulk Downloads page using this export id: %3$d";
  public static final String EXPORT_EXCEEDED_NON_BULK_TIMEOUT_NON_AUTH = "The export request took longer than %1$d seconds to execute. Please login and try the export request again.";
  public static final String EXPORT_EXCEEDED_NON_BULK_FILE_SIZE_NON_AUTH = "The export file size is: %1$s - it exceeds the non-bulk export limit of %2$s bytes. Please login and try the export request again.";

  public static final String RESULTS_NOT_FOUND = "No records found based on the specified criteria.";

  public static final String EXPORT_NOT_FOUND_FOR_EXPORT_ID = "No export file found for export id: %1$d.";
  public static final String EXPORT_NOT_READY = "This export is not ready yet is still processing or has encountered an error.";
  public static final String EXPORT_NOT_FOUND = "No export(s) found.";
  public static final String EXPORT_NOT_AUTHORIZED = "You are not authorized to perform this action for this exportId.";
  public static final String EXPORT_USER_NAME_WO_LIST = "A userName parameter can not be supplied if the listName parameter is not provided.";
  public static final String EXPORT_INVALID_RESULT_FIELDS = "The resultFields parameter cannot be present unless the the export.what parameter includes the metadata value.";
  public static final String EXPORT_MISSING_WHAT = "Export.what is missing";
  public static final String EXPORT_USER_NAME_NOT_PROVIDED = "A userName parameter must be provided too if a listName parameter is supplied.";
  public static final String EXPORT_LIST_USER_NAME_NOT_EXISTS = "A user does not exist in the system for the supplied list userName.";

  public static final String IMPORT_ENTITY_INVALID = "Invalid entity. Only 'tag' and 'transcription' is allowed";

  public static final String USER_ID_TOO_LARGE = "The value of the specified user id is larger then the maximum value allowed: 2,147,483,647";

  public static final String INVALID_TRANSCRIPTION_JSON = "The provided Json is invalid";
  public static final String INVALID_TRANSCRIPTIONS = "The provided Json has invalid transcriptions";
  public static final String INVALID_PAGE_NUMBER = "The page number value is not valid";
  public static final String INVALID_OBJECT_ID = "The object Id value is not valid";
  public static final String INVALID_NA_ID = "The NA Id value is not valid";
  public static final String INVALID_TRANSCRIPTION_PAGE_NUMBER_NAID_OBJECTID = "Invalid page number value in transcription for NaID '%1$s' and ObjectID '%2$s'";
  public static final String INVALID_ID_VALUE_IMPORT = "Invalid ID value '%1$s'";

  public static final String FILE_NOT_FOUND = "File not found: %1$s";
  public static final String INVALID_MIGRATION_ACTION = "Invalid action. Only 'read', 'write' or 'load' are allowed";

  public static final String INVALID_OBJECTS_FILE_FORMAT = "Invalid objects.xml file format";
  
  public static final String INVALID_SEARCH_RESULTS_FIELD = "Invalid result field: %1$s";
  public static final String INVALID_SEARCH_RESULTS_TYPE = "Invalid result type: %1$s";
  public static final String INVALID_SEARCH_QUERY = "Invalid search query: %1$s";
  
  public static final String INVALID_ACTION_FOR_HTTP_METHOD_MESSAGE = "The Action parameter is invalid for the Http method";
  
  public static final String NO_URL_MAPPINGS_FOUND_MESSAGE = "No url mappings found based on the supplied arcId";

  public static final String NO_LANGUAGES_FOUND_MESSAGE = "No languages found";

}