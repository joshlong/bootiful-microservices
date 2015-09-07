package demo;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.sleuth.Sampler;
import org.springframework.cloud.sleuth.Trace;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@EnableZuulProxy
@SpringBootApplication
@EnableBinding(Source.class)
@IntegrationComponentScan
@EnableCircuitBreaker
@EnableFeignClients
@EnableDiscoveryClient
public class DemoApplication {

    @Bean
    Sampler<?> defaultSampler() {
        return new AlwaysSampler();
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}

class Reservation {

    private Long id;
    private String reservationName;

    public Long getId() {
        return id;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Reservation{");
        sb.append("id=").append(id);
        sb.append(", reservationName='").append(reservationName).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public String getReservationName() {
        return reservationName;
    }
}


@Component
class ReservationIntegration {


    @Autowired
    private Trace trace;

    public Collection<String> getReservationNamesFallback() {
        return Collections.emptyList();
    }

    private Collection<Reservation> getReservations() {
        return this.restTemplate.exchange(
                "http://reservation-service/reservations",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Reservation>>() {
                }
        ).getBody();
    }

    @Autowired
    private RestTemplate restTemplate;

    @HystrixCommand(fallbackMethod = "getReservationNamesFallback")
    public Collection<String> getReservationNames() {
        this.trace.startSpan("getReservationNames", new AlwaysSampler(), null);
        List<String> collect = getReservations()
                .stream()
                .map(Reservation::getReservationName)
                .collect(Collectors.toList());
        this.trace.addAnnotation("names", Integer.toString(collect.size()));
        return collect;
    }
}

// todo this just doesn't want to play nice!
/*
@FeignClient("reservation-service")
interface ReservationReader {

    @RequestMapping(method = RequestMethod.GET, value = "/reservations")
    Collection<Reservation> getReservation();
}
*/

@MessagingGateway(name = "reservation")
interface ReservationWriter {

    @Gateway(requestChannel = Source.OUTPUT)
    void makeReservation(String reservationName);
}

@RestController
@RequestMapping(value = "/reservations")
class ReservationRestController {

    @Autowired
    private ReservationWriter reservationWriter;

    @Autowired
    private ReservationIntegration integration;

    @RequestMapping(consumes = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    void write(@RequestBody Reservation reservation) {
        this.reservationWriter.makeReservation(reservation.getReservationName());
    }

    @RequestMapping(method = RequestMethod.GET, value = "/names")
    Collection<String> readNames() {
        return this.integration.getReservationNames();
    }
}



















