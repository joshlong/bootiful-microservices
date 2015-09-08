package demo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.springframework.http.MediaType;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collection;

@EnableBinding(Sink.class)
@EnableDiscoveryClient
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    Sampler<?> defaultSampler() {
        return new AlwaysSampler();
    }

    @Bean
    HealthIndicator healthIndicator() {
        return () -> Health.status("I <3 Ele.me! I'm hungry!").build();
    }

    @Bean
    CommandLineRunner runner(ReservationRepository repository) {
        return args -> {
            Arrays.asList("Josh,Henry,Lei,Jixi,Alex,Tony".split(","))
                    .forEach(x -> repository.save(new Reservation(x)));
            repository.findAll().forEach(System.out::println);
            repository.findByReservationName("Alex").forEach(System.out::println);
        };
    }
}

@MessageEndpoint
class ReservationRecorder {

    @Autowired
    private ReservationRepository reservationRepository;

    private Log log = LogFactory.getLog(getClass());

    @ServiceActivator(inputChannel = Sink.INPUT)
    public void acceptReservation(String reservationName) {
        this.log.debug("accepted reservation for '" + reservationName + "'");
        this.reservationRepository.save(new Reservation(reservationName));
    }
}


@RefreshScope
@RestController
class MessageRestController {

    @Value("${message}")
    private String message;

    @RequestMapping("/message")
    String message() {
        return this.message;
    }
}

@RestController
class ReservationRestController {

    @Autowired
    private ReservationRepository reservationRepository;

    @RequestMapping(method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE,
            value = "/reservations")
    Collection<Reservation> reservations() {
        return this.reservationRepository.findAll();
    }
}

interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Collection<Reservation> findByReservationName(String rn);
}

