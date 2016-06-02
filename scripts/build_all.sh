#!/usr/bin/env bash

source common.sh || source scripts/common.sh || echo "No common.sh script found..."

set -e

# Packages all apps in parallel using 6 cores
./mvnw clean package -T 6