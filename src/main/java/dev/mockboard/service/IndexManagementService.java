package dev.mockboard.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndexManagementService {

    private final MongoTemplate mongoTemplate;

    public void deleteAllIndexes(final String collectionName) {
        log.info("Deleting all indexes for collection={}", collectionName);
        var indexOps = mongoTemplate.indexOps(collectionName);
        indexOps.dropAllIndexes();
    }
}
