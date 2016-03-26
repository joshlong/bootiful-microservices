package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

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
	HealthIndicator paris() {
		return () -> Health.status("je suis Paris!").build();
	}

	@Bean
	CommandLineRunner start(ReservationRepository reservationRepository) {
		return args -> {
			Stream.of("Marie", "Manyee", "Olivier", "Patrick", "Pierre", "William")
					.forEach(name -> reservationRepository.save(new Reservation(name)));
			reservationRepository.findAll().forEach(System.out::println);
		};
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
	public void acceptNewReservations(String reservationName) {
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

@RepositoryRestResource
interface ReservationRepository extends JpaRepository<Reservation, Long> {

	// select * from reservations where reservation_name = :rn
	@RestResource(path = "by-name")
	Collection<Reservation> findByReservationName(@Param("rn") String rn);
}

@Entity
class Reservation {  // RESERVATIONS

	@Id // unique key index
	@GeneratedValue // auto incrementing
	private Long id; // ID

	private String reservationName; // RESERVATION_NAME

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

	Reservation() {// pourquoi JPA pourquoi???
	}

	public Reservation(String reservationName) {

		this.reservationName = reservationName;
	}
}