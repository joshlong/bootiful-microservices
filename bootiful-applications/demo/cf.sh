#!/bin/sh


APP_NAME="bootiful-applications"
DB_SVC_NAME="$APP_NAME-postgresql"
NEWRELIC_SVC_NAME="$APP_NAME-newrelic"
PAPERTRAIL_LOGS_SVC_NAME="$APP_NAME-papertrail-logs"

# tear app and service down if they already exist
cf delete -f $APP_NAME
cf delete-service -f $DB_SVC_NAME
cf delete-service -f $NEWRELIC_SVC_NAME
cf delete-service -f $PAPERTRAIL_LOGS_SVC_NAME

# push the app to the cloud
cf push -p target/demo-0.0.1-SNAPSHOT.jar --random-route $APP_NAME

# give it a backing service
cf services | grep $DB_SVC_NAME || cf create-service elephantsql turtle $DB_SVC_NAME

# bind it to the app
cf bind-service $APP_NAME $DB_SVC_NAME
cf restage $APP_NAME

# scale it
cf scale -i 3 -f $APP_NAME # our free turtle tier PG DB only handles 5 at a time

# watch it auto-heal
URI="`cf a | grep $APP_NAME | tr " " "\n" | grep cfapps.io`"
curl http://$URI/killme
# now watch 'cf apps' reflect auto-healing

# connect to DB
DB_URI=`cf env $APP_NAME | grep postgres: | cut -f2- -d:`;
echo $DB_URI

# lets add New Relic APM
cf create-service newrelic standard $NEWRELIC_SVC_NAME
cf bind-service $APP_NAME $NEWRELIC_SVC_NAME
cf restage $APP_NAME

# lets add a PaperTrail log drain - see https://papertrailapp.com/systems/CloudFoundry/events
PAPERTRAIL_LOG_URL="logs2.papertrailapp.com:49046"
cf create-user-provided-service $PAPERTRAIL_LOGS_SVC_NAME -l syslog://$PAPERTRAIL_LOG_URL
cf bind-service $APP_NAME $PAPERTRAIL_LOGS_SVC_NAME
cf restage $APP_NAME

# make sure we can get back here again
cf create-app-manifest $APP_NAME

# how do we control everything programatically?
echo the OAuth token is `cf oauth-token`
