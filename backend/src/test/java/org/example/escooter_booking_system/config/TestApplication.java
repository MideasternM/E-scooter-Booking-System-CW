package org.example.escooter_booking_system.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
    "org.example.escooter_booking_system.service",
    "org.example.escooter_booking_system.controller", // 如果需要测试控制器层与服务层的集成，也扫描controller
    "org.example.escooter_booking_system.component", // 如果有其他自定义组件
    "org.example.escooter_booking_system.config.security", // 如果安全配置是测试的一部分
    "org.example.escooter_booking_system.util" // Added to scan for JwtTokenUtil and other utilities
})
@EntityScan(basePackages = "org.example.escooter_booking_system.model")
@EnableJpaRepositories(basePackages = "org.example.escooter_booking_system.repository")
public class TestApplication {
    // This class serves as a minimal Spring Boot application configuration
    // for running integration tests in an isolated context.
    // It ensures that only necessary components are loaded.
} 