package dev.mockboard.repository;

import dev.mockboard.repository.model.Board;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BoardRepository {

    private final JdbcTemplate jdbcTemplate;

    private static class BoardRowMapper implements RowMapper<Board> {
        @Override
        public Board mapRow(ResultSet rs, int _rowNum) throws SQLException {
            return Board.builder()
                    .id(rs.getString("id"))
                    .ownerToken(rs.getString("owner_token"))
                    .timestamp(rs.getTimestamp("created_at").toInstant())
                    .build();
        }
    }

    public void insert(Board board) {
        var sql = """
            INSERT INTO boards (id, owner_token, created_at)
            VALUES (?, ?, ?)
            """;

        jdbcTemplate.update(sql,
                board.getId(),
                board.getOwnerToken(),
                Timestamp.from(board.getTimestamp())
        );
    }

    public Optional<Board> findById(String boardId) {
        var sql = "SELECT * FROM boards WHERE id = ?;";
        try {
            var board =  jdbcTemplate.queryForObject(sql, new BoardRowMapper(), boardId);
            return Optional.ofNullable(board);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public long count() {
        var sql = "SELECT COUNT(*) FROM boards";
        var count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0;
    }

    // batch operations for events
    public void batchInsert(List<Board> boards) {
        var sql = """
            INSERT INTO boards (id, owner_token, created_at)
            VALUES (?, ?, ?)
            """;

        jdbcTemplate.batchUpdate(sql, boards, boards.size(), (ps, board) -> {
            ps.setString(1, board.getId());
            ps.setString(2, board.getOwnerToken());
            ps.setTimestamp(3, Timestamp.from(board.getTimestamp()));
        });
    }

    public void batchDelete(List<String> boardIds) {
        var sql = """
            DELETE FROM boards WHERE id IN (?)
        """;

        jdbcTemplate.batchUpdate(sql, boardIds, boardIds.size(),  (ps, id) -> ps.setString(1, id));
    }
}
