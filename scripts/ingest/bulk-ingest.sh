#!/bin/bash

while getopts h:f:p:l: flag
do
    case "${flag}" in
        h) host=${OPTARG};;
        f) filelist=${OPTARG};;
        p) plat=${OPTARG};;
		l) location=${OPTARG};;
    esac
done
echo "Host: $host";
echo "FileList: $filelist";
echo "platform: $plat";
echo "path to specs: $location";

INPUT=$filelist
FILESFOLDER=$location
OLDIFS=$IFS
IFS=','
[ ! -f $INPUT ] && { echo "$INPUT file not found"; exit 99; }
while read reference filename
do
	echo "reference : $reference"
	echo "path: $FILESFOLDER${filename}"
	   curl --location --request PUT "$host/services/apis/publish" \
--header 'x-platform-type: '$plat \
--header 'x-specification-type: OAS_V3' \
--header 'x-publisher-reference: '$reference \
--form 'selectedFile=@'"$FILESFOLDER${filename}"

done < $INPUT
IFS=$OLDIFS

