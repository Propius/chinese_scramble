# Chinese Word Scramble Game - Backend API

A gamified Chinese language learning platform featuring idiom scramble and sentence crafting games with progressive difficulty levels, achievements, and leaderboards.

## Features

### Game Modes
- **Idiom Scramble**: Unscramble Chinese idioms (成语)
- **Sentence Crafting**: Construct grammatically correct Chinese sentences

### Core Features
- 4 difficulty levels (EASY, MEDIUM, HARD, EXPERT)
- Dynamic scoring system with time bonuses and accuracy rewards
- 3-level progressive hint system with penalties
- Real-time leaderboards with daily recalculation
- 10 achievement types with metadata tracking
- Player statistics and game history
- Feature flag system for runtime toggles
- Hot-reload configuration system

## Technology Stack

- **Java 21** - Modern Java with Records and Pattern Matching
- **Spring Boot 3.2.2** - Enterprise application framework
- **Spring Data JPA** - Data persistence with Hibernate ORM
- **PostgreSQL** - Production database (H2 for development)
- **Flyway** - Database migration management
- **Spring Cache + Caffeine** - In-memory caching
- **Spring Security** - Authentication and authorization
- **SpringDoc OpenAPI** - API documentation (Swagger)
- **JUnit 5 + AssertJ** - Testing framework
- **Testcontainers** - Integration testing
- **Docker** - Containerization
- **Maven** - Build automation

## Quick Start

### Prerequisites
- Java 21 or higher
- Maven 3.9+
- Docker (optional, for containerized deployment)

### Development Setup

1. **Clone the repository**
```bash
git clone <repository-url>
cd chinese-scramble-backend
```

2. **Build the application**
```bash
./mvnw clean install
```

3. **Run tests**
```bash
./mvnw test
```

4. **Start the application**
```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

### Docker Deployment

1. **Build Docker image**
```bash
docker build -t chinese-scramble-backend .
```

2. **Run with Docker Compose** (includes PostgreSQL)
```bash
cd ..  # Navigate to project root
docker-compose up -d
```

## API Documentation

### Swagger UI
Access interactive API documentation at:
- Development: `http://localhost:8080/swagger-ui.html`
- API Docs: `http://localhost:8080/api-docs`

### API Endpoints

#### Player Management
- `POST /api/players/register` - Register new player
- `GET /api/players/{id}` - Get player by ID
- `GET /api/players/{id}/statistics` - Get player statistics

#### Idiom Game
- `POST /api/games/idiom/start` - Start new idiom game
- `POST /api/games/idiom/submit` - Submit answer
- `POST /api/games/idiom/hint/{level}` - Get hint (levels 1-3)
- `GET /api/games/idiom/history/{playerId}` - Get player history

#### Sentence Game
- `POST /api/games/sentence/start` - Start new sentence game
- `POST /api/games/sentence/submit` - Submit answer
- `POST /api/games/sentence/hint/{level}` - Get hint (levels 1-3)
- `GET /api/games/sentence/history/{playerId}` - Get player history

#### Leaderboards
- `GET /api/leaderboards/top` - Get top players
- `GET /api/leaderboards/player/{playerId}` - Get player rankings
- `GET /api/leaderboards/player/{playerId}/rank` - Get player rank

#### Achievements
- `GET /api/achievements/player/{playerId}` - Get all achievements
- `GET /api/achievements/player/{playerId}/unlocked` - Get unlocked achievements
- `GET /api/achievements/player/{playerId}/progress` - Get achievement progress
- `GET /api/achievements/all` - Get all available achievements

#### Feature Flags (Admin)
- `GET /api/features/all` - Get all feature flags
- `GET /api/features/{key}` - Get specific feature flag
- `POST /api/features/{key}/enable` - Enable feature
- `POST /api/features/{key}/disable` - Disable feature
- `GET /api/features/active` - Get active features

## Configuration

### Application Properties

**Development** (`application.properties`):
- H2 in-memory database
- H2 console enabled at `/h2-console`
- Debug logging enabled
- Swagger UI enabled

**Production** (`application-prod.properties`):
- PostgreSQL database
- Environment variable configuration
- Production-grade logging
- Swagger UI disabled

### Environment Variables

```bash
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/chinesescramble
DB_USERNAME=postgres
DB_PASSWORD=your_password

# CORS
CORS_ORIGINS=https://yourfrontend.com

# Security
ADMIN_USERNAME=admin
ADMIN_PASSWORD=secure_password
```

