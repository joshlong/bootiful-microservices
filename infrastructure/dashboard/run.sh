#!/bin/sh

set -e

export SPRING_APPLICATION_NAME=hystrix-dashboard
export SPRING_CLOUD_CONFIG_URI=http://localhost:8888

spring run script.groovy
