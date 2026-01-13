package dev.mockboard.cache;

import dev.mockboard.Constants;
import dev.mockboard.cache.config.CaffeineEntityCache;
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
           var mutableWebhooks = (webhooks == null)
                   ? new ArrayList<WebhookDto>()
                   : new ArrayList<>(webhooks);
           if (mutableWebhooks.size() >= Constants.MAX_WEBHOOKS) {
               var lastWebhook = Collections.min(mutableWebhooks, Comparator.comparing(WebhookDto::getTimestamp));
               lastWebhook.setMethod(webhook.getMethod());
               lastWebhook.setPath(webhook.getPath());
               lastWebhook.setFullUrl(webhook.getFullUrl());
               lastWebhook.setQueryParams(webhook.getQueryParams());
               lastWebhook.setHeaders(webhook.getHeaders());
               lastWebhook.setBody(webhook.getBody());
               lastWebhook.setContentType(webhook.getContentType());
               lastWebhook.setStatusCode(webhook.getStatusCode());
               lastWebhook.setMatched(webhook.isMatched());
               lastWebhook.setTimestamp(webhook.getTimestamp());
               lastWebhook.setProcessingTimeMs(webhook.getProcessingTimeMs());
               resultWrapper.set(lastWebhook);
           } else {
               mutableWebhooks.add(webhook);
               resultWrapper.set(webhook);
           }
           return mutableWebhooks;
        });
        return resultWrapper.get();
    }

    public List<WebhookDto> getWebhooks(String apiKey) {
        var webhooks = cache.getIfPresent(apiKey);
        if (CollectionUtils.isEmpty(webhooks)) {
            return Collections.emptyList();
        }
        return webhooks.stream()
                .sorted(Comparator.comparing(WebhookDto::getTimestamp).reversed())
                .toList();
    }
}
