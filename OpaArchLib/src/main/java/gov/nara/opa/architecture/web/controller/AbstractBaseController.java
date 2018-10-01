package gov.nara.opa.architecture.web.controller;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.web.controller.aspirehelper.ErrorResponseObjectContentHolder;
import gov.nara.opa.architecture.web.controller.aspirehelper.SuccessResponseAspireObjectContentHolder;
import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.architecture.web.validation.ArchitectureErrorCodeConstants;
import gov.nara.opa.architecture.web.validation.SimpleRequestParameters;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.architecture.web.valueobject.AbstractWebEntityValueObject;
import gov.nara.opa.common.services.docstransforms.impl.normalize.AbstractXslValueExtractor;
import gov.nara.opa.common.storage.OpaStorage;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.AspireObject;

/**
 * @author aolaru
 * @date Jun 3, 2014
 * 
 *       The AbstractBaseController is the super class from which all Controller
 *       should extend. It provides convenience methods for constructing error
 *       and success responses These methods will also help with centralizing
 *       the dependency on an external component (the AspireObject)
 */

public class AbstractBaseController {
	public static String VERSION = "VERSION=1.2 23:10 ";
	static OpaLogger logger = OpaLogger.getLogger(AbstractBaseController.class);

	public static final String XML_HTTP_CONTENT_TYPE = "application/xml";
	public static final String JSON_HTTP_CONTENT_TYPE = "application/json";

	public static final String CHARSET_ENCODING = "; charset=utf-8";

	private static final long DEFAULT_EXPIRE_TIME = 604800000L; // ..ms = 1
																// week.

	public static boolean useS3Storage = false;

	@Value(value = "${useS3Storage}")
	public void setUseS3Storage(boolean useS3Storage) {
		AbstractBaseController.useS3Storage = useS3Storage;
	}

	@Value(value = "${export.xls.location}")
	public void setXSL_FILE_PATH(String XSL_FILE_PATH) {
		AbstractXslValueExtractor.XSL_FILE_PATH = XSL_FILE_PATH;
	}

	/**
	 * Constructs an error Response Entity from the supplied parameters
	 * 
	 * @param validationResult
	 *            The validation result return by validators
	 * @param requestPath
	 *            The request path for the request
	 * @param action
	 *            The action associate with the method controller
	 * @return The response entity containing the json/xml response body + http
	 *         status
	 */
	public static ResponseEntity<String> createErrorResponseEntity(ValidationResult validationResult,
			HttpServletRequest request, String action) {
		return createErrorResponseEntity(validationResult.getErrorMessage(), validationResult.getErrorCode(),
				validationResult.getValidatedRequest(), validationResult.getHttpStatus(), request, action);
	}

	/**
	 * Constructs an error Response Entity from the supplied parameters
	 * 
	 * @param errorMessage
	 *            The error message to be included in the response
	 * @param errorCode
	 *            The error code to be included in the response
	 * @param requestParameters
	 *            The Map or request values will be pulled from this parameter
	 *            and included in the response
	 * @param httpStatus
	 *            The returned http status
	 * @param requestPath
	 *            The request path for the request
	 * @param action
	 *            The action associate with the method controller
	 * @return The response entity containing the json/xml response body + http
	 *         status
	 */
	public static ResponseEntity<String> createErrorResponseEntity(String errorMessage, String errorCode,
			AbstractRequestParameters requestParameters, HttpStatus httpStatus, HttpServletRequest request,
			String action) {
		return createErrorResponseEntity(errorMessage, errorCode, requestParameters.getAspireObjectContent(action),
				httpStatus, request, action);
	}

