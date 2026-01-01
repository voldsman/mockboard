package dev.mockboard.web.api;

import dev.mockboard.core.common.domain.dto.BoardDto;
import dev.mockboard.core.common.domain.dto.MockRuleDto;
import dev.mockboard.core.common.domain.response.IdResponse;
import dev.mockboard.service.BoardService;
import dev.mockboard.service.MockRuleService;
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

    @PostMapping
    public ResponseEntity<BoardDto> createBoard() {
        var boardDto = boardService.createBoard();
        return new ResponseEntity<>(boardDto, HttpStatus.CREATED);
    }

    @PostMapping("/{boardId}/mocks")
    public ResponseEntity<IdResponse> addMockRule(@PathVariable String boardId,
                                                  @RequestBody MockRuleDto mockRuleDto) {
        var boardDto = boardService.getBoardDto(boardId);
        var mockId = mockRuleService.addMockRule(boardDto.getId(), mockRuleDto);
        return new ResponseEntity<>(mockId, HttpStatus.CREATED);
    }

    @GetMapping("/{boardId}/mocks")
    public ResponseEntity<List<MockRuleDto>> getMockRules(@PathVariable String boardId) {
        var boardDto = boardService.getBoardDto(boardId);
        return new ResponseEntity<>(mockRuleService.getMockRules(boardDto.getId()), HttpStatus.OK);
    }

    @PutMapping("/{boardId}/mocks/{mockId}")
    public ResponseEntity<IdResponse> updateMockRule(@PathVariable String boardId,
                                                     @PathVariable String mockId,
                                                     @RequestBody MockRuleDto mockRuleDto) {
        var boardDto = boardService.getBoardDto(boardId);
        var response = mockRuleService.updateMockRule(boardDto.getId(), mockId, mockRuleDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{boardId}/mocks/{mockId}")
    public ResponseEntity<Void> deleteMockRule(@PathVariable String boardId, @PathVariable String mockId) {
        var boardDto = boardService.getBoardDto(boardId);
        mockRuleService.deleteMockRule(mockId, boardDto.getId());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
