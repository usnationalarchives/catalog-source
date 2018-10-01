package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.io.input.CloseShieldInputStream;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class DasExportFileIterator implements DasXmlIterator{
  private final ArchiveInputStream archiveInputStream;
    private final File sourceFile;
    private ArchiveEntry archiveEntry;
    private DasXmlFileIterator iterator;
    private File item;
    private String itemName;
  
  public DasExportFileIterator(File file) throws AspireException{
      sourceFile = file;
      archiveInputStream = Archives.createArchiveInputStream(file);
  }
  
  @Override
  public void close() throws AspireException {
      try {
          archiveInputStream.close();

          closeIterator();
      } catch (IOException e) {
          throw new AspireException("close", e);
      }
  }

    private void closeIterator() throws AspireException{
        if (iterator != null){
            iterator.close();
        }
    }

    @Override
    public String getName() {
        return itemName;
    }

    public boolean hasNext() throws AspireException {
        if (iterator == null || !iterator.hasNext()){

            closeIterator();

            moveToNextArchiveEntry();

            iterator = getNextIterator();
        }

	    return iterator != null && iterator.hasNext();
  }

    private void moveToNextArchiveEntry() throws AspireException{
        try {
            do {
                archiveEntry = archiveInputStream.getNextEntry();
            } while (archiveEntry != null && archiveEntry.isDirectory());
        } catch (IOException e) {
            throw new AspireException("get next archiveEntry", e);
        }
    }

    private DasXmlFileIterator getNextIterator() throws AspireException{
        return archiveEntry != null ? createDasXmlFileIterator() : null;
    }
  
  private DasXmlFileIterator createDasXmlFileIterator() throws AspireException{
      return new DasXmlFileIterator(createEntryReader(), getFullPathToCurrentFile());
  }

    private String getFullPathToCurrentFile() {
        return sourceFile + "/" + archiveEntry.getName();
    }

    private Reader createEntryReader(){
	  return new BufferedReader(new InputStreamReader(new CloseShieldInputStream(archiveInputStream), StandardCharsets.UTF_8));
  }

  public File next() throws AspireException {
      item = iterator.next();
      itemName = iterator.getName();
      return item;
  }
}
