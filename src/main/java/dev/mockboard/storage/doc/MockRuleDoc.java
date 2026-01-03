package dev.mockboard.storage.doc;

import dev.mockboard.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = Constants.MOCK_RULES)
public class MockRuleDoc implements Serializable {

    @Id
    private String id;

    @Indexed
    private String boardId;

    @Indexed
    private String apiKey;

    private String method;

    private String path;

    private Map<String, String> headers = new HashMap<>();

    private String body;

    private int statusCode = 200;

    private LocalDateTime createdAt;
}
