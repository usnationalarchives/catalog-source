package gov.nara.opa.server.export.tasklet;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.common.dataaccess.export.AccountExportDao;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.server.export.valueobject.SolrDocumentUnitOfWork;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class AbstractAccountExportTasklet implements Tasklet,
		StepExecutionListener {

	public static final String ACCOUNT_EXPORT_OBJECT_NAME = "current_export";

	public static final String SEARCH_RESULTS_WORK_QUEUE_OBJECT_NAME = "searchResultsQueues";

	public static final String PARTIAL_FILES_COMPLETED_QUEUE_OBJECT_NAME = "partialFilesQueues";

	public static final String SEARCHER_FILES_COMPLETED_QUEUE_OBJECT_NAME = "searcherFilesQueues";

	public static final String FILES_TO_APPEND_TO_FINAL_TAR_GZ_QUEUE = "filesToAppend";

	public static final String FINAL_TAR_GZ_FILE = "finalTarGz";
	public static final String FINAL_GZ_FILE = "finalGz";
	public static final String FINAL_GZ_BUFFERED_OS = "finalGzBufferedOs";
	public static final String FINAL_GZ_OS = "finalGzOs";

	public static final int SEARCHER_ID_MULTIPLIER = 1000;

	private static final Map<Integer, ExecutionContext> ALL_JOB_EXECUTION_CONTEXTS = new ConcurrentHashMap<Integer, ExecutionContext>();

	private static final Map<Integer, Map<String, Object>> EXEUTION_CONTENT_LARGE_OBJECTS = new ConcurrentHashMap<Integer, Map<String, Object>>();

	private ExecutionContext jobExecutionContext;

	@Value("${export.output.location}")
	protected String exportOutputLocation;
	
	private File exportOutputDir;

	private File exportOutputTempDir;

	private File exportOutputFinalDir;

	@Autowired
	AccountExportDao accountExportDao;

	AccountExportValueObject accountExport;

	@Value("#{jobParameters[execution_id]}")
	private Integer exportId;

	@Value("#{jobParameters[use_java_tar_gz]}")
	private Boolean useJavaTarGz;

	@Value("${noOfConcurrentRecordsProcessors}")
	protected int noOfConcurrentRecordsProcessors;

	@Value("${noOfConcurrentSearchers}")
	protected int noOfConcurrentSearchers;

	@Override
	public RepeatStatus execute(StepContribution contribution,
			ChunkContext chunkContext) throws Exception {
		return RepeatStatus.FINISHED;
	}

	public Integer getExportId() {
		return exportId;
	}

	public boolean getUseJavaTarGz() {
		if (useJavaTarGz == null || !useJavaTarGz.booleanValue()) {
			return false;
		}
		return true;
	}

	protected void initializeLargeObjectsMap() {
		Map<String, Object> largeObjectsMap = new ConcurrentHashMap<String, Object>();
		EXEUTION_CONTENT_LARGE_OBJECTS.put(getExportId(), largeObjectsMap);
		putLargeObjectInExecutionContext(
				PARTIAL_FILES_COMPLETED_QUEUE_OBJECT_NAME,
				new ConcurrentSkipListMap<Integer, Map<Integer, String>>());
		putLargeObjectInExecutionContext(
				SEARCH_RESULTS_WORK_QUEUE_OBJECT_NAME,
				new ConcurrentSkipListMap<Integer, Map<Integer, ConcurrentLinkedQueue<SolrDocumentUnitOfWork>>>());
		initFinalTarGzFile();
	}

	private void initFinalTarGzFile() {
		if (!getUseJavaTarGz()) {
			return;
		}
		EXEUTION_CONTENT_LARGE_OBJECTS.get(getExportId()).put(
				FILES_TO_APPEND_TO_FINAL_TAR_GZ_QUEUE,
				new ConcurrentLinkedQueue<File>());
		TarArchiveOutputStream finalFile;
		try {
			File tarFile = getExportOutputFinalArchiveFile();
			if (tarFile.exists()) {
				tarFile.delete();
			}
			tarFile.getParentFile().mkdirs();
			tarFile.createNewFile();
			FileOutputStream gzOs = new FileOutputStream(tarFile);
			EXEUTION_CONTENT_LARGE_OBJECTS.get(getExportId()).put(FINAL_GZ_OS,
					gzOs);
			OutputStream bufferedGzOs = new BufferedOutputStream(gzOs);
			EXEUTION_CONTENT_LARGE_OBJECTS.get(getExportId()).put(
					FINAL_GZ_BUFFERED_OS, bufferedGzOs);
			GZIPOutputStream gZipOs = new GZIPOutputStream(bufferedGzOs);
			EXEUTION_CONTENT_LARGE_OBJECTS.get(getExportId()).put(
					FINAL_GZ_FILE, gZipOs);
			finalFile = new TarArchiveOutputStream(gZipOs);
		} catch (IOException e) {
			throw new OpaRuntimeException(e);
		}
		EXEUTION_CONTENT_LARGE_OBJECTS.get(getExportId()).put(
				FINAL_TAR_GZ_FILE, finalFile);
	}

	protected void closeFinalTarGzFile() throws IOException {
		((OutputStream) getLargeObjectFromExecutionContext(FINAL_TAR_GZ_FILE))
				.close();
		((OutputStream) getLargeObjectFromExecutionContext(FINAL_GZ_FILE))
				.close();
		((OutputStream) getLargeObjectFromExecutionContext(FINAL_GZ_OS))
				.close();
		((OutputStream) getLargeObjectFromExecutionContext(FINAL_GZ_BUFFERED_OS))
				.close();
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		jobExecutionContext = stepExecution.getJobExecution()
				.getExecutionContext();
		ALL_JOB_EXECUTION_CONTEXTS.put(getExportId(), jobExecutionContext);
		setExportOutputDir(new File(exportOutputLocation + "/" + getExportId()));
		setExportOutputTempDir(new File(exportOutputLocation + "/"
				+ getExportId() + "/temp"));
		setExportOutputFinalDir(new File(exportOutputLocation + "/"
				+ getExportId() + "/final"));
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return stepExecution.getExitStatus();
	}

	public ExecutionContext getJobExecutionContext() {
		if (jobExecutionContext == null) {
			jobExecutionContext = ALL_JOB_EXECUTION_CONTEXTS.get(getExportId());
		}
		return jobExecutionContext;
	}

	protected synchronized AccountExportValueObject getAccountExport() {
		if (accountExport != null) {
			return accountExport;
		}

		Object accountExportObject = getLargeObjectFromExecutionContext(ACCOUNT_EXPORT_OBJECT_NAME);

		accountExport = (AccountExportValueObject) accountExportObject;
		return accountExport;
	}

	public File getExportOutputDir() {
		return exportOutputDir;
	}

	public void setExportOutputDir(File exportOutputDir) {
		this.exportOutputDir = exportOutputDir;
	}

	public File getExportOutputTempDir() {
		return exportOutputTempDir;
	}

	public File getExportOutputFinalDir() {
		return exportOutputFinalDir;
	}

	public File getExportOutputFinalMetadataFile() {
		String fileName = String.format("nara-export-%1$d.%2$s", getExportId(),
				getExportFormat());
		return new File(getExportOutputFinalDir() + "/" + fileName);
	}

	public File getExportOutputFinalArchiveFile() {
		String fileName = String.format("nara-bulk-export-%1$d.tar.gz",
				getExportId());
		return new File(getExportOutputDir() + "/" + fileName);
	}

	public void setExportOutputFinalDir(File exportOutputFinalDir) {
		this.exportOutputFinalDir = exportOutputFinalDir;
	}

	public File getExportOutputTempSearcherDir(int searcherId) {
		return new File(exportOutputTempDir.getAbsolutePath() + "/"
				+ searcherId + "-searcher");
	}

	public File getExportOutputTempSearcherFile(int searcherId) {
		return new File(exportOutputTempDir.getAbsolutePath() + "/"
				+ searcherId + "-tmpfile-searcher." + getExportFormat());
	}

	public void setExportOutputTempDir(File exportOutputTempDir) {
		this.exportOutputTempDir = exportOutputTempDir;
	}

	protected String getExportFormat() {
		return getAccountExport().getExportFormat();
	}

	@SuppressWarnings("unchecked")
	protected Map<Integer, Map<Integer, String>> getPartialFilesCompleteMap() {
		Object mapObject = getLargeObjectFromExecutionContext(PARTIAL_FILES_COMPLETED_QUEUE_OBJECT_NAME);

		// give a chance to the SearchQueryExecutors to initialize the work
		// queues;
		if (mapObject == null) {
			int waitFor = 3000;
			long startTime = new Date().getTime();
			long currentTime = startTime;
			while (currentTime - startTime < waitFor) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					throw new OpaRuntimeException(e);
				}
				mapObject = getLargeObjectFromExecutionContext(PARTIAL_FILES_COMPLETED_QUEUE_OBJECT_NAME);
				if (mapObject != null) {
					return (Map<Integer, Map<Integer, String>>) mapObject;
				}
				currentTime = new Date().getTime();
			}
			throw new OpaRuntimeException(
					"Could not get an instance of the workQueueMap or its size was not equal to noOfConcurrentRecordsProcessors");
		}

		return (Map<Integer, Map<Integer, String>>) mapObject;
	}

	@SuppressWarnings("unchecked")
	protected Map<Integer, Map<Integer, ConcurrentLinkedQueue<SolrDocumentUnitOfWork>>> getWorkQueuesMap() {
		Object workQueuesMapObject = getLargeObjectFromExecutionContext(SEARCH_RESULTS_WORK_QUEUE_OBJECT_NAME);

		// give a chance to the SearchQueryExecutors to initialize the work
		// queues;
		if (workQueuesMapObject == null
				|| getSizeOfWorkQueueMap((Map<Integer, Map<Integer, ConcurrentLinkedQueue<SolrDocumentUnitOfWork>>>) workQueuesMapObject) != noOfConcurrentRecordsProcessors) {
			int waitFor = 3000;
			long startTime = new Date().getTime();
			long currentTime = startTime;
			while (currentTime - startTime < waitFor) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					throw new OpaRuntimeException(e);
				}
				workQueuesMapObject = getLargeObjectFromExecutionContext(SEARCH_RESULTS_WORK_QUEUE_OBJECT_NAME);
				if (workQueuesMapObject != null
						&& getSizeOfWorkQueueMap(((Map<Integer, Map<Integer, ConcurrentLinkedQueue<SolrDocumentUnitOfWork>>>) workQueuesMapObject)) == noOfConcurrentRecordsProcessors) {
					return (Map<Integer, Map<Integer, ConcurrentLinkedQueue<SolrDocumentUnitOfWork>>>) workQueuesMapObject;
				}
				currentTime = new Date().getTime();
			}
			throw new OpaRuntimeException(
					"Could not get an instance of the workQueueMap or its size was not equal to noOfConcurrentRecordsProcessors");
		}

		return (Map<Integer, Map<Integer, ConcurrentLinkedQueue<SolrDocumentUnitOfWork>>>) workQueuesMapObject;
	}

	private int getSizeOfWorkQueueMap(
			Map<Integer, Map<Integer, ConcurrentLinkedQueue<SolrDocumentUnitOfWork>>> workQueueMap) {
		int size = 0;
		for (Integer searcherId : workQueueMap.keySet()) {
			size = size + workQueueMap.get(searcherId).size();
		}
		return size;
	}

	protected static TreeSet<Integer> getOderedRecordProcessorIds(
			Set<Integer> recordProcessorIds) {
		TreeSet<Integer> orderedRecordProcessorIds = new TreeSet<Integer>();
		orderedRecordProcessorIds.addAll(recordProcessorIds);
		return orderedRecordProcessorIds;
	}

	protected static void appendFile(FileOutputStream outFile,
			FileChannel outChannel, String inPath, boolean includeLineSeparator)
			throws IOException {
		RandomAccessFile inFile = new RandomAccessFile(new File(inPath), "r");
		FileChannel inChannel = inFile.getChannel();

		long outFileSize = outChannel.size();
		long inFileSize = inChannel.size();
		outChannel.position(outFileSize);
		long currentPosition = 0;

		while (currentPosition < inFileSize) {
			currentPosition += inChannel.transferTo(currentPosition,
					(1024 * 1024 * 10), outChannel);
			outFile.flush();
		}
		if (includeLineSeparator) {
			outFile.write(System.lineSeparator().getBytes());
			outFile.flush();
		}

		inChannel.close();
		inFile.close();
	}

	protected String getFinalFileUrl() {
		File finalFile;
		if (getAccountExport().getBulkExport().booleanValue()) {
			finalFile = getExportOutputFinalArchiveFile();
		} else {
			finalFile = getExportOutputFinalMetadataFile();
		}
		return finalFile.getAbsolutePath().substring(
				exportOutputLocation.length());
	}

	protected void putLargeObjectInExecutionContext(String key, Object value) {
		EXEUTION_CONTENT_LARGE_OBJECTS.get(getExportId()).put(key, value);
	}

	protected Object getLargeObjectFromExecutionContext(String key) {
		return EXEUTION_CONTENT_LARGE_OBJECTS.get(getExportId()).get(key);
	}
}
