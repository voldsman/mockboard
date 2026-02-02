package dev.mockboard.service;

import dev.mockboard.common.cache.WebhookCache;
import dev.mockboard.common.domain.MockExecutionResult;
import dev.mockboard.common.domain.RequestMetadata;
import dev.mockboard.common.domain.dto.BoardDto;
import dev.mockboard.common.domain.dto.WebhookDto;
import dev.mockboard.common.utils.IdGenerator;
import dev.mockboard.config.sse.SseManager;
import dev.mockboard.event.DomainEvent;
import dev.mockboard.event.EventQueue;
import dev.mockboard.repository.WebhookRepository;
import dev.mockboard.repository.model.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private final EventQueue eventQueue;
    private final ModelMapper modelMapper;
    private final WebhookCache webhookCache;
    private final WebhookRepository webhookRepository;
    private final SseManager sseManager;

    public List<WebhookDto> getWebhooks(BoardDto boardDto) {
        var cachedWebhooks = webhookCache.getWebhooks(boardDto.getId());
        if (CollectionUtils.isEmpty(cachedWebhooks)) {
            return getWebhooks(boardDto.getId());
        }
        return cachedWebhooks;
    }

    @Async
    public void processWebhookAsync(String boardId, RequestMetadata metadata, MockExecutionResult result, long executionTime) {
        try {
            var webhooks = webhookCache.getWebhooks(boardId);
            if (CollectionUtils.isEmpty(webhooks)) {
                log.debug("Initializing webhooks cache for boardId: {}", boardId);
                getWebhooks(boardId);
            }

            log.debug("Processing webhook async [{}] for key: {}", Thread.currentThread(), boardId);
            var webhookDto = new WebhookDto();
            webhookDto.setId(IdGenerator.generateId());
            webhookDto.setBoardId(boardId);
            webhookDto.setMatched(result.matchingMockRuleDto() != null);
            webhookDto.setProcessingTimeMs(executionTime);
            webhookDto.setTimestamp(Instant.now());

            webhookDto.setMethod(metadata.method());
            webhookDto.setPath(metadata.mockPath());
            webhookDto.setFullUrl(metadata.fullUrl());
            webhookDto.setQueryParams(metadata.queryParams());
            webhookDto.setHeaders(metadata.headers());
            webhookDto.setBody(metadata.requestBody());
            webhookDto.setContentType(metadata.contentType());
            webhookDto.setStatusCode(result.statusCode());

            var cachedResultDto = webhookCache.addWebhook(boardId, webhookDto);
            // when ids are equals - means new object added, should process insert
            // otherwise - reference rewrite happened, should process update and use cachedResultDto
            boolean isRecycled = !cachedResultDto.getId().equals(webhookDto.getId());
            if (isRecycled) {
                var webhook = modelMapper.map(webhookDto, Webhook.class);
                eventQueue.publish(DomainEvent.create(webhook, webhook.getId(), Webhook.class));
                sseManager.broadcast(boardId, webhookDto);
            } else {
                var webhook = modelMapper.map(cachedResultDto, Webhook.class);
                eventQueue.publish(DomainEvent.update(webhook, webhook.getId(), Webhook.class));
                sseManager.broadcast(boardId, cachedResultDto);
            }
        } catch (Exception e) {
            log.error("Failed to process webhook", e);
        }
    }

    private List<WebhookDto> getWebhooks(String boardId) {
        var persistedWebhooks = webhookRepository.findByBoardIdOrderByTimestampDesc(boardId);
        if (CollectionUtils.isEmpty(persistedWebhooks)) {
            return Collections.emptyList();
        }

        var dtos = persistedWebhooks.stream()
                .map(webhook -> modelMapper.map(webhook, WebhookDto.class))
                .toList();
        webhookCache.addWebhooks(boardId, dtos);
        return dtos;
    }
}
