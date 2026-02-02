package dev.mockboard.repository;

import dev.mockboard.repository.model.MockRule;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MockRuleRepository extends ListCrudRepository<MockRule, String> {

    List<MockRule> findByBoardIdAndDeletedFalseOrderByTimestampDesc(String boardId);

    @Modifying
    @Query("UPDATE mock_rules SET deleted = true WHERE id = :mockRuleId")
    void markDeleted(String mockRuleId);

    @Modifying
    @Query("""
        UPDATE mock_rules SET
            method = :#{#rule.method},
            path = :#{#rule.path},
            headers = :#{#rule.headers},
            body = :#{#rule.body},
            status_code = :#{#rule.statusCode},
            delay = :#{#rule.delay}
        WHERE id = :#{#rule.id}
    """)
    void update(@Param("rule") MockRule mockRule);


}
