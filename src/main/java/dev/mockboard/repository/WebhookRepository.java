package dev.mockboard.repository;

import dev.mockboard.repository.model.Webhook;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface WebhookRepository extends ListCrudRepository<Webhook, String> {

    List<Webhook> findByBoardIdOrderByTimestampDesc(String boardId);
}
