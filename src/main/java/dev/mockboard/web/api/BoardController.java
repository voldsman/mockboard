package dev.mockboard.web.api;

import dev.mockboard.common.domain.dto.BoardDto;
import dev.mockboard.common.domain.dto.MockRuleDto;
import dev.mockboard.common.domain.response.IdResponse;
import dev.mockboard.service.BoardSecurityService;
import dev.mockboard.service.BoardService;
import dev.mockboard.service.MockRuleService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static dev.mockboard.Constants.OWNER_TOKEN_HEADER_KEY;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final MockRuleService mockRuleService;
    private final BoardSecurityService boardSecurityService;

    @PostMapping
    public ResponseEntity<BoardDto> createBoard(HttpServletRequest _request) {
        var boardDto = boardService.createBoard();
        return new ResponseEntity<>(boardDto, HttpStatus.CREATED);
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<BoardDto> getBoard(@PathVariable String boardId,
                                             @RequestHeader(OWNER_TOKEN_HEADER_KEY) String ownerToken) {
        var boardDto = boardSecurityService.validateOwnershipAndGet(boardId, ownerToken);
        return new ResponseEntity<>(boardDto, HttpStatus.OK);
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<Void> deleteBoard(@PathVariable String boardId,
                                            @RequestHeader(OWNER_TOKEN_HEADER_KEY) String ownerToken) {
        var boardDto = boardSecurityService.validateOwnershipAndGet(boardId, ownerToken);
        boardService.deleteBoard(boardDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{boardId}/mocks")
    public ResponseEntity<IdResponse> addMockRule(@PathVariable String boardId,
                                                  @RequestBody MockRuleDto mockRuleDto,
                                                  @RequestHeader(OWNER_TOKEN_HEADER_KEY) String ownerToken) {
        var boardDto = boardSecurityService.validateOwnershipAndGet(boardId, ownerToken);
        var mockId = mockRuleService.createMockRule(boardDto, mockRuleDto);
        return new ResponseEntity<>(mockId, HttpStatus.CREATED);
    }

    @GetMapping("/{boardId}/mocks")
    public ResponseEntity<List<MockRuleDto>> getMockRules(@PathVariable String boardId,
                                                          @RequestHeader(OWNER_TOKEN_HEADER_KEY) String ownerToken) {
        var boardDto = boardSecurityService.validateOwnershipAndGet(boardId, ownerToken);
        var mockRules = mockRuleService.getMockRules(boardDto);
        return new ResponseEntity<>(mockRules, HttpStatus.OK);
    }

    @PutMapping("/{boardId}/mocks/{mockRuleId}")
    public ResponseEntity<IdResponse> updateMockRule(@PathVariable String boardId,
                                                     @PathVariable String mockRuleId,
                                                     @RequestBody MockRuleDto mockRuleDto,
                                                     @RequestHeader(OWNER_TOKEN_HEADER_KEY) String ownerToken) {
        var boardDto = boardSecurityService.validateOwnershipAndGet(boardId, ownerToken);
        var response = mockRuleService.updateMockRule(boardDto, mockRuleId, mockRuleDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{boardId}/mocks/{mockRuleId}")
    public ResponseEntity<Void> deleteMockRule(@PathVariable String boardId,
                                               @PathVariable String mockRuleId,
                                               @RequestHeader(OWNER_TOKEN_HEADER_KEY) String ownerToken) {
        var boardDto = boardSecurityService.validateOwnershipAndGet(boardId, ownerToken);
        mockRuleService.deleteMockRule(boardDto, mockRuleId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
