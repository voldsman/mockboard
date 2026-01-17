package dev.mockboard.web.sse;

import dev.mockboard.service.BoardSecurityService;
import dev.mockboard.config.sse.SseManager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class SseController {

    private final BoardSecurityService boardSecurityService;
    private final SseManager sseManager;

    @GetMapping("/{boardId}/stream")
    public SseEmitter subscribe(@PathVariable String boardId,
                                @RequestParam String token) {
        var boardDto = boardSecurityService.validateOwnershipAndGet(boardId, token);
        return sseManager.subscribe(boardDto);
    }
}
