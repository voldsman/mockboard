package dev.mockboard.repository;

import dev.mockboard.repository.model.MockRule;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MockRuleRepository {

    private final JdbcTemplate jdbcTemplate;

    private static class MockRuleRowMapper implements RowMapper<MockRule> {
        @Override
        public MockRule mapRow(ResultSet rs, int _rowNum) throws SQLException {
            return MockRule.builder()
                    .id(rs.getString("id"))
                    .boardId(rs.getString("board_id"))
                    .method(rs.getString("method"))
                    .path(rs.getString("path"))
                    .headers(rs.getString("headers"))
                    .body(rs.getString("body"))
                    .statusCode(rs.getInt("status_code"))
                    .timestamp(rs.getTimestamp("created_at").toInstant())
                    .build();
        }
    }

    public void insert(MockRule mockRule) {
        var sql = """
                INSERT INTO mock_rules(id, board_id, method, path, headers, body, status_code, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(sql,
                mockRule.getId(),
                mockRule.getBoardId(),
                mockRule.getMethod(),
                mockRule.getPath(),
                mockRule.getHeaders(),
                mockRule.getBody(),
                mockRule.getStatusCode(),
                Timestamp.from(mockRule.getTimestamp())
        );
    }

    public List<MockRule> findByBoardId(String boardId) {
        var sql = """
                SELECT * FROM mock_rules WHERE board_id = ? ORDER BY created_at DESC;
                """;
        try {
            return jdbcTemplate.query(sql, new MockRuleRowMapper(), boardId);
        } catch (Exception e) {
            return List.of();
        }
    }

    public long count() {
        var sql = "SELECT COUNT(*) FROM mock_rules";
        var count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0;
    }

    // batch operations for events
    public void batchInsert(List<MockRule> mockRules) {
        var sql = """
                INSERT INTO mock_rules(id, board_id, method, path, headers, body, status_code, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.batchUpdate(sql, mockRules, mockRules.size(), (ps, mockRule) -> {
            ps.setString(1, mockRule.getId());
            ps.setString(2, mockRule.getBoardId());
            ps.setString(3, mockRule.getMethod());
            ps.setString(4, mockRule.getPath());
            ps.setString(5, mockRule.getHeaders());
            ps.setString(6, mockRule.getBody());
            ps.setInt(7, mockRule.getStatusCode());
            ps.setTimestamp(8, Timestamp.from(mockRule.getTimestamp()));
        });
    }

    public void batchUpdate(List<MockRule> mockRules) {
        var sql = """
                UPDATE mock_rules SET method=?, path=?, headers=?, body=?, status_code=? WHERE id=?
        """;
        jdbcTemplate.batchUpdate(sql, mockRules, mockRules.size(), (ps, mockRule) -> {
            ps.setString(1, mockRule.getMethod());
            ps.setString(2, mockRule.getPath());
            ps.setString(3, mockRule.getHeaders());
            ps.setString(4, mockRule.getBody());
            ps.setInt(5, mockRule.getStatusCode());
            ps.setString(6, mockRule.getId());
        });
    }

    public void batchDelete(List<String> mockRuleIds) {
        var sql = """
            DELETE FROM mock_rules WHERE id IN (?)
        """;

        jdbcTemplate.batchUpdate(sql, mockRuleIds, mockRuleIds.size(),  (ps, id) -> ps.setString(1, id));
    }
}
