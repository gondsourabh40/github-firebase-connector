# GitHub-Firebase Connector

A Spring Boot REST API that syncs GitHub issues to Firebase Firestore. Built with Spring Boot 3.2.0, Lombok, and Firebase Admin SDK.

## Features

- REST API endpoints for GitHub-Firebase sync
- Spring Boot with dependency injection
- Lombok for cleaner code
- Retry mechanism with exponential backoff
- Duplicate detection
- Externalized configuration

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.6+
- Firebase project with Firestore
- GitHub repository access

### Setup

1. **Build the project**:
   ```bash
   mvn clean compile
   ```

2. **Configure Firebase**:
   - Download your Firebase service account JSON
   - Place it in `src/main/resources/serviceAccount.json`

3. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

   App starts on `http://localhost:8080`

## API Endpoints

Base URL: `http://localhost:8080/api/v1`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/sync` | Sync GitHub issues to Firestore |
| GET | `/issues` | Get all issues from Firestore |
| GET | `/issues/{id}/exists` | Check if issue exists |
| GET | `/health` | Health check |

### Example Usage

```bash
# Sync issues
curl -X POST http://localhost:8080/api/v1/sync

# Get all issues
curl http://localhost:8080/api/v1/issues

# Check if issue exists
curl http://localhost:8080/api/v1/issues/3415053916/exists

# Health check
curl http://localhost:8080/api/v1/health
```

### Response Format

All endpoints return standardized JSON responses:

```json
{
    "success": true,
    "message": "Operation completed",
    "data": { ... },
    "timestamp": "2025-09-14T20:54:41.550013"
}
```

## Configuration

Edit `src/main/resources/application.properties`:

```properties
# GitHub repository to sync
github.repository=gondsourabh40/ML-Projects

# Number of issues to fetch
connector.max-issues=5

# Firestore collection name
firestore.collection=github_issues

# Service account path
firebase.service-account-path=src/main/resources/serviceAccount.json

# Retry settings
connector.max-retries=3
connector.retry-delay-ms=1000

# Server port
server.port=8080
```

## Firebase Setup

1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Enable Firestore Database
3. Generate service account key:
   - Go to Project Settings → Service Accounts
   - Click "Generate new private key"
   - Download the JSON file
4. Replace `src/main/resources/serviceAccount.json` with your file

## Project Structure

```
src/main/java/com/connector/
├── model/              # Issue, ConnectorConfig, SyncResult
├── repository/         # GitHubApiClient, FirestoreIssueRepository
├── service/           # ConnectorService
├── controller/        # ConnectorController, ApiResponse
├── config/            # ConnectorConfiguration
└── GitHubFirebaseConnectorApplication.java
```

## Building

```bash
# Create JAR
mvn clean package

# Run JAR
java -jar target/github-firebase-connector-1.0-SNAPSHOT.jar
```

## Testing

```bash
# Run tests
mvn test

# Test API (start app first)
mvn spring-boot:run
curl -X POST http://localhost:8080/api/v1/sync
```

## Tech Stack

- Spring Boot 3.2.0
- Lombok
- Firebase Admin SDK
- Apache HttpClient
- Maven

## Error Handling

- Network errors: Automatic retry with exponential backoff
- Duplicate issues: Skipped with warning logs
- API errors: Standardized error responses with HTTP status codes

## Production Notes

- Store service account credentials securely
- Implement proper Firestore security rules
- Consider adding authentication for API endpoints
- Use environment variables for sensitive config
- Monitor logs for sync failures

## License

MIT License