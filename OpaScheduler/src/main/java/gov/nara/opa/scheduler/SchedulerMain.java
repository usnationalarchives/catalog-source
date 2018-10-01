package gov.nara.opa.scheduler;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.scheduler.services.impl.SchedulerProxy;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class SchedulerMain {

	static OpaLogger logger = OpaLogger.getLogger(SchedulerMain.class);

	@SuppressWarnings("resource")
	public static void main( String[] args ) {
		Integer processId = null;
		try {
			processId = getProcessId();
		} catch (Exception e) {
			throw new OpaRuntimeException(e);
		}
		logger.info("Scheduler was started with pid " + processId + " ...");
		ApplicationContext context = new FileSystemXmlApplicationContext(
				"file:${gov.nara.opa.api.config}/applicationContext.xml");
		SchedulerProxy mainController = (SchedulerProxy) context.getBean("schedulerProxyBean");
		mainController.setPid(processId);
		mainController.readConfigFile();
		mainController.startTasks();
	}

	@SuppressWarnings("restriction")
	private static Integer getProcessId() throws Exception {
		java.lang.management.RuntimeMXBean runtime = java.lang.management.ManagementFactory
				.getRuntimeMXBean();
		java.lang.reflect.Field jvm = runtime.getClass().getDeclaredField("jvm");
		jvm.setAccessible(true);
		sun.management.VMManagement mgmt = (sun.management.VMManagement) jvm
				.get(runtime);
		java.lang.reflect.Method pid_method = mgmt.getClass().getDeclaredMethod(
				"getProcessId");
		pid_method.setAccessible(true);

		return (Integer) pid_method.invoke(mgmt);
	}
}
