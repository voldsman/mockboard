package dev.mockboard;

import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;

public class MockboardLoadTest {

    /*
    LATEST TEST:

    Concurrent Users: 499
    Requests per User: 100

    Duration: 4 seconds
    Total Requests: 55888
    Successful: 55888 (100.0%)
    Failed: 0 (0.0%)
    Throughput: 13972 req/sec

    Status Code Distribution:
    200: 49900 requests
    201: 2994 requests
    204: 2994 requests

    Response Times(ms):
    min: 0
    max: 260
    avg: 9.074756656169482
    p50: 2
    p95: 50
    p99: 113
     */

    private static final String BASE_URL = "http://localhost:8000";
    private static final ObjectMapper JSON = new ObjectMapper();

    private static final int CONCURRENT_USERS = 499;
    private static final int REQUESTS_PER_USER = 100;

    private static final LongAdder totalRequests = new LongAdder();
    private static final LongAdder successfulRequests = new LongAdder();
    private static final LongAdder failedRequests = new LongAdder();
    private static final ConcurrentHashMap<String, LongAdder> statusCodes = new ConcurrentHashMap<>();
    private static final List<Long> responseTimes = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws Exception {
        System.out.println("Base URL: " + BASE_URL);
        System.out.println("Concurrent Users: " + CONCURRENT_USERS);
        System.out.println("Requests per User: " + REQUESTS_PER_USER);

        var client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .executor(Executors.newVirtualThreadPerTaskExecutor())
                .build();

        var startTime = Instant.now();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = new ArrayList<CompletableFuture<Void>>();
            for (int userId = 0; userId < CONCURRENT_USERS; userId++) {
                int finalUserId = userId;
                CompletableFuture<Void> future = CompletableFuture.runAsync(
                        () -> simulateUser(client, finalUserId),
                        executor
                );
                futures.add(future);
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }

        var endTime = Instant.now();
        var totalDuration = Duration.between(startTime, endTime);
        printResults(totalDuration);
    }

    private static void simulateUser(HttpClient client, int userId) {
        try {
            var createdBoardMap = createBoard(client);
            if (createdBoardMap == null) {
                throw new RuntimeException("Failed to create board");
            }
            var boardId = createdBoardMap.get("boardId");
            var ownerToken = createdBoardMap.get("ownerToken");
            if (boardId == null) {
                System.err.println("User " + userId + " failed to create board");
                return;
            }

            var mockIds = new ArrayList<String>();
            for (int i = 0; i < 5; i++) {
                var mockId = createMockRule(client, boardId, ownerToken, i);
                if (mockId != null) {
                    mockIds.add(mockId);
                }
            }

            for (int i = 0; i < REQUESTS_PER_USER; i++) {
                int operation = i % 4;
                switch (operation) {
                    case 0, 1 -> executeMockRule(client, boardId, i % 5);
                    case 2 -> listMockRules(client, boardId, ownerToken);
                    case 3 -> listWebhooks(client, boardId, ownerToken);
                }
                Thread.sleep(ThreadLocalRandom.current().nextInt(10, 50));
            }

            for (var mockId : mockIds) {
                deleteMockRule(client, boardId, mockId, ownerToken);
            }
            deleteBoard(client, boardId, ownerToken);
        } catch (Exception e) {
            System.err.println("User " + userId + " error: " + e.getMessage());
        }
    }