## Database Schema

### Core Entities
- **Player** - User accounts with authentication
- **IdiomScore** - Idiom game scores
- **SentenceScore** - Sentence game scores
- **GameSession** - Active game sessions
- **HintUsage** - Hint tracking per session
- **Leaderboard** - Pre-calculated rankings
- **Achievement** - Unlocked achievements
- **FeatureFlag** - Runtime feature toggles
- **ConfigCache** - Configuration hot-reload cache

### Migrations
Flyway migrations located in `src/main/resources/db/migration/`
- `V1__create_initial_schema.sql` - Core tables and relationships
- `V2__add_indexes_and_constraints.sql` - Performance optimization

## Scoring System

### Base Scores
- EASY: 100 points
- MEDIUM: 200 points
- HARD: 300 points
- EXPERT: 500 points

### Time Bonuses
- < 30 seconds: +50 points
- < 60 seconds: +30 points
- < 90 seconds: +15 points

### Accuracy Bonuses
- 100%: +100 points
- 95-99%: +50 points
- 90-94%: +25 points

### Hint Penalties
- Level 1: -10 points
- Level 2: -20 points
- Level 3: -30 points

### Difficulty Multipliers
- EASY: 1.0x
- MEDIUM: 1.2x
- HARD: 1.5x
- EXPERT: 2.0x

**Maximum Possible Scores**:
- EASY: 250 points
- MEDIUM: 420 points
- HARD: 675 points
- EXPERT: 1,300 points

## Achievements

1. **FIRST_WIN** - Complete first game
2. **SPEED_DEMON** - Complete in <30 seconds
3. **PERFECT_SCORE** - 100% accuracy, no hints
4. **HUNDRED_GAMES** - Play 100 games
5. **IDIOM_MASTER** - Rank #1 on idiom leaderboard
6. **SENTENCE_MASTER** - Rank #1 on sentence leaderboard
7. **TOP_RANKED** - Top 10 on any leaderboard
8. **CONSISTENCY** - Play 7 consecutive days
9. **HIGH_SCORER** - Score 1000+ in single game
10. **HINT_FREE** - Complete 10 games without hints

## Testing

### Unit Tests
```bash
./mvnw test
```

### Integration Tests
```bash
./mvnw verify
```

### Test Coverage
```bash
./mvnw jacoco:report
```
Coverage report: `target/site/jacoco/index.html`

**Target Coverage**: 80%+

## Monitoring

### Actuator Endpoints
- `GET /actuator/health` - Health check
- `GET /actuator/info` - Application info
- `GET /actuator/metrics` - Metrics
- `GET /actuator/prometheus` - Prometheus metrics

### Logging
- Development: Console output with DEBUG level
- Production: File output at `/var/log/chinese-scramble/application.log`

## Security

- BCrypt password hashing (strength 10)
- CORS configuration for frontend integration
- Input validation with Jakarta Bean Validation
- SQL injection prevention via JPA
- XSS protection with proper encoding

## Performance Optimization

- **Caching**: Caffeine in-memory cache with TTLs
  - Player statistics: 10 minutes
  - Leaderboards: 2 minutes
  - Configuration: 5 minutes

- **Database**:
  - Connection pooling (HikariCP)
  - Query optimization with indexes
  - Batch operations

- **Scheduled Tasks**:
  - Configuration reload: Every 5 minutes
  - Session expiry: Every 5 minutes
  - Leaderboard recalculation: Daily at 3 AM

## Development Guidelines

### Code Style
- Follow SOLID principles
- Constructor injection for dependencies
- Record classes for DTOs (Java 21)
- Lombok for boilerplate reduction
- Clear naming conventions

### Testing Standards
- Unit tests for all services
- Integration tests for all controllers
- Repository tests for custom queries
- Minimum 80% code coverage

### Git Workflow
- Feature branches from `main`
- Descriptive commit messages
- Pull request reviews required

## Troubleshooting

### Common Issues

**Port 8080 already in use**:
```bash
# Change port in application.properties
server.port=8081
```

**Database connection failed**:
- Verify PostgreSQL is running
- Check DATABASE_URL configuration
- Ensure database exists

**Tests failing**:
- Clear test database: `./mvnw clean`
- Check H2 configuration in `application-test.properties`

## Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## License

Apache License 2.0

## Contact

GovTech Development Team - dev@govtech.com

Project Link: [https://github.com/govtech/chinese-scramble](https://github.com/govtech/chinese-scramble)
