package org.example.escooter_booking_system.service;

import org.example.escooter_booking_system.model.Staff;
import org.example.escooter_booking_system.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Date;

@Service
public class StaffServiceImpl implements StaffService {

    @Autowired
    private StaffRepository staffRepository;

    @Override
    public Staff addStaff(Staff staff) {
        staff.setCreatedAt(new Date());
        return staffRepository.save(staff);
    }

    @Override
    public Staff getStaffById(Long id) {
        return staffRepository.findById(id).orElse(null);
    }

    @Override
    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }

    @Override
    public Staff getStaffByNumber(String staffNumber) {
        return staffRepository.findByStaffNumber(staffNumber);
    }

    @Override
    public Staff updateStaff(Long id, Staff staffDetails) {
        Staff staff = staffRepository.findById(id).orElse(null);
        if (staff != null) {
            staff.setName(staffDetails.getName());
            staff.setPosition(staffDetails.getPosition());
            staff.setPhoneNumber(staffDetails.getPhoneNumber());
            staff.setEmail(staffDetails.getEmail());
            staff.setArea(staffDetails.getArea());
            return staffRepository.save(staff);
        }
        return null;
    }

    @Override
    public void deleteStaff(Long id) {
        staffRepository.deleteById(id);
    }

    @Override
    public List<Staff> getStaffByStatus(String status) {
        return staffRepository.findByStatus(status);
    }

    @Override
    public List<Staff> getStaffByArea(String area) {
        return staffRepository.findByArea(area);
    }

    @Override
    public List<Staff> getStaffByPosition(String position) {
        return staffRepository.findByPosition(position);
    }

    @Override
    public Staff updateStaffStatus(Long id, String status) {
        Staff staff = staffRepository.findById(id).orElse(null);
        if (staff != null) {
            staff.setStatus(status);
            return staffRepository.save(staff);
        }
        return null;
    }

    @Override
    public Staff updateStaffArea(Long id, String area) {
        Staff staff = staffRepository.findById(id).orElse(null);
        if (staff != null) {
            staff.setArea(area);
            return staffRepository.save(staff);
        }
        return null;
    }
} 