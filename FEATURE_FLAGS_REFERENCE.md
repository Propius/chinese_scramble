# Feature Flags Reference Guide 🚩

Complete reference for all feature flags in the Chinese Word Scramble Game.

---

## 📑 Table of Contents

1. [Overview](#overview)
2. [Frontend Feature Flags](#frontend-feature-flags)
3. [Backend Feature Flags](#backend-feature-flags)
4. [Flag Configuration](#flag-configuration)
5. [Testing Feature Flags](#testing-feature-flags)
6. [Best Practices](#best-practices)

---

## Overview

Feature flags allow you to enable or disable specific functionality without changing code. This is useful for:

- **A/B Testing**: Test different features with different user groups
- **Gradual Rollout**: Enable features progressively
- **Emergency Rollback**: Quickly disable problematic features
- **Environment-Specific Settings**: Different configurations for dev/staging/prod

---

## Frontend Feature Flags

**Location**: `chinese-scramble-frontend/src/constants/game.constants.ts`

### Complete Flag List

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

---

### 1. ENABLE_IDIOM_SCRAMBLE

**Default**: `true`

**Purpose**: Controls whether the Idiom Scramble game mode is available.

**Impact**:
- **UI**: Shows/hides "Idiom Scramble" button on home page
- **Routing**: Enables/disables `/idiom-game` route
- **API Calls**: Frontend won't call idiom game endpoints if disabled

**When to Disable**:
- Maintenance on idiom database
- Feature not ready for production
- Focus testing on sentence game only

**Example**:
```typescript
ENABLE_IDIOM_SCRAMBLE: false,  // Idiom game hidden from home page
```

**Test Scenarios**:
- ✅ With flag ON: Idiom game button visible and functional
- ✅ With flag OFF: No idiom game button, direct URL access shows error

---

### 2. ENABLE_SENTENCE_CRAFTING

**Default**: `true`

**Purpose**: Controls whether the Sentence Crafting game mode is available.

**Impact**:
- **UI**: Shows/hides "Sentence Crafting" button on home page
- **Routing**: Enables/disables `/sentence-game` route
- **API Calls**: Frontend won't call sentence game endpoints if disabled

**When to Disable**:
- Maintenance on sentence database
- Feature not ready for production
- Focus testing on idiom game only

**Example**:
```typescript
ENABLE_SENTENCE_CRAFTING: false,  // Sentence game hidden from home page
```

**Test Scenarios**:
- ✅ With flag ON: Sentence game button visible and functional
- ✅ With flag OFF: No sentence game button, direct URL access shows error

---

### 3. ENABLE_LEADERBOARD

**Default**: `true`

**Purpose**: Controls whether leaderboards are visible to players.

**Impact**:
- **UI**: Shows/hides leaderboard link in navigation
- **Pages**: Enables/disables `/leaderboard` route
- **API Calls**: Frontend won't fetch leaderboard data if disabled
- **Player Motivation**: Players won't see rankings

**When to Disable**:
- Privacy concerns
- Competitive pressure not desired
- Leaderboard recalculation in progress
- Testing without social pressure

**Example**:
```typescript
ENABLE_LEADERBOARD: false,  // No leaderboard link or page
```

**Test Scenarios**:
- ✅ With flag ON: Leaderboard accessible, rankings shown
- ✅ With flag OFF: No leaderboard link, page returns 404

---

### 4. ENABLE_AUDIO_PRONUNCIATION

**Default**: `true`

**Purpose**: Controls whether audio pronunciation playback is available.

**Impact**:
- **UI**: Shows/hides speaker icon buttons
- **Audio**: Enables/disables audio playback functionality
- **Accessibility**: Removes audio learning aid

**When to Disable**:
- Audio files not ready
- Bandwidth concerns
- Silent environments (exams, libraries)
- Browser audio issues

**Example**:
```typescript
ENABLE_AUDIO_PRONUNCIATION: false,  // No audio buttons shown
```

**Test Scenarios**:
- ✅ With flag ON: Audio buttons visible, playback works
- ✅ With flag OFF: No audio buttons, no playback

**Technical Note**: Audio files should still be served by backend, only frontend controls visibility.

---

### 5. ENABLE_HINTS

**Default**: `true`

**Purpose**: Controls whether the hint system is available during gameplay.

**Impact**:
- **UI**: Shows/hides hint buttons (Level 1, 2, 3)
- **Gameplay**: Players can't request hints
- **Scoring**: No hint penalties applied (players can't use hints anyway)
- **Difficulty**: Game becomes harder without hint assistance

**When to Disable**:
- Hard mode testing
- Exam/assessment mode (no assistance)
- Hint database maintenance
- Encourage pure learning

**Example**:
```typescript
ENABLE_HINTS: false,  // No hint buttons, no hints available
```

**Hint Levels** (when enabled):
- **Level 1** (10 points penalty): Meaning/definition hint
- **Level 2** (20 points penalty): First character/word hint
- **Level 3** (30 points penalty): Complete answer structure hint

**Test Scenarios**:
- ✅ With flag ON: Hint buttons visible, hints work, penalties apply
- ✅ With flag OFF: No hint buttons, POST /hint returns error

---

### 6. ENABLE_PRACTICE_MODE

**Default**: `true`

**Purpose**: Controls whether practice mode toggle is available.

**Impact**:
- **UI**: Shows/hides "Practice Mode" toggle on game page
- **Gameplay**: Players can't switch to practice mode
- **Question Repetition**: All games follow ENABLE_NO_REPEAT_QUESTIONS setting

**Practice Mode Behavior** (when enabled):
- Questions CAN repeat
- No quiz completion state
- Continuous play
- Good for learning

**When to Disable**:
- Force all players to do full quiz mode
- Assessment/exam scenarios
- Limited question pool

**Example**:
```typescript
ENABLE_PRACTICE_MODE: false,  // No practice mode toggle
```

**Test Scenarios**:
- ✅ With flag ON: Toggle visible, can switch modes
- ✅ With flag OFF: No toggle, always in quiz mode

---

### 7. ENABLE_ACHIEVEMENTS

**Default**: `true`

**Purpose**: Controls whether achievements are tracked and displayed.

**Impact**:
- **UI**: Shows/hides achievement notifications and badges
- **Gamification**: No achievement unlocks or rewards
- **Motivation**: Less gamification incentive
- **API Calls**: Frontend won't fetch achievement data

**Achievement Types** (when enabled):
- **First Win**: Complete first question
- **Perfect Score**: 100% accuracy, no hints
- **Speed Demon**: Complete in < 30 seconds
- **Practice Makes Perfect**: Complete 10 questions
- **Master**: Complete 50 questions
- **Difficulty Champion**: Complete all difficulties

**When to Disable**:
- Remove gamification elements
- Focus on pure learning
- Achievement system maintenance
- Privacy concerns

**Example**:
```typescript
ENABLE_ACHIEVEMENTS: false,  // No achievement tracking
```

**Test Scenarios**:
- ✅ With flag ON: Achievements unlock, notifications show
- ✅ With flag OFF: No achievement tracking or notifications

---

### 8. ENABLE_NO_REPEAT_QUESTIONS

**Default**: `true`

**Purpose**: Controls whether questions can repeat during a session.

**Impact**:
- **Game Flow**: Changes between quiz mode and continuous practice
- **Question Selection**: Backend excludes answered questions if enabled
- **Completion**: Players can complete all questions (quiz complete state)
- **User Experience**: Different flows for learning vs assessment

**Quiz Mode** (flag = `true`):
```
Start Game → Question → Submit → Next Question → ... → Quiz Complete
```
- Questions never repeat
- Quiz ends when all questions answered
- Shows "恭喜完成！" completion message
- Backend tracks `excludedQuestionIds`

**Practice Mode** (flag = `false`):
```
Start Game → Question → Submit → Next Question → ... → (Infinite Loop)
```
- Questions CAN repeat
- No quiz completion state
- Continuous practice
- Good for repetition learning

**When to Disable**:
- Allow question repetition for practice
- Limited question pool
- Focus on memorization through repetition
- Testing specific questions

**Example**:
```typescript
ENABLE_NO_REPEAT_QUESTIONS: false,  // Questions can repeat
```

**Test Scenarios**:
- ✅ With flag ON: Questions don't repeat, quiz completes
- ✅ With flag OFF: Questions can repeat, no completion

**Implementation Details**:
- Frontend tracks `excludedQuestionIds` array
- Backend receives array in API calls
- Backend filters out excluded questions
- When all questions answered, returns completion message

---

## Backend Feature Flags

**Location**: `chinese-scramble-backend/src/main/resources/application.yml`

### Complete Flag List

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

---

### Backend Flag Details

#### 1. enable-idiom-scramble

**Default**: `true`

**Purpose**: Backend control for idiom game endpoints.

**Impact**:
- API endpoints `/api/idiom-game/*` return 403 if disabled
- Database queries for idioms not executed
- No idiom score tracking

**Configuration**:
```yaml
game:
  features:
    enable-idiom-scramble: false  # Disable idiom endpoints
```

---

#### 2. enable-sentence-crafting

**Default**: `true`

**Purpose**: Backend control for sentence game endpoints.

**Impact**:
- API endpoints `/api/sentence-game/*` return 403 if disabled
- Database queries for sentences not executed
- No sentence score tracking

**Configuration**:
```yaml
game:
  features:
    enable-sentence-crafting: false  # Disable sentence endpoints
```

---

#### 3. enable-leaderboard

**Default**: `true`

**Purpose**: Backend control for leaderboard functionality.

**Impact**:
- API endpoints `/api/leaderboard/*` return 403 if disabled
- No leaderboard updates after game completion
- Rank calculations not performed

**Configuration**:
```yaml
game:
  features:
    enable-leaderboard: false  # Disable leaderboard
```

---

#### 4. enable-hints

**Default**: `true`

**Purpose**: Backend control for hint system.

**Impact**:
- API endpoints `/api/*/hint/*` return 403 if disabled
- No hint penalty calculations
- Hint usage not tracked

**Configuration**:
```yaml
game:
  features:
    enable-hints: false  # Disable hints
```

---

#### 5. enable-achievements

**Default**: `true`

**Purpose**: Backend control for achievement tracking.

**Impact**:
- API endpoints `/api/achievements/*` return 403 if disabled
- No achievement checks after game completion
- Achievement unlocks not tracked

**Configuration**:
```yaml
game:
  features:
    enable-achievements: false  # Disable achievements
```

---

#### 6. enable-no-repeat-questions

**Default**: `true`

**Purpose**: Backend control for question repetition.

**Impact**:
- Backend filters `excludedQuestionIds` if enabled
- Returns completion message when all questions answered
- Ignores `excludedQuestionIds` array if disabled

**Configuration**:
```yaml
game:
  features:
    enable-no-repeat-questions: false  # Allow repeats
```

---

## Flag Configuration

### Changing Frontend Flags

**Step 1**: Open configuration file
```bash
code chinese-scramble-frontend/src/constants/game.constants.ts
```

**Step 2**: Modify flag value
```typescript
export const DEFAULT_FEATURE_FLAGS: FeatureFlags = {
  ENABLE_HINTS: false,  // Changed from true
};
```

**Step 3**: Restart frontend
```bash
cd chinese-scramble-frontend
npm start
```

**Step 4**: Verify changes
- Open browser console
- Check `window.gameConstants.DEFAULT_FEATURE_FLAGS`

---

### Changing Backend Flags

**Step 1**: Open configuration file
```bash
code chinese-scramble-backend/src/main/resources/application.yml
```

**Step 2**: Modify flag value
```yaml
game:
  features:
    enable-hints: false  # Changed from true
```

**Step 3**: Restart backend
```bash
cd chinese-scramble-backend
./mvnw spring-boot:run
```

**Step 4**: Verify changes
```bash
curl http://localhost:8080/api/feature-flags/all
```

---

### Using Environment Variables (Production)

**Frontend** (`.env` file):
```bash
REACT_APP_ENABLE_HINTS=false
REACT_APP_ENABLE_LEADERBOARD=true
```

**Backend** (environment variables):
```bash
GAME_FEATURES_ENABLE_HINTS=false
GAME_FEATURES_ENABLE_LEADERBOARD=true
```

---

## Testing Feature Flags

### Manual Testing

**Test Each Flag Independently**:
1. Set flag to `false`
2. Restart service
3. Verify UI changes
4. Verify API behavior
5. Verify error handling
6. Set flag back to `true`

**Example Test Plan for ENABLE_HINTS**:

| Test Case | Flag Value | Expected Behavior | Pass/Fail |
|-----------|------------|-------------------|-----------|
| UI: Hint buttons visible | true | Buttons shown | ✅ |
| UI: Hint buttons hidden | false | Buttons hidden | ✅ |
| API: Hint request success | true | Returns hint | ✅ |
| API: Hint request blocked | false | Returns 403 | ✅ |
| Score: Hint penalty applied | true | Penalty deducted | ✅ |
| Score: No penalty | false | No deduction | ✅ |

---

### Automated Testing

**Frontend Tests**:
```typescript
describe('Feature Flag: ENABLE_HINTS', () => {
  describe('when flag is true', () => {
    beforeAll(() => {
      Object.defineProperty(gameConstants.DEFAULT_FEATURE_FLAGS, 'ENABLE_HINTS', {
        value: true,
        writable: true,
        configurable: true,
      });
    });

    it('should show hint buttons', () => {
      // Test implementation
    });
  });

  describe('when flag is false', () => {
    beforeAll(() => {
      Object.defineProperty(gameConstants.DEFAULT_FEATURE_FLAGS, 'ENABLE_HINTS', {
        value: false,
        writable: true,
        configurable: true,
      });
    });

    it('should hide hint buttons', () => {
      // Test implementation
    });
  });
});
```

**Backend Tests**:
```java
@Test
@WithMockFeatureFlag(key = "enable-hints", value = false)
void hintEndpoint_WhenDisabled_ReturnsForbidden() throws Exception {
    mockMvc.perform(post("/api/idiom-game/hint/1")
            .param("playerId", "1"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value("Hints are disabled"));
}
```

---

## Best Practices

### 1. Feature Flag Hygiene

**DO**:
- ✅ Document flag purpose and impact
- ✅ Test both ON and OFF states
- ✅ Use consistent naming (ENABLE_*)
- ✅ Remove flags after feature is stable
- ✅ Keep frontend and backend flags in sync

**DON'T**:
- ❌ Leave unused flags in code
- ❌ Nest flags too deeply (complex logic)
- ❌ Use flags as permanent configuration
- ❌ Forget to update documentation

---

### 2. Flag Lifecycle

**Stage 1: Development**
```typescript
ENABLE_NEW_FEATURE: false,  // Feature in development
```

**Stage 2: Testing**
```typescript
ENABLE_NEW_FEATURE: true,  // Feature ready for testing
```

**Stage 3: Production (Gradual Rollout)**
```typescript
// Use environment variables for gradual rollout
// Dev: true, Staging: true, Prod: false → Prod: true
```

**Stage 4: Cleanup**
```typescript
// Remove flag, feature is stable
// Delete conditional code, make feature always-on
```

---

### 3. Flag Dependencies

Some flags depend on others:

**Example**: `ENABLE_PRACTICE_MODE` depends on `ENABLE_NO_REPEAT_QUESTIONS`
- If `ENABLE_NO_REPEAT_QUESTIONS` is `false`, practice mode has no effect
- Document these dependencies clearly

**Example**: `ENABLE_HINTS` affects scoring
- Hint buttons only show if flag is ON
- Penalties only apply if hints are actually used

---

### 4. Emergency Rollback

**Scenario**: Feature is causing issues in production

**Quick Fix**:
1. Change flag to `false` in configuration
2. Restart service (< 1 minute downtime)
3. Feature is disabled, users see stable version
4. Fix bug in code
5. Re-enable flag after fix

**Without Feature Flags**: Would require code rollback, rebuild, redeploy (15+ minutes)

---

### 5. A/B Testing

**Use Case**: Test if hints improve or hurt learning

**Group A** (Control):
```typescript
ENABLE_HINTS: false,
```

**Group B** (Experiment):
```typescript
ENABLE_HINTS: true,
```

**Measure**:
- Learning outcomes
- Completion rates
- Time to completion
- User satisfaction

---

## Flag Impact Matrix

Quick reference for all flags:

| Flag | UI Impact | API Impact | Database Impact | Scoring Impact |
|------|-----------|------------|-----------------|----------------|
| ENABLE_IDIOM_SCRAMBLE | Hides button | Blocks endpoints | No queries | No idiom scores |
| ENABLE_SENTENCE_CRAFTING | Hides button | Blocks endpoints | No queries | No sentence scores |
| ENABLE_LEADERBOARD | Hides page | Blocks endpoints | No updates | No effect |
| ENABLE_AUDIO_PRONUNCIATION | Hides audio buttons | No effect | No effect | No effect |
| ENABLE_HINTS | Hides hint buttons | Blocks endpoints | No hint tracking | No penalties |
| ENABLE_PRACTICE_MODE | Hides toggle | No effect | No effect | No effect |
| ENABLE_ACHIEVEMENTS | Hides notifications | Blocks endpoints | No tracking | No bonuses |
| ENABLE_NO_REPEAT_QUESTIONS | Changes flow | Filters questions | More tracking | No effect |

---

## Troubleshooting

### Flag Not Taking Effect

**Problem**: Changed flag but no visible change

**Solutions**:
1. ✅ Restart the service (frontend/backend)
2. ✅ Clear browser cache (Ctrl+Shift+R)
3. ✅ Check correct file was edited
4. ✅ Check for typos in flag name
5. ✅ Verify build succeeded

---

### Flag Causes Errors

**Problem**: Enabling flag causes API errors

**Solutions**:
1. ✅ Check backend and frontend flags match
2. ✅ Check database has required data
3. ✅ Check logs for error details
4. ✅ Verify feature is fully implemented

---

## Related Documentation

- **User Guide**: [USER_GUIDE.md](./USER_GUIDE.md) - How flags affect gameplay
- **Admin Guide**: [ADMIN_GUIDE.md](./ADMIN_GUIDE.md) - How to configure flags
- **API Docs**: [API_DOCUMENTATION.md](./chinese-scramble-backend/API_DOCUMENTATION.md) - API behavior with flags
- **Developer Setup**: [DEVELOPER_SETUP.md](./chinese-scramble-backend/DEVELOPER_SETUP.md) - Development configuration

---

**Last Updated**: 2025-10-03
**Maintainer**: GovTech Development Team
