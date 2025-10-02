# Chinese Word Scramble Game - API Documentation

## Overview

Base URL: `http://localhost:8080`

API Version: `1.0.0`

All endpoints return JSON responses with proper HTTP status codes.

## Authentication

Currently using basic authentication. Future releases will include JWT token-based authentication.

## Error Responses

All errors follow a standard format:

```json
{
  "timestamp": "2025-10-01T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Detailed error message",
  "path": "/api/endpoint",
  "details": {
    "field": "error details"
  }
}
```

### Common HTTP Status Codes

- `200 OK` - Request successful
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid request parameters
- `404 Not Found` - Resource not found
- `409 Conflict` - Duplicate resource
- `500 Internal Server Error` - Server error

---

## Player Management

### Register Player

Register a new player account.

**Endpoint**: `POST /api/players/register`

**Request Body**:
```json
{
  "username": "chineselearner",
  "email": "learner@example.com",
  "password": "SecurePassword123"
}
```

**Validation Rules**:
- `username`: 3-50 characters, required
- `email`: Valid email format, required
- `password`: Minimum 8 characters, required

**Success Response** (201 Created):
```json
{
  "id": 1,
  "username": "chineselearner",
  "email": "learner@example.com",
  "role": "PLAYER",
  "active": true,
  "message": "Player registered successfully"
}
```

**Error Response** (400 Bad Request):
```json
{
  "timestamp": "2025-10-01T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Username already exists",
  "path": "/api/players/register"
}
```

---

### Get Player

Retrieve player information by ID.

**Endpoint**: `GET /api/players/{id}`

**Path Parameters**:
- `id` (Long) - Player ID

**Success Response** (200 OK):
```json
{
  "id": 1,
  "username": "chineselearner",
  "email": "learner@example.com",
  "role": "PLAYER",
  "active": true,
  "lastLogin": "2025-10-01T10:30:00"
}
```

**Error Response** (404 Not Found):
```json
{
  "timestamp": "2025-10-01T10:30:00",
  "status": 404,
  "error": "Player Not Found",
  "message": "Player not found with ID: 999",
  "path": "/api/players/999"
}
```

---

### Get Player Statistics

Retrieve comprehensive player statistics.

**Endpoint**: `GET /api/players/{id}/statistics`

**Path Parameters**:
- `id` (Long) - Player ID

**Success Response** (200 OK):
```json
{
  "totalGames": 150,
  "idiomGames": 90,
  "sentenceGames": 60,
  "totalScore": 45000,
  "overallAccuracy": 92.5,
  "achievementCount": 7,
  "bestRank": 3,
  "leaderboardCount": 4
}
```

---

## Idiom Game

### Start Idiom Game

Start a new idiom scramble game.

**Endpoint**: `POST /api/games/idiom/start`

**Query Parameters**:
- `playerId` (Long) - Player ID

**Request Body**:
```json
{
  "difficulty": "MEDIUM"
}
```

**Difficulty Levels**: `EASY`, `MEDIUM`, `HARD`, `EXPERT`

**Success Response** (200 OK):
```json
{
  "sessionId": 123,
  "scrambledCharacters": ["语", "成", "全", "大"],
  "difficulty": "MEDIUM",
  "timeLimit": 120,
  "maxHints": 3,
  "idiomLength": 4,
  "startTime": "2025-10-01T10:30:00"
}
```

---

### Submit Idiom Answer

Submit an answer for the current idiom game.

**Endpoint**: `POST /api/games/idiom/submit`

**Query Parameters**:
- `playerId` (Long) - Player ID

**Request Body**:
```json
{
  "answer": "成语大全",
  "timeTaken": 45,
  "hintsUsed": 1
}
```

**Validation Rules**:
- `answer`: Required, not blank
- `timeTaken`: Minimum 1 second
- `hintsUsed`: 0-3

**Success Response** (200 OK):
```json
{
  "correct": true,
  "score": 320,
  "correctAnswer": "成语大全",
  "timeTaken": 45,
  "accuracyRate": 100.0,
  "hintsUsed": 1,
  "timeBonus": 50,
  "accuracyBonus": 100,
  "hintPenalty": -10,
  "baseScore": 200,
  "difficultyMultiplier": 1.2,
  "newAchievements": [
    {
      "type": "SPEED_DEMON",
      "title": "速度恶魔",
      "description": "在30秒内完成游戏"
    }
  ],
  "leaderboardUpdate": {
    "newRank": 5,
    "previousRank": 8,
    "improved": true
  }
}
```

