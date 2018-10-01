package gov.nara.opa.common.services.docstransforms.impl.doccreators;

import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.common.services.docstransforms.Constants;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.common.valueobject.export.ValueHolderValueObject;
import gov.nara.opa.common.valueobject.search.SearchRecordValueObject;
import gov.nara.opa.common.valueobject.search.SolrQueryResponseValueObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;

import org.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class JsonDocumentCreator extends AbstractAspireObjectDocumentCreator implements DocumentCreator, Constants {
	static OpaLogger logger = OpaLogger.getLogger(JsonDocumentCreator.class);

	@Override
	public void createDocument(LinkedList<ValueHolderValueObject> records, OutputStream outputStream, int documentIndex,
			int totalDocuments, boolean flush, String resultType, AccountExportValueObject accountExport, Object writer,
			SearchRecordValueObject document, SolrQueryResponseValueObject queryResponse) throws IOException {

		if (records == null) {
			if (documentIndex == 1) {
				outputStream.write("[\n".getBytes());
			}
			if (documentIndex == totalDocuments) {
				outputStream.write("\n]".getBytes());
				if (flush) {
					outputStream.flush();
				}
			}
			return;
		}

		String jsonRecord = getJsonRecord(records, outputStream, documentIndex, accountExport, document);
		jsonRecord = jsonRecord.replaceAll("\\{.*\"naraitem\"\\:", "");
		// remove the last two curly brackets }}
		jsonRecord = jsonRecord.substring(0, jsonRecord.length() - 2);
		// add the resultType and closing bracket
		jsonRecord = jsonRecord + ",\"resultType\": \"" + document.getResultType() + "\"}";
		String test = null;
		try {
			jsonRecord = removeSpecialCharacters(jsonRecord);
			JSONObject o = new JSONObject(jsonRecord);
			test = o.getString("resultType");
		} catch (Exception e) {

		}
		if (test == null) {
			logger.error("ERROR parsing json: " + jsonRecord);
		}
		//logger.error("jsonRecord=" + jsonRecord.replaceAll("\n", ""));
		if (documentIndex == 1) {
			jsonRecord = "[\n" + jsonRecord;
		} else {
			jsonRecord = ",\n" + jsonRecord;
		}
		if (documentIndex == totalDocuments) {
			jsonRecord = jsonRecord + "\n]";
		}
		accountExport.getRecordsWritten().incrementAndGet();
		outputStream.write(jsonRecord.getBytes());
		if (flush) {
			outputStream.flush();
		}
	}

	public static String removeSpecialCharacters(String in) {
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < in.length(); i++) {
			char c = in.charAt(i);
			if (c > 31 && c < 127) {
				b.append(c);
			}
		}
		return b.toString();
	}

	protected String getJsonRecord(LinkedList<ValueHolderValueObject> records, OutputStream outputStream,
			int documentIndex, AccountExportValueObject accountExport, SearchRecordValueObject document)
			throws IOException {
		return super.createDocument(records, outputStream, documentIndex, EXPORT_FORMAT_JSON, accountExport, document);
	}
}
