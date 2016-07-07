#!/bin/bash
set -e

#
# the big CloudFoundry installer
#

CLOUD_DOMAIN=${DOMAIN:-run.pivotal.io}
CLOUD_TARGET=api.${DOMAIN}


CF_USER=${1:-$CF_USER}
CF_PASSWORD=${2:-$CF_PASSWORD}
CF_ORG=${3:-$CF_ORG}
CF_SPACE=${4:-$CF_SPACE}


RESERVATIONS_DB=reservations-postgresql
RESERVATIONS_MQ=reservations-rabbitmq



## COMMON

function init_services(){
    cf cs cloudamqp lemur $RESERVATIONS_MQ
    cf cs elephantsql turtle $RESERVATIONS_DB
}

function install_cf(){
    mkdir -p $HOME/bin
    curl -v -L -o cf.tgz 'https://cli.run.pivotal.io/stable?release=linux64-binary&version=6.15.0&source=github-rel'
    tar zxpf cf.tgz
    mkdir -p $HOME/bin && mv cf $HOME/bin
}

function validate_cf(){

    cf  -v || install_cf

    export PATH=$PATH:$HOME/bin

    cf api https://api.run.pivotal.io
    cf auth $CF_USER "$CF_PASSWORD" && \
    cf target -o $CF_ORG -s $CF_SPACE && \
    cf apps
}

function login(){
    cf api | grep ${CLOUD_TARGET} || cf api ${CLOUD_TARGET} --skip-ssl-validation
    cf a | grep OK || cf login
}

function app_domain(){
    D=`cf apps | grep $1 | tr -s ' ' | cut -d' ' -f 6 | cut -d, -f1`
    echo $D
}

function deploy_app(){
    APP_NAME=$1
    echo "APP_NAME=$APP_NAME"
    cd $APP_NAME
    cf push ${APP_NAME}
    #APPLICATION_DOMAIN="`app_domain $APP_NAME`"
    #echo "APPLICATION_DOMAIN=$APPLICATION_DOMAIN"
    #cf env $APP_NAME | grep APPLICATION_DOMAIN || cf set-env $APP_NAME APPLICATION_DOMAIN $APPLICATION_DOMAIN
    #cf restart $APP_NAME
    cd ..
}

function deploy_service(){
    N=$1
    D=`app_domain $N`
    JSON='{"uri":"http://'$D'"}'
    echo cf cups $N  -p $JSON
    cf cups $N -p $JSON
}

function deploy_config_service(){
    NAME=config-service
    deploy_app $NAME
    deploy_service $NAME
}

function deploy_eureka_service(){
    NAME=eureka-service
    deploy_app $NAME
    deploy_service $NAME
}

function deploy_hystrix_dashboard(){
    deploy_app hystrix-dashboard
}

function deploy_zipkin_service(){
    deploy_app zipkin-service
}

function deploy_reservation_service(){
    deploy_app reservation-service
}

function deploy_reservation_client(){
    deploy_app reservation-client
}

function reset(){

    echo "reset.."
    apps="hystrix-dashboard reservation-client reservation-service eureka-service config-service zipkin-service"
    apps_arr=( $apps )
    for a in "${apps_arr[@]}";
    do
         echo $a
         cf d -f $a
    done

    services="${RESERVATIONS_DB} ${RESERVATIONS_MQ} zipkin-service eureka-service config-service"
    services_arr=( $services )
    for s in "${services_arr[@]}";
    do
        echo $s
        cf ds -f $s
    done

    cf delete-orphaned-routes -f

}

###
### INSTALLATION STEPS
###

mvn -DskipTests=true clean install

validate_cf
login
reset
init_services
deploy_config_service
deploy_eureka_service
deploy_zipkin_service
deploy_reservation_service
deploy_reservation_client
deploy_hystrix_dashboard