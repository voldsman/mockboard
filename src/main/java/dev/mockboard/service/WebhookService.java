package dev.mockboard.service;

import dev.mockboard.cache.WebhookCache;
import dev.mockboard.common.domain.MockExecutionResult;
import dev.mockboard.common.domain.RequestMetadata;
import dev.mockboard.common.domain.dto.WebhookDto;
import dev.mockboard.common.utils.IdGenerator;
import dev.mockboard.event.config.EventQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private final EventQueue eventQueue;
    private final ModelMapper modelMapper;
    private final WebhookCache webhookCache;
    private final SseService sseService;

    @Async
    public void processWebhookAsync(String apiKey, RequestMetadata metadata, MockExecutionResult result, long executionTime) {
        try {
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
                // send insert event
                sseService.broadcast(apiKey, webhookDto);
            } else {
                // send update event
                sseService.broadcast(apiKey, cachedResultDto);
            }
        } catch (Exception e) {
            log.error("Failed to process webhook", e);
        }
    }
}
