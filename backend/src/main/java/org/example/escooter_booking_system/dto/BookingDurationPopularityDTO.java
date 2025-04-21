package org.example.escooter_booking_system.dto;

import java.util.HashMap;
import java.util.Map;

public class BookingDurationPopularityDTO {

    // Using a Map to store counts for each label encountered
    private Map<String, Long> durationCounts = new HashMap<>();

    public Map<String, Long> getDurationCounts() {
        return durationCounts;
    }

    public void setDurationCounts(Map<String, Long> durationCounts) {
        this.durationCounts = durationCounts;
    }

    // Convenience method to increment the count for a specific duration label
    public void incrementCount(String durationLabel) {
        if (durationLabel == null || durationLabel.trim().isEmpty()) {
            durationLabel = "Unknown"; // Handle null or empty labels
        }
        durationCounts.put(durationLabel, durationCounts.getOrDefault(durationLabel, 0L) + 1);
    }

    // Initialize with expected keys if needed, otherwise they will be added
    // dynamically
    public BookingDurationPopularityDTO() {
        // Optionally pre-populate keys if you always want them present, even with count
        // 0
        // durationCounts.put("1 Hour", 0L);
        // durationCounts.put("4 Hours", 0L);
        // durationCounts.put("1 Day", 0L);
        // durationCounts.put("1 Week", 0L);
        // durationCounts.put("Unknown", 0L);
    }
}