package gov.nara.opa.api.controller.system;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import gov.nara.opa.architecture.dataaccess.AbstractOpaDbJDBCTemplate;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.logging.usage.UsageLogCode;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.utils.AspireObjectUtils;
import gov.nara.opa.common.dataaccess.utils.StoredProcedureDataAccessUtils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class HeathCheckController extends AbstractOpaDbJDBCTemplate {

	private static OpaLogger logger = OpaLogger
			.getLogger(HeathCheckController.class);

	private static final int timeout = 60;

	@RequestMapping(value = "/healthcheck", method = RequestMethod.GET)
	public ResponseEntity<String> healthCheck() {

		logger.info(" HealthCheck={Success}");

		AspireObject aspireObject = AspireObjectUtils
				.getAspireObject("opaResponse");

		try {
			aspireObject.add("revision", Constants.FINGERPRINT);

			for (Entry<String, Map<String, Object>> entry : ExportServerReportController.exportServers
					.entrySet()) {
				String key = entry.getKey();
				Map<String, Object> exportServer = ExportServerReportController.exportServers
						.get(key);
				if (exportServer != null) {
					exportServer.put("status", "RUNNING");
					Date last = (Date) exportServer.get("lastUpdated");
					long diff = TimeUnit.MILLISECONDS.toSeconds(new Date()
							.getTime() - last.getTime());
					if (diff > timeout) {
						exportServer.put("status", "DOWN");
					}
				}
			}

			String fingerprint = StoredProcedureDataAccessUtils
					.executeWithStringResult(getJdbcTemplate(),
							"spGetDBFingerprint",
							new HashMap<String, Object>(), "fingerprint");

			aspireObject.add("databaseFingerprint", fingerprint);
			aspireObject.add("exportServers",
					ExportServerReportController.exportServers);
		} catch (AspireException e) {
			e.printStackTrace();
		}

		String response = "";
		try {
			response = aspireObject.toJsonString(true);
		} catch (AspireException e) {
		}

		// Record log entry
		logger.info(UsageLogCode.DEAFULT + " : " + response);

		ResponseEntity<String> entity = null;
		entity = new ResponseEntity<String>(response, HttpStatus.OK);

		return entity;
	}
}
