package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.sleuth.Sampler;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.stream.Stream;

@EnableBinding(Sink.class)
@EnableDiscoveryClient
@SpringBootApplication
@IntegrationComponentScan
public class ReservationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservationServiceApplication.class, args);
    }

    @Bean
    Sampler<?> sampler() {
        return new AlwaysSampler();
    }

    @Bean
    HealthIndicator healthIndicator() {
        return () -> Health.status("I <3 Spring!").build();
    }

    @Bean
    CommandLineRunner dummy(ReservationRepository rr) {
        return args -> {
<<<<<<< HEAD:bootiful-microservices-brixton/reservation-service/src/main/java/com/example/ReservationServiceApplication.java
            Stream.of("Scott", "Wayne", "Josh", "Vivian", "George", "Bob", "Viktor")
                    .forEach(n -> rr.save(new Reservation(n)));

            rr.findByReservationName("Wayne").forEach(System.out::println);
=======
            Arrays.asList("Dr. Rod, Dr. Syer, Juergen,  COMMUNITY, Josh".split(","))
                    .stream()
                    .map(String::trim)
                    .forEach(x -> rr.save(new Reservation(x)));
>>>>>>> 23341a290b3e2234652331e5349dc1eac1c0ff5f:bootiful-microservices-brixton/reservation-service/src/main/java/demo/DemoApplication.java
            rr.findAll().forEach(System.out::println);
        };
    }
}

@MessageEndpoint
class ReservationProcessor {

    @Autowired
    private ReservationRepository reservationRepository;

    @ServiceActivator(inputChannel = Sink.INPUT)
    public void acceptNewReservations(String rn) {
        this.reservationRepository.save(new Reservation(rn));
    }
}

@RefreshScope
@RestController
class MessageRestController {

    @Value("${message}")
    private String msg;

    @RequestMapping("/message")
    String msg() {
        return this.msg;
    }
}

@RepositoryRestResource
interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @RestResource(path = "by-name", rel = "by-name")
    Collection<Reservation> findByReservationName(@Param("rn") String rn);
}