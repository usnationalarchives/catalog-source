package gov.nara.opa.api.controller.content;

import gov.nara.opa.api.services.content.ImageTileRetrievalService;
import gov.nara.opa.api.system.Constants;
import gov.nara.opa.api.validation.content.ImageTilesRetrievalRequestParameters;
import gov.nara.opa.api.validation.content.ImageTilesRetrievalValidator;
import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.ApiTypeLoggingEnum;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.logging.usage.UsageLogCode;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.architecture.web.controller.AbstractBaseController;
import gov.nara.opa.architecture.web.validation.ValidationResult;
import gov.nara.opa.common.storage.OpaStorageFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class ImageTilesRetrievalController extends AbstractBaseController {

	private static OpaLogger log = OpaLogger
			.getLogger(ImageTilesRetrievalController.class);
	// private static final String TILE_RETRIEVAL_ACTION = "tilesRetrieval";

	/**
	 * Location of rendition files. In production would look something like
	 * this: opa-renditions/image-tiles
	 */

	@Autowired
	private ImageTilesRetrievalValidator validator;

	@Autowired
	private ImageTileRetrievalService retrievalService;
	
	@Autowired
	OpaStorageFactory opaStorageFactory;

	@RequestMapping(value = { "/" + Constants.PUBLIC_API_PATH + "/"
			+ Constants.API_VERS_NUM
			+ "/id/{naId}/objects/{objectId:.+}/image-tiles" }, method = RequestMethod.GET)
	public void getTiles(
			@Valid ImageTilesRetrievalRequestParameters requestParameters,
			BindingResult bindingResult, HttpServletRequest request,
			HttpServletResponse response) {

		// Validate request
		ValidationResult validationResult = validator.validate(bindingResult,
				request);
		if (!validationResult.isValid()) {
			throw new OpaRuntimeException("Invalid image tile request");
		}

		requestParameters.setApiType(Constants.PUBLIC_API_PATH);

		// Call retrieval service
		log.info("Getting image tiles file path");
		String filePath = retrievalService.getImageTilesFilePath(
				requestParameters.getNaId(), requestParameters.getObjectId());
		if (StringUtils.isNullOrEmtpy(filePath)) {
			throw new OpaRuntimeException("Invalid file information");
		}

		// Output file
		try {
			// setContentTypeHeader(filePath, response, true);
			response.setContentType("application/x-gzip");
			Path source = Paths.get(filePath);
			String fileName = source.toFile().getName();
			response.setHeader("Content-Disposition", "attachment;filename="
					+ fileName);

			// Log usage call
			// TODO: Obtain missing values
			String logMessage = "Action=%1$s,Source=%2$s,NaId=%3$s,ObjectId=%4$s";
			log.usage(getClass(), (requestParameters.getApiType()
					.equals("iapi") ? ApiTypeLoggingEnum.API_TYPE_INTERNAL
					: ApiTypeLoggingEnum.API_TYPE_PUBLIC),
					UsageLogCode.DEAFULT, String.format(logMessage,
							"ViewFullResults", "TBD",
							requestParameters.getNaId(), ""));

			log.info("Writing file to response");

			boolean writeResult = writeFileContentToResponseOldStyle(filePath,
					response, request, opaStorageFactory.createOpaStorage());

			if (!writeResult) {
				return;
			} else {
				log.info("Deleting compressed file");
				retrievalService.deleteCompressedFile(filePath);
			}

			// Delete file
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new OpaRuntimeException(e);
		}
	}
}
