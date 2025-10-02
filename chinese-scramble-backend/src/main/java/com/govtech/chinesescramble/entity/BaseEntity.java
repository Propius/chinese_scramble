package com.govtech.chinesescramble.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * BaseEntity - Abstract base class for all JPA entities
 *
 * Provides common fields for all entities:
 * - id: Primary key (auto-generated)
 * - createdAt: Timestamp when entity was created
 * - updatedAt: Timestamp when entity was last modified
 * - version: Optimistic locking version number
 *
 * Audit Fields:
 * - Uses Spring Data JPA auditing (@CreatedDate, @LastModifiedDate)
 * - Automatically populated by AuditingEntityListener
 * - Requires @EnableJpaAuditing in main application class
 *
 * Optimistic Locking:
 * - @Version field prevents lost updates in concurrent scenarios
 * - Hibernate checks version on update
 * - Throws OptimisticLockException if version mismatch
 *
 * Usage:
 * All entity classes should extend this base class to inherit
 * common functionality and reduce boilerplate.
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Data
public abstract class BaseEntity {

    /**
     * Primary key for the entity
     * Strategy: IDENTITY - uses database auto-increment
     * PostgreSQL: Uses SERIAL type
     * H2: Uses AUTO_INCREMENT
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Timestamp when the entity was created
     * Automatically set by Spring Data JPA on entity creation
     * Immutable - cannot be updated after creation
     */
    @CreatedDate
    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Timestamp when the entity was last modified
     * Automatically updated by Spring Data JPA on entity update
     * Updated on every save operation
     */
    @LastModifiedDate
    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Version number for optimistic locking
     * Incremented automatically by Hibernate on each update
     * Prevents lost updates in concurrent scenarios
     *
     * Example scenario:
     * 1. User A loads Player entity (version=1)
     * 2. User B loads same Player entity (version=1)
     * 3. User A updates and saves (version=2)
     * 4. User B tries to update (still version=1)
     * 5. Hibernate throws OptimisticLockException (version mismatch)
     */
    @Version
    private Long version;

    /**
     * Checks if this entity is new (not yet persisted)
     *
     * @return true if id is null (entity not yet saved to database)
     */
    @Transient
    public boolean isNew() {
        return id == null;
    }

    /**
     * Gets the entity ID as a string
     * Useful for logging and debugging
     *
     * @return ID as string, or "NEW" if not yet persisted
     */
    @Transient
    public String getIdAsString() {
        return id != null ? id.toString() : "NEW";
    }
}