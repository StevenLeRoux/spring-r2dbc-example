package com.opensource.dada.r2db;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.postgresql.api.PostgresqlResult;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import org.reactivestreams.Publisher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@SpringBootApplication
public class SpringR2dbExampleApplication {

	public static Integer counter = 0;

	public static void main(String[] args) {
		SpringApplication.run(SpringR2dbExampleApplication.class, args);
	}

	@Bean
	public CommandLineRunner run(ConnectionFactory connectionFactory, String theServiceName){
		return new CommandLineRunner() {
			@Override
			public void run(String... args) throws Exception {
				Flux.from(connectionFactory.create())
						.flatMap(c ->
								Flux.from(c.createBatch()
										.add("DROP TABLE IF EXISTS test;")
										.add("CREATE TABLE test (test int4);")
										.add("INSERT INTO test VALUES (1);")
										.execute())
										.doFinally((st) -> c.close())
						)
						.log()
						.blockLast();

				createFlux(Mono.from(connectionFactory.create()).block(), theServiceName)
						.subscribe(o -> System.out.println(Thread.currentThread().getName() + " # writing : " + counter));

			}
		};
	}

	public Flux<Long> createFlux(Connection connectionFactory, String theServiceName){
		return Flux.interval(Duration.ofSeconds(1))
				.flatMap(i -> {
					return write(connectionFactory,i, theServiceName);
				})
				.doOnNext(i ->counter++)
				.doOnError(s -> System.out.println(Thread.currentThread().getName() + " : " + "error : " + s))
				.retryBackoff( 10, Duration.ofSeconds(2) , Duration.ofMinutes(5))
				.onErrorReturn(0L)
				.doOnCancel(() -> System.out.println(Thread.currentThread().getName() + " : " + "cancel"))
				.doOnComplete(() -> System.out.println(Thread.currentThread().getName() + " : " + "complete"));
	}


	public Mono<Long> write(Connection connectionFactory, Long  i, String theServiceName) {
		return Mono.just(connectionFactory)
				.flatMap(c -> Mono.from(c.beginTransaction())
						.then(Mono.from(c.createStatement("INSERT INTO test VALUES ($1)")
								.bind("$1", counter)
								.returnGeneratedValues("test")
								.execute()))
						.then(Mono.from(c.createStatement(String.format("NOTIFY \"my-channel\", 'theServiceName:%s %s';", theServiceName, i))
								.execute()))
						.map(result -> result.map((row, meta) ->
								i))
						.flatMap(pub -> Mono.from(pub))
						.delayUntil(r -> c.commitTransaction())
				);
	}

}

