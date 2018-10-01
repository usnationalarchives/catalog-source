package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Deque;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class DasExportDirectory {
  private static final Pattern EXPORT_FILENAME_PATTERN = Pattern.compile("dasexport_\\d\\d-\\d\\d-\\d\\d\\d\\d.tar.gz");

    /**
     * Returns a deque with DAS export files.
     * Exports whose name follow the pattern 'dasexport_mm-dd-yyyy.tar.gz' are added to the end of the deque,
     * sorted by name - ascending.
     * Files that do not follow the pattern are added to beginning of the deque, in the order they are read.
     */
  public Deque<Path> getFiles(Path dir) throws AspireException{

    final TreeMap<String,Path> dasExports = new TreeMap<>() ;
      final Deque<Path> allExports = new LinkedList<>();

    try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)){
        for (Path entry : stream){
            if (Files.isRegularFile(entry)){
                if (isDasExport(entry)){
                    dasExports.put(entry.getFileName().toString(), entry);
                } else {
                    allExports.add(entry);
                }
            }
        }
    } catch (Throwable e) {
      throw new AspireException("get das exports", e);
    }

      allExports.addAll(dasExports.values());
    return allExports;
  }

  private boolean isDasExport(Path file){
    return EXPORT_FILENAME_PATTERN
            .matcher(file.getFileName().toString())
            .matches();    
  }
  
}
