package gov.nara.opa.ingestion.dasexportutility;

import java.nio.file.Path;
import java.util.Queue;

abstract class DasRecordWorkerFactory{
  public abstract DasRecordWorker createWorker(Path file, Queue<String> results);
}
