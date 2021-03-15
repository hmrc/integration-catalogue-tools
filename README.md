# Integration-catalogue-tools

This is a tool that allows the generation of OpenAPI Specification ([OAS](https://www.openapis.org/)) files (version 3) primarily for publishing in the 'Get data for your service' (aka Integration Catalogue).

The input is a comma separated file (csv) with one row per API. Each row must contain a single endpoint and method which will generate a single OAS file.

## Format of the CSV
The first header row is skipped, and each subsequent row must contain these values:

```
<publisher-reference>, <platform>, <title>, <description>, <version>, <method>, <endpoint>
```

**Note**: A CSV exported from google sheets will be compliant with regards to values that contain line breaks or quotes around values.

### Fields:
 - **publisher-reference**: This should be a unique identifier that you use to identify the API. Is used as the output OAS filename.
 - **platform**: This is the identifier of your platform.
 - **title**: Used to populate the title in the OAS.
 - **description**: Used to populate the description in the OAS.
 - **version**: Used to populate the version in the OAS.
 - **method**: Used to populate the method in the OAS for the single endpoint defined.
 - **endpoint**: Used to populate the endpoint in the OAS for the single endpoint defined.

 ### Example
 CSV with one API.
 ```
publisherReference,platform, title,description,version,verb,endpoint
1,DES,"Example API 1","This is an example API.",V0.1.0,GET,/examples
```

Note: Be carefully if quoting values to not include a space between the comma and quote. e.g
 - ```1st value,"2nd value"``` -  good
 - ```2st value, "2nd value"``` - bad

This will produce a file called ```1.yaml``` with the following content:
```
openapi: 3.0.1
info:
  title: Example API 1
  description: This is an example API.
  version: V0.1.0
  x-integration-catalogue:
    platform: DES
    publisher-reference: "1"
paths:
  /examples:
    get:
      responses:
        "200":
          description: OK
        "400":
          description: Bad request
```

# Running and processing a CSV

This assumes you've been given a pre-build `integration-catalogue-tools`. You are required to have Java installed on the path.

```
  Usage:
    integration-catalogue-tools --version | -v
    integration-catalogue-tools --help | -h
    integration-catalogue-tools --csvToOas <input CSV> <output directory>
    integration-catalogue-tools --publish --platform <platform> --filename <oas file> --url <publish url>
    integration-catalogue-tools --publish --platform <platform> --directory <directory> --url <publish url>
```

# Building the tool from source

Bump the version in the build.sbt (if this version is going to be published). Even versions to release. Odd for development.


```
sbt packArchive
```

```target/integration-catalogue-tools-x.y.z-SNAPSHOT.zip``` will contain all the files to run the tool.

To create a downloadable release:
1. Commit and push the version change in the `build.sbt` file
1. Go to the github [releases](https://github.com/hmrc/integration-catalogue-tools/releases) page and draft a new release.
1. Give the release a name, and assign it to the tag / commit.
1. Upload into the release the ```integration-catalogue-tools-x.y.z-SNAPSHOT.zip``` that you have built.
1. Bump the version to the next odd version and commit that.

# Running the tool from source

## Convert CSV to OAS files
```
sbt 'run --csvToOas "<name-of.csv>" "<output-path>"'
```

## Convert CSV to File Transfer Json files
```
sbt 'run --csvToFileTransferJson "<name-of.csv>" "<output-path>"'
```

## To publish API(s)

A folder
```
run --publish --platform DES --filename output2/example-1.yaml --url http://localhost:11114/integration-catalogue-admin-frontend/services/apis/publish --authorizationKey <authorization-key>
```

A directory of OAS files
```
run --publish --platform DES --directory output2 --url http://localhost:11114/integration-catalogue-admin-frontend/services/apis/publish --authorizationKey <authorization-key>
```

## To publish File Transfers
From a directory of File Transfer Json files
```

run --publishFileTransfers --directory myDirectory --url http://localhost:11114/integration-catalogue-admin-frontend/services/filetransfers/publish --authorizationKey <authorization-key>
```
