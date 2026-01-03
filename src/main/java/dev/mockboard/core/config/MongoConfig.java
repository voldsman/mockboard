package dev.mockboard.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "dev.mockboard.storage.doc.repo")
public class MongoConfig {
}
