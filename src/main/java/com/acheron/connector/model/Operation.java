package com.acheron.connector.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author PraveenKumarReddy
 * 
 * The Operation class represents an operation with a specific operation type.
 * The 'operationType' field holds the type of the operation.
 * 
 * This class uses Lombok annotations to automatically generate common code,
 * such as getters, setters, all-arguments constructor, and a no-arguments constructor.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Operation{
	
	/**
	 * Represents the type of the operation. 
	 * This field stores the specific type of operation that is being performed.
	 * It is a required field and must not be empty or null.
	 */
	@Valid
	@NotEmpty
	private String operationType;
}