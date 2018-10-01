package gov.nara.opa.api.services.impl.export;
import gov.nara.opa.common.services.docstransforms.impl.*;

import gov.nara.opa.common.services.docstransforms.impl.*;
import gov.nara.opa.api.dataaccess.user.UserAccountDao;
import gov.nara.opa.api.dataaccess.user.lists.UserListDao;
import gov.nara.opa.api.security.OPAAuthenticationProvider;
import gov.nara.opa.api.services.export.NonBulkExportService;
import gov.nara.opa.api.services.system.ConfigurationService;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.api.user.lists.UserList;
import gov.nara.opa.api.user.lists.UserListItem;
import gov.nara.opa.api.validation.ErrorCodeConstants;
import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.exception.OpaSkipRecordException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.SolrUtils;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.ValidationError;
import gov.nara.opa.common.services.docstransforms.Constants;
import gov.nara.opa.common.services.docstransforms.DocumentTransformerService;
import gov.nara.opa.common.services.docstransforms.impl.*;

import gov.nara.opa.common.services.export.AccountExportDbProxyService;
import gov.nara.opa.common.services.solr.SolrGateway;
import gov.nara.opa.common.storage.OpaStorageFactory;
import gov.nara.opa.common.storage.StorageUtils;
import gov.nara.opa.common.validation.export.CreateAccountExportRequestParameters;
import gov.nara.opa.common.valueobject.export.AccountExportStatusEnum;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;
import gov.nara.opa.common.valueobject.export.AccountExportValueObjectConstants;
import gov.nara.opa.common.valueobject.export.AccountExportValueObjectHelper;
import gov.nara.opa.common.valueobject.search.SearchRecordValueObject;
import gov.nara.opa.common.valueobject.search.SearchRecordValueObjectHelper;
import gov.nara.opa.common.valueobject.search.SolrQueryResponseValueObjectHelper;
import gov.nara.opa.common.valueobject.user.accounts.UserAccountValueObject;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.base.Joiner;


/**
 * Fixed the transform method which wasn't processing inner arrays. now the tranform uses a stack.
 * As before the transform reads line by line from an array and tries to match the start tag.
 * Now, it also tries to match the end-tag from the last start-tag. A stack is used in this process.
 * A stack is the correct choice for json as elements are either nested within an element or they are not; there
 * is no partial nesting.
 */

