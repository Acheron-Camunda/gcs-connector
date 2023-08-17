package com.acheron.connector;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author PraveenKumarReddy
 *
 * The GCSResponse class represents a response to send to camunda operate after the operation is done.
 * 
 * This class uses Lombok annotations to automatically generate common code,
 * such as getters, setters, all-arguments constructor, and a no-arguments constructor.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GCSResponse {

	/**
     * The response data as a string.
     * This field holds the response received from the operation.
     */
	private String response;

}
