package gov.nara.opa.server.export.tasklet.wrapupwork;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.server.export.tasklet.AbstractAccountExportTasklet;
import gov.nara.opa.server.export.tasklet.dowork.RecordProcessorWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.Map;
import java.util.TreeSet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("partialFileAppender")
@Scope("step")
public class PartialFileAppender extends AbstractAccountExportTasklet {
	private static OpaLogger logger = OpaLogger.getLogger(PartialFileAppender.class);
  private Map<Integer, String> completedFileMap;

  @Value("#{stepExecutionContext[fileAppenderId]}")
  private int fileAppenderId;

  @Value("#{stepExecutionContext[recordProcessorIds]}")
  private TreeSet<Integer> recordProcessorIds;

  private final int timeoutWaitForFileCompletionInMillis = 600000;

  FileChannel concatenatedFileChannel;
  FileOutputStream concatenatedFile;

  @SuppressWarnings("unchecked")
  @Override
  public void beforeStep(StepExecution stepExecution) {
    super.beforeStep(stepExecution);
    //logger.error("");
    completedFileMap = (Map<Integer, String>) getLargeObjectFromExecutionContext(PartialFileAppenderPartitioner
        .getCompletedFileMapId(fileAppenderId));
  }

  @SuppressWarnings("unchecked")
  @Override
  public RepeatStatus execute(StepContribution contribution,
      ChunkContext chunkContext) throws Exception {
	  //logger.error("");
    File ouputFile = getExportOutputTempSearcherFile(fileAppenderId);
    concatenatedFile = new FileOutputStream(ouputFile);
    concatenatedFileChannel = concatenatedFile.getChannel();

    int i = 1;
    for (Integer recordProcessorId : recordProcessorIds) {
      String recordProcessorFilePath = getFilePath(recordProcessorId);
      boolean includeLineSeparator = true;
      if (i == recordProcessorIds.size()) {
        includeLineSeparator = false;
      }
      appendFile(concatenatedFile, concatenatedFileChannel,
          recordProcessorFilePath, includeLineSeparator);
      i++;
    }
    concatenatedFileChannel.close();
    concatenatedFile.close();
    Object searcherFiles = getLargeObjectFromExecutionContext(SEARCHER_FILES_COMPLETED_QUEUE_OBJECT_NAME);
    ((Map<Integer, String>) searcherFiles).put(fileAppenderId,
        ouputFile.getAbsolutePath());
    return RepeatStatus.FINISHED;
  }

  private String getFilePath(Integer recordProcessorId) {
    String filePath = completedFileMap.get(recordProcessorId);
    if (filePath == null) {
      long startTime = new Date().getTime();
      long currentTime = startTime;
      while (currentTime - startTime < timeoutWaitForFileCompletionInMillis) {
        filePath = completedFileMap.get(recordProcessorId);
        if (filePath != null) {
          return filePath;
        }
      }
      throw new OpaRuntimeException(
          "Timed out while waiting for file paths to be completed");
    } else {
      return filePath;
    }
  }
}
