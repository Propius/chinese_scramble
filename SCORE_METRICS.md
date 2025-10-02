# Score Metrics Documentation 📊

## Overview

This document provides a comprehensive guide to the scoring system in the Chinese Word Scramble Game. The scoring algorithm is designed to reward accuracy, speed, and efficiency while discouraging excessive hint usage.

---

## 📐 Scoring Formula

### Base Score Calculation

```
Total Score = Base Score × Difficulty Multiplier × Time Bonus × Hint Penalty
```

### Component Breakdown

#### 1. Base Score
- **Idiom Game**: 100 points per correct answer
- **Sentence Game**: 100 points per correct answer

#### 2. Difficulty Multiplier

| Difficulty Level | Multiplier | Description |
|-----------------|------------|-------------|
| **Easy (简单)** | 1.0x | Base scoring |
| **Medium (中等)** | 1.5x | +50% bonus |
| **Hard (困难)** | 2.0x | +100% bonus |
| **Expert (专家)** | 3.0x | +200% bonus |

#### 3. Time Bonus

Rewards players for completing challenges quickly.

```
Time Bonus = 1 + (Remaining Time / Total Time Limit) × 0.5
```

**Example Calculations:**

| Time Limit | Time Taken | Remaining Time | Time Bonus |
|------------|------------|----------------|------------|
| 180s (Easy) | 60s | 120s | 1 + (120/180) × 0.5 = **1.33x** |
| 180s (Easy) | 120s | 60s | 1 + (60/180) × 0.5 = **1.17x** |
| 180s (Easy) | 180s | 0s | 1 + (0/180) × 0.5 = **1.0x** |
| 120s (Medium) | 30s | 90s | 1 + (90/120) × 0.5 = **1.38x** |
| 60s (Expert) | 20s | 40s | 1 + (40/60) × 0.5 = **1.33x** |

**Time Bonus Range**: 1.0x (no time left) to 1.5x (instant completion)

#### 4. Hint Penalty

Discourages excessive hint usage while still allowing players to get help.

```
Hint Penalty = 1 - (Hints Used × 0.15)
```

**Penalty Table:**

| Hints Used | Penalty Factor | Effective Score |
|------------|----------------|-----------------|
| 0 hints | 1.0x | 100% |
| 1 hint | 0.85x | 85% |
| 2 hints | 0.70x | 70% |
| 3 hints | 0.55x | 55% |

**Maximum Hints**: 3 per question

---

## 🎯 Scoring Examples

### Example 1: Perfect Score (Easy Mode)

**Scenario:**
- Difficulty: Easy
- Time Limit: 180 seconds
- Time Taken: 30 seconds
- Hints Used: 0

**Calculation:**
```
Base Score = 100
Difficulty Multiplier = 1.0
Time Bonus = 1 + ((180-30)/180) × 0.5 = 1 + (150/180) × 0.5 = 1.42
Hint Penalty = 1.0

Total Score = 100 × 1.0 × 1.42 × 1.0 = 142 points
```

### Example 2: Expert with Hints

**Scenario:**
- Difficulty: Expert
- Time Limit: 60 seconds
- Time Taken: 40 seconds
- Hints Used: 2

**Calculation:**
```
Base Score = 100
Difficulty Multiplier = 3.0
Time Bonus = 1 + ((60-40)/60) × 0.5 = 1 + (20/60) × 0.5 = 1.17
Hint Penalty = 1 - (2 × 0.15) = 0.70

Total Score = 100 × 3.0 × 1.17 × 0.70 = 246 points
```

### Example 3: Medium Difficulty, Slow Completion

**Scenario:**
- Difficulty: Medium
- Time Limit: 120 seconds
- Time Taken: 115 seconds
- Hints Used: 1

**Calculation:**
```
Base Score = 100
Difficulty Multiplier = 1.5
Time Bonus = 1 + ((120-115)/120) × 0.5 = 1 + (5/120) × 0.5 = 1.02
Hint Penalty = 1 - (1 × 0.15) = 0.85

Total Score = 100 × 1.5 × 1.02 × 0.85 = 130 points
```

### Example 4: Hard Difficulty, No Time Bonus

