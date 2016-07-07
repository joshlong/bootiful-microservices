#!/usr/bin/env bash

source common.sh || source scripts/common.sh || echo "No common.sh script found..."

PROFILE="${PROFILE:-camden}"
BOM_VERSION="${BOM_VERSION:-Camden.BUILD-SNAPSHOT}"
ADDITIONAL_MAVEN_OPTS="${ADDITIONAL_MAVEN_OPTS:--Dspring.cloud.release.version=$BOM_VERSION}"

set -e

cd $ROOT_FOLDER

echo -e "\nRunning the build with additional options [$ADDITIONAL_MAVEN_OPTS] and profile [$PROFILE]"

# Packages all apps in parallel using 6 cores
./mvnw clean package -T 6 $ADDITIONAL_MAVEN_OPTS -P$PROFILE -U --batch-mode -Dmaven.test.redirectTestOutputToFile=true