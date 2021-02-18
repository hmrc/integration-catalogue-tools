#!/bin/bash

./bulk-ingest.sh  -h http://localhost:11114 -f core-if-files.csv -p "CORE_IF" -l "/home/developer/workspace/hmrc/macaroons/integration-catalogue-frontend/app/assets/coreifspecs/bulk/"

./bulk-ingest.sh  -h http://localhost:11114 -f des-files.csv -p "DES" -l "/home/developer/workspace/hmrc/macaroons/integration-catalogue-frontend/app/assets/desspecs/"