package gov.nara.opa.api.controller.system;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class ExportServerReportController {
	
	public static Map<String, Map<String, Object>> exportServers = new HashMap<String, Map<String, Object>>();

	@RequestMapping(value = { "/exportreport" }, method = RequestMethod.GET)
	  public ResponseEntity<String> exportreport(HttpServletRequest request ) {

		if (request.getQueryString() != null && !request.getQueryString().equals("")) {
			String[] splitQueryString = request.getQueryString().split("&");
			Map<String, String> params = new HashMap<String, String>();
			for (int i = 0; i < splitQueryString.length; i++) {
				String queryParam = splitQueryString[i].trim();
				String[] splitQueryParam = queryParam.split("=");
				if (splitQueryParam.length > 1) {
					params.put(splitQueryParam[0], splitQueryParam[1]);
				}
			}
			if (params.get("host") != null) {
				try {
					Map<String, Object> exportServer = exportServers.get(URLDecoder.decode(params.get("host"), "UTF-8"));
					if (exportServer == null) {
						exportServer = new HashMap<String, Object>();
					}
					exportServer.put("rev", URLDecoder.decode(params.get("rev"), "UTF-8"));
					exportServer.put("lastUpdated", new Date());
					exportServers.put(URLDecoder.decode(params.get("host"), "UTF-8"), exportServer);
				} catch (UnsupportedEncodingException e) {
				}
			}
		}

	    ResponseEntity<String> entity = null;
		entity = new ResponseEntity<String>(
			"OK", HttpStatus.OK);

	    return entity;
	  }

}
