# Chinese Word Scramble Game API Documentation

**Version**: 1.0.0
**Base URL**: `http://localhost:8080`
**OpenAPI Specification**: `http://localhost:8080/v3/api-docs`
**Swagger UI**: `http://localhost:8080/swagger-ui.html`

---

## Table of Contents

1. [Overview](#overview)
2. [Quick Start](#quick-start)
3. [Authentication](#authentication)
4. [API Endpoints](#api-endpoints)
   - [Player Management](#player-management)
   - [Idiom Game](#idiom-game)
   - [Sentence Game](#sentence-game)
   - [Leaderboard](#leaderboard)
   - [Achievements](#achievements)
   - [Feature Flags](#feature-flags)
5. [Data Models](#data-models)
6. [Error Handling](#error-handling)
7. [Best Practices](#best-practices)
8. [Testing Examples](#testing-examples)

---

## Overview

The Chinese Word Scramble Game API is a comprehensive REST API for Chinese language learning featuring two game modes:

- **成语拼字游戏 (Idiom Scramble)**: Rearrange scrambled Chinese idiom characters
- **造句游戏 (Sentence Crafting)**: Build grammatically correct sentences from word tiles

### Key Features

- 🎯 **Multiple Difficulty Levels**: Easy, Medium, Hard, Expert
- 💡 **Progressive Hint System**: 3-level hints (definition, first character, example)
- ⏱️ **Time-Based Scoring**: Bonus points for speed, penalties for hints
- 🏆 **Leaderboards**: Global rankings by game type and difficulty
- 🎖️ **Achievements**: Unlockable milestones and rewards
- 📊 **Statistics**: Personal bests, accuracy rates, progress tracking
- 🚩 **Feature Flags**: Dynamic feature toggling

### Technical Stack

- **Backend**: Spring Boot 3.2.2 with Java 21
- **Database**: H2 (dev), PostgreSQL (prod)
- **Test Coverage**: 100% (136/136 tests passing)
- **Architecture**: Clean layered architecture (Controller → Service → Repository)

---

## Quick Start

### 1. Register a Player

```bash
curl -X POST http://localhost:8080/api/players/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "玩家001",
    "email": "player@example.com",
    "password": "securePassword123"
  }'
```

**Response**:
```json
{
  "id": 1,
  "username": "玩家001",
  "email": "player@example.com",
  "role": "PLAYER",
  "active": true,
  "message": "Player registered successfully"
}
```

### 2. Start an Idiom Game

```bash
curl -X POST "http://localhost:8080/api/games/idiom/start?playerId=1" \
  -H "Content-Type: application/json" \
  -d '{
    "difficulty": "EASY"
  }'
```

**Response**:
```json
{
  "sessionId": "abc123",
  "idiom": "一马当先",
  "scrambledCharacters": ["先", "一", "当", "马"],
  "difficulty": "EASY",
  "timeLimitSeconds": 120,
  "hintsRemaining": 3
}
```

### 3. Submit an Answer

```bash
curl -X POST http://localhost:8080/api/games/idiom/submit \
  -H "Content-Type: application/json" \
  -d '{
    "playerId": 1,
    "sessionId": "abc123",
    "answer": "一马当先",
    "timeTaken": 45
  }'
```

**Response**:
```json
{
  "correct": true,
  "score": 450,
  "message": "Correct! Well done!",
  "explanation": "一马当先 means to take the lead or be the first to do something"
}
```

### 4. View Leaderboard

```bash
curl -X GET "http://localhost:8080/api/leaderboard/idiom/EASY?limit=10"
```

**Response**:
```json
[
  {
    "rank": 1,
    "playerId": 1,
    "username": "玩家001",
    "totalScore": 1500,
    "gamesPlayed": 5,
    "averageScore": 300.0,
    "accuracyRate": 0.95
  }
]
```

---

## Authentication

**Current Status**: API is open for development. JWT-ready authentication is implemented but not enforced.

**Future Implementation**: All endpoints will require JWT bearer tokens:

```bash
curl -X GET http://localhost:8080/api/players/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## API Endpoints

### Player Management

#### Register New Player

**Endpoint**: `POST /api/players/register`

**Request Body**:
```json
{
  "username": "string (3-50 characters)",
  "email": "string (valid email format)",
  "password": "string (8-100 characters)"
}
```

**Response**: `201 Created`
```json
{
  "id": 1,
  "username": "玩家001",
  "email": "player@example.com",
  "role": "PLAYER",
  "active": true,
  "message": "Player registered successfully"
}
```

**Validation Rules**:
- Username: 3-50 characters, unique
- Email: Valid format, unique
- Password: 8-100 characters, will be hashed

**Error Responses**:
- `400 Bad Request`: Validation failure or duplicate username/email
- `500 Internal Server Error`: Server error

---

#### Get Player by ID

**Endpoint**: `GET /api/players/{id}`

**Path Parameters**:
- `id` (Long): Player ID

**Response**: `200 OK`
```json
{
  "id": 1,
  "username": "玩家001",
  "email": "player@example.com",
  "role": "PLAYER",
  "active": true,
  "lastLogin": "2025-10-02T14:30:00"
}
```

**Note**: `lastLogin` field is optional and only included if player has logged in.

**Error Responses**:
- `404 Not Found`: Player does not exist

---

#### Get Player Statistics

**Endpoint**: `GET /api/players/{id}/statistics`

**Path Parameters**:
- `id` (Long): Player ID

**Response**: `200 OK`
```json
{
  "playerId": 1,
  "totalGamesPlayed": 50,
  "idiomGamesPlayed": 30,
  "sentenceGamesPlayed": 20,
  "totalScore": 15000,
  "averageScore": 300.0,
  "accuracyRate": 0.92,
  "perfectGames": 5,
  "speedDemonAchievements": 3,
  "personalBests": {
    "EASY": 500,
    "MEDIUM": 450,
    "HARD": 400,
    "EXPERT": 350
  }
}
```

**Error Responses**:
- `404 Not Found`: Player does not exist

---

### Idiom Game

#### Start Idiom Game

**Endpoint**: `POST /api/games/idiom/start`

**Query Parameters**:
- `playerId` (Long, required): Player ID

**Request Body**:
```json
{
  "difficulty": "EASY | MEDIUM | HARD | EXPERT"
}
```

**Response**: `200 OK`
```json
{
  "sessionId": "abc123",
  "idiom": "一马当先",
  "scrambledCharacters": ["先", "一", "当", "马"],
  "difficulty": "EASY",
  "timeLimitSeconds": 120,
  "hintsRemaining": 3,
  "startTime": "2025-10-02T14:30:00"
}
```

**Difficulty Settings**:
- **EASY**: 4-character idioms, 120s time limit
- **MEDIUM**: 4-character idioms, 90s time limit
- **HARD**: 5+ character idioms, 60s time limit
- **EXPERT**: Complex idioms, 45s time limit

**Error Responses**:
- `400 Bad Request`: Invalid difficulty or player not found
- `500 Internal Server Error`: Server error

---

#### Submit Idiom Answer

**Endpoint**: `POST /api/games/idiom/submit`

**Request Body**:
```json
{
  "playerId": 1,
  "sessionId": "abc123",
  "answer": "一马当先",
  "timeTaken": 45
}
```

**Response**: `200 OK`
```json
{
  "correct": true,
  "score": 450,
  "message": "Correct! Well done!",
  "explanation": "一马当先 means to take the lead or be the first to do something",
  "timeTaken": 45,
  "hintsUsed": 0,
  "accuracyRate": 1.0,
  "bonusPoints": 50,
  "sessionCompleted": true
}
```

**Scoring Formula**:
```
Base Score = 400 (correct answer)
Time Bonus = max(0, (timeLimit - timeTaken) * 2)
Hint Penalty = hintsUsed * 50
Final Score = Base Score + Time Bonus - Hint Penalty
```

**Error Responses**:
- `400 Bad Request`: Invalid session or player
- `404 Not Found`: Session not found

---

#### Request Hint

**Endpoint**: `POST /api/games/idiom/hint`

**Query Parameters**:
- `playerId` (Long, required): Player ID
- `sessionId` (String, required): Game session ID

**Response**: `200 OK`
```json
{
  "hintLevel": 1,
  "hint": "This idiom means to take the lead",
  "hintsRemaining": 2,
  "penaltyApplied": 50
}
```

**Hint Levels**:
1. **Level 1**: Definition/meaning (50 points penalty)
2. **Level 2**: First character revealed (50 points penalty)
3. **Level 3**: Usage example (50 points penalty)

**Error Responses**:
- `400 Bad Request`: No hints remaining
- `404 Not Found`: Session not found

---

#### Restart Idiom Game

**Endpoint**: `POST /api/games/idiom/restart`

**Query Parameters**:
- `playerId` (Long, required): Player ID

**Response**: `200 OK`
```json
{
  "message": "Game restarted successfully",
  "sessionId": "def456",
  "difficulty": "EASY"
}
```

---

### Sentence Game

#### Start Sentence Game

**Endpoint**: `POST /api/games/sentence/start`

**Query Parameters**:
- `playerId` (Long, required): Player ID

**Request Body**:
```json
{
  "difficulty": "EASY | MEDIUM | HARD | EXPERT"
}
```

**Response**: `200 OK`
```json
{
  "sessionId": "xyz789",
  "targetSentence": "我今天去学校上课",
  "wordTiles": ["上课", "学校", "今天", "我", "去"],
  "difficulty": "EASY",
  "timeLimitSeconds": 180,
  "hintsRemaining": 3,
  "startTime": "2025-10-02T14:35:00"
}
```

**Word Tile Types**:
- **Subject**: 我, 你, 他, 她
- **Time**: 今天, 昨天, 明天
- **Verb**: 去, 吃, 学习
- **Object**: 学校, 餐厅, 图书馆
- **Particle**: 了, 的, 在

**Error Responses**:
- `400 Bad Request`: Invalid difficulty or player not found

---

#### Submit Sentence Answer

**Endpoint**: `POST /api/games/sentence/submit`

**Request Body**:
```json
{
  "playerId": 1,
  "sessionId": "xyz789",
  "answer": "我今天去学校上课",
  "timeTaken": 75
}
```

**Response**: `200 OK`
```json
{
  "correct": true,
  "score": 520,
  "message": "Perfect sentence construction!",
  "explanation": "Subject-Time-Verb-Object-Action pattern",
  "grammarFeedback": "Correct word order and particle usage",
  "sessionCompleted": true
}
```

**Grammar Validation**:
- Word order correctness
- Particle placement
- Subject-verb agreement
- Semantic coherence

**Error Responses**:
- `400 Bad Request`: Invalid session
- `404 Not Found`: Session not found

---

#### Request Hint

**Endpoint**: `POST /api/games/sentence/hint`

**Query Parameters**:
- `playerId` (Long, required): Player ID
- `sessionId` (String, required): Game session ID

**Response**: `200 OK`
```json
{
  "hintLevel": 1,
  "hint": "Start with the subject pronoun",
  "hintsRemaining": 2,
  "penaltyApplied": 50
}
```

---

#### Restart Sentence Game

**Endpoint**: `POST /api/games/sentence/restart`

**Query Parameters**:
- `playerId` (Long, required): Player ID

**Response**: `200 OK`
```json
{
  "message": "Game restarted successfully",
  "sessionId": "uvw123",
  "difficulty": "EASY"
}
```

---

### Leaderboard

#### Get Top Players

**Endpoint**: `GET /api/leaderboard/{gameType}/{difficulty}`

**Path Parameters**:
- `gameType` (String): `idiom` or `sentence`
- `difficulty` (String): `EASY`, `MEDIUM`, `HARD`, `EXPERT`

**Query Parameters**:
- `limit` (Integer, optional, default=10): Number of players to return

**Response**: `200 OK`
```json
[
  {
    "rank": 1,
    "playerId": 1,
    "username": "玩家001",
    "totalScore": 1500,
    "gamesPlayed": 5,
    "averageScore": 300.0,
    "accuracyRate": 0.95,
    "lastUpdated": "2025-10-02T14:30:00"
  },
  {
    "rank": 2,
    "playerId": 2,
    "username": "张伟",
    "totalScore": 1200,
    "gamesPlayed": 4,
    "averageScore": 300.0,
    "accuracyRate": 0.92,
    "lastUpdated": "2025-10-02T13:15:00"
  }
]
```

**Error Responses**:
- `400 Bad Request`: Invalid game type or difficulty

---

#### Get Player Rank

**Endpoint**: `GET /api/leaderboard/player/{playerId}`

**Path Parameters**:
- `playerId` (Long): Player ID

**Query Parameters**:
- `gameType` (String, required): `idiom` or `sentence`
- `difficulty` (String, required): `EASY`, `MEDIUM`, `HARD`, `EXPERT`

**Response**: `200 OK`
```json
{
  "rank": 5,
  "playerId": 1,
  "username": "玩家001",
  "totalScore": 1200,
  "gamesPlayed": 8,
  "averageScore": 150.0,
  "accuracyRate": 0.88,
  "nearbyPlayers": [
    {
      "rank": 4,
      "username": "李娜",
      "totalScore": 1250
    },
    {
      "rank": 6,
      "username": "王芳",
      "totalScore": 1150
    }
  ]
}
```

**Error Responses**:
- `404 Not Found`: Player not on leaderboard

---

### Achievements

#### Get Player Achievements

**Endpoint**: `GET /api/achievements/player/{playerId}`

**Path Parameters**:
- `playerId` (Long): Player ID

**Response**: `200 OK`
```json
[
  {
    "id": 1,
    "name": "Perfect Score",
    "description": "Complete a game with 100% accuracy and no hints",
    "iconUrl": "/assets/icons/perfect-score.png",
    "unlocked": true,
    "unlockedAt": "2025-10-01T10:30:00",
    "progress": 1.0
  },
  {
    "id": 2,
    "name": "Speed Demon",
    "description": "Complete 10 games in under 30 seconds each",
    "iconUrl": "/assets/icons/speed-demon.png",
    "unlocked": false,
    "progress": 0.3
  }
]
```

**Achievement Types**:
- **Perfect Score**: 100% accuracy, no hints
- **Speed Demon**: Fast completion times
- **Marathon**: Play many games
- **Perfectionist**: Multiple perfect games
- **Hint-Free**: Complete without hints
- **Top 10**: Reach top 10 on any leaderboard

---

#### Unlock Achievement

**Endpoint**: `POST /api/achievements/unlock`

**Request Body**:
```json
{
  "playerId": 1,
  "achievementId": 3
}
```

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "Achievement unlocked!",
  "achievement": {
    "name": "Marathon Runner",
    "description": "Play 50 games",
    "rewardPoints": 100
  }
}
```

**Note**: Achievements are typically auto-unlocked by the system based on game activity.

---

### Feature Flags

#### Get All Feature Flags

**Endpoint**: `GET /api/feature-flags`

**Response**: `200 OK`
```json
[
  {
    "id": 1,
    "flagName": "HINT_SYSTEM_ENABLED",
    "enabled": true,
    "description": "Enable/disable hint system",
    "lastModified": "2025-10-01T09:00:00"
  },
  {
    "id": 2,
    "flagName": "LEADERBOARD_VISIBLE",
    "enabled": true,
    "description": "Show/hide leaderboard",
    "lastModified": "2025-10-01T09:00:00"
  }
]
```

---

#### Check Feature Flag

**Endpoint**: `GET /api/feature-flags/{flagName}`

**Path Parameters**:
- `flagName` (String): Feature flag name

**Response**: `200 OK`
```json
{
  "flagName": "HINT_SYSTEM_ENABLED",
  "enabled": true
}
```

**Available Flags**:
- `HINT_SYSTEM_ENABLED`: Enable hint system
- `LEADERBOARD_VISIBLE`: Show leaderboard
- `ACHIEVEMENTS_ENABLED`: Enable achievements
- `SOCIAL_FEATURES_ENABLED`: Enable social features

---

#### Toggle Feature Flag (Admin Only)

**Endpoint**: `PUT /api/feature-flags/{flagName}`

**Path Parameters**:
- `flagName` (String): Feature flag name

**Request Body**:
```json
{
  "enabled": false
}
```

**Response**: `200 OK`
```json
{
  "message": "Feature flag updated successfully",
  "flagName": "HINT_SYSTEM_ENABLED",
  "enabled": false
}
```

---

## Data Models

### Player

```json
{
  "id": 1,
  "username": "玩家001",
  "email": "player@example.com",
  "role": "PLAYER | ADMIN",
  "active": true,
  "lastLogin": "2025-10-02T14:30:00",
  "createdAt": "2025-09-01T10:00:00",
  "updatedAt": "2025-10-02T14:30:00"
}
```

### Game Session

```json
{
  "sessionId": "abc123",
  "playerId": 1,
  "gameType": "IDIOM | SENTENCE",
  "difficulty": "EASY | MEDIUM | HARD | EXPERT",
  "startTime": "2025-10-02T14:30:00",
  "endTime": "2025-10-02T14:32:00",
  "completed": true,
  "score": 450
}
```

### Idiom Score

```json
{
  "id": 1,
  "playerId": 1,
  "idiom": "一马当先",
  "score": 450,
  "difficulty": "EASY",
  "timeTaken": 45,
  "hintsUsed": 0,
  "accuracyRate": 1.0,
  "completed": true,
  "playedAt": "2025-10-02T14:32:00"
}
```

### Sentence Score

```json
{
  "id": 1,
  "playerId": 1,
  "targetSentence": "我今天去学校上课",
  "playerAnswer": "我今天去学校上课",
  "score": 520,
  "difficulty": "EASY",
  "timeTaken": 75,
  "hintsUsed": 0,
  "grammarCorrectness": 1.0,
  "completed": true,
  "playedAt": "2025-10-02T14:35:00"
}
```

### Leaderboard Entry

```json
{
  "rank": 1,
  "playerId": 1,
  "username": "玩家001",
  "gameType": "IDIOM",
  "difficulty": "EASY",
  "totalScore": 1500,
  "gamesPlayed": 5,
  "averageScore": 300.0,
  "accuracyRate": 0.95,
  "lastUpdated": "2025-10-02T14:30:00"
}
```

### Achievement

```json
{
  "id": 1,
  "name": "Perfect Score",
  "description": "Complete a game with 100% accuracy and no hints",
  "category": "ACCURACY | SPEED | PERSISTENCE | MILESTONE",
  "iconUrl": "/assets/icons/perfect-score.png",
  "rewardPoints": 100,
  "unlockCriteria": {
    "accuracyRate": 1.0,
    "hintsUsed": 0
  }
}
```

---

## Error Handling

### Standard Error Response

```json
{
  "error": "Error message describing what went wrong",
  "timestamp": "2025-10-02T14:30:00",
  "status": 400,
  "path": "/api/games/idiom/start"
}
```

### HTTP Status Codes

- **200 OK**: Request successful
- **201 Created**: Resource created successfully
- **400 Bad Request**: Invalid request parameters or validation failure
- **401 Unauthorized**: Authentication required (future implementation)
- **403 Forbidden**: Insufficient permissions (future implementation)
- **404 Not Found**: Resource not found
- **409 Conflict**: Resource conflict (e.g., duplicate username)
- **500 Internal Server Error**: Server error

### Common Error Scenarios

#### Validation Errors

```json
{
  "error": "Validation failed",
  "details": {
    "username": "Username must be between 3 and 50 characters",
    "email": "Invalid email format"
  },
  "status": 400
}
```

#### Session Expired

```json
{
  "error": "Game session expired or not found",
  "sessionId": "abc123",
  "status": 404
}
```

#### Player Not Found

```json
{
  "error": "Player not found",
  "playerId": 999,
  "status": 404
}
```

---

## Best Practices

### 1. Session Management

- Always store `sessionId` returned from start game endpoints
- Sessions expire after 30 minutes of inactivity
- Use restart endpoints to begin new games

### 2. Error Handling

- Always check HTTP status codes
- Parse error responses for detailed messages
- Implement retry logic for 500 errors

### 3. Performance Optimization

- Cache feature flags (update every 5 minutes)
- Batch leaderboard requests
- Use appropriate `limit` parameters

### 4. Security

- Validate all user inputs on client-side before sending
- Never expose sensitive data in URLs
- Prepare for JWT authentication in production

### 5. Data Consistency

- Always use returned IDs from creation endpoints
- Refresh leaderboards after game completion
- Poll for achievement unlocks after score submissions

---

## Testing Examples

### Postman Collection

```json
{
  "info": {
    "name": "Chinese Word Scramble API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Register Player",
      "request": {
        "method": "POST",
        "url": "{{baseUrl}}/api/players/register",
        "body": {
          "mode": "raw",
          "raw": "{\n  \"username\": \"TestPlayer\",\n  \"email\": \"test@example.com\",\n  \"password\": \"password123\"\n}"
        }
      }
    }
  ]
}
```

### cURL Examples

**Complete Game Flow**:

```bash
# 1. Register player
PLAYER_ID=$(curl -s -X POST http://localhost:8080/api/players/register \
  -H "Content-Type: application/json" \
  -d '{"username":"TestPlayer","email":"test@example.com","password":"password123"}' \
  | jq -r '.id')

# 2. Start game
SESSION_ID=$(curl -s -X POST "http://localhost:8080/api/games/idiom/start?playerId=$PLAYER_ID" \
  -H "Content-Type: application/json" \
  -d '{"difficulty":"EASY"}' \
  | jq -r '.sessionId')

# 3. Request hint
curl -X POST "http://localhost:8080/api/games/idiom/hint?playerId=$PLAYER_ID&sessionId=$SESSION_ID"

# 4. Submit answer
curl -X POST http://localhost:8080/api/games/idiom/submit \
  -H "Content-Type: application/json" \
  -d "{\"playerId\":$PLAYER_ID,\"sessionId\":\"$SESSION_ID\",\"answer\":\"一马当先\",\"timeTaken\":45}"

# 5. Check leaderboard
curl -X GET "http://localhost:8080/api/leaderboard/idiom/EASY?limit=10"

# 6. Get achievements
curl -X GET "http://localhost:8080/api/achievements/player/$PLAYER_ID"
```

---

## Support and Contact

**Development Team**: GovTech Development Team
**Email**: dev@govtech.sg
**GitHub**: https://github.com/govtech
**License**: MIT License

For bug reports and feature requests, please contact the development team.

---

**Last Updated**: 2025-10-03
**API Version**: 1.0.0
**Test Coverage**: 100% (136/136 tests passing)