---

### Get Idiom Hint

Request a hint for the current idiom game.

**Endpoint**: `POST /api/games/idiom/hint/{level}`

**Path Parameters**:
- `level` (Integer) - Hint level (1-3)

**Query Parameters**:
- `playerId` (Long) - Player ID

**Success Response** (200 OK):
```json
{
  "hintLevel": 1,
  "hintContent": "第一个字：成",
  "penaltyApplied": 10,
  "hintsRemaining": 2,
  "hintDescription": "Reveals the first character"
}
```

**Hint Levels**:
- Level 1: First character (-10 points)
- Level 2: First two characters (-20 points)
- Level 3: First three characters (-30 points)

---

### Get Idiom Game History

Retrieve player's idiom game history.

**Endpoint**: `GET /api/games/idiom/history/{playerId}`

**Path Parameters**:
- `playerId` (Long) - Player ID

**Query Parameters** (Optional):
- `limit` (Integer) - Number of records (default: 10)

**Success Response** (200 OK):
```json
[
  {
    "id": 456,
    "difficulty": "MEDIUM",
    "score": 320,
    "timeTaken": 45,
    "accuracyRate": 100.0,
    "hintsUsed": 1,
    "completedAt": "2025-10-01T10:30:00"
  },
  {
    "id": 455,
    "difficulty": "EASY",
    "score": 250,
    "timeTaken": 30,
    "accuracyRate": 100.0,
    "hintsUsed": 0,
    "completedAt": "2025-10-01T09:15:00"
  }
]
```

---

## Sentence Game

### Start Sentence Game

Start a new sentence crafting game.

**Endpoint**: `POST /api/games/sentence/start`

**Query Parameters**:
- `playerId` (Long) - Player ID

**Request Body**:
```json
{
  "difficulty": "HARD"
}
```

**Success Response** (200 OK):
```json
{
  "sessionId": 124,
  "scrambledWords": ["学习", "我", "中文", "喜欢"],
  "difficulty": "HARD",
  "timeLimit": 180,
  "maxHints": 3,
  "wordCount": 4,
  "grammarPoint": "Subject + Verb + Object structure",
  "startTime": "2025-10-01T11:00:00"
}
```

---

### Submit Sentence Answer

Submit an answer for the current sentence game.

**Endpoint**: `POST /api/games/sentence/submit`

**Query Parameters**:
- `playerId` (Long) - Player ID

**Request Body**:
```json
{
  "answer": "我喜欢学习中文",
  "timeTaken": 90,
  "hintsUsed": 0
}
```

**Success Response** (200 OK):
```json
{
  "correct": true,
  "score": 510,
  "correctSentence": "我喜欢学习中文",
  "playerSentence": "我喜欢学习中文",
  "accuracyRate": 100.0,
  "similarityScore": 100.0,
  "grammarValid": true,
  "timeTaken": 90,
  "hintsUsed": 0,
  "timeBonus": 15,
  "accuracyBonus": 100,
  "hintPenalty": 0,
  "baseScore": 300,
  "difficultyMultiplier": 1.5,
  "feedback": "Perfect! Your sentence structure is grammatically correct."
}
```

---

### Get Sentence Hint

Request a hint for the current sentence game.

**Endpoint**: `POST /api/games/sentence/hint/{level}`

**Path Parameters**:
- `level` (Integer) - Hint level (1-3)

**Query Parameters**:
- `playerId` (Long) - Player ID

**Success Response** (200 OK):
```json
{
  "hintLevel": 2,
  "hintContent": "提示：包含「喜欢」这个词",
  "penaltyApplied": 20,
  "hintsRemaining": 1,
  "hintDescription": "Grammar structure hint"
}
```

---

### Get Sentence Game History

Retrieve player's sentence game history.

**Endpoint**: `GET /api/games/sentence/history/{playerId}`

**Path Parameters**:
- `playerId` (Long) - Player ID

**Success Response** (200 OK):
```json
[
  {
    "id": 789,
    "difficulty": "HARD",
    "score": 510,
    "timeTaken": 90,
    "accuracyRate": 100.0,
    "hintsUsed": 0,
    "completedAt": "2025-10-01T11:05:00"
  }
]
```

