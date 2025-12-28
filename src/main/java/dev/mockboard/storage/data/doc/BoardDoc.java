package dev.mockboard.storage.data.doc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "boards")
public class BoardDoc implements Serializable {

//    private static final int MAX_MOCK_RULES = 12;

    @Id
    private String id;

    @Indexed
    private String apiKey;

    @Indexed
    private String ownerToken;

    private boolean shared;

    private List<MockRule> mockRules = new ArrayList<>();

    private LocalDateTime createdAt;

    public void addMockRule(MockRule mockRule) {
        mockRules.add(mockRule);
    }
}
