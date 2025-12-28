package dev.mockboard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "dev.mockboard.storage.data.repo")
public class MongoConfig {
}
