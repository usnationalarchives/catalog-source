/**
 * Copyright Search Technologies 2012
 */
package gov.nara.opa.ingestion;


import com.searchtechnologies.aspire.framework.Standards;
import com.searchtechnologies.aspire.scanner.DSConnection;
import com.searchtechnologies.aspire.scanner.ItemType;
import com.searchtechnologies.aspire.scanner.LinearSourceInfo;
import com.searchtechnologies.aspire.scanner.SourceItem;
import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;
import com.searchtechnologies.aspire.services.JobEvent;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.io.FileUtils;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Deque;
import java.util.LinkedList;
import java.util.UUID;

/**
 * Files from the input directory are obtained recursively.
 *
 * Files that are not DAS exports are processed first,
 * then DAS exports are processed ordered by name, ascending.
 *
 * If a given file is a multi-record xml file or a DAS export (i.e. a .tar.gz archive),
 * each record is extracted into a temporary file.
 *
 * File Unit, Item and ItemAv records are processed last. This is done to support the
 * feature to populate HMS entry numbers from parent Series into these types of records.
 * That guarantees that all Series will be processed before all File Units and Items.
 * HMS entry numbers are pulled from the corresponding Series if File Units/Items do not
 * have HMS entry numbers defined.
 *
 * File Unit, Item and ItemAv records are extracted and moved into temporary directories,
 * each of those directories have a maximum number of 10,000 files.
 *
 * This class keeps track of all temporary files and directories, and tries to delete them
 * when processing is done or aborted.
 *
 */
public class DasSourceInfo extends LinearSourceInfo {
  private Path inputDir;
    private final Deque<DasXmlIterator> iterators = new LinkedList<>();
    private DasXmlIterator currentIterator;
    private final XMLOutputter outputter = new XMLOutputter();
    private Deque<Path> dasExports;

    private final Predicate<File> elementIsFileUnitOrItem =
            new Predicate<File>() {
                @Override
                public boolean evaluate(File file) {
                    String name = getRootElementName(file);
                    return Records.FILE_UNIT_TAG.equals(name) ||
                            Records.ITEM_TAG.equals(name) ||
                            Records.ITEM_AV_TAG.equals(name);
                }
            };

    private String getRootElementName(File file) {
        try (InputStream inputStream = new FileInputStream(file)){
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(inputStream, StandardCharsets.UTF_8.toString());
            reader.nextTag();
            String name = reader.getLocalName();
            reader.close();
            return name;
        } catch (IOException | XMLStreamException e) {
            return null;
        }
    }

    private final Predicate<File> elementIsNeitherFileUnitNorItem =
            new Predicate<File>() {
                @Override
                public boolean evaluate(File file) {
                    String name = getRootElementName(file);
                    return !(Records.FILE_UNIT_TAG.equals(name) ||
                            Records.ITEM_TAG.equals(name) ||
                            Records.ITEM_AV_TAG.equals(name));
                }
            };

  public DasSourceInfo(){
    sourceType="das".toLowerCase();
  }

    @Override
    public void onScanStop() {
        stop();
    }

    @Override
    public void onScanPause(){
        stop();
    }

    private void stop(){

    }

    public void setInputDir(String dir){
        inputDir = Paths.get(dir);
    }
  
  public void initialize() throws AspireException{
      dasExports = new DasExportDirectory().getFiles(inputDir);

      moveToNextIterator();
  }

    @Override
  public void populateSourceItem(SourceItem item) throws AspireException{
  }

  @Override
  public DSConnection newDSConnection() {
    DasDSConnection connection = new DasDSConnection();
    return connection;
  }

    @Override
    public void jobComplete(Standards.Scanner.Action action, JobEvent jobEvent) throws AspireException {
    }

    @Override
  public boolean hasNextItem() throws AspireException {
      return currentIterator != null && currentIterator.hasNext();
  }

    @Override
    public SourceItem getNextItem() throws AspireException {
        File record = currentIterator.next();
        String fullPathToItem = record != null ? currentIterator.getName() : null;

        if (!currentIterator.hasNext()){
            currentIterator.close();
            moveToNextIterator();
        }

        return record != null ? createSourceItem(record, fullPathToItem) : null;
    }

    private void moveToNextIterator() throws AspireException {
        if (!iterators.isEmpty()){
            currentIterator = iterators.removeFirst();
        } else if (!dasExports.isEmpty()){
            File dasExport = dasExports.removeFirst().toFile();
            currentIterator = new FilteredDasXmlFileIterator(dasExport, elementIsNeitherFileUnitNorItem);
            iterators.add(new FilteredDasXmlFileIterator(dasExport, elementIsFileUnitOrItem));
        } else {
            currentIterator = null;
        }
    }

    private SourceItem createSourceItem(File file, String fullPathToItem) throws AspireException{

        SourceItem item = new SourceItem(fullPathToItem);

        ItemType type = new DasItemType();
        type.setValue(DasItemType.DasItemTypeEnum.file);
        item.setItemType(type);

        item.addField("record", createRecord(file));
        item.addField("md5", Digests.md5Hex(file));
        item.addField("forceFeed", fullCrawl());

        file.delete();

        return item;
    }

    private AspireObject createRecord(File file) throws AspireException{
        try(Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))){
            return AspireObject.createFromXML(reader);
        } catch (IOException e) {
            throw new AspireException("save to file", e);
        }
    }
}
