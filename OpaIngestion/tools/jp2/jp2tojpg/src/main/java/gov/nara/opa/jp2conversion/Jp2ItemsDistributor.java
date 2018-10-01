package gov.nara.opa.jp2conversion;

import java.io.File;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("workDistributor")
public class Jp2ItemsDistributor implements Tasklet {

	@Value("${base.jp2.directory}")
	private String baseJp2Directory;
	

	
	@Override
	public RepeatStatus execute(StepContribution contribution,
			ChunkContext chunkContext) throws Exception {
		Collection<File> files = getJp2Files();
		for (File file : files){
			Jp2TempWorkDataHolders.WORK_QUEUE.add(file);
			Jp2TempWorkDataHolders.NO_OF_FILES_READ++;
		}
		return null;
	}
	
	private Collection<File> getJp2Files(){
		return FileUtils.listFiles(
				  new File(baseJp2Directory), 
				  null, 
				  true
				);
	}

}
