package gov.nara.opa.server.export.tasklet.preparework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Joiner;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.common.services.solr.SolrGateway;
import gov.nara.opa.common.validation.export.CreateAccountExportRequestParameters;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.common.valueobject.export.AccountExportValueObjectHelper;
import gov.nara.opa.server.export.tasklet.AbstractAccountExportTasklet;
import gov.nara.opa.server.export.valueobject.SolrDocumentUnitOfWork;

@Component("searchQueryExecutor")
@Scope("step")
public class SearchQueryExecutor extends AbstractAccountExportTasklet {
	
	private static long LONG_QUERY_TIME_MILLIS = 10000;
	

	@Value("#{stepExecutionContext[rows]}")
	private int rows;

	@Value("#{stepExecutionContext[offset]}")
	private int offset;

	@Value("#{stepExecutionContext[totalRecords]}")
	private int totalRecords;

	@Value("#{stepExecutionContext[searcherId]}")
	private int searcherId;

	@Value("${recordsProcessorBatchSize}")
	private int recordsProcessorBatchSize;

	@Value("#{stepExecutionContext[documentIndexStart]}")
	private int documentIndexStart;

	@Value("#{stepExecutionContext[lastPartition]}")
	private boolean lastPartition;

	@Value("${maxRowsPerSearch}")
	private int maxRowsPerSearch;
	
	@Autowired
	SolrGateway solrGateway;

	OpaLogger logger = OpaLogger.getLogger(SearchQueryExecutor.class);

