#!/usr/bin/env bash
export SERVER_PORT=8888
#export SPRING_CLOUD_CONFIG_SERVER_GIT_URI="file://`PWD`/../config-repo"
export SPRING_CLOUD_CONFIG_SERVER_GIT_URI=$HOME/Desktop/bootiful-microservices-config

spring run script.groovy
