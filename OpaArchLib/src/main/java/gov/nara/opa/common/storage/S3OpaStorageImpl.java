package gov.nara.opa.common.storage;

import com.amazonaws.services.s3.model.*;
import gov.nara.opa.architecture.exception.OpaRuntimeException;
import gov.nara.opa.common.services.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.AmazonClientException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.google.common.io.ByteStreams;

@Component
public class S3OpaStorageImpl implements OpaStorage {

	@Value(value = "${s3StorageAccessKeyId}")
	private String s3StorageAccessKeyId;

	@Value(value = "${s3StorageSecretKey}")
	private String s3StorageSecretKey;

	@Value(value = "${s3StorageBucketName}")
	private String s3StorageBucketName;

	@Value(value = "${s3ExportsLocation}")
	private String s3ExportsLocation;

	@Value(value = "${useCloudFront}")
	private boolean useCloudFront;

	@Value(value = "${cloudFrontDomainName}")
	private String cloudFrontDomainName;
	
	@Value(value = "${amazonS3.maxConnections}")
	private int maxConnections;
	
	@Value(value = "${amazonS3.connectionTimeout}")
	private int connectionTimeout;

	private static final String OPA_STORAGE = "opastorage";
	private static final String LIVE = "live";

	private static final String XML_STORE = "xmlstore";
	private static final String FULL = "full";

	private TransferManager transferManager = null;
	private AmazonS3 amazonS3Client = null;

	/**
	 * Gets the TransferManager used to download or upload files to S3
	 * 
	 * @return A TransferManager based on the credentials specified in the
	 *         application.properties
	 */
	private TransferManager getTransferManager() {
		if (transferManager == null) {
			BasicAWSCredentials credentials = new BasicAWSCredentials(s3StorageAccessKeyId, s3StorageSecretKey);
			ClientConfiguration clientConfig = new ClientConfiguration();
			clientConfig.setMaxConnections(maxConnections);
			clientConfig.setConnectionTimeout(connectionTimeout);
			amazonS3Client = new AmazonS3Client(credentials, clientConfig);
			transferManager = new TransferManager(amazonS3Client);
		}
		return transferManager;
	}

	/**
	 * Gets the AmazonS3 client
	 * 
	 * @return The AmazonS3 client based on the TransferManager
	 */
	private AmazonS3 getAmazonS3Client() {
		if (amazonS3Client == null) {
			amazonS3Client = getTransferManager().getAmazonS3Client();
		}
		return amazonS3Client;
	}

	/**
	 * Lists an object in a specific path
	 * 
	 * @param key
	 *            Path in S3
	 * @return The listing of the object for the specified path or key
	 */
	private ObjectListing listObject(String key) {
		ListObjectsRequest request = new ListObjectsRequest().withBucketName(s3StorageBucketName).withPrefix(key)
				.withMaxKeys(1);
		return getAmazonS3Client().listObjects(request);
	}

	/**
	 * Lists objects in a specific path
	 * 
	 * @param key
	 *            Path in S3
	 * @return The listing of the objects for the specified path or key
	 */
	private ObjectListing listObjects(String key) {
		ListObjectsRequest request = new ListObjectsRequest().withBucketName(s3StorageBucketName).withPrefix(key);
		return amazonS3Client.listObjects(request);
	}

	/**
	 * Gets the /live path of the resource
	 * 
	 * @param naId
	 *            The identifier
	 * @return The /live path specified by the resource
	 */
	private String getLiveBaseKey(Integer naId) {
		return createOpaStorageBaseKey(LIVE, naId);
	}

	/**
	 * Gets the /full path of the resource
	 * 
	 * @param naId
	 *            The identifier
	 * @return The /full path specified by the resource
	 */
	private String getFullBaseKey(Integer naId) {
		return createXmlStoreBaseKey(FULL, naId);
	}

	/**
	 * Gets a base path
	 * 
	 * @param prefix
	 *            Prefix for the path
	 * @param naId
	 *            The identifier
	 * @return The base path specified by the prefix and identifier
	 */
	private String createOpaStorageBaseKey(String prefix, Integer naId) {
		return String.format("%s/%s/%s", OPA_STORAGE, prefix, FileUtils.getExpandedNaidPath(naId));
	}