	@Override
	public RepeatStatus execute(StepContribution contribution,
			ChunkContext chunkContext) throws Exception {
		
		logger.trace("Starting execution");

		
		Collection<ConcurrentLinkedQueue<SolrDocumentUnitOfWork>> workQueues = initializeWorkQueueMap()
				.get(searcherId).values();
		@SuppressWarnings("unchecked")
		Map<Integer, Map<Integer, String>> partialFilesCompletedQueue = (Map<Integer, Map<Integer, String>>) getLargeObjectFromExecutionContext(PARTIAL_FILES_COMPLETED_QUEUE_OBJECT_NAME);
		partialFilesCompletedQueue.put(searcherId,
				new ConcurrentSkipListMap<Integer, String>());
		AccountExportValueObject accountExport = getAccountExport();
		if (getRows() > 0) {
			if (!lastPartition) {
				Map<String, String[]> queryParameters = accountExport
						.getQueryParameters();
				
				queryParameters = prepareQuery(queryParameters, accountExport,
						false, null, 0);
				
				logger.debug("Result field query parameter: " + queryParameters.get("resultFields"));

				if (accountExport.getListName() != null
						&& queryParameters.containsKey("opaIds")
						&& queryParameters.get("opaIds").length > 0) {
					int maxSolrOpaIds = 100;
					String[] tempOpaIds = queryParameters.get("opaIds").clone();
					String[] offSetArray = { "0" };
					String[] sortArray = {"title ASC"};
					queryParameters.put("offset", offSetArray);
					queryParameters.put("sort", sortArray);
					List<String> items = Arrays.asList(tempOpaIds[0]
							.split("[\\s,]+"));
					int times = items.size() / maxSolrOpaIds;
					if (times == 0) {
						times = 1;
					}

					int initValue = 0;
					for (int index = 0; index < times; ++index) {
						List<String> opaIds = new ArrayList<String>();
						String[] emptyArray = {};
						queryParameters.put("opaIds", emptyArray);
						initValue = index * maxSolrOpaIds;
						int endValue = initValue + maxSolrOpaIds;
						for (int i = initValue; i < items.size()
								&& i < endValue; i++) {
							opaIds.add(items.get(i));
						}

						String ids = Joiner.on(",").join(opaIds);
						String[] array = { ids };
						queryParameters.put("opaIds", array);
						
						long startTime = new Date().getTime();
						QueryResponse response = solrGateway
								.solrQuery(queryParameters);
						long endTime = new Date().getTime();
						if(endTime - startTime > LONG_QUERY_TIME_MILLIS) {
							logger.info(String.format("ExportId:[%1$d] Query took more than [%2$d] secs [%3$d] for query: [%4$s]", accountExport.getExportId(), LONG_QUERY_TIME_MILLIS, endTime - startTime, printQueryParams(queryParameters)));
						}
						processResults(response.getResults(), workQueues,
								index, false, initValue);
					}

					processResults(null, workQueues, times, true, initValue);

					queryParameters.put("opaIds", tempOpaIds);

					return RepeatStatus.FINISHED;

				} else {
					String query = queryParameters.toString();
					try {
						long startTime = new Date().getTime();
						QueryResponse response = solrGateway
								.solrQuery(queryParameters);
						long endTime = new Date().getTime();
						if(endTime - startTime > LONG_QUERY_TIME_MILLIS) {
							logger.info(String.format("ExportId:[%1$d] Query took more than [%2$d] secs [%3$d] for query: [%4$s]", accountExport.getExportId(), LONG_QUERY_TIME_MILLIS, endTime - startTime, printQueryParams(queryParameters)));
						}

						processResults(response.getResults(), workQueues, 1, true,
								0);
					
					} catch(Exception ex) {
						logger.error(String.format("Failed query with the following parameters: %1$s", query), ex);
						throw ex;
					}
				}
			} else {
				int recordsProcessed = 0;
				String cursorMark = null;
				boolean continueLoop = true;
				int batchIndex = 1;
				while (recordsProcessed < getRows() && continueLoop) {
					Map<String, String[]> queryParameters = accountExport
							.getQueryParameters();
					int cursorMarkRows = getRows() - recordsProcessed > maxRowsPerSearch ? maxRowsPerSearch
							: getRows() - recordsProcessed;
					if (recordsProcessed == 0) {
						queryParameters = prepareQuery(queryParameters,
								accountExport, true, null, cursorMarkRows);
					} else {
						queryParameters = prepareQuery(queryParameters,
								accountExport, true, cursorMark, cursorMarkRows);
					}

					long startTime = new Date().getTime();
					QueryResponse response = solrGateway
							.solrQuery(queryParameters);
					long endTime = new Date().getTime();
					if(endTime - startTime > LONG_QUERY_TIME_MILLIS) {
						logger.info(String.format("ExportId:[%1$d] Query took more than [%2$d] secs [%3$d] for query: [%4$s]", accountExport.getExportId(), LONG_QUERY_TIME_MILLIS, endTime - startTime, printQueryParams(queryParameters)));
					}
					

					cursorMark = response.getNextCursorMark();
					logger.debug(String.format("ExportId: %1$s - Batch Index: %2$d - Cursor Mark: %3$s", accountExport.getExportId(), batchIndex, cursorMark));
					continueLoop = processResults(response.getResults(),
							workQueues, batchIndex, false, recordsProcessed);
					recordsProcessed = recordsProcessed + maxRowsPerSearch;
					if (recordsProcessed >= getRows() && continueLoop) {
						processResults(null, workQueues, batchIndex, true,
								recordsProcessed);
					}
				}

				batchIndex++;
			}

		} else {
			logger.trace("No results");

			processResults(null, workQueues, 1, true, 0);
		}

		return RepeatStatus.FINISHED;
	}
	
	private String printQueryParams(Map<String, String[]> queryParameters) {
		StringBuilder sb = new StringBuilder();
		boolean start = true;
		for(Entry<String, String[]> entry : queryParameters.entrySet()) {
			if(!start) {
				sb.append(",");
			}
			sb.append(entry.getKey() + "=");
			
			boolean innerStart = true;
			for(String value : entry.getValue()) {
				if(!innerStart) {
					sb.append(",");
				}
				sb.append(value);
				innerStart = false;
			}
			start = false;
		}
		
		return sb.toString();
	}

