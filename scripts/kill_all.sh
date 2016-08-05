#!/usr/bin/env bash

source common.sh || source scripts/common.sh || echo "No common.sh script found..."

set -e

echo -e "I'm in folder `pwd`"
echo -e "Killing all apps\n"
kill_app auth-service
kill_app config-service
kill_app dataflow-service
kill_app eureka-service
kill_app hystrix-dashboard
kill_app reservation-client
kill_app reservation-service
kill_app zipkin-service
docker-compose kill && echo "Killed rabbit" && exit 0 || echo "Failed to kill Rabbit" && exit 0
