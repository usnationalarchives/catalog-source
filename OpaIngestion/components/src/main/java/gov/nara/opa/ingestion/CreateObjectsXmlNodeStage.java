package gov.nara.opa.ingestion;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.searchtechnologies.aspire.framework.utilities.StringUtilities;
import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;
import com.searchtechnologies.aspire.services.Job;

import java.io.File;
import java.io.StringReader;
import java.nio.file.Files;

public class CreateObjectsXmlNodeStage extends IngestionStage {

  @Override
  public void process(Job job) throws AspireException {
    JobInfo jobInfo = Jobs.getJobInfo(job);
    AspireObject objects = createObjectsXml(jobInfo);
    if (objects != null) {
      AspireObject doc = job.get();
      doc.set(objects);
    }
  }

  private AspireObject createObjectsXml(JobInfo jobInfo) throws AspireException {
    AspireObject record = jobInfo.getRecord();
    AspireObject objects = null;
    AspireObject dasDigitalObjectArray = record.get("digitalObjectArray");

    /** if we're reindexing, get the previously processed objects.xml from S3.
     *  and return that instead.  if that doesn't work we'll try Solr.
     */
    if (Components.getSettings(this).isReindex() && dasDigitalObjectArray != null &&
            dasDigitalObjectArray.hasChildren()) {

      objects = jobInfo.getPreviousObjectsXML();
    }

    // Add object elements to objects.xml from DAS XML
    if (objects == null &&
            dasDigitalObjectArray != null &&
            dasDigitalObjectArray.hasChildren()) {

      // Start with objects.xml template (empty)
      objects = ObjectsXml.startNewObjects();

      // get previously generated objects.xml (if it exists)
      AspireObject previousObjects = jobInfo.getPreviousObjectsXML();

      for (AspireObject digitalObject : dasDigitalObjectArray.getChildren()) {
        if ("digitalObject".equals(digitalObject.getName())) {
          try {
            AspireObject object = ObjectsXml.createObjectsXmlObject(this, record, digitalObject, jobInfo.getSortNums(), previousObjects);
            objects.add(object);
          } catch (Throwable e) {
            error("%s failed to create entry for object %s: %s",
                    jobInfo.getDescription(), digitalObject.toXmlString(true),
                    StringUtilities.exceptionTraceToString(e)
            );
          }
        }
      }
    }
    return objects;
  }


}