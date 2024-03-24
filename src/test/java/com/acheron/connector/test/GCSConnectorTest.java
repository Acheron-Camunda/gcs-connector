package com.acheron.connector.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import com.acheron.connector.GCSRequest;
import com.acheron.connector.GCSResponse;
import com.acheron.connector.GoogleCloudStorageFunction;
import com.acheron.connector.exception.DirectoryNotFoundException;
import com.acheron.connector.exception.InvalidCredentialFileException;
import com.acheron.connector.exception.ObjectNotFoundException;
import com.acheron.connector.model.Operation;
import com.acheron.connector.model.OperationType;
import com.acheron.connector.model.RequestOptions;
import com.acheron.connector.util.GCSUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import io.camunda.connector.api.error.ConnectorInputException;
import io.camunda.connector.test.outbound.OutboundConnectorContextBuilder;
import io.camunda.connector.test.outbound.OutboundConnectorContextBuilder.TestConnectorContext;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

/**
 * @author PraveenKumarReddy
 *
 *The GCSConnectorTest test class which will contain test methods to test the application 
 */
@ExtendWith(SystemStubsExtension.class)
class GCSConnectorTest {

	@SystemStub
	private EnvironmentVariables environmentVariables;

	private static GCSUtil mockGcsUtil;
	private static GCSUtil gcsUtil;
	private static Storage mockStorage;

	@BeforeEach
	public void setup() {

		// stubbing the environment variable to system
		environmentVariables.set("JSON_CREDENTIALS_FILEPATH", "C:\\CloudAI\\credentials.json");

		// Initialize the mock objects
		mockStorage = Mockito.mock(Storage.class);
		mockGcsUtil = Mockito.mock(GCSUtil.class);

		// Initialize the objects
		gcsUtil = new GCSUtil();

	}

	@AfterEach
	public void teardown() {
		mockStorage = null;
		mockGcsUtil = null;
		gcsUtil = null;
		environmentVariables = null;
	}

	/**
	 * Test case to verify that the validation fails if inputs are missing in the GCSRequest.
	 */
	@Test
	void shouldFailIfInputsAreMissing() throws JsonProcessingException {
		// given
		GCSRequest request = getGCSRequestWithNull();

		String variablesAsJson = new ObjectMapper().writeValueAsString(request);

		// when
		TestConnectorContext context = OutboundConnectorContextBuilder.create().variables(variablesAsJson).build();

		// then
		assertThatThrownBy(() -> context.validate(request)).isInstanceOf(ConnectorInputException.class)
				.hasMessageContaining(
						"jakarta.validation.ValidationException: Found constraints violated while validating input:");
	}


	/**
     * Test case to verify that the execute method of GoogleCloudStorageFunction returns the received message correctly after executing.
	 */
	
	// This test method will directly perform operations to google cloud cloud so give proper inputs(actual filepaths, names) 

//	@Test
	void shouldReturnReceivedMessageWhenExecute() throws Exception {

		// given

		GCSRequest gcsRequest = getGCSrequest();
		String variablesAsJson = new ObjectMapper().writeValueAsString(gcsRequest);
		GoogleCloudStorageFunction function = new GoogleCloudStorageFunction();
		TestConnectorContext context = OutboundConnectorContextBuilder.create().variables(variablesAsJson).build();

		// when
		var result = function.execute(context);

		// then
		assertThat(result).isInstanceOf(GCSResponse.class).extracting("response")
				.isEqualTo("File " + gcsRequest.getStorageOptions().getUploadFilePath() + " uploaded to bucket "
						+ gcsRequest.getStorageOptions().getBucketName() +" folder " +gcsRequest.getStorageOptions().getUploadFolderName() + " as " + "SpringBoot.png");
	}

	/**
	 *   Test case to verify the functionality of the uploadFile method in GCSUtil Class.
	 */
	@Test
	void testUploadFileMethod() throws IOException {

		// given
		GCSRequest gcsRequest = getGCSrequest();
		GCSResponse expectedGcsResponse = new GCSResponse();
		String fileName = "SpringBoot.png";
		expectedGcsResponse.setResponse("File " + gcsRequest.getStorageOptions().getUploadFilePath() + " uploaded to bucket "
				+ gcsRequest.getStorageOptions().getBucketName() +" folder " +gcsRequest.getStorageOptions().getUploadFolderName() + " as " + fileName);

		when(mockGcsUtil.uploadFile(gcsRequest, mockStorage)).thenReturn(expectedGcsResponse);

// Tried  actually creating the storage and tested it  as below 
// It is directly performing operation to cloud so i'm going with the mockStorage for actualGcsResponse too
//
//		String jsonFilePath = System.getenv("JSON_CREDENTIALS_FILEPATH");
//		FileInputStream serviceAccountStream = new FileInputStream(jsonFilePath);
//		GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountStream);
//		Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
// 		GCSResponse actualGcsResponse = gcsUtil.uploadFile(gcsRequest, storage);

		// when
		GCSResponse actualGcsResponse = gcsUtil.uploadFile(gcsRequest, mockStorage);

		// then
		assertEquals(expectedGcsResponse, actualGcsResponse);

	}


