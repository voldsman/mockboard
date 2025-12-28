package dev.mockboard.data.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "dev.mockboard.data.repository")
@EnableMongoAuditing
public class MongoConfig {
}
