package dev.mockboard.data.repository;

import dev.mockboard.data.document.BoardDoc;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardDocRepository extends MongoRepository<BoardDoc, String> {
}
