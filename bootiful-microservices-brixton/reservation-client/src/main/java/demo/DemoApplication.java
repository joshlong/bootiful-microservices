package demo;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.sleuth.Sampler;
import org.springframework.cloud.sleuth.Trace;
import org.springframework.cloud.sleuth.TraceScope;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
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

@EnableBinding(Source.class)
@IntegrationComponentScan
@EnableZuulProxy
@EnableFeignClients
@EnableDiscoveryClient
@EnableCircuitBreaker
//@EnableOAuth2Client // todo @EnableOAuth2Sso
@EnableResourceServer
@SpringBootApplication
public class DemoApplication {



    @Bean
    Sampler<?> defaultSampler() {
        return new AlwaysSampler();
    }

    @Bean
    CommandLineRunner dc(DiscoveryClient dc) {
        return args ->
                dc.getInstances("reservation-service")
                        .forEach(si -> System.out.println(
                                String.format("%s %s:%s", si.getServiceId(), si.getHost(), si.getPort())
                        ));
    }
/*

    @Bean
    CommandLineRunner rt(RestTemplate rt) {
        return args -> {

            ParameterizedTypeReference<List<Reservation>> ptr =
                    new ParameterizedTypeReference<List<Reservation>>() {
                    }; // type token

            ResponseEntity<List<Reservation>> responseEntity =
                    rt.exchange("http://reservation-service/reservations", HttpMethod.GET, null, ptr);

            responseEntity.getBody().forEach(System.out::println);
        };
    }
*/


 /*   @Bean
    CommandLineRunner fc(ReservationReader reservationReader,
                         Trace trace) {
        return args -> {

            try (TraceScope scope = trace.startSpan("getReservationNamesCLR",
                    new AlwaysSampler(), null)) {
                List<String> collect = reservationReader
                        .getReservations()
                        .stream().map(Reservation::getReservationName)
                        .collect(Collectors.toList());
                trace.addAnnotation("names", Integer.toString(collect.size()));
                collect.forEach(System.out::println);
            }
        };
    }
*/
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}


@FeignClient("reservation-service")
interface ReservationReader {

    @RequestMapping(method = RequestMethod.GET, value = "/reservations")
    Collection<Reservation> getReservations();
}

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

@Component
class ReservationIntegration {

    @Autowired
    private ReservationReader reservationReader;

    public Collection<String> getReservationNamesFallback() {
        return Collections.emptyList();
    }

    @HystrixCommand(fallbackMethod = "getReservationNamesFallback")
    public Collection<String> getReservationNames() {
        return reservationReader.getReservations()
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Reservation{");
        sb.append("id=").append(id);
        sb.append(", reservationName='").append(reservationName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}