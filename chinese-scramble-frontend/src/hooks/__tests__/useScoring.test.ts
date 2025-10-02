import { renderHook } from '@testing-library/react';
import { useScoring } from '../useScoring';
import { Difficulty } from '../../constants/difficulties';

// Mock the constants
jest.mock('../../constants/game.constants', () => ({
  DIFFICULTY_SETTINGS: {
    EASY: {
      minCharacters: 4,
      maxCharacters: 4,
      timeLimit: 180,
      basePoints: 100,
      timeBonus: 50,
    },
    MEDIUM: {
      minCharacters: 4,
      maxCharacters: 4,
      timeLimit: 120,
      basePoints: 200,
      timeBonus: 100,
    },
    HARD: {
      minCharacters: 4,
      maxCharacters: 6,
      timeLimit: 90,
      basePoints: 300,
      timeBonus: 150,
    },
    EXPERT: {
      minCharacters: 6,
      maxCharacters: 8,
      timeLimit: 60,
      basePoints: 500,
      timeBonus: 250,
    },
  },
  DIFFICULTY_MULTIPLIERS: {
    EASY: 1.0,
    MEDIUM: 1.5,
    HARD: 2.0,
    EXPERT: 3.0,
  },
  HINT_PENALTIES: {
    LEVEL1: 10,
    LEVEL2: 20,
    LEVEL3: 30,
  },
}));

