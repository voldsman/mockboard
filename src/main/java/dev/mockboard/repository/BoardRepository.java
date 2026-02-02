package dev.mockboard.repository;

import dev.mockboard.repository.model.Board;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface BoardRepository extends ListCrudRepository<Board, String> {

    Optional<Board> findByIdAndDeletedFalse(String id);

    @Modifying
    @Query("UPDATE boards SET deleted = true WHERE id = :boardId")
    void markDeleted(String boardId);
}
