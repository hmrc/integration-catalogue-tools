#!/usr/bin/env bash

sbt pack

chmod +x target/pack/bin/integration-catalogue-tools                                                                      

# To run
# ./target/pack/bin/integration-catalogue-tools --csvToOas "input/API Library 2021-02-01 - Integration Catalogue Export.csv" output
