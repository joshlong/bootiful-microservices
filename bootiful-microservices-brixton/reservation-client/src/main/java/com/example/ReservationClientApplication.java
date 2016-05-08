package com.example;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.DiscoveryClient;
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


@EnableBinding(ReservationServiceChannels.class)
@EnableFeignClients
@EnableZuulProxy
@IntegrationComponentScan
@EnableCircuitBreaker
@EnableDiscoveryClient
@SpringBootApplication
public class ReservationClientApplication {


	@Bean
	CommandLineRunner discover(DiscoveryClient discoveryClient) {
		return args ->
				discoveryClient.getInstances("reservation-service")
						.forEach(si -> System.out.println(
								String.format("(%s) %s:%s", si.getServiceId(), si.getHost(), si.getPort())));
	}

	@Bean
	@LoadBalanced
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	public static void main(String[] args) {
		SpringApplication.run(ReservationClientApplication.class, args);
	}

}

interface ReservationServiceChannels {

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

	@RequestMapping(method = RequestMethod.GET, value = "/reservations")
	Resources<Reservation> readReservations();
}

@RestController
@RequestMapping("/reservations")
class ReservationApiGatewayRestController {

	private final RestTemplate restTemplate;
	private final ReservationReader reader;
	private final MessageChannel output;
	private final ReservationWriter writer;

	@Autowired
	public ReservationApiGatewayRestController(RestTemplate r,
	                                           ReservationReader reservationReader,
	                                           ReservationServiceChannels channels,
	                                           ReservationWriter writer
	) {
		this.restTemplate = r;
		this.output = channels.output();
		this.writer = writer;
		this.reader = reservationReader;
	}

	@RequestMapping(method = RequestMethod.POST)
	public void write(@RequestBody Reservation r) {
		/*Message<String> msg = MessageBuilder.withPayload(r.getReservationName()).build();
		this.output.send(msg);*/
		this.writer.write(r.getReservationName());
	}


	public Collection<String> myCustomFallbackPlease() {
		return new ArrayList<>();
	}

	@HystrixCommand(fallbackMethod = "myCustomFallbackPlease")
	@RequestMapping(method = RequestMethod.GET, value = "/names")
	public Collection<String> names() {

	/*	ParameterizedTypeReference <Resources<Reservation>> ptr =
				new ParameterizedTypeReference<Resources<Reservation>>() { };

		ResponseEntity<Resources<Reservation>> responseEntity = this.restTemplate.exchange(
				"http://reservation-service/reservations",
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
*/
		return reader
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