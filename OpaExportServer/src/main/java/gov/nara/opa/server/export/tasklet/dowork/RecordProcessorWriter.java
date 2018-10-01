package gov.nara.opa.server.export.tasklet.dowork;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.exception.OpaSkipRecordException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.common.services.docstransforms.DocumentTransformerService;
import gov.nara.opa.common.services.export.AccountExportDbProxyService;
import gov.nara.opa.common.services.io.FileUtils;
import gov.nara.opa.common.storage.OpaStorage;
import gov.nara.opa.common.storage.OpaStorageFactory;
import gov.nara.opa.common.storage.S3OpaStorageImpl;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.common.valueobject.export.AccountExportValueObjectHelper;
import gov.nara.opa.common.valueobject.export.DigitalObjectValueObject;
import gov.nara.opa.common.valueobject.search.SearchRecordValueObject;
import gov.nara.opa.common.valueobject.search.SearchRecordValueObjectHelper;
import gov.nara.opa.server.export.tasklet.AbstractAccountExportTasklet;
import gov.nara.opa.server.export.valueobject.SolrDocumentUnitOfWork;
import static gov.nara.opa.common.services.docstransforms.Constants.EXPORT_FORMAT_CSV;
import static gov.nara.opa.common.services.docstransforms.Constants.EXPORT_FORMAT_JSON;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


//import gov.nara.opa.api.services.impl.export.ConvertSolrToCSV;
@Component("recordProcessorWriter")
@Scope("step")
public class RecordProcessorWriter extends AbstractAccountExportTasklet implements ItemWriter<SolrDocumentUnitOfWork> {

	@Value("#{stepExecutionContext[recordProcessorId]}")
	private int recordProcessorId;

	@Value("#{stepExecutionContext[searcherId]}")
	private int searcherId;

	@Value("${maxBulkExportFileSize}")
	private long maxBulkExportFileSize;

	@Value(value = "${opaStorage.baseLocation}")
	String opaStorageBaseLocation;

	private File outputFile;

	private FileOutputStream outputFileStream;

	private File outputDirectory;

	private Object writer;

	private long timer;
	private final long LONG_STEP_MILLISECS = 10000;

	@Autowired
	AccountExportDbProxyService accountExportDaoTransactional;

	@Autowired
	DocumentTransformerService documentTransformService;

	@Autowired
	SearchRecordValueObjectHelper searchRecordValueObjectHelper;

	@Autowired
	OpaStorageFactory opaStorageFactory;

	private static OpaLogger logger = OpaLogger.getLogger(RecordProcessorWriter.class);

	@Override
	public void write(List<? extends SolrDocumentUnitOfWork> items) throws Exception {

		logger.debug("Starting write");

		checkBulkFileSizeLimitExceeded();
		Long fileSizeIncrease = (long) 0;
		long oldFileSize = outputFile.length();
		long sizeOfCopiedFiles = 0;
		String exportType = "" + getAccountExport().getExportType();
		AccountExportValueObject accountExport=getAccountExport();
		
		String exportFormat=accountExport.getExportFormat();
		boolean isCSV=exportFormat.toLowerCase().equals(EXPORT_FORMAT_CSV.toLowerCase());
			for (SolrDocumentUnitOfWork item : items) {
				SearchRecordValueObject searchRecord = null;
				AccountExportValueObject clone=getAccountExport().clone();
				try {

					logger.debug(String.format("Creating search record for batch: %1$d", item.getBatchIndex()));
					
					searchRecord = searchRecordValueObjectHelper.createSolrRecord(item.getDocument(),
							clone);
				} catch (OpaSkipRecordException ex) {
					logger.debug("Document skipped. Exception message: " + ex.getMessage(), ex);
					searchRecord = null;
				}

				if (searchRecord != null) {
					logger.debug(String.format("Calling document transformation for search record with opaId: %1$s",
							searchRecord.getOpaId()));
				} else {
					logger.debug("Search record is null");
				}
				if(isCSV){
					clone.setExportFormat(EXPORT_FORMAT_JSON);
				}
				try{
				documentTransformService.transformDocument(searchRecord, clone, outputFileStream,
						item.getDocumentIndex(), item.getTotalRecords(), false, writer, null);
				}catch(Exception e){
					logger.error("ERROR: document transform faile.\n"+stackTraceToString(e),e);;
				}
				sizeOfCopiedFiles = sizeOfCopiedFiles + Long.valueOf(copyFiles(searchRecord, getAccountExport()));
			}
		
		outputFileStream.flush();
		long newFileSize = outputFile.length();
		fileSizeIncrease = sizeOfCopiedFiles + newFileSize - oldFileSize;

		logger.debug(String.format("File size increased: %1$d", fileSizeIncrease));

		accountExportDaoTransactional.incrementRecordsProcessed(items.size(), fileSizeIncrease, getExportId());
	}
	public static String stackTraceToString(Exception e){
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		return errors.toString();
	}
	private void checkBulkFileSizeLimitExceeded() {
		AccountExportValueObject accountExport = accountExportDaoTransactional.selectById(getExportId());

		long currentFileSize = accountExport.getFileSize() == null ? (long) 0 : accountExport.getFileSize();
		if (currentFileSize > maxBulkExportFileSize) {
			throw new OpaRuntimeException(
					"The size of the current export resulted in a file that is larger then the allowable maximum value of "
							+ maxBulkExportFileSize + " bytes");
		}

	}

