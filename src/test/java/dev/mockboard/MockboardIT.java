package dev.mockboard;

import dev.mockboard.common.cache.BoardCache;
import dev.mockboard.common.cache.MockRuleCache;
import dev.mockboard.common.cache.WebhookCache;
import dev.mockboard.common.domain.dto.BoardDto;
import dev.mockboard.common.domain.dto.MockRuleDto;
import dev.mockboard.common.domain.dto.WebhookDto;
import dev.mockboard.common.domain.response.IdResponse;
import dev.mockboard.repository.BoardRepository;
import dev.mockboard.repository.MockRuleRepository;
import dev.mockboard.repository.WebhookRepository;
import dev.mockboard.repository.model.MockRule;
import dev.mockboard.service.CleanupService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MockboardIT {

    private static String boardId;
    private static String token;
    private static String mockId;

    @LocalServerPort private int port;
    @Autowired private BoardRepository boardRepository;
    @Autowired private MockRuleRepository mockRuleRepository;
    @Autowired private WebhookRepository webhookRepository;

    @Autowired private BoardCache boardCache;
    @Autowired private MockRuleCache mockRuleCache;
    @Autowired private WebhookCache webhookCache;

    @Autowired private CleanupService cleanupService;

    private RestClient restClient;

    @BeforeEach
    void setUp() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    @Order(1)
    void createBoard() {
        var response = restClient.post()
                .uri("/api/boards")
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(BoardDto.class);
        assertThat(response).isNotNull();

        boardId = response.getId();
        token = response.getOwnerToken();

        var cached = boardCache.get(boardId);
        assertThat(cached).isPresent();
        assertThat(cached.get()).isInstanceOf(BoardDto.class);
        assertThat(cached.get().getTimestamp()).isNotNull();
        assertThat(boardRepository.count()).isEqualTo(1);
    }

    @Test
    @Order(2)
    void createMockRule() {
        var mockRuleDto = MockRuleDto.builder()
                .method("GET")
                .path("/api/test")
                .statusCode(200)
                .body("{\"message\":\"success\"}")
                .delay(0)
                .build();

        var response = restClient.post()
                .uri("/api/boards/" + boardId + "/mocks")
                .header(Constants.OWNER_TOKEN_HEADER_KEY, token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mockRuleDto)
                .retrieve()
                .body(IdResponse.class);
        assertThat(response).isNotNull();
        mockId = response.id();

        var cached = mockRuleCache.getMockRules(boardId);
        cached.forEach(mr -> {
            assertThat(mr).isInstanceOf(MockRuleDto.class);
            assertThat(mr.getMethod()).isEqualTo("GET");
            assertThat(mr.getPath()).isEqualTo("/api/test");
            assertThat(mr.getBody()).contains("success");
            assertThat(mr.getTimestamp()).isNotNull();
        });
        assertThat(mockRuleRepository.count()).isEqualTo(1);
    }

    @Test
    @Order(3)
    void executeMockRule() {
        var body = restClient.get()
                .uri("/m/" + boardId + "/api/test")
                .retrieve()
                .body(String.class);
        assertThat(body).isNotNull().contains("success");

        var cached = webhookCache.getWebhooks(boardId);
        assertThat(cached).isNotEmpty().hasSize(1);
    }

    @Test
    @Order(4)
    void updateMockRule() {
        var mockRuleDto = MockRuleDto.builder()
                .method("GET")
                .path("/api/updated")
                .statusCode(201)
                .body("{\"message\": \"updated\"}")
                .delay(0)
                .build();

        var response = restClient.put()
                .uri("/api/boards/" + boardId + "/mocks/" + mockId)
                .header(Constants.OWNER_TOKEN_HEADER_KEY, token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(mockRuleDto)
                .retrieve()
                .body(IdResponse.class);
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(mockId);

        var cached = mockRuleCache.getMockRules(boardId);
        cached.forEach(mr -> {
            assertThat(mr).isInstanceOf(MockRuleDto.class);
            assertThat(mr.getMethod()).isEqualTo("GET");
            assertThat(mr.getPath()).isEqualTo("/api/updated");
            assertThat(mr.getBody()).contains("updated");
            assertThat(mr.getTimestamp()).isNotNull();
        });

        var mockRule = mockRuleRepository.findById(mockId);
        assertThat(mockRule).isPresent();
        assertThat(mockRule.get().getBody()).contains("updated");
    }

    @Test
    @Order(5)
    void executeMockRuleAndVerifyUpdated() {
        var body = restClient.get()
                .uri("/m/" + boardId + "/api/updated")
                .retrieve()
                .body(String.class);
        assertThat(body).isNotNull().contains("updated");

        var cached = webhookCache.getWebhooks(boardId);
        assertThat(cached).isNotEmpty().hasSize(2);
    }

    @Test
    @Order(6)
    void receiveMockRules() {
        var mockRules = restClient.get()
                .uri("/api/boards/" + boardId + "/mocks")
                .header(Constants.OWNER_TOKEN_HEADER_KEY, token)
                .retrieve()
                .body(new ParameterizedTypeReference<List<MockRuleDto>>() {});
        assertThat(mockRules).isNotEmpty().hasSize(1);
    }

    @Test
    @Order(7)
    void deleteMockRule() {
        restClient.delete()
                .uri("/api/boards/" + boardId + "/mocks/" + mockId)
                .header(Constants.OWNER_TOKEN_HEADER_KEY, token)
                .retrieve()
                .onStatus((e) -> {
                    assertThat(e.getStatusCode().value()).isEqualTo(204);
                    return true;
                }).toBodilessEntity();

        var cached = mockRuleCache.getMockRules(boardId);
        assertThat(cached).isEmpty();

        var mockRule = mockRuleRepository.findById(mockId);
        assertThat(mockRule).isPresent();
        assertThat(mockRule.get().isDeleted()).isTrue();
    }

    @Test
    @Order(8)
    void executeDeleteMockRule() {
        var body = restClient.get()
                .uri("/m/" + boardId + "/api/updated")
                .retrieve()
                .body(String.class);
        assertThat(body).isNotNull().containsIgnoringCase("Hello from Mockboard.dev");

        var cached = webhookCache.getWebhooks(boardId);
        assertThat(cached).isNotEmpty().hasSize(3);

        await().atMost(Duration.ofSeconds(Constants.EVENT_DEDUP_PROCESS_DELAY)).untilAsserted(() -> {
            assertThat(webhookRepository.count()).isEqualTo(3);
        });
    }

    @Test
    @Order(9)
    void receiveWebhooks() {
        var webhooks = restClient.get()
                .uri("/api/boards/" + boardId + "/webhooks")
                .header(Constants.OWNER_TOKEN_HEADER_KEY, token)
                .retrieve()
                .body(new ParameterizedTypeReference<List<WebhookDto>>() {});
        assertThat(webhooks).isNotEmpty().hasSize(3);
    }

    @Test
    @Order(10)
    void deleteBoard() {
        restClient.delete()
                .uri("/api/boards/" + boardId)
                .header(Constants.OWNER_TOKEN_HEADER_KEY, token)
                .retrieve()
                .onStatus((e) -> {
                    assertThat(e.getStatusCode().value()).isEqualTo(204);
                    return true;
                }).toBodilessEntity();

        assertThat(boardCache.get(boardId)).isEmpty();
        assertThat(mockRuleCache.getMockRules(boardId)).isEmpty();
        assertThat(webhookCache.getWebhooks(boardId)).isEmpty();

        var board = boardRepository.findById(boardId);
        assertThat(board).isPresent();
        assertThat(board.get().isDeleted()).isTrue();
    }

    @Test
    @Order(11)
    void hardDelete() {
        assertThat(mockRuleRepository.count()).isEqualTo(1);
        cleanupService.hardDeleteMockRules();
        assertThat(mockRuleRepository.count()).isZero();

        assertThat(boardRepository.count()).isEqualTo(1);
        cleanupService.hardDeleteBoards();
        assertThat(boardRepository.count()).isZero();

        // cascade deleted
        assertThat(webhookRepository.count()).isZero();
    }
}
