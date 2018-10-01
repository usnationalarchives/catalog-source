package gov.nara.opa.server.export;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.server.export.services.impl.ExportRequestsControllerProxy;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.task.TaskExecutor;

public class ExportServerMain {

  static OpaLogger logger = OpaLogger.getLogger(ExportServerMain.class);

  public static void main(String[] args) {
    Integer processId = null;
    try {
      processId = getProcessId();
    } catch (Exception e) {
      throw new OpaRuntimeException(e);
    }
    logger.info("Export Server was started with pid " + processId + " ...");
    // Load the Spring context
    @SuppressWarnings("resource")
    ApplicationContext context = new FileSystemXmlApplicationContext(
        "file:${gov.nara.opa.api.config}/applicationContext.xml");

    TaskExecutor executor = (TaskExecutor) context
        .getBean("mainControllerExecutor");
    ExportRequestsControllerProxy mainController = context
        .getBean(ExportRequestsControllerProxy.class);

    mainController.setPid(processId);
    executor.execute(mainController);
  }

  private static Integer getProcessId() throws Exception {
    java.lang.management.RuntimeMXBean runtime = java.lang.management.ManagementFactory
        .getRuntimeMXBean();
    java.lang.reflect.Field jvm = runtime.getClass().getDeclaredField("jvm");
    jvm.setAccessible(true);
    @SuppressWarnings("restriction")
    sun.management.VMManagement mgmt = (sun.management.VMManagement) jvm
        .get(runtime);
    java.lang.reflect.Method pid_method = mgmt.getClass().getDeclaredMethod(
        "getProcessId");
    pid_method.setAccessible(true);

    return (Integer) pid_method.invoke(mgmt);
  }
}
