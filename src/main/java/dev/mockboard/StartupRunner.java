package dev.mockboard;

import dev.mockboard.storage.data.doc.BoardDoc;
import dev.mockboard.storage.data.doc.MockRule;
import dev.mockboard.storage.data.repo.BoardRepository;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class StartupRunner implements CommandLineRunner {

    // todo: tmp for data storage verification

    private final BoardRepository boardRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        var faker = new Faker();
        var boardDoc = new BoardDoc();
        boardDoc.setApiKey(RandomStringUtils.secure().nextAlphanumeric(20));
        boardDoc.setOwnerToken(faker.name().maleFirstName());
        boardDoc.setShared(false);
        boardDoc.setCreatedAt(LocalDateTime.now());

        var mockRule = new MockRule();
        mockRule.setId(RandomStringUtils.secure().nextAlphabetic(10));
        mockRule.setPath("/api/users/" + RandomStringUtils.secure().nextNumeric(5) + "/profile");
        mockRule.setHeaders(Map.of("X-Header", RandomStringUtils.secure().nextAlphanumeric(10)));
        mockRule.setBody(objectMapper.writeValueAsString(Map.of(
                "username", faker.naruto().character()
        )));
        mockRule.setStatusCode(200);
        boardDoc.addMockRule(mockRule);
        boardRepository.save(boardDoc);
    }
}