---

## Leaderboards

### Get Top Players

Retrieve top players for a specific game type and difficulty.

**Endpoint**: `GET /api/leaderboards/top`

**Query Parameters**:
- `gameType` (String) - `IDIOM` or `SENTENCE`
- `difficulty` (String) - `EASY`, `MEDIUM`, `HARD`, `EXPERT`
- `limit` (Integer) - Number of players (default: 10)

**Success Response** (200 OK):
```json
[
  {
    "rank": 1,
    "playerId": 5,
    "username": "idiommaster",
    "bestScore": 1300,
    "averageScore": 1150.5,
    "totalGames": 50,
    "lastPlayed": "2025-10-01T10:30:00"
  },
  {
    "rank": 2,
    "playerId": 12,
    "username": "chinesepro",
    "bestScore": 1250,
    "averageScore": 1100.0,
    "totalGames": 45,
    "lastPlayed": "2025-10-01T09:15:00"
  }
]
```

---

### Get Player Rankings

Retrieve all rankings for a specific player.

**Endpoint**: `GET /api/leaderboards/player/{playerId}`

**Path Parameters**:
- `playerId` (Long) - Player ID

**Success Response** (200 OK):
```json
[
  {
    "gameType": "IDIOM",
    "difficulty": "EXPERT",
    "rank": 3,
    "bestScore": 1200,
    "averageScore": 1050.0,
    "totalGames": 30
  },
  {
    "gameType": "SENTENCE",
    "difficulty": "HARD",
    "rank": 5,
    "bestScore": 650,
    "averageScore": 580.0,
    "totalGames": 25
  }
]
```

---

### Get Player Rank

Get player's rank for a specific board.

**Endpoint**: `GET /api/leaderboards/player/{playerId}/rank`

**Path Parameters**:
- `playerId` (Long) - Player ID

**Query Parameters**:
- `gameType` (String) - `IDIOM` or `SENTENCE`
- `difficulty` (String) - `EASY`, `MEDIUM`, `HARD`, `EXPERT`

**Success Response** (200 OK):
```json
{
  "playerId": 10,
  "gameType": "IDIOM",
  "difficulty": "EXPERT",
  "rank": 3,
  "bestScore": 1200,
  "averageScore": 1050.0,
  "totalGames": 30
}
```

**Response when not ranked** (200 OK):
```json
{
  "message": "Player has not played this difficulty yet",
  "rank": null
}
```

---

## Achievements

### Get Player Achievements

Retrieve all achievements for a player.

**Endpoint**: `GET /api/achievements/player/{playerId}`

**Path Parameters**:
- `playerId` (Long) - Player ID

**Success Response** (200 OK):
```json
[
  {
    "id": 1,
    "type": "FIRST_WIN",
    "title": "首次胜利",
    "description": "完成第一场游戏",
    "unlockedAt": "2025-09-15T08:00:00",
    "metadata": "{\"gameType\": \"IDIOM\", \"score\": 250}"
  },
  {
    "id": 2,
    "type": "SPEED_DEMON",
    "title": "速度恶魔",
    "description": "在30秒内完成游戏",
    "unlockedAt": "2025-10-01T10:30:00",
    "metadata": "{\"timeTaken\": 28, \"difficulty\": \"MEDIUM\"}"
  }
]
```

---

### Get Unlocked Achievements

Retrieve only unlocked achievements.

**Endpoint**: `GET /api/achievements/player/{playerId}/unlocked`

**Success Response** (200 OK):
```json
{
  "count": 7,
  "achievements": [
    {
      "id": 1,
      "type": "FIRST_WIN",
      "title": "首次胜利",
      "description": "完成第一场游戏",
      "unlockedAt": "2025-09-15T08:00:00",
      "metadata": "{}"
    }
  ]
}
```

---

### Get Achievement Progress

Get player's achievement completion statistics.

**Endpoint**: `GET /api/achievements/player/{playerId}/progress`

**Success Response** (200 OK):
```json
{
  "totalAchievements": 10,
  "unlockedCount": 7,
  "lockedCount": 3,
  "completionRate": "70.0%"
}
```

---

### Get All Achievements

List all available achievement types.

