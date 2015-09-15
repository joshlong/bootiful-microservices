package demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.stream.Collectors;

@EnableOAuth2Sso
@EnableBinding(Source.class)
@IntegrationComponentScan
@EnableFeignClients
@EnableZuulProxy
@EnableDiscoveryClient
@SpringBootApplication
public class DemoApplication {

    @Bean
    AlwaysSampler alwaysSampler() {
        return new AlwaysSampler();
    }

    @Bean
    CommandLineRunner dc(DiscoveryClient dc) {
        return args ->
                dc.getInstances("reservation-service")
                        .forEach(si -> System.out.println(String.format("%s %s:%s", si.getServiceId(), si.getHost(), si.getPort())));
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}


@RestController
@RequestMapping("/reservations")
class ReservationEdgeController {

    @Output(Source.OUTPUT)
    @Autowired
    private MessageChannel messageChannel;

    @LoadBalanced
    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping(method = RequestMethod.POST)
    void write(@RequestBody Reservation reservationName) {
        this.messageChannel.send(MessageBuilder.withPayload(reservationName.getReservationName()).build());
    }

    @RequestMapping(value = "/names", method = RequestMethod.GET)
    Collection<String> readNames() {

        ParameterizedTypeReference<Resources<Reservation>> ptr =
                new ParameterizedTypeReference<Resources<Reservation>>() {
                };


        ResponseEntity<Resources<Reservation>> responseEntity = this.restTemplate.exchange(
                "http://reservation-service/reservations", HttpMethod.GET, null, ptr);

        return responseEntity.getBody()
                .getContent()
                .stream()
                .map(Reservation::getReservationName)
                .collect(Collectors.toList());
    }

}

class Reservation {
    public Long getId() {
        return id;
    }

    public String getReservationName() {
        return reservationName;
    }

    private Long id;
    private String reservationName;
}
