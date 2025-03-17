package org.example.escooter_booking_system.repository;

import org.example.escooter_booking_system.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    // 根据员工编号查找
    Staff findByStaffNumber(String staffNumber);
    
    // 根据状态查找
    List<Staff> findByStatus(String status);
    
    // 根据区域查找
    List<Staff> findByArea(String area);
    
    // 根据职位查找
    List<Staff> findByPosition(String position);
    
    // 根据邮箱查找
    Staff findByEmail(String email);
    
    // 根据电话号码查找
    Staff findByPhoneNumber(String phoneNumber);
} 