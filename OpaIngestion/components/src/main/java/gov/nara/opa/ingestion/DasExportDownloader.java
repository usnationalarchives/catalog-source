/**
 * Copyright Search Technologies 2013
 */
package gov.nara.opa.ingestion;

import com.searchtechnologies.aspire.services.AspireException;
import com.searchtechnologies.aspire.services.Component;
import com.searchtechnologies.aspire.services.logging.ALogger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * Downloads the tar.gz file that PPC provides
 */
public class DasExportDownloader {

	private final ALogger logger;

	/**
	 * Non-default Constructor
	 * 
	 * @param component
	 * @throws AspireException
	 * @throws IOException
	 * @throws ParseException
	 */
	public DasExportDownloader(Component component) throws AspireException,
			IOException, ParseException {

		this.logger = (ALogger) component;

	}

	/**
	 * Main method of this class that will trigger the download of the latest
	 * file from PPC and delete expired files after that
	 * 
	 * @param sourceUrl
	 *            URL of the PPC repository
	 * @param folderPath
	 *            Path where the xmlstorage folder is located
	 * @param daysToExpire
	 *            Number of days that determine when a file has expired
	 * @throws IOException
	 * @throws ParseException
	 * @throws AspireException
	 */
	public String startProcess(String sourceUrl, String folderPath,
			int daysToExpire) throws IOException, ParseException,
			AspireException {

        Path destinationDir = Paths.get(folderPath);
        Files.createDirectories(destinationDir);

        // Create the connection to the URL where the files are stored
		java.io.BufferedReader in = createConnection(sourceUrl);

		// Get the latest file provided using the date on the filename
		String latestFileName = getLatestFileName(in);

		// Triggers the latest file download, return true if file was downloaded
		boolean foundNew = downloadFile(sourceUrl, folderPath, latestFileName);

		// Delete expired files
		deleteExpiredFiles(new File(folderPath), latestFileName, daysToExpire);

		if (foundNew)
			return latestFileName;
		else
			return "";
	}

	/**
	 * Triggers the latest file download
	 * 
	 * @param sourceURL
	 *            URL where PPC provides the tar.gz file
	 * @param folderPath
	 *            Path where we will save the download file
	 * @throws IOException
	 * @throws ParseException
	 * @throws AspireException
	 */
	public boolean downloadFile(String sourceUrl, String folderPath,
			String latestFileName) throws IOException, ParseException,
			AspireException {

		File filePathInLiveDir = new File(folderPath + latestFileName);

		// Download the file to our xmlstorage folder.
		if (!filePathInLiveDir.exists()) {
                    File tempFile = OpaFileUtils.getTempFile(UUID.randomUUID() + latestFileName);
			downloadFileFromURL(sourceUrl + latestFileName, tempFile);
                        FileUtils.moveFile(tempFile, filePathInLiveDir);
			logger.info("File downloaded successfully at " + new Date());
			return true;
		} else {
			logger.info("Latest file already exists on the xmlstorage folder.");
			return false;
		}
	}

	/**
	 * Delete expired files with date older than the expiration days setting
	 * configured on the DXF)
	 * 
	 * @param filePath
	 *            xmlstorage folder where the tar.gz files are stored
	 * @param latestFileName
	 *            the latest file name that was downloaded
	 * @param daysToExpire
	 *            Number of days to consider a file expired
	 * @throws AspireException
	 */
	public void deleteExpiredFiles(File filePath, String latestFileName,
			int daysToExpire) throws AspireException {

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, -daysToExpire);

		try {
			// Look for expired files on the xmlstorage folder
			for (String s : filePath.list()) {
				File currentFile = new File(filePath, s);
				// Validate if the current file on the loop has to be deleted
				if (currentFile.getName() != latestFileName
						&& cal.getTime().after(
								new Date(currentFile.lastModified()))
						&& (currentFile.getName().contains("dasexport_") && currentFile
								.getName().contains(".tar.gz"))) {
					logger.info("Delete file: %s ", currentFile.getName());
					currentFile.delete();
				}
			}
		} catch (Exception ex) {
			logger.error(ex, "Error deleting file: %s , Exception Message: %s",
					filePath.getAbsoluteFile(), ex.getMessage());
			throw new AspireException("Files.move", ex);
		}
	}

	/**
	 * Create the connection to the URL where the files are stored
	 * 
	 * @param sourceURL
	 *            URL where PPC provides the tar.gz file
	 * @return BufferedReader containing the web-page to extract the filenames
	 * @throws IOException
	 */
	public BufferedReader createConnection(String sourceURL) throws IOException {

		URL url = new URL(sourceURL);
		java.net.URLConnection con = url.openConnection();
		con.connect();
		return new java.io.BufferedReader(new java.io.InputStreamReader(
				con.getInputStream()));

	}

	/**
	 * Get the latest file provided using the date on the filename
	 * 
	 * @param in
	 *            BufferedReader containing the web-page to extract the
	 *            filenames
	 * @return String containing the most recent filename
	 * @throws IOException
	 * @throws ParseException
	 */
	private String getLatestFileName(java.io.BufferedReader in)
			throws IOException, ParseException {

		String latestFileName = "";

		// Loop through the URL lines to get the latest file
		for (String line; (line = in.readLine()) != null;) {
			String fileName = "";
			if (line.contains("dasexport_") && line.contains(".tar.gz")) {
				// extract the name of the file from the current line
				fileName = StringUtils.substringBetween(line, "href=\"",
						"\">dasexport");

				latestFileName = chooseLatestFileName(latestFileName, fileName);
			}
		}

		return latestFileName;
	}

	/**
	 * Compares the filenames to get the latest file
	 * 
	 * @param latestFileName
	 *            Latest filename found until now
	 * @param fileName
	 *            Current filename on the loop
	 * @return String with the name of the most recent filename
	 * @throws ParseException
	 */
	public String chooseLatestFileName(String latestFileName, String fileName)
			throws ParseException {

		// If it is the first file return it as the latest
		if (latestFileName.equals("")) {
			return fileName;
		} else {
			// Compare against the latest file that we have found
			Date latestFileNameDate = extractDateFromFileName(latestFileName);
			Date currentLatestFileName = extractDateFromFileName(fileName);

			if (latestFileNameDate.after(currentLatestFileName)) {
				return latestFileName;
			} else {
				return fileName;
			}
		}
	}

	/**
	 * Extract the date substring from the file name
	 * 
	 * @param fileName
	 *            Name of the file that contains the date on its name
	 * @return Date object with the date extracted from the filename
	 * @throws ParseException
	 */
	public Date extractDateFromFileName(String fileName) throws ParseException {

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy");

		return simpleDateFormat.parse(StringUtils.substringBetween(fileName,
				"dasexport_", ".tar.gz"));

	}

	/**
	 * Copy the File content to the incremental folder
	 * 
	 * @param latestFileName
	 *            URL of the latest file found
	 * @param filePath
	 *            PAth where we will save the file
	 * @throws AspireException
	 */
	private void downloadFileFromURL(String latestFileName,
			File filePathInLiveDir) throws AspireException {
		try {
			URL sourceUrl = new URL(latestFileName);
			FileUtils.copyURLToFile(sourceUrl, filePathInLiveDir, 10000, 10000);
		} catch (IOException ex) {
			throw new AspireException("FileUtils.copyURLToFile", ex,
					"latestFileName: %s. Failed to download url %s to file %s",
					latestFileName, latestFileName, latestFileName);
		}
	}

}
