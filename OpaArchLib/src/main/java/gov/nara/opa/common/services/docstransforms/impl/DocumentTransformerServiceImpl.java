package gov.nara.opa.common.services.docstransforms.impl;

import gov.nara.opa.architecture.exception.OpaSkipRecordException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.common.services.docstransforms.DocumentCreatorService;
import gov.nara.opa.common.services.docstransforms.DocumentTransformerService;
import gov.nara.opa.common.services.docstransforms.DocumentValuesExtractorService;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.common.valueobject.export.ValueHolderValueObject;
import gov.nara.opa.common.valueobject.search.SearchRecordValueObject;
import gov.nara.opa.common.valueobject.search.SolrQueryResponseValueObject;

import java.io.OutputStream;
import java.util.LinkedList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class DocumentTransformerServiceImpl implements DocumentTransformerService {

	OpaLogger logger = OpaLogger.getLogger(DocumentTransformerServiceImpl.class);

	@Autowired
	DocumentValuesExtractorService valuesExtractor;

	@Autowired
	DocumentCreatorService documentCreatorService;

	@Override
	public void transformDocument(SearchRecordValueObject document, AccountExportValueObject accountExport,
			OutputStream outputStream, int documentIndex, int totalDocuments, boolean flush, Object writer,
			SolrQueryResponseValueObject queryResponse) {
		logger.trace(String.format("------------------- DocumentTra... line 36 document: %s", document));
		if(document != null) {
			String opaId = document.getOpaId();
			logger.debug(String.format("Transforming document: %1$s", (opaId != null ? opaId : document.toString().substring(0, 10))));
		} else {
			logger.debug(String.format("Document is null: %1$d", documentIndex));
		}
		
		LinkedList<ValueHolderValueObject> records = null;
		if (document != null && accountExport.extractValuesNeeded()) {
			try {
				records = valuesExtractor.getValues(document, accountExport);
			} catch (OpaSkipRecordException ex) {
				document = null;
				logger.trace(String.format("--------------------%s",ex.getMessage()));
				
			}
		}
		String resultType = null;
		if (document != null) {
			resultType = document.getResultType();
			
			
			logger.debug(String.format("Result type of document %1$s is %2$s", document.getOpaId(), resultType));
		}
		documentCreatorService.createDocument(records, accountExport, outputStream, documentIndex, totalDocuments,
				flush, resultType, writer, document, queryResponse);

	}

}
