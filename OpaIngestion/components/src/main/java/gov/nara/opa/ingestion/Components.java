package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.Component;

/**
 * Returns application components defined in the ingestion.xml app file.
 */
public class Components {
  public static IngestionDb getIngestionDb(Component context) throws AspireException{
    return (IngestionDb)context.getComponent("/Ingestion/IngestionDb");
  }
    
  public static Quarantine getQuarantine(Component context) throws AspireException{
    return (Quarantine)context.getComponent("/Ingestion/Quarantine");
  }
  
  public static Settings getSettings(Component context) throws AspireException{
    return (Settings)context.getComponent("/Ingestion/Settings");
  }
  
  public static Logger getLogger(Component context) throws AspireException {
    return (Logger)context.getComponent("/Ingestion/Logger");
  }
  
  public static OpaStorageFactory getOpaStorageFactory(Component context) throws AspireException {
    return (OpaStorageFactory)context.getComponent("/Ingestion/OpaStorageFactory");
  }

  public static DasPathsWhiteList getDasPathsWhiteList(Component context) throws AspireException {
    return (DasPathsWhiteList)context.getComponent("/Ingestion/DasPathsWhiteList");
  }
}
