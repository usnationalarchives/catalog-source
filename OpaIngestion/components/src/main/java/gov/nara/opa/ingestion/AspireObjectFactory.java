package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class AspireObjectFactory {
  
  public AspireObject createFromPath(Path file) throws AspireException{
    try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)){      
      return AspireObject.createFromXML(reader);
    } catch (Exception ex) {
      throw new AspireException("AspireObject.createFromXML", ex, "Failed to load file %s", file);
    }  
  }
}
