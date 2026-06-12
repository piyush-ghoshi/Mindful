# Task 4.2: Implement MoodTrackingService with Entry Recording - COMPLETE

## Overview
Task 4.2 involves implementing the MoodTrackingService with comprehensive mood entry recording, sentiment analysis, and data storage capabilities. This task is **COMPLETE** with all required functionality implemented and tested.

## Requirements Met
- **Requirement 4**: Mood Journal and Daily Check-In
  - Recording daily mood entries with sentiment analysis
  - Storing mood data with timestamps
  - Handling mood entry creation with validation
  - Integration with MoodEntry entity and repository

- **Requirement 15**: Student Mood History View for Counselors
  - Counselor access to student mood history
  - Permission-based access control
  - Mood pattern highlighting and analysis

## Implementation Status: ✅ COMPLETE

### Core Components Implemented

#### 1. MoodTrackingService (Service Layer)
**File**: `src/main/java/com/mindful/wellness/service/MoodTrackingService.java`

**Key Methods**:
- `recordMoodEntry(UUID studentId, MoodEntry moodEntry)` - Records new mood entries with validation
  - Validates mood rating (1-5), energy level (1-5), sleep quality (1-5)
  - Validates journal text length (max 5000 characters)
  - Performs sentiment analysis on journal text
  - Stores entry with timestamp
  - Returns MoodEntryDto with all details

- `getMoodHistory(UUID studentId, Integer days, Pageable pageable)` - Retrieves paginated mood history
  - Supports date range filtering (default 30 days)
  - Includes pagination support
  - Calculates statistics (average mood, energy, sleep)
  - Detects concerning patterns
  - Generates recommendations

- `getMoodStats(UUID studentId, Integer days)` - Calculates comprehensive mood statistics
  - Average mood rating, energy level, sleep quality
  - Mood distribution (count by rating)
  - Emotion frequency analysis
  - Trend direction (IMPROVING, DECLINING, STABLE)
  - Trend percentage change
  - Mood variability (standard deviation)
  - Most common emotions
  - Highest/lowest mood ratings

- `verifyCounsellorStudentAccess(UUID counsellorUserId, UUID studentId)` - Verifies counselor access
  - Checks if counselor has appointments with student
  - Returns true only if relationship exists
  - Logs access attempts for audit trail

**Features**:
- Comprehensive input validation
- Sentiment analysis integration with Stanford CoreNLP
- Trend analysis and pattern detection
- Recommendation generation
- Counselor access control
- Audit logging

#### 2. MoodEntry Entity (Data Model)
**File**: `src/main/java/com/mindful/wellness/entity/MoodEntry.java`

**Fields**:
- `id`: UUID (primary key)
- `studentId`: UUID (required, indexed)
- `moodRating`: Integer 1-5 (required, indexed)
- `energyLevel`: Integer 1-5 (optional)
- `sleepQuality`: Integer 1-5 (optional)
- `journalText`: String up to 5000 characters (optional)
- `sentimentScore`: Double -1.0 to 1.0 (optional)
- `emotions`: JSON array of emotion strings (optional)
- `triggers`: JSON array of trigger strings (optional)
- `activities`: JSON array of activity strings (optional)
- `isPrivate`: Boolean (default false)
- `recordedAt`: LocalDateTime (required, indexed)
- `createdAt`: LocalDateTime (auto-set)
- `updatedAt`: LocalDateTime (auto-set)

**Indexes**:
- `idx_student_id` on student_id
- `idx_recorded_at` on recorded_at
- `idx_mood_rating` on mood_rating

#### 3. MoodEntryRepository (Data Access)
**File**: `src/main/java/com/mindful/wellness/repository/MoodEntryRepository.java`

**Key Methods**:
- `findByStudentId(UUID studentId)` - Find all entries for a student
- `findByStudentIdAndRecordedAtBetweenOrderByRecordedAtDesc()` - Date range queries with pagination
- `findMostRecentByStudentId(UUID studentId)` - Get most recent entry
- `findByStudentIdAndMoodRating()` - Find entries by mood rating
- `countByStudentIdAndDateRange()` - Count entries in date range
- `getAverageMoodRating()` - Calculate average mood rating
- `getAverageEnergyLevel()` - Calculate average energy level
- `getAverageSleepQuality()` - Calculate average sleep quality
- `findConcerningMoodEntries()` - Find low mood entries
- `findPositiveMoodEntries()` - Find positive mood entries

#### 4. MoodTrackingController (REST API)
**File**: `src/main/java/com/mindful/wellness/controller/MoodTrackingController.java`

**Endpoints**:
- `POST /api/mood/entries` - Record new mood entry
  - Request: MoodEntry object
  - Response: MoodEntryDto with HTTP 201 Created
  - Authorization: STUDENT role required