**Scenario:**
- Difficulty: Hard
- Time Limit: 90 seconds
- Time Taken: 90 seconds (timeout)
- Hints Used: 3

**Calculation:**
```
Base Score = 100
Difficulty Multiplier = 2.0
Time Bonus = 1 + ((90-90)/90) × 0.5 = 1.0
Hint Penalty = 1 - (3 × 0.15) = 0.55

Total Score = 100 × 2.0 × 1.0 × 0.55 = 110 points
```

---

## 📈 Score Ranges

### Theoretical Score Ranges by Difficulty

| Difficulty | Min Score (3 hints, timeout) | Max Score (0 hints, instant) |
|------------|------------------------------|------------------------------|
| **Easy** | 55 points | 150 points |
| **Medium** | 83 points | 225 points |
| **Hard** | 110 points | 300 points |
| **Expert** | 165 points | 450 points |

### Score Rating System

| Score Range | Rating | Performance |
|-------------|--------|-------------|
| 0-50 | ⭐ | Needs Practice |
| 51-100 | ⭐⭐ | Fair |
| 101-150 | ⭐⭐⭐ | Good |
| 151-200 | ⭐⭐⭐⭐ | Great |
| 201-300 | ⭐⭐⭐⭐⭐ | Excellent |
| 301+ | 🏆 | Master |

---

## 🎮 Game-Specific Scoring

### Idiom Game (成语拼字)

**Scoring Factors:**
- **Correctness**: Must match exact idiom
- **Character Order**: All characters in correct positions
- **Time Efficiency**: Faster completion = higher bonus
- **Hint Usage**: Each hint reduces final score

**Additional Bonuses:**
- **Perfect Answer** (0 hints, <30s): +10 bonus points
- **Streak Bonus**: +5 points per consecutive correct answer (max +25)

**Sample Idiom Scores:**

| Idiom | Difficulty | Time | Hints | Score |
|-------|------------|------|-------|-------|
| 画龙点睛 | Easy | 45s | 0 | 126 |
| 一帆风顺 | Easy | 90s | 1 | 93 |
| 守株待兔 | Medium | 50s | 0 | 201 |
| 鹤立鸡群 | Hard | 60s | 1 | 238 |
| 刻舟求剑 | Expert | 30s | 0 | 420 |

### Sentence Game (造句游戏)

**Scoring Factors:**
- **Grammar Correctness**: Proper sentence structure
- **Word Order**: All words in grammatically correct positions
- **Semantic Accuracy**: Sentence makes sense
- **Time Efficiency**: Faster completion = higher bonus
- **Hint Usage**: Each hint reduces final score

**Additional Scoring:**
- **Grammar Score** (0-100): Based on sentence structure accuracy
- **Similarity Score** (0-100): Compared to reference answer
- **Combined Score**: `(Grammar Score + Similarity Score) / 2`

**Formula:**
```
Total Score = (Combined Score / 100) × Base Score × Difficulty × Time Bonus × Hint Penalty
```

**Sample Sentence Scores:**

| Sentence Type | Difficulty | Time | Hints | Grammar | Similarity | Score |
|--------------|------------|------|-------|---------|------------|-------|
| Simple (SVO) | Easy | 60s | 0 | 95 | 90 | 126 |
| Compound | Medium | 80s | 1 | 85 | 80 | 128 |
| Complex | Hard | 70s | 2 | 90 | 85 | 183 |
| Advanced | Expert | 40s | 0 | 95 | 95 | 410 |

---

## 📊 Leaderboard Scoring

### Overall Ranking Calculation

**Total Player Score** = Sum of Top 10 Games

Players are ranked by their cumulative best performances:

```
Leaderboard Score = Top 10 Game Scores (sorted descending)
```

**Example:**
```
Player A Scores: [450, 420, 380, 350, 320, 300, 280, 250, 220, 200, ...]
Leaderboard Score = 450 + 420 + 380 + 350 + 320 + 300 + 280 + 250 + 220 + 200
                  = 3,170 points
```

### Per-Difficulty Leaderboards

Each difficulty level maintains its own leaderboard:

