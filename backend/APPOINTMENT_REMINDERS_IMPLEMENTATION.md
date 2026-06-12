# Appointment Reminders Implementation

## Overview

This document describes the implementation of the appointment reminder system for the Mindful Wellness Platform. The system automatically sends appointment reminders to students and counsellors 24 hours before scheduled appointments, along with confirmation and cancellation notifications.

## Validates Requirements

- **Requirement 6**: Appointment Booking - Confirmation notifications sent to both student and counsellor
- **Requirement 27**: Notification System - Multi-channel notifications with scheduling and delivery

## Architecture

### Components

1. **Notification Entity** (`Notification.java`)
   - Represents a notification in the system
   - Tracks notification status (PENDING, SENT, FAILED, READ)
   - Supports multiple channels (EMAIL, IN_APP, SMS)
   - Stores related entity information for context

2. **NotificationStatus Enum** (`NotificationStatus.java`)
   - PENDING: Notification created but not yet sent
   - SENT: Notification successfully sent
   - FAILED: Notification failed to send
   - READ: Notification has been read by user

3. **NotificationRepository** (`NotificationRepository.java`)
   - Database access for notifications
   - Complex queries for finding pending, failed, and scheduled notifications
   - Filtering by user, type, status, and date range

4. **NotificationService** (`NotificationService.java`)
   - Core service for notification management
   - Handles creation, sending, and status updates
   - Implements retry logic for failed notifications
   - Supports async notification sending

5. **AppointmentReminderScheduler** (`AppointmentReminderScheduler.java`)
   - Scheduled task runner for appointment reminders
   - Sends reminders 24 hours before appointments
   - Sends confirmation notifications when appointments are booked
   - Sends cancellation notifications when appointments are cancelled
   - Runs every 5 minutes to check for upcoming appointments

6. **NotificationController** (`NotificationController.java`)
   - REST endpoints for notification management
   - Retrieve notifications for authenticated user
   - Mark notifications as read
   - Get unread notification count

## Database Schema

### Notifications Table

```sql
CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    related_entity_id UUID,
    related_entity_type VARCHAR(50),
    status VARCHAR(50) NOT NULL,
    channel VARCHAR(50),
    sent_at TIMESTAMP,
    read_at TIMESTAMP,
    scheduled_for TIMESTAMP,
    retry_count INTEGER DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_notifications_user_id (user_id),
    INDEX idx_notifications_status (status),
    INDEX idx_notifications_created_at (created_at),
    INDEX idx_notifications_user_status (user_id, status)
);
```

## Workflow

### 1. Appointment Booking Flow

```
1. Student calls POST /api/appointments
2. AppointmentService.bookAppointment() validates and creates appointment
3. AppointmentReminderScheduler.sendAppointmentConfirmation() is called
4. Confirmation notifications are created for both student and counsellor
5. Notifications are sent via email (or queued for later)
6. Response returned to student with appointment details
```

### 2. Appointment Reminder Flow

```
1. AppointmentReminderScheduler.sendAppointmentReminders() runs every 5 minutes
2. Queries for appointments scheduled 24 hours from now (±5 minute window)
3. For each appointment found:
   - Retrieves student and counsellor information
   - Creates reminder notification for student
   - Creates reminder notification for counsellor
   - Notifications are sent via email
4. Scheduler completes and waits for next execution
```

### 3. Appointment Cancellation Flow

```
1. Counsellor calls DELETE /api/appointments/{id}
2. AppointmentService.cancelAppointment() updates appointment status
3. AppointmentReminderScheduler.sendAppointmentCancellation() is called
4. Cancellation notifications are created for both parties
5. Notifications are sent via email
6. Response returned to counsellor
```

### 4. Notification Sending Flow

```
1. NotificationService.createNotification() creates notification record
2. If immediate send (no scheduledFor):
   - NotificationService.sendNotificationAsync() is called
   - Notification is sent based on channel (EMAIL, IN_APP, SMS)
   - Status updated to SENT or FAILED
3. If scheduled:
   - Notification stored with PENDING status
   - Scheduler picks it up when time arrives
   - Notification is sent and status updated
```

## API Endpoints

### Notification Management

#### Get All Notifications
```
GET /api/notifications
Authorization: Bearer {token}

Response:
[
  {
    "id": "uuid",
    "userId": "uuid",
    "notificationType": "APPOINTMENT_REMINDER",
    "title": "Appointment Reminder",
    "message": "You have an appointment tomorrow at 10:00 AM",
    "status": "SENT",
    "channel": "EMAIL",
    "sentAt": "2024-01-15T09:00:00",
    "createdAt": "2024-01-14T09:00:00"
  }
]
```

