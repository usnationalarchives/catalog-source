package gov.nara.opa.common.storage;

import gov.nara.opa.architecture.exception.OpaRuntimeException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FileSystemOpaStorageImpl implements OpaStorage {
	
	@Value(value = "${opaStorage.baseLocation}")
	private String opaStorageBaseLocation;

	@Value(value = "${xmlStore.baseLocation}")
	private String xmlStoreBaseLocation;

	private Path baseDir = null;

	/**
	 * Gets the base directory
	 * 
	 * @return The base directory
	 */
	public Path getBaseDir() {
		if (baseDir == null) {
			baseDir = Paths.get(opaStorageBaseLocation);
		}
		return baseDir;
	}

	/**
	 * Sets the base directory
	 * 
	 * @param baseDir The base directory
	 */
	public void setBaseDir(Path baseDir) {
		this.baseDir = baseDir;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nara.opa.common.storage.OpaStorage#getFile(java.lang.String)
	 */
	@Override
	public File getFile(String path) {
		return new File(path);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.nara.opa.common.storage.OpaStorage#getFileContent(java.lang.String)
	 */
	@Override
	public byte[] getFileContent(String path) throws IOException {
		byte[] data = Files.readAllBytes(Paths.get(path));
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nara.opa.common.storage.OpaStorage#saveFile(java.io.File,
	 * java.lang.String)
	 */
	@Override
	public void saveFile(File src, String destPath) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.nara.opa.common.storage.OpaStorage#getFullPathInLive(java.lang.String
	 * , java.lang.Integer)
	 */
	@Override
	public String getFullPathInLive(String path, Integer naId) {
		return String.format("%s/%s/%s", getBaseDir(),
				gov.nara.opa.common.services.io.FileUtils
						.getExpandedNaidPath(naId), path);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nara.opa.common.storage.OpaStorage#exists(java.lang.String)
	 */
	@Override
	public boolean exists(String path) {
		return getFile(path).exists();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nara.opa.common.storage.OpaStorage#getURL(java.lang.String)
	 */
	@Override
	public String getURL(String key) {
		return String.format("%s/%s", getBaseDir(), key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.nara.opa.common.storage.OpaStorage#getCloudFrontURL(java.lang.String)
	 */
	@Override
	public String getCloudFrontURL(String key) {
		return getURL(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.nara.opa.common.storage.OpaStorage#getExportPath(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public String getExportPath(String exportBaseLocation, String exportFilePath) {
		return exportBaseLocation + "/" + exportFilePath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.nara.opa.common.storage.OpaStorage#getFilesInDirectory(java.lang.
	 * String)
	 */
	@Override
	public List<String> getFilesInDirectory(String key) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.nara.opa.common.storage.OpaStorage#getContentLength(java.lang.String)
	 */
	@Override
	public long getContentLength(String key) {
		File f = new File(key);

		if (!f.exists())
			throw new OpaRuntimeException(
					String.format("File not found: ", key));

		return f.length();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nara.opa.common.storage.OpaStorage#getETag(java.lang.String)
	 */
	@Override
	public String getETag(String key) {
		return key;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nara.opa.common.storage.OpaStorage#getStream(java.lang.String)
	 */
	@Override
	public InputStream getStream(String key) {
		InputStream is = null;
		File f = new File(key);
		if (f.exists()) {
			try {
				is = new FileInputStream(f);
			} catch (FileNotFoundException e) {
				throw new OpaRuntimeException(String.format("File not found: ",
						key), e);
			}
		}

		return is;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.nara.opa.common.storage.OpaStorage#getFilePortionIntoOutput(java.
	 * lang.String, long, long, java.io.OutputStream)
	 */
	@Override
	public void getFilePortionIntoOutput(String key, long start, long length,
			OutputStream output) throws FileNotFoundException, IOException {
		RandomAccessFile input = new RandomAccessFile(key, "r");
		copy(input, output, start, length);
		input.close();
	}

	/**
	 * Copy the given byte range of the given input to the given output.
	 * 
	 * @param input
	 *            The input to copy the given range to the given output for.
	 * @param output
	 *            The output to copy the given range from the given input for.
	 * @param start
	 *            Start of the byte range.
	 * @param length
	 *            Length of the byte range.
	 * @throws IOException
	 *             If something fails at I/O level.
	 */
	private void copy(RandomAccessFile input, OutputStream output, long start,
			long length) throws IOException {
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		int read;

		if (input.length() == length) {
			// Write full range.
			while ((read = input.read(buffer)) > 0) {
				output.write(buffer, 0, read);
			}
		} else {
			// Write partial range.
			input.seek(start);
			long toRead = length;

			while ((read = input.read(buffer)) > 0) {
				if ((toRead -= read) > 0) {
					output.write(buffer, 0, read);
				} else {
					output.write(buffer, 0, (int) toRead + read);
					break;
				}
			}
		}
	}

	public String getFullPathInXmlStore(String path, Integer naId) {
		return String.format("%s/%s/%s", xmlStoreBaseLocation,
				gov.nara.opa.common.services.io.FileUtils
						.getExpandedNaidPath(naId), path);
	}

	@Override
	public void deleteFiles(String... keys) throws Exception {
		for(String key : keys) {
			File file = new File(key);
			if(file != null) {
				file.delete();
			}
		}
		
	}

}