@Component
public class NonBulkExportServiceImpl implements Constants,
	NonBulkExportService, AccountExportValueObjectConstants {

	@Autowired
	SolrGateway solrGateway;

	@Autowired
	ConfigurationService configurationService;

	@Autowired
	SearchRecordValueObjectHelper searchRecordValueObjectHelper;

	@Autowired
	DocumentTransformerService documentTransformerService;

	@Value("${export.nonbulk.output.location}")
	String nonbulkExportsOutputLocation;

	static OpaLogger logger = OpaLogger
			.getLogger(NonBulkExportServiceImpl.class);

	@Autowired
	AccountExportDbProxyService accountExportDaoDbProxy;
	
	@Autowired
	AccountExportValueObjectHelper exportValueObjectHelper;

	@Autowired
	private UserAccountDao userAccountDao;

	@Autowired
	private UserListDao userListDao;

	@Autowired
	StorageUtils storageUtils;

	@Autowired
	OpaStorageFactory opaStorageFactory;

	public final static int noOfRecordsToSkipBetweenLimitChecks = 1;

	@Value("${export.maxTimeForNonBulkExportRequestMillis}")
	Integer maxTimeForNonBulkExportRequest;

	@Value("${export.maxFileSizeForNonBulkExportsInBytes}")
	Integer maxFileSizeForNonBulkExports;

    @Value("${s3ExportsLocation}")
	String s3ExportsLocation;

    @Value("${export.output.location}")
    String exportOutputLocation;
    
    /**
     * This class is a simple bean class used in the transform(...) method
     * 
     *
     */
	private class MatchArray {
		private String endTag;
		private Integer startLine;
		private Integer endLine;
		private String spaces;
		private String regex;

		public MatchArray(Integer aStartLine, String aendTag, String aSpaces, String aRegex) {
			startLine = aStartLine;
			endTag = aendTag;
			spaces=aSpaces;
			regex=aRegex;
		}

		/**
		 * @return the spaces
		 */
		public String getSpaces() {
			return spaces;
		}

		/**
		 * @return the regex
		 */
		public String getRegex() {
			return regex;
		}

		/**
		 * @param spaces the spaces to set
		 */
		public void setSpaces(String spaces) {
			this.spaces = spaces;
		}

		/**
		 * @param regex the regex to set
		 */
		public void setRegex(String regex) {
			this.regex = regex;
		}

		/**
		 * @return the endTag
		 */
		public String getEndTag() {
			return endTag;
		}

		/**
		 * @return the startLine
		 */
		public Integer getStartLine() {
			return startLine;
		}

		/**
		 * @return the endLine
		 */
		public Integer getEndLine() {
			return endLine;
		}

		/**
		 * @param endTag
		 *            the endTag to set
		 */
		public void setEndTag(String endTag) {
			this.endTag = endTag;
		}

		/**
		 * @param startLine- the startLine to set
		 */
		public void setStartLine(Integer startLine) {
			this.startLine = startLine;
		}

		/**
		 * @param endLine- the endLine to set
		 */
		public void setEndLine(Integer endLine) {
			this.endLine = endLine;
		}
		
	}
	private Stack<MatchArray> matchStack = new Stack<MatchArray>();
	@Override
	public void executeExport(AccountExportValueObject accountExport,
			HttpServletResponse response, HttpServletRequest request)
			throws IOException, TimeoutException {
		long startTime = (new Date()).getTime();
		// used for csv export which changes the format to use the json
		// processing
		// and then processes the json to csv and changes the format back to csv
		// to save the file
		// as csv

		accountExport.setActualExportFormat(accountExport.getExportFormat());
		// IF IT IS FROM A LIST
		Map<String, String[]> queryParameters = accountExport
				.getQueryParameters();
		if (accountExport.getListName() != null) {
			executeExportFromList(accountExport, response, request);
			return;
		}

		// set sort
		String[] sortArray = { "score desc, ingestedDateTime desc, opaId asc" };
		if(!queryParameters.containsKey("sort")) {
			queryParameters.put("sort",sortArray);
		}
		accountExport.setSort( queryParameters.get("sort")[0] );

		QueryResponse queryResponse = null;
		try {
			logger.trace("START - Getting documents from search engine");
			queryResponse = getDocsFromSearchEngine(accountExport);
			logger.trace("END - Getting documents from search engine");
		} catch (TimeoutException tex) {
			OutputStream outputStream = getOutputStream(accountExport, response);
			Object writer = AccountExportValueObjectHelper.getWriter(accountExport,
					outputStream);
			ValidationError error = hasNonBulkExportLimitBeenReached(
					accountExport, startTime, writer);
			if (error != null) {
				if (error.getErrorCode().equals(ErrorCodeConstants.BULK_EXPORT)) {
					outputStream.flush();
					accountExport.setQueryParameters(queryParameters);
					turnNonBulkExportIntoBulExport(accountExport, writer,
							outputStream);
				}
				accountExport.setError(error);
				return;
			}
			throw tex;
		} catch (Exception e) {
			throw new IOException(e);
		}
		if (queryResponse == null) {
			throw new OpaRuntimeException(
					"The query response coming back from the solr engine was null.");
		}
		if (queryResponse.getResults() == null) {
			throw new OpaRuntimeException(
					"The query results (part of the query response) coming back from the solr engine was null.");
		}

		Iterator<SolrDocument> docs = queryResponse.getResults().iterator();
		OutputStream outputStream = getOutputStream(accountExport, response);
		Object writer = AccountExportValueObjectHelper.getWriter(accountExport,
				outputStream);
// START 84056		
//		if (EXPORT_FORMAT_JSON.equals(accountExport.getExportFormat().toLowerCase())
//				&& accountExport.getApiType().equals(AbstractRequestParameters.PUBLIC_API_TYPE)) {
//			// pass in a ByteArrayOutputStream to accumulate the result.
//			// the result should be formatted json
//			ByteArrayOutputStream bout = new ByteArrayOutputStream();
//			doExport(accountExport, response, request, docs, startTime, queryResponse, bout, writer, true);
//
//			try {
//				logger.error("TRANSFORMING DOCUMENT");
//				/**
//				 * bout should contain pretty formatted json the AspireObject's
//				 * toJson, formats the json to the correct format for the
//				 * transform. The format puts the opening quote of the key-field
//				 * directly above the closing bracket, }, for that object. for
//				 * example: "SomeArray": { ... }
//				 */
//				String t = transform(bout.toString());
//				logger.error("TRANSFORM COMPLETED");
//				outputStream.write(t.getBytes());
//			} catch (Exception e) {
//				safeWrite(e.getMessage(), outputStream);
//				logger.error(e.getStackTrace());
//				logger.error(e.getMessage());
//			}
//
//		} else {
			String exportFormat = accountExport.getExportFormat().toLowerCase();

			if (EXPORT_FORMAT_CSV.toLowerCase().equals(exportFormat)) {
				try {
					long exportId = accountExport.getExportId();
					logger.error("csv export start for: "+accountExport.getNonBulkExportFileName());
					ByteArrayOutputStream bout = new ByteArrayOutputStream();
					logger.error("doExport for CSV");
					accountExport.setExportFormat(EXPORT_FORMAT_JSON);
					accountExport.setApiType(AbstractRequestParameters.INTERNAL_API_TYPE);
					writer = AccountExportValueObjectHelper.getWriter(accountExport, bout);
					logger.error("calling doExport for JSON. actual Format: "+accountExport.getActualExportFormat());
					logger.error("doExport. export ID: "+exportId);
					logger.error("doExport. accountExport.getFileToWriteTo(): "+accountExport.getFileToWriteTo());
					doExport(accountExport, response, request, docs, startTime, queryResponse, bout, writer, false);
					logger.error("called doExport for JSON");
					accountExport.getFileToWriteTo();
					accountExport.setExportFormat(EXPORT_FORMAT_CSV);
					String jsonArrayData = ((ByteArrayOutputStream) bout).toString();// fileToString(accountExport.getFileToWriteTo());
					//logger.error("doExport. jsonArrayData=" + jsonArrayData);
					JSONArray jsonArray = new JSONArray(jsonArrayData);
					ConvertJSONToCsv convert = new ConvertJSONToCsv(Long.MAX_VALUE, Long.MAX_VALUE);
					try {
						convert.processJSON(jsonArray);
					} catch (JSONException | MaxNonBulkSizeExceededException | MaxNonBulkTimeExceededException e) {
						logger.error(stackTraceToString(e));
					}
					String data = convert.getCSV();
					File csvFile = getNonbulkExportFile(accountExport, false);
					logger.error("doExport. getNonBulkExportFile name: "+csvFile.getName());
					// logger.error("completed csv export. file is: " +
					// csvFile.getAbsolutePath() + ", data follows");
					logger.error(data);
					//toFile(data, csvFile);
					outputStream.write(data.getBytes());
					outputStream.flush();
					//
					//
					if (accountExport != null && accountExport.getFileToWriteTo() != null) {
						logger.trace(String.format("Getting url from storage"));
						storageUtils.getFinalUrl(accountExport, accountExport.getFileToWriteTo().getAbsolutePath(),
								nonbulkExportsOutputLocation);
					}

					readNonBulkExportFileIntoHttpStream(accountExport, response, request);
					if (CALLER_TYPE_EXPORT == accountExport.getCallerType()) {
						accountExportDaoDbProxy.update(accountExport);
					}
					logger.error("csv export finished for: "+accountExport.getNonBulkExportFileName());
				} catch (Exception e) {
					logger.error("csv export failed for: "+accountExport.getNonBulkExportFileName());
					logger.error("ERROR: "+e+", export failed for "+accountExport.getNonBulkExportFileName());
				}
			} // end CSV

			else {
				logger.error("calling normal export for exportFormat="+exportFormat);
				doExport(accountExport, response, request, docs, startTime, queryResponse, outputStream, writer, true);
				logger.error("called normal export completed");
			}
//		}
		try {
			if (outputStream != null) {
				outputStream.close();
			}
		} catch (Exception e) {
			//
		}
	}
	public static boolean safeWrite(String s,OutputStream o){
		try{
			o.write(s.getBytes());
			return true;
		}catch(Exception e){
			// do nothing
		}
		return false;
	}
	public static String repeatString(String s, int times) {
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < times; i++) {
			b.append(s);
		}
		return b.toString();
	}
	/**
	 * 
	 * @param json - a pretty formatted json. 
	 * @return
	 */
	public String transform(String json) {
		matchStack=new Stack<MatchArray>();
		String[] lines = json.split("\n");
		Pattern p = Pattern.compile("\\s+\"(.*?)Array\":\\s*\\{");
		String endTag = null;
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			Matcher m = p.matcher(line);
			if (m.matches()) {
				// the closing bracket , }, should align with the first quote
				int start = line.indexOf('"');
				String spaces = repeatString(" ", start);
				Pattern p2 = Pattern.compile(spaces + "\\}");
				endTag = spaces + "}";
				MatchArray ma = new MatchArray(i, endTag, spaces, endTag);
				matchStack.push(ma);

				// line2 = line2.replaceFirst(s + "\\}", s + "}]");
			} else if (endTag != null) {
				if (line.startsWith(endTag)) {
					MatchArray ma = matchStack.pop();
					int istart = ma.getStartLine();
					ma.setEndLine(i);
					lines[istart] = lines[istart].replaceFirst("\\{", "[{");
					String spaces=ma.getSpaces();
					lines[i] = lines[i].replaceFirst(spaces + "\\}", spaces + "}]");
					if(!matchStack.isEmpty()){
						endTag=matchStack.peek().getEndTag();
					}else{
						endTag=null;
					}
				}
			}

		}
		if(!matchStack.isEmpty()){
			StringBuffer b=new StringBuffer();
			String tab="";
			while(!matchStack.isEmpty()){
				MatchArray ma=matchStack.pop();
				b.append(tab);
				b.append("\nstart line: ");
				b.append(ma.getStartLine());
				b.append(tab);
				b.append("\nstart text: ");
				b.append(lines[ma.getStartLine()]);	
				tab=tab+"\t";
			}
			throw new RuntimeException("ERROR: missing end tags for:\n"+b.toString());
		}
		String jsonNew=flatten(lines,"\n");
		return jsonNew;
	}
	

	public final static String flatten(String[] list, String delim) {
		String f = "";
		if (list.length == 0) {
			return f;
		}
		f = f + list[0];
		for (int i = 1; i < list.length; i++) {
			f = f + delim;
			f = f + list[i];
		}
		return f;
	}
	public static String removeSpecialCharacters(String in){
		StringBuffer b=new StringBuffer();
		for(int i=0;i<in.length();i++){
			char c=in.charAt(i);
			if(c > 31 && c< 127){
				b.append(c);
			}
		}
		return b.toString();
	}

	private void executeExportFromList(AccountExportValueObject accountExport,
			HttpServletResponse response, HttpServletRequest request)
			throws IOException {

		long startTime = (new Date()).getTime();
		Map<String, String[]> queryParameters = accountExport
				.getQueryParameters();

		int maxSolrOpaIds = 200;
		OutputStream outputStream = getOutputStream(accountExport, response);
		Object writer = AccountExportValueObjectHelper.getWriter(accountExport,
				outputStream);
		SolrDocumentList list = null;
		String[] emptyArray = {};

		QueryResponse queryResponse = null;
		String[] offSetArray = { "0" };

		// set sort
		String[] sortArray = { "score desc, ingestedDateTime desc, opaId asc" };
		if(!queryParameters.containsKey("sort")) {
			queryParameters.put("sort",sortArray);
		}
		accountExport.setSort( queryParameters.get("sort")[0] );

		queryParameters.put("offset", offSetArray);
		String username = request.getParameter("userName");
		List<String> items = new ArrayList<String>();

		//Retrieve list items
		if (userAccountDao.verifyIfUserNameExists(username)) {
			UserAccountValueObject user = userAccountDao
					.selectByUserName(username);
			Integer accountId = user.getAccountId();
			UserList userList = null;
			try {
				List<UserList> resultList = userListDao.select(accountExport.getListName(), accountId);
				userList = (resultList != null && resultList.size() > 0 ? resultList
						.get(0) : null);
			} catch (UnsupportedEncodingException e) {
				logger.error(e.getMessage(), e);
			}
			try {
				List<UserListItem> result = userListDao.selectListItems(userList.getListId());
				for (UserListItem item : result) {
					items.add(item.getOpaId());
				}

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		} else {
			ValidationError error = new ValidationError();
			error.setErrorCode(ErrorCodeConstants.USER_NOT_FOUND);
			error.setErrorMessage(ErrorConstants.USER_NAME_DOES_NOT_EXIST);
			accountExport.setError(error);
			return;
		}
		
		if (items.isEmpty() ) {
			ValidationError error = new ValidationError();
			error.setErrorCode(ErrorCodeConstants.EMPTY_LIST);
			error.setErrorMessage(ErrorConstants.INVALID_LIST_NAME);
			accountExport.setError(error);
			return;
		}
		String[] tempOpaIds = items.toArray(new String[0]);
		int times = items.size() / maxSolrOpaIds;
		if (times == 0) {
			times = 1;
		}

		//Get items from solr by chunks of 200
		int initValue = 0;
		for (int index = 0; index < times; ++index) {
			List<String> opaIds = new ArrayList<String>();
			queryParameters.put("opaIds", emptyArray);
			initValue = (index * maxSolrOpaIds);
			int endValue = initValue + maxSolrOpaIds;
			for (int i = initValue; i < items.size() && i < endValue; i++) {
				opaIds.add(items.get(i));
			}

			String ids = Joiner.on(",").join(opaIds);
			String[] array = { ids };
			queryParameters.put("opaIds", array);

			accountExport.setQueryParameters(queryParameters);
			accountExport.setOffset(null);

			//Retrieve ids in subset
			try {
				queryResponse = getDocsFromSearchEngine(accountExport);
			} catch (Exception e) {
				throw new IOException(e);
			}

			if (queryResponse == null) {
				throw new OpaRuntimeException(
						"The query response coming back from the solr engine was null.");
			}
			if (queryResponse.getResults() == null) {
				throw new OpaRuntimeException(
						"The query results (part of the query response) coming back from the solr engine was null.");
			}

			if (list == null) {
				list = queryResponse.getResults();
			} else {
				list.addAll(queryResponse.getResults());
			}
			
			//MISSING LIST ITEMS
			List<String> missing = new ArrayList<String>();
			boolean exists = true;
			for(String opaId : opaIds) {
				for(SolrDocument doc : list) {
					if(doc.get("opaId").equals(opaId)) {
						exists = true;
						break;
					}
				}
				if(!exists) {
					missing.add(opaId);
				}
				exists = false;
			}
			logger.debug(String.format("Missing items in list: %1$s", missing.toString()));
			
			ValidationError error = hasNonBulkExportLimitBeenReached(
					accountExport, startTime, writer);
			if (error != null) {
				if (error.getErrorCode().equals(ErrorCodeConstants.BULK_EXPORT)) {
					outputStream.flush();
					queryParameters.put("opaIds", tempOpaIds);
					accountExport.setQueryParameters(queryParameters);
					turnNonBulkExportIntoBulExport(accountExport, writer,
							outputStream);
				}
				accountExport.setError(error);
				return;
			}

		}

		//Set totals and list iterator
		queryParameters.put("opaIds", tempOpaIds);
		Iterator<SolrDocument> docs = null;
		if (list != null) {
			docs = list.iterator();
			accountExport.setTotalRecordsToBeProcessed(list.size());
		}

		//Execute export over retrieved items list
		doExport(accountExport, response, request, docs, startTime,
				queryResponse, outputStream, writer,true);
	}

	private void doExport(AccountExportValueObject accountExport, HttpServletResponse response,
			HttpServletRequest request, Iterator<SolrDocument> docs, long startTime, QueryResponse queryResponse,
			OutputStream outputStream, Object writer, boolean closeFile) throws IOException {
		int documentIndex = 1;
		int totalRecordsToBeProcessed = accountExport
				.getTotalRecordsToBeProcessed();
		logger.trace(String.format("------------------- NonBulkExportServiceImpl line 332 totalRecordsToBeProcessed: %s", totalRecordsToBeProcessed));
		int noOfSkips = 0, recsProcessed = 0;

		if (totalRecordsToBeProcessed == 0
				&& !accountExport.getExportFormat().equals(EXPORT_FORMAT_PRINT)) {
			documentTransformerService.transformDocument(null, accountExport,
					outputStream, documentIndex, 1, true, writer,
					SolrQueryResponseValueObjectHelper.create(queryResponse));
		} else {

			//Print/export
			if (docs != null) {
				while (docs.hasNext()) {
					if (recsProcessed % noOfRecordsToSkipBetweenLimitChecks == 0) {
						outputStream.flush();
						ValidationError error = hasNonBulkExportLimitBeenReached(
								accountExport, startTime, writer);
						if (error != null) {
							if (error.getErrorCode().equals(
									ErrorCodeConstants.BULK_EXPORT)) {
								turnNonBulkExportIntoBulExport(accountExport,
										writer, outputStream);
							}
							accountExport.setError(error);
							return;
						}
					}
					SearchRecordValueObject searchRecord = null;

					try {
						logger.debug("Creating search record");
						searchRecord = searchRecordValueObjectHelper
								.createSolrRecord(docs.next(), accountExport);
					} catch (OpaSkipRecordException ex) {
						noOfSkips++;
						searchRecord = null;
					}

					Date sTime = new Date();
					logger.debug(String.format("Starting document transform: %1$s for docId: %2$s", sTime.toString(), (searchRecord != null ? searchRecord.getOpaId() : "unknown")));
					try {
						documentTransformerService.transformDocument(searchRecord, accountExport, outputStream,
								documentIndex, totalRecordsToBeProcessed, true, writer,
								SolrQueryResponseValueObjectHelper.create(queryResponse));
					} catch (Exception e) {
						logger.error("ERROR:transform failed.\n" + stackTraceToString(e), e);
					}
					Date eTime = new Date();
					logger.debug(String.format("Ended document transform: %1$s for docId: %2$s", sTime.toString(), (searchRecord != null ? searchRecord.getOpaId() : "unknown")));
					//logger.debug(String.format("Total transform time: %1$d for docId: %2$s", (eTime.getTime() - sTime.getTime())/1000d, (searchRecord != null ? searchRecord.getOpaId() : "unknown")));
					
					recsProcessed++;
					documentIndex++;
				}
			}
		}

		ValidationError error = isExportFileLargerThenNonBulkExportLimit(
				accountExport, writer);
		if (error != null) {
			if (error.getErrorCode().equals(ErrorCodeConstants.BULK_EXPORT)) {
				turnNonBulkExportIntoBulExport(accountExport, writer,
						outputStream);
			}
			accountExport.setError(error);
			return;
		}
		accountExport.setTotalSkipped(noOfSkips);
		accountExport.setTotalRecordsProcessed(recsProcessed);
		accountExport.setRequestStatus(AccountExportStatusEnum.COMPLETED);
		AccountExportValueObjectHelper.closeWriter(accountExport, writer);

		try {
			if (outputStream != null) {
				outputStream.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (closeFile) {
			if (accountExport != null && accountExport.getFileToWriteTo() != null) {
				logger.trace(String.format("Getting url from storage"));
				storageUtils.getFinalUrl(accountExport, accountExport.getFileToWriteTo().getAbsolutePath(),
						nonbulkExportsOutputLocation);
			}

			readNonBulkExportFileIntoHttpStream(accountExport, response, request);
			if (CALLER_TYPE_EXPORT == accountExport.getCallerType()) {
				accountExportDaoDbProxy.update(accountExport);
			}
		}
	}
	public static String stackTraceToString(Object e){
		if(e instanceof Exception){
		StringWriter errors = new StringWriter();
		((Exception)(e)).printStackTrace(new PrintWriter(errors));
		return errors.toString();
		}else{
			return ""+e;
		}
	}
	private void readNonBulkExportFileIntoHttpStream(
			AccountExportValueObject accountExport,
			HttpServletResponse response, HttpServletRequest request)
			throws IOException {
		if (accountExport.getApiType().equals(
				AbstractRequestParameters.PUBLIC_API_TYPE)
				&& CALLER_TYPE_EXPORT == accountExport.getCallerType()) {
			
			//Get correct url
			String relativeFilePath = exportValueObjectHelper.getExportFileRelativeLocation(accountExport, false);
			String absolutePath;
			if(AbstractBaseController.useS3Storage) {
				absolutePath = s3ExportsLocation + relativeFilePath;
			} else {
				absolutePath = exportOutputLocation + relativeFilePath;
			}
			logger.debug(String.format("Getting file from '%1$s'", absolutePath));
			AbstractBaseController.writeFileContentToResponseOldStyle(
					absolutePath,
					response, request, opaStorageFactory.createOpaStorage());
			
		}
	}

	private ValidationError hasNonBulkExportLimitBeenReached(
			AccountExportValueObject accountExport, long startTime,
			Object writer) throws IOException {
		if (accountExport.getCallerType() != CALLER_TYPE_EXPORT) {
			return null;
		}

		if (new Date().getTime() - startTime > maxTimeForNonBulkExportRequest) {
			if (OPAAuthenticationProvider.getAccountIdForLoggedInUser() == null) {
				return getLimitReachedNonAuthUserError(
						String.format(
								ErrorConstants.EXPORT_EXCEEDED_NON_BULK_TIMEOUT_NON_AUTH,
								maxTimeForNonBulkExportRequest / 1000),
								ErrorCodeConstants.BULK_EXPORT_NON_AUTH);
			}
			ValidationError error = new ValidationError();
			error.setErrorCode(ErrorCodeConstants.BULK_EXPORT);
			error.setErrorMessage(String.format(
					ErrorConstants.EXPORT_EXCEEDED_NON_BULK_TIMEOUT,
					maxTimeForNonBulkExportRequest / 1000,
					accountExport.getExportId()));
			File file = accountExport.getFileToWriteTo();
			AccountExportValueObjectHelper.closeWriter(accountExport, writer);
			file.delete();
			return error;
		}

		// File file = accountExport.getFileToWriteTo();
		// if (file != null && file.length() > maxFileSizeForNonBulkExports) {
		// if (OPAAuthenticationProvider.getAccountIdForLoggedInUser() == null)
		// {
		// return getLimitReachedNonAuthUserError(String.format(
		// ErrorConstants.EXPORT_EXCEEDED_NON_BULK_FILE_SIZE_NON_AUTH,
		// maxFileSizeForNonBulkExports),
		// ErrorCodeConstants.BULK_EXPORT_NON_AUTH);
		// }
		// ValidationError error = new ValidationError();
		// error.setErrorCode(ErrorCodeConstants.BULK_EXPORT);
		// error.setErrorMessage(String.format(
		// ErrorConstants.EXPORT_EXCEEDED_NON_BULK_FILE_SIZE,
		// maxFileSizeForNonBulkExports, accountExport.getExportId()));
		// return error;
		// }
		return null;
	}

	private ValidationError isExportFileLargerThenNonBulkExportLimit(
			AccountExportValueObject accountExport, Object writer)
			throws IOException {
		String fileLimit = String
				.valueOf(maxFileSizeForNonBulkExports / 1000000.00) + "Mb";
		File file = accountExport.getFileToWriteTo();
		if (file != null && file.length() > maxFileSizeForNonBulkExports) {
			String fileSize = String.valueOf(file.length() / 1000000.00) + "Mb";
			if (OPAAuthenticationProvider.getAccountIdForLoggedInUser() == null) {
				return getLimitReachedNonAuthUserError(
						String.format(
								ErrorConstants.EXPORT_EXCEEDED_NON_BULK_FILE_SIZE_NON_AUTH,
								fileSize, fileLimit),
								ErrorCodeConstants.BULK_EXPORT_NON_AUTH);
			}
			ValidationError error = new ValidationError();
			error.setErrorCode(ErrorCodeConstants.BULK_EXPORT);
			error.setErrorMessage(String.format(
					ErrorConstants.EXPORT_EXCEEDED_NON_BULK_FILE_SIZE,
					fileSize, fileLimit, accountExport.getExportId()));
			AccountExportValueObjectHelper.closeWriter(accountExport, writer);
			file.delete();
			return error;
		}
		return null;
	}

	private ValidationError getLimitReachedNonAuthUserError(
			String errorMessage, String errorCode) {
		ValidationError error = new ValidationError();
		error.setErrorCode(errorCode);
		error.setErrorMessage(errorMessage);
		return error;
	}

	/**
	 * had to keep this impl method in. it probably should have been declared
	 * private some time ago.
	 * 
	 * @param accountExport
	 * @return
	 * @throws IOException
	 */
	public File getNonbulkExportFile(AccountExportValueObject accountExport) throws IOException {
		return getNonbulkExportFile(accountExport, true);
	}

	/**
	 * 
	 * @param accountExport
	 * @param recreateDir
	 *            - try to remove dir before creating
	 * @return
	 * @throws IOException
	 */
	public File getNonbulkExportFile(AccountExportValueObject accountExport, boolean recreateDir) throws IOException {
		String fileName = accountExport.getNonBulkExportFileName();
		String filePath = accountExport.getExportId() + "/" + fileName;
		File returnValue = new File(nonbulkExportsOutputLocation + "/" + filePath);
		accountExport.setFileToWriteTo(returnValue);
		accountExport.setUrl(filePath);
		if (recreateDir) {
			FileUtils.deleteDirectory(new File(nonbulkExportsOutputLocation + "/" + accountExport.getExportId()));
			returnValue.getParentFile().mkdirs();
		}
		return returnValue;
	}



	private OutputStream getOutputStream(
			AccountExportValueObject accountExport, HttpServletResponse response)
			throws IOException {
		if (!accountExport.writeExportFile()) {
			setContentTypeAndDisposition(response, accountExport);
			return response.getOutputStream();
		}

		FileOutputStream fileOutputStream = new FileOutputStream(
				getNonbulkExportFile(accountExport));
		return fileOutputStream;
	}

	public static void setContentTypeAndDisposition(
			HttpServletResponse response, AccountExportValueObject accountExport) {
		setContentTypeAndDisposition(response, accountExport, null, false);
	}

	public static void setContentTypeAndDisposition(
			HttpServletResponse response,
			AccountExportValueObject accountExport, String fileName,
			boolean isIE8) {
		if (accountExport.getBulkExport()) {
			response.setContentType("application/x-gzip");
			response.setHeader("Content-Disposition", "filename=" + fileName);
			return;
		}

		// Set disposition
		if (!StringUtils.isNullOrEmtpy(fileName)) {
			response.setHeader("Content-Disposition", "filename=" + fileName);
		}

		String format = accountExport.getExportFormat();
		if (EXPORT_FORMAT_JSON.equals(format)
				|| EXPORT_FORMAT_PRINT.equals(format)) {
			response.setContentType("application/json");
			if (isIE8) {
				response.setHeader("Content-Disposition",
						"attachment; filename=" + fileName);
			}
		} else if (EXPORT_FORMAT_XML.equals(format)) {
			response.setContentType("application/xml");
		} else if (EXPORT_FORMAT_TEXT.equals(format)) {
			response.setContentType("text/plain");
		} else if (EXPORT_FORMAT_PDF.equals(format)) {
			response.setContentType("application/pdf");
			response.setHeader("Content-Disposition", "filename=OpaExport.pdf");
		} else if (EXPORT_FORMAT_CSV.equals(format)) {
			response.setContentType("text/csv");
			response.setHeader("Content-Disposition",
					"attachment; filename=" + (!StringUtils.isNullOrEmtpy(fileName) ? fileName : "OpaExport.csv"));

			if (isIE8) {
				response.setHeader("Content-Disposition",
						"attachment; filename=" + fileName);
			}
		}
		response.setCharacterEncoding("UTF-8");
	}

	private QueryResponse getDocsFromSearchEngine(
			AccountExportValueObject accountExport)
			throws InterruptedException, ExecutionException, TimeoutException {
		Map<String, String[]> queryParameters = accountExport
				.getQueryParameters();

		if (accountExport.getRows() != null) {
			queryParameters.put(
					CreateAccountExportRequestParameters.ROWS_HTTP_PARAM_NAME,
					new String[] { accountExport.getRows().toString() });
		}

		if (accountExport.getOffset() != null) {
			queryParameters
			.put(CreateAccountExportRequestParameters.OFFSET_HTTP_PARAM_NAME,
					new String[] { accountExport.getOffset().toString() });
		}

		// we want all result fields to be fetched - the filtering of fields
		// will be
		// done at a later stage
		String[] resultFields = queryParameters
				.get(CreateAccountExportRequestParameters.RESULT_FIELDS_HTTP_PARAM_NAME);

		if (resultFields != null) {
			if (accountExport.extractValuesNeeded()) {
				String[] allResultFields = new String[] { CreateAccountExportRequestParameters.ALL_RESULT_FIELDS };
				queryParameters
				.put(CreateAccountExportRequestParameters.RESULT_FIELDS_HTTP_PARAM_NAME,
						allResultFields);
				accountExport.setDefaultResultFieldsSet(true);
			} else {
                //TFS-82317-Refining-API-results-breaks-media-URLS
                String[] solrResultFields = new String[1];
                String oneField = resultFields[0];
                if (StringUtils.isNullOrEmtpy(oneField)) {
                    solrResultFields[0] = "type,level";
                } else if (oneField.equals("objects")) {
                    solrResultFields[0] = resultFields[0] + ",type,level,parentDescriptionNaId";
                } else {
                    solrResultFields[0] = resultFields[0] + ",type,level";
                }
				queryParameters
				.put(CreateAccountExportRequestParameters.RESULT_FIELDS_HTTP_PARAM_NAME,
						solrResultFields);
			}

		} else {
			String[] allResultFields = new String[] { CreateAccountExportRequestParameters.ALL_RESULT_FIELDS };
			if (!accountExport.getIncludeMetadata()) {
				allResultFields = new String[] { AccountExportValueObjectHelper
						.getResultFieldsForNoMetadata(accountExport) };
				accountExport.getQueryParameters().put("resultFields",
						allResultFields);
				accountExport.setDefaultResultFieldsSet(false);
			} else {
				accountExport.setDefaultResultFieldsSet(true);
			}
			queryParameters
			.put(CreateAccountExportRequestParameters.RESULT_FIELDS_HTTP_PARAM_NAME,
					allResultFields);

		}
		int searchTimeout = configurationService.getConfig().getSearchRunTime();
		QueryResponse response = null;
		if (accountExport.getCallerType() == CALLER_TYPE_SEARCH) {
			response = SolrUtils.doSearchWithTimeout(solrGateway,
					queryParameters, searchTimeout);
		} else {
			response = SolrUtils.doSearchWithTimeout(solrGateway,
					queryParameters, configurationService.getConfig().getMaxNonBulkTimer());
		}
		if (resultFields != null) {
			queryParameters
			.put(CreateAccountExportRequestParameters.RESULT_FIELDS_HTTP_PARAM_NAME,
					resultFields);

		}

		return response;

	}

	private void turnNonBulkExportIntoBulExport(
			AccountExportValueObject accountExport, Object writer,
			OutputStream outputStream) {
		try {
			if (outputStream != null) {
				outputStream.close();
			}
			// set it back to the correct format
			if (accountExport.getActualExportFormat() != null) {
				accountExport.setExportFormat(accountExport.getActualExportFormat());
			}
			File file = accountExport.getFileToWriteTo();
			AccountExportValueObjectHelper.closeWriter(accountExport, writer);
			file.delete();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (OPAAuthenticationProvider.getAccountIdForLoggedInUser() == null) {
			return;
		}
		accountExport.setBulkExport(true);
		accountExport.setTotalSkipped(0);
		accountExport.setTotalRecordsProcessed(0);
		accountExport.setRequestStatus(AccountExportStatusEnum.QUEUED);
		accountExportDaoDbProxy.update(accountExport);
	}
	public static void toFile(String data, File file) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(data);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (writer != null)
					writer.flush();
				writer.close();
			} catch (IOException e) {
			}
		}
	}

	private static String fileToString(File f) throws IOException {

		FileReader in = new FileReader(f);
		StringBuilder contents = new StringBuilder();
		char[] buffer = new char[4096];
		int read = 0;
		do {
			contents.append(buffer, 0, read);
			read = in.read(buffer);
		} while (read >= 0);
		return contents.toString();
	}
}