	private Long copyFiles(SearchRecordValueObject document, AccountExportValueObject accountExport)
			throws IOException {
		List<String> exportContent = accountExport.getBulkExportContent();
		if (exportContent == null) {
			return (long) 0;
		}

		long totalSizeOfCopiedFiles = 0;

		for (DigitalObjectValueObject digitalObject : document.getObjects().values()) {
			if (exportContent.contains("objects")) {
				String fileSubPath = digitalObject.getFilePath();
				totalSizeOfCopiedFiles = totalSizeOfCopiedFiles + copyFile(fileSubPath, document.getNaId());
			}
			if (exportContent.contains("thumbnails")) {
				String fileSubPath = digitalObject.getThumbnailPath();
				totalSizeOfCopiedFiles = totalSizeOfCopiedFiles + copyFile(fileSubPath, document.getNaId());
			}
		}
		return totalSizeOfCopiedFiles;
	}

	private long copyFile(String fileSubPath, String naId) throws IOException {
		OpaStorage storage = opaStorageFactory.createOpaStorage();
		File inFile = null;
		if (storage instanceof S3OpaStorageImpl) {
			String path = storage.getFullPathInLive(fileSubPath, Integer.parseInt(naId));
			inFile = storage.getFile(path);
		} else {
			String inPath = opaStorageBaseLocation + "/" + FileUtils.getExpandedNaidPath(Integer.valueOf(naId)) + "/"
					+ fileSubPath;
			inFile = new File(inPath);
		}
		String outPath = getExportOutputFinalDir().getAbsolutePath() + "/" + fileSubPath;

		long fileSize = inFile.length();
		if (!inFile.exists()) {
			logger.error("Object file does not exist: " + inFile.getAbsolutePath());
			return 0;
		}
		File outFile = new File(outPath);
		outFile.getParentFile().mkdirs();
		FileOutputStream outFileOS = new FileOutputStream(outFile);
		FileChannel outChannel = outFileOS.getChannel();
		appendFile(outFileOS, outChannel, inFile.getAbsolutePath(), false);
		outChannel.close();
		outFileOS.close();
		return fileSize;
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		super.beforeStep(stepExecution);

		// Set timer
		timer = new Date().getTime();

		outputDirectory = getExportOutputTempSearcherDir(searcherId);
		createBaseDirectory(outputDirectory);
		try {
			outputFile = new File(
					outputDirectory.getAbsolutePath() + "/recs-" + recordProcessorId + "." + getExportFormat());
			outputFileStream = new FileOutputStream(outputFile);
			writer = AccountExportValueObjectHelper.getWriter(getAccountExport(), outputFileStream);
		} catch (IOException e) {
			throw new OpaRuntimeException(e);
		}
	}

	private static synchronized void createBaseDirectory(File outputDirectory) {
		outputDirectory.mkdirs();
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {

		logger.debug("Finishing step");

		try {
			AccountExportValueObjectHelper.closeWriter(getAccountExport(), writer);
			outputFileStream.close();
		} catch (IOException e) {
			throw new OpaRuntimeException(e);
		}
		if (stepExecution.getExitStatus().equals(ExitStatus.COMPLETED)) {
			Map<Integer, String> partialFileCompleteMap = getPartialFilesCompleteMap().get(searcherId);
			partialFileCompleteMap.put(recordProcessorId, outputFile.getAbsolutePath());
		}

		long currentTime = new Date().getTime();
		if (timer - currentTime > LONG_STEP_MILLISECS) {
			logger.info(String.format("ExportId:[%1$d] Step took more than [%2$d] secs [%3$d] for query: [%4$s]",
					getAccountExport().getExportId(), LONG_STEP_MILLISECS, timer - currentTime));
		}

		return stepExecution.getExitStatus();
	}
}
