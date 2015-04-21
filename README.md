# Bootiful Microservices HOL

## Prerequisites

### Develop
- [Git](http://git-scm.com/downloads)
- [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [Maven](https://maven.apache.org/download.cgi)
- An IDE, for example [IntelliJ IDEA](https://www.jetbrains.com/idea/download/)
- [Spring Boot CLI](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#getting-started-installing-the-cli)

### Deploy and Manage
- Create a free account on [Pivotal Web Services](http://run.pivotal.io/)
- [Cloud Foundry CLI](https://github.com/cloudfoundry/cli/releases)

### Running the Examples 
To run everything, be sure to run `mvn clean install` in the `bootiful-applications/demo`, and `bootiful-microservices` folder. There's a script in the root called `build-all.sh`. Run that. It may take some time, so get some coffee. You can run each module using `java -jar target/module.jar` where `module.jar` is the name of the compiled `.jar` in the `target` directory. Alternatively, you can use `mvn spring-boot:run` in each module.

### Conference Wi-Fi & You
If you're coming to a conference, you should run the `build-all.sh` script (or at least the `mvn` commands in the script) before coming to the conference to avoid the wrath of the conference wi-fi. Also, import `bootiful-application/demo/pom.xml` and `bootiful-microservices/pom.xml` into your favorite IDE before relying on conference wi-fi. And, lastly, I'd run `mvn spring-boot:run` at least once just to force Maven to download everything.