describe('useScoring', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Hook initialization', () => {
    it('should return calculateScore function', () => {
      const { result } = renderHook(() => useScoring());

      expect(result.current.calculateScore).toBeDefined();
      expect(typeof result.current.calculateScore).toBe('function');
    });

    it('should maintain referential equality of calculateScore', () => {
      const { result, rerender } = renderHook(() => useScoring());

      const firstCalculateScore = result.current.calculateScore;

      rerender();

      expect(result.current.calculateScore).toBe(firstCalculateScore);
    });
  });

  describe('Incorrect answer handling', () => {
    it('should return zero score breakdown for incorrect answer', () => {
      const { result } = renderHook(() => useScoring());

      const scoreBreakdown = result.current.calculateScore({
        difficulty: Difficulty.EASY,
        timeTaken: 60,
        timeLimit: 180,
        hintsUsed: 0,
        correct: false,
      });

      expect(scoreBreakdown).toEqual({
        basePoints: 0,
        timeBonus: 0,
        difficultyMultiplier: 1,
        hintPenalty: 0,
        finalScore: 0,
      });
    });

    it('should return zero score regardless of difficulty for incorrect answer', () => {
      const { result } = renderHook(() => useScoring());

      const difficulties = [Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD, Difficulty.EXPERT];

      difficulties.forEach((difficulty) => {
        const scoreBreakdown = result.current.calculateScore({
          difficulty,
          timeTaken: 30,
          timeLimit: 180,
          hintsUsed: 0,
          correct: false,
        });

        expect(scoreBreakdown.finalScore).toBe(0);
      });
    });

    it('should return zero score even with fast completion time for incorrect answer', () => {
      const { result } = renderHook(() => useScoring());

      const scoreBreakdown = result.current.calculateScore({
        difficulty: Difficulty.EXPERT,
        timeTaken: 5,
        timeLimit: 60,
        hintsUsed: 0,
        correct: false,
      });

      expect(scoreBreakdown.finalScore).toBe(0);
    });
  });

  describe('Base points calculation', () => {
    it('should calculate base points from EASY difficulty settings', () => {
      const { result } = renderHook(() => useScoring());

      const scoreBreakdown = result.current.calculateScore({
        difficulty: Difficulty.EASY,
        timeTaken: 180,
        timeLimit: 180,
        hintsUsed: 0,
        correct: true,
      });

      expect(scoreBreakdown.basePoints).toBe(100);
    });

    it('should calculate base points from MEDIUM difficulty settings', () => {
      const { result } = renderHook(() => useScoring());

      const scoreBreakdown = result.current.calculateScore({
        difficulty: Difficulty.MEDIUM,
        timeTaken: 120,
        timeLimit: 120,
        hintsUsed: 0,
        correct: true,
      });

      expect(scoreBreakdown.basePoints).toBe(200);
    });

    it('should calculate base points from HARD difficulty settings', () => {
      const { result } = renderHook(() => useScoring());

      const scoreBreakdown = result.current.calculateScore({
        difficulty: Difficulty.HARD,
        timeTaken: 90,
        timeLimit: 90,
        hintsUsed: 0,
        correct: true,
      });

      expect(scoreBreakdown.basePoints).toBe(300);
    });

    it('should calculate base points from EXPERT difficulty settings', () => {
      const { result } = renderHook(() => useScoring());

      const scoreBreakdown = result.current.calculateScore({
        difficulty: Difficulty.EXPERT,
        timeTaken: 60,
        timeLimit: 60,
        hintsUsed: 0,
        correct: true,
      });

      expect(scoreBreakdown.basePoints).toBe(500);
    });

    it('should fallback to 100 base points for undefined difficulty', () => {
      const { result } = renderHook(() => useScoring());

      const scoreBreakdown = result.current.calculateScore({
        difficulty: 'UNKNOWN' as Difficulty,
        timeTaken: 60,
        timeLimit: 120,
        hintsUsed: 0,
        correct: true,
      });

      expect(scoreBreakdown.basePoints).toBe(100);
    });
  });

  describe('Time bonus calculation', () => {
    it('should calculate time bonus based on remaining time', () => {
      const { result } = renderHook(() => useScoring());

      // EASY: timeLimit=180, timeBonus=50, timeTaken=60, remaining=120
      // timeBonus = floor((120/180) * 50) = floor(33.33) = 33
      const scoreBreakdown = result.current.calculateScore({
        difficulty: Difficulty.EASY,
        timeTaken: 60,
        timeLimit: 180,
        hintsUsed: 0,
        correct: true,
      });

      expect(scoreBreakdown.timeBonus).toBe(33);
    });

    it('should give full time bonus when answered immediately', () => {
      const { result } = renderHook(() => useScoring());

      // EASY: timeLimit=180, timeBonus=50, timeTaken=0, remaining=180
      // timeBonus = floor((180/180) * 50) = floor(50) = 50
      const scoreBreakdown = result.current.calculateScore({
        difficulty: Difficulty.EASY,
        timeTaken: 0,
        timeLimit: 180,
        hintsUsed: 0,
        correct: true,
      });

      expect(scoreBreakdown.timeBonus).toBe(50);
    });

    it('should give zero time bonus when time limit exceeded', () => {
      const { result } = renderHook(() => useScoring());

      const scoreBreakdown = result.current.calculateScore({
        difficulty: Difficulty.EASY,
        timeTaken: 200,
        timeLimit: 180,
        hintsUsed: 0,
        correct: true,
      });

      expect(scoreBreakdown.timeBonus).toBe(0);
    });

    it('should give zero time bonus when answered exactly at time limit', () => {
      const { result } = renderHook(() => useScoring());

      const scoreBreakdown = result.current.calculateScore({
        difficulty: Difficulty.EASY,
        timeTaken: 180,
        timeLimit: 180,
        hintsUsed: 0,
        correct: true,
      });

      expect(scoreBreakdown.timeBonus).toBe(0);
    });

    it('should calculate time bonus correctly for MEDIUM difficulty', () => {
      const { result } = renderHook(() => useScoring());

      // MEDIUM: timeLimit=120, timeBonus=100, timeTaken=60, remaining=60
      // timeBonus = floor((60/120) * 100) = floor(50) = 50
      const scoreBreakdown = result.current.calculateScore({
        difficulty: Difficulty.MEDIUM,
        timeTaken: 60,
        timeLimit: 120,
        hintsUsed: 0,
        correct: true,
      });

      expect(scoreBreakdown.timeBonus).toBe(50);
    });

    it('should calculate time bonus correctly for HARD difficulty', () => {
      const { result } = renderHook(() => useScoring());

      // HARD: timeLimit=90, timeBonus=150, timeTaken=30, remaining=60
      // timeBonus = floor((60/90) * 150) = floor(100) = 100
      const scoreBreakdown = result.current.calculateScore({
        difficulty: Difficulty.HARD,
        timeTaken: 30,
        timeLimit: 90,
        hintsUsed: 0,
        correct: true,
      });

      expect(scoreBreakdown.timeBonus).toBe(100);
    });

    it('should calculate time bonus correctly for EXPERT difficulty', () => {
      const { result } = renderHook(() => useScoring());

      // EXPERT: timeLimit=60, timeBonus=250, timeTaken=20, remaining=40
      // timeBonus = floor((40/60) * 250) = floor(166.66) = 166
      const scoreBreakdown = result.current.calculateScore({
        difficulty: Difficulty.EXPERT,
        timeTaken: 20,
        timeLimit: 60,
        hintsUsed: 0,
        correct: true,
      });

      expect(scoreBreakdown.timeBonus).toBe(166);
    });

    it('should fallback to 50 time bonus for undefined difficulty', () => {
      const { result } = renderHook(() => useScoring());

      const scoreBreakdown = result.current.calculateScore({
        difficulty: 'UNKNOWN' as Difficulty,
        timeTaken: 0,
        timeLimit: 120,
        hintsUsed: 0,
        correct: true,
      });

      expect(scoreBreakdown.timeBonus).toBe(50);
    });
  });

  describe('Difficulty multiplier application', () => {
    it('should apply EASY multiplier (1.0x)', () => {
      const { result } = renderHook(() => useScoring());

      const scoreBreakdown = result.current.calculateScore({
        difficulty: Difficulty.EASY,
        timeTaken: 0,
        timeLimit: 180,
        hintsUsed: 0,
        correct: true,
      });

      expect(scoreBreakdown.difficultyMultiplier).toBe(1.0);
      // basePoints=100 + timeBonus=50 = 150 * 1.0 = 150
      expect(scoreBreakdown.finalScore).toBe(150);
    });

    it('should apply MEDIUM multiplier (1.5x)', () => {
      const { result } = renderHook(() => useScoring());

      const scoreBreakdown = result.current.calculateScore({
        difficulty: Difficulty.MEDIUM,
        timeTaken: 0,
        timeLimit: 120,
        hintsUsed: 0,
        correct: true,
      });

      expect(scoreBreakdown.difficultyMultiplier).toBe(1.5);
      // basePoints=200 + timeBonus=100 = 300 * 1.5 = 450
      expect(scoreBreakdown.finalScore).toBe(450);
    });

    it('should apply HARD multiplier (2.0x)', () => {
      const { result } = renderHook(() => useScoring());

      const scoreBreakdown = result.current.calculateScore({
        difficulty: Difficulty.HARD,
        timeTaken: 0,
        timeLimit: 90,
        hintsUsed: 0,
        correct: true,
      });

      expect(scoreBreakdown.difficultyMultiplier).toBe(2.0);
      // basePoints=300 + timeBonus=150 = 450 * 2.0 = 900
      expect(scoreBreakdown.finalScore).toBe(900);
    });

    it('should apply EXPERT multiplier (3.0x)', () => {
      const { result } = renderHook(() => useScoring());

      const scoreBreakdown = result.current.calculateScore({
        difficulty: Difficulty.EXPERT,
        timeTaken: 0,
        timeLimit: 60,
        hintsUsed: 0,
        correct: true,
      });

      expect(scoreBreakdown.difficultyMultiplier).toBe(3.0);
      // basePoints=500 + timeBonus=250 = 750 * 3.0 = 2250
      expect(scoreBreakdown.finalScore).toBe(2250);
    });

    it('should fallback to 1.0 multiplier for undefined difficulty', () => {
      const { result } = renderHook(() => useScoring());

      const scoreBreakdown = result.current.calculateScore({
        difficulty: 'UNKNOWN' as Difficulty,
        timeTaken: 0,
        timeLimit: 120,
        hintsUsed: 0,
        correct: true,
      });

      expect(scoreBreakdown.difficultyMultiplier).toBe(1.0);
    });
  });

  describe('Hint penalty application', () => {
    it('should apply no penalty when no hints used', () => {
      const { result } = renderHook(() => useScoring());

      const scoreBreakdown = result.current.calculateScore({
        difficulty: Difficulty.EASY,
        timeTaken: 0,
        timeLimit: 180,
        hintsUsed: 0,
        correct: true,
      });

      expect(scoreBreakdown.hintPenalty).toBe(0);
    });

    it('should apply penalty for 1 hint used', () => {
      const { result } = renderHook(() => useScoring());

      const scoreBreakdown = result.current.calculateScore({
        difficulty: Difficulty.EASY,
        timeTaken: 0,
        timeLimit: 180,
        hintsUsed: 1,
        correct: true,
      });

      expect(scoreBreakdown.hintPenalty).toBe(10);
      // (100 + 50) * 1.0 - 10 = 140
      expect(scoreBreakdown.finalScore).toBe(140);
    });

    it('should apply penalty for 2 hints used', () => {
      const { result } = renderHook(() => useScoring());

      const scoreBreakdown = result.current.calculateScore({
        difficulty: Difficulty.EASY,
        timeTaken: 0,
        timeLimit: 180,
        hintsUsed: 2,
        correct: true,
      });

      expect(scoreBreakdown.hintPenalty).toBe(20);
      // (100 + 50) * 1.0 - 20 = 130
      expect(scoreBreakdown.finalScore).toBe(130);
    });

    it('should apply penalty for 3 hints used', () => {
      const { result } = renderHook(() => useScoring());

      const scoreBreakdown = result.current.calculateScore({
        difficulty: Difficulty.EASY,
        timeTaken: 0,
        timeLimit: 180,
        hintsUsed: 3,
        correct: true,
      });

      expect(scoreBreakdown.hintPenalty).toBe(30);
      // (100 + 50) * 1.0 - 30 = 120
      expect(scoreBreakdown.finalScore).toBe(120);
    });

    it('should apply cumulative penalty correctly', () => {
      const { result } = renderHook(() => useScoring());

      const scoreBreakdown = result.current.calculateScore({
        difficulty: Difficulty.EXPERT,
        timeTaken: 0,
        timeLimit: 60,
        hintsUsed: 5,
        correct: true,
      });

      expect(scoreBreakdown.hintPenalty).toBe(50);
      // (500 + 250) * 3.0 - 50 = 2250 - 50 = 2200
      expect(scoreBreakdown.finalScore).toBe(2200);
    });
  });

  describe('Final score calculation', () => {
    it('should combine all factors correctly for EASY difficulty', () => {
      const { result } = renderHook(() => useScoring());

      // basePoints=100, timeTaken=90/180 (50% time), timeBonus=25
      // (100 + 25) * 1.0 - 10 = 115
      const scoreBreakdown = result.current.calculateScore({
        difficulty: Difficulty.EASY,
        timeTaken: 90,
        timeLimit: 180,
        hintsUsed: 1,
        correct: true,
      });

      expect(scoreBreakdown.basePoints).toBe(100);
      expect(scoreBreakdown.timeBonus).toBe(25);
      expect(scoreBreakdown.difficultyMultiplier).toBe(1.0);
      expect(scoreBreakdown.hintPenalty).toBe(10);
      expect(scoreBreakdown.finalScore).toBe(115);
    });

    it('should combine all factors correctly for MEDIUM difficulty', () => {
      const { result } = renderHook(() => useScoring());

      // basePoints=200, timeTaken=60/120 (50% time), timeBonus=50
      // (200 + 50) * 1.5 - 20 = 375 - 20 = 355
      const scoreBreakdown = result.current.calculateScore({
        difficulty: Difficulty.MEDIUM,
        timeTaken: 60,
        timeLimit: 120,
        hintsUsed: 2,
        correct: true,
      });

      expect(scoreBreakdown.basePoints).toBe(200);
      expect(scoreBreakdown.timeBonus).toBe(50);
      expect(scoreBreakdown.difficultyMultiplier).toBe(1.5);
      expect(scoreBreakdown.hintPenalty).toBe(20);
      expect(scoreBreakdown.finalScore).toBe(355);
    });

    it('should combine all factors correctly for HARD difficulty', () => {
      const { result } = renderHook(() => useScoring());

      // basePoints=300, timeTaken=45/90 (50% time), timeBonus=75
      // (300 + 75) * 2.0 - 30 = 750 - 30 = 720
      const scoreBreakdown = result.current.calculateScore({
        difficulty: Difficulty.HARD,
        timeTaken: 45,
        timeLimit: 90,
        hintsUsed: 3,
        correct: true,
      });

      expect(scoreBreakdown.basePoints).toBe(300);
      expect(scoreBreakdown.timeBonus).toBe(75);
      expect(scoreBreakdown.difficultyMultiplier).toBe(2.0);
      expect(scoreBreakdown.hintPenalty).toBe(30);
      expect(scoreBreakdown.finalScore).toBe(720);
    });

    it('should combine all factors correctly for EXPERT difficulty', () => {
      const { result } = renderHook(() => useScoring());

      // basePoints=500, timeTaken=30/60 (50% time), timeBonus=125
      // (500 + 125) * 3.0 - 10 = 1875 - 10 = 1865
      const scoreBreakdown = result.current.calculateScore({
        difficulty: Difficulty.EXPERT,
        timeTaken: 30,
        timeLimit: 60,
        hintsUsed: 1,
        correct: true,
      });

      expect(scoreBreakdown.basePoints).toBe(500);
      expect(scoreBreakdown.timeBonus).toBe(125);
      expect(scoreBreakdown.difficultyMultiplier).toBe(3.0);
      expect(scoreBreakdown.hintPenalty).toBe(10);
      expect(scoreBreakdown.finalScore).toBe(1865);
    });

    it('should ensure final score never goes negative', () => {
      const { result } = renderHook(() => useScoring());

      // basePoints=100, no time bonus (took full time), 20 hints used
      // (100 + 0) * 1.0 - 200 = -100, should be capped at 0
      const scoreBreakdown = result.current.calculateScore({
        difficulty: Difficulty.EASY,
        timeTaken: 180,
        timeLimit: 180,
        hintsUsed: 20,
        correct: true,
      });

      expect(scoreBreakdown.finalScore).toBe(0);
      expect(scoreBreakdown.finalScore).toBeGreaterThanOrEqual(0);
    });

    it('should floor the final score to integer', () => {
      const { result } = renderHook(() => useScoring());

      const scoreBreakdown = result.current.calculateScore({
        difficulty: Difficulty.MEDIUM,
        timeTaken: 40,
        timeLimit: 120,
        hintsUsed: 0,
        correct: true,
      });

      expect(Number.isInteger(scoreBreakdown.finalScore)).toBe(true);
    });
  });

  describe('Edge cases', () => {
    it('should handle zero time remaining', () => {
      const { result } = renderHook(() => useScoring());

      const scoreBreakdown = result.current.calculateScore({
        difficulty: Difficulty.EASY,
        timeTaken: 180,
        timeLimit: 180,
        hintsUsed: 0,
        correct: true,
      });

      expect(scoreBreakdown.timeBonus).toBe(0);
      expect(scoreBreakdown.finalScore).toBe(100);
    });

    it('should handle maximum hints scenario', () => {
      const { result } = renderHook(() => useScoring());

      const scoreBreakdown = result.current.calculateScore({
        difficulty: Difficulty.EASY,
        timeTaken: 0,
        timeLimit: 180,
        hintsUsed: 100,
        correct: true,
      });

      expect(scoreBreakdown.hintPenalty).toBe(1000);
      expect(scoreBreakdown.finalScore).toBe(0);
    });

    it('should handle negative time remaining gracefully', () => {
      const { result } = renderHook(() => useScoring());

      const scoreBreakdown = result.current.calculateScore({
        difficulty: Difficulty.EASY,
        timeTaken: 200,
        timeLimit: 180,
        hintsUsed: 0,
        correct: true,
      });

      expect(scoreBreakdown.timeBonus).toBe(0);
      expect(scoreBreakdown.finalScore).toBeGreaterThanOrEqual(0);
    });

    it('should handle missing difficulty settings gracefully', () => {
      const { result } = renderHook(() => useScoring());

      const scoreBreakdown = result.current.calculateScore({
        difficulty: 'INVALID' as Difficulty,
        timeTaken: 60,
        timeLimit: 120,
        hintsUsed: 0,
        correct: true,
      });

      expect(scoreBreakdown.basePoints).toBe(100);
      expect(scoreBreakdown.difficultyMultiplier).toBe(1.0);
      expect(scoreBreakdown.finalScore).toBeGreaterThanOrEqual(0);
    });

    it('should handle perfect score scenario', () => {
      const { result } = renderHook(() => useScoring());

      // Instant answer, hardest difficulty, no hints
      const scoreBreakdown = result.current.calculateScore({
        difficulty: Difficulty.EXPERT,
        timeTaken: 0,
        timeLimit: 60,
        hintsUsed: 0,
        correct: true,
      });

      expect(scoreBreakdown.basePoints).toBe(500);
      expect(scoreBreakdown.timeBonus).toBe(250);
      expect(scoreBreakdown.difficultyMultiplier).toBe(3.0);
      expect(scoreBreakdown.hintPenalty).toBe(0);
      expect(scoreBreakdown.finalScore).toBe(2250);
    });

    it('should handle worst score scenario (still correct)', () => {
      const { result } = renderHook(() => useScoring());

      // Timeout, easiest difficulty, max hints
      const scoreBreakdown = result.current.calculateScore({
        difficulty: Difficulty.EASY,
        timeTaken: 180,
        timeLimit: 180,
        hintsUsed: 20,
        correct: true,
      });

      expect(scoreBreakdown.finalScore).toBe(0);
    });
  });

  describe('Test all difficulty levels systematically', () => {
    it('should handle all difficulties with consistent logic', () => {
      const { result } = renderHook(() => useScoring());

      const difficulties = [
        { difficulty: Difficulty.EASY, expectedBase: 100, expectedMultiplier: 1.0 },
        { difficulty: Difficulty.MEDIUM, expectedBase: 200, expectedMultiplier: 1.5 },
        { difficulty: Difficulty.HARD, expectedBase: 300, expectedMultiplier: 2.0 },
        { difficulty: Difficulty.EXPERT, expectedBase: 500, expectedMultiplier: 3.0 },
      ];

      difficulties.forEach(({ difficulty, expectedBase, expectedMultiplier }) => {
        const scoreBreakdown = result.current.calculateScore({
          difficulty,
          timeTaken: 0,
          timeLimit: 180,
          hintsUsed: 0,
          correct: true,
        });

        expect(scoreBreakdown.basePoints).toBe(expectedBase);
        expect(scoreBreakdown.difficultyMultiplier).toBe(expectedMultiplier);
        expect(scoreBreakdown.finalScore).toBeGreaterThan(0);
      });
    });
  });
});
