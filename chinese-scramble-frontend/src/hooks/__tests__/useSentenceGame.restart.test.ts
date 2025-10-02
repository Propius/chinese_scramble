/**
 * Comprehensive restart flow tests for useSentenceGame hook
 * Tests feature flag ON/OFF scenarios with localStorage behavior
 */

import { renderHook, act, waitFor } from '@testing-library/react';
import { useSentenceGame } from '../useSentenceGame';
import sentenceService from '../../services/sentenceService';
import { usernameUtils } from '../../utils/usernameUtils';
import questionTracker from '../../utils/questionTracker';
import { Difficulty } from '../../constants/difficulties';
import { DEFAULT_FEATURE_FLAGS } from '../../constants/game.constants';

jest.mock('../../services/sentenceService');
jest.mock('../../utils/usernameUtils');
jest.mock('../../utils/questionTracker');

const mockSentenceService = sentenceService as jest.Mocked<typeof sentenceService>;
const mockUsernameUtils = usernameUtils as jest.Mocked<typeof usernameUtils>;
const mockQuestionTracker = questionTracker as jest.Mocked<typeof questionTracker>;

describe('useSentenceGame - Restart Flow', () => {
  const mockQuestion = {
    id: '1',
    text: '我每天早上都去公园跑步',
    pinyin: 'wǒ měi tiān zǎo shang dōu qù gōng yuán pǎo bù',
    translation: 'I go running in the park every morning',
    difficulty: Difficulty.EASY,
    words: ['我', '每天', '早上', '都', '去', '公园', '跑步'],
    wordCount: 7,
    grammarPattern: 'Subject + Time + Adverb + Verb + Location + Verb',
    grammarExplanation: '主语 + 时间状语 + 副词 + 动词 + 地点状语 + 动词',
    topic: '日常生活',
    vocabularyLevel: 'HSK1',
    scrambledWords: ['跑步', '公园', '我', '去', '都', '早上', '每天'],
    correctAnswer: ['我', '每天', '早上', '都', '去', '公园', '跑步'],
  };

  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
    mockUsernameUtils.getUsername.mockReturnValue('testPlayer');
    mockQuestionTracker.getSeenQuestions.mockReturnValue(new Set<string>());
    mockQuestionTracker.markQuestionAsSeen.mockReturnValue(true);
    mockQuestionTracker.resetSeenQuestions.mockReturnValue(true);
    mockSentenceService.startGame.mockResolvedValue(mockQuestion);
  });

  describe('Feature Flag OFF - NO_REPEAT_QUESTIONS disabled', () => {
    beforeEach(() => {
      (DEFAULT_FEATURE_FLAGS as any).ENABLE_NO_REPEAT_QUESTIONS = false;
    });

    it('should clear localStorage when starting game with feature flag OFF', async () => {
      const { result } = renderHook(() => useSentenceGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      await waitFor(() => expect(result.current.loading).toBe(false));

      expect(mockQuestionTracker.resetSeenQuestions).toHaveBeenCalledWith('SENTENCE', Difficulty.EASY);
      expect(mockQuestionTracker.getSeenQuestions).not.toHaveBeenCalled();
      expect(mockQuestionTracker.markQuestionAsSeen).not.toHaveBeenCalled();

      expect(mockSentenceService.startGame).toHaveBeenCalledWith(
        Difficulty.EASY,
        'testPlayer',
        undefined
      );
    });

    it('should NOT send excludedIds when feature flag is OFF', async () => {
      const { result } = renderHook(() => useSentenceGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      await waitFor(() => expect(result.current.question).toEqual(mockQuestion));

      const callArgs = mockSentenceService.startGame.mock.calls[0];
      expect(callArgs[2]).toBeUndefined();
    });

    it('should clear stale localStorage data on restart when flag is OFF', async () => {
      const staleSeenQuestions = new Set(['1', '2', '3', '4']);
      mockQuestionTracker.getSeenQuestions.mockReturnValue(staleSeenQuestions);

      const { result } = renderHook(() => useSentenceGame());

      await act(async () => {
        await result.current.startGame(Difficulty.MEDIUM);
      });

      await waitFor(() => expect(result.current.loading).toBe(false));

      expect(mockQuestionTracker.resetSeenQuestions).toHaveBeenCalledWith('SENTENCE', Difficulty.MEDIUM);
      expect(mockQuestionTracker.getSeenQuestions).not.toHaveBeenCalled();
    });

    it('should handle multiple restarts correctly with flag OFF', async () => {
      const { result } = renderHook(() => useSentenceGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      await waitFor(() => expect(result.current.question).toBeTruthy());

      act(() => {
        result.current.reset();
      });

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      await waitFor(() => expect(result.current.question).toBeTruthy());

      expect(mockQuestionTracker.resetSeenQuestions).toHaveBeenCalledTimes(2);

      mockSentenceService.startGame.mock.calls.forEach(call => {
        expect(call[2]).toBeUndefined();
      });
    });
  });

  describe('Feature Flag ON - NO_REPEAT_QUESTIONS enabled', () => {
    beforeEach(() => {
      (DEFAULT_FEATURE_FLAGS as any).ENABLE_NO_REPEAT_QUESTIONS = true;
    });

    it('should send excludedIds when feature flag is ON', async () => {
      const seenQuestions = new Set(['1', '2', '3']);
      mockQuestionTracker.getSeenQuestions.mockReturnValue(seenQuestions);

      const { result } = renderHook(() => useSentenceGame());

      await act(async () => {
        await result.current.startGame(Difficulty.HARD);
      });

      await waitFor(() => expect(result.current.question).toBeTruthy());

      expect(mockQuestionTracker.getSeenQuestions).toHaveBeenCalledWith('SENTENCE', Difficulty.HARD);
      expect(mockQuestionTracker.resetSeenQuestions).not.toHaveBeenCalled();

      expect(mockSentenceService.startGame).toHaveBeenCalledWith(
        Difficulty.HARD,
        'testPlayer',
        ['1', '2', '3']
      );

      expect(mockQuestionTracker.markQuestionAsSeen).toHaveBeenCalledWith('SENTENCE', Difficulty.HARD, '1');
    });

    it('should NOT clear localStorage when feature flag is ON', async () => {
      const { result } = renderHook(() => useSentenceGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EXPERT);
      });

      await waitFor(() => expect(result.current.loading).toBe(false));

      expect(mockQuestionTracker.resetSeenQuestions).not.toHaveBeenCalled();
      expect(mockQuestionTracker.getSeenQuestions).toHaveBeenCalled();
    });

    it('should handle empty seen questions when flag is ON', async () => {
      mockQuestionTracker.getSeenQuestions.mockReturnValue(new Set<string>());

      const { result } = renderHook(() => useSentenceGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      await waitFor(() => expect(result.current.question).toBeTruthy());

      expect(mockSentenceService.startGame).toHaveBeenCalledWith(
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
      mockSentenceService.startGame.mockRejectedValue(error);

      const { result } = renderHook(() => useSentenceGame());

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
      expect(mockQuestionTracker.resetSeenQuestions).toHaveBeenCalled();
    });

    it('should re-throw error for page component to handle', async () => {
      const error = new Error('All questions completed');
      mockSentenceService.startGame.mockRejectedValue(error);

      const { result } = renderHook(() => useSentenceGame());

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
      const { result } = renderHook(() => useSentenceGame());

      await act(async () => {
        await result.current.startGame(difficulty);
      });

      await waitFor(() => expect(result.current.loading).toBe(false));

      expect(mockQuestionTracker.resetSeenQuestions).toHaveBeenCalledWith('SENTENCE', difficulty);
    });
  });

  describe('Username Handling', () => {
    beforeEach(() => {
      (DEFAULT_FEATURE_FLAGS as any).ENABLE_NO_REPEAT_QUESTIONS = false;
    });

    it('should handle undefined username', async () => {
      mockUsernameUtils.getUsername.mockReturnValue(null);

      const { result } = renderHook(() => useSentenceGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      await waitFor(() => expect(result.current.loading).toBe(false));

      expect(mockSentenceService.startGame).toHaveBeenCalledWith(
        Difficulty.EASY,
        undefined,
        undefined
      );
    });

    it('should send playerId when username is set', async () => {
      mockUsernameUtils.getUsername.mockReturnValue('player456');

      const { result } = renderHook(() => useSentenceGame());

      await act(async () => {
        await result.current.startGame(Difficulty.MEDIUM);
      });

      await waitFor(() => expect(result.current.loading).toBe(false));

      expect(mockSentenceService.startGame).toHaveBeenCalledWith(
        Difficulty.MEDIUM,
        'player456',
        undefined
      );
    });
  });
});
