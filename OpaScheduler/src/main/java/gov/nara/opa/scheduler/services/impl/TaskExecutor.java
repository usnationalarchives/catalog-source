package gov.nara.opa.scheduler.services.impl;

import gov.nara.opa.architecture.logging.OpaLogger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpStatus;


public class TaskExecutor implements Runnable {

	private OpaLogger logger = OpaLogger.getLogger(TaskExecutor.class);

	private int index;

	public TaskExecutor() {
	}

	public TaskExecutor(int index) {
		logger.info("Executor " + SchedulerProxy.tasks.get(index).getTaskName());
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public void run() {
		BufferedReader in = null;
		try {
			logger.info(String.format("Calling %s", SchedulerProxy.tasks.get(index).getEndpoint()));

			URL url = new URL(SchedulerProxy.tasks.get(index).getEndpoint());

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);

			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.writeBytes("");
			wr.flush();
			wr.close();

			int responseCode = conn.getResponseCode();

			in = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			
			if (responseCode == HttpStatus.SC_OK) {
				logger.info(String.format("%1$s task has run successfully. A total of %2$s changes were made", 
						SchedulerProxy.tasks.get(index).getTaskName(), response));
			} else {
				logger.error(String.format("There was an error running task %1$s. Error: %2%s", 
						SchedulerProxy.tasks.get(index).getTaskName(), response));
			}
		} catch(IOException e) {
			logger.error(String.format("Cannot connect to %s", SchedulerProxy.tasks.get(index).getEndpoint()), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}

	}

}
