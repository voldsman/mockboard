package dev.mockboard.service;

import dev.mockboard.core.common.doc.MockRuleDoc;
import dev.mockboard.core.common.domain.dto.MockRuleDto;
import dev.mockboard.core.common.domain.response.IdResponse;
import dev.mockboard.core.common.exception.NotFoundException;
import dev.mockboard.core.common.mapper.MockRuleMapper;
import dev.mockboard.core.common.validator.MockRuleValidator;
import dev.mockboard.storage.repo.MockRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MockRuleService {

    private final MockRuleValidator mockRuleValidator;
    private final MockRuleRepository mockRuleRepository;
    private final MockRuleMapper mockRuleMapper;
    private final MockExecutionService mockExecutionService;
    private final BoardService boardService;

    public IdResponse addMockRule(String boardId, MockRuleDto mockRuleDto) {
        log.debug("addMockRule for boardId={}", boardId);
        mockRuleValidator.validateMockRule(mockRuleDto);

        // todo: validate mock exists by path

        mockRuleDto.setId(null);
        mockRuleDto.setBoardId(boardId);
        mockRuleDto.setCreatedAt(LocalDateTime.now());
        var mockRuleDoc = mockRuleMapper.mapMockRuleDtoToMockRuleDoc(mockRuleDto);
        mockRuleRepository.save(mockRuleDoc);

        var board = boardService.getBoardDto(boardId);
        mockExecutionService.invalidateEngine(board.getApiKey());
        return new IdResponse(mockRuleDoc.getId());
    }

    public IdResponse updateMockRule(String boardId, String mockRuleId, MockRuleDto mockRuleDto) {
        log.debug("updateMockRule boardId={} mockRuleId={}", boardId, mockRuleId);
        mockRuleValidator.validateMockRule(mockRuleDto);

        var mockRule = getMockRuleDoc(mockRuleId, boardId);
        // map manually to avoid swallowing
        mockRule.setMethod(mockRuleDto.getMethod());
        mockRule.setPath(mockRuleDto.getPath());
        mockRule.setHeaders(mockRuleDto.getHeaders());
        mockRule.setBody(mockRuleDto.getBody());
        mockRule.setStatusCode(mockRuleDto.getStatusCode());
        mockRuleRepository.save(mockRule);

        var board = boardService.getBoardDto(boardId);
        mockExecutionService.invalidateEngine(board.getApiKey());
        return new IdResponse(mockRule.getId());
    }

    public List<MockRuleDto> getMockRules(String boardId) {
        return mockRuleRepository.findByBoardId(boardId)
                .stream()
                .map(mockRuleMapper::mapMockRuleDocToMockRuleDto)
                .toList();
    }

    public void deleteMockRule(String mockRuleId, String boardId) {
        log.debug("delete mockRule with id={}, boardId={}", mockRuleId, boardId);
        var mockRule = getMockRuleDoc(mockRuleId, boardId);
        mockRuleRepository.delete(mockRule);

        var board = boardService.getBoardDto(boardId);
        mockExecutionService.invalidateEngine(board.getApiKey());
    }

    private MockRuleDoc getMockRuleDoc(String mockRuleId, String boardId) {
        var mockRuleDocOpt = mockRuleRepository.findByIdAndBoardId(mockRuleId, boardId);
        if (mockRuleDocOpt.isEmpty()) {
            throw new NotFoundException("mockRule with id: " + mockRuleId + " not found");
        }
        return mockRuleDocOpt.get();
    }
}
