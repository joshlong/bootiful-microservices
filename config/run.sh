export SPRING_APPLICATION_NAME=config-service
export SERVER_PORT=8888
export SPRING_CLOUD_CONFIG_SERVER_GIT_URI="file://`PWD`/../config-repo"

spring run script.groovy
