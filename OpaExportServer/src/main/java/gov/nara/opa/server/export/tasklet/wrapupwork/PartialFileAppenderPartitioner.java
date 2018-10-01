package gov.nara.opa.server.export.tasklet.wrapupwork;

import gov.nara.opa.server.export.tasklet.AbstractAccountExportTasklet;
import gov.nara.opa.server.export.valueobject.SolrDocumentUnitOfWork;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("partialFileAppenderPartitioner")
@Scope("step")
public class PartialFileAppenderPartitioner extends
    AbstractAccountExportTasklet implements Partitioner {

  @Override
  public Map<String, ExecutionContext> partition(int gridSize) {
    putLargeObjectInExecutionContext(
        SEARCHER_FILES_COMPLETED_QUEUE_OBJECT_NAME,
        new ConcurrentHashMap<Integer, String>());
    Map<String, ExecutionContext> result = new ConcurrentHashMap<String, ExecutionContext>();
    Map<Integer, Map<Integer, String>> partialFileCompleteMap = getPartialFilesCompleteMap();
    Map<Integer, Map<Integer, ConcurrentLinkedQueue<SolrDocumentUnitOfWork>>> workQueuesMap = getWorkQueuesMap();

    for (int i = 1; i <= gridSize; i++) {
      ExecutionContext executionContext = new ExecutionContext();
      executionContext.putInt("fileAppenderId", i);
      // executionContext.put("completedFileMap",
      // partialFileCompleteMap.get(i));
      putLargeObjectInExecutionContext(getCompletedFileMapId(i),
          partialFileCompleteMap.get(i));
      Set<Integer> recordProcessorIds = workQueuesMap.get(i).keySet();
      executionContext.put("recordProcessorIds",
          getOderedRecordProcessorIds(recordProcessorIds));
      result.put("fileAppender" + i, executionContext);
    }

    return result;
  }

  public static final String getCompletedFileMapId(int id) {
    return id + "-completedFileMap-fileAppender";
  }

}
