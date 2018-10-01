package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.Component;
import com.searchtechnologies.aspire.services.Job;
import com.searchtechnologies.aspire.services.logging.ALogger;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 * Creates zoom images for an image digital object.
 */
public class TileCreator {
  final Component component;
  final ALogger logger;
  final JobInfo jobInfo;
  final OpaStorage opaStorage;
  
  public TileCreator(Component component, Job job){
    this.component = component;
    this.logger = (ALogger)this.component;
    this.jobInfo = Jobs.getJobInfo(job);
    this.opaStorage = jobInfo.getOpaStorage();
  }
  
  public void CreateTiles() throws AspireException, IOException{    
        
    String pathToContent = jobInfo.getPathToContent();
    String pathToDeepZoomImage = jobInfo.getPathToDeepZoomImage();
    
    if (opaStorage.isFileNewer(pathToContent, pathToDeepZoomImage)){
      
      String pathToTilesDir = jobInfo.getPathToDeepZoomFilesDir();

      opaStorage.deleteFiles(pathToTilesDir);
      
      File contentFile = jobInfo.getContentFile();
      File baseDeepZoomImageFile = OpaFileUtils.getTempFile(UUID.randomUUID().toString());
      VIPS vips = new VIPS(component, contentFile, baseDeepZoomImageFile);
      vips.createTiles();    
      
      File deepZoomImageFile = new File(baseDeepZoomImageFile + ".dzi");
      opaStorage.saveFileAsPublic(deepZoomImageFile, pathToDeepZoomImage);
      
      File tilesDir = new File(baseDeepZoomImageFile + "_files");
      saveTiles(tilesDir, pathToTilesDir);
      
      logger.info("Created tile %s", pathToDeepZoomImage);   
      
      baseDeepZoomImageFile.delete();
      deepZoomImageFile.delete();
      FileUtils.deleteDirectory(tilesDir);
    }
    
  }
  
  private void saveTiles(File tilesDir, String pathToTilesDir) throws AspireException{
    int lenghtOfTilesBaseDir = tilesDir.getAbsolutePath().length();
    
    Collection<File> tiles = FileUtils.listFiles(tilesDir, null, true);

    Map<String, File> files = new LinkedHashMap<>(tiles.size());

    for (File file : tiles){
      String relativePathToTile = file.getAbsolutePath().substring(lenghtOfTilesBaseDir);
      String key =
        FilenameUtils.separatorsToUnix(pathToTilesDir + relativePathToTile);
      files.put(key, file);
    }

    opaStorage.saveFilesAsPublic(files);
  }
}
