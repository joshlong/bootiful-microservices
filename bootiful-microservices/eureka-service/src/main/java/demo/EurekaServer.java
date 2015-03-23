package demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.annotation.Bean;

@EnableEurekaServer
@SpringBootApplication
public class EurekaServer {

    @Bean
    CommandLineRunner run(@Value("${message}") String msg) {
        return args -> System.out.println("message = " + msg);
    }

    public static void main(String[] args) {
        SpringApplication.run(EurekaServer.class, args);
    }
}