# Mindful Wellness Platform - Backend

A comprehensive Spring Boot backend for the Mindful Wellness Platform, providing REST APIs for student mental health and wellness support.

## Project Overview

The Mindful Wellness Platform is a multi-role system designed to support:
- **Students**: Access wellness services, mood tracking, appointments, and peer support
- **Counsellors**: Manage appointments, view student wellness data, and provide support
- **Administrators**: Manage platform configuration, resources, and analytics

## Technology Stack

- **Framework**: Spring Boot 3.1.5
- **Language**: Java 17
- **Database**: PostgreSQL 12+
- **Build Tool**: Maven 3.6+
- **Authentication**: JWT (JSON Web Tokens)
- **Database Migrations**: Flyway
- **ORM**: Hibernate/JPA
- **Security**: Spring Security

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/mindful/
│   │   │   ├── MindfulWellnessPlatformApplication.java
│   │   │   ├── config/              # Configuration classes
│   │   │   ├── controller/          # REST controllers
│   │   │   ├── service/             # Business logic
│   │   │   ├── repository/          # Data access layer
│   │   │   ├── entity/              # JPA entities
│   │   │   ├── dto/                 # Data transfer objects
│   │   │   ├── security/            # Security configuration
│   │   │   ├── exception/           # Custom exceptions
│   │   │   └── util/                # Utility classes
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/        # Flyway migrations
│   └── test/
│       └── java/com/mindful/        # Unit and integration tests
├── pom.xml                          # Maven configuration
├── DATABASE_SETUP.md                # Database setup guide
└── README.md                        # This file
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- PostgreSQL 12 or higher
- Git

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd backend
   ```

2. **Create PostgreSQL database**
   ```bash
   psql -U postgres
   CREATE DATABASE mindful_wellness_db;
   \q
   ```

3. **Update database configuration**
   Edit `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/mindful_wellness_db
   spring.datasource.username=postgres
   spring.datasource.password=your_password
   ```

4. **Build the project**
   ```bash
   mvn clean install
   ```

5. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

   The application will start on `http://localhost:8080/api`

## Database Setup

For detailed database setup instructions, see [DATABASE_SETUP.md](DATABASE_SETUP.md)

### Quick Database Setup

```bash
# Run Flyway migrations
mvn flyway:migrate

# Verify migrations
mvn flyway:info
```

## API Documentation

API endpoints are organized by feature:

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login
- `POST /api/auth/refresh` - Refresh JWT token
- `POST /api/auth/logout` - User logout

### User Management
- `GET /api/users/{id}` - Get user profile
- `PUT /api/users/{id}` - Update user profile
- `GET /api/counsellors` - List available counsellors

### Appointments
- `GET /api/appointments` - List user appointments
- `POST /api/appointments` - Book appointment
- `GET /api/appointments/{id}` - Get appointment details
- `PUT /api/appointments/{id}` - Update appointment
- `DELETE /api/appointments/{id}` - Cancel appointment

### Mood Tracking
- `POST /api/mood-entries` - Record mood entry
- `GET /api/mood-entries` - Get mood history
- `GET /api/mood-entries/trends` - Get mood trends

### Messaging
- `GET /api/messages/conversations` - List conversations
- `POST /api/messages` - Send message
- `GET /api/messages/{conversationId}` - Get conversation history

### Forum
- `GET /api/forum/posts` - List forum posts
- `POST /api/forum/posts` - Create post
- `GET /api/forum/posts/{id}` - Get post details
- `POST /api/forum/posts/{id}/comments` - Add comment

### Resources
- `GET /api/resources` - List resources
- `GET /api/resources/{id}` - Get resource details
- `POST /api/resources/{id}/bookmark` - Bookmark resource

### Admin
- `GET /api/admin/dashboard` - Admin dashboard metrics
- `GET /api/admin/analytics` - Analytics reports
- `GET /api/admin/counsellors` - Manage counsellors
- `GET /api/admin/resources` - Manage resources

## Configuration

### Environment Variables

Create `.env` file in the project root:

```env
DATABASE_URL=jdbc:postgresql://localhost:5432/mindful_wellness_db
DATABASE_USER=postgres
DATABASE_PASSWORD=your_password
JWT_SECRET=your-secret-key-change-this-in-production
JWT_EXPIRATION=86400000
```

### Application Profiles

- **dev**: Development environment with debug logging
- **test**: Testing environment with H2 in-memory database
- **prod**: Production environment with optimized settings

Run with specific profile:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

## Development

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserServiceTest

# Run with coverage
mvn test jacoco:report
```

### Code Quality

```bash
# Run SonarQube analysis
mvn sonar:sonar

# Check code style
mvn checkstyle:check
```

### Building for Production

```bash
# Build JAR file
mvn clean package

# Run JAR file
java -jar target/mindful-wellness-platform-1.0.0.jar
```

## Security

### Authentication
- JWT-based authentication with Spring Security
- Password hashing using BCrypt
- Role-based access control (RBAC)

### Data Protection
- AES-256 encryption for sensitive data at rest
- TLS 1.3 for data in transit
- Audit logging for sensitive operations

### Best Practices
- Never commit secrets to version control
- Use environment variables for sensitive configuration
- Regularly update dependencies
- Implement rate limiting
- Use HTTPS in production

## Performance Optimization

### Database
- Connection pooling with HikariCP
- Query optimization with proper indexing
- Lazy loading for relationships
- Caching with Redis (optional)

### API
- Response compression with GZIP
- Pagination for large datasets
- Asynchronous processing for long-running tasks

## Deployment

### Docker

```bash
# Build Docker image
docker build -t mindful-wellness-platform .

# Run Docker container
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://db:5432/mindful_wellness_db \
  -e DATABASE_USER=postgres \
  -e DATABASE_PASSWORD=password \
  mindful-wellness-platform
```

### Kubernetes

See `k8s/` directory for Kubernetes deployment manifests.

## Troubleshooting

### Common Issues

**Database Connection Error**
```bash
# Check PostgreSQL is running
psql -U postgres -c "SELECT 1"

# Verify database exists
psql -U postgres -l | grep mindful_wellness_db
```

**Migration Failed**
```bash
# Check migration status
mvn flyway:info

# Validate migrations
mvn flyway:validate
```

**Port Already in Use**
```bash
# Change port in application.properties
server.port=8081
```

## Contributing

1. Create a feature branch: `git checkout -b feature/your-feature`
2. Commit changes: `git commit -am 'Add feature'`
3. Push to branch: `git push origin feature/your-feature`
4. Submit pull request

## License

This project is licensed under the MIT License - see LICENSE file for details.

## Support

For issues or questions:
1. Check existing issues on GitHub
2. Create a new issue with detailed description
3. Contact the development team

## Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [JWT Documentation](https://jwt.io/)
- [Spring Security Documentation](https://spring.io/projects/spring-security)

## Changelog

### Version 1.0.0 (Initial Release)
- Initial database schema with all core tables
- Flyway migration setup
- Spring Boot project structure
- JWT authentication infrastructure
- Basic REST API structure