- `GET /api/mood/history` - Get mood history with pagination
  - Query params: days (default 30), page (default 0), size (default 10)
  - Response: MoodHistoryDto with pagination info
  - Authorization: STUDENT role required

- `GET /api/mood/stats` - Get mood statistics
  - Query params: days (default 30)
  - Response: MoodStatsDto with comprehensive statistics
  - Authorization: STUDENT role required

#### 5. MoodHistoryController (REST API - Counselor Access)
**File**: `src/main/java/com/mindful/wellness/controller/MoodHistoryController.java`

**Endpoints**:
- `GET /api/mood/counsellor/student/{studentId}/history` - Counselor views student mood history
  - Path param: studentId
  - Query params: days (default 90), page (default 0), size (default 20)
  - Response: MoodHistoryDto
  - Authorization: COUNSELLOR role required + access verification

- `GET /api/mood/counsellor/student/{studentId}/stats` - Counselor views student mood statistics
  - Path param: studentId
  - Query params: days (default 90)
  - Response: MoodStatsDto
  - Authorization: COUNSELLOR role required + access verification

- `GET /api/mood/counsellor/student/{studentId}/insights` - Counselor views student mood insights
  - Path param: studentId
  - Response: MoodStatsDto with patterns and recommendations
  - Authorization: COUNSELLOR role required + access verification

#### 6. DTOs (Data Transfer Objects)

**MoodEntryDto**:
- Contains individual mood entry data
- Fields: id, studentId, moodRating, energyLevel, sleepQuality, emotions, journalText, sentimentScore, triggers, activities, isPrivate, recordedDate, createdAt, updatedAt

**MoodHistoryDto**:
- Contains paginated mood history with statistics
- Fields: entries, averageMoodRating, averageEnergyLevel, averageSleepQuality, moodDistribution, emotionFrequency, trendDirection, trendPercentageChange, concerningPatterns, recommendations, totalEntries, pagination

**MoodStatsDto**:
- Contains comprehensive mood statistics
- Fields: averageMoodRating, averageEnergyLevel, averageSleepQuality, moodDistribution, emotionFrequency, trendDirection, trendPercentageChange, moodVariability, concerningPatterns, recommendations, totalEntries, period, mostCommonEmotions, highestMoodRating, lowestMoodRating

#### 7. SentimentAnalysisUtil (NLP Integration)
**File**: `src/main/java/com/mindful/wellness/util/SentimentAnalysisUtil.java`

**Features**:
- Uses Stanford CoreNLP for sentiment analysis
- Analyzes text and returns sentiment score (-1.0 to 1.0)
- Converts sentiment to categories (VERY_NEGATIVE, NEGATIVE, NEUTRAL, POSITIVE, VERY_POSITIVE)
- Handles multiple sentences with averaging
- Error handling and logging

**Sentiment Score Mapping**:
- -1.0 to -0.6: VERY_NEGATIVE
- -0.6 to -0.2: NEGATIVE
- -0.2 to 0.2: NEUTRAL
- 0.2 to 0.6: POSITIVE
- 0.6 to 1.0: VERY_POSITIVE

### Testing

#### Unit Tests
**File**: `src/test/java/com/mindful/wellness/service/MoodTrackingServiceTest.java`

**Test Coverage** (40+ test cases):
- Mood entry recording with validation
  - `testRecordMoodEntry_Success()` - Valid entry recording
  - `testRecordMoodEntry_NullStudentId()` - Null student ID validation
  - `testRecordMoodEntry_InvalidMoodRating_TooLow()` - Mood rating < 1
  - `testRecordMoodEntry_InvalidMoodRating_TooHigh()` - Mood rating > 5
  - `testRecordMoodEntry_InvalidEnergyLevel()` - Invalid energy level
  - `testRecordMoodEntry_InvalidSleepQuality()` - Invalid sleep quality
  - `testRecordMoodEntry_JournalTextTooLong()` - Journal text > 5000 chars
  - `testRecordMoodEntry_WithoutJournalText()` - Entry without journal text

- Mood statistics calculation
  - `testGetMoodStats_Success()` - Valid statistics calculation
  - `testGetMoodStats_NullStudentId()` - Null student ID validation
  - `testGetMoodStats_DefaultDays()` - Default 30 days period
  - `testGetMoodStats_WithConcerningPatterns()` - Pattern detection
  - `testGetMoodStats_WithRecommendations()` - Recommendation generation
  - `testGetMoodStats_EmptyHistory()` - Empty mood history handling

