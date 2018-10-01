package gov.nara.opa.server.export.services.impl;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.common.services.export.AccountExportDbProxyService;
import gov.nara.opa.common.valueobject.export.AccountExportStatusEnum;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.server.export.services.ExportJobController;

import java.util.Date;
import java.util.List;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Lazy(true)
public class ExportJobControllerImpl implements ExportJobController {

  @Autowired
  @Qualifier("exportJob")
  Job exportJob;

  @Autowired
  JobLauncher jobLauncher;

  @Autowired
  JobOperator jobOperator;

  @Autowired
  JobRepository jobRepository;

  @Autowired
  JobExplorer jobExplorer;

  @Value("${waitTimeBetweenChecksForJobCompletionInMillis}")
  Integer waitTimeBetweenChecksForJobCompletionInMillis;

  @Value("${exportJobTimeOutInSeconds}")
  Integer exportJobTimeOutInSeconds;

  @Autowired
  AccountExportDbProxyService accountExportDao;
  
  @Value("${useJavaTarGz}")
  Boolean useJavaTarGz;

  private static final String JOB_EXECUTION_ERROR_MESSAGE = "Error executing exportid %1$s";

  public static final String EXECUTION_ID_JOB_PARAM_NAME = "execution_id";
  public static final String CURRENT_TIME_JOB_PARAM_NAME = "current_time";
  public static final String USE_JAVA_TAR_GZ_JOB_PARAM_NAME = "use_java_tar_gz";

  private static final int ERROR_MESSAGE_DB_COLUMN_LENGTH = 400;
  
  private static final int PROCESS_BEACON_TIME = 10000;

  OpaLogger logger = OpaLogger.getLogger(ExportJobControllerImpl.class);

  private boolean monitorJobCompletion(JobExecution jobExecution,
      long startTime, long exportJobTimeOutInMillis,
      AccountExportValueObject accountExport) throws InterruptedException {
	  
	long counter = 1;
	  
    while (true) {
      if (!hasJobTerminated(jobExecution, accountExport)) {
        Thread.sleep(waitTimeBetweenChecksForJobCompletionInMillis);
      } else {
        return true;
      }
      long currentTime = new Date().getTime();
      
      if (currentTime - startTime > exportJobTimeOutInMillis) {
        logger.error("The exportId " + accountExport.getExportId()
            + " associated with spring batch execution id "
            + jobExecution.getId() + " timed out.");
        accountExport.setRequestStatus(AccountExportStatusEnum.TIMEDOUT);
        accountExport.setErrorMessage("The job timed out after running for "
            + exportJobTimeOutInSeconds + " seconds.");
        updateAccountStatus(accountExport);
        jobExecution.stop();
        logger.error(String.format("Job %1$d has been stopped.", accountExport.getExportId()));
        return false;
      } else {
    	  AccountExportValueObject latestExport = getLatestUpdates(accountExport);
    	  if(currentTime - latestExport.getLastActionTs().getTime() > PROCESS_BEACON_TIME * counter) {
    	      logger.info(String.format("ExportId: [%1$d] no progress - [%2$d]", accountExport.getExportId(), counter));
    	      counter++;
    	  } else {
    		  counter = 1;
    	  }
      }
    }
  }

  @Override
  public void startJob(AccountExportValueObject accountExport) {
    try {
      ExportRequestsControllerImpl.PROCESSING_QUEUE.offer(accountExport);
      logger.info("Starting spring batch job for export id "
          + accountExport.getExportId());
      JobExecution jobExecution = startSpringJob(accountExport);
      long startTime = new Date().getTime();
      long exportJobTimeOutInMillis = exportJobTimeOutInSeconds * 1000;
      monitorJobCompletion(jobExecution, startTime, exportJobTimeOutInMillis,
          accountExport);
    } catch (Exception e) {
      logger.error(
          String.format(JOB_EXECUTION_ERROR_MESSAGE,
              accountExport.getExportId()), e);
      ExportRequestsControllerImpl.PROCESSING_QUEUE.remove(accountExport);
      throw new OpaRuntimeException(e);
    }
    logger.info("Completed spring batch job for export id "
        + accountExport.getExportId());
    ExportRequestsControllerImpl.PROCESSING_QUEUE.remove(accountExport);

  }

