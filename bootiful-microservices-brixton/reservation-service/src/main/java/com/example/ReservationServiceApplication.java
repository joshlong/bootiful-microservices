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
import org.springframework.context.annotation.Bean;
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

@EnableBinding(Sink.class)
@EnableDiscoveryClient
@SpringBootApplication
public class ReservationServiceApplication {

	@Bean
	HealthIndicator healthIndicator() {
		return () -> Health.status("I <3 Denver!!").build();
	}

	public static void main(String[] args) {
		SpringApplication.run(ReservationServiceApplication.class, args);
	}
}

@MessageEndpoint
class ReservationProcessor {

	@Autowired
	private ReservationRepository reservationRepository;

	@ServiceActivator(inputChannel = "input")
	public void acceptNewReservation(String reservationName) {
		this.reservationRepository.save(new Reservation(reservationName));
	}
}

@RestController
@RefreshScope
class MessageRestController {

	@Value("${message}")
	private String message;

	@RequestMapping(method = RequestMethod.GET, value = "/message")
	String readMessage() {
		return this.message;
	}
}


@Component
class DummyCLR implements CommandLineRunner {

	@Autowired
	private ReservationRepository reservationRepository;

	@Override
	public void run(String... args) throws Exception {
		Stream.of("Lokesh", "Robert", "Josh", "Chad", "Chad", "Sean")
				.forEach(name -> reservationRepository.save(new Reservation(name)));
		reservationRepository.findAll().forEach(System.out::println);
	}
}

@RepositoryRestResource
interface ReservationRepository extends JpaRepository<Reservation, Long> {

	// select * from reservations where reservation_name = :rn
	@RestResource(path = "by-name")
	Collection<Reservation> findByReservationName(@Param("rn") String rn);

}

@Entity
class Reservation { // reservations

	@Id
	@GeneratedValue
	private Long id;  // id

	@Override
	public String toString() {
		return "Reservation{" +
				"id=" + id +
				", reservationName='" + reservationName + '\'' +
				'}';
	}

	Reservation() {// why JPA why???
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

	private String reservationName; // reservation_name


}