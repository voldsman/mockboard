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
                    .apiKey(rs.getString("api_key"))
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
                INSERT INTO mock_rules(id, board_id, api_key, method, path, headers, body, status_code, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(sql,
                mockRule.getId(),
                mockRule.getBoardId(),
                mockRule.getApiKey(),
                mockRule.getApiKey(),
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
                INSERT INTO mock_rules(id, board_id, api_key, method, path, headers, body, status_code, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.batchUpdate(sql, mockRules, mockRules.size(), (ps, mockRule) -> {
            ps.setString(1, mockRule.getId());
            ps.setString(2, mockRule.getBoardId());
            ps.setString(3, mockRule.getApiKey());
            ps.setString(4, mockRule.getMethod());
            ps.setString(5, mockRule.getPath());
            ps.setString(6, mockRule.getHeaders());
            ps.setString(7, mockRule.getBody());
            ps.setInt(8, mockRule.getStatusCode());
            ps.setTimestamp(9, Timestamp.from(mockRule.getTimestamp()));
        });
    }

    public void batchDelete(List<String> mockRuleIds) {
        var sql = """
            DELETE FROM mock_rules WHERE id IN (?)
        """;

        jdbcTemplate.batchUpdate(sql, mockRuleIds, mockRuleIds.size(),  (ps, id) -> ps.setString(1, id));
    }
}
