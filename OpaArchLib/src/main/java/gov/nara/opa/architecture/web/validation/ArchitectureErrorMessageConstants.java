package gov.nara.opa.architecture.web.validation;

public class ArchitectureErrorMessageConstants {
  
  public static final String INVALID_USER_NAME = "Invalid username";
  public static final String TEXT_FIELD_LENGTH_RANGE = "The length of text field '%1$s' must be between %2$d and %3$d characters";
  public static final String INVALID_FORMAT = "The accepted values for the format parameter are json or xml.";
  public static final String INVALID_FORMAT_EXPORT = "The accepted values for the format parameter are json, xml, pdf, txt or csv.";
  public static final String INVALID_API_TYPE = "The API Type specified in the URL path is invalid. The only valid values are 'iapi' and 'api'";
  public static final String INVALID_API_SINGLE = "The API Type specified in the URL path is invalid. The only valid value is '%1$s'";
  public static final String MULTIPLE_PARAMETERS = "Multiple query parameters with the same parameter name '%1$s' are not allowed.";
  public static final String INTEGER_TYPE_MISMATCH = "The supplied value for parameter %1$s can not be converted to an integer.";
  public static final String INVALID_PASSWORD = "Invalid Password - Password must contain a minimum of 8 characters.";
  public static final String INVALID_NEW_PASSWORD = "Invalid New Password - Password must contain a minimum of 8 characters and a maximum of 32.";
  public static final String WRONG_PASSWORD = "Your password is incorrect";
  public static final String NOT_NULL_AND_NOT_EMPTY = "Please fill in %1$s";
  public static final String INVALID_EMAIL = "Your email is not in valid format";
  public static final String IS_NOT_CONTAINED_IN_LIST = "One of the supplied values for parmater (%1$s) is not part of the following allowed values: %2$s";
  public static final String EXCEEDS_SIZE = "The size of the value for the supplied paramater (%1$s) needs to be between %2$d and %3$d";
  public static final String EXCEEDS_SIZE_MAX_INT = "The size of the value for the supplied paramater (%1$s) cannot be larger than %3$d";
  public static final String EXCEEDS_SIZE_MIN_INT = "The size of the value for the supplied paramater (%1$s) must be larger than %2$d";
  public static final String EXCEEDS_SIZE_MAX_CHAR = "The size of the value for the supplied paramater (%1$s) cannot be longer than %3$d characters";
  public static final String EXCEEDS_SIZE_MIN_CHAR = "The size of the value for the supplied paramater (%1$s) must be larger than %2$d characters";
  public static final String EXCEEDS_VALUE_MIN_INT = "The value for the supplied paramater (%1$s) must be larger than %2$d";
  public static final String INVALID_PATTERN = "The supplied value paramater (%1$s) does not match pattern the regex pattern %3$s";
  public static final String INVALID_VALUE = "The value for the supplied paramater (%1$s) must be %2$s. %3$s";
  public static final String INVALID_DATE_VALUE = "The value for the supplied paramater (%1$s) is not an accepted date (YYYY-MM-DD)";
  public static final String INVALID_DATE_PART_VALUE = "The value for the supplied paramater (%1$s) is not an accepted date part and is not within the accepted range: %2$d - %3$d";
  public static final String INSUFFICIENT_RIGHTS = "The user attempting to %1$s has insufficient rights %2$s";
  public static final String INVALID_SIZE = "The size of the value for the supplied parameter (%1$s) needs to be %2$d";

  public static final String INVALID_INTEGER = "Invalid %1$s value.";
  public static final String INVALID_BOOLEAN = "Invalid %1$s value.";

  public static final String INVALID_SORT_INSTRUCTION = "Invalid sort instruction. Its value should have the format 'fieldName ASC|DESC'";
  public static final String INVALID_SORT_FIELD_NAME = "Invalid sort field name (%1$s). Allowed names are: %2$s";
  public static final String INVALID_SORT_DIRECTION = "Invalid sort direction. Allowed values are: ASC or DESC";
  public static final String TIMEOUT_REACHED = "This search has lasted more than (%1$d) seconds";

  public static final String NO_RECORDS_FOUND = "No %1$s found.";

  public static final String UNKOWN_ACTION = "unknownAction";
  public static final String UNKOWN_ERROR_MESSAGE = "Unkown error message";
  public static final String NO_ASSOCIATED_RESOURCE = "There is no resource associated with the requested path";
  public static final String FORBIDDEN_ACCESS = "Access to this resource not allowed.";
  public static final String PARAMETER_NOT_ALLOWED = "The http request parameter - %1$s - is not allowed. Please remove this parameter and resubmit the request.";

  public static final String XSS_VULNERABLE = "The request is XSS vulnerable and is not permitted";
  
  public static final String SEARCH_SERVER_UNAVAILABLE = "Search server unavailable.  Contact your system administrator.";
  public static final String SEARCH_SERVER_OUT_OF_MEMORY = "Insufficient server resources.  Contact your system administrator.";
  public static final String SEARCH_SERVER_INTERNAL_ERROR = "Internal server error.  Contact your system administrator.";
  
  public static final String NOT_FOUND = "No results were returned";
  public static final String DUPLICATE_RECORD = "Duplicate record";
}
