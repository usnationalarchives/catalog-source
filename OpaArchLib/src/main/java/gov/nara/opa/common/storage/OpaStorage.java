package gov.nara.opa.common.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface OpaStorage {

	public static final int DEFAULT_BUFFER_SIZE = 10240;

	/**
	 * Gets a file
	 * 
	 * @param path Relative path or key of the file 
	 * @return A File specified by the path
	 */
	public File getFile(String path);
	
	/**
	 * Gets the content of the file or resource
	 * 
	 * @param key Relative path or key of the file you want to fetch data from
	 * @return A byte array containing the data from the specified file
	 * @throws IOException
	 */
	public byte[] getFileContent(String key) throws IOException;

	/**
	 * Gets the content length of a file or resource
	 * 
	 * @param key Relative path or key of the file
	 * @return Length of the content 
	 */
	public long getContentLength(String key);

	/**
	 * Gets the ETag of a file or resource
	 * 
	 * @param key Relative path or key of the file
	 * @return The ETag of a file
	 */
	public String getETag(String key);

	/**
	 * Saves a file into the destination path
	 * 
	 * @param src Source file
	 * @param destPath Destination path
	 */
	public void saveFile(File src, String destPath);

	/**
	 * Gets the full URL or path of the file
	 * 
	 * @param relativePath Relative path of the file
	 * @param naId Identifier
	 * @return Full path in the /live directory
	 */
	public String getFullPathInLive(String relativePath, Integer naId);

	/**
	 * Gets the full URL or path of the xml file
	 * 
	 * @param relativePath Relative path of the file
	 * @param naId Identifier
	 * @return Full path in the /full directory
	 */
	public String getFullPathInXmlStore(String relativePath, Integer naId);

	/**
	 * Check if a file or resource exists
	 * 
	 * @param path Path of the file
	 * @return True if the file/resource exists, false otherwise
	 */
	public boolean exists(String path);

	/**
	 * Gets the URL of a file or resource
	 * 
	 * @param key Relative path or key of the file
	 * @return The URL of the file or resource
	 */
	public String getURL(String key);

	/**
	 * If using CloudFront, it will get the URL of the file or resource 
	 * 
	 * @param key Relative path or key of the file
	 * @return The CloudFront URL of the path or resource specified by the key
	 */
	public String getCloudFrontURL(String key);

	/**
	 * Gets the export path of a file
	 * 
	 * @param exportBaseLocation The export base location 
	 * @param exportFilePath The path of the file or resource
	 * @return The full path of the file or resource
	 */
	public String getExportPath(String exportBaseLocation, String exportFilePath);

	/**
	 * Gets the files in a specific directory
	 * 
	 * @param key Relative path or key of the file
	 * @return List of the files in the specified directory
	 */
	public List<String> getFilesInDirectory(String key);

	/**
	 * Gets an InputStream of a file or resource, specified by the key 
	 * 
	 * @param key Relative path or key of the file
	 * @return The stream of the file or resource
	 */
	public InputStream getStream(String key);

	/**
	 * Gets a portion of a file or resource
	 * 
	 * @param key Relative path or key of the file
	 * @param start Start position of the portion 
	 * @param length Length of the portion
	 * @param output The output stream where the portion will be written 
	 * @throws FileNotFoundException 
	 * @throws IOException
	 */
	public void getFilePortionIntoOutput(String key, long start, long length,
			OutputStream output) throws FileNotFoundException, IOException;
	
	public void deleteFiles(String... keys) throws Exception;
}
