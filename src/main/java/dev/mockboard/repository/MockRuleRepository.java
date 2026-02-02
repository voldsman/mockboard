package dev.mockboard.repository;

import dev.mockboard.repository.model.MockRule;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface MockRuleRepository extends ListCrudRepository<MockRule, String> {

    List<MockRule> findByBoardIdAndDeletedFalseOrderByTimestampDesc(String boardId);

    @Modifying
    @Query("UPDATE mock_rules SET deleted = true WHERE id = :mockRuleId")
    void markDeleted(String mockRuleId);

    @Modifying
    @Query("DELETE FROM mock_rules WHERE deleted = true")
    int hardDeleteMarkedRules();
}
