package gov.nara.opa.server.export.tasklet.wrapupwork;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.common.dataaccess.export.AccountExportDao;
import gov.nara.opa.common.services.docstransforms.Constants;
import gov.nara.opa.common.services.docstransforms.impl.ConvertJSONToCsv;
import gov.nara.opa.common.services.export.AccountExportDbProxyService;
import gov.nara.opa.common.storage.StorageUtils;
import gov.nara.opa.common.valueobject.export.AccountExportStatusEnum;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.server.export.tasklet.AbstractAccountExportTasklet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.Queue;
import java.util.TreeSet;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BadPdfFormatException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;

import javax.validation.constraints.NotNull;

@Component("finalizer")
@Scope("step")
public class Finalizer extends AbstractAccountExportTasklet implements Constants {
	@Autowired
	AccountExportDbProxyService accountExportDaoTransactional;

	@Autowired
	StorageUtils storageUtils;

	@Autowired
	AccountExportDao accountExportDao;

	private FileOutputStream finalMetaDataFile;
	private FileChannel finalMetaDataFileChannel;

	// pdf related objects
	private Document finalMetaDataFilePdf;
	private PdfCopy pdfCopy;

	static OpaLogger logger = OpaLogger.getLogger(Finalizer.class);

	class csvFilter implements FilenameFilter {

		@Override
		public boolean accept(File dir, String name) {
			boolean b=name.endsWith(".csv" ) && name.startsWith("nara-export");
			
			return b;
		}

	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		accountExportDaoTransactional.updateStatus(AccountExportStatusEnum.PACKAGING.toString(), getExportId());
		logger.debug("execute");
		initMedatadataFile();
		createMetadataFile();
		String finalFileName = createFinalFile();

		if (!finalFileName.isEmpty()) {
			if (finalFileName.charAt(0) == '/' || finalFileName.charAt(0) == '\\') {
				finalFileName = finalFileName.substring(1);
			}
		}

		AccountExportValueObject accountExport = accountExportDao.selectById(getExportId());
		accountExport.setRequestStatus(AccountExportStatusEnum.COMPLETED);
		accountExport.setCompletedTs(new Timestamp(new Date().getTime()));
		accountExport.setLastActionTs(new Timestamp(new Date().getTime()));
		accountExport.setUrl(finalFileName);
		accountExport.setFileSize(getFinalFileSize(finalFileName));

		String filePath = exportOutputLocation + "/" + finalFileName;
		storageUtils.getFinalUrl(accountExport, filePath, exportOutputLocation);

		accountExportDaoTransactional.update(accountExport);
		// deleteWorkingDirectories();
		return RepeatStatus.FINISHED;
	}

	private long getFinalFileSize(@NotNull String finalFileName) {
		logger.debug("getFinalFileSize");
		String filePath = exportOutputLocation + "/" + finalFileName;

		Long fileSize;
		try {
			fileSize = Files.size(Paths.get(filePath));
			if (finalFileName.endsWith(".csv")) {
				try {
					logger.debug("writing to file:" + finalFileName);
					File f = new File(finalFileName);
					String data = fileToString(f);
					data = "****************************************\n" + data;
					toFile(data, f);
					logger.debug("wrote to file:" + finalFileName);
				} catch (IOException e) {
					logger.error("could not write to file:" + e);
				}
			}
		} catch (IOException e) {
			fileSize = 0L;
		}

		return fileSize;
	}

	private void createMetadataFile() throws IOException, BadPdfFormatException {
		@SuppressWarnings("unchecked")
		Map<Integer, String> searcherFiles = (Map<Integer, String>) getLargeObjectFromExecutionContext(
				SEARCHER_FILES_COMPLETED_QUEUE_OBJECT_NAME);
		TreeSet<Integer> searcherIds = getOderedRecordProcessorIds(searcherFiles.keySet());
		int i = 1;
		logger.debug("createMetadataFile");
		for (Integer searcherId : searcherIds) {
			boolean includeLineSeparator = true;
			if (i == searcherIds.size()) {
				includeLineSeparator = false;
			}
			String inFilePath = searcherFiles.get(searcherId);
			if (EXPORT_FORMAT_PDF.equals(getAccountExport().getExportFormat())) {

				appendPdfFile(inFilePath, finalMetaDataFile);
			} else {
				appendFile(finalMetaDataFile, finalMetaDataFileChannel, inFilePath, includeLineSeparator);
			}

			i++;
		}
		if (getUseJavaTarGz()) {
			File metaFile = getExportOutputFinalMetadataFile();
			TarArchiveEntry entry = new TarArchiveEntry(metaFile);
			entry.setSize(metaFile.length());
			entry.setName(metaFile.getName());
			TarArchiveOutputStream tarGzFile = (TarArchiveOutputStream) getLargeObjectFromExecutionContext(
					FINAL_TAR_GZ_FILE);
			tarGzFile.putArchiveEntry(entry);
			FileInputStream is = new FileInputStream(metaFile);
			IOUtils.copy(is, tarGzFile);
			tarGzFile.closeArchiveEntry();
			is.close();
		}
		closeMedatadataFile();
	}

