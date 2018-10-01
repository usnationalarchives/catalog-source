package gov.nara.opa.ingestion.dasexportutility;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ConcurrentExecutor {
  
  public void execute(FileProcessor executor, int threadCount) {

    BlockingQueue<String> results = new LinkedBlockingQueue<>();

    Thread logger = new Thread(new LogThread(results));
    logger.start();
	  
    BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
    ExecutorService executorService = new ThreadPoolExecutor(threadCount, threadCount, 5, TimeUnit.SECONDS, queue);	

    executor.execute(executorService, results);
	  
    executorService.shutdown();
    try {
      executorService.awaitTermination(10, TimeUnit.MINUTES);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
		
    try {
      results.put("eof");
      logger.join();
    } catch (InterruptedException e1) {
      e1.printStackTrace();
    }	    	    
  }
}