	/**
	 *   Test case to verify the functionality of the downloadFile method in GCSUtil Class.
	 */
	
	// This test cannot be done with the mocked storage object like upload, so we need to do it with actual storage object with proper download inputs, JSON file path

//	@Test
	void testDownloadFileMethod() throws IOException {

		// given
		GCSRequest gcsRequest = getGCSrequest();
		gcsRequest.getOperation().setOperationType(OperationType.DOWNLOAD_OBJECT.toString());
		GCSResponse expectedGcsResponse = new GCSResponse();
		
		String objectName = getObjectNam(gcsRequest.getStorageOptions().getDownlodObjectPath());

		
		expectedGcsResponse.setResponse("File " + objectName
				+ " Downloaded to " + gcsRequest.getStorageOptions().getDownloadFileDirectory());

		when(mockGcsUtil.downloadObject(gcsRequest, mockStorage)).thenReturn(expectedGcsResponse);

		String jsonFilePath = System.getenv("JSON_CREDENTIALS_FILEPATH");

		FileInputStream serviceAccountStream = new FileInputStream(jsonFilePath);
		GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountStream);
		Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

		// when
		GCSResponse actualGcsResponse = gcsUtil.downloadObject(gcsRequest, storage);

//		GCSResponse actualGcsResponse = gcsUtil.downloadObject(gcsRequest, mockStorage);

		// then
		assertEquals(expectedGcsResponse, actualGcsResponse);

	}
	
	/**
	 *   Test case to verify the functionality of the deleteObject method in GCSUtil Class.
	 */
 // This test cannot be done with the mocked storage object like upload so we need to do it with actual storage object with proper delete inputs,JSON key filePath
