package org.example.escooter_booking_system.service;

import org.example.escooter_booking_system.model.Staff;
import java.util.List;

public interface StaffService {
    // 添加新员工
    Staff addStaff(Staff staff);
    
    // 获取特定员工
    Staff getStaffById(Long id);
    
    // 获取所有员工
    List<Staff> getAllStaff();
    
    // 根据员工编号获取员工
    Staff getStaffByNumber(String staffNumber);
    
    // 更新员工信息
    Staff updateStaff(Long id, Staff staffDetails);
    
    // 删除员工
    void deleteStaff(Long id);
    
    // 获取特定状态的员工
    List<Staff> getStaffByStatus(String status);
    
    // 获取特定区域的员工
    List<Staff> getStaffByArea(String area);
    
    // 获取特定职位的员工
    List<Staff> getStaffByPosition(String position);
    
    // 更新员工状态
    Staff updateStaffStatus(Long id, String status);
    
    // 更新员工区域
    Staff updateStaffArea(Long id, String area);
} 