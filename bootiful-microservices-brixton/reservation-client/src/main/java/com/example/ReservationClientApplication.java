package com.example;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.ribbon.proxy.annotation.Hystrix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.sleuth.Sampler;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@EnableCircuitBreaker
@EnableDiscoveryClient
@SpringBootApplication
@EnableBinding(Source.class)
public class ReservationClientApplication {

	@Bean
	Sampler sampler() {
		return () -> true;
	}

	public static void main(String[] args) {
		SpringApplication.run(ReservationClientApplication.class, args);
	}
}

@RestController
@RequestMapping("/reservations")
class ReservationApiGatewayRestController {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private Source source;

	public Collection<String> getReservationNamesFallback() {
		return new ArrayList<>();
	}

	@RequestMapping(method = RequestMethod.POST)
	public void write(@RequestBody Reservation r) {
		this.source.output().send(MessageBuilder.withPayload(r.getReservationName()).build());
	}

	@HystrixCommand(fallbackMethod = "getReservationNamesFallback")
	@RequestMapping(method = RequestMethod.GET, value = "/names")
	public Collection<String> getReservationNames() {
		return this.restTemplate
				.exchange("http://reservation-service/reservations", HttpMethod.GET, null, new ParameterizedTypeReference<Resources<Reservation>>() {
				})
				.getBody()
				.getContent()
				.stream()
				.map(Reservation::getReservationName)
				.collect(Collectors.toList());
	}
}


class Reservation {
	private Long id;
	private String reservationName;

	public Long getId() {
		return id;
	}

	public String getReservationName() {
		return reservationName;
	}
}