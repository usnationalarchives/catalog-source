package gov.nara.opa.ingestion.dasexportutility;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.concurrent.*;

public class DigitalObjectsPrinter {
    private final File sourceExport;
    private final int numberOfThreads;
    private final ExecutorService executorService;
    private final BlockingQueue<File> queue;
    private final BlockingDeque<String> results;

    private static final String EOF = "eof";

    public DigitalObjectsPrinter(File sourceExport, int numberOfThreads) throws IOException{

        this.sourceExport = sourceExport;
        this.numberOfThreads = numberOfThreads;
        this.executorService = Executors.newFixedThreadPool(numberOfThreads);
        this.queue = new LinkedBlockingQueue<>();
        this.results = new LinkedBlockingDeque<>();
    }

    public void execute() throws IOException, XMLStreamException, JDOMException {
        Thread printThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    String line = null;

                    try {
                        line = results.take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (EOF.equals(line)){
                        break;
                    }

                    System.out.println(line);
                }
            }
        });
        printThread.start();


        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {

                    while (true) {

                        File file = null;
                        try {
                            file = queue.take();

                            if (EOF.equals(file.getName())) {
                                break;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        try {
                            processFile(file);
                        } catch (IOException | JDOMException e) {
                            e.printStackTrace();
                        }

                        file.delete();
                    }
                }
            });
        }

        try (ArchiveInputStream archiveInputStream = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(sourceExport))))) {
            ArchiveEntry archiveEntry;
            while ((archiveEntry = archiveInputStream.getNextEntry()) != null) {

                if (archiveEntry.isDirectory()){
                    continue;
                }

                File entryFile = File.createTempFile("das", null);

                try (OutputStream out = new FileOutputStream(entryFile)) {
                    IOUtils.copy(archiveInputStream, out);
                }

                DasXmlFileIterator iterator = new DasXmlFileIterator(entryFile);

                while (iterator.hasNext()) {
                    File recordFile = iterator.next();
                    queue.add(recordFile);
                }

                entryFile.delete();
            }
        }

        for (int i = 0; i < numberOfThreads; i++){
            queue.add(new File(EOF));
        }

        executorService.shutdown();

        try {
            executorService.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        results.add(EOF);
    }

    private void processFile(File recordFile) throws IOException, JDOMException {
        SAXBuilder builder = new SAXBuilder();

        Document document = builder.build(recordFile);

        Element root = document.getRootElement();

        if (!Records.RECORD_TAGS.contains(root.getName())) {
            return;
        }

        Namespace namespace = root.getNamespace();

        Element digitalObjectArray = root.getChild("digitalObjectArray", namespace);

        if (digitalObjectArray == null){
            return;
        }

        String naId = root.getChildText("naId", namespace);

        if (naId == null) {
            return;
        }

        for (Element digitalObject : digitalObjectArray.getChildren("digitalObject", namespace)){
            String objectIdentifier = digitalObject.getChildText("objectIdentifier", namespace);
            String accessFileSizeText = digitalObject.getChildText("accessFileSize", namespace);
            String accessFileSize = accessFileSizeText != null ? accessFileSizeText : "0";

            String objectType = digitalObject.getChild("objectType", namespace).getChildText("termName", namespace);

            String line = String.format("%s\t%s\t%s\t%s", naId, objectIdentifier, accessFileSize, objectType);

            results.add(line);
        }
    }
}
