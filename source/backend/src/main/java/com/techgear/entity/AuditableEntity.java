package com.techgear.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/** Lớp cha cho các bảng có cả created_at và updated_at. */
@Getter
@Setter
@MappedSuperclass
public abstract class AuditableEntity extends CreatedEntity {

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
