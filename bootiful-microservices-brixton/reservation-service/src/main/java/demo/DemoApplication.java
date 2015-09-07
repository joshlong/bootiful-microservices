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
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Arrays;
import java.util.Collection;

@EnableResourceServer
@EnableBinding(Sink.class)
@EnableDiscoveryClient
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }


    @Bean
    HealthIndicator healthIndicator() {
        return () -> Health.status("I <3 China!").build();

    }

    @Bean
    CommandLineRunner runner(ReservationRepository r) {
        return args -> {

            Arrays.asList("Rod,Juergen,Dr. Dave,Jennifer,Tom,Josh".split(","))
                    .forEach(s -> r.save(new Reservation(s)));

            r.findAll().forEach(System.out::println);

            r.findByReservationName("Juergen").forEach(System.out::println);
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
        this.log.debug ("accepted reservation for '" + reservationName + "'");
        this.reservationRepository.save(new Reservation(reservationName));
    }
}

interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Collection<Reservation> findByReservationName(String rn);
}

@RefreshScope
@RestController
class MessageRestController {

    @Value("${message}")
    private String message;

    @RequestMapping("/message")
    String msg() {
        return this.message;
    }
}

@RestController
class ReservationRestController {

    @Autowired
    private ReservationRepository reservationRepository;

    @RequestMapping("/reservations")
    Collection<Reservation> reservations() {
        return this.reservationRepository.findAll();
    }
}

@Entity
class Reservation {

    @Id
    @GeneratedValue
    private Long id;
    private String reservationName;

    Reservation() {  // why JPA why??
    }

    public Reservation(String reservationName) {
        this.reservationName = reservationName;
    }

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
