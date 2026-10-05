package com.example.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;

//entity ini akan menjadi parent class untuk entity lainnya, seperti UserEntity dan RefreshToken. Dengan menggunakan @MappedSuperclass, kita bisa memastikan bahwa field createdAt dan updatedAt akan diwariskan ke semua entity yang mewarisi BaseEntity, sehingga kita tidak perlu mendefinisikan ulang field tersebut di setiap entity.
@MappedSuperclass
public abstract class BaseEntity {
    
    @Column(name = "created_at", nullable = false, updatable = false)
    protected LocalDateTime createdAt;

    @Column(name = "updated_at")
    protected LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;

    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
