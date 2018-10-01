package gov.nara.opa.ingestion.dasexportutility;

import java.util.Queue;

abstract class LineWorkerFactory {
  public abstract LineWorker createWorker(String line, Queue<String> results);
}
