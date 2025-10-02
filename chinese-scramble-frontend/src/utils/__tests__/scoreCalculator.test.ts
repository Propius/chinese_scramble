import {
  calculateScore,
  calculateHintPenalty,
  calculateAccuracy,
  calculateTimeTaken,
  formatScore,
  formatTime,
} from '../scoreCalculator';
import { DifficultyLevel } from '../../types/game.types';

describe('scoreCalculator', () => {
  describe('calculateScore', () => {
    it('should return zero score for incorrect answer', () => {
      const result = calculateScore('EASY', 100, 0, false);
      expect(result.totalScore).toBe(0);
      expect(result.basePoints).toBe(0);
      expect(result.timeBonus).toBe(0);
      expect(result.hintPenalty).toBe(0);
    });

    it('should calculate correct score for EASY difficulty with no hints', () => {
      const result = calculateScore('EASY', 100, 0, true);
      expect(result.basePoints).toBeGreaterThan(0);
      expect(result.timeBonus).toBeGreaterThan(0);
      expect(result.hintPenalty).toBe(0);
      expect(result.totalScore).toBeGreaterThan(0);
    });

    it('should calculate correct score for MEDIUM difficulty', () => {
      const result = calculateScore('MEDIUM', 100, 0, true);
      expect(result.basePoints).toBeGreaterThan(0);
      expect(result.difficultyMultiplier).toBeGreaterThan(1);
      expect(result.totalScore).toBeGreaterThan(0);
    });

    it('should calculate correct score for HARD difficulty', () => {
      const result = calculateScore('HARD', 100, 0, true);
      expect(result.basePoints).toBeGreaterThan(0);
      expect(result.difficultyMultiplier).toBeGreaterThan(1);
      expect(result.totalScore).toBeGreaterThan(0);
    });

    it('should calculate correct score for EXPERT difficulty', () => {
      const result = calculateScore('EXPERT', 100, 0, true);
      expect(result.basePoints).toBeGreaterThan(0);
      expect(result.difficultyMultiplier).toBeGreaterThan(1);
      expect(result.totalScore).toBeGreaterThan(0);
    });

    it('should apply time bonus correctly', () => {
      const result1 = calculateScore('EASY', 100, 0, true);
      const result2 = calculateScore('EASY', 50, 0, true);
      expect(result1.timeBonus).toBeGreaterThan(result2.timeBonus);
      expect(result1.totalScore).toBeGreaterThan(result2.totalScore);
    });

    it('should apply hint penalty correctly', () => {
      const result1 = calculateScore('EASY', 100, 0, true);
      const result2 = calculateScore('EASY', 100, 1, true);
      expect(result2.hintPenalty).toBeGreaterThan(0);
      expect(result2.totalScore).toBeLessThan(result1.totalScore);
    });

    it('should never return negative score', () => {
      const result = calculateScore('EASY', 1, 3, true);
      expect(result.totalScore).toBeGreaterThanOrEqual(0);
    });

    it('should handle zero time remaining', () => {
      const result = calculateScore('EASY', 0, 0, true);
      expect(result.timeBonus).toBe(0);
      expect(result.totalScore).toBeGreaterThanOrEqual(0);
    });

    it('should return integer score', () => {
      const result = calculateScore('EASY', 100, 0, true);
      expect(Number.isInteger(result.totalScore)).toBe(true);
    });
  });

  describe('calculateHintPenalty', () => {
    it('should return 0 for no hints', () => {
      expect(calculateHintPenalty(0)).toBe(0);
    });

    it('should calculate penalty for 1 hint', () => {
      const penalty = calculateHintPenalty(1);
      expect(penalty).toBeGreaterThan(0);
    });

    it('should calculate penalty for 2 hints', () => {
      const penalty = calculateHintPenalty(2);
      expect(penalty).toBeGreaterThan(calculateHintPenalty(1));
    });

    it('should calculate penalty for 3 hints', () => {
      const penalty = calculateHintPenalty(3);
      expect(penalty).toBeGreaterThan(calculateHintPenalty(2));
    });

    it('should cap penalty at 3 hints', () => {
      const penalty3 = calculateHintPenalty(3);
      const penalty4 = calculateHintPenalty(4);
      expect(penalty3).toBe(penalty4);
    });

    it('should handle large hint numbers', () => {
      expect(calculateHintPenalty(100)).toBeGreaterThan(0);
    });
  });

  describe('calculateAccuracy', () => {
    it('should return 0 for no questions', () => {
      expect(calculateAccuracy(0, 0)).toBe(0);
    });

    it('should return 100 for all correct', () => {
      expect(calculateAccuracy(10, 10)).toBe(100);
    });

    it('should return 0 for all incorrect', () => {
      expect(calculateAccuracy(0, 10)).toBe(0);
    });

    it('should calculate 50% accuracy', () => {
      expect(calculateAccuracy(5, 10)).toBe(50);
    });

    it('should round to nearest integer', () => {
      expect(calculateAccuracy(1, 3)).toBe(33);
      expect(calculateAccuracy(2, 3)).toBe(67);
    });

    it('should handle single question', () => {
      expect(calculateAccuracy(1, 1)).toBe(100);
      expect(calculateAccuracy(0, 1)).toBe(0);
    });

    it('should return percentage between 0 and 100', () => {
      const accuracy = calculateAccuracy(7, 13);
      expect(accuracy).toBeGreaterThanOrEqual(0);
      expect(accuracy).toBeLessThanOrEqual(100);
    });
  });

  describe('calculateTimeTaken', () => {
    it('should calculate time taken correctly', () => {
      expect(calculateTimeTaken(50, 100)).toBe(50);
    });

    it('should handle zero time remaining', () => {
      expect(calculateTimeTaken(0, 100)).toBe(100);
    });

    it('should handle full time remaining', () => {
      expect(calculateTimeTaken(100, 100)).toBe(0);
    });

    it('should return non-negative value', () => {
      expect(calculateTimeTaken(10, 100)).toBeGreaterThanOrEqual(0);
    });
  });

  describe('formatScore', () => {
    it('should format score as string', () => {
      const formatted = formatScore(1000);
      expect(typeof formatted).toBe('string');
    });

    it('should handle zero score', () => {
      const formatted = formatScore(0);
      expect(formatted).toBeTruthy();
    });

    it('should handle large scores', () => {
      const formatted = formatScore(999999);
      expect(typeof formatted).toBe('string');
      expect(formatted.length).toBeGreaterThan(0);
    });

    it('should handle negative scores', () => {
      const formatted = formatScore(-100);
      expect(typeof formatted).toBe('string');
    });
  });

  describe('formatTime', () => {
    it('should format 0 seconds correctly', () => {
      expect(formatTime(0)).toBe('0:00');
    });

    it('should format seconds less than a minute', () => {
      expect(formatTime(30)).toBe('0:30');
      expect(formatTime(59)).toBe('0:59');
    });

    it('should format exactly 1 minute', () => {
      expect(formatTime(60)).toBe('1:00');
    });

    it('should format minutes and seconds', () => {
      expect(formatTime(90)).toBe('1:30');
      expect(formatTime(125)).toBe('2:05');
    });

    it('should pad seconds with leading zero', () => {
      expect(formatTime(61)).toBe('1:01');
      expect(formatTime(605)).toBe('10:05');
    });

    it('should handle large time values', () => {
      expect(formatTime(3600)).toBe('60:00');
      expect(formatTime(3661)).toBe('61:01');
    });

    it('should return string format', () => {
      const formatted = formatTime(123);
      expect(typeof formatted).toBe('string');
      expect(formatted).toContain(':');
    });
  });
});