    private static Map<String, String> createBoard(HttpClient client) {
        try {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/boards"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            long start = System.nanoTime();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            recordMetrics(response.statusCode(), System.nanoTime() - start);

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                var body = JSON.readValue(response.body(), Map.class);

                var resp = new HashMap<String, String>();
                resp.put("boardId", body.get("id").toString());
                resp.put("ownerToken", body.get("ownerToken").toString());
                return resp;
            }
        } catch (Exception e) {
            failedRequests.increment();
        }
        return null;
    }

    private static String createMockRule(HttpClient client, String boardId, String token, int index) {
        try {
            var mockRule = String.format("""
                {
                    "method": "GET",
                    "path": "/api/test/%d",
                    "statusCode": 200,
                    "body": "{\\"message\\":\\"success %d\\"}",
                    "delay": "0"
                }
                """, index, index);

            var request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/boards/" + boardId + "/mocks"))
                    .header("Content-Type", "application/json")
                    .header("X-Owner-Token", token)
                    .POST(HttpRequest.BodyPublishers.ofString(mockRule))
                    .build();

            long start = System.nanoTime();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            recordMetrics(response.statusCode(), System.nanoTime() - start);

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                var body = JSON.readValue(response.body(), Map.class);
                return (String) body.get("id");
            }
        } catch (Exception e) {
            failedRequests.increment();
        }
        return null;
    }

    private static void executeMockRule(HttpClient client, String boardId, int index) {
        try {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/m/" + boardId + "/api/test/" + index))
                    .GET()
                    .build();

            long start = System.nanoTime();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            recordMetrics(response.statusCode(), System.nanoTime() - start);

        } catch (Exception e) {
            failedRequests.increment();
        }
    }

    private static void listMockRules(HttpClient client, String boardId, String token) {
        try {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/boards/" + boardId + "/mocks"))
                    .header("X-Owner-Token", token)
                    .GET()
                    .build();

            long start = System.nanoTime();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            recordMetrics(response.statusCode(), System.nanoTime() - start);

        } catch (Exception e) {
            failedRequests.increment();
        }
    }

    private static void listWebhooks(HttpClient client, String boardId, String token) {
        try {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/boards/" + boardId + "/webhooks"))
                    .header("X-Owner-Token", token)
                    .GET()
                    .build();

            long start = System.nanoTime();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            recordMetrics(response.statusCode(), System.nanoTime() - start);

        } catch (Exception e) {
            failedRequests.increment();
        }
    }

    private static void deleteMockRule(HttpClient client, String boardId, String mockId, String token) {
        try {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/boards/" + boardId + "/mocks/" + mockId))
                    .header("X-Owner-Token", token)
                    .DELETE()
                    .build();

            long start = System.nanoTime();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            recordMetrics(response.statusCode(), System.nanoTime() - start);

        } catch (Exception e) {
            failedRequests.increment();
        }
    }

    private static void deleteBoard(HttpClient client, String boardId, String token) {
        try {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/boards/" + boardId))
                    .header("X-Owner-Token", token)
                    .DELETE()
                    .build();

            long start = System.nanoTime();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            recordMetrics(response.statusCode(), System.nanoTime() - start);

        } catch (Exception e) {
            failedRequests.increment();
        }
    }

    private static void recordMetrics(int statusCode, long durationNanos) {
        totalRequests.increment();

        if (statusCode >= 200 && statusCode < 300) {
            successfulRequests.increment();
        } else {
            failedRequests.increment();
        }

        statusCodes.computeIfAbsent(String.valueOf(statusCode), k -> new LongAdder()).increment();
        responseTimes.add(durationNanos / 1_000_000);
    }

    private static void printResults(Duration totalDuration) {
        long total = totalRequests.sum();
        long successful = successfulRequests.sum();
        long failed = failedRequests.sum();

        System.out.println("\nDuration: " + totalDuration.toSeconds() + " seconds");
        System.out.println("Total Requests: " + total);
        System.out.println("Successful: " + successful + " (" + (successful * 100.0 / total) + "%)");
        System.out.println("Failed: " + failed + " (" + (failed * 100.0 / total) + "%)");
        System.out.println("Throughput: " + (total / totalDuration.toSeconds()) + " req/sec");
        System.out.println("\nStatus Code Distribution:");
        statusCodes.forEach((code, count) ->
                System.out.println(code + ": " + count.sum() + " requests")
        );

        if (!responseTimes.isEmpty()) {
            List<Long> sorted = new ArrayList<>(responseTimes);
            Collections.sort(sorted);

            System.out.println("\nResponse Times(ms):");
            System.out.println("min: " + sorted.get(0));
            System.out.println("max: " + sorted.get(sorted.size() - 1));
            System.out.println("avg: " + sorted.stream().mapToLong(Long::longValue).average().orElse(0));
            System.out.println("p50: " + percentile(sorted, 50));
            System.out.println("p95: " + percentile(sorted, 95));
            System.out.println("p99: " + percentile(sorted, 99));
        }
    }

    private static long percentile(List<Long> sorted, int percentile) {
        int index = (int) Math.ceil(sorted.size() * percentile / 100.0) - 1;
        return sorted.get(Math.max(0, Math.min(index, sorted.size() - 1)));
    }
}
