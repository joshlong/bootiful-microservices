package com.example;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.Resources;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
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


interface ReservationChannels {

	@Output
	MessageChannel output();
}


@FeignClient("reservation-service")
interface ReservationReader {

	@RequestMapping (method = RequestMethod.GET, value = "/reservations")
	Resources<Reservation> read();
}

@MessagingGateway
interface ReservationWriter {

	@Gateway(requestChannel = Source.OUTPUT)
	void write(String reservationName);
}

@EnableFeignClients
@EnableZuulProxy
@EnableBinding(ReservationChannels.class)
@EnableCircuitBreaker
@EnableDiscoveryClient
@IntegrationComponentScan
@SpringBootApplication
public class ReservationClientApplication {

	@Bean
	@LoadBalanced
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	public static void main(String[] args) {
		SpringApplication.run(ReservationClientApplication.class, args);
	}
}

@RestController
@RequestMapping("/reservations")
class ReservationApiGatewayRestController {

	private final RestTemplate restTemplate;
	private final MessageChannel output;
	private final ReservationReader reader;
	private final ReservationWriter writer;

	@Autowired
	public ReservationApiGatewayRestController(RestTemplate restTemplate,
	                                           ReservationChannels channels,
	                                           ReservationReader reader,
	                                           ReservationWriter writer) {
		this.restTemplate = restTemplate;
		this.output = channels.output();
		this.writer = writer;
		this.reader = reader;
	}

	public Collection<String> fallback() {
		return new ArrayList<>();
	}


	@RequestMapping(method = RequestMethod.POST)
	public void write(@RequestBody Reservation r) {
		// this.output.send(MessageBuilder.withPayload(r.getReservationName()).build());
		this.writer.write(r.getReservationName());
	}

	@HystrixCommand(fallbackMethod = "fallback")
	@RequestMapping(method = RequestMethod.GET, value = "/names")
	public Collection<String> names() {

		/*

		ParameterizedTypeReference<Resources<Reservation>> ptr =
				new ParameterizedTypeReference<Resources<Reservation>>() { };

		ResponseEntity<Resources<Reservation>> responseEntity = this.restTemplate.exchange(
				"http://reservation-service/reservations",
				HttpMethod.GET,
				null,
				ptr);

		*/

		return reader
				.read()
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