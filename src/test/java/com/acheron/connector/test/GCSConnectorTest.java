package com.acheron.connector.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

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
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;

import io.camunda.connector.api.outbound.OutboundConnectorContext;
import jakarta.validation.Validation;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith({ SystemStubsExtension.class, MockitoExtension.class })
class GCSConnectorTest {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@SystemStub
	private EnvironmentVariables environmentVariables;

	@Mock
	private Storage storage;

	@Mock
	private Blob blob;

	private final GCSUtil gcsUtil = new GCSUtil();

	@Test
	void shouldFailIfInputsAreMissing() {
		GCSRequest request = new GCSRequest(new Operation(), new RequestOptions());
		var validator = Validation.byDefaultProvider().configure()
				.messageInterpolator(new ParameterMessageInterpolator()).buildValidatorFactory().getValidator();
		var violations = validator.validate(request);

		assertThat(violations).hasSize(2);
	}

	@Test
	void shouldAcceptLegacyDownloadObjectPathAlias() throws JsonProcessingException {
		String variablesAsJson = """
				{
				  "operation": {
				    "operationType": "DOWNLOAD_OBJECT"
				  },
				  "storageOptions": {
				    "bucketName": "camunda-connector",
				    "downloadFileDirectory": "src/test/resources/",
				    "downlodObjectPath": "upload/download.png"
				  }
				}
				""";

		GCSRequest request = OBJECT_MAPPER.readValue(variablesAsJson, GCSRequest.class);

		assertThat(request.getStorageOptions().getDownloadObjectPath()).isEqualTo("upload/download.png");
		assertThat(request.getStorageOptions().getDownlodObjectPath()).isEqualTo("upload/download.png");
	}

	@Test
	void testUploadFileMethod() throws Exception {
		GCSRequest gcsRequest = getGcsRequest();

		GCSResponse actualGcsResponse = gcsUtil.uploadFile(gcsRequest, storage);

		assertThat(actualGcsResponse.getResponse()).isEqualTo(
				"File src/test/resources/SpringBoot.png uploaded to bucket camunda-connector folder upload/ as SpringBoot.png");
		verify(storage).createFrom(any(), any(Path.class), any(Storage.BlobWriteOption.class));
	}

	@Test
	void testDownloadFileMethod() throws Exception {
		GCSRequest gcsRequest = getGcsRequest();
		gcsRequest.getOperation().setOperationType(OperationType.DOWNLOAD_OBJECT.toString());
		gcsRequest.getStorageOptions().setDownloadObjectPath("upload/missing-download.png");
		gcsRequest.getStorageOptions().setDownloadFileDirectory("src/test/resources");
		when(storage.get("camunda-connector", "upload/missing-download.png")).thenReturn(blob);

		GCSResponse actualGcsResponse = gcsUtil.downloadObject(gcsRequest, storage);

		assertThat(actualGcsResponse.getResponse()).isEqualTo("File missing-download.png Downloaded to src/test/resources");
		verify(blob).downloadTo(Path.of("src/test/resources", "missing-download.png"));
	}

	@Test
	void testDeleteObjectMethod() {
		GCSRequest gcsRequest = getGcsRequest();
		gcsRequest.getOperation().setOperationType(OperationType.DELETE_OBJECT.toString());
		when(storage.get("camunda-connector", "upload/SpringBoot.png")).thenReturn(blob);
		when(blob.getGeneration()).thenReturn(1L);

		GCSResponse actualGcsResponse = gcsUtil.deleteObject(gcsRequest, storage);

		assertThat(actualGcsResponse.getResponse())
				.isEqualTo("Object upload/SpringBoot.png was deleted from camunda-connector");
		verify(storage).delete(org.mockito.ArgumentMatchers.eq("camunda-connector"),
				org.mockito.ArgumentMatchers.eq("upload/SpringBoot.png"), any(Storage.BlobSourceOption.class));
	}

	@Test
	void testWithInvalidObjectName() {
		GCSRequest gcsRequest = getGcsRequest();
		gcsRequest.getOperation().setOperationType(OperationType.DELETE_OBJECT.toString());
		gcsRequest.getStorageOptions().setDeleteObjectPath("Invalid-folder/invalid.png");

		assertThatThrownBy(() -> gcsUtil.deleteObject(gcsRequest, storage)).isInstanceOf(ObjectNotFoundException.class)
				.hasMessageContaining("Object with name Invalid-folder/invalid.png Not found in the bucket camunda-connector");
	}

