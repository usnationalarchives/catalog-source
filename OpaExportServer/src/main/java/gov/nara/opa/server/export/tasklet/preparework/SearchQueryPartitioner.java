package gov.nara.opa.server.export.tasklet.preparework;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.server.export.tasklet.AbstractAccountExportTasklet;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("searchQueryPartitioner")
@Scope("step")
public class SearchQueryPartitioner extends AbstractAccountExportTasklet
		implements Partitioner {

	@Value("${searchers.noofpartitions}")
	private String noOfPartiationsConfig;

	@Value("${maxRowsPerSearch}")
	private int maxRowsPerSearch;

	@Value("${maxOffset}")
	private int maxOffset;

	@Override
	public Map<String, ExecutionContext> partition(int springGridSize) {
		Map<String, ExecutionContext> result = new ConcurrentHashMap<String, ExecutionContext>();
		int totalRecords = getAccountExport().getTotalRecordsToBeProcessed();
		int gridSize = getAccountExport().getListName() != null ? 1 : ((Integer) getNoOfPartitions(totalRecords,
				springGridSize).get("noOfPartitions")).intValue();
		boolean lastPartition = ((Boolean) getNoOfPartitions(totalRecords,
				springGridSize).get("lastPartition")).booleanValue();
		Integer clientOffset = getAccountExport().getOffset();
		int clientOffsetInt = 0;
		if (clientOffset != null) {
			clientOffsetInt = clientOffset.intValue();
		}
		int amountOfRecordsPerPartition = totalRecords > gridSize ? totalRecords
				/ gridSize
				: 1;
		int offset = 0;
		for (int i = 0; i < springGridSize; i++) {
			ExecutionContext executionContext = new ExecutionContext();
			int rows = 0;
			if (i < gridSize) {
				rows = i + 1 == gridSize ? totalRecords - offset
						: amountOfRecordsPerPartition;
				if (offset >= totalRecords) {
					rows = 0;
				}
			}
			executionContext.putInt("totalRecords", totalRecords);
			executionContext.putInt("rows", rows);
			if (rows > 0 && offset + clientOffsetInt > maxOffset) {
				int totalOffset = offset + clientOffsetInt;
				throw new OpaRuntimeException(
						"The offset of "
								+ totalOffset
								+ " set for this query is larger then the allowed maximum offset of "
								+ maxOffset);
			}
			executionContext.putInt("offset", offset + clientOffsetInt);
			executionContext.putInt("documentIndexStart", offset);
			int searcherId = i + 1;
			executionContext.putInt("searcherId", searcherId);
			executionContext.putString("lastPartition", new Boolean(
					lastPartition).toString());
			result.put("searcher" + searcherId, executionContext);
			offset = offset + amountOfRecordsPerPartition;
		}
		return result;
	}

	private Map<String, Object> getNoOfPartitions(int totalRecords,
			int springGridSize) {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			String[] partitions = noOfPartiationsConfig.split("\\|");
			for (int i = 0; i < partitions.length; i++) {
				String[] partitionTokens = partitions[i].split(";");
				String[] ranges = partitionTokens[0].split("-");
				if (totalRecords >= Integer.valueOf(ranges[0])
						&& totalRecords <= Integer.valueOf(ranges[1])) {
					int gridSize = Integer.valueOf(partitionTokens[1]);
					if (gridSize > springGridSize) {
						throw new OpaRuntimeException(
								"The number of partitions ("
										+ gridSize
										+ ") configured in "
										+ "the searchers.noofpartitions for this total no of records to be retrieved by the search - "
										+ totalRecords
										+ " - can not be higher then the noOfConcurrentSearchers ("
										+ springGridSize
										+ "). Check the values in application.properties");
					}
					boolean lastPartition = new Boolean(
							i == partitions.length - 1).booleanValue();
					if (!lastPartition
							&& totalRecords / gridSize > maxRowsPerSearch) {
						throw new OpaRuntimeException(
								"There is a total no of records of "
										+ totalRecords
										+ " to be extracted across "
										+ gridSize
										+ " partitions. The number of records "
										+ " per partition is larger then the max rows per search of: "
										+ maxRowsPerSearch
										+ ". Please adjust the searchers.noofpartitions setting.");
					}
					if (lastPartition && gridSize != 1) {
						throw new OpaRuntimeException(
								"The last partition for the searchers.noofpartitions needs to have a value of 1.");
					}
					
					result.put("noOfPartitions", new Integer(gridSize));
					result.put("lastPartition", lastPartition);
					return result;
				}
			}
			throw new OpaRuntimeException(
					"The total records to be processed ("
							+ totalRecords
							+ ") does not fall into any of the partitions "
							+ " configured in the searchers.noofpartitions. Check the application.properties");
		} catch (Exception ex) {
			if (!(ex instanceof OpaRuntimeException)) {
				throw new OpaRuntimeException(
						"Encountered error while trying to setup up searcher partitions. "
								+ "E.g. of a partition setup: searchers.noofpartitions=0-100;1|101-400;2|401-1000;3|1001-9999999999999;4",
						ex);
			} else {
				throw ex;
			}

		}

	}
}
