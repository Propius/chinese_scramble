import { renderHook, act, waitFor } from '@testing-library/react';
import { useSentenceGame } from '../useSentenceGame';
import { Difficulty } from '../../constants/difficulties';
import sentenceService from '../../services/sentenceService';
import { usernameUtils } from '../../utils/usernameUtils';
import questionTracker from '../../utils/questionTracker';

// Mock dependencies
jest.mock('../../services/sentenceService');
jest.mock('../../utils/usernameUtils');
jest.mock('../../utils/questionTracker');

const mockedSentenceService = sentenceService as jest.Mocked<typeof sentenceService>;
const mockedUsernameUtils = usernameUtils as jest.Mocked<typeof usernameUtils>;
const mockedQuestionTracker = questionTracker as jest.Mocked<typeof questionTracker>;

describe('useSentenceGame', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockedUsernameUtils.getUsername.mockReturnValue('testUser');
    // Mock questionTracker.getSeenQuestions to return empty Set by default
    mockedQuestionTracker.getSeenQuestions.mockReturnValue(new Set<string>());
  });

  describe('Initial state', () => {
    it('should have correct initial state', () => {
      const { result } = renderHook(() => useSentenceGame());

      expect(result.current.question).toBeNull();
      expect(result.current.loading).toBe(false);
      expect(result.current.error).toBeNull();
      expect(result.current.userAnswer).toEqual([]);
      expect(result.current.hintsUsed).toBe(0);
      expect(result.current.timeTaken).toBe(0);
      expect(result.current.validationResult).toBeNull();
    });

    it('should have all required methods', () => {
      const { result } = renderHook(() => useSentenceGame());

      expect(typeof result.current.startGame).toBe('function');
      expect(typeof result.current.submitAnswer).toBe('function');
      expect(typeof result.current.getHint).toBe('function');
      expect(typeof result.current.setUserAnswer).toBe('function');
      expect(typeof result.current.reset).toBe('function');
    });
  });

  describe('startGame', () => {
    const mockQuestion = {
      id: 'sent-123',
      text: '我喜欢吃苹果',
      pinyin: 'wǒ xǐhuān chī píngguǒ',
      translation: 'I like to eat apples',
      difficulty: Difficulty.EASY,
      words: ['我', '喜欢', '吃', '苹果'],
      wordCount: 4,
      grammarPattern: 'Subject + Verb + Object',
      grammarExplanation: 'Basic SVO sentence structure',
      topic: 'Food',
      vocabularyLevel: 'HSK1',
      scrambledWords: ['吃', '我', '苹果', '喜欢'],
    };

    it('should start game successfully', async () => {
      mockedSentenceService.startGame.mockResolvedValue(mockQuestion);

      const { result } = renderHook(() => useSentenceGame());

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
      expect(mockedSentenceService.startGame).toHaveBeenCalledWith(Difficulty.EASY, 'testUser', []);
    });

    it('should handle start game with Guest user', async () => {
      mockedUsernameUtils.getUsername.mockReturnValue(null);
      mockedSentenceService.startGame.mockResolvedValue(mockQuestion);

      const { result } = renderHook(() => useSentenceGame());

      await act(async () => {
        await result.current.startGame(Difficulty.MEDIUM);
      });

      expect(mockedSentenceService.startGame).toHaveBeenCalledWith(Difficulty.MEDIUM, undefined, []);
    });

    it('should handle different difficulty levels - EASY', async () => {
      mockedSentenceService.startGame.mockResolvedValue(mockQuestion);

      const { result } = renderHook(() => useSentenceGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      expect(mockedSentenceService.startGame).toHaveBeenCalledWith(Difficulty.EASY, 'testUser', []);
    });

    it('should handle different difficulty levels - MEDIUM', async () => {
      mockedSentenceService.startGame.mockResolvedValue(mockQuestion);

      const { result } = renderHook(() => useSentenceGame());

      await act(async () => {
        await result.current.startGame(Difficulty.MEDIUM);
      });

      expect(mockedSentenceService.startGame).toHaveBeenCalledWith(Difficulty.MEDIUM, 'testUser', []);
    });

    it('should handle different difficulty levels - HARD', async () => {
      mockedSentenceService.startGame.mockResolvedValue(mockQuestion);

      const { result } = renderHook(() => useSentenceGame());

      await act(async () => {
        await result.current.startGame(Difficulty.HARD);
      });

      expect(mockedSentenceService.startGame).toHaveBeenCalledWith(Difficulty.HARD, 'testUser', []);
    });

    it('should pass excludedIds when there are seen questions', async () => {
      const seenQuestions = new Set<string>(['sent-1', 'sent-2', 'sent-3']);
      mockedQuestionTracker.getSeenQuestions.mockReturnValue(seenQuestions);
      mockedSentenceService.startGame.mockResolvedValue(mockQuestion);

      const { result } = renderHook(() => useSentenceGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      expect(mockedQuestionTracker.getSeenQuestions).toHaveBeenCalledWith('SENTENCE', Difficulty.EASY);
      expect(mockedSentenceService.startGame).toHaveBeenCalledWith(
        Difficulty.EASY,
        'testUser',
        expect.arrayContaining(['sent-1', 'sent-2', 'sent-3'])
      );
    });

    it('should limit excludedIds to MAX_EXCLUDED_QUESTIONS (50)', async () => {
      // Create 100 seen questions
      const seenQuestions = new Set<string>(
        Array.from({ length: 100 }, (_, i) => `sent-${i + 1}`)
      );
      mockedQuestionTracker.getSeenQuestions.mockReturnValue(seenQuestions);
      mockedSentenceService.startGame.mockResolvedValue(mockQuestion);

      const { result } = renderHook(() => useSentenceGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      // Should only pass last 50 IDs (sent-51 to sent-100)
      const callArgs = mockedSentenceService.startGame.mock.calls[0];
      const excludedIds = callArgs[2] as string[];

      expect(excludedIds).toHaveLength(50);
      expect(excludedIds[0]).toBe('sent-51'); // First of last 50
      expect(excludedIds[49]).toBe('sent-100'); // Last of last 50
    });

    it('should handle start game error', async () => {
      const error = new Error('Failed to load game');
      mockedSentenceService.startGame.mockRejectedValue(error);

      const { result } = renderHook(() => useSentenceGame());

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
      mockedSentenceService.startGame.mockResolvedValue(mockQuestion);

      const { result } = renderHook(() => useSentenceGame());

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

    it('should reset time taken when starting new game', async () => {
      mockedSentenceService.startGame.mockResolvedValue(mockQuestion);

      const { result } = renderHook(() => useSentenceGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      expect(result.current.timeTaken).toBe(0);
    });
  });

  describe('submitAnswer', () => {
    const mockQuestion = {
      id: 'sent-123',
      text: '我喜欢吃苹果',
      pinyin: 'wǒ xǐhuān chī píngguǒ',
      translation: 'I like to eat apples',
      difficulty: Difficulty.EASY,
      words: ['我', '喜欢', '吃', '苹果'],
      wordCount: 4,
      grammarPattern: 'Subject + Verb + Object',
      grammarExplanation: 'Basic SVO sentence structure',
      topic: 'Food',
      vocabularyLevel: 'HSK1',
      scrambledWords: ['吃', '我', '苹果', '喜欢'],
    };

    const mockValidationResponse = {
      correct: true,
      score: 100,
      translation: 'I like to eat apples',
      grammarExplanation: 'Basic SVO sentence structure',
      correctAnswer: ['我', '喜欢', '吃', '苹果'],
      feedback: 'Correct!',
    };

    beforeEach(async () => {
      mockedSentenceService.startGame.mockResolvedValue(mockQuestion);
    });

    it('should submit answer successfully', async () => {
      mockedSentenceService.validateAnswer.mockResolvedValue(mockValidationResponse);

      const { result } = renderHook(() => useSentenceGame());

      // Start game first
      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      const answer = ['我', '喜欢', '吃', '苹果'];
      const timeTaken = 30;

      await act(async () => {
        await result.current.submitAnswer(answer, timeTaken);
      });

      expect(mockedSentenceService.validateAnswer).toHaveBeenCalledWith(
        {
          sentenceId: 'sent-123',
          userAnswer: answer,
          timeTaken: 30,
          hintsUsed: 0,
        },
        'testUser'
      );
      expect(result.current.validationResult).toEqual(mockValidationResponse);
      expect(result.current.timeTaken).toBe(30);
    });

    it('should not submit if no question loaded', async () => {
      const { result } = renderHook(() => useSentenceGame());

      await act(async () => {
        await result.current.submitAnswer(['test'], 10);
      });

      expect(mockedSentenceService.validateAnswer).not.toHaveBeenCalled();
    });

    it('should include hints used in submission', async () => {
      mockedSentenceService.validateAnswer.mockResolvedValue(mockValidationResponse);
      mockedSentenceService.getHint.mockResolvedValue({ hint: 'Test hint', pointPenalty: 10 });

      const { result } = renderHook(() => useSentenceGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      // Get a hint first
      await act(async () => {
        await result.current.getHint(1);
      });

      await act(async () => {
        await result.current.submitAnswer(['我', '喜欢', '吃', '苹果'], 30);
      });

      expect(mockedSentenceService.validateAnswer).toHaveBeenCalledWith(
        expect.objectContaining({
          hintsUsed: 1,
        }),
        'testUser'
      );
    });

    it('should handle submit answer error', async () => {
      const error = new Error('Submit failed');
      mockedSentenceService.validateAnswer.mockRejectedValue(error);

      const { result } = renderHook(() => useSentenceGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      await act(async () => {
        await result.current.submitAnswer(['我', '喜欢', '吃', '苹果'], 30);
      });

      expect(result.current.error).toBe('提交失败，请稍后重试');
    });

    it('should return validation result', async () => {
      mockedSentenceService.validateAnswer.mockResolvedValue(mockValidationResponse);

      const { result } = renderHook(() => useSentenceGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      let validationResult;
      await act(async () => {
        validationResult = await result.current.submitAnswer(['我', '喜欢', '吃', '苹果'], 30);
      });

      expect(validationResult).toEqual(mockValidationResponse);
    });

    it('should handle incorrect answer', async () => {
      const incorrectResponse = {
        ...mockValidationResponse,
        correct: false,
        score: 0,
        feedback: 'Incorrect, try again!',
      };
      mockedSentenceService.validateAnswer.mockResolvedValue(incorrectResponse);

      const { result } = renderHook(() => useSentenceGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      await act(async () => {
        await result.current.submitAnswer(['吃', '我', '喜欢', '苹果'], 30);
      });

      expect(result.current.validationResult?.correct).toBe(false);
      expect(result.current.validationResult?.score).toBe(0);
    });
  });

  describe('getHint', () => {
    const mockQuestion = {
      id: 'sent-123',
      text: '我喜欢吃苹果',
      pinyin: 'wǒ xǐhuān chī píngguǒ',
      translation: 'I like to eat apples',
      difficulty: Difficulty.EASY,
      words: ['我', '喜欢', '吃', '苹果'],
      wordCount: 4,
      grammarPattern: 'Subject + Verb + Object',
      grammarExplanation: 'Basic SVO sentence structure',
      topic: 'Food',
      vocabularyLevel: 'HSK1',
      scrambledWords: ['吃', '我', '苹果', '喜欢'],
    };

    const mockHint = {
      hint: '第一个字是"我"',
      pointPenalty: 10,
    };

    beforeEach(async () => {
      mockedSentenceService.startGame.mockResolvedValue(mockQuestion);
    });

    it('should get hint successfully', async () => {
      mockedSentenceService.getHint.mockResolvedValue(mockHint);

      const { result } = renderHook(() => useSentenceGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      let hintResult;
      await act(async () => {
        hintResult = await result.current.getHint(1);
      });

      expect(mockedSentenceService.getHint).toHaveBeenCalledWith(
        {
          sentenceId: 'sent-123',
          hintLevel: 1,
        },
        'testUser'
      );
      expect(hintResult).toEqual(mockHint);
      expect(result.current.hintsUsed).toBe(1);
    });

    it('should increment hints used counter', async () => {
      mockedSentenceService.getHint.mockResolvedValue(mockHint);

      const { result } = renderHook(() => useSentenceGame());

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
      const { result } = renderHook(() => useSentenceGame());

      await act(async () => {
        await result.current.getHint(1);
      });

      expect(mockedSentenceService.getHint).not.toHaveBeenCalled();
    });

    it('should handle get hint error', async () => {
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
      const error = new Error('Hint failed');
      mockedSentenceService.getHint.mockRejectedValue(error);

      const { result } = renderHook(() => useSentenceGame());

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

    it('should handle multiple hint levels', async () => {
      const hint1 = { hint: 'Level 1 hint', pointPenalty: 10 };
      const hint2 = { hint: 'Level 2 hint', pointPenalty: 15 };

      mockedSentenceService.getHint.mockResolvedValueOnce(hint1);
      mockedSentenceService.getHint.mockResolvedValueOnce(hint2);

      const { result } = renderHook(() => useSentenceGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      let firstHint;
      await act(async () => {
        firstHint = await result.current.getHint(1);
      });

      expect(firstHint).toEqual(hint1);

      let secondHint;
      await act(async () => {
        secondHint = await result.current.getHint(2);
      });

      expect(secondHint).toEqual(hint2);
      expect(result.current.hintsUsed).toBe(2);
    });
  });

  describe('setUserAnswer', () => {
    it('should update user answer', () => {
      const { result } = renderHook(() => useSentenceGame());

      const answer = ['我', '喜欢', '吃', '苹果'];

      act(() => {
        result.current.setUserAnswer(answer);
      });

      expect(result.current.userAnswer).toEqual(answer);
    });

    it('should handle empty answer', () => {
      const { result } = renderHook(() => useSentenceGame());

      act(() => {
        result.current.setUserAnswer([]);
      });

      expect(result.current.userAnswer).toEqual([]);
    });

    it('should allow updating answer multiple times', () => {
      const { result } = renderHook(() => useSentenceGame());

      act(() => {
        result.current.setUserAnswer(['我', '喜欢']);
      });

      expect(result.current.userAnswer).toEqual(['我', '喜欢']);

      act(() => {
        result.current.setUserAnswer(['我', '喜欢', '吃', '苹果']);
      });

      expect(result.current.userAnswer).toEqual(['我', '喜欢', '吃', '苹果']);
    });

    it('should handle single word answer', () => {
      const { result } = renderHook(() => useSentenceGame());

      act(() => {
        result.current.setUserAnswer(['我']);
      });

      expect(result.current.userAnswer).toEqual(['我']);
    });
  });

  describe('reset', () => {
    it('should reset all state to initial values', async () => {
      const mockQuestion = {
        id: 'sent-123',
        text: '我喜欢吃苹果',
        pinyin: 'wǒ xǐhuān chī píngguǒ',
        translation: 'I like to eat apples',
        difficulty: Difficulty.EASY,
        words: ['我', '喜欢', '吃', '苹果'],
        wordCount: 4,
        grammarPattern: 'Subject + Verb + Object',
        grammarExplanation: 'Basic SVO sentence structure',
        topic: 'Food',
        vocabularyLevel: 'HSK1',
        scrambledWords: ['吃', '我', '苹果', '喜欢'],
      };

      mockedSentenceService.startGame.mockResolvedValue(mockQuestion);
      mockedSentenceService.getHint.mockResolvedValue({ hint: 'Test', pointPenalty: 10 });

      const { result } = renderHook(() => useSentenceGame());

      // Set some state
      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      await act(async () => {
        await result.current.getHint(1);
      });

      act(() => {
        result.current.setUserAnswer(['我', '喜欢']);
      });

      // Verify state changed
      expect(result.current.question).not.toBeNull();
      expect(result.current.hintsUsed).toBe(1);
      expect(result.current.userAnswer).toEqual(['我', '喜欢']);

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
        id: 'sent-123',
        text: '我喜欢吃苹果',
        pinyin: 'wǒ xǐhuān chī píngguǒ',
        translation: 'I like to eat apples',
        difficulty: Difficulty.EASY,
        words: ['我', '喜欢', '吃', '苹果'],
        wordCount: 4,
        grammarPattern: 'Subject + Verb + Object',
        grammarExplanation: 'Basic SVO sentence structure',
        topic: 'Food',
        vocabularyLevel: 'HSK1',
        scrambledWords: ['吃', '我', '苹果', '喜欢'],
      };

      mockedSentenceService.startGame.mockImplementation(
        () => new Promise((resolve) => setTimeout(() => resolve(mockQuestion), 100))
      );

      const { result } = renderHook(() => useSentenceGame());

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
        id: 'sent-123',
        text: '我喜欢吃苹果',
        pinyin: 'wǒ xǐhuān chī píngguǒ',
        translation: 'I like to eat apples',
        difficulty: Difficulty.EASY,
        words: ['我', '喜欢', '吃', '苹果'],
        wordCount: 4,
        grammarPattern: 'Subject + Verb + Object',
        grammarExplanation: 'Basic SVO sentence structure',
        topic: 'Food',
        vocabularyLevel: 'HSK1',
        scrambledWords: ['吃', '我', '苹果', '喜欢'],
      };

      mockedSentenceService.startGame.mockResolvedValue(mockQuestion);
      mockedSentenceService.validateAnswer.mockImplementation(
        () => new Promise((resolve) => setTimeout(() => resolve({
          correct: true,
          score: 100,
          translation: 'I like to eat apples',
          grammarExplanation: 'Basic SVO sentence structure',
          correctAnswer: ['我', '喜欢', '吃', '苹果'],
        }), 100))
      );

      const { result } = renderHook(() => useSentenceGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      act(() => {
        result.current.submitAnswer(['我', '喜欢', '吃', '苹果'], 30);
      });

      expect(result.current.loading).toBe(true);

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });
    });

    it('should handle loading state during getHint', async () => {
      const mockQuestion = {
        id: 'sent-123',
        text: '我喜欢吃苹果',
        pinyin: 'wǒ xǐhuān chī píngguǒ',
        translation: 'I like to eat apples',
        difficulty: Difficulty.EASY,
        words: ['我', '喜欢', '吃', '苹果'],
        wordCount: 4,
        grammarPattern: 'Subject + Verb + Object',
        grammarExplanation: 'Basic SVO sentence structure',
        topic: 'Food',
        vocabularyLevel: 'HSK1',
        scrambledWords: ['吃', '我', '苹果', '喜欢'],
      };

      mockedSentenceService.startGame.mockResolvedValue(mockQuestion);
      mockedSentenceService.getHint.mockImplementation(
        () => new Promise((resolve) => setTimeout(() => resolve({ hint: 'Test', pointPenalty: 10 }), 100))
      );

      const { result } = renderHook(() => useSentenceGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      act(() => {
        result.current.getHint(1);
      });

      expect(result.current.loading).toBe(true);

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });
    });
  });

  describe('No-repeat questions feature integration', () => {
    const mockQuestion = {
      id: 'sent-123',
      text: '我喜欢吃苹果',
      pinyin: 'wǒ xǐhuān chī píngguǒ',
      translation: 'I like to eat apples',
      difficulty: Difficulty.EASY,
      words: ['我', '喜欢', '吃', '苹果'],
      wordCount: 4,
      grammarPattern: 'Subject + Verb + Object',
      grammarExplanation: 'Basic SVO sentence structure',
      topic: 'Food',
      vocabularyLevel: 'HSK1',
      scrambledWords: ['吃', '我', '苹果', '喜欢'],
    };

    beforeEach(() => {
      mockedSentenceService.startGame.mockResolvedValue(mockQuestion);
    });

    it('should mark question as seen when starting a game', async () => {
      const { result } = renderHook(() => useSentenceGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      expect(mockedQuestionTracker.markQuestionAsSeen).toHaveBeenCalledWith(
        'SENTENCE',
        Difficulty.EASY,
        'sent-123'
      );
    });

    it('should track questions separately by difficulty', async () => {
      const { result } = renderHook(() => useSentenceGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      expect(mockedQuestionTracker.markQuestionAsSeen).toHaveBeenCalledWith(
        'SENTENCE',
        Difficulty.EASY,
        'sent-123'
      );

      await act(async () => {
        await result.current.startGame(Difficulty.HARD);
      });

      expect(mockedQuestionTracker.markQuestionAsSeen).toHaveBeenCalledWith(
        'SENTENCE',
        Difficulty.HARD,
        'sent-123'
      );
    });

    it('should not mark question as seen if question is null', async () => {
      mockedSentenceService.startGame.mockResolvedValue(null as any);

      const { result } = renderHook(() => useSentenceGame());

      await act(async () => {
        await result.current.startGame(Difficulty.EASY);
      });

      expect(mockedQuestionTracker.markQuestionAsSeen).not.toHaveBeenCalled();
    });

    it('should mark question as seen only for SENTENCE game type', async () => {
      const { result } = renderHook(() => useSentenceGame());

      await act(async () => {
        await result.current.startGame(Difficulty.MEDIUM);
      });

      expect(mockedQuestionTracker.markQuestionAsSeen).toHaveBeenCalledWith(
        'SENTENCE',
        expect.any(String),
        expect.any(String)
      );

      // Verify it's called with 'SENTENCE' and not 'IDIOM'
      const callArgs = mockedQuestionTracker.markQuestionAsSeen.mock.calls[0];
      expect(callArgs[0]).toBe('SENTENCE');
    });
  });
});
