package com.example;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.sun.org.apache.xpath.internal.operations.String;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.sleuth.Sampler;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@EnableBinding(Source.class)
@EnableZuulProxy
@EnableCircuitBreaker
@EnableDiscoveryClient
@SpringBootApplication
public class ReservationClientApplication {

    public static void main(java.lang.String[] args) {
        SpringApplication.run(ReservationClientApplication.class, args);
    }

    @Bean
    Sampler<?> sampler() {
        return new AlwaysSampler();
    }
}

@RestController
@RequestMapping("/reservations")
<<<<<<< HEAD:bootiful-microservices-brixton/reservation-client/src/main/java/com/example/ReservationClientApplication.java
class ReservationApiGateway {
=======
class ReservationApiGatewayRestController {
>>>>>>> 23341a290b3e2234652331e5349dc1eac1c0ff5f:bootiful-microservices-brixton/reservation-client/src/main/java/demo/DemoApplication.java

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    @Qualifier(Source.OUTPUT)
    private MessageChannel messageChannel;

    public Collection<String> getReservationNamesFallback() {
        return Collections.emptyList();
    }

    @RequestMapping(method = RequestMethod.POST)
    public void write(@RequestBody Reservation r) {
        this.messageChannel.send(MessageBuilder.withPayload(r.getReservationName()).build());
    }

    @HystrixCommand(fallbackMethod = "getReservationNamesFallback")
    @RequestMapping("/names")
    public Collection<String> getReservationNames() {

        ParameterizedTypeReference<Resources<Reservation>> ptr =
                new ParameterizedTypeReference<Resources<Reservation>>() {
                };
        return this.restTemplate
                .exchange("http://reservation-service/reservations", HttpMethod.GET, null, ptr)
                .getBody()
                .getContent()
                .stream()
                .map(Reservation::getReservationName)
                .collect(Collectors.toList());
    }
}


class Reservation {
    private Long id;
    private String reservationName;

    public Long getId() {
        return id;
    }

    public String getReservationName() {
        return reservationName;
    }
}













<<<<<<< HEAD:bootiful-microservices-brixton/reservation-client/src/main/java/com/example/ReservationClientApplication.java
=======
    @Override
    public String toString() {
        return "Reservation{" + "id=" + this.id +
                ", reservationName='" + this.reservationName + '\'' +
                '}';
    }
}
>>>>>>> 23341a290b3e2234652331e5349dc1eac1c0ff5f:bootiful-microservices-brixton/reservation-client/src/main/java/demo/DemoApplication.java
