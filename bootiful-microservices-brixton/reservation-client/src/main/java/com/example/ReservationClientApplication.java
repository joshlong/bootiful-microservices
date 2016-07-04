package com.example;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

interface ReservationClientChannels {

	@Output
	MessageChannel output();
}

@EnableBinding(ReservationClientChannels.class)
@EnableCircuitBreaker
@EnableZuulProxy
@EnableDiscoveryClient
@SpringBootApplication
public class ReservationClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationClientApplication.class, args);
	}

	@Bean
	@LoadBalanced
	RestTemplate restTemplate() {
		return new RestTemplate();
	}
}


@RestController
@RequestMapping("/reservations")
class ReservationApiGatewayRestController {

	private final RestTemplate restTemplate;

	private final MessageChannel reservationService;

	@Autowired
	public ReservationApiGatewayRestController(
			ReservationClientChannels channels,
			@LoadBalanced RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
		this.reservationService = channels.output();
	}


	@RequestMapping(method = RequestMethod.POST)
	public void write(@RequestBody Reservation reservation) {

		String reservationName = reservation.getReservationName();

		Message<String> msg = MessageBuilder
				.withPayload(reservationName)
				.build();

		this.reservationService.send(msg);
	}


	public Collection<String> fallback() {
		return new ArrayList<>();
	}

	// localhost:9999/reservations/names
	@HystrixCommand(fallbackMethod = "fallback")
	@RequestMapping(method = RequestMethod.GET, value = "/names")
	public Collection<String> names() {

		ParameterizedTypeReference<Resources<Reservation>> ptr =
				new ParameterizedTypeReference<Resources<Reservation>>() {
				};

		ResponseEntity<Resources<Reservation>> responseEntity =
				this.restTemplate.exchange("http://reservation-service/reservations",
						HttpMethod.GET,
						null,
						ptr
				);


		return responseEntity
				.getBody()
				.getContent()
				.stream()
				.map(Reservation::getReservationName)
				.collect(Collectors.toList());
	}
}

class Reservation {
	private String reservationName;

	public String getReservationName() {
		return reservationName;
	}
}