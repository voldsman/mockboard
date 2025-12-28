package dev.mockboard.engine;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class PathMatchingEngine {

    private static final int MAX_WILDCARDS = 3;
    private static final char WILDCARD = '*';
    private static final int MAX_LENGTH = 512;

    private final Map<String, String> exactMatches = new ConcurrentHashMap<>();
    private final Map<Integer, List<PathPattern>> wildcardPatterns = new ConcurrentHashMap<>();
    private final Map<String, PathPattern> patternCache = new ConcurrentHashMap<>();

    public void register(String pattern, String mockId) {
        if (pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException("Pattern cannot be null or empty");
        }

        if (pattern.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Pattern exceeds maximum length of " + MAX_LENGTH + " characters: " + pattern.length()
            );
        }

        int wildcardCount = countWildcards(pattern);
        if (wildcardCount > MAX_WILDCARDS) {
            throw new IllegalArgumentException(
                    "Pattern cannot have more than " + MAX_WILDCARDS + " wildcards: " + pattern
            );
        }

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

        if (requestPath.length() > MAX_LENGTH) {
            log.warn("Request path exceeds maximum length of {}: {}", MAX_LENGTH, requestPath.length());
            return Optional.empty();
        }

        var exactMatch = exactMatches.get(requestPath);
        if (exactMatch != null) {
            log.trace("Exact match found for: {}", requestPath);
            return Optional.of(exactMatch);
        }

        for (int wildcardCount = 1; wildcardCount <= MAX_WILDCARDS; wildcardCount++) {
            List<PathPattern> patterns = wildcardPatterns.get(wildcardCount);
            if (patterns == null || patterns.isEmpty()) {
                continue;
            }

            for (PathPattern pattern : patterns) {
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

        int wildcardCount = countWildcards(pattern);

        if (wildcardCount == 0) {
            return exactMatches.remove(pattern) != null;
        }

        List<PathPattern> patterns = wildcardPatterns.get(wildcardCount);
        if (patterns == null) {
            return false;
        }

        boolean removed = patterns.removeIf(p -> p.getPattern().equals(pattern));
        if (removed) {
            patternCache.remove(pattern);
        }

        return removed;
    }

    private int countWildcards(String pattern) {
        int count = 0;
        for (int i = 0; i < pattern.length(); i++) {
            if (pattern.charAt(i) == WILDCARD) {
                count++;
            }
        }
        return count;
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
