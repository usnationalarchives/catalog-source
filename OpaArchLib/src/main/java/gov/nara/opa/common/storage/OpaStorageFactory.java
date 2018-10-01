package gov.nara.opa.common.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class OpaStorageFactory {

	@Value(value = "${useS3Storage}")
	private boolean useS3Storage;

	@Autowired
	private FileSystemOpaStorageImpl fileSystemOpaStorage;

	@Autowired
	private S3OpaStorageImpl s3OpaStorage;

	/**
	 * Creates and OpaStorage
	 * 
	 * @return An OpaStorage based on the properties 
	 */
	public OpaStorage createOpaStorage() {
		if (useS3Storage) {
			return createS3OpaStorage();
		} else {
			return createFilesystemOpaStorage();
		}
	}

	/**
	 * @return A FileSystemOpaStorageImpl instance
	 */
	private OpaStorage createFilesystemOpaStorage() {
		return fileSystemOpaStorage;
	}

	/**
	 * @return A S3OpaStorageImpl instance
	 */
	private OpaStorage createS3OpaStorage() {
		return s3OpaStorage;
	}

}