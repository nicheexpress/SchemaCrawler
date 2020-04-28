#!/bin/bash
set -e

echo "** Setting up SchemaCrawler v$SCHEMACRAWLER_VERSION distribution"

SCHEMACRAWLER_VERSION=16.7.1
SC_DIR=`pwd`/schemacrawler-"$SCHEMACRAWLER_VERSION"-distribution

# Download additional libraries to allow SchemaCrawler commands to work
echo "Downloading additional libraries to support SchemaCrawler"
cd "$SC_DIR"
cd ./_downloader
pwd
chmod +x ./download.sh

./download.sh jackson

./download.sh groovy
./download.sh python
./download.sh ruby

./download.sh velocity
./download.sh thymeleaf
./download.sh mustache

echo "Done"

