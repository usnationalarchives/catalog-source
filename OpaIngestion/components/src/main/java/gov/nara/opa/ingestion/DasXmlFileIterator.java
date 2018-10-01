
package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import org.apache.commons.io.FileUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import javax.xml.stream.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class DasXmlFileIterator implements DasXmlIterator {

    private final String sourceFilename;
    private final XMLStreamReader reader;
    private File item;
    private String itemName;

  public DasXmlFileIterator(Reader reader, String sourceFilename) throws AspireException{
      this.sourceFilename = sourceFilename;
      this.reader = createXMLReader(reader);
      initialize();
  }

    private XMLStreamReader createXMLReader(Reader reader) throws AspireException {
        try {
            return XMLInputFactory.newInstance().createXMLStreamReader(reader);
        } catch (XMLStreamException e) {
            throw new AspireException("createXMLStreamReader", e);
        }
    }

    public DasXmlFileIterator(File file) throws AspireException{
        this.sourceFilename = file.toString();
        this.reader = createXMLReader(file);
        initialize();
    }

    private XMLStreamReader createXMLReader(File file) throws AspireException {
        try {
            return XMLInputFactory.newInstance().createXMLStreamReader(new FileInputStream(file), StandardCharsets.UTF_8.toString());
        } catch (XMLStreamException | FileNotFoundException e) {
            throw new AspireException("createXMLStreamReader", e);
        }
    }

    private void initialize() throws AspireException{
        moveToNextStartElement();

        if (reader.isStartElement() && Records.DAS_ITEMS_TAG.equals(reader.getLocalName())){
            moveToNextStartElement();
        }
    }

    private void moveToNextStartElement() throws AspireException{
        try {
            int eventType;
            while(reader.hasNext()){
                eventType = reader.next();
                if (eventType == XMLStreamReader.START_ELEMENT){
                    break;
                }
            }
        } catch (XMLStreamException e) {
            throw new AspireException("moveToNextStartElement", e);
        }
    }

    @Override
    public boolean hasNext() throws AspireException{
        return reader.isStartElement();
    }

    @Override
    public File next() throws AspireException{
        return (item = getNextRecord());
    }

    private File getNextRecord() throws AspireException {

        try {
            File file = File.createTempFile("DasXml", null);

            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));

            XMLStreamWriter writer = XMLOutputFactory.newFactory().createXMLStreamWriter(bufferedWriter);
            writer.writeStartDocument("utf-8", "1.0");
            writer.writeCharacters("\n");

            String recordTag = reader.getLocalName();
            this.itemName = recordTag;

            writer.writeStartElement(recordTag);

            String namespaceURI = reader.getNamespaceURI();
            if (namespaceURI != null) {
                writer.writeDefaultNamespace(namespaceURI);
            }

            int recordTagOccurrences = 1;
            boolean done = false;

            while(!done){
                int eventType = reader.next();

                switch(eventType){

                    case XMLStreamReader.START_ELEMENT:
                        String localName = reader.getLocalName();
                        writer.writeStartElement(localName);

                        if (localName.equals(recordTag)){
                            recordTagOccurrences++;
                        }

                        writeAttributes(reader, writer);
                        break;

                    case XMLStreamReader.CHARACTERS:
                        writer.writeCharacters(reader.getText());
                        break;

                    case XMLStreamReader.END_ELEMENT:
                        writer.writeEndElement();

                        if (reader.getLocalName().equals(recordTag)){
                            recordTagOccurrences--;
                        }

                        if (recordTagOccurrences == 0){
                            done = true;
                        }
                        break;
                }
            }

            writer.close();
            bufferedWriter.close();

            moveToNextStartElement();

            return file;

        } catch (IOException | XMLStreamException e) {
            throw new AspireException("getNextRecord", e);
        }
    }

    private void writeAttributes(XMLStreamReader reader, XMLStreamWriter writer) throws XMLStreamException{
        int attributeCount = reader.getAttributeCount();
        for (int i = 0; i < attributeCount; i++) {
            writer.writeAttribute(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
        }
    }

    @Override
    public void close() throws AspireException {
        try {
            reader.close();
        } catch (XMLStreamException e) {
            throw new AspireException("close", e);
        }
    }

    @Override
    public String getName() {
        return item != null ? getFullPathToItem() : null;
    }

    private String getFullPathToItem(){
        StringBuilder sb = new StringBuilder(
                sourceFilename + "/" + itemName
        );

        String naId = getNaId(item);
        if (naId != null){
            sb.append('/');
            sb.append(naId);
        }

        return sb.toString();
    }

    private String getNaId(File file) {
        try {
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(file);
            Element element = document.getRootElement();
            Element naId = element.getChild("naId", element.getNamespace());
            return naId != null ? naId.getText() : null;
        } catch (JDOMException | IOException e) {
            return null;
        }
    }


}

