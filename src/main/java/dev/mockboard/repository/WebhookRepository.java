package dev.mockboard.repository;

import dev.mockboard.repository.model.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class WebhookRepository {

    private final JdbcTemplate jdbcTemplate;

    private static class WebhookRowMapper implements RowMapper<Webhook> {
        @Override
        public Webhook mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Webhook.builder()
                    .id(rs.getString("id"))
                    .boardId(rs.getString("board_id"))
                    .method(rs.getString("method"))
                    .path(rs.getString("path"))
                    .fullUrl(rs.getString("full_url"))
                    .queryParams(rs.getString("query_params"))
                    .headers(rs.getString("headers"))
                    .body(rs.getString("body"))
                    .contentType(rs.getString("content_type"))
                    .statusCode(rs.getInt("status_code"))
                    .timestamp(rs.getTimestamp("received_at").toInstant())
                    .processingTimeMs(rs.getLong("processing_time_ms"))
                    .matched(rs.getBoolean("matched"))
                    .build();
        }
    }

    public List<Webhook> findByBoardId(String boardId) {
        var sql = """
                SELECT * FROM webhooks WHERE board_id = ? ORDER BY received_at DESC;
                """;
        try {
            return jdbcTemplate.query(sql, new WebhookRowMapper(), boardId);
        } catch (Exception e) {
            return List.of();
        }
    }
}
