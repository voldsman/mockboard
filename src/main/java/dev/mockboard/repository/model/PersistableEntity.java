package dev.mockboard.repository.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;

/**
 * Base class for entities with manual ID generation.
 * Manually setting IDs causes Spring Data JDBC to
 * incorrectly default to UPDATE instead of INSERT when using .save(T).
 * This base class uses an explicit flag to force INSERT for new entities with pre-assigned IDs.
 */
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class PersistableEntity<ID> implements Persistable<ID> {

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }

    public void markNotNew() {
        this.isNew = false;
    }
}
