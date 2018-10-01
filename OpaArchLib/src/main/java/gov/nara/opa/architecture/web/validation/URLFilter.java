package gov.nara.opa.architecture.web.validation;

import gov.nara.opa.architecture.logging.OpaLogger;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;

public class URLFilter {
  private static OpaLogger logger = OpaLogger.getLogger(URLFilter.class);

  // private FilterConfig config;
  // private final static Pattern PATTERN = Pattern.compile("^/(\\w+)/(.*)$");
  // private final static String FORWARD_URL = "/%s.xhtml?path=%s&edition=%s";
  // private final Pattern pt = Pattern.compile("javax.faces.resource(s)?");

  // xss, javascript injection patterns
  private final static Pattern xssp0 = Pattern.compile("<.*?>");
  private final static Pattern xssp1 = Pattern.compile("<script.*?>");
  private final static Pattern xssp2 = Pattern
      .compile("<.*?javascript:.*?>.*?</.*?>");
  private final static Pattern xssp3 = Pattern.compile("</script.*");
  private final static Pattern xssp4 = Pattern.compile("eval\\(.*");
  private final static Pattern xssp5 = Pattern.compile("alert\\(.*");
  private final static Pattern xssp6 = Pattern.compile("img\\(.*\\)");
  //private final static Pattern xssp7 = Pattern.compile("document\\.+");
  private final static Pattern xssp7 = Pattern.compile("document\\.[\\(a-zA-Z].*");
  private final static Pattern xssp8 = Pattern.compile("settimeout\\(.*");
  private final static Pattern xssp9 = Pattern.compile("setinterval\\(.*");
  private final static Pattern xssp10 = Pattern.compile("execscript\\(.*");
  private final static Pattern xssp11 = Pattern.compile("javascript:");
  //private final static Pattern xssp12 = Pattern.compile("window\\.");
  private final static Pattern xssp12 = Pattern.compile("window\\.[\\(a-zA-Z].*");
  private final static Pattern xssp13 = Pattern.compile("prompt\\(.*");
  private final static Pattern xssp14 = Pattern.compile("confirm\\(.*");
  private final static Pattern xssp15 = Pattern.compile("\\(\\)");
  private final static Pattern xssp16 = Pattern.compile("unescape\\(\\)");

  /*
   * private static List<Pattern> xsspatterns;
   * 
   * public URLFilter() { List<Pattern> xsspatterns = new ArrayList<Pattern>();
   * xsspatterns.add(xssp0); xsspatterns.add(xssp1); xsspatterns.add(xssp2);
   * xsspatterns.add(xssp3); xsspatterns.add(xssp4); xsspatterns.add(xssp5);
   * xsspatterns.add(xssp6); xsspatterns.add(xssp7); xsspatterns.add(xssp8);
   * xsspatterns.add(xssp9); xsspatterns.add(xssp10); xsspatterns.add(xssp11);
   * xsspatterns.add(xssp12); xsspatterns.add(xssp13); xsspatterns.add(xssp14);
   * xsspatterns.add(xssp15); xsspatterns.add(xssp16); }
   */

  public boolean isXssVulnarable(HttpServletRequest request) {
    try {

      List<Pattern> xsspatterns = new ArrayList<Pattern>();
      xsspatterns.add(xssp0);
      xsspatterns.add(xssp1);
      xsspatterns.add(xssp2);
      xsspatterns.add(xssp3);
      xsspatterns.add(xssp4);
      xsspatterns.add(xssp5);
      xsspatterns.add(xssp6);
      xsspatterns.add(xssp7);
      xsspatterns.add(xssp8);
      xsspatterns.add(xssp9);
      xsspatterns.add(xssp10);
      xsspatterns.add(xssp11);
      xsspatterns.add(xssp12);
      xsspatterns.add(xssp13);
      xsspatterns.add(xssp14);
      xsspatterns.add(xssp15);
      xsspatterns.add(xssp16);

      request.setCharacterEncoding("UTF-8");
      if (request.getQueryString() != null) {
        String queryString = request.getQueryString();
        queryString = queryString.replaceAll("\"", "&quot;");
        queryString = queryString.replaceAll("\'", "&#39;");
        queryString = queryString.replaceAll("`", "&#96;");
        try {
          queryString = escapeString(queryString);
        } catch (Exception e) {
          System.out.println("Error decoding query string");
        }
        queryString = queryString.toLowerCase();

        logger
            .debug("*****   xsspatterns.size == (" + xsspatterns.size() + ")");

        for (int i = 0; i < xsspatterns.size(); i++) {

          if (xsspatterns.get(i) == null) {

            logger.debug("*****   xsspatterns.get(" + i
                + ") is null in URL Filter");

            return false;
          }
          Matcher m1 = xsspatterns.get(i).matcher(queryString);
          if (m1.find()) {

            logger.info("Contains script..matches " + m1.group() + ", query="
                + queryString);

            return true;
          }
        }
      }

      Enumeration<String> reqparams = request.getParameterNames();
      while (reqparams.hasMoreElements()) {
        String key = (String) reqparams.nextElement();
        String value = request.getParameter(key);
        try {
          key = escapeString(key);
          value = escapeString(value);
        } catch (Exception ue) {
          System.out.println("Error decoding key/value");
        }
        key = key.toLowerCase();
        value = value.toLowerCase();

        for (int i = 0; i < xsspatterns.size(); i++) {
          Pattern p = xsspatterns.get(i);
          Matcher m1 = p.matcher(key);
          if (m1.find()) {
            System.out.println("Contains script...matches " + m1.group()
                + ", key=" + key);
            return true;
          }
          Matcher m2 = p.matcher(value);
          if (m2.find()) {
            System.out.println("Contains script...matches " + m2.group()
                + ", value=" + value + ", key=" + key);
            if (value.equals("executive_document") && key.contains("advsearch")) {
              return false;
            }
            return true;
          }
        }
      }

    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    return false;
  }

  protected static String escapeString(String string) {
    if (string != null) {
      string = string.replaceAll("\"", "&quot;");
      string = string.replaceAll("\'", "&#39;");
      string = string.replaceAll("`", "&#96;");
    }
    return string;
  }
}