package dev.mockboard.repository.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, of = {"id", "boardId", "timestamp"})

@Table("webhooks")
public class Webhook extends PersistableEntity<String> implements Serializable {

    @Id
    private String id;
    @Column("board_id")
    private String boardId;
    private String method;
    private String path;
    @Column("full_url")
    private String fullUrl;
    @Column("query_params")
    private String queryParams;
    private String headers;
    private String body;
    @Column("content_type")
    private String contentType;
    @Column("status_code")
    private Integer statusCode;

    // metadata
    private boolean matched;
    @Column("received_at")
    private Instant timestamp;
    @Column("processing_time_ms")
    private long processingTimeMs;
}
