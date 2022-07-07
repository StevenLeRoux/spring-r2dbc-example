package com.opensource.dada.r2db;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;

import java.time.Duration;

@Configuration
public class ConnectionFactoryConfiguration {

	@Value("${postgresql.database:db}")
	private String database;

	@Value("${postgresql.password:postgres}")
	private String password;

	@Value("${postgresql.host:localhost}")
	private String host;

	@Value("${postgresql.username:postgres}")
	private String username;

	@Value("${postgresql.port:5432}")
	private Integer port;

	@Value("${service.name}")
	private String serviceName;

	@Value("${service.instanceId}")
	private String serviceInstanceId;

	@Value("${service.instanceNumber}")
	private String serviceInstanceNumber;

	@Value("${service.version}")
	private String serviceVersion;

	@Bean
	public ConnectionFactory connectionFactory() {
		return new PostgresqlConnectionFactory(PostgresqlConnectionConfiguration
				.builder()
				.database(database)
				.password(password)
				.username(username)
				.host(host)
				.port(port)
				.build());		
	}

	@Bean
    public ConnectionPool connectionPool(){
        ConnectionPoolConfiguration configuration = ConnectionPoolConfiguration.builder(connectionFactory())
                .maxIdleTime(Duration.ofMillis(1000))
                .maxSize(1)
                .build();
        return new ConnectionPool(configuration);
    }

    @Bean
	public String theServiceName() {
		return String.format("%s_%s_%s_%s", serviceName,serviceVersion,serviceInstanceId, serviceInstanceNumber);
	}

}
