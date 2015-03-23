# Bootiful Production and Operations with Cloud Foundry

## Intro
In this section we'll look at using Cloud Foundry, an open-source PaaS from Pivotal, to make short work of managing the application in production.


- we'll demonstrate how to tear an app and associated services down if they already exist
- we'll then push our application to Cloud Foundry, a hosted Platform-as-a-Service
- we'll use ElephantSQL to provision a PostgresSQL database and then bind it to our application. Auto-reconfiguration will take over from there.
- we'll then use Cloud Foundry easy auto-scale features to scale an application up-and-out
- we'll watch Cloud Foundry provide us with auto-healing by invoking the `/killme` endpoint that we've added to our application and then watching it go from 2/3 instances to 3/3
- we'll demonstrate that we can easily also connect to any of our backing services by going to the run.pivotal.io dashboard or just using the API.
- we'll add application performance monitoring by adding New Relic
- we'll add a hosted logging drain, [PaperTrail](https://papertrailapp.com/systems/CloudFoundry/events)
- then we'll look at how Cloud Foundry can give us a `manifest.yml` so that all of our steps need not be repeated.
- we'll _also_ look at how `cf oauth-token` can give us an access-token that you can plugin to, for example, the Java CF Client API to programatically do everything we've just done.



## Running the Example
This example uses H2 and so should run locally. Use `mvn spring-boot:run` for the `demo` module or simply run the application in your IDE using `public static void main`.
