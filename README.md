# Bootiful Microservices HOL

## Prerequisites

### Develop
- [Git](http://git-scm.com/downloads)
- [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [Spring Boot CLI](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#getting-started-installing-the-cli)

### Deploy and Manage
- Create a free account on [Pivotal Web Services](http://run.pivotal.io/)
- install the [Cloud Foundry CLI](https://github.com/cloudfoundry/cli/releases)

### Running the Examples

To follow the simple Spring Boot CLI example:

- in a new file called `hi.groovy` type:

```
@RestController
class GreetingsRestController {

  @RequestMapping("/hi/{name}")
  def hi(@PathVariable String name){
    [ greeting : "Hello, "+name+"!" ]
  }
}
```
- from the terminal in the same directory as the newly created `hi.groovy`, run `spring jar hi.jar hi.groovy`
- you'll be given a `.jar` that you can execute: `java -jar hi.jar`

### Pushing to Cloud Foundry

- `cf login` to ensure that you've authenticated against your Pivotal Web Services account. My session looked like this, yours will feature your own Cloud Foundry credentials.
```
> cf login

API endpoint> api.run.pivotal.io

Email> MY_EMAIL@HOST.com

Password>
Authenticating...
OK

Select an org (or press enter to skip):
1. platform-eng
2. codecafe

Org> 1
Targeted org platform-eng

Targeted space joshlong


API endpoint:   https://api.run.pivotal.io (API version: 2.44.0)
User:           MY_EMAIL@HOST.com
Org:            platform-eng
Space:          joshlong

```
- `cf push -p hi.jar SOME_NAME_YOU_MAKEUP_HERE` - the `SOME_NAME_YOU_MAKEUP_HERE` is arbitrary; it'll inform the URL that's used to mount the application and as such it shares a shared global (DNS) namespace
