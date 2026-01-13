package dev.mockboard.service;

import dev.mockboard.cache.WebhookCache;
import dev.mockboard.common.domain.MockExecutionResult;
import dev.mockboard.common.domain.RequestMetadata;
import dev.mockboard.common.domain.dto.BoardDto;
import dev.mockboard.common.domain.dto.WebhookDto;
import dev.mockboard.common.utils.IdGenerator;
import dev.mockboard.event.config.DomainEvent;
import dev.mockboard.event.config.EventQueue;
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
    private final SseService sseService;

    public List<WebhookDto> getWebhooks(BoardDto boardDto) {
        var cachedWebhooks = webhookCache.getWebhooks(boardDto.getId());
        if (CollectionUtils.isEmpty(cachedWebhooks)) {
            return getWebhooks(boardDto.getId());
        }
        return cachedWebhooks;
    }

    @Async
    public void processWebhookAsync(String apiKey, RequestMetadata metadata, MockExecutionResult result, long executionTime) {
        try {
            var webhooks = webhookCache.getWebhooks(apiKey);
            if (CollectionUtils.isEmpty(webhooks)) {
                log.debug("Initializing webhooks cache for apiKey: {}", apiKey);
                getWebhooks(apiKey);
            }

            log.debug("Processing webhook async [{}] for key: {}", Thread.currentThread(), apiKey);
            var webhookDto = new WebhookDto();
            webhookDto.setId(IdGenerator.generateId());
            webhookDto.setBoardId(apiKey);
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

            var cachedResultDto = webhookCache.addWebhook(apiKey, webhookDto);
            // when ids are equals - means new object added, should process insert
            // otherwise - reference rewrite happened, should process update and use cachedResultDto
            if (cachedResultDto.getId().equals(webhookDto.getId())) {
                var webhook = modelMapper.map(webhookDto, Webhook.class);
                eventQueue.publish(DomainEvent.create(webhook, Webhook.class));
                sseService.broadcast(apiKey, webhookDto);
            } else {
                var webhook = modelMapper.map(cachedResultDto, Webhook.class);
                eventQueue.publish(DomainEvent.update(webhook, webhook.getId(), Webhook.class));
                sseService.broadcast(apiKey, cachedResultDto);
            }
        } catch (Exception e) {
            log.error("Failed to process webhook", e);
        }
    }

    private List<WebhookDto> getWebhooks(String apiKey) {
        var persistedWebhooks = webhookRepository.findByBoardId(apiKey);
        if (CollectionUtils.isEmpty(persistedWebhooks)) {
            return Collections.emptyList();
        }

        var dtos = persistedWebhooks.stream()
                .map(webhook -> modelMapper.map(webhook, WebhookDto.class))
                .toList();
        webhookCache.addWebhooks(apiKey, dtos);
        return dtos;
    }
}
