package dev.mockboard.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathMatchingEngineTest {

    private PathMatchingEngine engine;

    @BeforeEach
    void setUp() {
        engine = new PathMatchingEngine();
    }

    @Test
    void exactMatch() {
        engine.register("/api/users/profile", "mock-1");

        var result = engine.match("/api/users/profile");
        assertTrue(result.isPresent());
        assertEquals("mock-1", result.get());
    }

    @Test
    void exactMismatch() {
        engine.register("/api/users/profile", "mock-1");

        var result = engine.match("/api/users/settings");
        assertFalse(result.isPresent());
    }

    @Test
    void singleWildcard() {
        engine.register("/api/users/*/profile", "mock-1");

        var result = engine.match("/api/users/12345/profile");
        assertTrue(result.isPresent());
        assertEquals("mock-1", result.get());
    }

    @Test
    void multipleWildcards() {
        engine.register("/api/*/users/*/profile", "mock-1");

        var result = engine.match("/api/v1/users/12345/profile");
        assertTrue(result.isPresent());
        assertEquals("mock-1", result.get());
    }

    @Test
    void threeWildcards() {
        engine.register("/api/*/users/*/posts/*", "mock-1");

        var result = engine.match("/api/v1/users/12345/posts/67890");
        assertTrue(result.isPresent());
        assertEquals("mock-1", result.get());
    }

    @Test
    void tooManyWildcards() {
        assertThrows(IllegalArgumentException.class, () -> {
            engine.register("/api/*/*/*/*", "mock-1");
        });
    }

    @Test
    void exactMatchPriority() {
        engine.register("/api/users/*/profile", "mock-1");
        engine.register("/api/users/12345/profile", "mock-2");

        var result = engine.match("/api/users/12345/profile");
        assertTrue(result.isPresent());
        assertEquals("mock-2", result.get());
    }

    @Test
    void wildcardPriority() {
        engine.register("/api/*/users/*/posts/*", "mock-1");
        engine.register("/api/v1/users/*/posts/*", "mock-2");

        var result = engine.match("/api/v1/users/12345/posts/67890");
        assertTrue(result.isPresent());
        assertEquals("mock-2", result.get());
    }

    @ParameterizedTest
    @CsvSource({
            "/api/users/123/profile, mock-1",
            "/api/users/456/profile, mock-1",
            "/api/users/abc-def-ghi/profile, mock-1",
            "/api/users/user@email.com/profile, mock-1"
    })
    void variousWildcardValues(String requestPath, String expectedMockId) {
        engine.register("/api/users/*/profile", "mock-1");

        var result = engine.match(requestPath);
        assertTrue(result.isPresent());
        assertEquals(expectedMockId, result.get());
    }

    @Test
    void differentSegmentCount() {
        engine.register("/api/users/*/profile", "mock-1");

        var result = engine.match("/api/users/12345/profile/settings");
        assertFalse(result.isPresent());
    }

    @Test
    void tooFewSegments() {
        engine.register("/api/users/*/profile", "mock-1");

        var result = engine.match("/api/users/12345");
        assertFalse(result.isPresent());
    }

    @Test
    void leadingSlash() {
        engine.register("/api/users/*/profile", "mock-1");

        assertTrue(engine.match("/api/users/123/profile").isPresent());
        assertTrue(engine.match("api/users/123/profile").isPresent());
    }

    @Test
    void emptyWildcardSegment() {
        engine.register("/api/users/*/profile", "mock-1");

        var result = engine.match("/api/users//profile");
        assertTrue(result.isPresent());
    }

    @Test
    void rootPath() {
        engine.register("/", "mock-1");

        var result = engine.match("/");
        assertTrue(result.isPresent());
        assertEquals("mock-1", result.get());
    }

    @Test
    void complexUUID() {
        engine.register("/api/resources/*/data", "mock-1");

        var result = engine.match("/api/resources/550e8400-e29b-41d4-a716-446655440000/data");
        assertTrue(result.isPresent());
        assertEquals("mock-1", result.get());
    }

    @Test
    void numericIds() {
        engine.register("/api/users/*/orders/*", "mock-1");

        var result = engine.match("/api/users/12345/orders/67890");
        assertTrue(result.isPresent());
        assertEquals("mock-1", result.get());
    }

    @Test
    void unregisterExact() {
        engine.register("/api/users/profile", "mock-1");

        assertTrue(engine.unregister("/api/users/profile"));
        assertFalse(engine.match("/api/users/profile").isPresent());
    }

    @Test
    void unregisterWildcard() {
        engine.register("/api/users/*/profile", "mock-1");

        assertTrue(engine.unregister("/api/users/*/profile"));
        assertFalse(engine.match("/api/users/12345/profile").isPresent());
        assertFalse(engine.match("/api/users/54321/profile").isPresent());
    }

    @Test
    void unregisterNonExistent() {
        assertFalse(engine.unregister("/api/users/profile"));
    }

    @Test
    void clear() {
        engine.register("/api/users/profile", "mock-1");
        engine.register("/api/users/*/profile", "mock-2");
        engine.register("/api/*/users/*/profile", "mock-3");

        assertEquals(3, engine.size());

        engine.clear();

        assertEquals(0, engine.size());
        assertFalse(engine.match("/api/users/profile").isPresent());
        assertFalse(engine.match("/api/users/12345/profile").isPresent());
    }

    @Test
    void nullPatternRegistration() {
        assertThrows(IllegalArgumentException.class, () -> {
            engine.register(null, "mock-1");
        });
    }

    @Test
    void emptyPatternRegistration() {
        assertThrows(IllegalArgumentException.class, () -> {
            engine.register("", "mock-1");
        });
    }

    @Test
    void nullRequestPath() {
        engine.register("/api/users/profile", "mock-1");

        var result = engine.match(null);
        assertFalse(result.isPresent());
    }

    @Test
    void emptyRequestPath() {
        engine.register("/api/users/profile", "mock-1");

        var result = engine.match("");
        assertFalse(result.isPresent());
    }

    @Test
    void concurrentRegistration() throws InterruptedException {
        var threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    engine.register("/api/thread-" + threadId + "/resource-" + j, "mock-" + threadId + "-" + j);
                }
            });
        }

        for (var thread : threads) {
            thread.start();
        }

        for (var thread : threads) {
            thread.join();
        }

        assertEquals(1000, engine.size());
    }

    @Test
    void concurrentMatching() throws InterruptedException {
        engine.register("/api/users/*/profile", "mock-1");

        var threads = new Thread[10];
        final boolean[] results = new boolean[10];

        for (int i = 0; i < threads.length; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    var result = engine.match("/api/users/" + j + "/profile");
                    results[threadId] = result.isPresent() && "mock-1".equals(result.get());
                }
            });
        }

        for (var thread : threads) {
            thread.start();
        }

        for (var thread : threads) {
            thread.join();
        }

        for (boolean result : results) {
            assertTrue(result);
        }
    }

    @Test
    void specialCharacters() {
        engine.register("/api/users/john.doe@example.com/profile", "mock-1");

        var result = engine.match("/api/users/john.doe@example.com/profile");
        assertTrue(result.isPresent());
        assertEquals("mock-1", result.get());
    }

    @Test
    void partialSegmentMismatch() {
        engine.register("/api/users/*/profile", "mock-1");

        var result = engine.match("/api/users/123/profiles");
        assertFalse(result.isPresent());
    }

    @Test
    void consecutiveWildcards() {
        engine.register("/api/*/*/profile", "mock-1");

        var result = engine.match("/api/v1/users/profile");
        assertTrue(result.isPresent());
        assertEquals("mock-1", result.get());
    }

    @Test
    void wildcardAtStart() {
        engine.register("/*/users/profile", "mock-start");

        var result = engine.match("/api/users/profile");
        assertTrue(result.isPresent());
        assertEquals("mock-start", result.get());
    }

    @ParameterizedTest
    @CsvSource({
            "/api/users/123/profile, true",
            "/api/users/456/settings, false",
            "/api/users/789/profile/edit, false",
            "/api/products/123/profile, false"
    })
    void variousPathMatching(String requestPath, boolean shouldMatch) {
        engine.register("/api/users/*/profile", "mock-1");

        var result = engine.match(requestPath);
        assertEquals(shouldMatch, result.isPresent());
    }

    @Test
    void longSegmentValues() {
        engine.register("/api/users/*/data", "mock-1");

        String longValue = "a".repeat(512 - 16);
        var result = engine.match("/api/users/" + longValue + "/data");
        assertTrue(result.isPresent());
        assertEquals("mock-1", result.get());
    }

    @Test
    void sizeAfterOperations() {
        assertEquals(0, engine.size());

        engine.register("/api/users/profile", "mock-1");
        assertEquals(1, engine.size());

        engine.register("/api/users/*/profile", "mock-2");
        assertEquals(2, engine.size());

        engine.unregister("/api/users/profile");
        assertEquals(1, engine.size());

        engine.clear();
        assertEquals(0, engine.size());
    }
}