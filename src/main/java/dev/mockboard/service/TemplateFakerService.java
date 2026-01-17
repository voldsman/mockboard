package dev.mockboard.service;

import dev.mockboard.common.faker.TemplateFakerProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateFakerService {

    private final TemplateFakerProcessor templateFakerProcessor;

    public String processTemplates(String body) {
        if (body == null || body.isEmpty()) return "{}";
        return templateFakerProcessor.applyFaker(body);
    }
}