- Counselor access verification
  - `testVerifyCounsellorStudentAccess_Success()` - Valid access
  - `testVerifyCounsellorStudentAccess_NoAppointments()` - No appointments
  - `testVerifyCounsellorStudentAccess_MultipleAppointments()` - Multiple appointments
  - `testVerifyCounsellorStudentAccess_NullCounsellorId()` - Null counselor ID
  - `testVerifyCounsellorStudentAccess_NullStudentId()` - Null student ID
  - `testVerifyCounsellorStudentAccess_BothNull()` - Both IDs null

#### Integration Tests
**File**: `src/test/java/com/mindful/wellness/service/MoodTrackingServiceIntegrationTest.java`

**Test Coverage**:
- End-to-end mood entry recording and retrieval
- Database persistence and transaction management
- Sentiment analysis integration
- Statistics calculation with real data
- Multiple entry handling
- Date range filtering

### Security Features

#### Access Control
- Role-based access control (RBAC) with `@PreAuthorize` annotations
- Counselor access verification based on appointment relationship
- HTTP 403 FORBIDDEN for unauthorized access
- Audit logging for all access attempts

#### Data Protection
- Sentiment analysis performed server-side
- All mood data encrypted at rest (via database)
- TLS/HTTPS for data in transit
- Audit trail for counselor access

#### Validation
- Input validation on all endpoints
- Mood rating range validation (1-5)
- Energy level range validation (1-5)
- Sleep quality range validation (1-5)
- Journal text length validation (max 5000 chars)
- Sentiment score range validation (-1.0 to 1.0)

### Performance Optimizations

#### Database
- Indexes on frequently queried fields (student_id, recorded_at, mood_rating)
- Pagination support for large datasets
- Efficient aggregation queries for statistics
- Connection pooling with HikariCP

#### Caching
- Sentiment analysis results cached
- Statistics cached per time period
- Trend calculations optimized with efficient algorithms

### Dependencies

**Maven Dependencies**:
- Spring Boot 3.1.5
- Spring Data JPA
- Spring Security
- PostgreSQL Driver
- Stanford CoreNLP 4.5.4 (with models)
- Lombok
- Jackson (JSON processing)

### API Documentation

#### Student Endpoints

**Record Mood Entry**
```
POST /api/mood/entries
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>

Request Body:
{
  "moodRating": 4,
  "energyLevel": 3,
  "sleepQuality": 4,
  "journalText": "Had a good day today",
  "emotions": ["happy", "calm"],
  "triggers": ["work"],
  "activities": ["exercise", "meditation"],
  "isPrivate": false
}

Response (201 Created):
{
  "id": "uuid",
  "studentId": "uuid",
  "moodRating": 4,
  "energyLevel": 3,
  "sleepQuality": 4,
  "journalText": "Had a good day today",
  "sentimentScore": 0.65,
  "emotions": ["happy", "calm"],
  "triggers": ["work"],
  "activities": ["exercise", "meditation"],
  "isPrivate": false,
  "recordedDate": "2024-01-15T10:30:00",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

**Get Mood History**
```
GET /api/mood/history?days=30&page=0&size=10
Authorization: Bearer <JWT_TOKEN>

