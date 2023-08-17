package com.acheron.connector.exception;

/**
 * @author PraveenKumarReddy
 * 
 * 
 *         This exception will be thrown while read invalid Google credentials
 *         from stream
 *
 */
public class InvalidCredentialFileException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * This is a default constructor
	 */
	public InvalidCredentialFileException() {
		super();
	}

	/**
	 * This is a parameterized constructor
	 * 
	 * @param message To Pass the message about exception
	 */
	public InvalidCredentialFileException(String message) {
		super(message);
	}

}