	private boolean processResults(
			SolrDocumentList results,
			Collection<ConcurrentLinkedQueue<SolrDocumentUnitOfWork>> workQueues,
			int searchBatchIndex, boolean lastBatch, int batchStartDocIndex) {
		
		if(results != null) {
			logger.trace(String.format("Processing results: %1$d", results.size()));
		} else {
			logger.trace("Empty results");			
		}
		
		if (results == null || results.size() == 0) {
			
			logger.trace("Finishing queue");
			loadEndOfDataMesssagesToQueues(workQueues);
			return false;
		}
		

		loadWorkToQueues(results, workQueues, searchBatchIndex,
				batchStartDocIndex);
		if (lastBatch) {
			
			logger.trace("Processing last batch");

			loadEndOfDataMesssagesToQueues(workQueues);
			return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private Map<Integer, Map<Integer, ConcurrentLinkedQueue<SolrDocumentUnitOfWork>>> initializeWorkQueueMap() {
		Map<Integer, Map<Integer, ConcurrentLinkedQueue<SolrDocumentUnitOfWork>>> searchResultsMap = (ConcurrentSkipListMap<Integer, Map<Integer, ConcurrentLinkedQueue<SolrDocumentUnitOfWork>>>) getLargeObjectFromExecutionContext(SEARCH_RESULTS_WORK_QUEUE_OBJECT_NAME);

		int totalQueues = getNoOfWorkQueues();
		Map<Integer, ConcurrentLinkedQueue<SolrDocumentUnitOfWork>> workQueuesMap = new ConcurrentSkipListMap<Integer, ConcurrentLinkedQueue<SolrDocumentUnitOfWork>>();
		for (int i = 1; i <= totalQueues; i++) {
			workQueuesMap.put(searcherId * SEARCHER_ID_MULTIPLIER + i,
					new ConcurrentLinkedQueue<SolrDocumentUnitOfWork>());
		}
		searchResultsMap.put(searcherId, workQueuesMap);
		return searchResultsMap;
	}

	private int getNoOfWorkQueues() {
		if ((noOfConcurrentRecordsProcessors % noOfConcurrentSearchers) != 0) {
			throw new OpaRuntimeException(
					"The noOfConcurrentRecordsProcessors needs to be a multiplier of noOfConcurrentSearchers. "
							+ "Please verify the configuration file - application.properties");
		}
		return noOfConcurrentRecordsProcessors / noOfConcurrentSearchers;
	}

	private Map<String, String[]> prepareQuery(
			Map<String, String[]> queryParameters,
			AccountExportValueObject accountExport, boolean useCursorMark,
			String cursorMark, int cursorMarkRows) {
		try {
			
			Map<String, String[]> returnValue = new ConcurrentHashMap<String, String[]>();
			
			for (String key : queryParameters.keySet()) {
				if (key.equals(CreateAccountExportRequestParameters.RESULT_FIELDS_HTTP_PARAM_NAME)) {
					if (accountExport.extractValuesNeeded()) {
						returnValue
								.put(key,
										new String[] { CreateAccountExportRequestParameters.ALL_RESULT_FIELDS });
						accountExport.setDefaultResultFieldsSet(true);
					} else {
						// type and level are needed all the time as they drive how
						// records
						// are processed
						String[] solrResultFields = new String[1];
						String[] resultFields = queryParameters.get(key);
						solrResultFields[0] = resultFields[0] == null
								|| resultFields[0].trim().equals("") ? "type,level"
								: resultFields[0] + ",type,level";
						returnValue.put(key, solrResultFields);
					}
				} else {
					returnValue.put(key, queryParameters.get(key));
				}
			}
			
			if (returnValue
					.get(CreateAccountExportRequestParameters.RESULT_FIELDS_HTTP_PARAM_NAME) == null) {
				String[] allResultFields = new String[] { CreateAccountExportRequestParameters.ALL_RESULT_FIELDS };
				String resultFields = null;
				if (!accountExport.getIncludeMetadata()) {
					resultFields = AccountExportValueObjectHelper
							.getResultFieldsForNoMetadata(accountExport);
					allResultFields = new String[] { resultFields };
					if (!StringUtils.isNullOrEmtpy(resultFields)) {
						accountExport.getQueryParameters().put("resultFields",
							allResultFields);
					}
					accountExport.setDefaultResultFieldsSet(false);
				} else {
					accountExport.setDefaultResultFieldsSet(true);
				}
				if (!StringUtils.isNullOrEmtpy(resultFields)) { 
					returnValue
						.put(CreateAccountExportRequestParameters.RESULT_FIELDS_HTTP_PARAM_NAME,
								allResultFields);
				}
	
			}
			
			//Cursor usage logic
			String rowsString = useCursorMark ? new Integer(cursorMarkRows)
					.toString() : new Integer(getRows()).toString();
			returnValue.put("rows", new String[] { rowsString });
			if (!useCursorMark) {
				returnValue.put("offset",
						new String[] { new Integer(getOffset()).toString() });
			} else {
				if (cursorMark == null) {
					cursorMark = "*";
					returnValue.put("start", new String[] { "0" });
				}
				returnValue.put("cursorMark", new String[] { cursorMark });
				
				//Disable query elevation when cursor is in use
				returnValue.put("enableElevation", new String[] { "false" });
			}
	
			String sort = accountExport.getSort();
			if (sort != null) {
				returnValue.put("sort", new String[] { sort });
			} else {
				if (useCursorMark) {
					returnValue
							.put("sort", new String[] { "score desc,opaId asc" });
				}
			}
			return returnValue;
			
		} catch(Exception ex) {
			logger.error("SEARCH QUERY EXECUTOR EXCEPTION", ex);
			
			throw ex;
		}
	}

	private void loadWorkToQueues(
			SolrDocumentList results,
			Collection<ConcurrentLinkedQueue<SolrDocumentUnitOfWork>> workQueues,
			int searchBatchIndex, int batchStartDocIndex) {

		Iterator<SolrDocument> iteratorDocuments = results.iterator();
		Iterator<ConcurrentLinkedQueue<SolrDocumentUnitOfWork>> iteratorQueues = workQueues
				.iterator();

		int documentIndex = 1;
		ConcurrentLinkedQueue<SolrDocumentUnitOfWork> currentQueue = iteratorQueues
				.next();
		int currentBatchIndex = 1;
		while (iteratorDocuments.hasNext()) {
			SolrDocument document = iteratorDocuments.next();
			if (documentIndex > recordsProcessorBatchSize) {
				if (!iteratorQueues.hasNext()) {
					iteratorQueues = workQueues.iterator();
				}
				currentQueue = iteratorQueues.next();
				currentBatchIndex++;
			}
			currentQueue.add(new SolrDocumentUnitOfWork(document,
					getSearcherId(), documentIndexStart + documentIndex
							+ batchStartDocIndex, searchBatchIndex * 1000000
							+ currentBatchIndex, totalRecords));
			logger.trace("Added a document to the working queue. NaId: " + document.get("naId"));
			documentIndex++;
		}
	}

	private void loadEndOfDataMesssagesToQueues(
			Collection<ConcurrentLinkedQueue<SolrDocumentUnitOfWork>> queues) {
		for (ConcurrentLinkedQueue<SolrDocumentUnitOfWork> queue : queues) {
			queue.add(new SolrDocumentUnitOfWork(null, getSearcherId(), -1, -1,
					-1));
		}

	}

	public int getRows() {
		return rows;
	}

	public int getOffset() {
		return offset;
	}

	public int getSearcherId() {
		return searcherId;
	}

	public SolrGateway getSolrGateway() {
		return solrGateway;
	}

}
