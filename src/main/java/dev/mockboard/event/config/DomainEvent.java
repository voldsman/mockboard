package dev.mockboard.event.config;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;

@Data
@AllArgsConstructor
public class DomainEvent<T> implements Serializable {

    private EventType type;
    private T entity;
    private String entityId;
    private Class<T> entityClass;
    private Instant timestamp;

    public DomainEvent(EventType type, T entity, String entityId, Class<T> entityClass) {
        this.type = type;
        this.entity = entity;
        this.entityId = entityId;
        this.entityClass = entityClass;
        this.timestamp = Instant.now();
    }

    public static <T> DomainEvent<T> create(T entity, Class<T> entityClass) {
        return new DomainEvent<>(EventType.CREATE, entity, null, entityClass);
    }

    public static <T> DomainEvent<T> update(T entity, String entityId, Class<T> entityClass) {
        return new DomainEvent<>(EventType.UPDATE, entity, entityId, entityClass);
    }

    public static <T> DomainEvent<T> delete(String entityId, Class<T> entityClass) {
        return new DomainEvent<>(EventType.DELETE, null, entityId, entityClass);
    }
}
