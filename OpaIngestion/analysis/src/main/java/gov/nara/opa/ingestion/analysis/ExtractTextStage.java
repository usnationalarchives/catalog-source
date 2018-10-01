/**
 * Copyright Search Technologies 2014
 * for NARA OPA
 */
package gov.nara.opa.ingestion.analysis;

import gov.nara.opa.ingestion.Components;
import gov.nara.opa.ingestion.JobInfo;
import gov.nara.opa.ingestion.Jobs;
import gov.nara.opa.ingestion.OpaStorage;

import java.io.*;
import java.nio.charset.StandardCharsets;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.w3c.dom.Element;

import com.searchtechnologies.aspire.framework.StageImpl;
import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.Job;

/**
 * Stage to extract text from objects.
 * 
 * @author OPA Ingestion Team
 */
public class ExtractTextStage extends StageImpl {

	/**
	 * Extract text from objects.
	 * 
	 * @param job
	 *            The job to process.
	 * @throws com.searchtechnologies.aspire.services.AspireException
	 */
	@Override
	public void process(Job job) throws AspireException {
		debug("entering Extract Text Stage...");
		if (Jobs.getJobInfo(job).isDoTMDRegeneration() || Jobs.getJobInfo(job).isDoExtractText()) {
			try {
				extractMetadata(job);
			} catch (Throwable ex) {
				logStageFailedError(job, ex);
			}
		}
	}

	private void logStageFailedError(Job job, Throwable cause) {
		JobInfo jobInfo = Jobs.getJobInfo(job);
		if (jobInfo != null) {
			jobInfo.getParent().getSubJobFailedCount().incrementAndGet();
			String recordDescription = jobInfo.getParent() != null ? jobInfo
					.getParent().getDescription() : "";
			error(cause, "Text extracted failed: %s, object %s",
					recordDescription, jobInfo.getObjectId());
		}
	}

	/**
	 * This function read each digital object, create their zoom images and
	 * update the objects.xml file.
	 * 
	 * @param job
	 * @throws AspireException
	 */
	public void extractMetadata(Job job) throws AspireException, IOException {
		JobInfo jobInfo = Jobs.getJobInfo(job);
		if (!jobInfo.isPrimary()) {
			return;
		}
		if(jobInfo.isLegacyObject()) {
			if (jobInfo.isDoTMDRegeneration() && jobInfo.getTMDContentFile() != null) {
				LegacyTechnicalMetadataUpdater extractor = new LegacyTechnicalMetadataUpdater(this, job);
				extractor.setFile(jobInfo.getTMDContentFile());
				extractor.updateMetadata();
			}
			if (jobInfo.isDoExtractText() && jobInfo.getTMDContentFile() != null) {
				extractText(jobInfo.getTMDContentFile(), jobInfo);
			}
		} else if (jobInfo.isDoTMDRegeneration()){
			TechnicalMetadataUpdater extractor = new TechnicalMetadataUpdater(this, job);
			extractor.updateMetadata();
		}
	}

	private void extractPaginatedText(File source, JobInfo jobInfo) throws AspireException{
		PDDocument document;
		PDFTextStripper stripper;
		try {
			document = PDDocument.load(new FileInputStream(source));
			stripper = new PDFTextStripper(StandardCharsets.UTF_8.toString());
		} catch (IOException e) {
			throw new AspireException("Loading document", e);
		}

		stripper.setStartPage(1);
		stripper.setEndPage(document.getNumberOfPages());
		stripper.setPageStart("--------------------------------Page Start--------------------------------\n");
		stripper.setPageEnd("\n--------------------------------Page End----------------------------------\n");

		File paginatedText;
		try {
			paginatedText = File.createTempFile("paginated-text", ".txt");
			Writer output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(paginatedText), StandardCharsets.UTF_8));
			stripper.writeText(document, output);
			output.close();
			document.close();
		} catch (IOException e) {
			throw new AspireException("Writing text", e);
		}

		if (paginatedText.length() > 0) {
			String pathToPaginatedText = jobInfo.isLegacyObject() ? jobInfo.getLegacyPathToPaginatedText() : jobInfo.getPathToPaginatedText();
			jobInfo.getOpaStorage().saveFile(paginatedText, pathToPaginatedText);
			info("Saved paginated text to %s", pathToPaginatedText);
		}

		jobInfo.setExtractedPaginatedTextFile(paginatedText);
	}

	private void extractText(File source, JobInfo jobInfo) throws AspireException {
		File extractedText;
		String pathToExtractedText = jobInfo.isLegacyObject() ? jobInfo.getLegacyPathToExtractedText() : jobInfo.getPathToExtractedText();
		debug("path to extracted text: "+pathToExtractedText);
		try {
			extractedText = File.createTempFile("text", ".txt");
			debug("temporary text file:" +extractedText.getCanonicalPath());

				Writer writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(extractedText),
							StandardCharsets.UTF_8));

				Tika.extractText(source, writer);

				debug("Extracted text after TIKA extract: %s", extractedText);

			if (jobInfo.isPDF()) {
				extractPaginatedText(source, jobInfo);
			}


			if (extractedText.length() > 0) {
				jobInfo.getOpaStorage().saveFile(extractedText, pathToExtractedText);
				info("Extracted text to %s", pathToExtractedText);
				debug("Extracted text after save: %s", extractedText);
			}

			jobInfo.setExtractedTextFile(extractedText);
		} catch (IOException e) {
			throw new AspireException("Writing text for "+jobInfo.getNAID()+":"+jobInfo.getObjectId(),e);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void initialize(Element elmnt) throws AspireException {
	}

	@Override
	public void close() {
	}
}
