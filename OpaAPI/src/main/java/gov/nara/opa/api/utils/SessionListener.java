package gov.nara.opa.api.utils;

import gov.nara.opa.architecture.logging.OpaLogger;

import java.util.Enumeration;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class SessionListener implements HttpSessionListener {

  OpaLogger logger = OpaLogger.getLogger(SessionListener.class);

  @Override
  public void sessionCreated(HttpSessionEvent se) {
    HttpSession session = se.getSession();
    logger.trace("HTTP SESSION CREATED: " + session.getId() + " - "
        + getSessionAttributes(se.getSession()));
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent se) {
    HttpSession session = se.getSession();
    logger.trace("HTTP SESSION DESTROYED: " + session.getId() + " - "
        + getSessionAttributes(se.getSession()));
    logger.trace("Stack trace for the session destruction: \n");
    Throwable t = new Throwable();
    logger.trace("strack trace", t);
  }

  private String getSessionAttributes(HttpSession session) {
    StringBuilder sb = new StringBuilder();
    Enumeration<String> attributeNames = session.getAttributeNames();
    while (attributeNames.hasMoreElements()) {
      String aN = attributeNames.nextElement();
      sb.append(aN + ": ");
      sb.append(session.getAttribute(aN) + ";");
    }
    return sb.toString();
  }

}
