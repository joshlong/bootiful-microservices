package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.stream.Stream;

@EnableBinding  ( Sink.class )
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
	public void acceptNewReservationsPlease (String reservationName) {
		this.reservationRepository.save(new Reservation(reservationName));
	}

}

@RepositoryRestResource
interface ReservationRepository extends JpaRepository <Reservation, Long> {

}

@RefreshScope
@RestController
class MessageRestController {

	@Value( "${message}")
	private String message ;

	@RequestMapping ("/message")
	String read(){
		return this.message ;
	}

}


@Component
class DummyCLR implements CommandLineRunner {

	private final ReservationRepository reservationRepository ;

	@Autowired
	public DummyCLR(ReservationRepository reservationRepository) {
		this.reservationRepository = reservationRepository;
	}

	@Override
	public void run(String... strings) throws Exception {
		Stream.of( "Adrian" , "Josh", "Emre" , "Ayse" , "Rod" ,"Dave", "Spencer" , "Adrian")
				.forEach(  n -> reservationRepository.save( new Reservation(n)));
		reservationRepository.findAll().forEach(System.out::println);
	}
}

@Entity
class Reservation {

	@Id
	@GeneratedValue
	private Long id ;

	private String reservationName;

	@Override
	public String toString() {
		return "Reservation{" +
				"id=" + id +
				", reservationName='" + reservationName + '\'' +
				'}';
	}

	public Reservation() {
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
}