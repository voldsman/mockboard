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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = Constants.BOARDS)
public class BoardDoc implements Serializable {

    @Id
    private String id;

    @Indexed(unique = true)
    private String apiKey;

    private String ownerToken;

    @Indexed
    private LocalDateTime createdAt;
}
