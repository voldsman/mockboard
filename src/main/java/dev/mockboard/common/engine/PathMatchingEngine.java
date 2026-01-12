package dev.mockboard.common.engine;

import dev.mockboard.Constants;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class PathMatchingEngine implements Serializable {

    private static final char WILDCARD = '*';

    private final Map<String, String> exactMatches = new ConcurrentHashMap<>();
    private final Map<Long, List<PathPattern>> wildcardPatterns = new ConcurrentHashMap<>();
    private final Map<String, PathPattern> patternCache = new ConcurrentHashMap<>();

    public void register(String pattern, String mockId) {
        long wildcardCount = countWildcards(pattern);
        if (wildcardCount == 0) {
            exactMatches.put(pattern, mockId);
            log.debug("Registered exact match pattern: {} -> {}", pattern, mockId);
            return;
        }

        var pathPattern = patternCache.computeIfAbsent(pattern, p -> new PathPattern(p, mockId));
        wildcardPatterns.computeIfAbsent(wildcardCount, k -> new ArrayList<>()).add(pathPattern);
        log.debug("Registered wildcard pattern: {} (wildcards: {}) -> {}", pattern, wildcardCount, mockId);
    }

    public Optional<String> match(String requestPath) {
        if (requestPath == null || requestPath.isEmpty()) {
            return Optional.empty();
        }

        if (requestPath.length() > Constants.MAX_PATH_LENGTH) {
            log.warn("Request path exceeds maximum length of {}: {}", Constants.MAX_PATH_LENGTH, requestPath.length());
            return Optional.empty();
        }

        var exactMatch = exactMatches.get(requestPath);
        if (exactMatch != null) {
            log.trace("Exact match found for: {}", requestPath);
            return Optional.of(exactMatch);
        }

        for (long wildcardCount = 1; wildcardCount <= Constants.MAX_WILDCARDS; wildcardCount++) {
            var patterns = wildcardPatterns.get(wildcardCount);
            if (patterns == null || patterns.isEmpty()) {
                continue;
            }

            for (var pattern : patterns) {
                if (pattern.matches(requestPath)) {
                    log.trace("Wildcard match found: {} matches {}", requestPath, pattern.getPattern());
                    return Optional.of(pattern.getMockId());
                }
            }
        }

        log.trace("No match found for: {}", requestPath);
        return Optional.empty();
    }

    public boolean unregister(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return false;
        }

        long wildcardCount = countWildcards(pattern);
        if (wildcardCount == 0) {
            return exactMatches.remove(pattern) != null;
        }

        var patterns = wildcardPatterns.get(wildcardCount);
        if (patterns == null) {
            return false;
        }

        boolean removed = patterns.removeIf(p -> p.getPattern().equals(pattern));
        if (removed) {
            patternCache.remove(pattern);
        }

        return removed;
    }

    private long countWildcards(String pattern) {
        return pattern.chars().filter(ch -> ch == WILDCARD).count();
    }

    public int size() {
        int wildcardCount = wildcardPatterns.values().stream()
                .mapToInt(List::size)
                .sum();
        return exactMatches.size() + wildcardCount;
    }

    public void clear() {
        exactMatches.clear();
        wildcardPatterns.clear();
        patternCache.clear();
        log.debug("Cleared all patterns");
    }
}