	/**
	 * Constructs an error Response Entity from the supplied parameters
	 * 
	 * @param errorMessage
	 *            The error message to be included in the response
	 * @param errorCode
	 *            The error code to be included in the response
	 * @param requestParams
	 *            The map of request parameters and their values
	 * @param httpStatus
	 *            The returned http status
	 * @param requestPath
	 *            The request path for the request
	 * @param action
	 *            The action associate with the method controller
	 * @return The response entity containing the json/xml response body + http
	 *         status
	 */
	public static ResponseEntity<String> createErrorResponseEntity(String errorMessage, String errorCode,
			LinkedHashMap<String, Object> requestParametersMap, HttpStatus httpStatus, HttpServletRequest request,
			String action) {

		if (errorCode == null) {
			errorCode = ArchitectureErrorCodeConstants.UNKOWN_ERROR_CODE;
		}

		ErrorResponseObjectContentHolder responseObject = new ErrorResponseObjectContentHolder(httpStatus,
				getRequestPath(request), action, errorCode, errorMessage, requestParametersMap);

		AspireObject aspireObject = responseObject
				.createAspireObject(SuccessResponseAspireObjectContentHolder.OPA_RESPONSE, action, false);

		String message = getResponseOutputString(aspireObject, requestParametersMap);

		closeAspireObject(aspireObject);

		logger.trace("Opa Error Response:\n" + message);

		HttpHeaders headers = getContentTypeHeader(getFormatFromRequestParameters(requestParametersMap));

		ResponseEntity<String> entity = null;
		if (headers == null) {
			entity = new ResponseEntity<String>(message, httpStatus);
		} else {
			entity = new ResponseEntity<String>(message, headers, httpStatus);
		}
		return entity;
	}

	public static String getRequestPath(HttpServletRequest request) {
		return request.getServletPath();
	}

	/**
	 * Constructs a success Response Entity from the supplied parameters. For
	 * this overload the entityValueObject is supposed to be a simple object
	 * with no children (other ValueObjects) underneath it
	 * 
	 * @param entityName
	 *            The name of the business entity/object involved by this
	 *            request
	 * @param requestParameters
	 *            The requestParameters provides the map of parameter
	 *            names/values to be included in the response
	 * @param abstractValueObject
	 *            The entityValueObject provides the aspire content associate
	 *            with the response
	 * @param requestPath
	 *            The request path for the request
	 * @param action
	 *            The action associate with the method controller
	 * @return The response entity containing the json/xml response body + http
	 *         status
	 */
	public static ResponseEntity<String> createSuccessResponseEntity(String entityName,
			AbstractRequestParameters requestParameters, AbstractWebEntityValueObject entityValueObject,
			HttpServletRequest request, String action) {
		LinkedHashMap<String, Object> entityObjectsMap = new LinkedHashMap<String, Object>();
		entityObjectsMap.put(entityName, entityValueObject);
		return createSuccessResponseEntity(requestParameters, entityObjectsMap, request, action);
	}

	/**
	 * Constructs a success Response Entity from the supplied parameters. To be
	 * used with more complex value objects that have children underneath them.
	 * The names associated with these children in the aspire content map will
	 * supply the names for the top level entity names in the response
	 * 
	 * @param requestParameters
	 *            The requestParameters provides the map of parameter
	 *            names/values to be included in the response
	 * @param abstractValueObject
	 *            The entityValueObject provides the aspire content associate
	 *            with the response
	 * @param requestPath
	 *            The request path for the request
	 * @param action
	 *            The action associate with the method controller
	 * @return The response entity containing the json/xml response body + http
	 *         status
	 */
	public static ResponseEntity<String> createSuccessResponseEntity(AbstractRequestParameters requestParameters,
			AbstractWebEntityValueObject entityValueObject, HttpServletRequest request, String action) {
		return createSuccessResponseEntity(requestParameters, entityValueObject.getAspireObjectContent(action), request,
				action);

	}

	public static ResponseEntity<String> createSuccessResponseEntity(AbstractRequestParameters requestParameters,
			LinkedHashMap<String, Object> entityObjectsMap, HttpServletRequest request, String action) {
		return createSuccessResponseEntity(requestParameters, entityObjectsMap, getRequestPath(request), action);
	}

