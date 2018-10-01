package gov.nara.opa.server.export.services;

import gov.nara.opa.common.valueobject.export.AccountExportValueObject;

public interface ExportJobController {
  public void startJob(AccountExportValueObject accountExport);

  void updateAccountStatus(AccountExportValueObject accountExport);
}
