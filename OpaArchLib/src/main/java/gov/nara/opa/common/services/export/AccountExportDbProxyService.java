package gov.nara.opa.common.services.export;

import gov.nara.opa.common.valueobject.export.AccountExportValueObject;

public interface AccountExportDbProxyService {

  void create(AccountExportValueObject accountExport);

  void update(AccountExportValueObject accountExport);

  void updateStatus(String requestStatus, Integer exportId);

  void incrementRecordsProcessed(Integer recordsProcessed, Long fileSize,
      Integer exportId);

  AccountExportValueObject selectById(Integer exportId);

}
