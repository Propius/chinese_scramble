package com.govtech.chinesescramble.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JpaAuditingConfig - Enables JPA Auditing for automatic timestamp management
 *
 * Features:
 * - Automatic createdAt timestamp on entity creation
 * - Automatic updatedAt timestamp on entity modification
 * - Works with @CreatedDate and @LastModifiedDate annotations
 * - Requires @EntityListeners(AuditingEntityListener.class) on entities
 *
 * Applied to:
 * - BaseEntity (all entities extend this)
 * - createdAt: Set on INSERT, never updated
 * - updatedAt: Set on INSERT and UPDATE
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
    // JPA Auditing is now enabled for all entities
}