package gov.nara.opa.common.validation.print;

import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.annotation.HttpParameterName;
import gov.nara.opa.architecture.web.validation.constraint.OpaConstrainedStringList;
import gov.nara.opa.architecture.web.validation.constraint.OpaNotNullAndNotEmpty;
import gov.nara.opa.common.valueobject.export.AccountExportValueObjectConstants;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class PrintResultsRequestParameters extends AbstractRequestParameters
    implements AccountExportValueObjectConstants {

  // public static final String LIST_NAME_PARAM_NAME = "listName";
  public static final String PRINT_TYPE_PARAM_NAME = "export.type";
  public static final String PRINT_WHAT_PARAM_NAME = "export.what";
  public static final String PRINT_FORMAT_PARAM_NAME = "export.format";
  // public static final String BULK_EXPORT_PARAM_NAME = "export.bulk";
  public static final String BULK_EXPORT_CONTENT_PARAM_NAME = "export.bulk.content";
  // public static final String PROCESSING_HINT_PARAM_NAME = "processingHint";

  public static final String SORT_PARAM_NAME = "sort";
  public static final String ROWS_HTTP_PARAM_NAME = "rows";
  public static final String OFFSET_HTTP_PARAM_NAME = "offset";
  public static final String RESULT_FIELDS_HTTP_PARAM_NAME = "resultFields";

  // private String listName;

  @OpaNotNullAndNotEmpty
  @HttpParameterName(PRINT_TYPE_PARAM_NAME)
  private String printType;

  @OpaNotNullAndNotEmpty
  @HttpParameterName(PRINT_WHAT_PARAM_NAME)
  private ArrayList<String> exportWhat;

  @OpaNotNullAndNotEmpty
  @HttpParameterName(PRINT_FORMAT_PARAM_NAME)
  private String exportFormat;

  private Integer rows = 20;

  private Integer offset = 0;

  // private String processingHint;

  private Integer waitTime;

  // @HttpParameterName(BULK_EXPORT_PARAM_NAME)
  // private Boolean bulkExport = false;

  @OpaConstrainedStringList(allowedValues = { "objects", "thumbnails" })
  @HttpParameterName(BULK_EXPORT_CONTENT_PARAM_NAME)
  private ArrayList<String> bulkExportContent;

  private String sort;

  private Map<String, String[]> queryParameters;

  @Override
  public LinkedHashMap<String, Object> getAspireObjectContent(String action) {
    LinkedHashMap<String, Object> aspireContent = new LinkedHashMap<String, Object>();
    // aspireContent.put(arg0, arg1)
    return aspireContent;
  }

  // public String getListName() {
  // return listName;
  // }

  // public void setListName(String listName) {
  // this.listName = listName;
  // }

  public String getPrintType() {
    return printType;
  }

  public void setPrintType(String printType) {
    this.printType = printType;
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

  // public String getProcessingHint() {
  // return processingHint;
  // }

  // public void setProcessingHint(String processingHint) {
  // this.processingHint = processingHint;
  // }

  public Integer getWaitTime() {
    return waitTime;
  }

  public void setWaitTime(Integer waitTime) {
    this.waitTime = waitTime;
  }

  // public Boolean getBulkExport() {
  // return bulkExport;
  // }

  // public void setBulkExport(Boolean bulkExport) {
  // this.bulkExport = bulkExport;
  // }

  public ArrayList<String> getBulkExportContent() {
    return bulkExportContent;
  }

  public void setBulkExportContent(ArrayList<String> bulkExportContent) {
    this.bulkExportContent = bulkExportContent;
  }

  @Override
  public boolean bypassExtraneousHttpParametersValidation() {
    return true;
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

}