	@Test
	void testWithInvalidFilePathforJsonKey() throws JsonProcessingException {
		environmentVariables.set("JSON_CREDENTIALS_FILEPATH", "src/test/resources/GCS-key.json");
		GCSRequest gcsRequest = getGcsRequest();
		GoogleCloudStorageFunction function = new GoogleCloudStorageFunction();
		OutboundConnectorContext context = mockContext(gcsRequest);

		assertThatThrownBy(() -> function.execute(context)).isInstanceOf(FileNotFoundException.class);
	}

	@Test
	void testWithInvalidJsonCredentialsInFile() throws JsonProcessingException {
		environmentVariables.set("JSON_CREDENTIALS_FILEPATH", "src/test/resources/key.json");
		GCSRequest gcsRequest = getGcsRequest();
		GoogleCloudStorageFunction function = new GoogleCloudStorageFunction();
		OutboundConnectorContext context = mockContext(gcsRequest);

		assertThatThrownBy(() -> function.execute(context)).isInstanceOf(InvalidCredentialFileException.class)
				.hasMessageContaining("Invalid credentials in the provided JSON File");
	}

	@Test
	void shouldPreferGoogleApplicationCredentialsWhenPresent() throws JsonProcessingException {
		environmentVariables.set("GOOGLE_APPLICATION_CREDENTIALS", "src/test/resources/GCS-key.json");
		environmentVariables.set("JSON_CREDENTIALS_FILEPATH", "src/test/resources/key.json");
		GCSRequest gcsRequest = getGcsRequest();
		GoogleCloudStorageFunction function = new GoogleCloudStorageFunction();
		OutboundConnectorContext context = mockContext(gcsRequest);

		assertThatThrownBy(() -> function.execute(context)).isInstanceOf(FileNotFoundException.class);
	}

	@Test
	void testWithDirectoryNotFound() {
		GCSRequest gcsRequest = getGcsRequest();
		gcsRequest.getOperation().setOperationType(OperationType.DOWNLOAD_OBJECT.toString());
		gcsRequest.getStorageOptions().setDownloadFileDirectory("src/Invalid-directory");
		when(storage.get("camunda-connector", "upload/download.png")).thenReturn(blob);

		assertThatThrownBy(() -> gcsUtil.downloadObject(gcsRequest, storage)).isInstanceOf(DirectoryNotFoundException.class)
				.hasMessageContaining("The provided directory src/Invalid-directory is not found in the file system");
		verify(blob, never()).downloadTo(any(Path.class));
	}

	@Test
	void testWithFileAlreadyExists() {
		GCSRequest gcsRequest = getGcsRequest();
		gcsRequest.getOperation().setOperationType(OperationType.DOWNLOAD_OBJECT.toString());
		when(storage.get("camunda-connector", "upload/download.png")).thenReturn(blob);

		assertThatThrownBy(() -> gcsUtil.downloadObject(gcsRequest, storage)).isInstanceOf(FileAlreadyExistsException.class)
				.hasMessageContaining("File with name download.png already exists in directory src/test/resources/");
		verify(blob, never()).downloadTo(any(Path.class));
	}

	private GCSRequest getGcsRequest() {
		Operation operation = new Operation(OperationType.UPLOAD_OBJECT.toString());
		RequestOptions storageOptions = new RequestOptions();
		storageOptions.setUploadFilePath("src/test/resources/SpringBoot.png");
		storageOptions.setUploadFolderName("upload/");
		storageOptions.setBucketName("camunda-connector");
		storageOptions.setDownloadFileDirectory("src/test/resources/");
		storageOptions.setDownloadObjectPath("upload/download.png");
		storageOptions.setDeleteObjectPath("upload/SpringBoot.png");

		return new GCSRequest(operation, storageOptions);
	}

	private OutboundConnectorContext mockContext(GCSRequest gcsRequest) {
		OutboundConnectorContext context = Mockito.mock(OutboundConnectorContext.class);
		when(context.bindVariables(GCSRequest.class)).thenReturn(gcsRequest);
		return context;
	}
}