	private void appendPdfFile(String inFilePath, OutputStream finalFileOs)
			throws FileNotFoundException, IOException, BadPdfFormatException {
		PdfReader reader = new PdfReader(new FileInputStream(inFilePath));
		int n = reader.getNumberOfPages();
		for (int page = 1; page <= n; page++) {
			pdfCopy.addPage(pdfCopy.getImportedPage(reader, page));
		}
		pdfCopy.freeReader(reader);
		reader.close();
		finalFileOs.flush();
	}

	private void initMedatadataFile() throws FileNotFoundException, DocumentException {
		logger.debug("initMedatadataFile");
		finalMetaDataFile = new FileOutputStream(getExportOutputFinalMetadataFile());
		if (EXPORT_FORMAT_PDF.equals(getAccountExport().getExportFormat())) {
			finalMetaDataFilePdf = new Document();
			pdfCopy = new PdfCopy(finalMetaDataFilePdf, finalMetaDataFile);
			finalMetaDataFilePdf.open();
		} else {
			finalMetaDataFileChannel = finalMetaDataFile.getChannel();
		}

	}

	private void closeMedatadataFile() throws IOException {
		logger.debug("closeMedatadataFile");
		if (EXPORT_FORMAT_PDF.equals(getAccountExport().getExportFormat())) {
			finalMetaDataFilePdf.close();
		} else {
			finalMetaDataFileChannel.close();
		}

		finalMetaDataFile.close();
	}

	public static String fileToString(File f) throws IOException {

		FileReader in = new FileReader(f);
		StringBuilder contents = new StringBuilder();
		char[] buffer = new char[4096];
		int read = 0;
		do {
			contents.append(buffer, 0, read);
			read = in.read(buffer);
		} while (read >= 0);
		return contents.toString();
	}

