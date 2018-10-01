package gov.nara.opa.common.dataaccess.impl.export;

import gov.nara.opa.common.valueobject.export.AccountExportStatusEnum;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.common.valueobject.export.AccountExportValueObjectConstants;
import gov.nara.opa.common.valueobject.export.AccountExportValueObjectHelper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class AccountExportValueObjectExtractor implements
    ResultSetExtractor<AccountExportValueObject>,
    AccountExportValueObjectConstants {

  @Override
  public AccountExportValueObject extractData(ResultSet rs) throws SQLException {
    AccountExportValueObject row = new AccountExportValueObject();

    Integer accountId = rs.getInt(ACCOUNT_ID_DB);
    if (accountId != null && accountId != 0) {
      row.setAccountId(rs.getInt(ACCOUNT_ID_DB));
    }
    row.setBulkExport(rs.getBoolean(BULK_EXPORT_DB));
    row.setCompletedTs(rs.getTimestamp(COMPLETED_TS_DB));
    row.setExportFormat(rs.getString(EXPORT_FORMAT_DB));
    row.setApiType(rs.getString(API_TYPE_DB));
    row.setExportId(rs.getInt(EXPORT_ID_DB));
    row.setUrl(rs.getString(URL_DB));
    row.setIncludeComments(rs.getBoolean(INCLUDE_COMMENTS_DB));
    row.setIncludeContent(rs.getBoolean(INCLUDE_CONTENT_DB));
    row.setIncludeEmbeddedThumbnails(rs
        .getBoolean(INCLUDE_EMBEDDED_THUMBNAILS_DB));
    row.setIncludeMetadata(rs.getBoolean(INCLUDE_METADATA_DB));
    row.setIncludeTags(rs.getBoolean(INCLUDE_TAGS_DB));
    row.setIncludeThumbnails(rs.getBoolean(INCLUDE_THUMBNAILS_DB));
    row.setIncludeTranscriptions(rs.getBoolean(INCLUDE_TRANSCRIPTIONS_DB));
    row.setIncludeTranslations(rs.getBoolean(INCLUDE_TRANSLATIONS_DB));
    row.setLastActionTs(rs.getTimestamp(LAST_ACTION_TS_DB));
    row.setExportName(rs.getString(EXPORT_NAME_DB));
    row.setListName(rs.getString(LIST_NAME_DB));
    row.setProcessingHint(rs.getString(PROCESSING_HINT_DB));
    row.setRequestStatus(AccountExportStatusEnum.fromString(rs
        .getString(REQUEST_STATUS_DB)));
    row.setErrorMessage(rs.getString(ERROR_MESSAGE_DB));
    row.setRequestTs(rs.getTimestamp(REQUEST_TS_DB));
    row.setExportType(rs.getString(EXPORT_TYPE_DB));
    row.setQueryParameters(AccountExportValueObjectHelper
        .fromJsonStringMapOfStrings(rs.getString(QUERY_PARAMETERS_DB)));
    row.setBulkExportContent(AccountExportValueObjectHelper
        .fromJsonStringListOfStrings(rs.getString(BULK_EXPORT_CONTENT_DB)));
    row.setServerInstanceId(rs.getInt(SERVER_INSTANCE_ID_DB));
    row.setSpringJobExecutionId(rs.getLong(SPRING_JOB_EXECUTION_ID_DB));
    row.setSort(rs.getString(SORT_DB));
    row.setRows(rs.getInt(ROWS_DB));
    row.setOffset(rs.getInt(OFFSET_DB));
    row.setTotalRecordsProcessed(rs.getInt(TOTAL_RECS_PROCESSED_DB));
    row.setTotalErrors(rs.getInt(TOTAL_ERRORS_DB));
    row.setTotalSkipped(rs.getInt(TOTAL_SKIPPED_DB));
    row.setTotalRecordsToBeProcessed(rs.getInt(TOTAL_RECS_TO_BE_PROCESSED_DB));
    row.setFileSize(rs.getLong(FILE_SIZE_DB));
    row.setExpiresTs(rs.getTimestamp(EXPIRES_TS_DB));
    return row;
  }

}
