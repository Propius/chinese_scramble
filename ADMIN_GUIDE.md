# Chinese Word Scramble Game - Administrator Guide 🔧

Complete guide for administrators managing questions, configuration, and system settings.

---

## 📑 Table of Contents

1. [Question Management](#question-management)
2. [Database Structure](#database-structure)
3. [Adding Questions](#adding-questions)
4. [Feature Flag Configuration](#feature-flag-configuration)
5. [Backend Configuration](#backend-configuration)
6. [Data Import/Export](#data-importexport)
7. [Monitoring and Maintenance](#monitoring-and-maintenance)

---

## Question Management

### Question Types

The game supports two types of questions:

1. **Idiom Questions** (成语)
   - 4-8 characters
   - Chinese idioms with pinyin and meaning
   - Three-level hint system

2. **Sentence Questions** (句子)
   - 5-12 words
   - Complete sentences with translation
   - Grammar pattern hints

---

## Database Structure

### Collections/Tables

#### 1. `idioms` Collection

```json
{
  "id": "ObjectId or Long",
  "content": "一心一意",
  "pinyin": "yī xīn yī yì",
  "meaning": "Only one heart and mind - completely devoted",
  "difficulty": "EASY",
  "characterCount": 4,
  "audioUrl": "/audio/idioms/yixinyiyi.mp3",
  "createdAt": "2025-01-15T10:30:00Z",
  "updatedAt": "2025-01-15T10:30:00Z"
}
```

**Fields**:
- `id`: Unique identifier
- `content`: The idiom in Chinese characters (必填)
- `pinyin`: Romanized pronunciation (必填)
- `meaning`: English explanation (必填)
- `difficulty`: EASY, MEDIUM, HARD, or EXPERT (必填)
- `characterCount`: Number of characters, used for difficulty filtering (必填)
- `audioUrl`: Path to audio file (可选)
- `createdAt`: Timestamp of creation
- `updatedAt`: Timestamp of last update

**Constraints**:
- `content`: Must be 4-8 characters
- `difficulty`: Must match characterCount ranges
- `pinyin`: Must use valid pinyin format

**Indexes**:
- `difficulty` (for quick filtering)
- `characterCount` (for difficulty-based queries)

---

#### 2. `sentences` Collection

```json
{
  "id": "ObjectId or Long",
  "chineseText": "我喜欢学习中文",
  "translation": "I like learning Chinese",
  "words": ["我", "喜欢", "学习", "中文"],
  "wordCount": 4,
  "difficulty": "EASY",
  "grammarPattern": "Subject + Verb + Object",
  "audioUrl": "/audio/sentences/wo-xihuan-xuexi-zhongwen.mp3",
  "createdAt": "2025-01-15T10:30:00Z",
  "updatedAt": "2025-01-15T10:30:00Z"
}
```

**Fields**:
- `id`: Unique identifier
- `chineseText`: The complete sentence (必填)
- `translation`: English translation (必填)
- `words`: Array of words to scramble (必填)
- `wordCount`: Number of words (必填)
- `difficulty`: EASY, MEDIUM, HARD, or EXPERT (必填)
- `grammarPattern`: Grammar structure hint (必填)
- `audioUrl`: Path to audio file (可选)
- `createdAt`: Timestamp of creation
- `updatedAt`: Timestamp of last update

**Constraints**:
- `words`: Must join to form `chineseText`
- `wordCount`: Must equal length of `words` array
- `difficulty`: Must match wordCount ranges

**Indexes**:
- `difficulty` (for quick filtering)
- `wordCount` (for difficulty-based queries)

---

#### 3. `players` Collection

```json
{
  "id": "Long",
  "username": "alice123",
  "email": "alice@example.com",
  "password": "$2a$10$hashedpassword...",
  "registrationDate": "2025-01-15T10:30:00Z",
  "lastLoginDate": "2025-01-20T14:22:00Z"
}
```

---

#### 4. `idiom_scores` Collection

```json
{
  "id": "Long",
  "player": { "id": 1, "username": "alice123" },
  "idiom": { "id": 10, "content": "一心一意" },
  "score": 450,
  "timeTaken": 45,
  "hintsUsed": 1,
  "difficulty": "MEDIUM",
  "completedAt": "2025-01-20T14:30:00Z"
}
```

---

#### 5. `sentence_scores` Collection

```json
{
  "id": "Long",
  "player": { "id": 1, "username": "alice123" },
  "sentence": { "id": 20, "chineseText": "我喜欢学习中文" },
  "score": 380,
  "timeTaken": 60,
  "hintsUsed": 0,
  "difficulty": "EASY",
  "completedAt": "2025-01-20T14:35:00Z"
}
```

---

#### 6. `leaderboard` Collection

```json
{
  "id": "Long",
  "player": { "id": 1, "username": "alice123" },
  "gameType": "IDIOM_GAME",
  "difficulty": "MEDIUM",
  "highScore": 450,
  "rank": 3,
  "lastUpdated": "2025-01-20T14:30:00Z"
}
```

---

#### 7. `achievements` Collection

```json
{
  "id": "Long",
  "player": { "id": 1, "username": "alice123" },
  "achievementType": "PERFECT_SCORE",
  "gameType": "IDIOM_GAME",
  "difficulty": "MEDIUM",
  "unlockedAt": "2025-01-20T14:30:00Z"
}
```

---

## Adding Questions

### Method 1: Direct Database Insert (MongoDB)

#### Adding Idiom Questions

```javascript
// Connect to MongoDB
use chinese_scramble;

// Insert single idiom
db.idioms.insertOne({
  content: "画蛇添足",
  pinyin: "huà shé tiān zú",
  meaning: "Drawing a snake and adding feet - to ruin something by adding unnecessary details",
  difficulty: "MEDIUM",
  characterCount: 4,
  audioUrl: "/audio/idioms/huashetianzhu.mp3",
  createdAt: new Date(),
  updatedAt: new Date()
});

// Insert multiple idioms
db.idioms.insertMany([
  {
    content: "守株待兔",
    pinyin: "shǒu zhū dài tù",
    meaning: "Waiting by a tree for a rabbit - relying on luck rather than effort",
    difficulty: "EASY",
    characterCount: 4,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    content: "狐假虎威",
    pinyin: "hú jiǎ hǔ wēi",
    meaning: "A fox borrowing the tiger's might - to bully others by using someone's power",
    difficulty: "EASY",
    characterCount: 4,
    createdAt: new Date(),
    updatedAt: new Date()
  }
]);
```

#### Adding Sentence Questions

```javascript
// Insert single sentence
db.sentences.insertOne({
  chineseText: "我每天早上都去跑步",
  translation: "I go running every morning",
  words: ["我", "每天", "早上", "都", "去", "跑步"],
  wordCount: 6,
  difficulty: "MEDIUM",
  grammarPattern: "Subject + Time + Adverb + Verb",
  audioUrl: "/audio/sentences/morning-run.mp3",
  createdAt: new Date(),
  updatedAt: new Date()
});

// Insert multiple sentences
db.sentences.insertMany([
  {
    chineseText: "她喜欢吃水果",
    translation: "She likes eating fruit",
    words: ["她", "喜欢", "吃", "水果"],
    wordCount: 4,
    difficulty: "EASY",
    grammarPattern: "Subject + Verb + Object",
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    chineseText: "我们明天一起去图书馆学习",
    translation: "We will go to the library to study together tomorrow",
    words: ["我们", "明天", "一起", "去", "图书馆", "学习"],
    wordCount: 6,
    difficulty: "HARD",
    grammarPattern: "Subject + Time + Adverb + Verb + Location + Verb",
    createdAt: new Date(),
    updatedAt: new Date()
  }
]);
```

---

### Method 2: REST API (Recommended)

The backend provides REST endpoints for question management.

#### API Endpoints

**Base URL**: `http://localhost:8080/api/admin`

#### Add Idiom

```bash
curl -X POST http://localhost:8080/api/admin/idioms \
  -H "Content-Type: application/json" \
  -d '{
    "content": "画蛇添足",
    "pinyin": "huà shé tiān zú",
    "meaning": "Drawing a snake and adding feet",
    "difficulty": "MEDIUM",
    "characterCount": 4,
    "audioUrl": "/audio/idioms/huashetianzhu.mp3"
  }'
```

#### Add Sentence

```bash
curl -X POST http://localhost:8080/api/admin/sentences \
  -H "Content-Type: application/json" \
  -d '{
    "chineseText": "我每天早上都去跑步",
    "translation": "I go running every morning",
    "words": ["我", "每天", "早上", "都", "去", "跑步"],
    "wordCount": 6,
    "difficulty": "MEDIUM",
    "grammarPattern": "Subject + Time + Adverb + Verb",
    "audioUrl": "/audio/sentences/morning-run.mp3"
  }'
```

---

### Method 3: CSV Import

Create CSV files and import via script.

#### Idioms CSV Format

```csv
content,pinyin,meaning,difficulty,characterCount,audioUrl
守株待兔,shǒu zhū dài tù,Waiting by a tree for a rabbit,EASY,4,
狐假虎威,hú jiǎ hǔ wēi,A fox borrowing the tiger's might,EASY,4,
画蛇添足,huà shé tiān zú,Drawing a snake and adding feet,MEDIUM,4,/audio/idioms/huashetianzhu.mp3
```

#### Sentences CSV Format

```csv
chineseText,translation,words,wordCount,difficulty,grammarPattern,audioUrl
她喜欢吃水果,She likes eating fruit,"她,喜欢,吃,水果",4,EASY,Subject + Verb + Object,
我每天早上都去跑步,I go running every morning,"我,每天,早上,都,去,跑步",6,MEDIUM,Subject + Time + Adverb + Verb,/audio/sentences/morning-run.mp3
```

#### Import Script

```bash
# Import idioms
mongoimport --db chinese_scramble --collection idioms --type csv --headerline --file idioms.csv

# Import sentences
mongoimport --db chinese_scramble --collection sentences --type csv --headerline --file sentences.csv
```

---

## Feature Flag Configuration

### Frontend Feature Flags

**File**: `chinese-scramble-frontend/src/constants/game.constants.ts`

```typescript
export const DEFAULT_FEATURE_FLAGS: FeatureFlags = {
  ENABLE_IDIOM_SCRAMBLE: true,          // Show idiom game mode
  ENABLE_SENTENCE_CRAFTING: true,       // Show sentence game mode
  ENABLE_LEADERBOARD: true,             // Show leaderboard
  ENABLE_AUDIO_PRONUNCIATION: true,     // Enable audio playback
  ENABLE_HINTS: true,                   // Enable hint system
  ENABLE_PRACTICE_MODE: true,           // Enable practice mode toggle
  ENABLE_ACHIEVEMENTS: true,            // Enable achievements
  ENABLE_NO_REPEAT_QUESTIONS: true,     // Prevent question repetition
};
```

**To Change Flags**:
1. Edit `game.constants.ts`
2. Change `true` to `false` (or vice versa)
3. Save file
4. Restart frontend: `npm start`

**Example - Disable Hints**:
```typescript
ENABLE_HINTS: false,  // Hints will not show in game
```

---

### Backend Feature Flags

**File**: `chinese-scramble-backend/src/main/resources/application.yml`

```yaml
game:
  features:
    enable-idiom-scramble: true
    enable-sentence-crafting: true
    enable-leaderboard: true
    enable-hints: true
    enable-achievements: true
    enable-no-repeat-questions: true
```

**To Change Flags**:
1. Edit `application.yml`
2. Change `true` to `false` (or vice versa)
3. Save file
4. Restart backend: `./mvnw spring-boot:run`

**Example - Disable Achievements**:
```yaml
game:
  features:
    enable-achievements: false  # No achievement tracking
```

---

### Feature Flag Impact Matrix

| Flag | Frontend Impact | Backend Impact | Database Impact |
|------|----------------|----------------|-----------------|
| **ENABLE_IDIOM_SCRAMBLE** | Hides idiom game button | Disables idiom endpoints | No impact |
| **ENABLE_SENTENCE_CRAFTING** | Hides sentence game button | Disables sentence endpoints | No impact |
| **ENABLE_LEADERBOARD** | Hides leaderboard link | Disables leaderboard queries | No impact |
| **ENABLE_AUDIO_PRONUNCIATION** | Hides audio buttons | No impact | No impact |
| **ENABLE_HINTS** | Hides hint buttons | Disables hint tracking | No `hint_usage` records |
| **ENABLE_PRACTICE_MODE** | Hides practice toggle | No impact | No impact |
| **ENABLE_ACHIEVEMENTS** | Hides achievement notifications | Disables achievement tracking | No `achievements` records |
| **ENABLE_NO_REPEAT_QUESTIONS** | Changes game flow (repeats vs no-repeats) | Changes question selection logic | More `excluded_questions` records |

---

## Backend Configuration

### Application Properties

**File**: `chinese-scramble-backend/src/main/resources/application.yml`

#### Database Configuration

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/chinese_scramble
      # For production with authentication:
      # uri: mongodb://username:password@host:27017/chinese_scramble
```

#### Server Configuration

```yaml
server:
  port: 8080
  servlet:
    context-path: /
```

#### CORS Configuration

```yaml
cors:
  allowed-origins:
    - http://localhost:3000
    - https://yourdomain.com
  allowed-methods:
    - GET
    - POST
    - PUT
    - DELETE
  allowed-headers:
    - "*"
```

#### Logging Configuration

```yaml
logging:
  level:
    root: INFO
    com.govtech.chinesescramble: DEBUG
  file:
    name: logs/application.log
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

#### Game Settings

```yaml
game:
  difficulty:
    easy:
      time-limit: 180    # seconds
      base-points: 100
      time-bonus: 50
    medium:
      time-limit: 120
      base-points: 200
      time-bonus: 100
    hard:
      time-limit: 90
      base-points: 300
      time-bonus: 150
    expert:
      time-limit: 60
      base-points: 500
      time-bonus: 250

  hints:
    level1-penalty: 10   # Meaning hint
    level2-penalty: 20   # First character
    level3-penalty: 30   # Complete answer

  leaderboard:
    top-count: 10        # Show top 10 players

  features:
    enable-idiom-scramble: true
    enable-sentence-crafting: true
    enable-leaderboard: true
    enable-hints: true
    enable-achievements: true
    enable-no-repeat-questions: true
```

---

## Data Import/Export

### Export Questions

#### MongoDB Export

```bash
# Export all idioms
mongoexport --db chinese_scramble --collection idioms --out idioms_backup.json

# Export all sentences
mongoexport --db chinese_scramble --collection sentences --out sentences_backup.json

# Export with query filter
mongoexport --db chinese_scramble --collection idioms \
  --query '{"difficulty": "EASY"}' \
  --out easy_idioms.json
```

### Import Questions

#### MongoDB Import

```bash
# Import idioms
mongoimport --db chinese_scramble --collection idioms --file idioms_backup.json

# Import sentences
mongoimport --db chinese_scramble --collection sentences --file sentences_backup.json

# Drop and reimport (replace all)
mongoimport --db chinese_scramble --collection idioms \
  --file idioms_backup.json \
  --drop
```

### Backup All Data

```bash
# Full database backup
mongodump --db chinese_scramble --out /backup/$(date +%Y%m%d)

# Restore from backup
mongorestore --db chinese_scramble /backup/20250120/chinese_scramble
```

---

## Monitoring and Maintenance

### Health Check Endpoints

```bash
# Backend health check
curl http://localhost:8080/actuator/health

# Database connection check
curl http://localhost:8080/api/health/db
```

### Database Maintenance

#### Check Collection Sizes

```javascript
use chinese_scramble;

db.stats();
db.idioms.count();
db.sentences.count();
db.players.count();
db.idiom_scores.count();
db.sentence_scores.count();
```

#### Clean Old Data

```javascript
// Delete scores older than 90 days
db.idiom_scores.deleteMany({
  completedAt: { $lt: new Date(Date.now() - 90 * 24 * 60 * 60 * 1000) }
});

db.sentence_scores.deleteMany({
  completedAt: { $lt: new Date(Date.now() - 90 * 24 * 60 * 60 * 1000) }
});
```

#### Rebuild Indexes

```javascript
db.idioms.reIndex();
db.sentences.reIndex();
db.leaderboard.reIndex();
```

### Log Monitoring

```bash
# View recent logs
tail -f chinese-scramble-backend/logs/application.log

# Search for errors
grep "ERROR" chinese-scramble-backend/logs/application.log

# Count errors by type
grep "ERROR" logs/application.log | awk '{print $NF}' | sort | uniq -c
```

### Performance Monitoring

#### Check Database Performance

```javascript
// Enable profiling
db.setProfilingLevel(2);

// Check slow queries
db.system.profile.find().sort({ts: -1}).limit(10).pretty();

// Explain query performance
db.idioms.find({difficulty: "EASY"}).explain("executionStats");
```

### User Management

#### List All Users

```bash
curl http://localhost:8080/api/admin/players
```

#### Delete Inactive Users

```javascript
// Delete users who haven't logged in for 180 days
db.players.deleteMany({
  lastLoginDate: { $lt: new Date(Date.now() - 180 * 24 * 60 * 60 * 1000) }
});
```

---

## Troubleshooting

### Common Issues

**Problem**: Questions not showing up
- **Check**: Verify questions exist in database
- **Check**: Verify difficulty matches character/word count
- **Solution**: Run `db.idioms.find({difficulty: "EASY"})` to verify

**Problem**: Leaderboard not updating
- **Check**: Verify `ENABLE_LEADERBOARD` is `true`
- **Check**: Check backend logs for errors
- **Solution**: Restart backend service

**Problem**: Feature flag changes not taking effect
- **Check**: Did you restart the service?
- **Solution**: Restart both frontend and backend

**Problem**: Database connection errors
- **Check**: Is MongoDB running? `systemctl status mongod`
- **Check**: Connection string in `application.yml`
- **Solution**: Verify MongoDB is running and accessible

---

## Security Considerations

### Authentication

- All admin endpoints should require authentication
- Use JWT tokens for API access
- Implement role-based access control (ADMIN, USER)

### Data Validation

- Validate all input data
- Sanitize user-submitted content
- Prevent SQL/NoSQL injection

### Rate Limiting

```yaml
# Add rate limiting configuration
rate-limit:
  enabled: true
  requests-per-minute: 60
```

---

## Next Steps

- **Players**: See [USER_GUIDE.md](./USER_GUIDE.md) for game instructions
- **Developers**: See [DEVELOPER_SETUP.md](./chinese-scramble-backend/DEVELOPER_SETUP.md) for technical setup
- **API Reference**: See [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) for endpoints
