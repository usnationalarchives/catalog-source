package gov.nara.opa.common.storage;

import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.common.valueobject.export.AccountExportValueObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class StorageUtils {

	private static OpaLogger logger = OpaLogger.getLogger(StorageUtils.class);

	public static final String OBJECT_XML_FILE_NAME = "objects.xml";
	public static final String DESCRIPTION_XML_FILE_NAME = "description.xml";
	public static final String PDF_EXTRACTED_TEXT_PATH = "opa-renditions/extracted-text/";

	@Autowired
	OpaStorageFactory opaStorageFactory;

	@Value(value = "${s3ExportsLocation}")
	String s3ExportsLocation;

	@Value(value = "${useS3Storage}")
	boolean useS3Storage;

	@Value("${naraBaseUrl}")
	String naraBaseUrl;

	@Value("${tinyfyLocation}")
	private String tinyfyLocation;

	public String saveTinyfyImage(File image, String naId) {
		OpaStorage storage = opaStorageFactory.createOpaStorage();
		String path = null;
		if (storage instanceof S3OpaStorageImpl) {
			try {
				String finalPath = String.format("%s%s-%s", tinyfyLocation, naId,
						image.toPath().getFileName());
				path = FilenameUtils.separatorsToUnix(finalPath.toString());
				storage.saveFile(image,  path);
				Path dirPath = image.toPath().getParent();
				File directory = new File(image.toPath().getParent().toString());
				FileUtils.cleanDirectory(directory);
				Files.deleteIfExists(dirPath);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}

		return path;
	}

	/**
	 * Provides the final URL address to be used by exports. It determines if
	 * the destination is either a regular URL or an S3 link.
	 * 
	 * @param accountExport
	 * @param url
	 * @param exportsLocation
	 * @return
	 */
	public String getFinalUrl(AccountExportValueObject accountExport,
			String url, String exportsLocation) {
		OpaStorage storage = opaStorageFactory.createOpaStorage();
		String finalUrl = url;
		if (storage instanceof S3OpaStorageImpl) {
			File file = new File(finalUrl);
			Path exportLocationPath = Paths.get(exportsLocation);
			Path s3location = Paths.get(s3ExportsLocation);
			Path filePath = Paths.get(file.getAbsolutePath());
			Path relativePath = filePath.subpath(
					exportLocationPath.getNameCount(), filePath.getNameCount());
			Path finalPath = Paths.get(s3location.toString(),
					relativePath.toString());
			String path = FilenameUtils.separatorsToUnix(finalPath.toString());
			storage.saveFile(file, path);

			try {
				Path dirPath = filePath.getParent();
				File directory = new File(filePath.getParent().toString());
				FileUtils.cleanDirectory(directory);
				Files.deleteIfExists(dirPath);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}

		return finalUrl;
	}

	/**
	 * Gets the final URL for the thumbnails. File system or S3 implementation
	 * 
	 * @param partialThumbnailPath
	 *            The partial path of the thumbnail
	 * @param naId
	 * @return
	 */
	public String getThumbnailUrl(String partialThumbnailPath, String naId) {
		logger.debug("partialThumbnailPath: "+partialThumbnailPath);
		logger.debug("naId: "+naId);
		String url = "";
		if (partialThumbnailPath != null && !partialThumbnailPath.isEmpty()
				&& naId != null && !naId.isEmpty()) {
			if (!useS3Storage) {
				url = naraBaseUrl + "OpaAPI/media/" + naId + "/"
						+ partialThumbnailPath;
			} else {
				OpaStorage storage = opaStorageFactory.createOpaStorage();
				if (storage != null) {
					url = storage.getURL(storage.getFullPathInLive(
							partialThumbnailPath, Integer.valueOf(naId)));
				}
			}
		}
		logger.debug("result url: "+url);

		return url;
	}

	/**
	 * Gets the path or link of an export
	 * 
	 * @param exportBaseLocation
	 *            The export base location
	 * @param exportFilePath
	 *            The path of the export
	 * @return The full path of the export file
	 */
	public String getExportPath(String exportBaseLocation, String exportFilePath) {
		OpaStorage storage = opaStorageFactory.createOpaStorage();
		return storage.getExportPath(exportBaseLocation, exportFilePath);
	}
	
	
}
