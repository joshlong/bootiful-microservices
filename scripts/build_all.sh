#!/usr/bin/env bash

source common.sh || source scripts/common.sh || echo "No common.sh script found..."

ADDITIONAL_MAVEN_OPTS="${ADDITIONAL_MAVEN_OPTS:--Dspring.cloud.release.version=Brixton.BUILD-SNAPSHOT}"

set -e

cd $ROOT_FOLDER

echo -e "\nRunning the build with additional options [$ADDITIONAL_MAVEN_OPTS]"

# Packages all apps in parallel using 6 cores
./mvnw clean package -T 6 $ADDITIONAL_MAVEN_OPTS