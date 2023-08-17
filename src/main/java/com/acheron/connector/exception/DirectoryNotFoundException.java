package com.acheron.connector.exception;

/**
 * @author PraveenKumarReddy
 * 
 * This exception will be thrown when the Directory is not found for the given download directory input
 *
 */
public class DirectoryNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * This is a default constructor
	 */
	public DirectoryNotFoundException() {
		super();
	}

	/**
	 * This is a parameterized constructor
	 * 
	 * @param message To Pass the message about exception
	 */
	public DirectoryNotFoundException(String message) {
		super(message);
	}

}
