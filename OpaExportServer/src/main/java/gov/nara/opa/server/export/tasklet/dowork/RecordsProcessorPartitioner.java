package gov.nara.opa.server.export.tasklet.dowork;

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

@Component("recordProcessorPartitioner")
@Scope("step")
public class RecordsProcessorPartitioner extends AbstractAccountExportTasklet
    implements Partitioner {

  @Override
  public Map<String, ExecutionContext> partition(int gridSize) {
    Map<String, ExecutionContext> result = new ConcurrentHashMap<String, ExecutionContext>();
    Map<Integer, Map<Integer, ConcurrentLinkedQueue<SolrDocumentUnitOfWork>>> workQueuesMap = getWorkQueuesMap();

    Set<Integer> searcherIds = workQueuesMap.keySet();

    for (Integer searcherId : searcherIds) {
      Map<Integer, ConcurrentLinkedQueue<SolrDocumentUnitOfWork>> recordsProcessormap = workQueuesMap
          .get(searcherId);
      Set<Integer> recordProcessorIds = recordsProcessormap.keySet();
      for (Integer recordProcessorId : recordProcessorIds) {
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.putInt("searcherId", searcherId);
        executionContext.putInt("recordProcessorId", recordProcessorId);
        // executionContext.put("workQueue",
        // recordsProcessormap.get(recordProcessorId));
        putLargeObjectInExecutionContext(getWorkQueueName(recordProcessorId),
            recordsProcessormap.get(recordProcessorId));
        result.put("recordProcessor" + recordProcessorId, executionContext);
      }
    }

    return result;
  }

  public static String getWorkQueueName(int recordProcessorId) {
    return recordProcessorId + "-workQueue";
  }
}
