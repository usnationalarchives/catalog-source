package gov.nara.opa.common.services.io;

import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.architecture.logging.OpaLogger;
import gov.nara.opa.common.storage.OpaStorage;
import gov.nara.opa.common.storage.S3OpaStorageImpl;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

public class FileUtils {

	static OpaLogger logger = OpaLogger.getLogger(FileUtils.class);

	public static String getExpandedNaidPath(String naId) {
		return getExpandedNaidPath(Integer.valueOf(naId));
	}

	/**
	 * Takes in the NAID and it expands it to create the subfolders structure.
	 * For example, a NAID of 1234567 will be transformed into 67/2345/1234567
	 * 
	 * @param naid
	 *            The NAID
	 * @return THe expanded path
	 * 
	 */
	public static String getExpandedNaidPath(Integer naid) {
		int firstDirectoryInt = naid % 100;
		int tempNaid = naid - firstDirectoryInt;
		int secondDirectoryInt = (tempNaid % 1000000) / 100;
		return firstDirectoryInt + "/" + secondDirectoryInt + "/" + naid;
	}

	/**
	 * Creates a tar.gz file
	 * 
	 * @param source
	 *            - The path of the directory that needs to be zipped.
	 * @param target
	 *            - The path to the zip file to be created. It is expected the
	 *            file name will end in .tar.gz extension. To execute this on
	 *            Windows the 7-zip utility needs to be installed and the steps
	 *            for setting it up, from the Export Server Setup dev guide,
	 *            need to be followed
	 */
	public static void createTarGzFile(String source, String target,
			OpaStorage storage) {
		if (storage != null && storage instanceof S3OpaStorageImpl) {
			try {
				createArchiveFileUsingS3(source, target, storage);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			if (System.getProperty("os.name").startsWith("Windows")) {
				createWindowsArchiveFile(source, target);
			} else {
				createLinuxArchiveFile(source, target);
			}
		}
	}

	private static void createWindowsArchiveFile(String source, String target) {
		String tarBallFileName = target.substring(0, target.length() - 3);
		String tarCommand = String.format("7z a -ttar %1$s %2$s",
				tarBallFileName, source);
		String gzipCommand = String.format("7z a -tgzip %1$s %2$s", target,
				tarBallFileName);
		
		
		
		try {
			java.lang.ProcessBuilder pb = new java.lang.ProcessBuilder(tarCommand);
			java.lang.Process p = pb.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			while ((reader.readLine()) != null) {
			}
			p.waitFor();
			p = new java.lang.ProcessBuilder(gzipCommand).start();
			p.waitFor();
		} catch (IOException | InterruptedException e) {
			throw new OpaRuntimeException(e);
		}
		new File(tarBallFileName).delete();

	}

	private static void createLinuxArchiveFile(String source, String target) {

		String tarGzipCommand = String.format("tar -zcvf %1$s -C %2$s .",
				target, source);

		logger.debug(String.format("Creating gzip with this command %1$s",
				tarGzipCommand));
		
		
		try {
			java.lang.ProcessBuilder pb = new java.lang.ProcessBuilder(tarGzipCommand);
			java.lang.Process p = pb.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			while ((reader.readLine()) != null) {
			}
			p.waitFor();
		} catch (IOException | InterruptedException e) {
			throw new OpaRuntimeException(e);
		}

	}

	private static void createArchiveFileUsingS3(String source, String target,
			OpaStorage storage) throws IOException {
		List<String> files = storage.getFilesInDirectory(source);
		File finalArchive = new File(target);
		FileOutputStream gzOs = new FileOutputStream(finalArchive);
		OutputStream bufferedGzOs = new BufferedOutputStream(gzOs);
		GZIPOutputStream gZipOs = new GZIPOutputStream(bufferedGzOs);
		TarArchiveOutputStream tarGzFile = new TarArchiveOutputStream(gZipOs);
		// TarArchiveOutputStream tarGzFile = (TarArchiveOutputStream)
		// getLargeObjectFromExecutionContext(FINAL_TAR_GZ_FILE);

		logger.debug("Starting to insert files into tarball");
		for (String file : files) {
			String fileName = file.replace(source, "");
			logger.debug(String.format("Writing file %1$s to tar.gz", fileName));
			try {
				TarArchiveEntry entry = new TarArchiveEntry(file);
				byte[] bytes = storage.getFileContent(file);
				entry.setSize(bytes.length);
				entry.setName(fileName);
				tarGzFile.putArchiveEntry(entry);
				tarGzFile.write(bytes);
				tarGzFile.closeArchiveEntry();
			} catch (IOException e) {
				logger.error(String.format(
						"Cannot add file %1$s to tar.gz file. Reason: %2$s",
						fileName, e));
			}
		}

		try {
			tarGzFile.finish();
			tarGzFile.close();
		} catch (IOException e) {
			logger.error(String.format("Cannot close file %1$s. Reason %2$s",
					finalArchive.getName(), e));
		}

	}
}
