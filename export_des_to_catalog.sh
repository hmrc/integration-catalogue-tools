#!/usr/bin/env bash

sbt run

m -f $WORKSPACE/integration-catalogue-frontend/app/assets/desspecs/stub/*

cp output/* $WORKSPACE/integration-catalogue-frontend/app/assets/desspecs/stub 
