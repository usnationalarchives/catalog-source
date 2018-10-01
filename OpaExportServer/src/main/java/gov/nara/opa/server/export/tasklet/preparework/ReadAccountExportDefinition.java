package gov.nara.opa.server.export.tasklet.preparework;

import java.io.IOException;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.common.dataaccess.export.AccountExportDao;
import gov.nara.opa.common.services.export.AccountExportDbProxyService;
import gov.nara.opa.common.valueobject.export.AccountExportStatusEnum;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.server.export.tasklet.AbstractAccountExportTasklet;

import org.apache.commons.io.FileUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("readAccountExportDefinition")
@Scope("step")
public class ReadAccountExportDefinition extends AbstractAccountExportTasklet {
  OpaLogger logger = OpaLogger.getLogger(ReadAccountExportDefinition.class);

  @Autowired
  AccountExportDao accountExportDao;

  @Autowired
  AccountExportDbProxyService accountExportDaoTransactional;
  
  @Override
  public RepeatStatus execute(StepContribution contribution,
      ChunkContext chunkContext) throws Exception {

    initializeLargeObjectsMap();

    AccountExportValueObject accountExport = getAccountExport();
    if (accountExport != null) {
      return RepeatStatus.FINISHED;
    }

    accountExport = accountExportDao.selectById(getExportId());
    if (accountExport == null) {
      throw new OpaRuntimeException("No account export found for export id: "
          + getExportId());
    }
    putLargeObjectInExecutionContext(ACCOUNT_EXPORT_OBJECT_NAME, accountExport);
    try {
    	FileUtils.deleteDirectory(getExportOutputDir());
	} catch (IOException e) {
		logger.warn(String.format("Cannot delete directory %1$s. Reason %2$s", getExportOutputDir(), e));
	}
    getExportOutputTempDir().mkdirs();
    getExportOutputFinalDir().mkdirs();
    accountExportDaoTransactional.updateStatus(
        AccountExportStatusEnum.PROCESSING.toString(), getExportId());
    return RepeatStatus.FINISHED;
  }
}
