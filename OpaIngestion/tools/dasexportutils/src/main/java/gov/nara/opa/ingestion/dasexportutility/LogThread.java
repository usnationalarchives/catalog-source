package gov.nara.opa.ingestion.dasexportutility;

import java.util.concurrent.BlockingQueue;

public class LogThread implements Runnable{
  private final BlockingQueue<String> results;

  public LogThread(BlockingQueue<String> queue) {
    this.results = queue;			
  }

  @Override
  public void run() {
    while(true) {
      String line = null;

      try {
        line = results.take();
      } catch (InterruptedException e) {
      }

      if ("eof".equals(line)) {
        break;
      }

      if (line != null) {
        System.out.print(line);
      }
    }			
  }		
}
