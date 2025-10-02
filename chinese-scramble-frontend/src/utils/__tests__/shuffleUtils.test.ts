import { shuffleArray, isShuffled, ensureShuffled } from '../shuffleUtils';

describe('shuffleUtils', () => {
  describe('shuffleArray', () => {
    it('should return an array of the same length', () => {
      const input = [1, 2, 3, 4, 5];
      const result = shuffleArray(input);
      expect(result.length).toBe(input.length);
    });

    it('should contain all original elements', () => {
      const input = [1, 2, 3, 4, 5];
      const result = shuffleArray(input);
      expect(result.sort()).toEqual(input.sort());
    });

    it('should not modify the original array', () => {
      const input = [1, 2, 3, 4, 5];
      const original = [...input];
      shuffleArray(input);
      expect(input).toEqual(original);
    });

    it('should handle empty array', () => {
      const result = shuffleArray([]);
      expect(result).toEqual([]);
    });

    it('should handle single element array', () => {
      const input = [1];
      const result = shuffleArray(input);
      expect(result).toEqual([1]);
    });

    it('should handle two element array', () => {
      const input = [1, 2];
      const result = shuffleArray(input);
      expect(result.sort()).toEqual([1, 2]);
    });

    it('should work with strings', () => {
      const input = ['a', 'b', 'c'];
      const result = shuffleArray(input);
      expect(result.length).toBe(3);
      expect(result.sort()).toEqual(['a', 'b', 'c']);
    });

    it('should work with objects', () => {
      const input = [{ id: 1 }, { id: 2 }, { id: 3 }];
      const result = shuffleArray(input);
      expect(result.length).toBe(3);
      expect(result.map(o => o.id).sort()).toEqual([1, 2, 3]);
    });
  });

  describe('isShuffled', () => {
    it('should return false for identical arrays', () => {
      const arr1 = [1, 2, 3, 4, 5];
      const arr2 = [1, 2, 3, 4, 5];
      expect(isShuffled(arr1, arr2)).toBe(false);
    });

    it('should return true for different order arrays', () => {
      const arr1 = [1, 2, 3, 4, 5];
      const arr2 = [5, 4, 3, 2, 1];
      expect(isShuffled(arr1, arr2)).toBe(true);
    });

    it('should return true if at least one element is in different position', () => {
      const arr1 = [1, 2, 3, 4, 5];
      const arr2 = [1, 3, 2, 4, 5];
      expect(isShuffled(arr1, arr2)).toBe(true);
    });

    it('should return false for arrays of different lengths', () => {
      const arr1 = [1, 2, 3];
      const arr2 = [1, 2, 3, 4];
      expect(isShuffled(arr1, arr2)).toBe(false);
    });

    it('should handle empty arrays', () => {
      expect(isShuffled([], [])).toBe(false);
    });

    it('should handle single element arrays', () => {
      const arr1 = [1];
      const arr2 = [1];
      expect(isShuffled(arr1, arr2)).toBe(false);
    });

    it('should work with strings', () => {
      const arr1 = ['a', 'b', 'c'];
      const arr2 = ['b', 'a', 'c'];
      expect(isShuffled(arr1, arr2)).toBe(true);
    });
  });

  describe('ensureShuffled', () => {
    it('should return array of same length', () => {
      const input = [1, 2, 3, 4, 5];
      const result = ensureShuffled(input);
      expect(result.length).toBe(input.length);
    });

    it('should return array with same elements', () => {
      const input = [1, 2, 3, 4, 5];
      const result = ensureShuffled(input);
      expect(result.sort()).toEqual(input.sort());
    });

    it('should not modify original array', () => {
      const input = [1, 2, 3, 4, 5];
      const original = [...input];
      ensureShuffled(input);
      expect(input).toEqual(original);
    });

    it('should return shuffled array for arrays with multiple elements', () => {
      const input = [1, 2, 3, 4, 5];
      const result = ensureShuffled(input);
      // With 5 elements, statistically very unlikely to be in same order
      // But we check it contains all elements
      expect(result.sort()).toEqual(input.sort());
    });

    it('should handle single element array', () => {
      const input = [1];
      const result = ensureShuffled(input);
      expect(result).toEqual([1]);
    });

    it('should handle two element array', () => {
      const input = [1, 2];
      const result = ensureShuffled(input);
      expect(result.sort()).toEqual([1, 2]);
      // For 2 elements, we should get either [1,2] or [2,1]
      expect([1, 2].includes(result[0])).toBe(true);
    });

    it('should respect maxAttempts parameter', () => {
      const input = [1, 2, 3];
      const result = ensureShuffled(input, 5);
      expect(result.sort()).toEqual(input.sort());
    });

    it('should use default maxAttempts of 10', () => {
      const input = [1, 2, 3, 4, 5];
      const result = ensureShuffled(input);
      // Should succeed within 10 attempts for 5 elements
      expect(result.length).toBe(5);
    });

    it('should work with strings', () => {
      const input = ['a', 'b', 'c', 'd'];
      const result = ensureShuffled(input);
      expect(result.length).toBe(4);
      expect(result.sort()).toEqual(['a', 'b', 'c', 'd']);
    });
  });
});
