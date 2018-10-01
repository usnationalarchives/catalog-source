package gov.nara.opa.common.services.docstransforms.impl.doccreators;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.common.services.docstransforms.Constants;
import gov.nara.opa.common.services.docstransforms.DocumentCreatorService;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.common.valueobject.export.ValueHolderValueObject;
import gov.nara.opa.common.valueobject.search.SearchRecordValueObject;
import gov.nara.opa.common.valueobject.search.SolrQueryResponseValueObject;

@Component
@Scope("prototype")
public class DocumentCreatorServiceImpl implements DocumentCreatorService,
		InitializingBean, Constants {
	
	OpaLogger logger = OpaLogger.getLogger(DocumentCreatorServiceImpl.class);

	public Map<String, DocumentCreator> documentCreators = new ConcurrentHashMap<String, DocumentCreator>();

	@Autowired
	TextDocumentCreator textDocumentCreator;

	@Autowired
	JsonDocumentCreator jsonDocumentCreator;

	@Autowired
	XmlDocumentCreator xmlDocumentCreator;

	@Autowired
	JsonPublicAPIDocumentCreator jsonPublicApiDocumentCreator;

	@Autowired
	XmlPublicAPIDocumentCreator xmlPublicApiDocumentCreator;

	@Autowired
	PdfDocumentCreator pdfDocumentCreator;

	@Autowired
	CsvDocumentCreator csvDocumentCreator;

	@Autowired
	PrintDocumentCreator printDocumentCreator;

	@Override
	public void createDocument(LinkedList<ValueHolderValueObject> records,
			AccountExportValueObject accountExport, OutputStream outputStream,
			int documentIndex, int totalDocuments, boolean flush,
			String resultType, Object writer, SearchRecordValueObject document,
			SolrQueryResponseValueObject queryResponse) {

		logger.trace(String.format("------------------- DocumentCreator... line 64 document: %s", document));
		try {
			if(document != null) {
				logger.debug(String.format("Creating document for id %1$s in format %2$s", document.getOpaId(), accountExport.getExportFormat()));
			} else {
				logger.debug(String.format("Document is null: %1$d", documentIndex));
			}

			documentCreators.get(
					accountExport.getExportFormat()
							+ accountExport.getApiType()).createDocument(
					records, outputStream, documentIndex, totalDocuments,
					flush, resultType, accountExport, writer, document,
					queryResponse);
		} catch (IOException e) {
			throw new OpaRuntimeException(e);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		documentCreators.put(EXPORT_FORMAT_TEXT
				+ AbstractRequestParameters.INTERNAL_API_TYPE,
				textDocumentCreator);
		documentCreators.put(EXPORT_FORMAT_TEXT
				+ AbstractRequestParameters.PUBLIC_API_TYPE,
				textDocumentCreator);
		documentCreators.put(EXPORT_FORMAT_JSON
				+ AbstractRequestParameters.INTERNAL_API_TYPE,
				jsonDocumentCreator);
		documentCreators.put(EXPORT_FORMAT_JSON
				+ AbstractRequestParameters.PUBLIC_API_TYPE,
				jsonPublicApiDocumentCreator);
		documentCreators.put(EXPORT_FORMAT_XML
				+ AbstractRequestParameters.INTERNAL_API_TYPE,
				xmlDocumentCreator);
		documentCreators.put(EXPORT_FORMAT_XML
				+ AbstractRequestParameters.PUBLIC_API_TYPE,
				xmlPublicApiDocumentCreator);
		documentCreators.put(EXPORT_FORMAT_CSV
				+ AbstractRequestParameters.INTERNAL_API_TYPE,
				csvDocumentCreator);
		documentCreators
				.put(EXPORT_FORMAT_CSV
						+ AbstractRequestParameters.PUBLIC_API_TYPE,
						csvDocumentCreator);
		documentCreators.put(EXPORT_FORMAT_PDF
				+ AbstractRequestParameters.INTERNAL_API_TYPE,
				pdfDocumentCreator);
		documentCreators
				.put(EXPORT_FORMAT_PDF
						+ AbstractRequestParameters.PUBLIC_API_TYPE,
						pdfDocumentCreator);
		documentCreators.put(EXPORT_FORMAT_PRINT
				+ AbstractRequestParameters.INTERNAL_API_TYPE,
				printDocumentCreator);
	}
}
