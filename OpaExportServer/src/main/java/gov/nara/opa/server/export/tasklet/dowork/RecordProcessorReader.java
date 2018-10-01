package gov.nara.opa.server.export.tasklet.dowork;

import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.server.export.tasklet.AbstractAccountExportTasklet;
import gov.nara.opa.server.export.valueobject.SolrDocumentUnitOfWork;

@Component("recordProcessorReader")
@Scope("step")
public class RecordProcessorReader extends AbstractAccountExportTasklet
		implements ItemReader<SolrDocumentUnitOfWork>, InitializingBean {

	OpaLogger logger = OpaLogger.getLogger(RecordProcessorReader.class);

	private ConcurrentLinkedQueue<SolrDocumentUnitOfWork> workQueue;

	@Value("#{stepExecutionContext[recordProcessorId]}")
	private int recordProcessorId;

	@Value("${waitTimeBetweenPollingForDocumentsInWorkingQueueInMillis}")
	private int waitTimeBetweenPollingForDocumentsInWorkingQueueInMillis;

	@Value("${timeoutPollingForDocumentsInWorkingQueue}")
	private int timeoutPollingForDocumentsInWorkingQueue;

	private int timeoutPollingForDocumentsInWorkingQueueInMillis;

	@SuppressWarnings("unchecked")
	@Override
	public void beforeStep(StepExecution stepExecution) {
		super.beforeStep(stepExecution);
		workQueue = (ConcurrentLinkedQueue<SolrDocumentUnitOfWork>) getLargeObjectFromExecutionContext(
				RecordsProcessorPartitioner.getWorkQueueName(recordProcessorId));
	}

	@Override
	public SolrDocumentUnitOfWork read()
			throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

		
		logger.debug("Starting read");

		SolrDocumentUnitOfWork document = workQueue.poll();
		long startTime = new Date().getTime();
		long currentTime = startTime;
		while (document == null && (currentTime - startTime) < timeoutPollingForDocumentsInWorkingQueueInMillis) {
			Thread.sleep(waitTimeBetweenPollingForDocumentsInWorkingQueueInMillis);
			document = workQueue.poll();
			currentTime = new Date().getTime();
		}
		if (document == null) {
			throw new OpaRuntimeException(
					"The read of records from processing from the queue timed out for recordProcessorId: "
							+ recordProcessorId);
		}
		if (document.getDocument() == null) {
			return null;
		}
		return document;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		timeoutPollingForDocumentsInWorkingQueueInMillis = timeoutPollingForDocumentsInWorkingQueue * 1000;
	}
}
