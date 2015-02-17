Microservices
=============

-	start the script-y infrastructure pieces after you've introduced 'em'
-	`mkdir -p services/{contacts,bookmarks,gateway}/src/main/{resources,java}/demo` -
-	add the contact-service and bookmark-service code. Use following Maven pom.xml as template:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <packaging>jar</packaging>

    <parent>
        <groupId>oreilly</groupId>
        <artifactId>microservices-parent</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
    <artifactId>contacts</artifactId>

    <dependencies>
      <dependency>
          <groupId>org.springframework.cloud</groupId>
          <artifactId>spring-cloud-starter-hystrix</artifactId>
      </dependency>
      <dependency>
          <groupId>org.springframework.cloud</groupId>
          <artifactId>spring-cloud-starter-feign</artifactId>
      </dependency>
      <dependency>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-web</artifactId>
      </dependency>
      <dependency>
          <groupId>org.springframework.cloud</groupId>
          <artifactId>spring-cloud-starter-zuul</artifactId>
      </dependency>
      <dependency>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-data-jpa</artifactId>
      </dependency>
      <dependency>
          <groupId>org.springframework.cloud</groupId>
          <artifactId>spring-cloud-config-client</artifactId>
      </dependency>
      <dependency>
          <groupId>org.springframework.cloud</groupId>
          <artifactId>spring-cloud-starter-eureka</artifactId>
      </dependency>
      <dependency>
          <groupId>com.h2database</groupId>
          <artifactId>h2</artifactId>
      </dependency>
    </dependencies>

</project>


```

-	add initializers for the contact-service:

```java

    @Bean
  CommandLineRunner init(ContactRepository cr) {
    return args ->
      Arrays.asList("jlong,rwinch,dsyer,pwebb,sgibb".split(",")).forEach(
        userId -> Arrays.asList("Dave,Syer;Phil,Webb;Juergen,Hoeller".split(";"))
            .stream()
            .map(n -> n.split(","))
            .forEach(name -> cr.save(new Contact(
                userId, name[0], name[1], name[0].toLowerCase() + "@email.com"))));
  }
```

-	add initializers for the bookmark-service:

```java

  @Bean
  CommandLineRunner init(@Value("${bookmark.mask}") String bookmarkMask, BookmarkRepository br) {
    return args ->
        Arrays.asList("jlong,rwinch,dsyer,pwebb,sgibb".split(",")).forEach(userId -> {
          String href = String.format("http://%s-link.com", userId);
          String descriptionForBookmark = this.descriptionForBookmark(bookmarkMask, userId, href);
          br.save(new Bookmark(href, userId, descriptionForBookmark));
        });
  }
```

-	Add repository finder for contacts:

```java
   Collection<Contact> findByUserId(String userId);
```

-	Add repository finder for bookmarks:

```java
  Collection<Bookmark> findByUserId(String userId);
```

-	add REST service for contacts:

```java

@RestController
class ContactRestController {

  @RequestMapping("/{userId}/contacts")
  Collection<Contact> contacts(@PathVariable String userId) {
    return this.contactRepository.findByUserId(userId);
  }

  @Autowired
  private ContactRepository contactRepository;
}
```

-	add REST service for bookmarks:

```java


  @RestController
  class BookmarkRestController {

    @RequestMapping("/{userId}/bookmarks")
    Collection<Bookmark> bookmarks(@PathVariable String userId) {
      return this.bookmarkRepository.findByUserId(userId);
    }

    @Autowired
    private BookmarkRepository bookmarkRepository;
  }
```

-	show the REST services have data in 'em

-	theyre up and running. theyre benefitting from some basic config in the config-repo (theyre running on random ports ). the `spring-cloud-starter-config-client` is working!

-	use the `@RefreshScope` to make it so that i can change the bookmark mask!

-	register them in Eureka by just ading `@EnableDiscoveryClient` to each service. Restart.

-	Add the following dependencies to `gateway`:

```xml
          <dependency>
              <groupId>org.springframework.cloud</groupId>
              <artifactId>spring-cloud-starter-hystrix</artifactId>
          </dependency>
          <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
          <dependency>
              <groupId>org.springframework.cloud</groupId>
              <artifactId>spring-cloud-starter-feign</artifactId>
          </dependency>
          <dependency>
              <groupId>org.springframework.boot</groupId>
              <artifactId>spring-boot-starter-web</artifactId>
          </dependency>
          <dependency>
              <groupId>org.springframework.cloud</groupId>
              <artifactId>spring-cloud-starter-zuul</artifactId>
          </dependency>
          <dependency>
              <groupId>org.springframework.boot</groupId>
              <artifactId>spring-boot-starter-data-jpa</artifactId>
          </dependency>
          <dependency>
              <groupId>org.springframework.cloud</groupId>
              <artifactId>spring-cloud-config-client</artifactId>
          </dependency>
          <dependency>
              <groupId>org.springframework.cloud</groupId>
              <artifactId>spring-cloud-starter-eureka</artifactId>
          </dependency>
          <dependency>
              <groupId>com.h2database</groupId>
              <artifactId>h2</artifactId>
          </dependency>
