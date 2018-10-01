package gov.nara.opa.api.response;

import gov.nara.opa.architecture.logging.OpaLogger;

import java.io.StringReader;

import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Component
public class APIResponse {
  
  private static OpaLogger logger = OpaLogger.getLogger(APIResponse.class);

  /**
   * @param aspireObject
   *          The Aspire OPA response object
   * @param title
   *          Json Nesting Title
   * @param paramObj
   *          Json paramter values
   * @return The Aspire OPA response object
   */
  public AspireObject setResponse(AspireObject aspireObject, String title,
      Object paramObj) {
    try {
      
      GsonBuilder builder = new GsonBuilder().disableHtmlEscaping();
      Gson gson = builder.create();
      String json = gson.toJson(paramObj);
      
      StringReader sr = new StringReader(json);
      aspireObject.loadJson(title, sr);
      
//      HashMap<String, Object> content = new HashMap<String, Object>();
//      content.put(title, paramObj);
//      
//      ValueObjectUtils.addAspireMapToParent(content, title, aspireObject);
      
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      //throw new OpaRuntimeException(e);
    }

    return aspireObject;
  }

  /**
   * Gets the aspire object output from an aspire object. Abstracts the
   * exception handling from calling methods.
   * 
   * @param object
   *          The Aspire OPA response object
   * @param format
   *          The output format. Either json or xml.
   * @param pretty
   *          Specifies if the output should be pretty printed.
   * @return The string representing the aspire object in either json or xml
   *         format.
   */
  public String getResponseOutputString(AspireObject aspireObject,
      String format, boolean pretty) {
    try {
      if (format.equals("xml")) {
        return aspireObject.toXmlString(pretty);
      } else {
        return aspireObject.toJsonString(pretty);
      }
    } catch (AspireException e) {
      logger.error(e.getMessage(), e);
    }
    return "";
  }

}
