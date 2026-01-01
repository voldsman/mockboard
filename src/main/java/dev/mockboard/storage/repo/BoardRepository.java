package dev.mockboard.storage.repo;

import dev.mockboard.core.common.doc.BoardDoc;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface BoardRepository extends MongoRepository<BoardDoc, String> {
    Optional<BoardDoc> findByApiKey(String apiKey);
}