	public static ResponseEntity<String> createSuccessResponseEntity(AbstractRequestParameters requestParameters,
			LinkedHashMap<String, Object> entityObjectsMap, String requestPath, String action) {

		SuccessResponseAspireObjectContentHolder responseObject = new SuccessResponseAspireObjectContentHolder(
				HttpStatus.OK, requestPath, action, requestParameters.getAspireObjectContent(action), entityObjectsMap);

		AspireObject aspireObject = responseObject
				.createAspireObject(SuccessResponseAspireObjectContentHolder.OPA_RESPONSE, action, false);

		String message = getResponseOutputString(aspireObject, requestParameters.getAspireObjectContent(action));

		closeAspireObject(aspireObject);

		logger.trace("Opa Success Response:\n" + message);

		HttpHeaders headers = getContentTypeHeader(
				getFormatFromRequestParameters(requestParameters.getAspireObjectContent("")));

		ResponseEntity<String> entity = null;
		if (headers == null) {
			entity = new ResponseEntity<String>(message, HttpStatus.OK);
		} else {
			entity = new ResponseEntity<String>(message, headers, HttpStatus.OK);
		}

		return entity;
	}

	private static HttpHeaders getContentTypeHeader(String format) {
		HttpHeaders headers = new HttpHeaders();
		if (format == null) {
			return null;
		} else if (format.equals(AbstractRequestParameters.JSON_FORMAT)) {
			headers.add("Content-type", JSON_HTTP_CONTENT_TYPE + CHARSET_ENCODING);
			return headers;
		} else if (format.equals(AbstractRequestParameters.XML_FORMAT)) {
			headers.add("Content-type", XML_HTTP_CONTENT_TYPE + CHARSET_ENCODING);
			return headers;
		}
		return null;
	}

	private static void closeAspireObject(AspireObject aspireObject) {
		try {
			aspireObject.close();
		} catch (IOException e) {
			throw new OpaRuntimeException(e);
		}
	}

	private static String getResponseOutputString(AspireObject aspireObject,
			LinkedHashMap<String, Object> requestParametersMap) {

		Object prettyObject = requestParametersMap.get(AbstractRequestParameters.PARAM_NAME_PRETTY);
		boolean pretty = true;
		if (prettyObject != null) {
			pretty = ((Boolean) prettyObject).booleanValue();
		}

		String format = getFormatFromRequestParameters(requestParametersMap);

		try {
			if (format.equals(AbstractRequestParameters.JSON_FORMAT)) {
				return aspireObject.toJsonString(pretty);
			} else {
				return aspireObject.toXmlString(pretty);
			}
		} catch (AspireException e) {
			throw new OpaRuntimeException(e);
		}
	}

	private static String getFormatFromRequestParameters(LinkedHashMap<String, Object> requestParametersMap) {

		Object formatObject = requestParametersMap.get(AbstractRequestParameters.PARAM_NAME_FORMAT);
		String format = AbstractRequestParameters.JSON_FORMAT;
		if (formatObject != null) {
			format = (String) formatObject;
		}
		return format;
	}

