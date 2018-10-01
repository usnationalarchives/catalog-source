package gov.nara.opa.common.dataaccess.export;

import gov.nara.opa.common.valueobject.export.AccountExportStatusValueObject;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;

public interface AccountExportDao {

  void create(AccountExportValueObject accountExport);

  List<AccountExportValueObject> getNewQueuedExports(Integer limit);

  void update(AccountExportValueObject accountExport);

  AccountExportValueObject selectById(Integer exportId);

  void updateRequestStatusAndJobExecutionId(Integer exportId,
      String requestStatus, Long springJobExecutionId, Timestamp lastActionTs);

  AccountExportStatusValueObject getCurrentStatusObject(Integer exportId);

  String getCurrentStatus(Integer exportId);

  List<AccountExportValueObject> getExportsForAccount(Integer limit);
  
  List<AccountExportValueObject> getExpiredExports();

  LinkedHashMap<String, Object> getStatusSummary(Integer accountId);

  void deleteAccountExport(Integer accountExportId);

  void incrementRecordsProcessed(Integer exportId, Integer recordsProcessed,
      Long fileSize, Timestamp lastActionTs);
}
