package gov.nara.opa.api.controller.content;

import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.validation.ValidationUtils;
import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.ApiTypeLoggingEnum;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.logging.usage.UsageLogCode;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.AbstractRequestParameters;
import gov.nara.opa.common.services.io.FileUtils;
import gov.nara.opa.common.storage.OpaStorageFactory;
import gov.nara.opa.common.storage.StorageUtils;

import java.net.URL;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller used for fetching the content of media files from the opastorage
 */
@Controller
@Lazy(value = false)
public class RenditionsFileRetrivalController extends AbstractBaseController {

	/**
	 * Properties file setting for the root location of media files. In
	 * production it would look something like this: /opastorage/live/
	 */
	@Value(value = "${opaStorage.baseLocation}")
	String opaStorageBaseLocation;

	@Value(value = "${useS3Storage}")
	boolean useS3Storage;

	@Autowired
	StorageUtils storageUtils;

	@Autowired
	OpaStorageFactory opaStorageFactory;

	/**
	 * Location of rendition files. In production would look something like
	 * this: opa-renditions/image-tiles
	 */

	static OpaLogger log = OpaLogger
			.getLogger(RenditionsFileRetrivalController.class);

	private static final String PUBLIC_API_PATH_PREFIX = "/"
			+ AbstractRequestParameters.PUBLIC_API_TYPE + "/"
			+ Constants.API_VERS_NUM;

	/**
	 * All requests that start with /media will be handled by this controlled.
	 * The first parameter after the media will be an naid followed at the end
	 * by the file name/path to be retrieved
	 * 
	 * @param request
	 *            HttpServletRequest - the file path and naid will be extracted
	 *            from it
	 * @param response
	 *            - The content of the file that is retrieved will be writted to
	 *            it
	 * @throws Exception
	 */
	@RequestMapping(value = { "/media/**", PUBLIC_API_PATH_PREFIX + "/media/**" }, method = RequestMethod.GET)
	public void getFile(
			HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "download", required = false, defaultValue = "false") boolean download)
			throws Exception {

		String path = request.getServletPath();
		if (path.startsWith(PUBLIC_API_PATH_PREFIX)) {
			path = path.substring(PUBLIC_API_PATH_PREFIX.length());
		}

		Integer naId = 0;
		try {
			naId = getNaraId(path);
		} catch (OpaRuntimeException e) {
			writeFileContentToResponseOldStyle(request.getServletPath(), response, request, opaStorageFactory.createOpaStorage());
			return;
		}

		// Validate allowed parameters
		String[] paramNamesStringArray = { "download" };
		LinkedHashMap<String, String> validRequestParameterNames = StringUtils
				.convertStringArrayToLinkedHashMap(paramNamesStringArray);
		if (!ValidationUtils.validateRequestParameterNames(
				validRequestParameterNames, request.getQueryString())) {
			throw new OpaRuntimeException("Invalid parameter");
		}

		String filePath = "";
		String renditionFilePath = path.substring(("/media/" + naId.toString())
				.length());

		if (!useS3Storage) {
			String expandedNaidPath = FileUtils.getExpandedNaidPath(naId);
			filePath = opaStorageBaseLocation + expandedNaidPath
					+ renditionFilePath;
			setContentTypeHeader(filePath, response, download);
		} else {
			filePath = storageUtils.getThumbnailUrl(
					renditionFilePath.substring(1), naId.toString());
			URL url = new URL(filePath);
			setContentTypeHeader(url, filePath, response, download);
		}

		String contentType = response.getContentType();

		if (contentType.contains("audio") || contentType.contains("video")) {
			if (!writeFileContentToResponseRanged(filePath, response, request,
					opaStorageFactory.createOpaStorage())) {
				return;
			}
		} else {
			if (!writeFileContentToResponseOldStyle(filePath, response, request, opaStorageFactory.createOpaStorage())) {
				return;
			}
		}

		// Log usage call
		// TODO: Obtain missing values
		String logMessage = "Action=%1$s,Source=%2$s,NaId=%3$d,ObjectId=%4$s";
		log.usage(getClass(),
				(path.contains("iapi") ? ApiTypeLoggingEnum.API_TYPE_INTERNAL
						: ApiTypeLoggingEnum.API_TYPE_PUBLIC),
				UsageLogCode.DEAFULT, String.format(logMessage,
						"ViewFullResults", "TBD", naId, ""));

		// response.getOutputStream().close();
	}

	/**
	 * Extract the NAID from the URL path
	 * 
	 * @param path
	 *            URL path
	 * @return The NAID
	 */
	private Integer getNaraId(String path) throws OpaRuntimeException {
		String[] pathTokens = path.split("/");
		if (pathTokens.length < 4) {
			throw new OpaRuntimeException(
					"Invalid path for accessing content files. The file path should follow the media/{naid}/{filePath} format");
		}
		Integer naid = null;
		try {
			naid = Integer.valueOf(pathTokens[2]);
		} catch (NumberFormatException ex) {
			log.error(ex.getMessage(), ex);
			throw new OpaRuntimeException(
					"The path token following /media/ could not be converted into an integer to represent the NAID");
		}
		return naid;
	}

	// leave it here in case we need it for performance reasons later

	// private void writeFileContentToResponse(String filePath,
	// HttpServletResponse response) throws IOException {
	// RandomAccessFile aFile = new RandomAccessFile(filePath, "r");
	// FileChannel inChannel = aFile.getChannel();
	// ByteBuffer buffer = ByteBuffer.allocate(1024);
	// while (inChannel.read(buffer) > 0) {
	// buffer.flip();
	// response.getOutputStream().write(buffer.array());
	// buffer.clear();
	// }
	// inChannel.close();
	// aFile.close();
	// }

}
