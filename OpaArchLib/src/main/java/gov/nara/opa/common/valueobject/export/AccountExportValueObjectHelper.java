package gov.nara.opa.common.valueobject.export;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.stereotype.Component;
import org.supercsv.encoder.DefaultCsvEncoder;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.TimestampUtils;
import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.common.services.docstransforms.Constants;
import gov.nara.opa.common.validation.export.CreateAccountExportRequestParameters;
import gov.nara.opa.common.validation.print.PrintResultsRequestParameters;
import gov.nara.opa.common.validation.search.ApiSearchRequestParameters;

@Component
public class AccountExportValueObjectHelper implements Constants,
		AccountExportValueObjectConstants {
  
  private static OpaLogger logger = OpaLogger.getLogger(AccountExportValueObjectHelper.class);

	public static final String COMMENTS = "comments";
	public static final String THUMBNAILS = "thumbnails";
	public static final String TAGS = "tags";
	public static final String TRANSCRIPTIONS = "transcriptions";
	public static final String TRANSLATIONS = "translations";
	public static final String CONTENT = "content";
	public static final String METADATA = "metadata";
	public static boolean useS3Storage = false;

	public static final List<String> VALID_WHAT_VALUES = new ArrayList<String>();
	static {

		VALID_WHAT_VALUES.add(THUMBNAILS);
		VALID_WHAT_VALUES.add(TAGS);
		VALID_WHAT_VALUES.add(TRANSCRIPTIONS);
		VALID_WHAT_VALUES.add(CONTENT);
		VALID_WHAT_VALUES.add(COMMENTS);

		// To be uncommented once this contribution types are supported
		// VALID_WHAT_VALUES.add(TRANSLATIONS);
	}

	public static final List<String> DONT_SEND_TO_SOLR_PARAMS = new ArrayList<String>();
	static {

		DONT_SEND_TO_SOLR_PARAMS
				.add(CreateAccountExportRequestParameters.BULK_EXPORT_PARAM_NAME);
		DONT_SEND_TO_SOLR_PARAMS
				.add(CreateAccountExportRequestParameters.BULK_EXPORT_CONTENT_PARAM_NAME);
		DONT_SEND_TO_SOLR_PARAMS
				.add(CreateAccountExportRequestParameters.EXPORT_FORMAT_PARAM_NAME);
		DONT_SEND_TO_SOLR_PARAMS
				.add(CreateAccountExportRequestParameters.EXPORT_TYPE_PARAM_NAME);
		DONT_SEND_TO_SOLR_PARAMS
				.add(CreateAccountExportRequestParameters.ROWS_HTTP_PARAM_NAME);
		DONT_SEND_TO_SOLR_PARAMS
				.add(CreateAccountExportRequestParameters.OFFSET_HTTP_PARAM_NAME);
		DONT_SEND_TO_SOLR_PARAMS
				.add(CreateAccountExportRequestParameters.EXPORT_WHAT_PARAM_NAME);
		DONT_SEND_TO_SOLR_PARAMS
				.add(CreateAccountExportRequestParameters.PROCESSING_HINT_PARAM_NAME);
		DONT_SEND_TO_SOLR_PARAMS
				.add(CreateAccountExportRequestParameters.LIST_NAME_PARAM_NAME);
	}

	public static String toJsonString(Object object) {
		if (object == null) {
			return null;
		}
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(object);
		} catch (IOException e) {
			throw new OpaRuntimeException(e);
		}
	}

	public static ConcurrentHashMap<String, String[]> fromJsonStringMapOfStrings(
			String objectString) {
		if (objectString == null) {
			return null;
		}
		ObjectMapper mapper = new ObjectMapper();
		try {
			TypeReference<ConcurrentHashMap<String, String[]>> typeRef = new TypeReference<ConcurrentHashMap<String, String[]>>() {
			};
			return mapper.readValue(objectString, typeRef);
		} catch (IOException e) {
			throw new OpaRuntimeException(e);
		}
	}

	public static ArrayList<String> fromJsonStringListOfStrings(
			String objectString) {
		if (objectString == null) {
			return null;
		}
		ObjectMapper mapper = new ObjectMapper();
		try {
			TypeReference<ArrayList<String>> typeRef = new TypeReference<ArrayList<String>>() {
			};
			return mapper.readValue(objectString, typeRef);
		} catch (IOException e) {
			throw new OpaRuntimeException(e);
		}
	}

	public AccountExportValueObject createAccountExportForInsert(
			CreateAccountExportRequestParameters requestParameters,
			Integer accountId, String userName,
			Map<String, String[]> queryParameters, int expirationDays) {
		AccountExportValueObject valueObject = new AccountExportValueObject();
		if (EXPORT_FORMAT_PRINT.equals(requestParameters.getExportFormat())) {
			valueObject.setCallerType(CALLER_TYPE_PRINT);
		} else {
			valueObject.setCallerType(CALLER_TYPE_EXPORT);
		}
		valueObject.setAccountId(accountId);
		valueObject.setExportFormat(requestParameters.getExportFormat());
		valueObject.setApiType(requestParameters.getApiType());
		valueObject.setIncludeComments(requestParameters.getExportWhat()
				.contains(COMMENTS));
		valueObject.setIncludeContent(requestParameters.getExportWhat()
				.contains(CONTENT));
		valueObject.setIncludeTags(requestParameters.getExportWhat().contains(
				TAGS));
		valueObject.setIncludeThumbnails(requestParameters.getExportWhat()
				.contains(THUMBNAILS));
		valueObject.setIncludeTranscriptions(requestParameters.getExportWhat()
				.contains(TRANSCRIPTIONS));
		valueObject.setIncludeTranslations(requestParameters.getExportWhat()
				.contains(TRANSLATIONS));
		if (AbstractRequestParameters.PUBLIC_API_TYPE.equals(requestParameters
				.getApiType())
				&& CALLER_TYPE_EXPORT == valueObject.getCallerType()) {
			valueObject.setIncludeMetadata(requestParameters.getExportWhat()
					.contains(METADATA));
		}
		Timestamp currentTime = new Timestamp(new Date().getTime());
		valueObject.setLastActionTs(currentTime);
		valueObject.setExpiresTs(TimestampUtils.addDaysToTs(currentTime, expirationDays));
		valueObject.setExportName(getExportName(userName));
		valueObject.setListName(requestParameters.getListName());
		valueObject.setProcessingHint(requestParameters.getProcessingHint());
		valueObject.setQueryParameters(queryParameters);
		valueObject.setRequestStatus(AccountExportStatusEnum.QUEUED);
		valueObject.setRequestTs(currentTime);
		valueObject.setRows(requestParameters.getRows());
		valueObject.setOffset(requestParameters.getOffset());
		valueObject.setSort(requestParameters.getSort());
		valueObject.setExportType(requestParameters.getExportType());
		valueObject.setBulkExportContent(requestParameters
				.getBulkExportContent());
		valueObject.setBulkExport(requestParameters.getBulkExport());
		setPrintFormat(requestParameters, valueObject,
				requestParameters.getRecordLine());
		return valueObject;
	}

	public AccountExportValueObject createAccountExportForSearch(
			ApiSearchRequestParameters requestParameters, Integer accountId,
			String userName, int expirationDays) {
		AccountExportValueObject valueObject = new AccountExportValueObject();
		valueObject.setApiRequestParams(requestParameters
				.getAspireObjectContent(null));
		valueObject.setCallerType(CALLER_TYPE_SEARCH);
		valueObject.setAccountId(accountId);
		valueObject.setExportFormat(requestParameters.getFormat());
		valueObject.setApiType(requestParameters.getApiType());
		valueObject.setIncludeComments(true);
		valueObject.setIncludeContent(false);
		valueObject.setIncludeTags(true);
		valueObject.setIncludeThumbnails(true);
		valueObject.setIncludeTranscriptions(true);
		valueObject.setIncludeTranslations(true);
		Timestamp currentTime = new Timestamp(new Date().getTime());
		valueObject.setLastActionTs(currentTime);
		valueObject.setExpiresTs(TimestampUtils.addDaysToTs(currentTime, expirationDays));
		valueObject.setExportName(getExportName(userName));
		valueObject.setQueryParameters(requestParameters.getQueryParameters());
		valueObject.setRequestStatus(AccountExportStatusEnum.QUEUED);
		valueObject.setRequestTs(currentTime);
		valueObject.setRows(requestParameters.getRows());
		valueObject.setOffset(requestParameters.getOffset());
		valueObject.setSort(requestParameters.getSort());
		valueObject.setExportType(Constants.EXPORT_TYPE_FULL);
		valueObject.setBulkExportContent(null);
		valueObject.setBulkExport(false);
		valueObject.setIncludeOpaResponseWrapper(true);
		setPrintFormat(requestParameters, valueObject,
				requestParameters.getRecordLine());
		return valueObject;
	}

	private void setPrintFormat(AbstractRequestParameters requestParameters,
			AccountExportValueObject accountExport, Boolean recordLine) {
		if (requestParameters.getApiType().equals(
				AbstractRequestParameters.INTERNAL_API_TYPE)) {
			accountExport.setPrintingFormat(PRINTING_RECORD_LINE);
			return;
		}

		if (recordLine != null && recordLine) {
			accountExport.setPrintingFormat(PRINTING_RECORD_LINE);
			return;
		}
		if (requestParameters.isPretty()) {
			accountExport.setPrintingFormat(PRINTING_FORMAT_PRETTY_TRUE);
		} else {
			accountExport.setPrintingFormat(PRINTING_FORMAT_PRETTY_FALSE);
		}
	}

	public AccountExportValueObject createAccountExportForPrint(
			PrintResultsRequestParameters requestParameters, Integer accountId,
			String userName, Map<String, String[]> queryParameters) {
		AccountExportValueObject valueObject = new AccountExportValueObject();
		valueObject.setCallerType(CALLER_TYPE_PRINT);
		valueObject.setAccountId(accountId);
		valueObject.setExportFormat(requestParameters.getExportFormat());
		valueObject.setIncludeComments(requestParameters.getExportWhat()
				.contains(COMMENTS));
		valueObject.setIncludeContent(requestParameters.getExportWhat()
				.contains(CONTENT));
		valueObject.setIncludeTags(requestParameters.getExportWhat().contains(
				TAGS));
		valueObject.setIncludeThumbnails(requestParameters.getExportWhat()
				.contains(THUMBNAILS));
		valueObject.setIncludeTranscriptions(requestParameters.getExportWhat()
				.contains(TRANSCRIPTIONS));
		valueObject.setIncludeTranslations(requestParameters.getExportWhat()
				.contains(TRANSLATIONS));
		Timestamp currentTime = new Timestamp(new Date().getTime());
		valueObject.setQueryParameters(queryParameters);
		valueObject.setRequestStatus(AccountExportStatusEnum.QUEUED);
		valueObject.setRequestTs(currentTime);
		valueObject.setRows(requestParameters.getRows());
		valueObject.setOffset(requestParameters.getOffset());
		valueObject.setSort(requestParameters.getSort());
		valueObject.setExportType(requestParameters.getPrintType());
		valueObject.setApiType(requestParameters.getApiType());
		valueObject.setBulkExport(false);
		valueObject.setPrintingFormat(PRINTING_RECORD_LINE);
		return valueObject;
	}

	private String getExportName(String userName) {
		DateTime dt = new DateTime();
		return userName + " - " + dt.toDateTime(DateTimeZone.UTC).toString();
	}

	/**
	 * Retruns a Map<String, String[]> with only the parameters that are allowed
	 * to be sent to Solr
	 * 
	 * @param queryParameters
	 * @param apiType
	 * @return
	 */
	public static Map<String, String[]> scrubQueryParameters(
			Map<String, String[]> queryParameters, String apiType) {
		Map<String, String[]> returnValue = new ConcurrentHashMap<String, String[]>();
		for (String key : queryParameters.keySet()) {
			if (!DONT_SEND_TO_SOLR_PARAMS.contains(key)) {

				List<String> paramValues = new ArrayList<String>();
				String[] params = queryParameters.get(key);
				for (String value : params) {
					try {
						paramValues.add(new String(value.getBytes(), "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						logger.error(e);
					}
				}

				String[] paramValueString = new String[1];
				paramValueString = paramValues.toArray(paramValueString);

				returnValue.put(key, paramValueString);
			}
		}
		if (AbstractRequestParameters.INTERNAL_API_TYPE.equals(apiType)) {
			returnValue.put(EXCLUDE_RESULT_TYPES_HTTP_PARAM_NAME,
					new String[] { "object" });
		}
		return returnValue;
	}

	public static String getDownloadUrl(Integer exportId, Integer accountId,
			String apiType, String url) {
		if (accountId == null || accountId == 0) {
			return "/OpaAPI/" + apiType + "/v1/exports/noauth/files/"
					+ exportId;
		} else {
			return "/OpaAPI/" + apiType + "/v1/exports/auth/files/" + exportId;
		}
	}

	public static Integer getPercentageComplete(
			Integer totalRecordsToBeProcessed, Integer totalRecordsProcessed) {

		if (totalRecordsToBeProcessed == null || totalRecordsToBeProcessed == 0) {
			return 0;
		}
		double percentageComplete = totalRecordsProcessed
				/ totalRecordsToBeProcessed.doubleValue();
		return (int) (percentageComplete * 100);
	}

	public static final List<String> getResultFields(
			AccountExportValueObject accountExport) {
		String[] resultFieldsParam = accountExport.getQueryParameters().get(
				"resultFields");
		if (resultFieldsParam == null) {
			return null;
		}
		String[] resultFieldsArray = resultFieldsParam[0].split(",");
		return Arrays.asList(resultFieldsArray);
	}

	public static Object getWriter(AccountExportValueObject accountExport,
			OutputStream outputStream) {
		Object writer = null;
		if (EXPORT_FORMAT_PDF.equals(accountExport.getExportFormat())) {
			Document pdfDocument = new Document();

			PdfWriter pdfWriter = null;

			try {
				pdfWriter = PdfWriter.getInstance(pdfDocument, outputStream);
			} catch (DocumentException e) {
				throw new OpaRuntimeException(e);
			}
			pdfDocument.open();
			writer = new Object[] { pdfDocument, pdfWriter };
		} else if (EXPORT_FORMAT_CSV.equals(accountExport.getExportFormat())) {
			// work around to fix the bug described here:
			// http://sourceforge.net/p/supercsv/bugs/43/
			// create a new instance of CsvPreferences for each thread
			writer = new CsvMapWriter(
					new OutputStreamWriter(outputStream),
					new CsvPreference.Builder(CsvPreference.STANDARD_PREFERENCE)
							.useEncoder(new DefaultCsvEncoder()).build());
			// CsvPreference.STANDARD_PREFERENCE);
		}
		return writer;
	}

	public static void closeWriter(AccountExportValueObject accountExport,
			Object writer) throws IOException {

		if (EXPORT_FORMAT_PDF.equals(accountExport.getExportFormat())) {
			Document pdfDocument = (Document) ((Object[]) writer)[0];
			try {
				pdfDocument.add(new Chunk(""));
			} catch (DocumentException e) {
				throw new OpaRuntimeException(e);
			}
			pdfDocument.close();

		} else if (EXPORT_FORMAT_CSV.equals(accountExport.getExportFormat())) {
			CsvMapWriter csvWriter = (CsvMapWriter) writer;
			csvWriter.close();
		}

	}

	public static String getResultFieldsForNoMetadata(
			AccountExportValueObject accountExport) {
		String resultFields = "";
		if (accountExport.getIncludeTags()) {
			resultFields = resultFields
					+ "publicContributions.tags,objects.object.publicContributions.tags,";
		}
		if (accountExport.getIncludeTranscriptions()) {
			resultFields = resultFields
					+ "publicContributions.transcription,objects.object.publicContributions.transcription,";
		}
		if (accountExport.getIncludeComments()) {
			//logger.debug("Not adding new default no metadafields for comments");
			resultFields = resultFields
					+ "publicContributions.comments,objects.object.publicContributions.comments,naId,";
		}
		if (accountExport.getIncludeThumbnails()) {
			resultFields = resultFields + "objects.object.thumbnail,naId,";
		}
		if (resultFields.endsWith(",")) {
			//NARA-2681 - json and xml documents where not being extracted because of the
            //check for extractValuesNeeded.  
            //if (accountExport.extractValuesNeeded()) {
				resultFields = resultFields + "level,type";
			//} else {
				//resultFields = resultFields.substring(0,
					//	resultFields.length() - 1);
			//}
		}
		return resultFields;
	}
	
	public String getExportFileRelativeLocation(AccountExportValueObject accountExport, boolean isCompressed) {
		String fileNameFormat = "nara-%1$sbulk-export-%2$d.%3$s";
		
		String exportId = accountExport.getExportId().toString();
		String fileName; 
				
		if(isCompressed) {
			fileName = String.format(fileNameFormat,
					(!accountExport.getBulkExport() ? "non-" : ""),
					accountExport.getExportId(), 
					"tar.gz");
		} else {
			fileName = String.format(fileNameFormat,
					(!accountExport.getBulkExport() ? "non-" : ""),
					accountExport.getExportId(), 
					accountExport.getExportFormat());			
		}
		
		return exportId + "/" + fileName;
	}

}
