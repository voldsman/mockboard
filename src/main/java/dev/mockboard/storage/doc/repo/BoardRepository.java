package dev.mockboard.storage.doc.repo;

import dev.mockboard.storage.doc.BoardDoc;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface BoardRepository extends MongoRepository<BoardDoc, String> {
    Optional<BoardDoc> findByApiKey(String apiKey);
}
