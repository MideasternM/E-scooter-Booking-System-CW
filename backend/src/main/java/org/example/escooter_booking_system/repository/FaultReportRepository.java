package org.example.escooter_booking_system.repository;

import org.example.escooter_booking_system.model.FaultReport;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FaultReportRepository extends JpaRepository<FaultReport, Long> {

    // Override findAll to specify EntityGraph for fetching associated entities
    // This ensures that related entities like Booking, and Booking's User are
    // fetched appropriately (e.g., with LEFT JOINs)
    @Override
    @EntityGraph(attributePaths = { "booking", "booking.user", "booking.scooter", "reportedBy", "scooter",
            "assignedStaff" }, type = EntityGraph.EntityGraphType.FETCH)
    List<FaultReport> findAll();

    // 根据预订ID查找故障报告
    @EntityGraph(attributePaths = { "booking", "booking.user", "booking.scooter", "reportedBy", "scooter",
            "assignedStaff" }, type = EntityGraph.EntityGraphType.FETCH)
    List<FaultReport> findByBookingId(Long bookingId);

    // 根据用户ID查找故障报告
    @EntityGraph(attributePaths = { "booking", "booking.user", "booking.scooter", "reportedBy", "scooter",
            "assignedStaff" }, type = EntityGraph.EntityGraphType.FETCH)
    List<FaultReport> findByReportedById(Long userId);

    // 根据滑板车ID查找故障报告
    @EntityGraph(attributePaths = { "booking", "booking.user", "booking.scooter", "reportedBy", "scooter",
            "assignedStaff" }, type = EntityGraph.EntityGraphType.FETCH)
    List<FaultReport> findByScooterId(Long scooterId);

    // 根据维修人员ID查找故障报告
    @EntityGraph(attributePaths = { "booking", "booking.user", "booking.scooter", "reportedBy", "scooter",
            "assignedStaff" }, type = EntityGraph.EntityGraphType.FETCH)
    List<FaultReport> findByAssignedStaffId(Long staffId);

    // 根据状态查找故障报告
    @EntityGraph(attributePaths = { "booking", "booking.user", "booking.scooter", "reportedBy", "scooter",
            "assignedStaff" }, type = EntityGraph.EntityGraphType.FETCH)
    List<FaultReport> findByStatus(String status);

    // 根据故障类型查找故障报告
    @EntityGraph(attributePaths = { "booking", "booking.user", "booking.scooter", "reportedBy", "scooter",
            "assignedStaff" }, type = EntityGraph.EntityGraphType.FETCH)
    List<FaultReport> findByFaultType(String faultType);

    // 根据严重程度查找故障报告
    @EntityGraph(attributePaths = { "booking", "booking.user", "booking.scooter", "reportedBy", "scooter",
            "assignedStaff" }, type = EntityGraph.EntityGraphType.FETCH)
    List<FaultReport> findBySeverity(String severity);
}