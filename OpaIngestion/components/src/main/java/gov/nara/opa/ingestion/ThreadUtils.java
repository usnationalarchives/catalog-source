package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.framework.utilities.DateTimeUtilities;

/**
 * This class has helper methods for threading.
 */
public final class ThreadUtils {
  private static final int SPIN_WAIT_MS = DateTimeUtilities.SECONDS(1);
  
  /**
   * Sleeps for a period, waking every second to check the finished flags.
   * @param milliseconds the time to sleep for.
   */
  public static void sleep(int milliseconds)
  {
    if (milliseconds <= 0) return;
    
    // loop, sleeping for a period and then wake to check the flags
    while (milliseconds > 0)
    {     
      // sleep for the defined period or the default, which ever is less
      int sleepPeriod = Math.min(SPIN_WAIT_MS, milliseconds);
      try{
        Thread.sleep(sleepPeriod);
      }
      catch (InterruptedException e){
        // Do nothing
      }
      
      // take the time we slept off the total time to sleep
      milliseconds -= sleepPeriod;
    }

  }  
}
