package dev.mockboard.web.api;

import dev.mockboard.core.common.domain.dto.BoardDto;
import dev.mockboard.core.common.domain.dto.MockRuleDto;
import dev.mockboard.core.common.domain.response.IdResponse;
import dev.mockboard.core.utils.RequestUtils;
import dev.mockboard.service.BoardSecurityService;
import dev.mockboard.service.BoardService;
import dev.mockboard.service.MockRuleService;
import dev.mockboard.storage.cache.ratelimiter.BoardCreationRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final MockRuleService mockRuleService;
    private final BoardSecurityService boardSecurityService;
    private final BoardCreationRateLimiter boardCreationRateLimiter;

    private static final String OWNER_TOKEN_HEADER_KEY = "X-Owner-Token";

    @PostMapping
    public ResponseEntity<BoardDto> createBoard(HttpServletRequest request) {
        var ipAddress = RequestUtils.getClientIp(request);
        boardCreationRateLimiter.checkLimit(ipAddress);

        var boardDto = boardService.createBoard();
        return new ResponseEntity<>(boardDto, HttpStatus.CREATED);
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<BoardDto> getBoard(@PathVariable String boardId,
                                             @RequestHeader(OWNER_TOKEN_HEADER_KEY) String ownerToken) {
        var boardDto = boardSecurityService.validateOwnership(boardId, ownerToken);
        return new ResponseEntity<>(boardDto, HttpStatus.OK);
    }

    @PostMapping("/{boardId}/mocks")
    public ResponseEntity<IdResponse> addMockRule(@PathVariable String boardId,
                                                  @RequestBody MockRuleDto mockRuleDto,
                                                  @RequestHeader(OWNER_TOKEN_HEADER_KEY) String ownerToken) {
        var boardDto = boardSecurityService.validateOwnership(boardId, ownerToken);
        var mockId = mockRuleService.addMockRule(boardDto, mockRuleDto);
        return new ResponseEntity<>(mockId, HttpStatus.CREATED);
    }

    @GetMapping("/{boardId}/mocks")
    public ResponseEntity<List<MockRuleDto>> getMockRules(@PathVariable String boardId,
                                                          @RequestHeader(OWNER_TOKEN_HEADER_KEY) String ownerToken) {
        var boardDto = boardSecurityService.validateOwnership(boardId, ownerToken);
        var mockRules = mockRuleService.getMockRuleDtos(boardDto);
        return new ResponseEntity<>(mockRules, HttpStatus.OK);
    }

    @PutMapping("/{boardId}/mocks/{mockId}")
    public ResponseEntity<IdResponse> updateMockRule(@PathVariable String boardId,
                                                     @PathVariable String mockId,
                                                     @RequestBody MockRuleDto mockRuleDto,
                                                     @RequestHeader(OWNER_TOKEN_HEADER_KEY) String ownerToken) {
        var boardDto = boardSecurityService.validateOwnership(boardId, ownerToken);
        var response = mockRuleService.updateMockRule(boardDto, mockId, mockRuleDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{boardId}/mocks/{mockId}")
    public ResponseEntity<Void> deleteMockRule(@PathVariable String boardId,
                                               @PathVariable String mockId,
                                               @RequestHeader(OWNER_TOKEN_HEADER_KEY) String ownerToken) {
        var boardDto = boardSecurityService.validateOwnership(boardId, ownerToken);
        mockRuleService.deleteMockRule(boardDto, mockId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
