#!/usr/bin/env bash

set -e

WAIT_TIME="${WAIT_TIME:-5}"
RETRIES="${RETRIES:-10}"
SERVICE_PORT="${SERVICE_PORT:-8081}"
TEST_ENDPOINT="${TEST_ENDPOINT:-check}"
JAVA_PATH_TO_BIN="${JAVA_HOME}/bin/"
if [[ -z "${JAVA_HOME}" ]] ; then
    JAVA_PATH_TO_BIN=""
fi
BUILD_FOLDER="${BUILD_FOLDER:-target}" #target - maven, build - gradle
PRESENCE_CHECK_URL="${PRESENCE_CHECK_URL:-http://localhost:8761/eureka/apps}"
TEST_PATH="${TEST_PATH:-reservations/names}"
HEALTH_HOST="${DEFAULT_HEALTH_HOST:-localhost}" #provide DEFAULT_HEALT HOST as host of your docker machine
RABBIT_MQ_PORT="${RABBIT_MQ_PORT:-9672}"
SYSTEM_PROPS="-Dspring.rabbitmq.host=${HEALTH_HOST} -Dspring.rabbitmq.port=${RABBIT_MQ_PORT}"

# ${RETRIES} number of times will try to curl to /health endpoint to passed port $1 and localhost
function wait_for_app_to_boot_on_port() {
    curl_health_endpoint $1 "127.0.0.1"
}

# ${RETRIES} number of times will try to curl to /health endpoint to passed port $1 and host $2
function curl_health_endpoint() {
    local PASSED_HOST="${2:-$HEALTH_HOST}"
    local READY_FOR_TESTS=1
    for i in $( seq 1 "${RETRIES}" ); do
        sleep "${WAIT_TIME}"
        curl -m 5 "${PASSED_HOST}:$1/health" && READY_FOR_TESTS=0 && break
        echo "Fail #$i/${RETRIES}... will try again in [${WAIT_TIME}] seconds"
    done
    return $READY_FOR_TESTS
}

# Check the app $1 (in capital)
function check_app_presence_in_discovery() {
    echo -e "\n\nChecking for the presence of $1 in Service Discovery for [$(( WAIT_TIME * RETRIES ))] seconds"
    READY_FOR_TESTS="no"
    for i in $( seq 1 "${RETRIES}" ); do
        sleep "${WAIT_TIME}"
        curl -m 5 $PRESENCE_CHECK_URL | grep $1 && READY_FOR_TESTS="yes" && break
        echo "Fail #$i/${RETRIES}... will try again in [${WAIT_TIME}] seconds"
    done
    if [[ "${READY_FOR_TESTS}" == "yes" ]] ; then
        return 0
    else
        return 1
    fi
}

# Runs the `java -jar` for given application $1 and system properties $2
function java_jar() {
    echo -e "\n\nStarting app $1 \n"
    local APP_JAVA_PATH=$1/${BUILD_FOLDER}
    local EXPRESSION="nohup ${JAVA_PATH_TO_BIN}java $2 $SYSTEM_PROPS -jar $APP_JAVA_PATH/*.jar >$APP_JAVA_PATH/nohup.log &"
    echo -e "\nTrying to run [$EXPRESSION]"
    eval $EXPRESSION
    pid=$!
    echo $pid > $APP_JAVA_PATH/app.pid
    echo -e "[$1] process pid is [$pid]"
    echo -e "System props are [$2]"
    echo -e "Logs are under [$APP_JAVA_PATH/nohup.log]\n"
    return 0
}

# Kills an app with given $1 version
function kill_app() {
    echo -e "Killing app $1"
    pkill -f "$1" && echo "Killed $1" || echo "$1 was not running"
    echo -e ""
    return 0
}

# Runs H2 from proper folder
function build_all_apps() {
    ${ROOT_FOLDER}/scripts/build_all.sh
}

# Kill all the apps
function kill_all_apps() {
    ${ROOT_FOLDER}/scripts/kill_all.sh
}

# Calls a POST curl to /person to an app on localhost with port $1 on path $2
function send_test_request() {
    READY_FOR_TESTS="no"
    for i in $( seq 1 "${RETRIES}" ); do
        sleep "${WAIT_TIME}"
        echo -e "Sending a GET to 127.0.0.1:$1/$2 . This is the response:\n"
        curl --fail "127.0.0.1:${1}/${2}" && READY_FOR_TESTS="yes" && break
        echo "Fail #$i/${RETRIES}... will try again in [${WAIT_TIME}] seconds"
    done
    if [[ "${READY_FOR_TESTS}" == "yes" ]] ; then
        return 0
    else
        return 1
    fi
}

# Calls a GET to zipkin to dependencies
function check_trace() {
    echo -e "\nChecking if Zipkin has stored the trace"
    local STRING_TO_FIND="\"parent\":\"reservation-client\",\"child\":\"reservation-service\""
    local CURRENT_TIME=`python -c 'import time; print int(round(time.time() * 1000))'`
    local URL_TO_CALL="http://localhost:9411/api/v1/dependencies?endTs=$CURRENT_TIME"
    READY_FOR_TESTS="no"
    for i in $( seq 1 "${RETRIES}" ); do
        sleep "${WAIT_TIME}"
        echo -e "Sending a GET to $URL_TO_CALL . This is the response:\n"
        curl --fail "$URL_TO_CALL" | grep $STRING_TO_FIND &&  READY_FOR_TESTS="yes" && break
        echo "Fail #$i/${RETRIES}... will try again in [${WAIT_TIME}] seconds"
    done
    if [[ "${READY_FOR_TESTS}" == "yes" ]] ; then
        echo -e "\n\nSuccess! Zipkin is working fine!"
        return 0
    else
        echo -e "\n\nFailure...! Zipkin failed to store the trace!"
        return 1
    fi
}

export WAIT_TIME
export RETIRES
export SERVICE_PORT

export -f wait_for_app_to_boot_on_port
export -f curl_health_endpoint
export -f java_jar
export -f build_all_apps
export -f kill_app
export -f kill_all_apps
export -f check_app_presence_in_discovery
export -f send_test_request

ROOT_FOLDER=`pwd`
if [[ ! -e "${ROOT_FOLDER}/.git" ]]; then
    cd ..
    ROOT_FOLDER=`pwd`
    if [[ ! -e "${ROOT_FOLDER}/.git" ]]; then
        echo "No .git folder found"
        exit 1
    fi
fi

mkdir -p ${ROOT_FOLDER}/${BUILD_FOLDER}