	/**
	 * Gets a base path
	 * 
	 * @param prefix
	 *            Prefix for the path
	 * @param naId
	 *            The identifier
	 * @return The base path specified by the prefix and identifier
	 */
	private String createXmlStoreBaseKey(String prefix, Integer naId) {
		return String.format("%s/%s/%s.xml", XML_STORE, prefix, FileUtils.getExpandedNaidPath(naId));
	}

	/**
	 * Checks if and object listing is empty or not
	 * 
	 * @param objectListing
	 *            The listing
	 * @return True if the object listing is empty, false otherwise
	 */
	private boolean isEmpty(ObjectListing objectListing) {
		return objectListing.getObjectSummaries().isEmpty();
	}

	/**
	 * Gets the base URL for Amazon S3
	 * 
	 * @return The base URL
	 */
	public String getBaseURL() {
		return String.format("https://s3.amazonaws.com/%s", s3StorageBucketName);
	}

	/**
	 * Checks the key or path of the file
	 * 
	 * @param key
	 *            The key or path of the resource
	 * @return The key checked and ready to use
	 */
	private String checkKey(String key) {
//		try {
//			key = URLDecoder.decode(key, "UTF-8");
//		} catch (UnsupportedEncodingException e) {
//		}
		String result = key;
		if (key.contains(getBaseURL())) {
			result = URIUtilities.getUrlWithPathEncoded(key).getPath();
			result = result.replace("/" + s3StorageBucketName + "/", "");
		}

		return result;
	}

	/**
	 * Uploads a file to Amazon S3
	 * 
	 * @param file
	 *            The file to be uploaded
	 * @param key
	 *            Key of the file in S3
	 * @throws Exception
	 */
	private void upload(File file, String key) throws Exception {
		try {
			InputStream targetStream = new FileInputStream(file);
			getTransferManager().upload(new PutObjectRequest(s3StorageBucketName, key, targetStream, null)
					.withCannedAcl(CannedAccessControlList.PublicRead)).waitForCompletion();
		} catch (AmazonClientException e) {
			throw new OpaRuntimeException(String.format("Error uploading file: %s, Message: %s", key, e.getMessage()),
					e);
		}
	}

