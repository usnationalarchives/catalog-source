package gov.nara.opa.ingestion.dasexportutility;

import java.net.URI;

public class OpaStorage {
  private static final int DEFAULT_LEVEL1_DIRS = 100;  
  private static final int DEFAULT_LEVEL2_DIRS = 10000;
  
  private final String naid;
  private final int naidInteger;
  private final String baseDir;
  
  public OpaStorage(String naid){
    this.naid = naid;    
    this.naidInteger = Integer.parseInt(naid);
    baseDir = String.format("/opt/vol/opastorage/live/%s/%s/%s", 
            getLevel1(), getLevel2(), naid);
  }
  
  public int getLevel1(){      
    return naidInteger % DEFAULT_LEVEL1_DIRS;
  }
  
  public int getLevel2(){
    return (naidInteger/DEFAULT_LEVEL1_DIRS) % DEFAULT_LEVEL2_DIRS;
  }
  
  public String getContentFilename(URI uri){
    return String.format("%s/content%s", baseDir, uri.getPath());
  }
}
