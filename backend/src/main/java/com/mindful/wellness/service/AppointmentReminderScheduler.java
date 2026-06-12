package com.mindful.wellness.service;

import com.mindful.wellness.entity.Appointment;
import com.mindful.wellness.entity.AppointmentStatus;
import com.mindful.wellness.entity.Notification;
import com.mindful.wellness.repository.AppointmentRepository;
import com.mindful.wellness.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler for appointment reminders.
 * 
 * Automatically sends appointment reminders to students and counsellors
 * 24 hours before scheduled appointments.
 * 
 * Validates: Requirements 6, 27
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentReminderScheduler {

    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    /**
     * Send appointment reminders.
     * 
     * Runs every 5 minutes to check for appointments scheduled within the next 24 hours
     * and sends reminders to both students and counsellors.
     * 
     * Validates: Requirements 6, 27
     */
    @Scheduled(fixedDelay = 300000) // Run every 5 minutes
    @Transactional
    public void sendAppointmentReminders() {
        try {
            log.info("Starting appointment reminder scheduler");

            // Calculate the time window: 24 hours from now
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime reminderTime = now.plusHours(24);
            LocalDateTime reminderTimeEnd = reminderTime.plusMinutes(5); // 5-minute window

            // Find appointments scheduled within the next 24 hours that haven't been reminded yet
            List<Appointment> upcomingAppointments = appointmentRepository
                    .findAppointmentsForReminder(now, reminderTime, reminderTimeEnd);

            log.info("Found {} appointments for reminders", upcomingAppointments.size());

            for (Appointment appointment : upcomingAppointments) {
                sendReminderForAppointment(appointment);
            }

            log.info("Appointment reminder scheduler completed");
        } catch (Exception e) {
            log.error("Error in appointment reminder scheduler", e);
        }
    }

    /**
     * Send reminder for a specific appointment.
     * 
     * @param appointment the appointment
     */
    @Transactional
    private void sendReminderForAppointment(Appointment appointment) {
        try {
            // Get student and counsellor information
            var student = userRepository.findById(appointment.getStudentId())
                    .orElse(null);
            var counsellor = userRepository.findById(appointment.getCounsellorId())
                    .orElse(null);

            if (student == null || counsellor == null) {
                log.warn("Student or counsellor not found for appointment {}", appointment.getId());
                return;
            }

            // Send reminder to student
            String studentMessage = String.format(
                    "Reminder: You have an appointment with %s %s on %s at %s",
                    counsellor.getFirstName(),
                    counsellor.getLastName(),
                    appointment.getScheduledStartTime().toLocalDate(),
                    appointment.getScheduledStartTime().toLocalTime()
            );

            notificationService.createNotification(
                    appointment.getStudentId(),
                    "APPOINTMENT_REMINDER",
                    "Appointment Reminder",
                    studentMessage,
                    "EMAIL",
                    appointment.getId(),
                    "APPOINTMENT",
                    null
            );

            // Send reminder to counsellor
            String counsellorMessage = String.format(
                    "Reminder: You have an appointment with %s %s on %s at %s",
                    student.getFirstName(),
                    student.getLastName(),
                    appointment.getScheduledStartTime().toLocalDate(),
                    appointment.getScheduledStartTime().toLocalTime()
            );

            notificationService.createNotification(
                    appointment.getCounsellorId(),
                    "APPOINTMENT_REMINDER",
                    "Appointment Reminder",
                    counsellorMessage,
                    "EMAIL",
                    appointment.getId(),
                    "APPOINTMENT",
                    null
            );

            log.info("Sent appointment reminders for appointment {}", appointment.getId());
        } catch (Exception e) {
            log.error("Error sending reminder for appointment {}", appointment.getId(), e);
        }
    }

    /**
     * Send appointment confirmation notifications.
     * 
     * Runs when an appointment is created to send confirmation to both parties.
     * 
     * @param appointment the newly created appointment
     */
    @Transactional
    public void sendAppointmentConfirmation(Appointment appointment) {
        try {
            var student = userRepository.findById(appointment.getStudentId())
                    .orElse(null);
            var counsellor = userRepository.findById(appointment.getCounsellorId())
                    .orElse(null);

            if (student == null || counsellor == null) {
                log.warn("Student or counsellor not found for appointment {}", appointment.getId());
                return;
            }

            // Send confirmation to student
            String studentMessage = String.format(
                    "Your appointment with %s %s has been confirmed for %s at %s",
                    counsellor.getFirstName(),
                    counsellor.getLastName(),
                    appointment.getScheduledStartTime().toLocalDate(),
                    appointment.getScheduledStartTime().toLocalTime()
            );

            notificationService.createNotification(
                    appointment.getStudentId(),
                    "APPOINTMENT_CONFIRMATION",
                    "Appointment Confirmed",
                    studentMessage,
                    "EMAIL",
                    appointment.getId(),
                    "APPOINTMENT",
                    null
            );

            // Send confirmation to counsellor
            String counsellorMessage = String.format(
                    "Appointment with %s %s has been confirmed for %s at %s",
                    student.getFirstName(),
                    student.getLastName(),
                    appointment.getScheduledStartTime().toLocalDate(),
                    appointment.getScheduledStartTime().toLocalTime()
            );

            notificationService.createNotification(
                    appointment.getCounsellorId(),
                    "APPOINTMENT_CONFIRMATION",
                    "Appointment Confirmed",
                    counsellorMessage,
                    "EMAIL",
                    appointment.getId(),
                    "APPOINTMENT",
                    null
            );

            log.info("Sent appointment confirmation for appointment {}", appointment.getId());
        } catch (Exception e) {
            log.error("Error sending confirmation for appointment {}", appointment.getId(), e);
        }
    }

    /**
     * Send appointment cancellation notifications.
     * 
     * @param appointment the cancelled appointment
     */
    @Transactional
    public void sendAppointmentCancellation(Appointment appointment) {
        try {
            var student = userRepository.findById(appointment.getStudentId())
                    .orElse(null);
            var counsellor = userRepository.findById(appointment.getCounsellorId())
                    .orElse(null);

            if (student == null || counsellor == null) {
                log.warn("Student or counsellor not found for appointment {}", appointment.getId());
                return;
            }

            String cancellationReason = appointment.getCancellationReason() != null 
                    ? "\nReason: " + appointment.getCancellationReason() 
                    : "";

            // Send cancellation to student
            String studentMessage = String.format(
                    "Your appointment with %s %s scheduled for %s at %s has been cancelled.%s",
                    counsellor.getFirstName(),
                    counsellor.getLastName(),
                    appointment.getScheduledStartTime().toLocalDate(),
                    appointment.getScheduledStartTime().toLocalTime(),
                    cancellationReason
            );

            notificationService.createNotification(
                    appointment.getStudentId(),
                    "APPOINTMENT_CANCELLATION",
                    "Appointment Cancelled",
                    studentMessage,
                    "EMAIL",
                    appointment.getId(),
                    "APPOINTMENT",
                    null
            );

            // Send cancellation to counsellor
            String counsellorMessage = String.format(
                    "Appointment with %s %s scheduled for %s at %s has been cancelled.%s",
                    student.getFirstName(),
                    student.getLastName(),
                    appointment.getScheduledStartTime().toLocalDate(),
                    appointment.getScheduledStartTime().toLocalTime(),
                    cancellationReason
            );

            notificationService.createNotification(
                    appointment.getCounsellorId(),
                    "APPOINTMENT_CANCELLATION",
                    "Appointment Cancelled",
                    counsellorMessage,
                    "EMAIL",
                    appointment.getId(),
                    "APPOINTMENT",
                    null
            );

            log.info("Sent appointment cancellation for appointment {}", appointment.getId());
        } catch (Exception e) {
            log.error("Error sending cancellation for appointment {}", appointment.getId(), e);
        }
    }
}