	protected static boolean writeFileContentToResponseRanged(String filePath, HttpServletResponse response,
			HttpServletRequest request, OpaStorage storage) {
		try {
			long length = storage.getContentLength(filePath);
			// long lastModified = f.lastModified();
			long lastModified = System.currentTimeMillis();
			Range full = new Range(0, length - 1, length);
			List<Range> ranges = new ArrayList<Range>();
			long expires = System.currentTimeMillis() + DEFAULT_EXPIRE_TIME;

			String range = request.getHeader("Range");
			if (range != null) {
				// Range header should match format "bytes=n-n,n-n,n-n...". If
				// not, then
				// return 416.
				if (!range.matches("^bytes=\\d*-\\d*(,\\d*-\\d*)*$")) {
					response.setHeader("Content-Range", "bytes */" + length); // Required
					// in 416.
					response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
					return false;
				}

				// If-Range header should either match ETag or be greater then
				// LastModified. If not,
				// then return full file.
				String ifRange = request.getHeader("If-Range");
				if (ifRange != null && !ifRange.equals(filePath)) {
					try {
						long ifRangeTime = request.getDateHeader("If-Range"); // Throws
																				// IAE
						// if invalid.
						if (ifRangeTime != -1 && ifRangeTime + 1000 < lastModified) {
							ranges.add(full);
						}
					} catch (IllegalArgumentException ignore) {
						ranges.add(full);
					}
				}

				// If any valid If-Range header, then process each part of byte
				// range.
				if (ranges.isEmpty()) {
					for (String part : range.substring(6).split(",")) {
						// Assuming a file with length of 100, the following
						// examples
						// returns bytes at:
						// 50-80 (50 to 80), 40- (40 to length=100), -20
						// (length-20=80 to
						// length=100).
						long start = sublong(part, 0, part.indexOf("-"));
						long end = sublong(part, part.indexOf("-") + 1, part.length());

						if (start == -1) {
							start = length - end;
							end = length - 1;
						} else if (end == -1 || end > length - 1) {
							end = length - 1;
						}

						// Check if Range is syntactically valid. If not, then
						// return 416.
						if (start > end) {
							response.setHeader("Content-Range", "bytes */" + length); // Required
							// in
							// 416.
							response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
							return false;
						}

						// Add range.
						ranges.add(new Range(start, end, length));
					}
				}
			}

			if (useS3Storage) {
				URL url = new URL(filePath);
				setContentTypeHeader(url, filePath, response, false);
			} else {
				setContentTypeHeader(filePath, response, false);
			}

			response.setHeader("Accept-Ranges", "bytes");
			response.setHeader("ETag", storage.getETag(filePath));
			response.setDateHeader("Last-Modified", lastModified);
			response.setDateHeader("Expires", expires);

			OutputStream output = null;

			try {
				// Open streams.
				// input = new RandomAccessFile(filePath, "r");
				output = response.getOutputStream();

				if (ranges.isEmpty() || ranges.get(0) == full) {

					// Return full file.
					Range r = full;
					response.setHeader("Content-Range", "bytes " + r.start + "-" + r.end + "/" + r.total);
					response.setHeader("Content-Length", String.valueOf(r.length));

					if (true) {

						// Copy full range.
						// copy(input, output, r.start, r.length);
						storage.getFilePortionIntoOutput(filePath, r.start, r.length, output);
					}

				} else if (ranges.size() == 1) {

					// Return single part of file.
					Range r = ranges.get(0);
					response.setHeader("Content-Range", "bytes " + r.start + "-" + r.end + "/" + r.total);
					response.setHeader("Content-Length", String.valueOf(r.length));
					response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.

					if (true) {
						// Copy single part range.
						// copy(input, output, r.start, r.length);
						storage.getFilePortionIntoOutput(filePath, r.start, r.length, output);
					}

				}
			} finally {
				// Gently close streams.
				close(output);
			}

		} catch (Exception e) {
			throw new OpaRuntimeException(e);
		}

		return true;
	}

