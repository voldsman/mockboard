package dev.mockboard.common.cache;

import dev.mockboard.Constants;
import dev.mockboard.common.domain.dto.WebhookDto;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static dev.mockboard.Constants.DEFAULT_CACHE_EXP_AFTER_ACCESS_MINUTES;
import static dev.mockboard.Constants.DEFAULT_CACHE_MAX_ENTRIES;

@Component
public class WebhookCache extends CaffeineEntityCache<List<WebhookDto>> {

    public WebhookCache() {
        super(DEFAULT_CACHE_MAX_ENTRIES, DEFAULT_CACHE_EXP_AFTER_ACCESS_MINUTES);
    }

    public void addWebhooks(String key, List<WebhookDto> webhooks) {
        var list = new ArrayList<WebhookDto>(Constants.MAX_WEBHOOKS);
        list.addAll(webhooks);
        cache.put(key, list);
    }

    public WebhookDto addWebhook(String key, WebhookDto webhook) {
        var resultWrapper = new AtomicReference<WebhookDto>();
        cache.asMap().compute(key, (k, webhooks) -> {
            // given the small collection size, TreeSet would be overkill here
           var mutableWebhooks = (CollectionUtils.isEmpty(webhooks))
                   ? new ArrayList<WebhookDto>(Constants.MAX_WEBHOOKS)
                   : new ArrayList<>(webhooks);

            // objects recycling
            // reuses old objects when size > MAX_WEBHOOKS
            // kepping id and boardId, remapping rest of the fields
           if (mutableWebhooks.size() >= Constants.MAX_WEBHOOKS) {
               var lastWebhook = recycleOldestWebhookDto(webhook, mutableWebhooks);
               resultWrapper.set(lastWebhook);
           } else {
               mutableWebhooks.add(webhook);
               resultWrapper.set(webhook);
           }
           return mutableWebhooks;
        });
        return resultWrapper.get();
    }

    private WebhookDto recycleOldestWebhookDto(WebhookDto webhook, ArrayList<WebhookDto> mutableWebhooks) {
        var oldWebhook = Collections.min(mutableWebhooks, Comparator.comparing(WebhookDto::getTimestamp));
        oldWebhook.setMethod(webhook.getMethod());
        oldWebhook.setPath(webhook.getPath());
        oldWebhook.setFullUrl(webhook.getFullUrl());
        oldWebhook.setQueryParams(webhook.getQueryParams());
        oldWebhook.setHeaders(webhook.getHeaders());
        oldWebhook.setBody(webhook.getBody());
        oldWebhook.setContentType(webhook.getContentType());
        oldWebhook.setStatusCode(webhook.getStatusCode());
        oldWebhook.setMatched(webhook.getMatched());
        oldWebhook.setTimestamp(webhook.getTimestamp());
        oldWebhook.setProcessingTimeMs(webhook.getProcessingTimeMs());
        return oldWebhook;
    }

    public List<WebhookDto> getWebhooks(String boardId) {
        var webhooks = cache.getIfPresent(boardId);
        if (CollectionUtils.isEmpty(webhooks)) {
            return Collections.emptyList();
        }
        return webhooks.stream()
                .sorted(Comparator.comparing(WebhookDto::getTimestamp).reversed())
                .toList();
    }
}
