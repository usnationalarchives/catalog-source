package gov.nara.opa.architecture.utils;

import gov.nara.opa.architecture.exception.OpaRuntimeException;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;

/**
 * @author aolaru
 * @data Sep 29, 2014
 */
public class ObjectsXmlUtils {

  public static boolean objectIdExists(String objectsXml, String objectId) {
    AspireObject ao = new AspireObject("root");
    try {
      ao.loadXML(new StringReader(objectsXml));
      AspireObject objectsRootAo = ao.get("objects");
      if (objectsRootAo != null) {
        List<AspireObject> objectsAo = objectsRootAo.getChildren();
        
        for (AspireObject objectAo : objectsAo) {
          String objectIdAo = objectAo.getAttribute("id");
          if (objectIdAo != null && objectIdAo.equals(objectId)) {
            return true;
          }
        }
      }

    } catch (AspireException e) {
      throw new OpaRuntimeException(e);
    } finally {
      try {
        ao.close();
      } catch (IOException e) {
        throw new OpaRuntimeException(e);
      }
    }
    
    return false;
  }
  
  /**
   * Retrieves the path to image tiles for a particular object
   * 
   * @param objectsXml
   *          - The string containing the objectsXml XML
   * @param objectId
   *          - the id for the object whose ImageTiles path is to be retrieved
   * @return null if the path canțt be found or the path string if it was found
   */
  public static String getObjectFileImageTilesPath(String objectsXml,
      String objectId) {
    AspireObject ao = new AspireObject("root");
    try {
      ao.loadXML(new StringReader(objectsXml));
      AspireObject objectsRootAo = ao.get("objects");
      if (objectsRootAo == null) {
        return null;
      }

      List<AspireObject> objectsAo = objectsRootAo.getChildren();
      for (AspireObject objectAo : objectsAo) {
        String objectIdAo = objectAo.getAttribute("id");
        if (objectIdAo != null && objectIdAo.equals(objectId)) {
          AspireObject imageTilesAo = objectAo.get("imageTiles");
          if (imageTilesAo == null) {
            return null;
          }
          return imageTilesAo.getAttribute("path");
        }
      }
    } catch (AspireException e) {
      throw new OpaRuntimeException(e);
    } finally {
      try {
        ao.close();
      } catch (IOException e) {
        throw new OpaRuntimeException(e);
      }
    }

    return null;
  }
}