	public static void toFile(String data, File f) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(f));
			writer.write(data);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (writer != null)
					writer.flush();
				writer.close();
			} catch (IOException e) {
			}
		}
	}

	private String createFinalFile() {
		logger.debug("Starting to create tarball");
		File f = getAccountExport().getFileToWriteTo();
		logger.debug("output file:" + f);
		if (f != null && f.exists()) {
			logger.debug("file exists: " + f);

		} else {
			logger.debug("File does not exist:" + f);
		}
		if (getUseJavaTarGz()) {
			@SuppressWarnings("unchecked")
			Queue<File> appendQueue = (Queue<File>) getLargeObjectFromExecutionContext(
					FILES_TO_APPEND_TO_FINAL_TAR_GZ_QUEUE);
			TarArchiveOutputStream tarGzFile = (TarArchiveOutputStream) getLargeObjectFromExecutionContext(
					FINAL_TAR_GZ_FILE);
			File finalArchive = getExportOutputFinalArchiveFile();
			logger.debug("Starting to insert files into tarball");
			for (File file : appendQueue) {
				String name = file.getName();
				if (name.endsWith(".csv")) {
					try {
						String data = fileToString(file);
						data = "****************************************\n" + data;
						toFile(data, f);
					} catch (IOException e) {
						logger.error("could not write to file:" + f);
					}
				}

				TarArchiveEntry entry = new TarArchiveEntry(file);
				entry.setSize(file.length());
				entry.setName(file.getName());
				logger.debug(String.format("Writing file %1$s to tar.gz", file.getName()));
				try {
					tarGzFile.putArchiveEntry(entry);
					tarGzFile.write(Files.readAllBytes(file.toPath()));
					tarGzFile.closeArchiveEntry();
				} catch (IOException e) {
					logger.error(String.format("Cannot add file %1$s to tar.gz file. Reason: %2$s", file.getName(), e));
				}
			}
			try {
				tarGzFile.finish();
				tarGzFile.close();
			} catch (IOException e) {
				logger.error(String.format("Cannot close file %1$s. Reason %2$s", finalArchive.getName(), e));
			}
		} else if (getAccountExport().getBulkExport().booleanValue()) {
			createArchiveFile();
		}

		return getFinalFileUrl();
	}

	private void deleteWorkingDirectories() {
		try {
			FileUtils.deleteDirectory(getExportOutputTempDir());
			FileUtils.deleteDirectory(getExportOutputFinalDir());
		} catch (IOException e) {
			logger.error(String.format("Cannot delete directories %1$s, %2$s. Reason %3$s", getExportOutputTempDir(),
					getExportOutputFinalDir(), e));
		}
	}

	private void createArchiveFile() {
		File archiveFile = getExportOutputFinalArchiveFile();
		if (System.getProperty("os.name").startsWith("Windows")) {
			createWindowsArchiveFile(archiveFile);
		} else {
			createLinuxArchiveFile(archiveFile);
		}
	}

	private void createWindowsArchiveFile(File archiveFile) {
		String directoryToArchive = getExportOutputFinalDir().getAbsolutePath() + "\\*";
		String finalFileName = archiveFile.getAbsolutePath();
		// remove the .gz so from the full file name
		String tarBallFileName = finalFileName.substring(0, finalFileName.length() - 3);
		String tarCommand = String.format("7z a -ttar %1$s %2$s", tarBallFileName, directoryToArchive);
		String gzipCommand = String.format("7z a -tgzip %1$s %2$s", finalFileName, tarBallFileName);
		try {
			java.lang.ProcessBuilder pb = new java.lang.ProcessBuilder(tarCommand);
			Process p = pb.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((reader.readLine()) != null) {
			}
			p.waitFor();
			pb = new java.lang.ProcessBuilder(gzipCommand);
			p = pb.start();
			p.waitFor();
		} catch (IOException | InterruptedException e) {
			throw new OpaRuntimeException(e);
		}
		new File(tarBallFileName).delete();
	}

	private void createLinuxArchiveFile(File archiveFile) {
		String directoryToArchive = getExportOutputFinalDir().getAbsolutePath();
		String finalFileName = archiveFile.getAbsolutePath();

		String tarGzipCommand = String.format("tar -zcvf %1$s -C %2$s .", finalFileName, directoryToArchive);
		File dir = getExportOutputDir();
//		logger.error("searching dir for csv files1: " + dir.getAbsolutePath());
//		if (dir != null) {
//			File[] files = dir.listFiles(new csvFilter());
//			if (files.length == 0) {
//				logger.error("no csv files in dir: " + dir.getAbsolutePath());
//			} else {
//				logger.error("found csv files in dir: " + dir.getAbsolutePath());
//				for (File f : files) {
//					logger.error("found csv file:" + f.getAbsolutePath());
//					transform(f);
//				}
//			}
//		}
//		dir = getExportOutputTempDir();
//		logger.error("searching dir for csv files2: " + dir);
//		if (dir != null) {
//			logger.error("searching dir for csv files: " + dir.getAbsolutePath());
//			File[] files = dir.listFiles(new csvFilter());
//			if (files.length == 0) {
//				logger.error("no csv files in dir: " + dir.getAbsolutePath());
//			} else {
//				logger.error("found csv files in dir: " + dir.getAbsolutePath());
//				for (File f : files) {
//					logger.error("found csv file:" + f.getAbsolutePath());
//					transform(f);
//				}
//			}
//		}
		dir = getExportOutputFinalDir();
		logger.debug("searching dir for csv files3: " + dir);
		if (dir != null) {
			logger.debug("searching dir for csv files: " + dir.getAbsolutePath());
			File[] files = dir.listFiles(new csvFilter());
			if (files.length == 0) {
				logger.debug("no csv files in dir: " + dir.getAbsolutePath());
			} else {
				logger.debug("found csv files in dir: " + dir.getAbsolutePath());
				for (File f : files) {
					logger.debug("found csv file:" + f.getAbsolutePath());
					transform(f);
				}
			}
		}
//		dir = getExportOutputTempSearcherDir(getExportId());
//		logger.error("searching dir for csv files4: " + dir);
//		if (dir != null) {
//			logger.error("searching dir for csv files: " + dir.getAbsolutePath());
//			File[] files = dir.listFiles(new csvFilter());
//			if (files.length == 0) {
//				logger.error("no csv files in dir: " + dir.getAbsolutePath());
//			} else {
//				logger.error("found csv files in dir: " + dir.getAbsolutePath());
//				for (File f : files) {
//					logger.error("found csv file:" + f.getAbsolutePath());
//					transform(f);
//				}
//			}
//		}
		
		logger.debug(String.format("Creating gzip with this command %1$s", tarGzipCommand));
		try {
			java.lang.ProcessBuilder pb = new java.lang.ProcessBuilder(tarGzipCommand);
			Process p = pb.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((reader.readLine()) != null) {
			}
			p.waitFor();
		} catch (IOException | InterruptedException e) {
			throw new OpaRuntimeException(e);
		}
	}
	public void transform(File f){
		try{
			logger.debug("transform "+f);
			String data=fileToString(f);
			if(!data.startsWith("[")){
				data="["+data;
			}
			if(!data.endsWith("]")){
				data=data+"]";
			}
			JSONArray a=new JSONArray(data);
			ConvertJSONToCsv c=new ConvertJSONToCsv(Long.MAX_VALUE, Long.MAX_VALUE);
			c.processJSON(a);
			FileOutputStream out = new FileOutputStream(f);
			logger.debug("transform calling write:"+f);
			c.write(out);
			out.flush();
			out.close();
			//data=c.getCSV();
			//toFile(data,f);
		}catch(Exception e){
			logger.error("transform caught exception:"+e);
		}
	}
}