  private JobExecution startSpringJob(AccountExportValueObject accountExport)
      throws JobExecutionException {
    JobParameters jobParameters = getExportJobParameters(accountExport);
    JobExecution jobExecution = jobRepository.getLastJobExecution(
        exportJob.getName(), jobParameters);
    if (jobExecution == null) {
      return jobLauncher.run(exportJob, jobParameters);
    } else if (newParametersNeeded(jobExecution)) {
      JobParametersBuilder jobParamsBuilder = new JobParametersBuilder(
          jobParameters);
      jobParamsBuilder.addDate(CURRENT_TIME_JOB_PARAM_NAME, new Date());
      jobParameters = jobParamsBuilder.toJobParameters();
      return jobLauncher.run(exportJob, jobParameters);
    } else {
      Long restartedExecutionId = jobOperator.restart(jobExecution.getId());
      return jobExplorer.getJobExecution(restartedExecutionId);
    }
  }

  private boolean newParametersNeeded(JobExecution jobExecution)
      throws NoSuchJobExecutionException, JobExecutionAlreadyRunningException,
      JobExecutionNotRunningException {
    long executionId = jobExecution.getId();
    if (jobExecution.getStatus().equals(BatchStatus.STARTED)
        && jobExecution.getExitStatus().equals(ExitStatus.UNKNOWN)) {
      jobOperator.stop(executionId);
      jobOperator.abandon(executionId);
      return true;
    } else if ((jobExecution.getStatus().equals(BatchStatus.ABANDONED) && jobExecution
        .getExitStatus().equals(ExitStatus.UNKNOWN))
        || (jobExecution.getStatus().equals(BatchStatus.COMPLETED) && jobExecution
            .getExitStatus().equals(ExitStatus.COMPLETED))) {
      return true;
    }
    return false;
  }

  private JobParameters getExportJobParameters(
      AccountExportValueObject accountExport) {
    JobParametersBuilder jobParamsBuilder = new JobParametersBuilder();
    jobParamsBuilder.addLong(EXECUTION_ID_JOB_PARAM_NAME, accountExport
        .getExportId().longValue());
    jobParamsBuilder.addString(USE_JAVA_TAR_GZ_JOB_PARAM_NAME, useJavaTarGz.toString());
    return jobParamsBuilder.toJobParameters();
  }

  private boolean hasJobTerminated(JobExecution jobExecution,
      AccountExportValueObject accountExport) {
    BatchStatus status = jobExecution.getStatus();
    if (status.equals(BatchStatus.ABANDONED)
        || status.equals(BatchStatus.COMPLETED)
        || status.equals(BatchStatus.UNKNOWN)
        || status.equals(BatchStatus.FAILED)) {
      if (!status.equals(BatchStatus.COMPLETED)) {
        List<Throwable> exceptions = jobExecution.getAllFailureExceptions();
        String errorMessage = "Export job failed.";
        Throwable exception = null;
        if (exceptions != null && exceptions.size() >= 1) {
          errorMessage = exceptions.get(0).getMessage();
          exception = exceptions.get(0);
        }
        try {
        	accountExport.setErrorMessage(errorMessage.substring(0,
        		Math.min(errorMessage.length(), ERROR_MESSAGE_DB_COLUMN_LENGTH)));
        } catch (Exception e) {
        	logger.error(String.format("Error message %1$s", errorMessage));
        	logger.error(e.getMessage(), e);
        }
        accountExport.setRequestStatus(AccountExportStatusEnum.FAILED);

        String logMessage = "The export job with export id "
                + accountExport.getExportId() + " and spring batch id "
                + jobExecution.getId() + " failed due to reason: " + errorMessage;
        
        if (exception != null) {
        	logger.error(logMessage, exception);
        } else {
        	logger.error(logMessage);
        }
        
        updateAccountStatus(accountExport);
      }

      return true;
    } else {
      return false;
    }
  }

  @Override
  public void updateAccountStatus(AccountExportValueObject accountExport) {
    accountExportDao.update(accountExport);
  }
  
  private AccountExportValueObject getLatestUpdates(AccountExportValueObject accountExport) {
	  return accountExportDao.selectById(accountExport.getExportId());
  }
}
