package gov.nara.opa.api.services.content.impl;

import gov.nara.opa.api.services.content.ImageTileRetrievalService;
import gov.nara.opa.api.system.ErrorConstants;
import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.architecture.utils.ObjectsXmlUtils;
import gov.nara.opa.architecture.utils.StringUtils;
import gov.nara.opa.common.services.io.FileUtils;
import gov.nara.opa.common.storage.OpaStorage;
import gov.nara.opa.common.storage.OpaStorageFactory;
import gov.nara.opa.common.storage.StorageUtils;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ImageTileRetrievalServiceImpl implements ImageTileRetrievalService {

	private static OpaLogger logger = OpaLogger
			.getLogger(ImageTileRetrievalServiceImpl.class);

	/**
	 * Properties file setting for the root location of media files. In
	 * production it would look something like this: /opastorage/live/
	 */
	@Value(value = "${opaStorage.baseLocation}")
	private String opaStorageBaseLocation;

	@Value(value = "${export.nonbulk.output.location}")
	private String exportOutputFolder;

	@Autowired
	OpaStorageFactory opaStorageFactory;

	@Override
	public String getImageTilesFilePath(String naId, String objectId) {

		String outputPath = addEndingSlash(exportOutputFolder);
		OpaStorage storage = opaStorageFactory.createOpaStorage();
		String basePath = storage.getFullPathInLive("", Integer.parseInt(naId));
		String objectXmlPath = basePath + StorageUtils.OBJECT_XML_FILE_NAME;

		if (storage.exists(objectXmlPath)) {

			boolean isValid = true;

			logger.info("Reading objects.xml");

			String objectsXmlContents = "";
			try {
				byte[] bytes = storage.getFileContent(objectXmlPath);
				objectsXmlContents = new String(bytes, "UTF-8");
			} catch (IOException e) {
				e.printStackTrace();
			}

			logger.info("Getting tiles file path");
			String tileImagesFileRelativePath = ObjectsXmlUtils
					.getObjectFileImageTilesPath(objectsXmlContents, objectId);

			String tileImagesFilePath = null;
			if (!StringUtils.isNullOrEmtpy(tileImagesFileRelativePath)) {
				tileImagesFilePath = basePath + tileImagesFileRelativePath;
				logger.info(String.format("Tiles file is: %1$s",
						tileImagesFilePath));
			} else {
				logger.error(String.format(
						"Image tiles path doesn't exist for objectId: %1$s",
						objectId));
				throw new OpaRuntimeException(String.format(
						"Image tiles path doesn't exist for objectId: %1$s",
						objectId));
			}

			String tileImagesZipFile = null;
			if (isValid && storage.exists(tileImagesFilePath)) {
				String tileImagesFolderPath = basePath
						+ tileImagesFileRelativePath.replace(".dzi", "_files");

				tileImagesZipFile = outputPath
						+ tileImagesFileRelativePath
								.substring(
										tileImagesFileRelativePath
												.lastIndexOf("/") + 1,
										tileImagesFileRelativePath.length())
								.replace(".jpg.dzi", ".tar.gz");

				// Create tar file
				logger.info(String.format("Compressing file: %1$s",
						tileImagesZipFile));

				FileUtils.createTarGzFile(tileImagesFolderPath,
						tileImagesZipFile, storage);
			} else {
				logger.error(String.format("File doesn't exist: %1$s",
						tileImagesFilePath));
				throw new OpaRuntimeException(String.format(
						ErrorConstants.FILE_NOT_FOUND, tileImagesFilePath));
			}

			// Return zipped file
			File zipedFile = new File(tileImagesZipFile);
			if (isValid) {
				if (zipedFile.exists()) {
					return tileImagesZipFile;
				} else {
					logger.error(String.format("File doesn't exist: %1$s",
							tileImagesZipFile));
					throw new OpaRuntimeException(String.format(
							ErrorConstants.FILE_NOT_FOUND, tileImagesZipFile));
				}
			} else {
				throw new OpaRuntimeException(
						ErrorConstants.INVALID_OBJECTS_FILE_FORMAT);
			}

		} else {
			throw new OpaRuntimeException(String.format(
					ErrorConstants.FILE_NOT_FOUND, basePath + "/"
							+ StorageUtils.OBJECT_XML_FILE_NAME));
		}
	}

	@Override
	public void deleteCompressedFile(String filePath) {
		File fileToDelete = new File(filePath);
		if (fileToDelete.exists()) {
			fileToDelete.delete();
		}
	}

	private String addEndingSlash(String directory) {
		return (directory.endsWith("/") ? directory : directory + "/");
	}
}
