# Integration-catalogue-tools

This tool allows you manage content for publishing in the API Catalogue.

## Features

- Generating OpenAPI Specification files
- Generating file transfer definition files
- Processing OpenAPI Specification files which do not have x-integration-catalogue metadata
- Bulk publishing of OpenAPI Specification or file transfer definition files in the API Catalogue

## API & OpenAPI Specification (OAS)

[OpenAPI Specification (version 3)](https://www.openapis.org/)) 

### Generation

You can bulk generated OpenAPI specification files from an input comma separated file (csv) with one row per API. Each row must contain a single endpoint and method which will generate a single OpenAPI Specification file as the output.

### Format of the CSV
The first header row is skipped, and each subsequent row must contain these values:

```
<publisher-reference>, <platform>, <title>, <description>, <version>, <method>, <endpoint>, <status>, <reviewed-date>
```

**Note**: A CSV exported from google sheets will be compliant with regards to values that contain line breaks or quotes around values.

#### Fields:
 - **publisher-reference**: This should be a unique identifier that you use to identify the API. Is used as the output OpenAPI Specification filename
 - **platform**: This is the identifier of your platform
 - **title**: Used to populate the title in the OpenAPI Specification
 - **description**: Used to populate the description in the OpenAPI Specification
 - **version**: Used to populate the version in the OpenAPI Specification
 - **method**: Used to populate the method in the OpenAPI Specification for the single endpoint defined
 - **endpoint**: Used to populate the endpoint in the OpenAPI Specification for the single endpoint defined
 - **status**: Used to populate the api status in the OpenAPI Specification, allowed values are [ALPHA, BETA, DEPRECATED or LIVE]
- **reviewed-date** : ISO 8601 date of when the api was last reviewed.

#### Example
A  CSV with one API defined;

```
publisherReference,platform,title,description,version,verb,endpoint,status,reviewedDate
1,DES,"Example API 1","This is an example API.",V0.1.0,GET,/examples,LIVE,2020-11-04T20:27:05.000Z
```

Note: Be carefully if quoting values to not include a space between the comma and quote. e.g
 - ```1st value,"2nd value"``` - correct
 - ```2st value, "2nd value"``` - incorrect

This will produce a file called ```1.yaml``` with the following content:
```
openapi: 3.0.1
info:
  title: Example API 1
  description: This is an example API.
  version: V0.1.0
  x-integration-catalogue:
    reviewed-date: 2020-11-04T20:27:05.000Z
    platform: DES
    publisher-reference: "1"
    status: LIVE
paths:
  /examples:
    get:
      responses:
        "200":
          description: OK
        "400":
          description: Bad request
```

## File Transfer definition

The API catalogue allows the publishing of a File Transfer definition.

### File Transfer definition specification
See API catalogue publishers guide.

### Generation

You can bulk generate file transfer  definition files from an input comma separated file (csv) with one row per file transfer. 
### Format of the CSV

The rows are read using the following column headers:

```
PublisherReference,Title,Description,LastUpdated,ReviewedDate,Platform,ContactName,ContactEmail,Source,Target,Transport1,Transport2,Pattern
```

**Note**: A CSV exported from google sheets will be compliant with regards to values that contain line breaks or quotes around values.

#### Fields
- **PublisherReference** : This should be a unique identifier that you use to identify the file transfer
- **Title** :
- **Description** :
- **LastUpdated** : ISO 8601 date of when the definition was last updated.
- **ReviewedDate** : ISO 8601 date of when the definition was last reviewed.
- **Platform** : This is the identifier of your platform.
- **ContactName** :
- **ContactEmail** :
- **Source** : This is the source system
- **Target** : This is the target system
- **Transport1**: Transport 1
- **Transport2**: Transport 2 (Optional) **Note**: If you want more than two transports, you can add more in the generated Json file.
- **Pattern** : This is the architectural pattern name

#### Example CSV Input

```
PublisherReference,Title,Description,LastUpdated,ReviewedDate,Platform,ContactName,ContactEmail,Source,Target,Transport1,Transport2,Pattern
MyRef-1, My File Transfer, This is my awesome file transfer. A file goes from here to over here, 2021-01-01T13:45:10Z, 2020-12-25T20:27:05.000Z, API_Platform, example contact, example@example.con, System A, System B, S3, UTM, Corp to Corp
```

#### Example File Transfer Definition
 
```
{
   "publisherReference":"MyRef-1",
   "fileTransferSpecificationVersion":"0.1",
   "title":"My File Transfer",
   "description":"This is my awesome file transfer. A file goes from here to over here",
   "platformType":"API_Platform",
   "lastUpdated":"2021-01-01T13:45:10Z",
   "reviewedDate": "2020-11-04T20:27:05.000Z",
   "contact":{
      "name":"example contact",
      "emailAddress":"example@example.con"
   },
   "sourceSystem":[
      "System A"
   ],
   "targetSystem":[
      "System B"
   ],
   "transports": [
    "S3",
    "UTM"
  ],
   "fileTransferPattern":"Corp to Corp"
}

 ```

## Processing OpenAPI Specification files which do not have x-integration-catalogue metadata

If a platform team does not publish its own API documentation but passes it to the API Platform team for publication,
this section assists with publication.

The [first step](#comparing-file-lists-to-find-apis-that-must-be-unpublished) can manage the OAS files in the
integration-catalogue-oas-files repository by looking for APIs that are no longer required and must be unpublished.

The [second step](#adding-metadata-to-openapi-specification-files) adds the `x-integration-catalogue` metadata section
to each file if it is not already present.

### Comparing file lists to find APIs that must be unpublished

This assumes that the updated YAML files are the complete set of the API files for that platform.
Thus, any API files in the integration-catalogue-oas-files repository that are not in the updated set are no longer required.
As input, put all the updated files in some directory, which will be compared to files in the previous path.
The console outputs a list of API filenames that need to be unpublished.

The tool compares files based on the first 4-digit number in their filenames, which is assumed to be the API reference number.

The following input constraints apply:

* the 'previous' and 'updated' directories must exist
* only files ending `.yaml` will be taken into account

### Adding metadata to OpenAPI Specification files

If the YAML files do not have the `x-integration-catalogue` section, this can be added programmatically.
As input, provide the path to the updated files used in the previous step, together with the platform concerned (e.g. CORE_IF),
the reviewed date, and an output directory. The following input constraints apply:

* the input directory must exist
* only files ending `.yaml` will be processed
* subdirectories are ignored
* file names must have at least one 4-digit number that is taken to be the publication reference; longer numbers are ignored
* the reviewed date must be in ISO-8601 format in UTC, e.g., 2022-07-13T17:12:00Z
* the output directory must not exist; it will be created

An `x-integration-catalogue` section will be added to each file that does not already have one.
It is added at the end of the `info` section, e.g.

```
info:
  title: An superb API
  x-integration-catalogue:
    reviewed-date: 2022-07-13T17:12:00Z
    platform: CORE_IF
    publisher-reference: <the 4-digit number from the file name>
servers:
```

The amended files are added to the output directory using the same file names. They will be ready to publish.

# Downloading the tools

Look in the releases [here](https://github.com/hmrc/integration-catalogue-tools/releases).

Find the latest release that has as `integration-catalogue-tools-x.y.z-SNAPSHOT.zip` as an asset. Download and unzip it.

Note: Make sure tha the `bin` folder is in your path, or run all the commands using the full path to the `integration-catalogue-tools` in the bin folder.

# Running the integration-catalogue-tools

This assumes you've been given a pre-build `integration-catalogue-tools`. You are required to have Java installed on the path.

Tested on: 
 - Windows 10
 - Linux (Ubuntu)
 - macOS

```
Usage:
    integration-catalogue-tools --version | -v
    integration-catalogue-tools --help | -h
    integration-catalogue-tools --csvToOas <inputCsv> <output directory>
    integration-catalogue-tools --csvToFileTransferJson <inputCsv> <output directory>
    integration-catalogue-tools --yamlFindApisToUnpublish <previous directory> <updated directory>
    integration-catalogue-tools --yamlAddMetadata <input directory> <platform> <reviewed date> <output directory>
    integration-catalogue-tools --publish [--useFilenameAsPublisherReference] --platform <platform> --filename <oasFile> --url <publish url> --authorizationKey <key>
    integration-catalogue-tools --publish [--useFilenameAsPublisherReference] --platform <platform> --directory <directory> --url <publish url> --authorizationKey <key>
    integration-catalogue-tools --publishFileTransfers --platform <platform> --directory  <directory> --url <publish url> --authorizationKey <key>
    
    Arguments:
        - directory : All files with .yaml or .json extension will be processed
        - useFilenameAsPublisherReference : Uses the filename as the optional publisherReference header. If not included the publisherReference must be present in the OpenAPI Specification file
```

# Building the tool from source and releasing

1. Checkout the git tag that you want to release
1. Set the version number in '''build_output.sh''' to match the tag.
1. Package the release by running;
    ```
    ./build_output.sh
    ```

    ```target/integration-catalogue-tools-<version>.zip``` will contain all the files to run the tool
1. Go to the github [releases](https://github.com/hmrc/integration-catalogue-tools/releases) page and find the release for that version
1. Update the name & notes (if needed).
1. Upload into the release the ```integration-catalogue-tools-x.y.z-SNAPSHOT.zip``` that you have built

# Running the tool from source

## Convert CSV to OpenAPI Specification files
```
sbt 'run --csvToOas "<name-of.csv>" "<output-path>"'
```

## Convert CSV to File Transfer Json files
```
sbt 'run --csvToFileTransferJson "<name-of.csv>" "<output-path>"'
```

## Comparing file lists to find APIs that must be unpublished
```
sbt 'run --yamlFindApisToUnpublish <prevoius-path> <updated-path>'
```

## Add metadata to OpenAPI Specification YAML files
```
sbt 'run --yamlAddMetadata <input-path> <platform> <reviewed-date> <output-path>'
```

## To publish API(s)

A folder
```
run --publish --platform DES --filename output2/example-1.yaml --url http://localhost:11114/integration-catalogue-admin-frontend/services/apis/publish --authorizationKey <authorization-key>
```

A directory of OpenAPI Specification files
```
run --publish --platform DES --directory output2 --url http://localhost:11114/integration-catalogue-admin-frontend/services/apis/publish --authorizationKey <authorization-key>
```

## To publish File Transfers
From a directory of File Transfer Json files
```
run --publishFileTransfers --platform CORE_IF --directory myDirectory --url http://localhost:11114/integration-catalogue-admin-frontend/services/filetransfers/publish --authorizationKey <authorization-key>
```
