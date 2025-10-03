import { renderHook, act, waitFor } from '@testing-library/react';
import { useIdiomGame } from '../useIdiomGame';
import { Difficulty } from '../../constants/difficulties';
import idiomService from '../../services/idiomService';
import { usernameUtils } from '../../utils/usernameUtils';
import questionTracker from '../../utils/questionTracker';

// Mock dependencies
jest.mock('../../services/idiomService');
jest.mock('../../utils/usernameUtils');
jest.mock('../../utils/questionTracker');

const mockedIdiomService = idiomService as jest.Mocked<typeof idiomService>;
const mockedUsernameUtils = usernameUtils as jest.Mocked<typeof usernameUtils>;
const mockedQuestionTracker = questionTracker as jest.Mocked<typeof questionTracker>;

describe('useIdiomGame', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockedUsernameUtils.getUsername.mockReturnValue('testUser');
    // Mock questionTracker.getSeenQuestions to return empty Set by default
    mockedQuestionTracker.getSeenQuestions.mockReturnValue(new Set<string>());
  });

  describe('Initial state', () => {
    it('should have correct initial state', () => {
      const { result } = renderHook(() => useIdiomGame());

      expect(result.current.question).toBeNull();
      expect(result.current.loading).toBe(false);
      expect(result.current.error).toBeNull();
      expect(result.current.userAnswer).toEqual([]);
      expect(result.current.hintsUsed).toBe(0);
      expect(result.current.timeTaken).toBe(0);
      expect(result.current.validationResult).toBeNull();
    });

    it('should have all required methods', () => {
      const { result } = renderHook(() => useIdiomGame());

      expect(typeof result.current.startGame).toBe('function');
      expect(typeof result.current.submitAnswer).toBe('function');
      expect(typeof result.current.getHint).toBe('function');
      expect(typeof result.current.setUserAnswer).toBe('function');
      expect(typeof result.current.reset).toBe('function');
    });
  });

  describe('startGame', () => {
    const mockQuestion = {
      id: 1,
      idiom: '画龙点睛',
      scrambledCharacters: ['睛', '画', '点', '龙'],
      difficulty: Difficulty.EASY,
    };

    it('should start game successfully', async () => {
      mockedIdiomService.startGame.mockResolvedValue(mockQuestion);

      const { result } = renderHook(() => useIdiomGame());

      expect(result.current.loading).toBe(false);

      act(() => {
        result.current.startGame(Difficulty.EASY);
      });

      expect(result.current.loading).toBe(true);

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.question).toEqual(mockQuestion);
      expect(result.current.error).toBeNull();
      expect(result.current.userAnswer).toEqual([]);
      expect(result.current.hintsUsed).toBe(0);
      expect(mockedIdiomService.startGame).toHaveBeenCalledWith(Difficulty.EASY, 'testUser', []);
    });

    it('should handle start game with Guest user', async () => {
      mockedUsernameUtils.getUsername.mockReturnValue(null);
      mockedIdiomService.startGame.mockResolvedValue(mockQuestion);

      const { result } = renderHook(() => useIdiomGame());

      await act(async () => {
        await result.current.startGame(Difficulty.MEDIUM);
      });

      expect(mockedIdiomService.startGame).toHaveBeenCalledWith(Difficulty.MEDIUM, undefined, []);
    });

    it('should handle different difficulty levels', async () => {
      mockedIdiomService.startGame.mockResolvedValue(mockQuestion);

      const { result } = renderHook(() => useIdiomGame());

      await act(async () => {
        await result.current.startGame(Difficulty.HARD);
      });

      expect(mockedIdiomService.startGame).toHaveBeenCalledWith(Difficulty.HARD, 'testUser', []);
    });

    it('should pass excludedIds when there are seen questions', async () => {
      const seenQuestions = new Set<string>(['1', '2', '3']);
      mockedQuestionTracker.getSeenQuestions.mockReturnValue(seenQuestions);
      mockedIdiomService.startGame.mockResolvedValue(mockQuestion);

      const { result } = renderHook(() => useIdiomGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      expect(mockedQuestionTracker.getSeenQuestions).toHaveBeenCalledWith('IDIOM', Difficulty.EASY);
      expect(mockedIdiomService.startGame).toHaveBeenCalledWith(
        Difficulty.EASY,
        'testUser',
        expect.arrayContaining(['1', '2', '3'])
      );
    });

    it('should limit excludedIds to MAX_EXCLUDED_QUESTIONS (50)', async () => {
      // Create 100 seen questions
      const seenQuestions = new Set<string>(
        Array.from({ length: 100 }, (_, i) => String(i + 1))
      );
      mockedQuestionTracker.getSeenQuestions.mockReturnValue(seenQuestions);
      mockedIdiomService.startGame.mockResolvedValue(mockQuestion);

      const { result } = renderHook(() => useIdiomGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      // Should only pass last 50 IDs (51-100)
      const callArgs = mockedIdiomService.startGame.mock.calls[0];
      const excludedIds = callArgs[2] as string[];

      expect(excludedIds).toHaveLength(50);
      expect(excludedIds[0]).toBe('51'); // First of last 50
      expect(excludedIds[49]).toBe('100'); // Last of last 50
    });

    it('should handle start game error', async () => {
      const error = new Error('Failed to load game');
      mockedIdiomService.startGame.mockRejectedValue(error);

      const { result } = renderHook(() => useIdiomGame());

      await act(async () => {
        try {
          await result.current.startGame(Difficulty.EASY);
        } catch (e) {
          // Expected error - hook re-throws after setting state
        }
      });

      expect(result.current.loading).toBe(false);
      expect(result.current.error).toBe('加载失败，请稍后重试');
      expect(result.current.question).toBeNull();
    });

    it('should reset validation result when starting new game', async () => {
      mockedIdiomService.startGame.mockResolvedValue(mockQuestion);

      const { result } = renderHook(() => useIdiomGame());

      // Set some validation result
      act(() => {
        result.current.setUserAnswer(['test']);
      });

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      expect(result.current.validationResult).toBeNull();
      expect(result.current.userAnswer).toEqual([]);
    });
  });

  describe('submitAnswer', () => {
    const mockQuestion = {
      id: 1,
      idiom: '画龙点睛',
      scrambledCharacters: ['睛', '画', '点', '龙'],
      difficulty: Difficulty.EASY,
    };

    const mockValidationResponse = {
      correct: true,
      score: 100,
      feedback: 'Correct!',
    };

    beforeEach(async () => {
      mockedIdiomService.startGame.mockResolvedValue(mockQuestion);
    });

    it('should submit answer successfully', async () => {
      mockedIdiomService.validateAnswer.mockResolvedValue(mockValidationResponse);

      const { result } = renderHook(() => useIdiomGame());

      // Start game first
      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      const answer = ['画', '龙', '点', '睛'];
      const timeTaken = 30;

      await act(async () => {
        await result.current.submitAnswer(answer, timeTaken);
      });

      expect(mockedIdiomService.validateAnswer).toHaveBeenCalledWith(
        {
          idiomId: 1,
          userAnswer: '画龙点睛',
          timeTaken: 30,
          hintsUsed: 0,
        },
        'testUser'
      );
      expect(result.current.validationResult).toEqual(mockValidationResponse);
      expect(result.current.timeTaken).toBe(30);
    });

    it('should not submit if no question loaded', async () => {
      const { result } = renderHook(() => useIdiomGame());

      await act(async () => {
        await result.current.submitAnswer(['test'], 10);
      });

      expect(mockedIdiomService.validateAnswer).not.toHaveBeenCalled();
    });

    it('should include hints used in submission', async () => {
      mockedIdiomService.validateAnswer.mockResolvedValue(mockValidationResponse);
      mockedIdiomService.getHint.mockResolvedValue({ hint: 'Test hint' });

      const { result } = renderHook(() => useIdiomGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      // Get a hint first
      await act(async () => {
        await result.current.getHint(1);
      });

      await act(async () => {
        await result.current.submitAnswer(['画', '龙', '点', '睛'], 30);
      });

      expect(mockedIdiomService.validateAnswer).toHaveBeenCalledWith(
        expect.objectContaining({
          hintsUsed: 1,
        }),
        'testUser'
      );
    });

    it('should handle submit answer error', async () => {
      const error = new Error('Submit failed');
      mockedIdiomService.validateAnswer.mockRejectedValue(error);

      const { result } = renderHook(() => useIdiomGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      await act(async () => {
        await result.current.submitAnswer(['画', '龙', '点', '睛'], 30);
      });

      expect(result.current.error).toBe('提交失败，请稍后重试');
    });
  });

  describe('getHint', () => {
    const mockQuestion = {
      id: 1,
      idiom: '画龙点睛',
      scrambledCharacters: ['睛', '画', '点', '龙'],
      difficulty: Difficulty.EASY,
    };

    const mockHint = {
      hint: '这个成语与绘画有关',
      level: 1,
    };

    beforeEach(async () => {
      mockedIdiomService.startGame.mockResolvedValue(mockQuestion);
    });

    it('should get hint successfully', async () => {
      mockedIdiomService.getHint.mockResolvedValue(mockHint);

      const { result } = renderHook(() => useIdiomGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      let hintResult;
      await act(async () => {
        hintResult = await result.current.getHint(1);
      });

      expect(mockedIdiomService.getHint).toHaveBeenCalledWith(
        {
          idiomId: 1,
          hintLevel: 1,
        },
        'testUser'
      );
      expect(hintResult).toEqual(mockHint);
      expect(result.current.hintsUsed).toBe(1);
    });

    it('should increment hints used counter', async () => {
      mockedIdiomService.getHint.mockResolvedValue(mockHint);

      const { result } = renderHook(() => useIdiomGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      expect(result.current.hintsUsed).toBe(0);

      await act(async () => {
        await result.current.getHint(1);
      });

      expect(result.current.hintsUsed).toBe(1);

      await act(async () => {
        await result.current.getHint(2);
      });

      expect(result.current.hintsUsed).toBe(2);
    });

    it('should not get hint if no question loaded', async () => {
      const { result } = renderHook(() => useIdiomGame());

      await act(async () => {
        await result.current.getHint(1);
      });

      expect(mockedIdiomService.getHint).not.toHaveBeenCalled();
    });

    it('should handle get hint error', async () => {
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
      const error = new Error('Hint failed');
      mockedIdiomService.getHint.mockRejectedValue(error);

      const { result } = renderHook(() => useIdiomGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      await act(async () => {
        await result.current.getHint(1);
      });

      expect(consoleSpy).toHaveBeenCalledWith('Failed to get hint:', error);
      expect(result.current.loading).toBe(false);

      consoleSpy.mockRestore();
    });
  });

  describe('setUserAnswer', () => {
    it('should update user answer', () => {
      const { result } = renderHook(() => useIdiomGame());

      const answer = ['画', '龙', '点', '睛'];

      act(() => {
        result.current.setUserAnswer(answer);
      });

      expect(result.current.userAnswer).toEqual(answer);
    });

    it('should handle empty answer', () => {
      const { result } = renderHook(() => useIdiomGame());

      act(() => {
        result.current.setUserAnswer([]);
      });

      expect(result.current.userAnswer).toEqual([]);
    });

    it('should allow updating answer multiple times', () => {
      const { result } = renderHook(() => useIdiomGame());

      act(() => {
        result.current.setUserAnswer(['画', '龙']);
      });

      expect(result.current.userAnswer).toEqual(['画', '龙']);

      act(() => {
        result.current.setUserAnswer(['画', '龙', '点', '睛']);
      });

      expect(result.current.userAnswer).toEqual(['画', '龙', '点', '睛']);
    });
  });

  describe('reset', () => {
    it('should reset all state to initial values', async () => {
      const mockQuestion = {
        id: 1,
        idiom: '画龙点睛',
        scrambledCharacters: ['睛', '画', '点', '龙'],
        difficulty: Difficulty.EASY,
      };

      mockedIdiomService.startGame.mockResolvedValue(mockQuestion);
      mockedIdiomService.getHint.mockResolvedValue({ hint: 'Test' });

      const { result } = renderHook(() => useIdiomGame());

      // Set some state
      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      await act(async () => {
        await result.current.getHint(1);
      });

      act(() => {
        result.current.setUserAnswer(['画', '龙']);
      });

      // Verify state changed
      expect(result.current.question).not.toBeNull();
      expect(result.current.hintsUsed).toBe(1);
      expect(result.current.userAnswer).toEqual(['画', '龙']);

      // Reset
      act(() => {
        result.current.reset();
      });

      // Verify reset to initial state
      expect(result.current.question).toBeNull();
      expect(result.current.loading).toBe(false);
      expect(result.current.error).toBeNull();
      expect(result.current.userAnswer).toEqual([]);
      expect(result.current.hintsUsed).toBe(0);
      expect(result.current.timeTaken).toBe(0);
      expect(result.current.validationResult).toBeNull();
    });
  });

  describe('Loading states', () => {
    it('should handle loading state during startGame', async () => {
      const mockQuestion = {
        id: 1,
        idiom: '画龙点睛',
        scrambledCharacters: ['睛', '画', '点', '龙'],
        difficulty: Difficulty.EASY,
      };

      mockedIdiomService.startGame.mockImplementation(
        () => new Promise((resolve) => setTimeout(() => resolve(mockQuestion), 100))
      );

      const { result } = renderHook(() => useIdiomGame());

      act(() => {
        result.current.startGame(Difficulty.EASY);
      });

      expect(result.current.loading).toBe(true);

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });
    });

    it('should handle loading state during submitAnswer', async () => {
      const mockQuestion = {
        id: 1,
        idiom: '画龙点睛',
        scrambledCharacters: ['睛', '画', '点', '龙'],
        difficulty: Difficulty.EASY,
      };

      mockedIdiomService.startGame.mockResolvedValue(mockQuestion);
      mockedIdiomService.validateAnswer.mockImplementation(
        () => new Promise((resolve) => setTimeout(() => resolve({ correct: true, score: 100 }), 100))
      );

      const { result } = renderHook(() => useIdiomGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      act(() => {
        result.current.submitAnswer(['画', '龙', '点', '睛'], 30);
      });

      expect(result.current.loading).toBe(true);

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });
    });
  });

  describe('No-repeat questions feature integration', () => {
    const mockQuestion = {
      id: 1,
      idiom: '画龙点睛',
      scrambledCharacters: ['睛', '画', '点', '龙'],
      difficulty: Difficulty.EASY,
    };

    beforeEach(() => {
      mockedIdiomService.startGame.mockResolvedValue(mockQuestion);
    });

    it('should mark question as seen when starting a game', async () => {
      const { result } = renderHook(() => useIdiomGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      expect(mockedQuestionTracker.markQuestionAsSeen).toHaveBeenCalledWith(
        'IDIOM',
        Difficulty.EASY,
        1
      );
    });

    it('should track questions separately by difficulty', async () => {
      const { result } = renderHook(() => useIdiomGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      expect(mockedQuestionTracker.markQuestionAsSeen).toHaveBeenCalledWith(
        'IDIOM',
        Difficulty.EASY,
        1
      );

      await act(async () => {
        await result.current.startGame(Difficulty.HARD);
      });

      expect(mockedQuestionTracker.markQuestionAsSeen).toHaveBeenCalledWith(
        'IDIOM',
        Difficulty.HARD,
        1
      );
    });

    it('should not mark question as seen if question is null', async () => {
      mockedIdiomService.startGame.mockResolvedValue(null as any);

      const { result } = renderHook(() => useIdiomGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      expect(mockedQuestionTracker.markQuestionAsSeen).not.toHaveBeenCalled();
    });

    it('should mark question as seen only for IDIOM game type', async () => {
      const { result } = renderHook(() => useIdiomGame());

      await act(async () => {
        await result.current.startGame(Difficulty.MEDIUM);
      });

      expect(mockedQuestionTracker.markQuestionAsSeen).toHaveBeenCalledWith(
        'IDIOM',
        expect.any(String),
        expect.any(Number)
      );

      // Verify it's called with 'IDIOM' and not 'SENTENCE'
      const callArgs = mockedQuestionTracker.markQuestionAsSeen.mock.calls[0];
      expect(callArgs[0]).toBe('IDIOM');
    });
  });
});
