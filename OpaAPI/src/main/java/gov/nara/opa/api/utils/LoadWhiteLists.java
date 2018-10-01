package gov.nara.opa.api.utils;

import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.common.services.SingletonServices;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LoadWhiteLists implements InitializingBean {

  @Value("${dasWhiteListFileLocation}")
  String dasWhiteListFileLocation;

  @Value("${solrWhiteListFileLocation}")
  String solrWhiteListFileLocation;

  @Value("${solrInternalWhiteListFileLocation}")
  String solrInternalWhiteListFileLocation;

  @Value("${solrInternalStartsWithListFileLocation}")
  String solrInternalStartsWithListFileLocation;
  
  @Value("${solrResultFieldsWhiteListFileLocation}")
  String solrResultFieldsWhiteListFileLocation;
  
  OpaLogger logger = OpaLogger.getLogger(LoadWhiteLists.class);

  @Override
  public void afterPropertiesSet() throws IOException {
    loadTheDasList();
    loadSolrFieldsWhiteList();
    loadSolrFieldsInternalWhiteList();
    loadStartsWithList();
    loadSolrResultFieldsWhiteList();
  }

  private void loadSolrResultFieldsWhiteList() throws IOException {
	    loadFileIntoSet(solrResultFieldsWhiteListFileLocation, SingletonServices.SOLR_RESULT_FIELDS_WITH_LIST,
	        "Solr result fields white list");
  }

  private void loadStartsWithList() throws IOException {
    loadFileIntoSet(solrInternalStartsWithListFileLocation, SingletonServices.SOLR_FIELDS_STARTS_WITH_LIST, 
        "Solr fields starts with list");
  }
  
  private void loadSolrFieldsWhiteList() throws IOException {
    loadFileIntoSet(solrWhiteListFileLocation, SingletonServices.SOLR_FIELDS_WHITE_LIST, "Solr fields white list");
  }

  private void loadSolrFieldsInternalWhiteList() throws IOException {
    loadFileIntoSet(solrInternalWhiteListFileLocation, SingletonServices.SOLR_FIELDS_INTERNAL_WHITE_LIST,
        "Solr fields internal white list");
  }

  private void loadTheDasList() throws IOException {
    loadFileIntoSet(dasWhiteListFileLocation, SingletonServices.DAS_WHITE_LIST,
        "DAS white list");
  }

  private void loadFileIntoSet(String filePath, Set<String> destinationSet, String setName) throws IOException {
    if (destinationSet.size() > 0) {
      return;
    }
    
    long startTime = (new Date()).getTime();
    logger.debug("Loading " + setName + " records from file: " + filePath);
    BufferedReader reader = null;
    int i = 0;

    try {
    	reader = new BufferedReader(new FileReader(filePath));

    	String line = reader.readLine();
    	while (line != null) {
    		destinationSet.add(line.trim());
    		line = reader.readLine();
    		i++;
    	}
    	reader.close();
    } catch (IOException e) {
    	if (reader != null) {
    		reader.close();
    	}
    	throw e;
    }
    long totalTime = (new Date()).getTime() - startTime;
    logger.debug(i + " records were loaded in " + setName + " list in "
        + totalTime + " seconds.");
  }
  
}
