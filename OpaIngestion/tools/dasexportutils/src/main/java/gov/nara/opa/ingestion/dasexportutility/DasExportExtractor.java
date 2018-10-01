package gov.nara.opa.ingestion.dasexportutility;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.zip.GZIPOutputStream;

public class DasExportExtractor {
    private final File sourceExport;
    private final File destinationExport;

    public DasExportExtractor(File sourceExport, File destinationExport) throws IOException{

        this.sourceExport = sourceExport;
        this.destinationExport = destinationExport;
    }

    public void extract() throws IOException, XMLStreamException, JDOMException {

        try (ArchiveInputStream archiveInputStream = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(sourceExport))))) {

            try (TarArchiveOutputStream archiveOutputStream = new TarArchiveOutputStream(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(destinationExport))))) {

                ArchiveEntry archiveEntry;

                while ((archiveEntry = archiveInputStream.getNextEntry()) != null) {

                    System.out.println(archiveEntry.getName());

                    File entryFile = FileUtils.getFile(FileUtils.getTempDirectory(), UUID.randomUUID().toString() + ".xml");

                    try (OutputStream out = new FileOutputStream(entryFile)) {
                        IOUtils.copy(archiveInputStream, out);
                    }

                    DasXmlFileIterator iterator = new DasXmlFileIterator(entryFile);

                    while (iterator.hasNext()) {
                        File recordFile = iterator.next();
                        addToArchive(recordFile, archiveOutputStream);
                        recordFile.delete();
                    }

                    entryFile.delete();
                }

                archiveOutputStream.finish();
            }
        }
    }

    private void addToArchive(File recordFile, TarArchiveOutputStream outputStream) throws IOException, JDOMException {
        SAXBuilder builder = new SAXBuilder();

        Document document = builder.build(recordFile);

        Element root = document.getRootElement();

        Element naIdElement = root.getChild("naId", root.getNamespace());

        if (naIdElement == null) {
            return;
        }

        String naIdText = naIdElement.getText();

        long naId = Long.parseLong(naIdText);
        Long level1 = naId % 100;
        Long level2 = (naId / 100) % 10000;

        File destinationPath = FileUtils.getFile(level1.toString(), level2.toString(), naIdText + ".xml");

        TarArchiveEntry archiveEntry = new TarArchiveEntry(recordFile, destinationPath.toString());
        archiveEntry.setSize(recordFile.length());
        outputStream.putArchiveEntry(archiveEntry);
        try(InputStream inputStream = new FileInputStream(recordFile)) {
            IOUtils.copy(inputStream, outputStream);
        }
        outputStream.closeArchiveEntry();
    }
}
