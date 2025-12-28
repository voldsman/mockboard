package dev.mockboard.storage.data.repo;

import dev.mockboard.storage.data.doc.BoardDoc;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BoardRepository extends MongoRepository<BoardDoc, String> {
}
