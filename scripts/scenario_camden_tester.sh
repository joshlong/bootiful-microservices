#!/usr/bin/env bash

source common.sh || source scripts/common.sh || echo "No common.sh script found..."

set -e

export BOM_VERSION="Camden.BUILD-SNAPSHOT"
export PROFILE="camden"

echo "Ensure that apps are not running"
kill_all_apps

echo -e "Ensure that all the apps are built with $BOM_VERSION!\n"
build_all_apps

cat <<EOF

This Bash file will run all the apps required for Camden tests.

NOTE:

- you need internet connection for the apps to download configuration from Github.
- you need docker-compose for RabbitMQ to start
- you need python to calculate current millis

We will do it in the following way:

01) Run config-server
02) Wait for the app (config-server) to boot (port: 8888)
03) Run eureka-service
04) Wait for the app (eureka-service) to boot (port: 8761)
05) Run hystrix-dashboard
06) Wait for the app (hystrix-dashboard) to boot (port: 8010)
07) Run auth-service
08) Wait for the app (auth-service) to boot (port: 9191)
09) Run dataflow-service
10) Wait for the app (dataflow-service) to boot (port: 9393)
11) Run reservation-client
12) Wait for the app (reservation-client) to boot (port: 9999)
13) Wait for the app (reservation-client) to register in Eureka Server
14) Run reservation-service
15) Wait for the app (reservation-service) to boot (port: 8000)
16) Wait for the app (reservation-service) to register in Eureka Server
17) Run zipkin-service
18) Wait for the app (zipkin-service) to boot (port: 9411)
19) Wait for the app (zipkin-service) to register in Eureka Server
20) Send a test request to populate some entries in Zipkin
21) Check if Zipkin has stored the trace for the aforementioned request

EOF

echo "Starting RabbitMQ on port 9672 with docker-compose"
docker-compose up -d || echo "RabbitMQ seems to be working already or some other exception occurred"

cd $ROOT_FOLDER/bootiful-microservices-$PROFILE

java_jar config-service
wait_for_app_to_boot_on_port 8888

java_jar eureka-service
wait_for_app_to_boot_on_port 8761

java_jar auth-service
wait_for_app_to_boot_on_port 9191

java_jar hystrix-dashboard
wait_for_app_to_boot_on_port 8010

java_jar dataflow-service
wait_for_app_to_boot_on_port 9393

java_jar reservation-client
wait_for_app_to_boot_on_port 9999
check_app_presence_in_discovery RESERVATION-CLIENT

java_jar reservation-service
wait_for_app_to_boot_on_port 8000
check_app_presence_in_discovery RESERVATION-SERVICE

java_jar zipkin-service
wait_for_app_to_boot_on_port 9411
check_app_presence_in_discovery ZIPKIN-SERVICE

get_token
send_test_request 9999 "reservations/names"
echo -e "\n\nThe $BOM_VERSION Reservation client successfully responded to the call"

check_trace
check_span_names

cd $ROOT_FOLDER