	/**
	 * Gets the full path of a file or resource
	 * 
	 * @param basePath
	 *            The base path
	 * @param relativePath
	 *            The relative path of the file or resource
	 * @return The full path of the file
	 */
	private String getFullPath(String basePath, String relativePath) {
		return FilenameUtils.separatorsToUnix(basePath + File.separator + relativePath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nara.opa.common.storage.OpaStorage#getFile(java.lang.String)
	 */
	@Override
	public File getFile(String key) {
		File dest = null;
		try {
			dest = File.createTempFile("s3o", FilenameUtils.getName(key));
		} catch (IOException e) {
			throw new OpaRuntimeException(
					String.format("Error creating temp file: %s,  Message: %s", key, e.getMessage()), e);
		}

		try {
			getTransferManager().download(s3StorageBucketName, key, dest).waitForCompletion();
		} catch (AmazonClientException | InterruptedException e) {
			throw new OpaRuntimeException(String.format("Error getting file: %s, Message: %s", key, e.getMessage()), e);
		}

		return dest;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.nara.opa.common.storage.OpaStorage#getFileContent(java.lang.String)
	 */
	@Override
	public byte[] getFileContent(String key) throws IOException {
		byte[] data = null;
		S3Object object = amazonS3Client.getObject(new GetObjectRequest(s3StorageBucketName, key));
		InputStream is = object.getObjectContent();
		data = IOUtils.toByteArray(is);
		is.close();
		object.close();
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nara.opa.common.storage.OpaStorage#saveFile(java.io.File,
	 * java.lang.String)
	 */
	@Override
	public void saveFile(File file, String key) {
		ObjectListing objectListing = listObject(key);
		boolean fileExists = !isEmpty(objectListing);

		if (!fileExists) {
			try {
				upload(file, key);
			} catch (Exception e) {
				throw new OpaRuntimeException(
						String.format("Error uploading file: %s, Message: %s", key, e.getMessage()), e);
			}
		}
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
		List<String> files = new ArrayList<String>();
		ObjectListing list = listObjects(key);
		do {
			List<S3ObjectSummary> summaries = list.getObjectSummaries();
			for (S3ObjectSummary summary : summaries) {
				String summaryKey = summary.getKey();
				files.add(summaryKey);
			}

			list = amazonS3Client.listNextBatchOfObjects(list);
		} while (list.isTruncated());

		return files;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.nara.opa.common.storage.OpaStorage#getFullPathInLive(java.lang.String
	 * , java.lang.Integer)
	 */
	@Override
	public String getFullPathInLive(String relativePath, Integer naId) {
//		try {
//			return URLEncoder.encode(getFullPath(getLiveBaseKey(naId), relativePath), "UTF-8");
//		} catch (UnsupportedEncodingException e) {
//		}
//		return null;
		return getFullPath(getLiveBaseKey(naId), relativePath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nara.opa.common.storage.OpaStorage#exists(java.lang.String)
	 */
	@Override
	public boolean exists(String key) {
		return getObjectMetadata(key)!=null;
	}

	private ObjectMetadata getObjectMetadata(String key){
		ObjectMetadata md;
		try {
			md = getAmazonS3Client().getObjectMetadata(s3StorageBucketName, key);
		} catch (AmazonS3Exception e) {
			md = null;
		}
		return md;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nara.opa.common.storage.OpaStorage#getURL(java.lang.String)
	 */
	@Override
	public String getURL(String key) {
		return String.format("%s/%s", getBaseURL(), key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.nara.opa.common.storage.OpaStorage#getCloudFrontURL(java.lang.String)
	 */
	@Override
	public String getCloudFrontURL(String key) {
		if (useCloudFront) {
			String url = String.format("%s%s", cloudFrontDomainName, key);
			key = URIUtilities.getUrlWithPathEncoded(url).getPath();
			if (!key.isEmpty()) {
				if (key.charAt(0) == '/' || key.charAt(0) == '\\') {
					key = key.substring(1);
				}
			}

			url = String.format("%s%s", cloudFrontDomainName, key);
			return url;
		}

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
		return String.format("%s/%s%s", getBaseURL(), s3ExportsLocation, exportFilePath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.nara.opa.common.storage.OpaStorage#getContentLength(java.lang.String)
	 */
	@Override
	public long getContentLength(String key) {
		key = checkKey(key);

		if (!exists(key))
			throw new OpaRuntimeException(String.format("File not found: %s", key));

		S3Object s3object = getAmazonS3Client().getObject(new GetObjectRequest(s3StorageBucketName, key));
		long length = s3object.getObjectMetadata().getContentLength();
		try {
			s3object.close();
		} catch (IOException e) {
		}
		return length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nara.opa.common.storage.OpaStorage#getETag(java.lang.String)
	 */
	@Override
	public String getETag(String key) {
		key = checkKey(key);

		if (!exists(key))
			throw new OpaRuntimeException(String.format("File not found: %s", key));

		S3Object s3object = getAmazonS3Client().getObject(new GetObjectRequest(s3StorageBucketName, key));
		String etag = s3object.getObjectMetadata().getETag();
		try {
			s3object.close();
		} catch (IOException e) {
		}
		return etag;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nara.opa.common.storage.OpaStorage#getStream(java.lang.String)
	 */
	@Override
	public InputStream getStream(String key) {
		key = checkKey(key);

		if (!exists(key))
			throw new OpaRuntimeException(String.format("File not found: %s", key));

		S3Object object = amazonS3Client.getObject(new GetObjectRequest(s3StorageBucketName, key));
		InputStream is = object.getObjectContent();
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
	public void getFilePortionIntoOutput(String key, long start, long length, OutputStream output)
			throws FileNotFoundException, IOException {
		key = checkKey(key);
		GetObjectRequest rangeObjectRequest = new GetObjectRequest(s3StorageBucketName, key);
		rangeObjectRequest.setRange(start, length);
		S3Object objectPortion = getAmazonS3Client().getObject(rangeObjectRequest);

		InputStream objectData = objectPortion.getObjectContent();
		ByteStreams.copy(objectData, output);
		objectData.close();
		objectPortion.close();
	}

	@Override
	public String getFullPathInXmlStore(String relativePath, Integer naId) {
		String fullPath = getFullPath(getFullBaseKey(naId), relativePath);
		fullPath = fullPath.substring(0, fullPath.length() - 1);
		return fullPath;
	}

	public void deleteFiles(String... keys) throws Exception {
		try {
			DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(s3StorageBucketName).withKeys(keys);
			amazonS3Client.deleteObjects(deleteObjectsRequest);
		} catch (Exception e) {
			throw new OpaRuntimeException(e);
		}
	}

}
