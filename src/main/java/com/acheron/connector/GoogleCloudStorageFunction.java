package com.acheron.connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acheron.connector.model.OperationType;
import com.acheron.connector.util.GCSUtil;
import com.google.cloud.storage.Storage;

import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;

/**
 * @author PraveenKumarReddy
 *
 * The GoogleCloudStorageFunction class represents an outbound connector function for Google Cloud Storage (GCS) operations.
 *
 * This class is annotated with @OutboundConnector, specifying the details of the GCS connector.
 *
 */
@OutboundConnector(name = "GCS", inputVariables = { "operation", "storageOptions" }, type = "com.acheron.connector:gcs:1")

public class GoogleCloudStorageFunction implements OutboundConnectorFunction {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GoogleCloudStorageFunction.class);

	/**
	 * Executes the outbound connector function for Google Cloud Storage (GCS) operations.
	 * This method is required to be implemented as part of the OutboundConnectorFunction interface.
	 *
	 * @param context The OutboundConnectorContext containing the context information
	 *                and input data for the GCS operation.
	 * @return A  GCSResponse representing the response from the GCS operation.
	 * @throws Exception If an error occurs during the execution of the GCS operation.
	 */
	
	@Override
	public GCSResponse execute(OutboundConnectorContext context) throws Exception {

		final var connectorRequest = context.bindVariables(GCSRequest.class);

		String jsonFilePath= System.getenv("JSON_CREDENTIALS_FILEPATH");

		GCSUtil gcsUtil = new GCSUtil();
		
		GCSResponse gcsResponse = executeConnector(connectorRequest, gcsUtil.getStorage(jsonFilePath));

		LOGGER.info("GCSResponse {}", gcsResponse);

		return gcsResponse;
	}
	/**
	 * Executes the Google Cloud Storage (GCS) operation based on the provided GCS request and Storage instance.
	 *
	 * @param connectorRequest The GCSRequest containing the GCS operation details and configuration.
	 * @param storage          The  Storage instance used to interact with Google Cloud Storage.
	 * @return A  GCSResponse representing the response from the GCS operation.
	 * @throws Exception If an error occurs during the execution of the GCS operation.
	 */
	private GCSResponse executeConnector(final GCSRequest connectorRequest, Storage storage) throws Exception {

        GCSUtil gcsUtil = new GCSUtil();
		
		GCSResponse gcsResponse = new GCSResponse();

		if (connectorRequest.getOperation().getOperationType().equals(OperationType.UPLOAD_OBJECT.toString())) {

			gcsResponse = gcsUtil.uploadFile(connectorRequest, storage);

		} else if (connectorRequest.getOperation().getOperationType().equals(OperationType.DOWNLOAD_OBJECT.toString())) {

			gcsResponse = gcsUtil.downloadObject(connectorRequest, storage);

		} else if (connectorRequest.getOperation().getOperationType().equals(OperationType.DELETE_OBJECT.toString())) {

			gcsResponse = gcsUtil.deleteObject(connectorRequest, storage);
		}
		return gcsResponse;

	}
}
