package gov.nara.opa.api.services.system;

import gov.nara.opa.api.system.Config;

public interface ConfigurationService {

  /**
   * Method to get the config.xml parameter values from the default location
   * 
   * @return Config Object containing config.xml parameters
   */
  public Config getConfig();
  
  /**
   * Method to get the config.xml parameter values
   * 
   * @param configXMLfilePath
   *          path to the file config.xml
   * @return Config Object containing config.xml parameters
   */
  public Config getConfig(String configXMLfilePath);
  
  public int getSearchLimitForUser(String accountType);
}
