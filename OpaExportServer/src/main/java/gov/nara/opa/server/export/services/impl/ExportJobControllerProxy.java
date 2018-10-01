package gov.nara.opa.server.export.services.impl;

import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.server.export.services.ExportJobController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Lazy(true)
public class ExportJobControllerProxy implements Runnable {

  AccountExportValueObject accountExport;

  @Autowired
  ExportJobController exportJobController;

  @Override
  public void run() {
    exportJobController.startJob(accountExport);

  }

  public void setAccountExport(AccountExportValueObject accountExport) {
    this.accountExport = accountExport;
  }

}
