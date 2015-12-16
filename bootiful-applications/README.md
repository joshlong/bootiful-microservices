# Bootiful Applications with Spring Boot



## Intro
In this section we'll look at using Spring Boot to make short work of building individual applications and services.

- we'll look at how easily Spring Boot can talk to a backend database
- we'll look at Spring Boot's support for building web applications and REST services
- we'll look at how to monitor and make production-ready a service using Spring Boot Actuator
- we'll look at how Spring Boot supports various degrees of configuration and use that configuration to change things in the application (like `endpoints.env.sensitive` and `spring.jpa.generate-ddl`) and change things  (like `server.port`, `management.port`, and `management.contextPath`) externally using `-D` arguments and environment variables.
- we'll look at how Boot applications can be deployed
- we'll look at other ways to customize Spring Boot with DI  
- we'll look at Spring Boot's auto-configuration at more length 

## Running the Example
This example uses H2 and so should run locally. Use `mvn spring-boot:run` for the `demo` module or simply run the application in your IDE using `public static void main`.