	@SuppressWarnings("resource")
	public static boolean writeFileContentToResponseOldStyle(String filePath, HttpServletResponse response,
			HttpServletRequest request, OpaStorage storage) throws IOException {
		InputStream is = null;
		OutputStream responseOutputStream = response.getOutputStream();
		byte[] buf = new byte[12000];
		if (!useS3Storage) {
			File f = new File(filePath);
			if (!f.exists()) {
				logger.error(String.format("Error fetching the response content.  File not found: %1$s", filePath));
				throw new OpaRuntimeException(String.format("File not found: %1$s", filePath));
			}

			logger.info(String.format("File size:%1$d", f.length()));
			response.setContentLength((int) f.length());
			response.setHeader("Accept-Ranges", "bytes");

			try {
				is = new FileInputStream(f);
			} catch (FileNotFoundException ex) {
				SimpleRequestParameters requestParameters = new SimpleRequestParameters(request);
				String errorMessage = String.format("Message for exception (%1$s): %2$s", ex.getClass().getName(),
						ex.getMessage());
				logger.error(errorMessage);
				ResponseEntity<String> responseEntity = AbstractBaseController.createErrorResponseEntity(errorMessage,
						ArchitectureErrorCodeConstants.FILE_NOT_FOUND, requestParameters, HttpStatus.NOT_FOUND, request,
						"GET_FILE");
				response.setStatus(responseEntity.getStatusCode().value());
				try {
					response.getOutputStream().print(responseEntity.getBody());
				} catch (IOException e) {
					logger.error("Error fetching the response content", e);
				}
				return false;
			}
		} else {
			try {

				try {
					// currently this tries to open S3 urls. That will always fail.
					File f = new File(filePath);
					if (f.exists()) {
						is = new FileInputStream(f);
					}
				} catch (Exception e) {
					logger.debug("writeFileContentToResponseOldStyle: file doesnt exist:," + " filePath=" + filePath);
					// First try to open file if it not exists then try the
					// other way
				}

				if (is == null) {
					is = storage.getStream(filePath);
					// is = new URL(filePath).openStream();
				}
			} catch (Exception ex) {
				SimpleRequestParameters requestParameters = new SimpleRequestParameters(request);
				String errorMessage = String.format("Message for exception (%1$s): %2$s", ex.getClass().getName(),
						ex.getMessage());
				logger.error(errorMessage);
				if (errorMessage.contains("403")) {
					logger.error(String.format("%s: %s", ArchitectureErrorCodeConstants.FILE_NOT_FOUND,
							ex.getMessage().substring(ex.getMessage().indexOf("URL:") + 4)));
				}
				ResponseEntity<String> responseEntity = AbstractBaseController.createErrorResponseEntity(errorMessage,
						ArchitectureErrorCodeConstants.FILE_NOT_FOUND, requestParameters, HttpStatus.NOT_FOUND, request,
						"GET_FILE");
				response.setStatus(responseEntity.getStatusCode().value());
				try {
					response.getOutputStream().print(responseEntity.getBody());
				} catch (IOException e) {
					logger.error("Error fetching the response content", e);
				}
				return false;
			}
		}

		int c = 0;
		while ((c = is.read(buf, 0, buf.length)) > 0) {
			responseOutputStream.write(buf, 0, c);
			responseOutputStream.flush();
		}
		is.close();

		logger.trace(String.format("Wrote file to output: %1$s", filePath));
		return true;
	}

// @formatter:off
	/**
	 * Sets the content type based on the mime type.
	 * 
	 * @param filePath-  The path of the file to be retrieved
	 * @param response-  The HttpServletResponse
	 * @throws IOException
	 */
// @formatter:on
	protected static void setContentTypeHeader(String filePath, HttpServletResponse response, boolean download)
			throws IOException {
		Path source = Paths.get(filePath);
		String mimeType = getMimeTypeForFile(filePath);
		String fileName = source.toFile().getName();
		setContentTypeHeaderWithMimeType(mimeType, filePath, response, download, fileName);
	}

	/**
	 * @param url
	 * @param filePath
	 * @param response
	 * @param download
	 * @throws IOException
	 */
	protected static void setContentTypeHeader(URL url, String filePath, HttpServletResponse response, boolean download)
			throws IOException {

		try {
			String mimeType = getMimeType(url);
			logger.debug("setContentTypeHeader: url=" + url.toExternalForm() + ", mimeType=" + mimeType);
			setContentTypeHeaderWithMimeType(mimeType, filePath, response, download, filePath);
		} catch (Exception ex) {
			String s = getStackTrace(ex);
			logger.error(
					"setContentTypeHeader2: exception caught printing stack trace: " + ex.getMessage() + " \n" + s);
			String errorMessage = String.format("Message for exception (%1$s): %2$s", ex.getClass().getName(),
					ex.getMessage());
			logger.error(errorMessage);
		}
		logger.debug("setContentTypeHeader3: COMPLETED url=" + url.toExternalForm() + ", filePath=" + filePath);
	}

