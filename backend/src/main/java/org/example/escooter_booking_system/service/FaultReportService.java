package org.example.escooter_booking_system.service;

import org.example.escooter_booking_system.model.FaultReport;
import java.util.List;

public interface FaultReportService {
    // 创建新的故障报告
    FaultReport createFaultReport(FaultReport faultReport);
    
    // 获取特定故障报告
    FaultReport getFaultReportById(Long id);
    
    // 获取预订相关的故障报告
    List<FaultReport> getFaultReportsByBooking(Long bookingId);
    
    // 获取用户报告的故障
    List<FaultReport> getFaultReportsByUser(Long userId);
    
    // 获取滑板车的故障报告
    List<FaultReport> getFaultReportsByScooter(Long scooterId);
    
    // 获取分配给特定维修人员的故障报告
    List<FaultReport> getFaultReportsByStaff(Long staffId);
    
    // 更新故障报告
    FaultReport updateFaultReport(Long id, FaultReport faultReportDetails);
    
    // 分配故障给维修人员
    FaultReport assignToStaff(Long faultReportId, Long staffId);
    
    // 更新故障状态
    FaultReport updateStatus(Long id, String status);
    
    // 添加维修解决方案
    FaultReport addResolution(Long id, String resolution);
    
    // 获取特定状态的故障报告
    List<FaultReport> getFaultReportsByStatus(String status);
    
    // 获取特定类型的故障报告
    List<FaultReport> getFaultReportsByType(String faultType);
    
    // 获取特定严重程度的故障报告
    List<FaultReport> getFaultReportsBySeverity(String severity);
} 