package com.mindful.wellness.entity;

/**
 * Enumeration for appointment status values.
 * 
 * Represents the lifecycle states of an appointment:
 * - SCHEDULED: Initial state when appointment is booked
 * - CONFIRMED: Confirmed by both parties
 * - IN_PROGRESS: Currently happening
 * - COMPLETED: Successfully completed
 * - CANCELLED: Cancelled by student or counsellor
 * - NO_SHOW: Student did not attend
 * - RESCHEDULED: Rescheduled to different time
 */
public enum AppointmentStatus {
    SCHEDULED,
    CONFIRMED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    NO_SHOW,
    RESCHEDULED
}
