package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import org.apache.commons.collections4.Predicate;

import java.io.File;

public class FilteredDasXmlFileIterator implements DasXmlIterator {
  private final DasXmlIterator iterator;
    private final Predicate<File> filter;

    public FilteredDasXmlFileIterator(File file, Predicate<File> filter) throws AspireException{
        this.filter = filter;
        this.iterator = createDasXmlIterator(file);
  }

  private DasXmlIterator createDasXmlIterator(File file) throws AspireException{
      return isArchive(file) ? new DasExportFileIterator(file) : new DasXmlFileIterator(file);
  }
  
  public void close() throws AspireException {
      iterator.close();
  }

    @Override
    public String getName() {
        return iterator.getName();
    }

    public boolean hasNext() throws AspireException {
    return iterator.hasNext();
  }

  public File next() throws AspireException {
    while (hasNext()){
        File next = iterator.next();

        if (filter.evaluate(next)){
            return next;
        } else {
            next.delete();
        }
    }
      return null;
  }

    private boolean isArchive(File file){
        return Archives.isArchive(file.toString());
    }
  
}
