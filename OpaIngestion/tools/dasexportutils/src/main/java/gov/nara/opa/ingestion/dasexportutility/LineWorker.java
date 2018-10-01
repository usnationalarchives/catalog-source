package gov.nara.opa.ingestion.dasexportutility;

import java.util.Queue;

abstract class LineWorker implements Runnable{
  private final String line;
  private final Queue<String> results;
  
  public LineWorker(String line, Queue<String> results){
    this.line = line;
    this.results = results;
  }
  
  public Queue<String> getResults() {
    return results;
  }

  public String getLine() {
    return line;
  }
  
  @Override
  public void run() {
    try{
      process();
    }catch(Throwable e){
      System.err.printf("Line '%s' failed: %s\n", getLine(), e.getMessage());
    }
  }

  abstract void process();
}
