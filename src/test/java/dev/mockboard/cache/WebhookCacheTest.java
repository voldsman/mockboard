package dev.mockboard.cache;

import dev.mockboard.Constants;
import dev.mockboard.common.cache.WebhookCache;
import dev.mockboard.common.domain.dto.WebhookDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class WebhookCacheTest {

    private WebhookCache webhookCache;
    private final String API_KEY = "test-api-key";

    @BeforeEach
    void setUp() {
        webhookCache = new WebhookCache();
    }

    @Test
    void addWebhooks() {
        var initList = List.of(
                createDto("1", Instant.now().minusSeconds(100)),
                createDto("2", Instant.now())
        );
        webhookCache.addWebhooks(API_KEY, initList);

        var result = webhookCache.getWebhooks(API_KEY);
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getId()).isEqualTo("2");
    }

    @Test
    void addWebhookShouldUpdateOldest() {
        int maxWebhooks = Constants.MAX_WEBHOOKS;
        for (int i = 0; i < maxWebhooks - 1; i++) {
            webhookCache.addWebhook(API_KEY, createDto("" + i, Instant.now().minusSeconds(100)));
        }

        var oldestId = "oldest-id";
        webhookCache.addWebhook(API_KEY, createDto(oldestId, Instant.now().minusSeconds(1000)));

        var incomingWebhook = createDto("ignored-id", Instant.now().plusSeconds(500));
        incomingWebhook.setBody("Updated body");
        webhookCache.addWebhook(API_KEY, incomingWebhook);

        var result = webhookCache.getWebhooks(API_KEY);
        assertThat(result).hasSize(maxWebhooks);
        assertThat(result)
                .filteredOn(d -> d.getId().equals(oldestId))
                .hasSize(1)
                .first()
                .satisfies(d -> {
                    assertThat(d.getId()).isEqualTo(oldestId);
                    assertThat(d.getBody()).isEqualTo(incomingWebhook.getBody());
                    assertThat(d.getTimestamp()).isEqualTo(incomingWebhook.getTimestamp());
                });
        assertThat(result.getFirst().getTimestamp()).isEqualTo(incomingWebhook.getTimestamp());
    }

    @Test
    public void addWebhookUnderConcurrency() {
        int maxWebhooks = Constants.MAX_WEBHOOKS;
        for (int i = 0; i < maxWebhooks; i++) {
            webhookCache.addWebhook(API_KEY, createDto("plain-" + i, Instant.now().minusSeconds(100)));
        }

        int maxThreads = 50;
        var completableFutures = IntStream.range(0, maxThreads)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    webhookCache.addWebhook(API_KEY, createDto("concurrent-" + i, Instant.now()));
                })).toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(completableFutures).join();

        var result = webhookCache.getWebhooks(API_KEY);
        assertThat(result).hasSize(maxWebhooks);
    }

    private WebhookDto createDto(String id, Instant timestamp) {
        var dto = new WebhookDto();
        dto.setId(id);
        dto.setBoardId("board-1");
        dto.setTimestamp(timestamp);
        dto.setBody("body");
        return dto;
    }
}