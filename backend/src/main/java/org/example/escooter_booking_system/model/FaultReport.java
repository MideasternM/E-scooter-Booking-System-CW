package org.example.escooter_booking_system.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
public class FaultReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking; // 关联的预订

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User reportedBy; // 报告故障的用户

    @ManyToOne
    @JoinColumn(name = "scooter_id", nullable = false)
    private Scooter scooter;

    @Column(nullable = false)
    private String faultType; // MECHANICAL, ELECTRICAL, BATTERY, OTHER等

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String severity; // HIGH, MEDIUM, LOW

    @Column(nullable = false)
    private String status = "NEW"; // NEW, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED

    @ManyToOne
    @JoinColumn(name = "assigned_staff_id")
    private Staff assignedStaff;

    @Column(nullable = false)
    private String location; // 故障发生的具体位置

    @Column
    private String resolution; // 解决方案

    @Column
    private String staffNotes; // 维修人员的备注

    @Column(nullable = false)
    private Date reportedAt = new Date();

    @Column
    private Date assignedAt; // 分配给维修人员的时间

    @Column
    private Date startedAt; // 开始处理的时间

    @Column
    private Date resolvedAt; // 解决时间

    @Column
    private Boolean requiresPartReplacement = false; // 是否需要更换零件

    @Column
    private String replacementParts; // 需要更换的零件清单

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public User getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(User reportedBy) {
        this.reportedBy = reportedBy;
    }

    public Scooter getScooter() {
        return scooter;
    }

    public void setScooter(Scooter scooter) {
        this.scooter = scooter;
    }

    public String getFaultType() {
        return faultType;
    }

    public void setFaultType(String faultType) {
        this.faultType = faultType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Staff getAssignedStaff() {
        return assignedStaff;
    }

    public void setAssignedStaff(Staff assignedStaff) {
        this.assignedStaff = assignedStaff;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getStaffNotes() {
        return staffNotes;
    }

    public void setStaffNotes(String staffNotes) {
        this.staffNotes = staffNotes;
    }

    public Date getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(Date reportedAt) {
        this.reportedAt = reportedAt;
    }

    public Date getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Date assignedAt) {
        this.assignedAt = assignedAt;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    public Date getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Date resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public Boolean getRequiresPartReplacement() {
        return requiresPartReplacement;
    }

    public void setRequiresPartReplacement(Boolean requiresPartReplacement) {
        this.requiresPartReplacement = requiresPartReplacement;
    }

    public String getReplacementParts() {
        return replacementParts;
    }

    public void setReplacementParts(String replacementParts) {
        this.replacementParts = replacementParts;
    }
} 