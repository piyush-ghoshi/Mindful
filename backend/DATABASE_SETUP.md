# Mindful Wellness Platform - Database Setup Guide

## Overview

This document provides instructions for setting up the PostgreSQL database and running Flyway migrations for the Mindful Wellness Platform.

## Prerequisites

- PostgreSQL 12 or higher installed and running
- Maven 3.6+ installed
- Java 17+ installed
- Git (for version control)

## Database Setup Steps

### 1. Create PostgreSQL Database

Connect to PostgreSQL and create the database:

```bash
# Connect to PostgreSQL as superuser
psql -U postgres

# Create the database
CREATE DATABASE mindful_wellness_db;

# Create a dedicated user (optional but recommended)
CREATE USER mindful_user WITH PASSWORD 'secure_password';

# Grant privileges
GRANT ALL PRIVILEGES ON DATABASE mindful_wellness_db TO mindful_user;

# Connect to the new database
\c mindful_wellness_db

# Grant schema privileges
GRANT ALL ON SCHEMA public TO mindful_user;

# Exit psql
\q
```

### 2. Update Database Configuration

Edit `src/main/resources/application.properties` with your database credentials:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mindful_wellness_db
spring.datasource.username=postgres
spring.datasource.password=your_password
```

### 3. Run Flyway Migrations

#### Option A: Using Maven

```bash
# Navigate to the backend directory
cd backend

# Run migrations using Maven
mvn flyway:migrate

# Verify migrations
mvn flyway:info
```

#### Option B: Using Spring Boot

```bash
# Navigate to the backend directory
cd backend

# Run the Spring Boot application (migrations run automatically on startup)
mvn spring-boot:run
```

### 4. Verify Database Schema

Connect to the database and verify the schema was created:

```bash
# Connect to the database
psql -U postgres -d mindful_wellness_db

# List all tables
\dt

# View table structure
\d users
\d appointments
\d messages
\d mood_entries

# Check indexes
\di

# Exit
\q
```

## Database Schema Overview

### Core Tables

#### User Management
- **users**: Core user information for all roles (STUDENT, COUNSELLOR, ADMIN)
- **student_profiles**: Student-specific profile information
- **counsellor_profiles**: Counsellor-specific profile information

#### Appointment Management
- **appointments**: Appointment records with status tracking
- **availability_schedules**: Counsellor availability by day of week
- **availability_exceptions**: Holidays and time-off exceptions

#### Messaging
- **conversations**: Conversation threads between users
- **messages**: Individual encrypted messages
- **attachments**: File attachments to messages

#### Mood Tracking
- **mood_entries**: Daily mood check-ins and journal entries

#### Chatbot
- **chatbot_interactions**: AI chatbot conversation logs

#### Forum
- **forum_posts**: Peer support forum posts
- **forum_comments**: Comments on forum posts
- **forum_votes**: Voting on posts and comments
- **forum_reports**: Moderation reports

#### Resources
- **resources**: Wellness resources (articles, videos, etc.)
- **offline_resources**: Non-digital resources and referrals
- **resource_bookmarks**: User bookmarks

#### Wellness Tracking
- **wellness_goals**: Personal wellness goals
- **achievements**: Earned badges and achievements
- **wellness_roadmaps**: Personalized wellness plans

#### Crisis Support
- **crisis_events**: Crisis incidents and escalations
- **emergency_contacts**: Emergency hotline information

#### Notifications
- **notifications**: User notifications
- **notification_preferences**: User notification settings

#### Feedback
- **feedback**: User feedback and complaints
- **feedback_responses**: Admin responses to feedback

#### Audit
- **audit_logs**: System audit trail

## Key Features of the Schema

### Security
- UUID primary keys for all tables
- Role-based access control (RBAC) with user roles
- Encrypted message storage
- Audit logging for sensitive operations
- Password hash storage (not plain text)

### Data Integrity
- Foreign key constraints for referential integrity
- Check constraints for valid enum values
- Unique constraints for email and other identifiers
- NOT NULL constraints for required fields

### Performance
- Comprehensive indexing on frequently queried columns
- Indexes on foreign keys for join performance
- Indexes on status and date fields for filtering
- Connection pooling with HikariCP

### Scalability
- JSONB columns for flexible data storage (notification preferences, privacy settings)
- Partitioning-ready design for large tables
- Efficient query patterns for common operations

## Flyway Migration Management

### Migration Files

Migrations are stored in `src/main/resources/db/migration/` with naming convention:
- `V1__Initial_Schema.sql` - Initial database schema
- `V2__Add_new_feature.sql` - Future migrations

### Running Migrations

```bash
# Run pending migrations
mvn flyway:migrate

# View migration history
mvn flyway:info

# Validate migrations
mvn flyway:validate

# Repair migration history (use with caution)
mvn flyway:repair
```

### Creating New Migrations

1. Create a new file: `V{version}__Description.sql`
2. Write SQL statements
3. Run `mvn flyway:migrate`

Example:
```sql
-- V2__Add_user_preferences.sql
ALTER TABLE users ADD COLUMN theme_preference VARCHAR(50) DEFAULT 'light';
CREATE INDEX idx_users_theme_preference ON users(theme_preference);
```

## Backup and Recovery

### Backup Database

```bash
# Full backup
pg_dump -U postgres mindful_wellness_db > backup_$(date +%Y%m%d_%H%M%S).sql

# Compressed backup
pg_dump -U postgres mindful_wellness_db | gzip > backup_$(date +%Y%m%d_%H%M%S).sql.gz
```

### Restore Database

```bash
# Restore from backup
psql -U postgres mindful_wellness_db < backup_20240101_120000.sql

# Restore from compressed backup
gunzip -c backup_20240101_120000.sql.gz | psql -U postgres mindful_wellness_db
```

## Troubleshooting

### Connection Issues

```bash
# Test PostgreSQL connection
psql -U postgres -h localhost -d mindful_wellness_db -c "SELECT 1"

# Check PostgreSQL service status
sudo systemctl status postgresql

# Start PostgreSQL service
sudo systemctl start postgresql
```

### Migration Issues

```bash
# Check migration status
mvn flyway:info

# Validate migrations
mvn flyway:validate

# Repair migration history (if needed)
mvn flyway:repair
```

### Performance Issues

```bash
# Analyze query performance
EXPLAIN ANALYZE SELECT * FROM appointments WHERE student_id = 'uuid';

# Rebuild indexes
REINDEX TABLE appointments;

# Vacuum database
VACUUM ANALYZE;
```

## Environment-Specific Configuration

### Development

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mindful_wellness_db_dev
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
```

### Testing

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true
```

### Production

```properties
spring.datasource.url=jdbc:postgresql://prod-db-host:5432/mindful_wellness_db
spring.datasource.hikari.maximum-pool-size=30
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
```

## Data Privacy and Compliance

- All sensitive data is encrypted at rest
- Audit logs track all data access
- GDPR-compliant data retention policies
- HIPAA-compliant security measures
- Regular backups stored in geographically separate locations

## Support

For issues or questions about the database setup, please refer to:
- PostgreSQL Documentation: https://www.postgresql.org/docs/
- Flyway Documentation: https://flywaydb.org/documentation/
- Spring Boot Documentation: https://spring.io/projects/spring-boot