**Endpoint**: `GET /api/achievements/all`

**Success Response** (200 OK):
```json
{
  "FIRST_WIN": {
    "title": "首次胜利",
    "description": "完成第一场游戏"
  },
  "SPEED_DEMON": {
    "title": "速度恶魔",
    "description": "在30秒内完成游戏"
  },
  "PERFECT_SCORE": {
    "title": "完美得分",
    "description": "达到100%准确率且不使用提示"
  },
  "HUNDRED_GAMES": {
    "title": "百场达人",
    "description": "完成100场游戏"
  },
  "IDIOM_MASTER": {
    "title": "成语大师",
    "description": "在成语排行榜上排名第一"
  },
  "SENTENCE_MASTER": {
    "title": "句子大师",
    "description": "在句子排行榜上排名第一"
  },
  "TOP_RANKED": {
    "title": "顶级玩家",
    "description": "进入任何排行榜前10名"
  },
  "CONSISTENCY": {
    "title": "坚持不懈",
    "description": "连续7天玩游戏"
  },
  "HIGH_SCORER": {
    "title": "高分达人",
    "description": "单场游戏得分超过1000分"
  },
  "HINT_FREE": {
    "title": "无提示挑战",
    "description": "不使用提示完成10场游戏"
  }
}
```

---

## Feature Flags (Admin)

### Get All Feature Flags

Retrieve all feature flags with their status.

**Endpoint**: `GET /api/features/all`

**Success Response** (200 OK):
```json
[
  {
    "id": 1,
    "featureKey": "idiom_game_enabled",
    "description": "Enable idiom game mode",
    "enabled": true,
    "lastModified": "2025-10-01T08:00:00"
  },
  {
    "id": 2,
    "featureKey": "sentence_game_enabled",
    "description": "Enable sentence game mode",
    "enabled": true,
    "lastModified": "2025-10-01T08:00:00"
  }
]
```

---

### Get Feature Status

Check if a specific feature is enabled.

**Endpoint**: `GET /api/features/{key}`

**Path Parameters**:
- `key` (String) - Feature key

**Success Response** (200 OK):
```json
{
  "featureKey": "idiom_game_enabled",
  "description": "Enable idiom game mode",
  "enabled": true,
  "lastModified": "2025-10-01T08:00:00"
}
```

---

### Enable Feature

Enable a feature flag.

**Endpoint**: `POST /api/features/{key}/enable`

**Path Parameters**:
- `key` (String) - Feature key

**Success Response** (200 OK):
```json
{
  "message": "Feature enabled successfully",
  "featureKey": "beta_features",
  "enabled": true
}
```

---

### Disable Feature

Disable a feature flag.

**Endpoint**: `POST /api/features/{key}/disable`

**Success Response** (200 OK):
```json
{
  "message": "Feature disabled successfully",
  "featureKey": "beta_features",
  "enabled": false
}
```

---

### Get Active Features

Retrieve all currently active features.

**Endpoint**: `GET /api/features/active`

**Success Response** (200 OK):
```json
{
  "count": 5,
  "features": [
    {
      "featureKey": "idiom_game_enabled",
      "description": "Enable idiom game mode"
    },
    {
      "featureKey": "sentence_game_enabled",
      "description": "Enable sentence game mode"
    }
  ]
}
```

---

## Rate Limiting

Currently not implemented. Future releases will include rate limiting with the following limits:

- **Anonymous users**: 100 requests per hour
- **Authenticated users**: 1000 requests per hour
- **Game endpoints**: 10 games per minute per user

---

## Pagination

For endpoints returning lists, pagination parameters:

- `page` (Integer) - Page number (0-indexed, default: 0)
- `size` (Integer) - Page size (default: 20, max: 100)
- `sort` (String) - Sort field and direction (e.g., `score,desc`)

Response includes pagination metadata:

```json
{
  "content": [...],
  "totalElements": 150,
  "totalPages": 15,
  "size": 10,
  "number": 0,
  "first": true,
  "last": false
}
```

---

## Versioning

Current API version: `v1`

Future versions will use URL versioning: `/api/v2/...`

---

## Support

For API support and issues:
- GitHub Issues: https://github.com/govtech/chinese-scramble/issues
- Email: dev@govtech.com
- Documentation: http://localhost:8080/swagger-ui.html
