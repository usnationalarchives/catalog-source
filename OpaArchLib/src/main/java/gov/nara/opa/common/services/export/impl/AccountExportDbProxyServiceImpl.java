package gov.nara.opa.common.services.export.impl;

import gov.nara.opa.common.dataaccess.export.AccountExportDao;
import gov.nara.opa.common.services.export.AccountExportDbProxyService;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AccountExportDbProxyServiceImpl implements
    AccountExportDbProxyService {

  @Autowired
  AccountExportDao accountExportDao;

  @Override
  @Transactional
  public void create(AccountExportValueObject accountExport) {
    accountExportDao.create(accountExport);
  }

  @Override
  @Transactional
  public void updateStatus(String requestStatus, Integer exportId) {
    accountExportDao.updateRequestStatusAndJobExecutionId(exportId,
        requestStatus, null, null);
  }

  @Override
  @Transactional
  public void incrementRecordsProcessed(Integer recordsProcessed,
      Long fileSize, Integer exportId) {
    accountExportDao.incrementRecordsProcessed(exportId, recordsProcessed,
        fileSize, null);
  }

  @Override
  @Transactional
  public void update(AccountExportValueObject accountExport) {
    accountExportDao.update(accountExport);
  }

  @Override
  @Transactional
  public AccountExportValueObject selectById(Integer exportId) {
    // TODO Auto-generated method stub
    return accountExportDao.selectById(exportId);
  }

}
