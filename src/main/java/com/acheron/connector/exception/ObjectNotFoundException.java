package com.acheron.connector.exception;

/**
 * @author PraveenKumarReddy
 * 
 * This exception will be thrown when we try to access the object which is not present in GCS Bucket
 *
 */
public class ObjectNotFoundException extends RuntimeException {

	
	private static final long serialVersionUID = 1L;

	/**
	 * This is a default constructor
	 */
	public ObjectNotFoundException() {
		super();
	}

	/**
	 * This is a parameterized constructor
	 * 
	 * @param message To Pass the message about exception
	 */
	public ObjectNotFoundException(String message) {
		super(message);
	}

}
