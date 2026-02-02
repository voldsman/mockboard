package dev.mockboard.service;

import dev.mockboard.Constants;
import dev.mockboard.common.domain.RequestMetadata;
import dev.mockboard.common.domain.dto.BoardDto;
import dev.mockboard.common.domain.dto.MockRuleDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MockExecutionServiceMockTest {

    @Mock private ObjectMapper objectMapper;
    @Mock private MockRuleService mockRuleService;
    @Mock private TemplateFakerService templateFakerService;

    @InjectMocks private MockExecutionService mockExecutionService;

    @Test
    void execute_defaultResponse() {
        var boardId = "board-123";
        var metadata = new RequestMetadata(
                "GET",
                "/api/test",
                "http://localhost/api/test", "", "", "", null, null
        );

        when(mockRuleService.getMockRules(any(BoardDto.class))).thenReturn(Collections.emptyList());

        var result = mockExecutionService.execute(boardId, metadata);
        assertThat(result).isNotNull();
        assertThat(result.statusCode()).isEqualTo(200);
        assertThat(result.responseBody()).isEqualTo(Constants.DEFAULT_EXECUTION_RESPONSE);
        assertThat(result.matchingMockRuleDto()).isNull();
        assertThat(result.headers().getFirst(HttpHeaders.CONTENT_TYPE))
                .isEqualTo(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void execute_exactPathMatch() {
        var boardId = "board-123";
        var metadata = new RequestMetadata(
                "POST",
                "http://localhost/api/users",
                "/api/users", "", "", "",
                "{\"name\":\"John\"}",
                "application/json"
        );

        var mockRuleDto = new MockRuleDto();
        mockRuleDto.setId("rule-1");
        mockRuleDto.setMethod("POST");
        mockRuleDto.setPath("/api/users");
        mockRuleDto.setBody("{\"id\":123}");
        mockRuleDto.setStatusCode(201);
        mockRuleDto.setDelay(0);
        mockRuleDto.compilePattern();

        when(mockRuleService.getMockRules(any(BoardDto.class))).thenReturn(List.of(mockRuleDto));
        when(templateFakerService.processTemplates("{\"id\":123}")).thenReturn("{\"id\":123}");

        var result = mockExecutionService.execute(boardId, metadata);
        assertThat(result.statusCode()).isEqualTo(201);
        assertThat(result.responseBody()).isEqualTo("{\"id\":123}");
        assertThat(result.matchingMockRuleDto()).isEqualTo(mockRuleDto);
    }

    @Test
    void execute_wildcardPath() {
        var boardId = "board-123";
        var metadata = new RequestMetadata(
                "GET",
                "http://localhost/api/users/123/profile",
                "/api/users/123/profile", "", "", "", null, null
        );

        var mockRuleDto = new MockRuleDto();
        mockRuleDto.setId("rule-1");
        mockRuleDto.setMethod("GET");
        mockRuleDto.setPath("/api/users/*/profile");
        mockRuleDto.setBody("{\"profile\":\"data\"}");
        mockRuleDto.setStatusCode(200);
        mockRuleDto.setDelay(0);
        mockRuleDto.compilePattern();

        when(mockRuleService.getMockRules(any(BoardDto.class))).thenReturn(List.of(mockRuleDto));
        when(templateFakerService.processTemplates(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = mockExecutionService.execute(boardId, metadata);
        assertThat(result.statusCode()).isEqualTo(200);
        assertThat(result.matchingMockRuleDto()).isEqualTo(mockRuleDto);
    }

    @Test
    void execute_methodMismatch() {
        var boardId = "board-123";
        var metadata = new RequestMetadata(
                "POST",
                "/api/test",
                "http://localhost/api/test", "", "", "", null, null
        );

        var getRule = new MockRuleDto();
        getRule.setMethod("GET");
        getRule.setPath("/api/test");
        getRule.compilePattern();

        when(mockRuleService.getMockRules(any(BoardDto.class))).thenReturn(List.of(getRule));

        var result = mockExecutionService.execute(boardId, metadata);
        assertThat(result.matchingMockRuleDto()).isNull();
        assertThat(result.statusCode()).isEqualTo(200);
    }

    @Test
    void execute_templatesInBody() {
        var boardId = "board-123";
        var metadata = new RequestMetadata(
                "GET",
                "http://localhost/api/test",
                "/api/test", "", "", "", null, null
        );

        var mockRuleDto = new MockRuleDto();
        mockRuleDto.setMethod("GET");
        mockRuleDto.setPath("/api/test");
        mockRuleDto.setBody("{\"name\":\"{{faker.name}}\"}");
        mockRuleDto.setStatusCode(200);
        mockRuleDto.setDelay(0);
        mockRuleDto.compilePattern();

        when(mockRuleService.getMockRules(any(BoardDto.class))).thenReturn(List.of(mockRuleDto));
        when(templateFakerService.processTemplates("{\"name\":\"{{faker.name}}\"}"))
                .thenReturn("{\"name\":\"John Doe\"}");

        var result = mockExecutionService.execute(boardId, metadata);

        assertThat(result.responseBody()).isEqualTo("{\"name\":\"John Doe\"}");
        verify(templateFakerService).processTemplates("{\"name\":\"{{faker.name}}\"}");
    }

    @Test
    void execute_delay() {
        var boardId = "board-123";
        var metadata = new RequestMetadata(
                "GET",
                "http://localhost/api/test",
                "/api/test",
                "", "", "", null, null
        );

        var mockRuleDto = new MockRuleDto();
        mockRuleDto.setMethod("GET");
        mockRuleDto.setPath("/api/test");
        mockRuleDto.setStatusCode(200);
        mockRuleDto.setDelay(100);
        mockRuleDto.compilePattern();

        when(mockRuleService.getMockRules(any(BoardDto.class))).thenReturn(List.of(mockRuleDto));

        var startTime = System.currentTimeMillis();
        mockExecutionService.execute(boardId, metadata);
        var endTime = System.currentTimeMillis();

        assertThat(endTime - startTime).isGreaterThanOrEqualTo(100);
    }
}