package gov.nara.opa.jp2conversion;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component("summarizer")
public class Jp2Summarizer implements Tasklet {

	private static final Log log = LogFactory.getLog(Jp2ItemReader.class);
	
	@Override
	public RepeatStatus execute(StepContribution contribution,
			ChunkContext chunkContext) throws Exception {
		// TODO Auto-generated method stub
		log.info("No of files read: " + Jp2TempWorkDataHolders.NO_OF_FILES_READ);
		log.info("No of files converted: " + Jp2TempWorkDataHolders.NO_OF_FILES_CONVERTED);
		log.info("No of files processed: " + Jp2TempWorkDataHolders.NO_OF_FILES_PROCESSED); 
		long executionTime = (new Date()).getTime() - Jp2TempWorkDataHolders.START_TIME;
		log.info("Total processing time (running " + Jp2TempWorkDataHolders.NO_OF_THREADS + " ways): " + executionTime/(double)(1000 * 60) + " minutes.");
		log.info("Converted " + Jp2TempWorkDataHolders.NO_OF_FILES_CONVERTED.longValue()/(double)(executionTime/1000) + " files/sec.");
		return null;
	}

}
