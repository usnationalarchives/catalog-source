package gov.nara.opa.server.export.services.impl;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.server.export.services.ExportRequestsController;
import gov.nara.opa.server.export.system.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Lazy(true)
public class ExportRequestsControllerProxy implements Runnable {

  public static final Queue<AccountExportValueObject> PROCESSING_QUEUE = new ConcurrentLinkedQueue<AccountExportValueObject>();

  Integer pid;

  @Autowired
  ExportRequestsController exportsRequestController;

  @Value("${waitTimeBetweenChecksForNewExportRequestsInMillis}")
  Integer waitTimeBetweenChecksForNewExportRequestsInMillis;

  @Value("${maxNoOfConcurrentJobs}")
  Integer maxNoOfConcurrentJobs;

  @Value("${binRuntimeDir}")
  String binRuntimeDir;

  @Value("${apiURL}")
  String apiURL;

  OpaLogger logger = OpaLogger.getLogger(ExportRequestsControllerProxy.class);

  public Integer getPid() {
    return pid;
  }

  public void setPid(Integer pid) {
    this.pid = pid;
  }

  private void writePidFile(Integer pid) throws IOException {
	  logger.info(String.format("Writing Pid into file %1$s", binRuntimeDir + "pid.txt"));
	  File pidFile = new File(binRuntimeDir + "pid.txt");
	  FileWriter pidFileWriter = new FileWriter(pidFile, false);
	  pidFileWriter.write(pid.toString());
	  pidFileWriter.close();
  }

  private void logExportServer(URL url) {
	  HttpURLConnection con;
	  try {
		  con = (HttpURLConnection) url.openConnection();
		  con.setRequestMethod("GET");
		  BufferedReader br = new BufferedReader(
				  new InputStreamReader(con.getInputStream()));
		  br.close();
	  } catch (IOException e) {
		  logger.warn(String.format("Cannot report to the API server: %s", apiURL));
	  }
  }

  @Override
  public void run() {
	URL url = null;
    try {
      writePidFile(getPid());
      InetAddress localMachine = InetAddress.getLocalHost();
      apiURL = apiURL.replaceAll("^\"|\"$", "");
      String address = "http://" + apiURL + "/OpaAPI/exportreport?host=" + URLEncoder.encode(localMachine.getHostName(), "UTF-8") + "&rev=" + URLEncoder.encode(Constants.FINGERPRINT, "UTF-8");
      url = new URL(address);
    } catch (MalformedURLException mue) {
    	logger.warn(String.format("The url used for export server reporting is malformed: %s", apiURL));
    } catch (IOException e1) {
      new OpaRuntimeException(e1);
    } 
    logger.info("The export server was started.");
    
    //Retry logic
    int maxRetries = 3;
    int retryCounter = 0;
    
    while (true) {
      try {
        if (PROCESSING_QUEUE.size() <= maxNoOfConcurrentJobs + 2) {
          exportsRequestController.startNewExports();
        }
        logger
            .trace(String
                .format(
                    "Waiting for %1$s seconds before checking for more queued requests",
                    waitTimeBetweenChecksForNewExportRequestsInMillis / 1000));

        logExportServer(url);
        Thread.sleep(waitTimeBetweenChecksForNewExportRequestsInMillis);
        retryCounter = 0;
      } catch (Exception e) {
      	if(retryCounter < maxRetries) {
    		//Fail gracefully
    		logger.error("New export retrieval has failed, retrying...", e);
    		try {
				Thread.sleep(waitTimeBetweenChecksForNewExportRequestsInMillis);
			} catch (InterruptedException e1) {
				logger.error(e1);
			}
    		retryCounter++;
    	} else {
    		logger.fatal("EXPORT RETRIEVAL HAS FAILED AFTER SEVERAL RETRIES", e);
    		try {
				Thread.sleep(waitTimeBetweenChecksForNewExportRequestsInMillis);
			} catch (InterruptedException e1) {
				logger.error(e1);
			}
    	}
        
      }
    }
  }

}
