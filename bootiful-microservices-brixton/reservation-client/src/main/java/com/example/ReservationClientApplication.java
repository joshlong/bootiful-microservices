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
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.Resources;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@EnableZuulProxy
@EnableFeignClients
@EnableCircuitBreaker
@EnableDiscoveryClient
@SpringBootApplication
@IntegrationComponentScan
@EnableBinding(ReservationChannels.class)
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

interface ReservationChannels {

	@Output
	MessageChannel output();
}

@MessagingGateway
interface ReservationWriter {

	@Gateway(requestChannel = "output")
	void write(String reservationName);
}

@FeignClient("reservation-service")
interface ReservationReader {

	@RequestMapping(value = "/reservations", method = RequestMethod.GET)
	Resources<Reservation> readReservations();
}

@RestController
@RequestMapping("/reservations")
class ReservationServiceApiGatewayRestController {

	@Autowired
	private ReservationReader reader;

	@Autowired
	private ReservationWriter writer;

	@RequestMapping(method = RequestMethod.POST)
	public void write(@RequestBody Reservation reservation) {
		/*
			MessageChannel output = this.channels.output();
			output.send(MessageBuilder.withPayload(reservation.getReservationName()).build());
		*/

		writer.write(reservation.getReservationName());
	}

	public Collection<String> fallback() {
		return new ArrayList<>();
	}

	@HystrixCommand(fallbackMethod = "fallback")
	@RequestMapping(method = RequestMethod.GET, value = "/names")
	public Collection<String> reservationNames() {
		return this.reader
				.readReservations()
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