//	@Test
	void testDeleteObjectMethod() throws IOException {

		// given
		GCSRequest gcsRequest = getGCSrequest();
		gcsRequest.getOperation().setOperationType(OperationType.DELETE_OBJECT.toString());

		GCSResponse expectedGcsResponse = new GCSResponse();

		expectedGcsResponse.setResponse("Object " + gcsRequest.getStorageOptions().getDeleteObjectPath()
				+ " was deleted from " + gcsRequest.getStorageOptions().getBucketName());

		when(mockGcsUtil.deleteObject(gcsRequest, mockStorage)).thenReturn(expectedGcsResponse);

		String jsonFilePath = System.getenv("JSON_CREDENTIALS_FILEPATH");
		FileInputStream serviceAccountStream = new FileInputStream(jsonFilePath);
		GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountStream);
		Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

		// when
		GCSResponse actualGcsResponse = gcsUtil.deleteObject(gcsRequest, storage);

		// then
		assertEquals(expectedGcsResponse, actualGcsResponse);

	}

	/**
     * Test case to verify the behavior when attempting to give an object with an invalid name that is not present in GCS bucket.
	 */
	@Test
	void testWithInvalidObjectName() throws IOException {
		// given
		GCSRequest gcsRequest = getGCSrequest();
		gcsRequest.getOperation().setOperationType(OperationType.DELETE_OBJECT.toString());
		gcsRequest.getStorageOptions().setDeleteObjectPath("Invalid-folder/invalid.png");

		String jsonFilePath = System.getenv("JSON_CREDENTIALS_FILEPATH");
		FileInputStream serviceAccountStream = new FileInputStream(jsonFilePath);
		GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountStream);
		Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
		
		// then
		assertThatThrownBy(() -> gcsUtil.deleteObject(gcsRequest,storage)).isInstanceOf(ObjectNotFoundException.class)
		.hasMessageContaining("Object with name " + gcsRequest.getStorageOptions().getDeleteObjectPath() + " Not found in the bucket " + gcsRequest.getStorageOptions().getBucketName());
	}
	
	
	/**
     * Test case to verify the behavior when providing an invalid file path for the JSON key file.
	 */
	@Test
	void testWithInvalidFilePathforJsonKey() throws JsonProcessingException   {
		// given
		environmentVariables.set("JSON_CREDENTIALS_FILEPATH", "src/test/resources/GCS-key.json");
		GCSRequest gcsRequest = getGCSrequest();
		GoogleCloudStorageFunction function = new GoogleCloudStorageFunction();
		
		//when
		TestConnectorContext context = OutboundConnectorContextBuilder.create()
				.variables(new ObjectMapper().writeValueAsString(gcsRequest)).build();

		// then
		assertThatThrownBy(() -> function.execute(context)).isInstanceOf(FileNotFoundException.class);

	}
	
	/**
     * Test case to verify the behavior when providing invalid JSON credentials in the key file.
	 */
	@Test
	void testWithInvalidJsonCredentialsInFile() throws JsonProcessingException {
		// given
		environmentVariables.set("JSON_CREDENTIALS_FILEPATH", "src/test/resources/key.json");
		GCSRequest gcsRequest = getGCSrequest();
		GoogleCloudStorageFunction function = new GoogleCloudStorageFunction();
		
		// when
		TestConnectorContext context = OutboundConnectorContextBuilder.create()
				.variables(new ObjectMapper().writeValueAsString(gcsRequest)).build();

		// then
		assertThatThrownBy(() -> function.execute(context)).isInstanceOf(InvalidCredentialFileException.class)
		.hasMessageContaining("Invalid credentials in the provided JSON File");

		
	}
	
	
	/**
     * Test method to demonstrate handling scenarios when the specified directory does not exist.
	 */
	@Test
	void testWithDirectoryNotFound() throws JsonProcessingException {
		// given
		GCSRequest gcsRequest = getGCSrequest();
		gcsRequest.getOperation().setOperationType(OperationType.DOWNLOAD_OBJECT.toString());
		gcsRequest.getStorageOptions().setDownloadFileDirectory("src/Invalid-directory");
		
		GoogleCloudStorageFunction function = new GoogleCloudStorageFunction();
		
		// when
		TestConnectorContext context = OutboundConnectorContextBuilder.create()
				.variables(new ObjectMapper().writeValueAsString(gcsRequest)).build();

		// then
		assertThatThrownBy(() -> function.execute(context)).isInstanceOf(DirectoryNotFoundException.class)
		.hasMessageContaining("The provided directory " + gcsRequest.getStorageOptions().getDownloadFileDirectory() + " is not found in the file system");
				
	}

	
	/**
     * Test method to demonstrate handling scenarios when a file with the same name already exists in the provided download directory.
	 */
	// This test needs  real  object  that is present in the bucket  
//	@Test
	void testWithFileAlreadyExists() throws JsonProcessingException {
		// given
		GCSRequest gcsRequest = getGCSrequest();
		gcsRequest.getOperation().setOperationType(OperationType.DOWNLOAD_OBJECT.toString());
		
		GoogleCloudStorageFunction function = new GoogleCloudStorageFunction();
      
		String objectName = getObjectNam(gcsRequest.getStorageOptions().getDownlodObjectPath());
		
		// when
		TestConnectorContext context = OutboundConnectorContextBuilder.create()
				.variables(new ObjectMapper().writeValueAsString(gcsRequest)).build();

		// then
		assertThatThrownBy(() -> function.execute(context)).isInstanceOf(FileAlreadyExistsException.class)
		.hasMessageContaining("File with name " + objectName + " already exists in directory " + gcsRequest.getStorageOptions().getDownloadFileDirectory());
		

		
	}

	GCSRequest getGCSrequest() {

		Operation operation = new Operation(OperationType.UPLOAD_OBJECT.toString());
		RequestOptions storage = new RequestOptions();
		storage.setUploadFilePath("src/test/resources/SpringBoot.png");
		storage.setUploadFolderName("upload/");
		storage.setBucketName("camunda-connector");
		storage.setDownloadFileDirectory("src/test/resources/");
		storage.setDownlodObjectPath("upload/download.png");
		storage.setDeleteObjectPath("upload/SpringBoot.png");
		
		return new GCSRequest(operation, storage);

	}
	
	GCSRequest getGCSRequestWithNull() {

		Operation operation = new Operation();
		RequestOptions storage = new RequestOptions();

		return new GCSRequest(operation, storage);
	}

	
	String getObjectNam(String downloadObjectPath) {
		String[] parts=downloadObjectPath.split("/");
		return  parts[parts.length - 1];
		
	}


}
