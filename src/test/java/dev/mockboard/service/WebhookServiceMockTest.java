package dev.mockboard.service;

import dev.mockboard.common.cache.WebhookCache;
import dev.mockboard.common.domain.MockExecutionResult;
import dev.mockboard.common.domain.RequestMetadata;
import dev.mockboard.common.domain.dto.BoardDto;
import dev.mockboard.common.domain.dto.MockRuleDto;
import dev.mockboard.common.domain.dto.WebhookDto;
import dev.mockboard.config.sse.SseManager;
import dev.mockboard.event.EventQueue;
import dev.mockboard.repository.WebhookRepository;
import dev.mockboard.repository.model.Webhook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookServiceMockTest {

    @Mock private EventQueue eventQueue;
    @Mock private ModelMapper modelMapper;
    @Mock private WebhookCache webhookCache;
    @Mock private WebhookRepository webhookRepository;
    @Mock private SseManager sseManager;

    @InjectMocks private WebhookService webhookService;

    @Test
    void getWebhooks_cacheHit() {
        var boardId = "board-123";
        var boardDto = BoardDto.builder().id(boardId).build();
        var cachedWebhook = new WebhookDto();
        cachedWebhook.setId("webhook-1");
        cachedWebhook.setBoardId(boardId);

        when(webhookCache.getWebhooks(boardId)).thenReturn(List.of(cachedWebhook));

        var result = webhookService.getWebhooks(boardDto);
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo("webhook-1");
        verify(webhookRepository, never()).findByBoardIdOrderByTimestampDesc(any());
    }

    @Test
    void getWebhooks_cacheMiss() {
        var boardId = "board-123";
        var boardDto = BoardDto.builder().id(boardId).build();
        var webhook = new Webhook();
        webhook.setId("webhook-1");
        webhook.setBoardId(boardId);

        var webhookDto = new WebhookDto();
        webhookDto.setId("webhook-1");
        webhookDto.setBoardId(boardId);

        when(webhookCache.getWebhooks(boardId)).thenReturn(Collections.emptyList());
        when(webhookRepository.findByBoardIdOrderByTimestampDesc(boardId)).thenReturn(List.of(webhook));
        when(modelMapper.map(webhook, WebhookDto.class)).thenReturn(webhookDto);

        var result = webhookService.getWebhooks(boardDto);
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo("webhook-1");
        verify(webhookCache).addWebhooks(eq(boardId), any());
    }

    @Test
    void getWebhooks_noWebhooks() {
        var boardId = "board-123";
        var boardDto = BoardDto.builder().id(boardId).build();

        when(webhookCache.getWebhooks(boardId)).thenReturn(Collections.emptyList());
        when(webhookRepository.findByBoardIdOrderByTimestampDesc(boardId)).thenReturn(Collections.emptyList());

        var result = webhookService.getWebhooks(boardDto);
        assertThat(result).isEmpty();
    }

    @Test
    void processWebhookAsync_idsMatch() {
        var boardId = "board-123";
        var mockRuleDto = new MockRuleDto();
        mockRuleDto.setId("rule-1");

        var metadata = new RequestMetadata(
                "GET",
                "http://localhost/api/test",
                "/api/test",
                "",
                "abc=cde",
                "\"Content-Type\", \"application/json\"",
                "{\"test\":\"data\"}",
                "application/json"
        );

        var result = new MockExecutionResult(mockRuleDto, null, "{}", 200);
        var executionTime = 100L;

        var webhookDto = new WebhookDto();
        webhookDto.setId("webhook-1");
        webhookDto.setBoardId(boardId);

        var webhook = new Webhook();
        webhook.setId("webhook-1");

        when(webhookCache.getWebhooks(boardId)).thenReturn(List.of());
        when(webhookCache.addWebhook(eq(boardId), any(WebhookDto.class))).thenAnswer(invocation -> invocation.<WebhookDto>getArgument(1));
        when(modelMapper.map(any(WebhookDto.class), eq(Webhook.class))).thenReturn(webhook);

        webhookService.processWebhookAsync(boardId, metadata, result, executionTime);

        var webhookCaptor = ArgumentCaptor.forClass(WebhookDto.class);
        verify(sseManager, timeout(1000)).broadcast(eq(boardId), webhookCaptor.capture());

        var captured = webhookCaptor.getValue();
        assertThat(captured.getBoardId()).isEqualTo(boardId);
        assertThat(captured.getMatched()).isTrue();
        assertThat(captured.getProcessingTimeMs()).isEqualTo(executionTime);
        assertThat(captured.getMethod()).isEqualTo("GET");
        assertThat(captured.getPath()).isEqualTo("/api/test");
        assertThat(captured.getStatusCode()).isEqualTo(200);

        verify(eventQueue, timeout(1000)).publish(any());
    }

    @Test
    void processWebhookAsync_idsDoNotMatch() {
        var boardId = "board-123";
        var metadata = new RequestMetadata(
                "POST",
                "/api/update",
                "http://localhost/api/update",
                "",
                "abc=cde",
                "\"Content-Type\", \"application/json\"",
                "{}",
                "application/json"
        );

        var result = new MockExecutionResult(null, null, "{}", 200);
        var executionTime = 50L;

        var cachedDto = new WebhookDto();
        cachedDto.setId("cached-webhook-1");
        cachedDto.setBoardId(boardId);

        var webhook = new Webhook();
        webhook.setId("cached-webhook-1");

        when(webhookCache.getWebhooks(boardId)).thenReturn(List.of());
        when(webhookCache.addWebhook(eq(boardId), any(WebhookDto.class))).thenReturn(cachedDto);
        when(modelMapper.map(cachedDto, Webhook.class)).thenReturn(webhook);

        webhookService.processWebhookAsync(boardId, metadata, result, executionTime);

        verify(sseManager, timeout(1000)).broadcast(boardId, cachedDto);
        verify(eventQueue, timeout(1000)).publish(any());
    }

    @Test
    void processWebhookAsync_unmatchedRequest() {
        var boardId = "board-123";
        var metadata = new RequestMetadata(
                "DELETE",
                "/api/delete",
                "http://localhost/api/delete",
                "",
                "",
                "abc=cde",
                null,
                null
        );

        var result = new MockExecutionResult(null, null, "{}", 200);
        var executionTime = 25L;

        var webhookDto = new WebhookDto();
        webhookDto.setId("webhook-1");

        when(webhookCache.getWebhooks(boardId)).thenReturn(List.of());
        when(webhookCache.addWebhook(eq(boardId), any(WebhookDto.class))).thenAnswer(invocation -> invocation.getArgument(1));
        when(modelMapper.map(any(WebhookDto.class), eq(Webhook.class))).thenReturn(new Webhook());

        webhookService.processWebhookAsync(boardId, metadata, result, executionTime);

        var webhookCaptor = ArgumentCaptor.forClass(WebhookDto.class);
        verify(sseManager, timeout(1000)).broadcast(eq(boardId), webhookCaptor.capture());
        assertThat(webhookCaptor.getValue().getMatched()).isFalse();
    }
}