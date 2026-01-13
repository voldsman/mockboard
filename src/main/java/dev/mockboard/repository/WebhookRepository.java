package dev.mockboard.repository;

import dev.mockboard.repository.model.Webhook;
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

    // batch ops
    public void batchInsert(List<Webhook> webhooks) {
        var sql = """
                INSERT INTO webhooks(id, board_id, method, path, full_url, query_params, headers, body, content_type, status_code, received_at, processing_time_ms, matched)
                VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        jdbcTemplate.batchUpdate(sql, webhooks, webhooks.size(), (ps, webhook) -> {
            ps.setString(1, webhook.getId());
            ps.setString(2, webhook.getBoardId());
            ps.setString(3, webhook.getMethod());
            ps.setString(4, webhook.getPath());
            ps.setString(5, webhook.getFullUrl());
            ps.setString(6, webhook.getQueryParams());
            ps.setString(7, webhook.getHeaders());
            ps.setString(8, webhook.getBody());
            ps.setString(9, webhook.getContentType());
            ps.setInt(10, webhook.getStatusCode());
            ps.setTimestamp(11, Timestamp.from(webhook.getTimestamp()));
            ps.setLong(12, webhook.getProcessingTimeMs());
            ps.setBoolean(13, webhook.isMatched());
        });
    }

    public void batchUpdate(List<Webhook> webhooks) {
        var sql = """
                UPDATE webhooks SET method = ?, path = ?, full_url = ?, query_params = ?,
                                    headers = ?, body = ?, content_type = ?, status_code = ?,
                                    received_at = ?, processing_time_ms = ?, matched = ?
                                WHERE id = ?
        """;
        jdbcTemplate.batchUpdate(sql, webhooks, webhooks.size(), (ps, webhook) -> {
            ps.setString(1, webhook.getMethod());
            ps.setString(2, webhook.getPath());
            ps.setString(3, webhook.getFullUrl());
            ps.setString(4, webhook.getQueryParams());
            ps.setString(5, webhook.getHeaders());
            ps.setString(6, webhook.getBody());
            ps.setString(7, webhook.getContentType());
            ps.setInt(8, webhook.getStatusCode());
            ps.setTimestamp(9, Timestamp.from(webhook.getTimestamp()));
            ps.setLong(10, webhook.getProcessingTimeMs());
            ps.setBoolean(11, webhook.isMatched());
            ps.setString(12, webhook.getId());
        });
    }
}
