package gov.nara.opa.scheduler.services.impl;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.TimestampUtils;
import gov.nara.opa.scheduler.constants.Constants;
import gov.nara.opa.scheduler.valueobject.TaskValueObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

@Component
public class SchedulerProxy {

	public static List<TaskValueObject> tasks = new ArrayList<TaskValueObject>();

	private OpaLogger logger = OpaLogger.getLogger(SchedulerProxy.class);

	private int pid;

	private String configFilePath;

	private String binRuntimeDir;

	private String apiURL;

	private String fingerprintPath;

	private ScheduledExecutorService scheduler;

	private final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;

	private final long SECONDS_PER_DAY = 24 * 60 * 60;

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public String getConfigFilePath() {
		return configFilePath;
	}

	public void setConfigFilePath(String configFilePath) {
		this.configFilePath = configFilePath;
	}

	public String getBinRuntimeDir() {
		return binRuntimeDir;
	}

	public void setBinRuntimeDir(String binRuntimeDir) {
		this.binRuntimeDir = binRuntimeDir;
	}

	public String getApiURL() {
		return apiURL;
	}

	public void setApiURL(String apiURL) {
		this.apiURL = apiURL;
	}

	public String getFingerprintPath() {
		return fingerprintPath;
	}

	public void setFingerprintPath(String fingerprintPath) {
		this.fingerprintPath = fingerprintPath;
	}

	public void readConfigFile() throws OpaRuntimeException {
		ConfigReader reader = new ConfigReader(getConfigFilePath());
		tasks = reader.read(apiURL);
		scheduler = Executors.newScheduledThreadPool(tasks.size());
		try {
			writePidFile(getPid());
		} catch (IOException e) {
			logger.error(String.format("Cannot write pid to file: %1$s", 
					getBinRuntimeDir() + "pid.txt"));
		}
	}

	private void writePidFile(Integer pid) throws IOException {
		logger.info(String.format("Writing Pid into file %1$s", getBinRuntimeDir() + "pid.txt"));
		File pidFile = new File(getBinRuntimeDir() + "pid.txt");
		FileWriter pidFileWriter = new FileWriter(pidFile, false);
		pidFileWriter.write(pid.toString());
		pidFileWriter.close();
		File fingerprintFile = new File(getFingerprintPath());
		FileWriter fingerprintFileWriter = new FileWriter(fingerprintFile, false);
		fingerprintFileWriter.write(Constants.FINGERPRINT + "\n");
		fingerprintFileWriter.close();
	}

	public void startTasks() {
		Runtime.getRuntime().freeMemory();
		long timePortion = TimestampUtils.getCurrentTimeInUtc().
				getTimeInMillis() % MILLIS_PER_DAY;

		int i = 0;
		for (TaskValueObject task : tasks) {
			long diffInMillies = task.getTime().getTime() - timePortion;
			if (diffInMillies < 0) {
				diffInMillies = MILLIS_PER_DAY + diffInMillies;
			}
			if (diffInMillies >= MILLIS_PER_DAY) {
				diffInMillies -= MILLIS_PER_DAY;
			}
			logger.info(String.format("Starting task %1$s in %2$d seconds...", 
					task.getTaskName(), diffInMillies / 1000));
			TaskExecutor executor = new TaskExecutor(i++);
			scheduler.scheduleAtFixedRate(executor, diffInMillies / 1000, 
					SECONDS_PER_DAY, TimeUnit.SECONDS);
		}
	}

}
