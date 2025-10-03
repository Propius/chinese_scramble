# Chinese Word Scramble Game - User Guide 🎮🀄

Complete guide for players, administrators, and developers.

---

## 📑 Table of Contents

1. [Getting Started](#getting-started)
2. [Game Modes](#game-modes)
3. [Game Flow](#game-flow)
4. [Feature Flags](#feature-flags)
5. [Scoring System](#scoring-system)
6. [Leaderboard](#leaderboard)
7. [Achievements](#achievements)
8. [Configuration](#configuration)

---

## Getting Started

### For Players

1. **Access the Game**
   - Navigate to `http://localhost:3000` (local)
   - Or use your deployed URL

2. **Enter Your Name**
   - First-time players: Enter a username
   - Returning players: Use your existing username
   - Your progress is automatically saved

3. **Choose a Game Mode**
   - **Idiom Scramble**: Unscramble Chinese idioms (成语)
   - **Sentence Crafting**: Arrange words to form correct sentences

4. **Select Difficulty**
   - **EASY**: 4 characters, 180 seconds
   - **MEDIUM**: 4 characters, 120 seconds
   - **HARD**: 4-6 characters, 90 seconds
   - **EXPERT**: 6-8 characters, 60 seconds

---

## Game Modes

### 1. Idiom Scramble (成语挑战)

**Objective**: Unscramble Chinese characters to form correct idioms.

**How to Play**:
1. View the scrambled characters
2. Drag and drop characters into the correct order
3. Use hints if needed (costs points)
4. Submit your answer before time runs out

**Example**:
- **Scrambled**: 明争暗斗
- **Correct**: 明争暗斗 (open strife and veiled struggle)

**Hints Available** (if `ENABLE_HINTS` is ON):
- **Level 1** (10 points): Meaning hint
- **Level 2** (20 points): First character hint
- **Level 3** (30 points): Complete pinyin

---

### 2. Sentence Crafting (句子组合)

**Objective**: Arrange Chinese words to form grammatically correct sentences.

**How to Play**:
1. View the scrambled words and translation
2. Drag and drop words into the correct order
3. Follow the grammar pattern shown
4. Submit your answer before time runs out

**Example**:
- **Translation**: "I like learning Chinese"
- **Scrambled**: 我 | 学习 | 喜欢 | 中文
- **Correct**: 我喜欢学习中文
- **Pattern**: Subject + Verb + Object

**Hints Available** (if `ENABLE_HINTS` is ON):
- **Level 1** (10 points): Grammar pattern explanation
- **Level 2** (20 points): First word hint
- **Level 3** (30 points): Complete sentence structure

---

## Game Flow

### Standard Flow (ENABLE_NO_REPEAT_QUESTIONS = true)

```
┌─────────────────┐
│  Start Game     │ ← Select difficulty
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Load Question   │ ← Backend checks excluded questions
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Play Question   │ ← Drag/drop, use hints, timer running
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Submit Answer   │ ← Validate and score
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌─────┐   ┌─────┐
│ ✓   │   │  ✗  │
└──┬──┘   └──┬──┘
   │         │
   └────┬────┘
        │
        ▼
┌─────────────────┐
│  Show Result    │ ← Display score, feedback
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Load Next       │ ← Automatically load next question
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌─────────┐  ┌──────────────┐
│ Success │  │ All Complete │
└────┬────┘  └──────┬───────┘
     │              │
     │              ▼
     │         ┌──────────────┐
     │         │ Quiz Complete│ ← Show total score, offer restart
     │         └──────────────┘
     │
     └───────► (Continue playing)
```

### Practice Mode Flow (ENABLE_NO_REPEAT_QUESTIONS = false)

```
┌─────────────────┐
│  Start Game     │ ← Select difficulty
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Load Random Q   │ ← No exclusion, questions can repeat
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Play Question   │ ← Same as standard
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Submit Answer   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Show Result    │ ← 3-second delay
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Load Next       │ ← Automatically after 3s
└────────┬────────┘
         │
         └───────► (Repeat indefinitely)
```

**Key Difference**: In practice mode, questions can repeat and there's no "quiz complete" state - players practice continuously.

---

## Feature Flags

Feature flags control what functionality is available in the game.

### Available Feature Flags

| Flag | Default | Description | Impact |
|------|---------|-------------|--------|
| **ENABLE_IDIOM_SCRAMBLE** | `true` | Enable idiom game mode | Shows/hides idiom game on home page |
| **ENABLE_SENTENCE_CRAFTING** | `true` | Enable sentence game mode | Shows/hides sentence game on home page |
| **ENABLE_LEADERBOARD** | `true` | Enable leaderboard | Shows/hides leaderboard link and page |
| **ENABLE_AUDIO_PRONUNCIATION** | `true` | Enable audio playback | Shows/hides audio buttons in game |
| **ENABLE_HINTS** | `true` | Enable hint system | Shows/hides hint buttons and penalties |
| **ENABLE_PRACTICE_MODE** | `true` | Enable practice mode | Shows/hides practice mode toggle |
| **ENABLE_ACHIEVEMENTS** | `true` | Enable achievements | Shows/hides achievement notifications |
| **ENABLE_NO_REPEAT_QUESTIONS** | `true` | Prevent question repetition | Changes game flow (see above) |

### Changing Feature Flags

#### Frontend Configuration

**File**: `chinese-scramble-frontend/src/constants/game.constants.ts`

```typescript
export const DEFAULT_FEATURE_FLAGS: FeatureFlags = {
  ENABLE_IDIOM_SCRAMBLE: true,
  ENABLE_SENTENCE_CRAFTING: true,
  ENABLE_LEADERBOARD: true,
  ENABLE_AUDIO_PRONUNCIATION: true,
  ENABLE_HINTS: true,
  ENABLE_PRACTICE_MODE: true,
  ENABLE_ACHIEVEMENTS: true,
  ENABLE_NO_REPEAT_QUESTIONS: true,
};
```

**Steps to Change**:
1. Open `game.constants.ts`
2. Change the flag value (`true` → `false` or vice versa)
3. Save the file
4. Restart the frontend server

**Example - Disable Hints**:
```typescript
export const DEFAULT_FEATURE_FLAGS: FeatureFlags = {
  // ... other flags
  ENABLE_HINTS: false,  // Changed from true
  // ... other flags
};
```

#### Backend Configuration

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

**Steps to Change**:
1. Open `application.yml`
2. Change the flag value
3. Save the file
4. Restart the backend server

---

## Scoring System

### Base Score Calculation

```
Base Score = DIFFICULTY_BASE_POINTS × DIFFICULTY_MULTIPLIER
```

| Difficulty | Base Points | Multiplier | Final Base |
|------------|-------------|------------|------------|
| EASY | 100 | 1.0 | 100 |
| MEDIUM | 200 | 1.5 | 300 |
| HARD | 300 | 2.0 | 600 |
| EXPERT | 500 | 3.0 | 1,500 |

### Time Bonus

```
Time Bonus = (Remaining Time / Total Time) × TIME_BONUS_POINTS
```

| Difficulty | Time Bonus Points |
|------------|-------------------|
| EASY | 50 |
| MEDIUM | 100 |
| HARD | 150 |
| EXPERT | 250 |

**Example** (MEDIUM difficulty):
- Time limit: 120s
- Time taken: 45s
- Remaining time: 75s
- Time bonus: (75/120) × 100 = 62.5 points

### Hint Penalties

| Hint Level | Penalty |
|------------|---------|
| Level 1 (Meaning) | -10 points |
| Level 2 (First character) | -20 points |
| Level 3 (Complete answer) | -30 points |

### Accuracy Bonus/Penalty

| Accuracy | Multiplier |
|----------|------------|
| 100% (Perfect) | 1.0 (no change) |
| < 100% | Score reduced based on errors |

### Final Score Formula

```
Final Score = (Base Score + Time Bonus - Hint Penalties) × Accuracy
```

**Example Calculation**:
- Base Score: 300 (MEDIUM)
- Time Bonus: 62.5
- Hints Used: Level 1 (-10)
- Accuracy: 100%
- **Final: (300 + 62.5 - 10) × 1.0 = 352.5 points**

---

## Leaderboard

### Leaderboard Types

1. **Idiom Game Leaderboard**
   - Separate rankings per difficulty
   - Top 10 players per difficulty
   - Ranked by highest score

2. **Sentence Game Leaderboard**
   - Separate rankings per difficulty
   - Top 10 players per difficulty
   - Ranked by highest score

### Leaderboard Display

```
┌─────────────────────────────────────┐
│  IDIOM GAME - EASY DIFFICULTY       │
├──────┬──────────┬────────┬──────────┤
│ Rank │ Player   │ Score  │ Date     │
├──────┼──────────┼────────┼──────────┤
│  🥇  │ Alice    │ 450    │ Oct 3    │
│  🥈  │ Bob      │ 420    │ Oct 3    │
│  🥉  │ Charlie  │ 400    │ Oct 2    │
│  4   │ David    │ 380    │ Oct 3    │
│  5   │ Emma     │ 350    │ Oct 1    │
└──────┴──────────┴────────┴──────────┘
```

### Rank Calculation

- Players are ranked by score (descending)
- Ties are broken by earliest timestamp
- Only the highest score per player is shown
- Ranks are recalculated after each game

---

## Achievements

### Achievement Types

| Achievement | Criteria | Reward |
|-------------|----------|--------|
| **First Win** | Complete your first question | Badge |
| **Perfect Score** | Get 100% accuracy with no hints | Badge + 50 bonus points |
| **Speed Demon** | Complete in < 30 seconds | Badge |
| **Practice Makes Perfect** | Complete 10 questions | Badge |
| **Master** | Complete 50 questions | Badge |
| **Difficulty Champion** | Complete all difficulties | Badge + 100 bonus points |

### Achievement Notifications

When you unlock an achievement:
1. Confetti animation
2. Achievement badge displayed
3. Bonus points added to score (if applicable)
4. Achievement saved to your profile

---

## Configuration

### Difficulty Settings

#### Idiom Game

**File**: `chinese-scramble-frontend/src/constants/game.constants.ts`

```typescript
export const DIFFICULTY_SETTINGS = {
  EASY: {
    minCharacters: 4,
    maxCharacters: 4,
    timeLimit: 180,  // seconds
    basePoints: 100,
    timeBonus: 50,
  },
  // ... other difficulties
};
```

#### Sentence Game

```typescript
export const SENTENCE_DIFFICULTY_SETTINGS = {
  EASY: {
    minWords: 5,
    maxWords: 8,
    timeLimit: 180,  // seconds
    basePoints: 100,
    timeBonus: 50,
  },
  // ... other difficulties
};
```

### Hint Configuration

```typescript
export const HINT_PENALTIES = {
  LEVEL1: 10,   // Meaning hint
  LEVEL2: 20,   // First character/word
  LEVEL3: 30,   // Complete answer
};

export const MAX_HINTS_PER_QUESTION = 3;
```

### UI Configuration

```typescript
export const LEADERBOARD_TOP_COUNT = 10;  // Show top 10 players

export const MAX_EXCLUDED_QUESTIONS = 50;  // Max questions to track
```

---

## Tips for Players

### Maximizing Your Score

1. **Work Quickly**: Time bonus adds significant points
2. **Avoid Hints**: Each hint costs points
3. **Practice First**: Use practice mode to learn patterns
4. **Start Easy**: Build confidence with EASY difficulty
5. **Use Audio**: Listen to pronunciation for hints

### Strategy by Game Mode

#### Idiom Scramble
- Look for common character pairs (e.g., 一...一)
- Use the meaning hint if stuck
- Remember idioms follow patterns (e.g., ABCD, AABB, ABAC)

#### Sentence Crafting
- Follow the grammar pattern shown
- Subject typically comes first
- Use translation to understand meaning
- Common patterns: Subject + Verb + Object

---

## Troubleshooting

### Common Issues

**Problem**: Questions are repeating
- **Solution**: Ensure `ENABLE_NO_REPEAT_QUESTIONS` is `true`

**Problem**: Hints not showing
- **Solution**: Check `ENABLE_HINTS` is `true`

**Problem**: Leaderboard empty
- **Solution**: Check `ENABLE_LEADERBOARD` is `true` and play some games

**Problem**: Audio not playing
- **Solution**: Check `ENABLE_AUDIO_PRONUNCIATION` is `true` and browser audio permissions

---

## Next Steps

- **Administrators**: See [ADMIN_GUIDE.md](./ADMIN_GUIDE.md) for question management
- **Developers**: See [DEVELOPER_SETUP.md](./chinese-scramble-backend/DEVELOPER_SETUP.md) for technical setup
- **API Reference**: See [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) for endpoints