#### Get Unread Notifications
```
GET /api/notifications/unread
Authorization: Bearer {token}

Response:
[
  {
    "id": "uuid",
    "userId": "uuid",
    "notificationType": "APPOINTMENT_REMINDER",
    "title": "Appointment Reminder",
    "message": "You have an appointment tomorrow at 10:00 AM",
    "status": "PENDING",
    "channel": "IN_APP",
    "createdAt": "2024-01-14T09:00:00"
  }
]
```

#### Get Unread Notification Count
```
GET /api/notifications/unread/count
Authorization: Bearer {token}

Response:
5
```

#### Get Specific Notification
```
GET /api/notifications/{id}
Authorization: Bearer {token}

Response:
{
  "id": "uuid",
  "userId": "uuid",
  "notificationType": "APPOINTMENT_REMINDER",
  "title": "Appointment Reminder",
  "message": "You have an appointment tomorrow at 10:00 AM",
  "status": "SENT",
  "channel": "EMAIL",
  "sentAt": "2024-01-15T09:00:00",
  "createdAt": "2024-01-14T09:00:00"
}
```

#### Mark Notification as Read
```
PUT /api/notifications/{id}/read
Authorization: Bearer {token}

Response: 204 No Content
```

## Configuration

### Application Properties

```properties
# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true

# Notification Configuration
app.mail.from=noreply@mindfulwellness.com
app.mail.enabled=true
app.notification.max-retries=3
```

### Scheduling Configuration

The application uses Spring's `@EnableScheduling` annotation to enable scheduled tasks. The `AppointmentReminderScheduler` runs every 5 minutes to check for upcoming appointments.

```java
@Scheduled(fixedDelay = 300000) // 5 minutes
public void sendAppointmentReminders() {
    // Implementation
}
```

## Testing

### Unit Tests

1. **AppointmentReminderSchedulerTest**
   - Tests reminder sending for upcoming appointments
   - Tests confirmation notification sending
   - Tests cancellation notification sending
   - Tests error handling when users not found

2. **NotificationServiceTest**
   - Tests notification creation
   - Tests notification sending
   - Tests status transitions
   - Tests retry logic
   - Tests notification retrieval

### Integration Tests

1. **AppointmentReminderIntegrationTest**
   - Tests complete appointment booking flow with notifications
   - Tests appointment cancellation with notifications
   - Tests reminder scheduler finding upcoming appointments
   - Tests notification status transitions
   - Tests multiple appointments with reminders

## Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AppointmentReminderSchedulerTest

# Run with coverage
mvn test jacoco:report
```

## Error Handling

### Notification Failures

When a notification fails to send:

1. Error is logged
2. Retry count is incremented
3. If retry count < max retries:
   - Status set to PENDING
   - Scheduled for retry in 5 * retryCount minutes
4. If retry count >= max retries:
   - Status set to FAILED
   - Error message stored
   - Manual intervention may be required

### Missing Users

If student or counsellor not found when sending notifications:
- Warning is logged
- Notification is skipped
- No error is thrown (graceful degradation)

## Performance Considerations

1. **Database Indexes**
   - Indexes on user_id, status, created_at for fast queries
   - Composite index on (user_id, status) for common queries

2. **Async Sending**
   - Notifications sent asynchronously to avoid blocking
   - Uses Spring's @Async annotation

3. **Batch Processing**
   - Scheduler processes multiple appointments in single run
   - Reduces database queries

4. **Caching**
   - User information cached during reminder sending
   - Reduces repeated database lookups

## Future Enhancements

1. **SMS Notifications**
   - Integrate with SMS provider (Twilio, AWS SNS)
   - Send SMS reminders for critical appointments

2. **Push Notifications**
   - Integrate with Firebase Cloud Messaging
   - Send push notifications to mobile apps

3. **Notification Preferences**
   - Allow users to customize notification channels
   - Allow users to opt-out of specific notification types

4. **Advanced Scheduling**
   - Support for multiple reminders (24h, 1h, 15m before)
   - Customizable reminder times per user

5. **Notification Templates**
   - Customizable email templates
   - Support for multiple languages

6. **Analytics**
   - Track notification delivery rates
   - Monitor notification performance
   - Generate notification reports

## Troubleshooting

### Reminders Not Sending

1. Check if scheduler is enabled: `@EnableScheduling` on main class
2. Check application logs for errors
3. Verify email configuration is correct
4. Check if appointments exist in database
5. Verify notification records are being created

### Email Not Sending

1. Verify email configuration in application.properties
2. Check if `app.mail.enabled=true`
3. Verify SMTP credentials are correct
4. Check firewall/network settings
5. Review email provider's security settings

### High Notification Latency

1. Check database query performance
2. Verify indexes are created
3. Check email provider's response time
4. Consider increasing scheduler frequency
5. Monitor application memory usage

## References

- Spring Boot Scheduling: https://spring.io/guides/gs/scheduling-tasks/
- Spring Mail: https://spring.io/guides/gs/sending-email/
- JPA Queries: https://docs.spring.io/spring-data/jpa/docs/current/reference/html/
