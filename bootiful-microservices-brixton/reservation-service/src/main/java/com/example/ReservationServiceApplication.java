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
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Collection;
import java.util.stream.Stream;

@EnableBinding (Sink.class )
@EnableDiscoveryClient
@SpringBootApplication
public class ReservationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationServiceApplication.class, args);
	}
}

@MessageEndpoint
class ReservationProcessor {

	private final ReservationRepository reservationRepository ;

	@Autowired
	public ReservationProcessor(ReservationRepository reservationRepository) {
		this.reservationRepository = reservationRepository;
	}

	@ServiceActivator (inputChannel = "input")
	public void acceptNewReservations (String reservationName){
		this.reservationRepository.save(new Reservation(reservationName));
	}
}

@RefreshScope
@RestController
class MessageRestController {

	private final String message;

	@Autowired
	public MessageRestController(@Value("${message}") String message) {
		this.message = message;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/message")
	String read() {
		return this.message;
	}
}

@Component
class CustomHealthIndicator implements HealthIndicator {

	@Override
	public Health health() {
		return Health.status("I <3 Betfair!").build();
	}
}

@Component
class DummyCLR implements CommandLineRunner {

	private final ReservationRepository reservationRepository;

	@Autowired
	public DummyCLR(ReservationRepository reservationRepository) {
		this.reservationRepository = reservationRepository;
	}

	@Override
	public void run(String... strings) throws Exception {
		Stream.of("Tim", "Josh", "Jose", "Tasha", "Rod", "Spencer", "Dr. Suess", "Dr. Who")
				.forEach(n -> reservationRepository.save(new Reservation(n)));
		reservationRepository.findAll().forEach(System.out::println);
	}
}

@RepositoryRestResource
interface ReservationRepository extends JpaRepository<Reservation, Long> {

	@RestResource(path = "by-name")
	Collection<Reservation> findByReservationName(@Param("rn") String rn);
}


@Entity
class Reservation {

	@Id
	@GeneratedValue
	private Long id;  // id

	private String reservationName;  // reservation_name

	public Long getId() {
		return id;
	}

	public String getReservationName() {
		return reservationName;
	}

	@Override
	public String toString() {
		return "Reservation{" +
				"id=" + id +
				", reservationName='" + reservationName + '\'' +
				'}';
	}

	Reservation() {// why JPA why??
	}

	public Reservation(String reservationName) {

		this.reservationName = reservationName;
	}
}