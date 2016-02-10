package com.example;

import com.vaadin.annotations.Theme;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Grid;
import com.vaadin.ui.UI;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

@SpringBootApplication
public class ReservationServiceApplication {

	@Bean
	CommandLineRunner dummyCLR(ReservationRepository reservationRepository) {
		return args -> {
			Stream.of("Dave", "George", "Rod", "Mattias")
					.forEach(name -> reservationRepository.save(new Reservation(name)));
			reservationRepository.findAll().forEach(System.out::println);
		};
	}

	@Bean
	HealthIndicator healthIndicator() {
		return () -> Health.status("I <3 JFokus!").build();
	}

	public static void main(String[] args) {
		SpringApplication.run(ReservationServiceApplication.class, args);
	}
}

@Controller
class ReservationMvcController {

	@Autowired
	private ReservationRepository reservationRepository;

	@RequestMapping("/reservations.php")
	String mvcPage(Model model) {
		model.addAttribute("reservations", this.reservationRepository.findAll());
		return "reservations";
	}

}

@SpringUI(path = "/ui")
@Theme("valo")
class ReservationUI extends UI {

	@Autowired
	private ReservationRepository reservationRepository ;

	@Override
	protected void init(VaadinRequest request) {
		Grid g = new Grid(new BeanItemContainer<>(Reservation.class,
				this.reservationRepository.findAll()));
		g.setSizeFull();
		setContent(g);
	}
}

@RestController
class BasicRestController {

	@RequestMapping(method = RequestMethod.GET, value = "/hi/{name}")
	Map<String, Object> hi(@PathVariable String name) {
		return Collections.singletonMap("greeting", "Hi " + name + "!");
	}
}

@RepositoryRestResource
interface ReservationRepository extends JpaRepository<Reservation, Long> {

	@RestResource(path = "by-name")
	Collection<Reservation> findByReservationName(@Param("rn") String rn);
}