package org.example.escooter_booking_system.repository;

import org.example.escooter_booking_system.model.FaultReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FaultReportRepository extends JpaRepository<FaultReport, Long> {
    // 根据预订ID查找故障报告
    List<FaultReport> findByBookingId(Long bookingId);
    
    // 根据用户ID查找故障报告
    List<FaultReport> findByReportedById(Long userId);
    
    // 根据滑板车ID查找故障报告
    List<FaultReport> findByScooterId(Long scooterId);
    
    // 根据维修人员ID查找故障报告
    List<FaultReport> findByAssignedStaffId(Long staffId);
    
    // 根据状态查找故障报告
    List<FaultReport> findByStatus(String status);
    
    // 根据故障类型查找故障报告
    List<FaultReport> findByFaultType(String faultType);
    
    // 根据严重程度查找故障报告
    List<FaultReport> findBySeverity(String severity);
} 