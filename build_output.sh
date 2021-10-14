#!/usr/bin/env bash

VERSION="1.16.1"

sbt 'set version:= "'$VERSION'"' clean packArchive

chmod +x target/pack/bin/integration-catalogue-tools                                                                      

# To run
# ./target/pack/bin/integration-catalogue-tools --csvToOas "input/API Library 2021-02-01 - Integration Catalogue Export.csv" output