| Leaderboard Type | Calculation Method |
|------------------|-------------------|
| **Easy Leaderboard** | Top 10 Easy mode scores |
| **Medium Leaderboard** | Top 10 Medium mode scores |
| **Hard Leaderboard** | Top 10 Hard mode scores |
| **Expert Leaderboard** | Top 10 Expert mode scores |
| **Overall Leaderboard** | Top 10 scores across all difficulties |

### Ranking Tie-Breakers

When players have identical scores, ranking is determined by:

1. **Total Games Played** (fewer is better)
2. **Average Time per Game** (faster is better)
3. **Total Hints Used** (fewer is better)
4. **Registration Date** (earlier is better)

---

## 📈 Statistics Metrics

### Player Statistics Tracked

#### Performance Metrics

| Metric | Description | Calculation |
|--------|-------------|-------------|
| **Total Score** | Sum of all game scores | Σ(all scores) |
| **Average Score** | Mean score across all games | Total Score ÷ Games Played |
| **Best Score** | Highest single game score | MAX(all scores) |
| **Games Played** | Total number of games | COUNT(games) |
| **Win Rate** | Percentage of correct answers | (Correct ÷ Total) × 100% |

#### Efficiency Metrics

| Metric | Description | Calculation |
|--------|-------------|-------------|
| **Average Time** | Mean completion time | Σ(time) ÷ Games Played |
| **Fastest Time** | Quickest completion | MIN(time) |
| **Hint Usage Rate** | Average hints per game | Σ(hints) ÷ Games Played |
| **Perfect Games** | Games with no hints, high score | COUNT(hints=0 AND score>threshold) |

#### Difficulty Distribution

| Metric | Description |
|--------|-------------|
| **Easy Games** | Count of Easy mode games |
| **Medium Games** | Count of Medium mode games |
| **Hard Games** | Count of Hard mode games |
| **Expert Games** | Count of Expert mode games |

---

## 🏆 Achievements & Milestones

### Achievement System

| Achievement | Requirement | Bonus Points |
|------------|-------------|--------------|
| **First Win** | Complete first game | 50 |
| **Speed Demon** | Complete in <20s | 100 |
| **No Help Needed** | Win without hints | 75 |
| **Perfect 10** | 10 consecutive wins | 200 |
| **Expert Master** | Win 10 Expert games | 300 |
| **High Scorer** | Single game score >400 | 150 |
| **Dedicated Player** | Play 100 games | 250 |
| **Time Trial** | Complete 5 games in 5 minutes | 200 |

### Milestone Rewards

| Milestone | Requirement | Reward |
|-----------|-------------|--------|
| **Bronze Tier** | 1,000 total points | Badge + 10% score bonus |
| **Silver Tier** | 5,000 total points | Badge + 15% score bonus |
| **Gold Tier** | 10,000 total points | Badge + 20% score bonus |
| **Platinum Tier** | 25,000 total points | Badge + 25% score bonus |
| **Diamond Tier** | 50,000 total points | Badge + 30% score bonus |

---

## 🔄 Score Decay & Freshness

### Activity-Based Scoring

To encourage regular play, the leaderboard considers score freshness:

```
Adjusted Score = Base Score × Freshness Factor
```

**Freshness Factor:**

| Last Activity | Freshness Factor | Impact |
|--------------|------------------|--------|
| <7 days | 1.0x | No decay |
| 7-14 days | 0.95x | -5% |
| 14-30 days | 0.90x | -10% |
| 30-60 days | 0.80x | -20% |
| 60+ days | 0.70x | -30% |

**Note**: Score decay applies only to leaderboard rankings, not to player's actual scores.

---

## 📉 Negative Scoring Events

### Score Deductions

| Event | Penalty | Description |
|-------|---------|-------------|
| **Wrong Answer** | -10 points | Incorrect submission |
| **Timeout** | -5 points | Failed to complete in time |
| **Excessive Hints** | -15 points | Using all 3 hints |
| **Quit Mid-Game** | -20 points | Abandoning active game |
| **Cheating Detection** | -500 points | Automated detection |

**Minimum Score**: 0 points (scores cannot go negative)

---

## 🎯 Optimal Scoring Strategies

### Tips for Maximum Score

