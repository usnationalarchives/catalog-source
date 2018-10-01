package gov.nara.opa.common.services.docstransforms.impl.doccreators;

import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.common.valueobject.export.ValueHolderValueObject;
import gov.nara.opa.common.valueobject.search.SearchRecordValueObject;
import gov.nara.opa.common.valueobject.search.SolrQueryResponseValueObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class TextDocumentCreator extends AbstractDocumentCreator implements
		DocumentCreator {

	public static final int MAX_LINE_LENGTH = 164;

	@Override
	protected int getMaxLineLength() {
		return MAX_LINE_LENGTH;
	}

	@Override
	public void createDocument(LinkedList<ValueHolderValueObject> records,
			OutputStream outputStream, int documentIndex, int totalDocuments,
			boolean flush, String resultType,
			AccountExportValueObject accountExport, Object writer,
			SearchRecordValueObject document,
			SolrQueryResponseValueObject queryResponse) throws IOException {
		StringBuilder sb = new StringBuilder();
		if (documentIndex == 1) {
			sb.append("National Archives Catalog\n");
			sb.append("U.S. National Archives and Records Administration\n\n");
			sb.append(String.format(
					"%1$d results retrieved for the query submitted\n\n",
					accountExport.getTotalRecordsToBeProcessed()));
		}
		if (records == null) {
			if (documentIndex == totalDocuments) {
				writeFooter(sb);
			}
			outputStream.write(sb.toString().getBytes());
			return;
		}
		records = flattenValue(records);
		populatePublicContributions(records, document, accountExport);
		populateThumbnails(records, document, accountExport);
		sb.append(getSeparator(documentIndex));

		for (ValueHolderValueObject record : records) {
			appendRecord(record, sb);
		}
		sb.append("\n");
		if (documentIndex == totalDocuments) {
			writeFooter(sb);
		}
		outputStream.write(sb.toString().getBytes());
		if (flush) {
			outputStream.flush();
		}
	}

	private void writeFooter(StringBuilder sb) {
		sb.append("National Archives Catalog\n");
		sb.append("U.S. National Archives and Records Administration\n");
		sb.append("8601 Adelphi Road\n");
		sb.append("College Park, MD  20740\n");
		sb.append("Email:  search@nara.gov\n");
		sb.append("On the web:   http://www.archives.gov/research/search/\n\n");
		sb.append("Disclaimer:\n");
		sb.append("The user contributed portion of this description has been contributed by a Citizen Archivist. NARA has not\n");
		sb.append("reviewed these contributions and cannot guarantee the information is complete, accurate or authoritative.\n");
	}

}
