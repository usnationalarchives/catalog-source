package gov.nara.opa.ingestion;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Methods in this class build directory structures based on NAIDs.
 */
public final class NAIDDirectories {
  private static final int DEFAULT_LEVEL1_DIRS = 100;  
  private static final int DEFAULT_LEVEL2_DIRS = 10000;
     
  /**
     * This function create the name of the level 1 folder.
     * @param naId is needed to get the name of level 1
     * @param numLevel1Dirs depending on this value, we take some values from the naId
     * @return the variable level1 with the name of level 1
     */
  public static int getLevel1(int naId){
      int level1 = naId % DEFAULT_LEVEL1_DIRS;
      return level1;
  }
  
  public static String getLevel1String(int naId){
    return Integer.toString(getLevel1(naId));
  }
    
     /**
      * This function create the name of the level 2 folder.
      * @param naId is needed to get the name of level 2
      * @param numLevel1Dirs depending on this value, we take some values from the naId
      * @return the variable level2 with the name of level 2
      */
  public static int getLevel2(int naId){
    int level2 = (naId/DEFAULT_LEVEL1_DIRS) % DEFAULT_LEVEL2_DIRS;
    return level2;
  }
  
  public static String getLevel2String(int naId){
    return Integer.toString(getLevel2(naId));
  }
  
  public static Path getSecondLevelDir(String rootDir, int naId){
    return Paths.get(rootDir, getLevel1String(naId), getLevel2String(naId));
  }
  
  public static Path getThirdLevelDir(String rootDir, int naId){
    return getSecondLevelDir(rootDir, naId).resolve(Integer.toString(naId));
  }
  
  public static Path getSecondLevelDir(Path baseDir, int naId){
    return
      baseDir
        .resolve(getLevel1String(naId))
        .resolve(getLevel2String(naId));
  }
  
  public static Path getThirdLevelDir(Path baseDir, Integer naid){
    return 
      getSecondLevelDir(baseDir, naid)
        .resolve(naid.toString());
  }
}