1. **Difficulty Selection**
   - Start with Medium/Hard for higher multipliers
   - Master each level before advancing
   - Expert mode offers highest potential scores

2. **Time Management**
   - Aim for <50% of time limit
   - Practice speed without sacrificing accuracy
   - Time bonus can add up to 50% to base score

3. **Hint Usage**
   - Avoid hints when possible (0 hints = full score)
   - Use hints strategically (1 hint = 15% penalty)
   - Save hints for genuinely difficult questions

4. **Consistency**
   - Build winning streaks for bonus points
   - Maintain high accuracy rate
   - Play regularly to maintain leaderboard position

5. **Practice Focus**
   - Learn common idiom patterns
   - Study grammar structures
   - Improve pattern recognition speed

---

## 📊 Score Analysis Dashboard

### Personal Score Breakdown

Players can view detailed score analytics:

```
┌─────────────────────────────────────────┐
│  Your Performance Summary               │
├─────────────────────────────────────────┤
│  Total Score:      12,450 points        │
│  Average Score:    186 points/game      │
│  Best Score:       425 points           │
│  Games Played:     67 games             │
│  Win Rate:         82.5%                │
│  Avg Time:         52 seconds           │
│  Avg Hints:        0.8 hints/game       │
│  Current Rank:     #23                  │
│  Tier:             Silver ⭐⭐          │
└─────────────────────────────────────────┘
```

### Score Distribution Graph

```
Score Range        Count  Percentage
0-100             ▓▓▓▓▓  12 games (18%)
101-200           ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  32 games (48%)
201-300           ▓▓▓▓▓▓▓▓▓  18 games (27%)
301-400           ▓▓▓  4 games (6%)
401+              ▓  1 game (1%)
```

---

## 🔮 Future Scoring Enhancements

### Planned Features

1. **Seasonal Challenges**
   - Limited-time events with bonus scoring
   - Special multipliers for seasonal content
   - Exclusive achievements

2. **Multiplayer Scoring**
   - Head-to-head competitions
   - Team-based scoring
   - Real-time leaderboards

3. **Adaptive Difficulty**
   - Dynamic difficulty adjustment
   - Personalized scoring thresholds
   - Skill-based matchmaking

4. **Social Features**
   - Share scores on social media
   - Challenge friends
   - Collaborative scoring

---

## 📖 API Response Examples

### Idiom Game Score Response

```json
{
  "isCorrect": true,
  "score": 246,
  "breakdown": {
    "baseScore": 100,
    "difficultyMultiplier": 3.0,
    "timeBonus": 1.17,
    "hintPenalty": 0.70,
    "bonusPoints": 0
  },
  "stats": {
    "timeTaken": 40,
    "timeLimit": 60,
    "hintsUsed": 2,
    "difficulty": "EXPERT"
  },
  "meaning": "比喻在困境中等待时机...",
  "example": "他守株待兔，终于等到了机会。"
}
```

### Sentence Game Score Response

```json
{
  "isValid": true,
  "score": 183,
  "breakdown": {
    "baseScore": 100,
    "grammarScore": 90,
    "similarityScore": 85,
    "combinedScore": 87.5,
    "difficultyMultiplier": 2.0,
    "timeBonus": 1.23,
    "hintPenalty": 0.70
  },
  "stats": {
    "timeTaken": 70,
    "timeLimit": 90,
    "hintsUsed": 2,
    "difficulty": "HARD"
  },
  "translation": "He quickly ran to school.",
  "grammarExplanation": "Subject-Adverb-Verb-Preposition-Object pattern"
}
```

---

## 📞 Support & Feedback

### Questions About Scoring?

- **API Documentation**: http://localhost:8080/swagger-ui.html
- **Issue Tracking**: [Issue.md](./Issue.md)
- **README**: [README.md](./README.md)

### Scoring Algorithm Changes

All scoring algorithm updates are documented in:
- **Version**: Check API response headers
- **Changelog**: See release notes
- **Backward Compatibility**: Maintained for existing scores

---

**Last Updated**: January 2025
**Version**: 1.0.0
**Status**: ✅ Production Ready

---

Made with ❤️ for competitive Chinese language learners
