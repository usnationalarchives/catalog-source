package gov.nara.opa.common.services;

import gov.nara.opa.architecture.logging.OpaLogger;

import java.util.HashSet;
import java.util.Set;

import net.sf.saxon.s9api.Processor;

public class SingletonServices {

  public static final Processor SAXON_PROCESSOR = new Processor(false);
  static final OpaLogger logger = OpaLogger.getLogger(SingletonServices.class);
  static {
    logger.info("JVM file.encoding: " + System.getProperty("file.encoding"));
    logger.info("JVM user.timezone: " + System.getProperty("user.timezone"));
  }

  public static final Set<String> DAS_WHITE_LIST = new HashSet<String>();

  public static final Set<String> SOLR_FIELDS_WHITE_LIST = new HashSet<String>();

  public static final Set<String> SOLR_FIELDS_INTERNAL_WHITE_LIST = new HashSet<String>();

  public static final Set<String> SOLR_FIELDS_STARTS_WITH_LIST = new HashSet<String>();
  
  public static final Set<String> SOLR_RESULT_FIELDS_WITH_LIST = new HashSet<String>();

}
