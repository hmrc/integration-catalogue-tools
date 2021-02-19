#!/bin/bash

./bulk-ingest.sh  -h http://localhost:11114/integration-catalogue-admin-frontend -f core-if-files.csv -p "CORE_IF" -l "/home/developer/workspace/hmrc/macaroons/integration-catalogue-frontend/app/assets/coreifspecs/bulk/"

./bulk-ingest.sh  -h http://localhost:11114/integration-catalogue-admin-frontend -f des-files.csv -p "DES" -l "/home/developer/workspace/hmrc/macaroons/integration-catalogue-frontend/app/assets/desspecs/"

 ./bulk-ingest.sh  -h http://localhost:11114/integration-catalogue-admin-frontend -f api-platform-files.csv -p "API_PLATFORM" -l "/home/developer/workspace/hmrc/macaroons/integration-catalogue-frontend/app/assets/apiplatformspecs/"
