package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;
import com.searchtechnologies.aspire.services.Job;
import org.apache.http.client.utils.URLEncodedUtils;
import org.w3c.dom.Element;

import java.net.URL;
import java.net.URLEncoder;

public class DownloadDigitalObjectsStage extends IngestionStage {

  @Override
  public void process(Job job) throws AspireException {
      JobInfo jobInfo = Jobs.getJobInfo(job);
      debug("In Download digital objects stage for %s.", job.getJobId());

      try {

          DigitalObjectDownloader downloader = new DigitalObjectDownloader(this, job);

          jobInfo.setObjectSourceURL(new URL(job.get().getText("accessFilename")));
          String bucketName = downloader.getS3BucketName();
          String key = jobInfo.getObjectSourceURL().getPath().substring(1);

          if (key.startsWith(bucketName)) {
              key = key.replace(bucketName + "/", "");
          }
          if (key.startsWith("lz")) {
              jobInfo.setLegacyObject(false);
          } else {
              jobInfo.setLegacyObject(true);
          }

          // check for technical metadata element in object XML
          AspireObject object = jobInfo.getDigitalObject();
          AspireObject tmd = object.get("technicalMetadata");

          if (tmd != null && tmd.getText("size") != null) {
              debug("technicalMetadata in previous objects.xml...");
              jobInfo.setDoTMDRegeneration(false);
          } else {
              jobInfo.setDoTMDRegeneration(true);
          }

          if (!jobInfo.isLegacyObject()) {
              if (jobInfo.getOpaStorage().isFileNewer(key, jobInfo.getPathToObjectsXml())) {
                  info("LZ file is newer than previous Objects. Performing TMD (re)processing.");
                  jobInfo.setDoTMDRegeneration(true);
              }
          }

          if (jobInfo.isLegacyObject()) {
            if(jobInfo.isDoTMDRegeneration()) {
                debug("attempting to download original file for TMD (re)processing...");
                downloader.downloadOriginalContentForTMDprocessing();
            }
          } else {
              debug("attempting to download post-TO4 extracted text elements.");
              downloader.downloadExtractedText();
          }

          if (jobInfo.isLegacyObject()) {
              String extractedTextKey = jobInfo.getLegacyPathToExtractedText();
              debug("extracted text S3 Key: "+extractedTextKey);
              if (jobInfo.getOpaStorage().exists(extractedTextKey)) {
                  debug("attempting to download legacy extracted text elements.");
                  jobInfo.setDoExtractText(false);
                  downloader.downloadLegacyExtractedText();
              } else {
                  if (isTextualDocument(jobInfo)) {
                      jobInfo.setDoExtractText(true);
                      debug("will perform text extraction for textual document...");
                      if (jobInfo.getTMDContentFile() == null) {
                          debug("attempting to download original file for text extraction...");
                          downloader.downloadOriginalContentForTMDprocessing();
                      }
                  }
              }
          }


      } catch (Throwable e) {
          jobInfo.getParent().getSubJobFailedCount().incrementAndGet();
          error("Download failed for %s - %s: %s", job.get().getText("accessFilename"), jobInfo.getDescription(), e.getMessage());
          e.printStackTrace();
      }

      return;
  }

  private boolean isTextualDocument(JobInfo jobInfo) {
      switch(jobInfo.getMimeType()) {
          case MimeTypes.PDF:
          case MimeTypes.PLAIN_TEXT:
          case MimeTypes.HTML:
          case MimeTypes.WORD:
          case MimeTypes.EXCEL:
          case MimeTypes.POWERPOINT:
              return true;
          default:
              return false;
      }
  }
}
