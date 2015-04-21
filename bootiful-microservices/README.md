# Bootiful Microservices with Spring Cloud

## Intro
In this section we look at how Spring Cloud reduces complexity introduced in standing up new services.

- we'll look at service registration and discovery using Eureka (or another service registry like Consul)
- we'll look at centralized configuration using the Spring Cloud Configuration Server
- we'll look at smarter service-to-service invocations using Netflix's Feign, Hystrix, and Ribbon

## Pre-Requisites
See the parent [README](../README.md) for pre-requisites.

## Running the Examples
This example uses H2 and so should run locally. Use `mvn spring-boot:run` for the `demo` module or simply run the application in your IDE using the `public static void main` class.

You can deploy everything to a Cloud Foundry-based Platform-as-a-Service using the provided `cf.sh` script.
