package gov.nara.opa.ingestion.dasexportutility;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

public class DasXmlFileIterator {
  private final XMLStreamReader reader;

  public DasXmlFileIterator(File source) throws XMLStreamException, IOException{
    this.reader =  XMLInputFactory.newInstance().createXMLStreamReader(new FileInputStream(source), StandardCharsets.UTF_8.toString());
    initialize();
  }


  private void initialize() throws XMLStreamException{
    moveToNextStartElement();

    if (reader.isStartElement() && "das_items".equals(reader.getLocalName())){
      moveToNextStartElement();
    }
  }

  public boolean hasNext(){
    return reader.isStartElement();
  }

  public File next() throws IOException, XMLStreamException{
    return getNextRecord();
  }

  private File getNextRecord() throws IOException, XMLStreamException {
    File file = FileUtils.getFile(FileUtils.getTempDirectory(), UUID.randomUUID().toString() + ".xml");

    try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))){
      XMLStreamWriter writer = XMLOutputFactory.newFactory().createXMLStreamWriter(bufferedWriter);
      writer.writeStartDocument("utf-8", "1.0");
      writer.writeCharacters("\n");

      writer.writeStartElement(reader.getLocalName());
      writer.writeDefaultNamespace(reader.getNamespaceURI());    

      int recordTagOccurrences = 0;
      String recordTag = reader.getLocalName();
      recordTagOccurrences++;

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
    } 

    moveToNextStartElement();

    return file;
  }
  
  private void moveToNextStartElement() throws XMLStreamException{
    int eventType;
    while(reader.hasNext()){
      eventType = reader.next();
      if (eventType == XMLStreamReader.START_ELEMENT){
        break;
      }
    }
  }

  private void writeAttributes(XMLStreamReader reader, XMLStreamWriter writer) throws XMLStreamException{
    int attributeCount = reader.getAttributeCount();
    for (int i = 0; i < attributeCount; i++) {
      writer.writeAttribute(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
    }
  }
  
  public void close() throws XMLStreamException{
	reader.close();
  }
}