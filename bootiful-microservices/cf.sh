#!/bin/bash
set -e

#
# the big CloudFoundry installer
#

CLOUD_DOMAIN=${DOMAIN:-run.pivotal.io}
CLOUD_TARGET=api.${DOMAIN}
BUS_SERVICE=bus-rabbitmq

function login(){
    cf api | grep ${CLOUD_TARGET} || cf api ${CLOUD_TARGET} --skip-ssl-validation
    cf a | grep OK || cf login
}

function app_domain(){
    D=`cf apps | grep $1 | tr -s ' ' | cut -d' ' -f 6 | cut -d, -f1`
    echo $D
}

### common
function deploy_cli_app(){
    cf cs cloudamqp tiger $BUS_SERVICE

    APP_NAME=$1
    cd $APP_NAME
    mkdir -p target
    spring jar target/$APP_NAME.jar script.groovy
    cd target
    cf push $APP_NAME --no-start
    APPLICATION_DOMAIN=`app_domain $APP_NAME`
    echo determined that application_domain for $APP_NAME is $APPLICATION_DOMAIN.
    cf env $APP_NAME | grep APPLICATION_DOMAIN || cf set-env $APP_NAME APPLICATION_DOMAIN $APPLICATION_DOMAIN
    cf restart $APP_NAME
    cd ..
}

function deploy_app(){
    # common to all nodes
    cf cs cloudamqp tiger $BUS_SERVICE

    APP_NAME=$1
    cd $APP_NAME
    cf push $APP_NAME  --no-start
    APPLICATION_DOMAIN=`app_domain $APP_NAME`
    echo determined that application_domain for $APP_NAME is $APPLICATION_DOMAIN.
    cf env $APP_NAME | grep APPLICATION_DOMAIN || cf set-env $APP_NAME APPLICATION_DOMAIN $APPLICATION_DOMAIN
    cf restart $APP_NAME
    cd ..
}

function deploy_service(){
    N=$1
    D=`app_domain $N`
    JSON='{"uri":"http://'$D'"}'
    echo cf cups $N  -p $JSON
    cf cups $N -p $JSON
}

function deploy_configuration_service(){
    NAME=configuration-service
    deploy_app $NAME
    deploy_service $NAME
}

function deploy_configuration_service_cli(){
    NAME=configuration-service
    echo $NAME-cli
    deploy_cli_app $NAME-cli
    deploy_service $NAME
}

function deploy_eureka_service(){
    NAME=eureka-service
    deploy_app $NAME
    deploy_service $NAME
}

function deploy_eureka_service_cli(){
    NAME=eureka-service
    deploy_cli_app $NAME-cli
    deploy_service $NAME
}

function deploy_dashboard_service(){
    deploy_app dashboard-service
}

function deploy_contact_service(){
    cf cs elephantsql turtle contacts-postgresql
    deploy_app contact-service
}

function deploy_bookmark_service(){
    cf cs elephantsql turtle bookmarks-postgresql
    deploy_app bookmark-service
}

function deploy_passport_service(){
    deploy_app passport-service
}

function reset(){

    echo "reset.."
    apps="bookmark-service configuration-service contact-service dashboard-service eureka-service passport-service"
    apps_arr=( $apps )
    for a in "${apps_arr[@]}";
    do
         echo $a
         cf d -f $a
    done

    services="bookmarks-postgresql bus-rabbitmq configuration-service contacts-postgresql eureka-service"
    services_arr=( $services )
    for s in "${services_arr[@]}";
    do
        echo $s
        cf ds -f $s
    done
}

###
### INSTALLATION STEPS
###

mvn -DskipTests=true clean install

#login
#reset
#deploy_configuration_service
#deploy_eureka_service
deploy_dashboard_service
#deploy_contact_service
#deploy_bookmark_service
#deploy_passport_service
