# Google Cloud Storage Connector

A Camunda outbound connector to connect with the Google Cloud Storage and perform operations with it

## GCS Connector Operations

| Operation           | Functionality                                             |
|---------------------|-----------------------------------------------------------|
| Upload File         | Upload a file from file system to a GCS bucket.           |
| Download File       | Download an object from a GCS bucket to file system.      |
| Delete File         | Delete an object from a GCS bucket.                       |

## Prereqisites
- Access credentials for your Google Cloud Storage project can be obtained through a Service Account. 
- To create the necessary Service Account, refer to the [Developer's Guide for creating a GCS service account](https://developers.google.com/workspace/guides/create-credentials#service-account)
- The IAM Service Account requires a role with appropriate permissions for object operations within a bucket. You can find more information about [cloud storage roles](https://cloud.google.com/storage/docs/access-control/iam-roles) and [cloud storage permissions](https://cloud.google.com/storage/docs/access-control/iam-permissions) 
- Follow the [Developers guide for creating credentials for a service account](https://developers.google.com/workspace/guides/create-credentials#create_credentials_for_a_service_account) to create and download a JSON key file.

- Make sure to define the complete path of the JSON key file as an environment variable  to the connector application

- The JSON file is formatted as follows
```json
{
    "type": "service_account",
    "project_id": "",
    "private_key_id": "",
    "private_key": "-----BEGIN PRIVATE KEY-----\n ...\n-----END PRIVATE KEY-----\n",
    "client_email": "",
    "client_id": "",
    "auth_uri": "",
    "token_uri": "",
    "auth_provider_x509_cert_url": "",
    "client_x509_cert_url": "",
    "universe_domain": "googleapis.com"
  }
  
```

## Connector Configuration

| Name                    | Description                                                      | Example                            |
|-------------------------|------------------------------------------------------------------|------------------------------------|
| `operationType`         | Type of operation you intend to perform with the GCS bucket      | Upload/Download/Delete File        |
| `bucketName`            | Name of the GCS bucket that you want to perform operations with  | `camunda-connector`                |
| `uploadFilePath`        | Absolute path to the file you want to upload                     | `/gcs/SpringBoot.png`              |
| `uploadFolderName`      | Folder name within the bucket where the uploaded file will be placed.| `/your-folder/sub-folder/`       |
| `downloadFileDirectory` | Directory to save the downloaded file                            | `/gcs/`                            |
| `downloadObjectPath`    | Absolute path of the object to be downloaded from the bucket.    | `/your-folder/sub-folder/SpringBoot.png`|
| `deleteObjectPath`      | Absolute path of the object to be deleted from GCS bucket        | `/your-folder/sub-folder/SpringBoot.png`|

**Note:**  The GCS connector doesn't require secret inputs. Instead, it operates by taking the path of the credential JSON key file as an environment variable.

In Modeler the configuration for each operation looks like this  
### Upload file
<img src="assets/upload.png" alt="upload image" width="400" />

###  Download File
<img src="assets/download.png" alt="download image" width="400" />

###  Delete File
<img src="assets/delete.png" alt="delete image" width="400" />

## Runtime 

Build a jar file with dependencies

```bash
mvn clean package
```
Build an image by using the Dockerfile 

```Dockerfile
FROM camunda/connectors:0.22.1

COPY target/google-cloud-storage-jar-with-dependencies.jar /opt/app/
```
Command for Building an Image 

```bash
docker build -t gcs-connector .
```

## Docker Compose  

- In this scenario, We are utilizing the self-managed version of Camunda 8, and we have created a corresponding Docker Compose file.

- A Docker volume is utilized to establish a connection between local storage and the container. 

- Any modifications made to the local storage will be mirrored within the Docker-mounted volume.

- Ensure that the local storage you're mounting includes the necessary JSON credential file.

- Please note that the ability to upload files is limited to those present either within the mounted volume or stored directly within the container.
- You can utilize the `docker-compose.yaml` file to start a local connector runtime.

## Sample BPMN

You can find the sample BPMN file in the `bpmn` folder of this directory.

## GCS Connector Resources

| Content        | Link                                                                                                      |
|----------------|-----------------------------------------------------------------------------------------------------------|
| YouTube Video  | [How to Use GCS connector: A Developer's Walkthrough](https://www.youtube.com/watch?v=q4Pc4Wz6M7Q)        |
| Blog Post      | [Using GCS Connector: A Step-by-Step Guide](https://blog.acheron-tech.com/google-cloud-storage-connector/)|


## Contact
If you have any questions, concerns, or suggestions regarding this connector, please feel free to contact us: camunda.support@acheron-tech.com











