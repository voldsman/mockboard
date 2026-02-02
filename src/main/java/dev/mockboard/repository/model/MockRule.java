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

@Table("mock_rules")
public class MockRule extends PersistableEntity<String> implements Serializable {

    @Id
    private String id;
    @Column("board_id")
    private String boardId;
    private String method;
    private String path;
    private String headers;
    private String body;
    @Column("status_code")
    private int statusCode;
    private int delay; //ms

    @Column("created_at")
    private Instant timestamp;
    private boolean deleted;
}
