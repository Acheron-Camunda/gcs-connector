package com.acheron.connector.model;



import jakarta.validation.Valid;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
* @author PraveenKumarReddy
* 
* The RequestOptions class represents a set of configurable options for various operations.
* It allows specifying file paths, bucket name, and object names for performing operations
* such as uploading, downloading, and deleting objects in Google Cloud Storage.
*
* This class uses Lombok annotations to automatically generate common code,
* such as getters, setters, all-arguments constructor, and a no-arguments constructor.
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestOptions {
	
	/**
     * The file path used for uploading an object to the bucket.
     * Specify the location of the file to be uploaded.
     * This field is optional, and it can be left empty if not used.
     */
	@Valid
	private String uploadFilePath;

	
	/**
	 * The folder name within the bucket where the uploaded file will be placed.
	 * This field is optional and can be left empty if not used. If provided,
	 * the uploaded file will be stored within this folder in the bucket.
	 */
	
	@Valid
	private String uploadFolderName;
	
	/**
     * The directory for downloading an object from the bucket.
     * Specify the directory where the downloaded file will be saved.
     * This field is optional, and it can be left empty if not used.
     */
	@Valid
	private String downloadFileDirectory;
	/**
	 * The path of the object to be downloaded from the bucket.
	 * Specify the path of the object that should be downloaded within the bucket.
	 * This field is optional, and it can be left empty if not used.
	 */
	@Valid
	private String downlodObjectPath;

	/**
     * The name of the Google Cloud Storage bucket for performing operations.
     * Specify the name of the bucket where the operations will be executed.
     * This field must not be empty or null, and it is mandatory for all operations.
     */
	@Valid
	@NotEmpty
	private String bucketName;

	/**
     * The name of the object to be deleted from the bucket.
     * Specify the name of the object that should be deleted.
     * This field is optional, and it can be left empty if not used.
     */
	@Valid
	private String deleteObjectPath;



}
