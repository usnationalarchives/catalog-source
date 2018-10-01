package gov.nara.opa.server.export.services.impl;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.common.dataaccess.export.AccountExportDao;
import gov.nara.opa.common.services.docstransforms.impl.normalize.AbstractXslValueExtractor;
import gov.nara.opa.common.valueobject.export.AccountExportStatusEnum;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.server.export.services.ExportRequestsController;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component(value = "exportRequestsController")
public class ExportRequestsControllerImpl implements ExportRequestsController,
ApplicationContextAware {

	OpaLogger logger = OpaLogger.getLogger(ExportRequestsControllerImpl.class);

	@Value("${maxNoOfNewRequestsSelection}")
	Integer maxNoOfNewRequestsSelection;

	@Value(value = "${export.xls.location}")
	public void setXSL_FILE_PATH(String XSL_FILE_PATH) {
		AbstractXslValueExtractor.XSL_FILE_PATH = XSL_FILE_PATH;
	}

	@Autowired
	AccountExportDao accountExportDao;

	@Autowired
	@Qualifier("exportJobExecutor")
	TaskExecutor taskExecutor;

	ApplicationContext applicationContext;

	public static final Queue<AccountExportValueObject> PROCESSING_QUEUE = new ConcurrentLinkedQueue<AccountExportValueObject>();

	@Override
	@Transactional
	public void startNewExports() {
		List<AccountExportValueObject> newRequests = accountExportDao
				.getNewQueuedExports(maxNoOfNewRequestsSelection);
		for (AccountExportValueObject accountExport : newRequests) {
			// need to add a small wait time before starting a new job as otherwise
			// deadlock conditions against the spring batch tables occur while trying
			// to start multiple jobs at the same time
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				throw new OpaRuntimeException(e);
			}

			ExportJobControllerProxy jobController = applicationContext
					.getBean(ExportJobControllerProxy.class);
			jobController.setAccountExport(accountExport);
			accountExport.setRequestStatus(AccountExportStatusEnum.SCHEDULED);
			accountExportDao.update(accountExport);
			logger.debug("Scheduled job export id " + accountExport.getExportId());
			taskExecutor.execute(jobController);
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}
}
