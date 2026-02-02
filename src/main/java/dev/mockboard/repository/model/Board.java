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
@EqualsAndHashCode(callSuper = false, of = {"id", "ownerToken"})

@Table("boards")
public class Board extends PersistableEntity<String> implements Serializable {

    @Id
    private String id;

    @Column("owner_token")
    private String ownerToken;

    @Column("created_at")
    private Instant timestamp;

    private boolean deleted;
}
