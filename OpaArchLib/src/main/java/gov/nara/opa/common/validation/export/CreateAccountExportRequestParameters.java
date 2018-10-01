package gov.nara.opa.common.validation.export;

import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.annotation.HttpParameterName;
import gov.nara.opa.architecture.web.validation.constraint.OpaConstrainedStringList;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;
import gov.nara.opa.common.services.SingletonServices;
import gov.nara.opa.common.valueobject.export.AccountExportValueObjectConstants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class CreateAccountExportRequestParameters extends
    AbstractRequestParameters implements AccountExportValueObjectConstants {

  public static final String LIST_NAME_PARAM_NAME = "listName";
  public static final String EXPORT_TYPE_PARAM_NAME = "export.type";
  public static final String EXPORT_WHAT_PARAM_NAME = "export.what";
  public static final String EXPORT_FORMAT_PARAM_NAME = "export.format";
  public static final String BULK_EXPORT_PARAM_NAME = "export.bulk";
  public static final String BULK_EXPORT_CONTENT_PARAM_NAME = "export.bulk.content";
  public static final String PROCESSING_HINT_PARAM_NAME = "processingHint";

  public static final String SORT_PARAM_NAME = "sort";
  public static final String ROWS_HTTP_PARAM_NAME = "rows";
  public static final String OFFSET_HTTP_PARAM_NAME = "offset";
  public static final String RESULT_FIELDS_HTTP_PARAM_NAME = "resultFields";

  public static final String ALL_RESULT_FIELDS = "level,type,naId,parentDescriptionNaId,opaId,title,url,teaser,webArea,webAreaUrl,description,"
      + "authority,objects,publicContributions";


  public static final String DEFAULT_EXPORT_TYPE = "brief";
  public static final String DEFAULT_EXPORT_FORMAT = "json";
  public static final ArrayList<String> DEFAULT_EXPORT_WHAT = new ArrayList<String>();
  { DEFAULT_EXPORT_WHAT.add("metadata"); }

  private String listName;

  @OpaNotNullAndNotEmpty
  @HttpParameterName(EXPORT_TYPE_PARAM_NAME)
  private String exportType = DEFAULT_EXPORT_TYPE;

  @OpaNotNullAndNotEmpty
  @HttpParameterName(EXPORT_WHAT_PARAM_NAME)
  private ArrayList<String> exportWhat = DEFAULT_EXPORT_WHAT;

  @HttpParameterName(EXPORT_FORMAT_PARAM_NAME)
  private String exportFormat = DEFAULT_EXPORT_FORMAT;

  private Integer rows = 200;

  private Integer offset;

  private String processingHint;

  private Integer waitTime;

  @HttpParameterName(BULK_EXPORT_PARAM_NAME)
  private Boolean bulkExport = false;

  @OpaConstrainedStringList(allowedValues = { "objects", "thumbnails" })
  @HttpParameterName(BULK_EXPORT_CONTENT_PARAM_NAME)
  private ArrayList<String> bulkExportContent;

  private String sort;

  private Map<String, String[]> queryParameters;

  private String userName;

  private Boolean recordLine = false;

  private static final Set<String> EXPORT_SPECIFIC_PARAMETERS = new HashSet<String>();

  static {
    EXPORT_SPECIFIC_PARAMETERS.add("export.ids");
  }

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    LinkedHashMap<String, Object> aspireContent = new LinkedHashMap<String, Object>();
    // aspireContent.put(arg0, arg1)
    return aspireContent;
  }

  public String getListName() {
    return listName;
  }

  public void setListName(String listName) {
    this.listName = listName;
  }

  public String getExportType() {
    return exportType;
  }

  public void setExportType(String exportType) {
    this.exportType = exportType;
  }

  public ArrayList<String> getExportWhat() {
    return exportWhat;
  }

  public void setExportWhat(ArrayList<String> exportWhat) {
    this.exportWhat = exportWhat;
  }

  public String getExportFormat() {
    return exportFormat;
  }

  public void setExportFormat(String exportFormat) {
    this.exportFormat = exportFormat;
  }

  public Integer getRows() {
    return rows;
  }

  public void setRows(Integer rows) {
    this.rows = rows;
  }

  public String getProcessingHint() {
    return processingHint;
  }

  public void setProcessingHint(String processingHint) {
    this.processingHint = processingHint;
  }

  public Integer getWaitTime() {
    return waitTime;
  }

  public void setWaitTime(Integer waitTime) {
    this.waitTime = waitTime;
  }

  public Boolean getBulkExport() {
    return bulkExport;
  }

  public void setBulkExport(Boolean bulkExport) {
    this.bulkExport = bulkExport;
  }

  public ArrayList<String> getBulkExportContent() {
    return bulkExportContent;
  }

  public void setBulkExportContent(ArrayList<String> bulkExportContent) {
    this.bulkExportContent = bulkExportContent;
  }

  @Override
  public boolean isInWhiteList(String parameterName) {
    if (AbstractRequestParameters.INTERNAL_API_TYPE.equals(getApiType())) {
      return true;
    }
    boolean inWhiteList = SingletonServices.SOLR_FIELDS_WHITE_LIST
        .contains(parameterName);
    if (inWhiteList) {
      return true;
    }

    inWhiteList = EXPORT_SPECIFIC_PARAMETERS.contains(parameterName);
    if (inWhiteList) {
      return true;
    }

    // TODO: add "ends with" list
    // to avoid having to create a new list for testing, we'll use STARTS_WITH
    for(Object suffixObj : SingletonServices.SOLR_FIELDS_STARTS_WITH_LIST.toArray()) {
      String suffix = suffixObj.toString();
      if(parameterName.endsWith(suffix)) {
        return SingletonServices.DAS_WHITE_LIST.contains(parameterName.replace(suffix,""));
      }
    }

    return SingletonServices.DAS_WHITE_LIST.contains(parameterName);
  }

  public Integer getOffset() {
    return offset;
  }

  public void setOffset(Integer offset) {
    this.offset = offset;
  }

  public String getSort() {
    return sort;
  }

  public void setSort(String sort) {
    this.sort = sort;
  }

  public Map<String, String[]> getQueryParameters() {
    return queryParameters;
  }

  public void setQueryParameters(Map<String, String[]> queryParameters) {
    this.queryParameters = queryParameters;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public Boolean getRecordLine() {
    return recordLine;
  }

  public void setRecordLine(Boolean recordLine) {
    this.recordLine = recordLine;
  }

}
