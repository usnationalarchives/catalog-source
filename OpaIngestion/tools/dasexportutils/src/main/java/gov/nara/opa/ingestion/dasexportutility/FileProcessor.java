package gov.nara.opa.ingestion.dasexportutility;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

public interface FileProcessor {
  void execute(ExecutorService executorService, BlockingQueue<String> results);
}
