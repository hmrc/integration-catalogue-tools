# Integration-catalogue-tools

This is a tool that allows the generation of OpenAPI Specification ([OAS](https://www.openapis.org/)) files (version 3) primarily for publishing in the 'Get data for your service' (aka Integration Catalogue).

The input is a comma separated file (csv) with one row per API. Each row must contain a single endpoint and method which will generate a single OAS file.

## Format of the CSV
The first header row is skipped, and each subsequent row must contain these six values:

```
<publisher-reference>, <title>, <description>, <version>, <method>, <endpoint>
```

**Note**: A CSV exported from google sheets will be compliant with regards to values that contain line breaks or quotes around values.

### Fields:
 - **publisher-reference**: This should be a unique identifier that you use to identify the API. Is used as the output OAS filename.
 - **title**: Used to populate the title in the OAS.
 - **description**: Used to populate the description in the OAS.
 - **version**: Used to populate the version in the OAS.
 - **method**: Used to populate the method in the OAS for the single endpoint defined.
 - **endpoint**: Used to populate the endpoint in the OAS for the single endpoint defined.

 ### Example
 CSV with one API.
 ```
publisherReference,title,description,version,verb,endpoint
1,"Example API 1","This is an example API.",V0.1.0,GET,/examples
```

This will produce a file called ```1.yaml``` with the following content:
```
openapi: 3.0.1
info:
  title: Example API 1
  description: This is an example API.
  version: V0.1.0
paths:
  /examples:
    get:
      summary: Example API 1
      requestBody:
        content:
          application/json:
            examples:
              TODO Example Description:
                value:
                  SomeValue: theValue
      responses:
        "200":
          description: response description
          content:
            application/json: {}
```

# Running and processing a CSV

This assumes you've been given a pre-build `integration-catalogue-tools`. You are required to have Java installed on the path.

```
integration-catalogue-tools --csvToOas "<name-of.csv>" "<output-path>"
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

```
sbt 'run --csvToOas "<name-of.csv>" "<output-path>"'
```