```

-	add a simple configuration class  

```java


@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
@EnableCircuitBreaker
@EnableZuulProxy
public class PassportService {

  public static void main(String[] args) {
    SpringApplication.run(PassportService.class, args);
  }
}

```

-	add a simple CLR that demonstrates the Spring Cloud Commons / Eureka `DiscoveryClient`

```java


@Order(1)
@Component
class DiscoveryClientExample implements CommandLineRunner {

  @Autowired
  private DiscoveryClient discoveryClient;

  @Override
  public void run(String... strings) throws Exception {

    System.out.println( "DiscoveryClient Example");
   discoveryClient.getInstances("contact-service").forEach((ServiceInstance s) -> {
      System.out.println(ToStringBuilder.reflectionToString(s));
    });
    discoveryClient.getInstances("bookmark-service").forEach((ServiceInstance s) -> {
      System.out.println(ToStringBuilder.reflectionToString(s));
    });
  }
}
```

-	demonstrate using the `RestTemplate` & Ribbon/Eureka. Add `Bookmark` POJO

```java

@Order(2)
@Component
class RestTemplateExample implements CommandLineRunner {

  @Autowired
  private RestTemplate restTemplate;

  @Override
  public void run(String... strings) throws Exception {
    // use the "smart" Eureka-aware RestTemplate
    System.out.println( "RestTemplate Example");
    ParameterizedTypeReference<List<Bookmark>> responseType =
        new ParameterizedTypeReference<List<Bookmark>>() {
        };

    ResponseEntity<List<Bookmark>> exchange = this.restTemplate.exchange(
        "http://bookmark-service/{userId}/bookmarks",
        HttpMethod.GET, null, responseType, (Object) "pwebb");

    exchange.getBody().forEach(System.out::println);
  }
}

class Bookmark {
  private Long id;
  private String href, description, userId;

}

```

-	add a Feign example, including :

```java

@FeignClient("bookmark-service")
interface BookmarkClient {

  @RequestMapping(method = RequestMethod.GET,
      value = "/{userId}/bookmarks")
  Collection<Bookmark> getBookmarks(@PathVariable("userId") String userId);
}

@FeignClient("contact-service")
interface ContactClient {

  @RequestMapping(method = RequestMethod.GET, value = "/{userId}/contacts")
  Collection<Contact> getContacts(@PathVariable("userId") String userId);
}

@Order(3)
@Component
class FeignExample implements CommandLineRunner {

  @Autowired
  private ContactClient contactClient;

  @Autowired
  private BookmarkClient bookmarkClient;

  @Override
  public void run(String... strings) throws Exception {
    System.out.println( "Feign Example");
    this.bookmarkClient.getBookmarks("jlong").forEach(System.out::println);
    this.contactClient.getContacts("jlong").forEach(System.out::println);
  }
}

class Contact {
  private Long id;

  private String  userId, firstName, lastName, email;

}
```

-	build an API gateway, a *passport* service, that ties togther the various services. Demonstrate that i can 'exit' the contact service and itll do the right thing here in the integratier tier thanks ot the circuit breaker http://localhost:52657/pwebb/passport

```java

@Component
class IntegrationClient {

  @Autowired
  private ContactClient contactClient;

  @Autowired
  private BookmarkClient bookmarkClient;

  Collection<Bookmark> getBookmarksFallback(String userId) {
    System.out.println("getBookmarksFallback");
    return Arrays.asList();
  }

  @HystrixCommand(fallbackMethod = "getBookmarksFallback")
  Collection<Bookmark> getBookmarks(String userId) {
    return this.bookmarkClient.getBookmarks(userId);
  }

  Collection<Contact> getContactsFallback(String userId) {
    System.out.println("getContactsFallback");
    return Arrays.asList();
  }

  @HystrixCommand(fallbackMethod = "getContactsFallback")
  Collection<Contact> getContacts(String userId) {
    return this.contactClient.getContacts(userId);
  }
}

@RestController
class PassportRestController {

  @Autowired
  private IntegrationClient integrationClient;

  @RequestMapping("/{userId}/passport")
  Passport passport(@PathVariable String userId) {
    return new Passport(userId,
        this.integrationClient.getContacts(userId),
        this.integrationClient.getBookmarks(userId));
  }
}

class Passport {
  private String userId;
  private Collection<Bookmark> bookmarks;
  private Collection<Contact> contacts;
}

```

-	demo Zuul, as well `../bookmark-service/pwebb/bookmarks`
