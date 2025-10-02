/**
 * Comprehensive restart flow tests for useIdiomGame hook
 * Tests feature flag ON/OFF scenarios with localStorage behavior
 */

import { renderHook, act, waitFor } from '@testing-library/react';
import { useIdiomGame } from '../useIdiomGame';
import idiomService from '../../services/idiomService';
import { usernameUtils } from '../../utils/usernameUtils';
import questionTracker from '../../utils/questionTracker';
import { Difficulty } from '../../constants/difficulties';
import { DEFAULT_FEATURE_FLAGS } from '../../constants/game.constants';

jest.mock('../../services/idiomService');
jest.mock('../../utils/usernameUtils');
jest.mock('../../utils/questionTracker');

const mockIdiomService = idiomService as jest.Mocked<typeof idiomService>;
const mockUsernameUtils = usernameUtils as jest.Mocked<typeof usernameUtils>;
const mockQuestionTracker = questionTracker as jest.Mocked<typeof questionTracker>;

describe('useIdiomGame - Restart Flow', () => {
  const mockQuestion = {
    id: '1',
    text: '一马当先',
    pinyin: 'yī mǎ dāng xiān',
    meaning: '比喻走在前面，起带头作用',
    translation: 'To take the lead',
    difficulty: Difficulty.EASY,
    category: '动物',
    usageExample: '在这次比赛中，他一马当先。',
    exampleTranslation: 'In this competition, he took the lead.',
    characterCount: 4,
    origin: '明·施耐庵《水浒传》',
    scrambledCharacters: ['马', '一', '先', '当'],
    correctAnswer: '一马当先',
  };

  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
    mockUsernameUtils.getUsername.mockReturnValue('testPlayer');
    mockQuestionTracker.getSeenQuestions.mockReturnValue(new Set<string>());
    mockQuestionTracker.markQuestionAsSeen.mockReturnValue(true);
    mockQuestionTracker.resetSeenQuestions.mockReturnValue(true);
    mockIdiomService.startGame.mockResolvedValue(mockQuestion);
  });

  describe('Feature Flag OFF - NO_REPEAT_QUESTIONS disabled', () => {
    beforeEach(() => {
      // Ensure feature flag is OFF
      (DEFAULT_FEATURE_FLAGS as any).ENABLE_NO_REPEAT_QUESTIONS = false;
    });

    it('should clear localStorage when starting game with feature flag OFF', async () => {
      const { result } = renderHook(() => useIdiomGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      await waitFor(() => expect(result.current.loading).toBe(false));

      // Should clear localStorage
      expect(mockQuestionTracker.resetSeenQuestions).toHaveBeenCalledWith('IDIOM', Difficulty.EASY);

      // Should NOT call getSeenQuestions because flag is OFF
      expect(mockQuestionTracker.getSeenQuestions).not.toHaveBeenCalled();

      // Should NOT mark question as seen
      expect(mockQuestionTracker.markQuestionAsSeen).not.toHaveBeenCalled();

      // Should call startGame with undefined excludedIds
      expect(mockIdiomService.startGame).toHaveBeenCalledWith(
        Difficulty.EASY,
        'testPlayer',
        undefined
      );
    });

    it('should NOT send excludedIds when feature flag is OFF', async () => {
      const { result } = renderHook(() => useIdiomGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      await waitFor(() => expect(result.current.question).toEqual(mockQuestion));

      // Verify excludedIds was undefined (not sent)
      const callArgs = mockIdiomService.startGame.mock.calls[0];
      expect(callArgs[2]).toBeUndefined();
    });

    it('should clear stale localStorage data on restart when flag is OFF', async () => {
      // Simulate stale data in localStorage from when feature was ON
      const staleSeenQuestions = new Set(['1', '2', '3']);
      mockQuestionTracker.getSeenQuestions.mockReturnValue(staleSeenQuestions);

      const { result } = renderHook(() => useIdiomGame());

      await act(async () => {
        await result.current.startGame(Difficulty.MEDIUM);
      });

      await waitFor(() => expect(result.current.loading).toBe(false));

      // Should clear stale data
      expect(mockQuestionTracker.resetSeenQuestions).toHaveBeenCalledWith('IDIOM', Difficulty.MEDIUM);

      // Should NOT read stale data
      expect(mockQuestionTracker.getSeenQuestions).not.toHaveBeenCalled();
    });

    it('should handle multiple restarts correctly with flag OFF', async () => {
      const { result } = renderHook(() => useIdiomGame());

      // First game
      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      await waitFor(() => expect(result.current.question).toBeTruthy());

      // Reset and restart
      act(() => {
        result.current.reset();
      });

      // Second game
      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      await waitFor(() => expect(result.current.question).toBeTruthy());

      // Should clear localStorage both times
      expect(mockQuestionTracker.resetSeenQuestions).toHaveBeenCalledTimes(2);

      // Should never send excludedIds
      mockIdiomService.startGame.mock.calls.forEach(call => {
        expect(call[2]).toBeUndefined();
      });
    });
  });

  describe('Feature Flag ON - NO_REPEAT_QUESTIONS enabled', () => {
    beforeEach(() => {
      // Enable feature flag
      (DEFAULT_FEATURE_FLAGS as any).ENABLE_NO_REPEAT_QUESTIONS = true;
    });

    it('should send excludedIds when feature flag is ON', async () => {
      const seenQuestions = new Set(['1', '2', '3']);
      mockQuestionTracker.getSeenQuestions.mockReturnValue(seenQuestions);

      const { result } = renderHook(() => useIdiomGame());

      await act(async () => {
        await result.current.startGame(Difficulty.HARD);
      });

      await waitFor(() => expect(result.current.question).toBeTruthy());

      // Should read seen questions
      expect(mockQuestionTracker.getSeenQuestions).toHaveBeenCalledWith('IDIOM', Difficulty.HARD);

      // Should NOT clear localStorage
      expect(mockQuestionTracker.resetSeenQuestions).not.toHaveBeenCalled();

      // Should send excludedIds
      expect(mockIdiomService.startGame).toHaveBeenCalledWith(
        Difficulty.HARD,
        'testPlayer',
        ['1', '2', '3']
      );

      // Should mark new question as seen
      expect(mockQuestionTracker.markQuestionAsSeen).toHaveBeenCalledWith('IDIOM', Difficulty.HARD, '1');
    });

    it('should NOT clear localStorage when feature flag is ON', async () => {
      const { result } = renderHook(() => useIdiomGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EXPERT);
      });

      await waitFor(() => expect(result.current.loading).toBe(false));

      // Should NOT clear localStorage
      expect(mockQuestionTracker.resetSeenQuestions).not.toHaveBeenCalled();

      // Should read seen questions
      expect(mockQuestionTracker.getSeenQuestions).toHaveBeenCalled();
    });

    it('should handle empty seen questions when flag is ON', async () => {
      mockQuestionTracker.getSeenQuestions.mockReturnValue(new Set<string>());

      const { result } = renderHook(() => useIdiomGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      await waitFor(() => expect(result.current.question).toBeTruthy());

      // Should send empty array (which service layer will not append to URL)
      expect(mockIdiomService.startGame).toHaveBeenCalledWith(
        Difficulty.EASY,
        'testPlayer',
        []
      );
    });
  });

  describe('Error Handling', () => {
    beforeEach(() => {
      (DEFAULT_FEATURE_FLAGS as any).ENABLE_NO_REPEAT_QUESTIONS = false;
    });

    it('should handle errors during game start with flag OFF', async () => {
      const error = new Error('Network error');
      mockIdiomService.startGame.mockRejectedValue(error);

      const { result } = renderHook(() => useIdiomGame());

      await act(async () => {
        try {
          await result.current.startGame(Difficulty.EASY);
        } catch (err) {
          expect(err).toBe(error);
        }
      });

      await waitFor(() => expect(result.current.loading).toBe(false));

      expect(result.current.error).toBe('加载失败，请稍后重试');
      expect(result.current.question).toBeNull();

      // Should still have cleared localStorage
      expect(mockQuestionTracker.resetSeenQuestions).toHaveBeenCalled();
    });

    it('should re-throw error for page component to handle', async () => {
      const error = new Error('All questions completed');
      mockIdiomService.startGame.mockRejectedValue(error);

      const { result } = renderHook(() => useIdiomGame());

      let caughtError;
      await act(async () => {
        try {
          await result.current.startGame(Difficulty.EASY);
        } catch (err) {
          caughtError = err;
        }
      });

      expect(caughtError).toBe(error);
    });
  });

  describe('Different Difficulty Levels', () => {
    beforeEach(() => {
      (DEFAULT_FEATURE_FLAGS as any).ENABLE_NO_REPEAT_QUESTIONS = false;
    });

    it.each([
      [Difficulty.EASY, 'EASY'],
      [Difficulty.MEDIUM, 'MEDIUM'],
      [Difficulty.HARD, 'HARD'],
      [Difficulty.EXPERT, 'EXPERT'],
    ])('should clear localStorage for %s difficulty when flag is OFF', async (difficulty, label) => {
      const { result } = renderHook(() => useIdiomGame());

      await act(async () => {
        await result.current.startGame(difficulty);
      });

      await waitFor(() => expect(result.current.loading).toBe(false));

      expect(mockQuestionTracker.resetSeenQuestions).toHaveBeenCalledWith('IDIOM', difficulty);
    });
  });

  describe('Username Handling', () => {
    beforeEach(() => {
      (DEFAULT_FEATURE_FLAGS as any).ENABLE_NO_REPEAT_QUESTIONS = false;
    });

    it('should handle undefined username', async () => {
      mockUsernameUtils.getUsername.mockReturnValue(null);

      const { result } = renderHook(() => useIdiomGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      await waitFor(() => expect(result.current.loading).toBe(false));

      // Should call with undefined playerId
      expect(mockIdiomService.startGame).toHaveBeenCalledWith(
        Difficulty.EASY,
        undefined,
        undefined
      );
    });

    it('should send playerId when username is set', async () => {
      mockUsernameUtils.getUsername.mockReturnValue('player123');

      const { result } = renderHook(() => useIdiomGame());

      await act(async () => {
        await result.current.startGame(Difficulty.MEDIUM);
      });

      await waitFor(() => expect(result.current.loading).toBe(false));

      expect(mockIdiomService.startGame).toHaveBeenCalledWith(
        Difficulty.MEDIUM,
        'player123',
        undefined
      );
    });
  });
});
