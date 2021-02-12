#!/usr/bin/env bash

sbt 'run --csvToOas "input/API Library 2021-02-01 - Integration Catalogue Export.csv" output'

# rm -f $WORKSPACE/integration-catalogue-frontend/app/assets/desspecs/stub/*

# cp output/* $WORKSPACE/integration-catalogue-frontend/app/assets/desspecs/stub 
