package dev.mockboard.repository.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;

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
