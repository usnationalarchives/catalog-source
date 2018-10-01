package gov.nara.opa.jp2conversion;

import java.io.File;
import java.io.IOException;
import java.util.List;

import magick.MagickException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;


/**
 * Dummy {@link ItemWriter} which only logs data it receives.
 */

public abstract class  Jp2ConverterWriterBase implements ItemWriter<String>, StepExecutionListener {

	private static final Log log = LogFactory.getLog(Jp2ConverterWriterBase.class);
	
	@Value("${output.format}")
	protected String outputFormat;
	private int recordsConverted = 0;
	private int recordsProcessed = 0;
	/**
	 * @see ItemWriter#write(java.util.List)
	 */
	public void write(List<? extends String> filePaths) throws Exception {
		for (String filePath : filePaths){
			
			String convertedFilePath = filePath.replace("\\Expanded\\",convertGetConvertedFolderName());
			boolean isJp2File = convertedFilePath.endsWith(".jp2");
			if (isJp2File){
				convertedFilePath = convertedFilePath.replace(".jp2", "." + outputFormat);
			}
				
			File targetFile = new File(convertedFilePath);
			targetFile.getParentFile().mkdirs();
			if (isJp2File) {
				log.debug("Converting file: " + filePath);
				convertFile(filePath, convertedFilePath);
				recordsConverted++;
				if ( recordsConverted % 100 == 0){
					log.info("Current thread converted " + recordsConverted + " files so far.");
				}
			} else {
				log.debug("Copying file: " + filePath);
				FileUtils.copyFile(new File(filePath), targetFile);
			}
			recordsProcessed++;

		}
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		// TODO Auto-generated method stub
		Jp2TempWorkDataHolders.NO_OF_FILES_CONVERTED.addAndGet(recordsConverted);
		Jp2TempWorkDataHolders.NO_OF_FILES_PROCESSED.addAndGet(recordsProcessed);
		Jp2TempWorkDataHolders.NO_OF_THREADS.incrementAndGet();
		return ExitStatus.COMPLETED;
	}
	
	protected abstract void convertFile(String oldFilePath, String newFilePath) throws IOException, MagickException, InterruptedException;
	protected abstract String convertGetConvertedFolderName();

}
