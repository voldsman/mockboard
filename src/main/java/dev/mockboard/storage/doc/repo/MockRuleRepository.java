package dev.mockboard.storage.doc.repo;

import dev.mockboard.storage.doc.MockRuleDoc;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MockRuleRepository extends MongoRepository<MockRuleDoc, String> {

    List<MockRuleDoc> findByBoardId(String boardId);

    List<MockRuleDoc> findByApiKey(String apiKey);

    Optional<MockRuleDoc> findByIdAndBoardId(String id, String boardId);

    void deleteByIdAndBoardId(String mockRuleId, String boardId);
}
