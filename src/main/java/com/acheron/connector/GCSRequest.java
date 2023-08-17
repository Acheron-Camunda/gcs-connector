package com.acheron.connector;

import com.acheron.connector.model.Operation;
import com.acheron.connector.model.RequestOptions;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author PraveenKumarReddy
 * 
 * The GCSRequest class represents a request to Google Cloud Storage (GCS)  from camunda operate.
 * It encapsulates an  Operation and RequestOptions to specify the type of operation
 * and configurable options for performing operations in Google Cloud Storage.
 * 
 * This class uses Lombok annotations to automatically generate common code,
 * such as getters, setters, all-arguments constructor, and a no-arguments constructor.
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GCSRequest {

	@Valid
	private Operation operation;

	@Valid
	private RequestOptions storageOptions;

}