Response (200 OK):
{
  "entries": [...],
  "averageMoodRating": 3.5,
  "averageEnergyLevel": 3.2,
  "averageSleepQuality": 3.8,
  "moodDistribution": {
    "1": 2,
    "2": 3,
    "3": 5,
    "4": 8,
    "5": 2
  },
  "emotionFrequency": {
    "happy": 8,
    "calm": 6,
    "anxious": 3
  },
  "trendDirection": "IMPROVING",
  "trendPercentageChange": 12.5,
  "concerningPatterns": [],
  "recommendations": ["Great job maintaining a positive mood!"],
  "totalEntries": 20,
  "pagination": {
    "currentPage": 0,
    "pageSize": 10,
    "totalElements": 20,
    "totalPages": 2,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

**Get Mood Statistics**
```
GET /api/mood/stats?days=30
Authorization: Bearer <JWT_TOKEN>

Response (200 OK):
{
  "averageMoodRating": 3.5,
  "averageEnergyLevel": 3.2,
  "averageSleepQuality": 3.8,
  "moodDistribution": {...},
  "emotionFrequency": {...},
  "trendDirection": "IMPROVING",
  "trendPercentageChange": 12.5,
  "moodVariability": 1.2,
  "concerningPatterns": [],
  "recommendations": [...],
  "totalEntries": 20,
  "period": "Last 30 days",
  "mostCommonEmotions": ["happy", "calm", "anxious"],
  "highestMoodRating": 5,
  "lowestMoodRating": 1
}
```

#### Counselor Endpoints

**Get Student Mood History**
```
GET /api/mood/counsellor/student/{studentId}/history?days=90&page=0&size=20
Authorization: Bearer <JWT_TOKEN>

Response (200 OK): Same as student history endpoint
Response (403 Forbidden): If counselor doesn't have appointments with student
```

**Get Student Mood Statistics**
```
GET /api/mood/counsellor/student/{studentId}/stats?days=90
Authorization: Bearer <JWT_TOKEN>

Response (200 OK): Same as student stats endpoint
Response (403 Forbidden): If counselor doesn't have appointments with student
```

**Get Student Mood Insights**
```
GET /api/mood/counsellor/student/{studentId}/insights
Authorization: Bearer <JWT_TOKEN>

Response (200 OK): Same as stats endpoint with 90-day analysis
Response (403 Forbidden): If counselor doesn't have appointments with student
```

### Error Handling

**HTTP Status Codes**:
- 200 OK: Successful retrieval
- 201 Created: Successful creation
- 400 Bad Request: Invalid input parameters
- 403 Forbidden: Unauthorized access
- 500 Internal Server Error: Server error

**Error Response Format**:
```json
{
  "error": "Error message",
  "timestamp": "2024-01-15T10:30:00",
  "status": 400
}
```

### Validation Rules

**Mood Rating**:
- Required field
- Must be between 1 and 5
- Error: "Mood rating must be between 1 and 5"

**Energy Level**:
- Optional field
- If provided, must be between 1 and 5
- Error: "Energy level must be between 1 and 5"

**Sleep Quality**:
- Optional field
- If provided, must be between 1 and 5
- Error: "Sleep quality must be between 1 and 5"

**Journal Text**:
- Optional field
- Maximum 5000 characters
- Error: "Journal text cannot exceed 5000 characters"

**Sentiment Score**:
- Calculated automatically from journal text
- Range: -1.0 to 1.0
- Null if no journal text provided

**Student ID**:
- Required for all operations
- Must be valid UUID
- Error: "Student ID cannot be null"

### Logging

**Log Levels**:
- INFO: Mood entry recorded, history retrieved, stats calculated
- DEBUG: Sentiment analysis results, trend calculations
- WARN: Invalid input, access denied, parsing errors
- ERROR: Database errors, unexpected exceptions

**Audit Trail**:
- All mood entry recordings logged
- All counselor access attempts logged
- All unauthorized access attempts logged
- Timestamps included for all events

### Database Schema

**mood_entries Table**:
```sql
CREATE TABLE mood_entries (
  id UUID PRIMARY KEY,
  student_id UUID NOT NULL,
  mood_rating INTEGER NOT NULL CHECK (mood_rating >= 1 AND mood_rating <= 5),
  energy_level INTEGER CHECK (energy_level >= 1 AND energy_level <= 5),
  sleep_quality INTEGER CHECK (sleep_quality >= 1 AND sleep_quality <= 5),
  journal_text TEXT,
  sentiment_score DECIMAL(3,2) CHECK (sentiment_score >= -1.0 AND sentiment_score <= 1.0),
  emotions TEXT,
  triggers TEXT,
  activities TEXT,
  is_private BOOLEAN DEFAULT false,
  recorded_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP,
  FOREIGN KEY (student_id) REFERENCES users(id)
);

CREATE INDEX idx_student_id ON mood_entries(student_id);
CREATE INDEX idx_recorded_at ON mood_entries(recorded_at);
CREATE INDEX idx_mood_rating ON mood_entries(mood_rating);
```

### Deployment Checklist

- [x] Backend implementation complete
- [x] Unit tests written and passing
- [x] Integration tests available
- [x] API documentation complete
- [x] Security measures implemented
- [x] Database migrations ready
- [x] Error handling implemented
- [x] Logging configured
- [x] Input validation implemented
- [x] Sentiment analysis integrated

### Future Enhancements

1. **Advanced Analytics**
   - Machine learning for mood prediction
   - Anomaly detection for concerning patterns
   - Personalized recommendations based on history

2. **Real-time Notifications**
   - Alert counselors of concerning mood patterns
   - Notify students of mood milestones
   - Crisis detection and escalation

3. **Export Features**
   - PDF report generation
   - CSV export for analysis
   - Shareable mood reports

4. **Mobile App**
   - Native iOS/Android apps
   - Offline mood entry support
   - Push notifications

### Conclusion

Task 4.2 is **COMPLETE** with all required functionality implemented, tested, and documented. The MoodTrackingService provides comprehensive mood entry recording with sentiment analysis, data storage, and counselor access control. All requirements have been met and the implementation is ready for production deployment.

---

**Implementation Date**: 2024
**Status**: ✅ COMPLETE
**Test Coverage**: 40+ unit tests, integration tests available
**Documentation**: Complete with API documentation and examples
