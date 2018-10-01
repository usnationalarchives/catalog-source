package gov.nara.opa.ingestion.dasexportutility;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

public class ProgressThread implements Runnable{
  private final Lock lock;
  private final AtomicInteger recordCount;

  public ProgressThread(Lock lock, AtomicInteger recordCount) {
    this.lock = lock;
    this.recordCount = recordCount;
  }

  @Override
  public void run() {
    boolean acquired;
    
    while(true) {				
      try {
        acquired = lock.tryLock(1, TimeUnit.MINUTES);
        if (acquired) {
          lock.unlock();
          break;
        }
      } catch (InterruptedException e) {
      }

      System.out.println(recordCount.intValue());
    }
  }
}
