package demo;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@EnableFeignClients
@EnableDiscoveryClient
@EnableCircuitBreaker
@EnableZuulProxy
@SpringBootApplication
public class DemoApplication {

    @Bean
    CommandLineRunner dc(DiscoveryClient dc) {
        return args ->
                dc.getInstances("reservation-service")
                        .forEach(si -> System.out.println(
                                si.getHost() + ':' + si.getPort()));
    }

    @Bean
    CommandLineRunner rt(RestTemplate restTemplate) {
        return args -> {
            ParameterizedTypeReference<List<Reservation>> ptr
                    = new ParameterizedTypeReference<List<Reservation>>() {
            };

            List<Reservation> reservations = restTemplate.exchange(
                    "http://reservation-service/reservations",
                    HttpMethod.GET, null, ptr).getBody();

            reservations.forEach(System.out::println);
        };
    }

    @Bean
    CommandLineRunner feign(ReservationsRestClient client) {
        return args ->
                client.getReservations().forEach(System.out::println);
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}


@Component
class ReservationIntegration {

    @Autowired
    private ReservationsRestClient reservationsRestClient;

    public Collection<String> getReservationNamesFallback() {
        return Collections.emptyList();
    }

    @HystrixCommand(fallbackMethod = "getReservationNamesFallback")
    public Collection<String> getReservationNames() {
        return reservationsRestClient.getReservations()
                .stream()
                .map(Reservation::getReservationName)
                .collect(Collectors.toList());
    }

}

@RestController
@RequestMapping ("/reservations")
class ReservationNamesRestController {

    @RequestMapping("/names")
    Collection<String> rs() {
        return this.reservationIntegration.getReservationNames();
    }

    @Autowired
    private ReservationIntegration reservationIntegration;

}


@FeignClient("reservation-service")
interface ReservationsRestClient {

    @RequestMapping(value = "/reservations", method = RequestMethod.GET)
    Collection<Reservation> getReservations();
}

class Reservation {

    private Long id;
    private String reservationName;

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", reservationName='" + reservationName + '\'' +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReservationName() {
        return reservationName;
    }

    public void setReservationName(String reservationName) {
        this.reservationName = reservationName;
    }
}