	public static String getStackTrace(Exception ex) {
		StringWriter w = new StringWriter();
		PrintWriter pw = new PrintWriter(w);
		ex.printStackTrace(pw);
		return w.toString();
	}

	// @formatter:off
	/**
	 * 
	 * @param apath-a path to a file
	 * @return - the mime type (text, image/jpeg etc.)
	 * @throws IOException
	 *             -
	 * @formatter:on
	 */
	// @formatter:on
	public static String getMimeTypeForFile(String apath) throws IOException {
		File f = new File(apath);
		URL url = f.toURI().toURL();
		return getMimeType(url);
	}

	public static String getMimeType(URL aURL) throws IOException {
		Tika tika = new Tika();
		String mimeType = "text";
		URL newURL = aURL;
		String urlString = newURL.toExternalForm();
		// use a try catch to ignore a bad url or tika exception
		try {
			if (useS3Storage) {
				if (urlString.contains(" ")) {
					// TFS 84875: fixed space character handling problem in ingest.  The former code here
					// breaks the API/UI handling of legitimate space characters.  Will have to handle
					// the broken pre-84875 records on a case-by-case basis, or have the API handle it on
					// failure.

					urlString.replaceAll(" ", "%20");
					newURL = new URL(urlString);
				} 
			} 
			mimeType = tika.detect(newURL);
		} catch (Exception e) {
			logger.debug("getMimeType:  WARNING: failed to get mimeType for newURL: " + newURL + " Exception=" + e.getMessage());
			logger.debug("getMimeType4: failed to get mimeType. return default 'text'");

		}
		return mimeType;

	}

	/**
	 * @param mimeType
	 * @param filePath
	 * @param response
	 * @param download
	 * @param filename
	 */
	private static void setContentTypeHeaderWithMimeType(String mimeType, String filePath, HttpServletResponse response,
			boolean download, String filename) {
		if (mimeType == null && filePath.trim().endsWith(".dzi")) {
			mimeType = "application/xml";
		}
		if (mimeType == null && filePath.trim().endsWith(".gz")) {
			mimeType = "application/x-gzip";
		}
		if (mimeType != null) {
			response.setContentType(mimeType);
		}

		if (download) {
			String fName = filename;
			try {
				if (!useS3Storage) {
					File f = new File(filename);
					fName = f.getName();
				} else {
					URL url = new URL(filename);
					File f = new File(url.getPath());
					fName = f.getName();
				}
			} catch (Exception ex) {
			}

			response.setHeader("Content-Disposition", "attachment;filename=" + fName);
		}
	}

	/**
	 * Returns a substring of the given string value from the given begin index
	 * to the given end index as a long. If the substring is empty, then -1 will
	 * be returned
	 * 
	 * @param value
	 *            The string value to return a substring as long for.
	 * @param beginIndex
	 *            The begin index of the substring to be returned as long.
	 * @param endIndex
	 *            The end index of the substring to be returned as long.
	 * @return A substring of the given string value as long or -1 if substring
	 *         is empty.
	 */
	private static long sublong(String value, int beginIndex, int endIndex) {
		String substring = value.substring(beginIndex, endIndex);
		return (substring.length() > 0) ? Long.parseLong(substring) : -1;
	}

	/**
	 * Close the given resource.
	 * 
	 * @param resource
	 *            The resource to be closed.
	 */
	private static void close(Closeable resource) {
		if (resource != null) {
			try {
				resource.close();
			} catch (IOException ignore) {
				// Ignore IOException. If you want to handle this anyway, it
				// might be
				// useful to know
				// that this will generally only be thrown when the client
				// aborted the
				// request.
			}
		}
	}

}
