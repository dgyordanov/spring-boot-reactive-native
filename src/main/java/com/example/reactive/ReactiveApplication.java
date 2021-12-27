package com.example.reactive;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;

import java.util.stream.Stream;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@SpringBootApplication
public class ReactiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReactiveApplication.class, args);
    }

    @Bean
    RouterFunction<ServerResponse> routes(CustomerRepository customerRepository) {
        return route()
                .GET("/customer", request -> {
                            customerRepository.save(new Customer(null, "Dido" + System.currentTimeMillis()));
                            return ok().body(
                                    customerRepository.findAll(),
                                    Customer.class
                            );
                        }
                ).build();
    }

    @Bean
    ApplicationRunner init(CustomerRepository repository, DatabaseClient client) {
        return args -> {
            client.sql("create table IF NOT EXISTS CUSTOMER" +
                    "(id int, name varchar(255));").fetch().first().subscribe();
            client.sql("DELETE FROM CUSTOMER;").fetch().first().subscribe();

            Stream<Customer> stream = Stream.of(
                    new Customer(1, "Dido 1"),
                    new Customer(2, "Dido 2"),
                    new Customer(3, "Dido 3"));

            repository.saveAll(Flux.fromStream(stream))
                    .then()
                    .subscribe(); // execute
        };
    }

}

interface CustomerRepository extends ReactiveCrudRepository<Customer, Integer> {
}

record Customer(Integer id, String name) {
}