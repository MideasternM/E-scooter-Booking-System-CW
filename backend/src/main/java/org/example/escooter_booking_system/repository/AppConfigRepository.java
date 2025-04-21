package org.example.escooter_booking_system.repository;

import org.example.escooter_booking_system.model.AppConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppConfigRepository extends JpaRepository<AppConfig, String> {
    // Spring Data JPA 会自动实现基本的 CRUD 操作
    // 可以根据 key 查找
    Optional<AppConfig> findByKey(String key);
}