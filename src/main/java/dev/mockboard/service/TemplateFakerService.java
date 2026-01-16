package dev.mockboard.service;

import dev.mockboard.common.engine.TemplateFakerEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateFakerService {

    private final TemplateFakerEngine templateFakerEngine;

    public String processTemplates(String body) {
        if (body == null || body.isEmpty()) return "{}";
        return templateFakerEngine.applyFaker(body);
    }
}
