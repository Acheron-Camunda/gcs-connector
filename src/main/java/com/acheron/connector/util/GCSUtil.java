package com.acheron.connector.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acheron.connector.GCSRequest;
import com.acheron.connector.GCSResponse;
import com.acheron.connector.exception.DirectoryNotFoundException;
import com.acheron.connector.exception.InvalidCredentialFileException;
import com.acheron.connector.exception.ObjectNotFoundException;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

/**
 * @author PraveenKumarReddy
 * 
 * The GCSUtil class provides utility methods for interacting with Google Cloud Storage (GCS).
 * It contains methods for uploading, downloading, and deleting objects in GCS.
 */
public class GCSUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(GCSUtil.class);

	/**
	 * Returns a Google Cloud Storage (GCS) instance based on the provided JSON configuration file.
	 *
	 * @param jsonFilePath The path to the JSON configuration file containing GCS credentials.
	 * @return A Storage object representing the GCS instance.
	 * @throws IOException If an I/O error occurs while reading the JSON
	 *                     configuration file.
	 */
	public Storage getStorage(String jsonFilePath) throws IOException {

		try (FileInputStream serviceAccountStream = new FileInputStream(jsonFilePath)) {

			GoogleCredentials credentials = null;
			try {
				credentials = GoogleCredentials.fromStream(serviceAccountStream);
			} catch (IOException e) {
				throw new InvalidCredentialFileException("Invalid credentials in the provided JSON File");
			}

			return StorageOptions.newBuilder().setCredentials(credentials).build().getService();

		}

	}

	/**
	 * Uploads a file to the specified Google Cloud Storage bucket.
	 *
	 * @param connectorRequest The GCSRequest containing upload configuration  details.
	 * @param storage The Storage object used to interact with GCS.
	 * @return A GCSResponse containing the upload response from GCS.
	 * @throws IOException If an I/O error occurs during the upload process.
	 */
	public GCSResponse uploadFile(GCSRequest connectorRequest, Storage storage) throws IOException {

		String uploadFilePath = connectorRequest.getStorageOptions().getUploadFilePath();

		String uploadFolderName = connectorRequest.getStorageOptions().getUploadFolderName();

		Path path = Paths.get(uploadFilePath);

		String fileName = path.getFileName().toString();

//		File file = new File(uploadFilePath);

		String bucketName = connectorRequest.getStorageOptions().getBucketName();

		LOGGER.info("Upload File path {} ", uploadFilePath);
		LOGGER.info("file Name {}", fileName);
		LOGGER.info("Bucket Name {}", bucketName);

		BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, uploadFolderName + fileName).build();

		// set a generation-match precondition to avoid potential race conditions and  data corruptions.
		// The request to upload returns a 412 error if the preconditions are not met.
		Storage.BlobWriteOption precondition;
		if (storage.get(bucketName, fileName) == null) {
			// For a target object that does not yet exist, set the DoesNotExist precondition.
			// This will cause the upload request to fail if the object is created by another process before the upload request is executed.
			// This is to prevent overwriting an existing object unintentionally
			precondition = Storage.BlobWriteOption.doesNotExist();
		} else {

			// If the destination already exists in your bucket, instead set a generation-match precondition.
			// This will cause the request to fail if the existing object's generation changes before the request runs.
			// The generation number is a unique identifier assigned to each version of an object in Google Cloud Storage.
			// If the generation number changes before the upload request is executed(meaning the object was modified by another process), the request will fail
			// This ensures that the upload request doesn't overwrite the existing object with outdated data.
			precondition = Storage.BlobWriteOption.generationMatch(storage.get(bucketName, fileName).getGeneration());
		}
		storage.createFrom(blobInfo, Paths.get(uploadFilePath), precondition);

		String result = "File " + uploadFilePath + " uploaded to bucket " + bucketName + " folder " + uploadFolderName
				+ " as " + fileName;

		LOGGER.debug(result);

		return new GCSResponse(result);

	}

	/**
	 * Downloads an object from the specified Google Cloud Storage bucket.
	 *
	 * @param connectorRequest The GCSRequest containing download configuration details.
	 * @param storage          The Storage object used to interact with GCS.
	 * @return A GCSResponse containing the download response from GCS.
	 * @throws FileAlreadyExistsException
	 * @throws FileNotFoundException
	 */
	public GCSResponse downloadObject(GCSRequest connectorRequest, Storage storage)
			throws FileAlreadyExistsException, FileNotFoundException {

		String downloadFileDirectory = connectorRequest.getStorageOptions().getDownloadFileDirectory();

		String downloadObjectPath = connectorRequest.getStorageOptions().getDownlodObjectPath();

		String[] parts = downloadObjectPath.split("/");
		String objectName = parts[parts.length - 1];

		String bucketName = connectorRequest.getStorageOptions().getBucketName();

		LOGGER.info("Download File directory {}", downloadFileDirectory);
		LOGGER.info("File Name {}", objectName);
		LOGGER.info("Bucket Name {}", bucketName);

		Blob blob = storage.get(bucketName, downloadObjectPath);

		Path directoryPath = Paths.get(downloadFileDirectory);
		boolean directoryExists = Files.exists(directoryPath) && Files.isDirectory(directoryPath);

		if (!directoryExists) {
			throw new DirectoryNotFoundException("The provided directory " + downloadFileDirectory + " is not found in the file system");

		}

		if (blob != null) {
			if (Files.exists(Paths.get(downloadFileDirectory, objectName))) {
				throw new FileAlreadyExistsException("File with name " + objectName + " already exists in directory " + downloadFileDirectory);

			}

			blob.downloadTo(Paths.get(downloadFileDirectory, objectName));

		} else {
			throw new ObjectNotFoundException("Object with name " + objectName + " Not found in the bucket " + bucketName);
		}
		
		String result = "File " + objectName + " Downloaded to " + downloadFileDirectory;

		LOGGER.debug(result);
		return new GCSResponse(result);

	}

	/**
	 * Deletes an object from the specified Google Cloud Storage bucket.
	 *
	 * @param connectorRequest The GCSRequest containing delete configuration details.
	 * @param storage          The Storage object used to interact with GCS.
	 * @return A GCSResponse containing the delete response from GCS.
	 */
	public GCSResponse deleteObject(GCSRequest connectorRequest, Storage storage) {

		String bucketName = connectorRequest.getStorageOptions().getBucketName();
		String deleteObjectName = connectorRequest.getStorageOptions().getDeleteObjectPath();

		LOGGER.info("Bucket Name {}", bucketName);
		LOGGER.info("Object Name {}", deleteObjectName);

		Blob blob = storage.get(bucketName, deleteObjectName);
		if (blob == null) {
			throw new ObjectNotFoundException(
					"Object with name " + deleteObjectName + " Not found in the bucket " + bucketName);
		}

		// set a generation-match precondition to avoid potential race conditions and data corruptions.
		// The request to delete returns a 412 error if the object's generation number does not match your precondition.
		Storage.BlobSourceOption precondition = Storage.BlobSourceOption.generationMatch(blob.getGeneration());

		storage.delete(bucketName, deleteObjectName, precondition);

		String result = "Object " + deleteObjectName + " was deleted from " + bucketName;

		LOGGER.debug(result);

		return new GCSResponse(result);
	}

}
