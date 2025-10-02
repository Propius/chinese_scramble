import apiClient from '../api';
import sentenceService, {
  SentenceQuestion,
  SentenceValidationRequest,
  SentenceValidationResponse,
  SentenceHintRequest,
  SentenceHintResponse
} from '../sentenceService';
import { Difficulty } from '../../constants/difficulties';

jest.mock('../api');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

describe('sentenceService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('startGame', () => {
    const mockQuestion: SentenceQuestion = {
      id: 'sentence-1',
      text: '我喜欢吃水果',
      pinyin: 'wǒ xǐ huān chī shuǐ guǒ',
      translation: 'I like to eat fruit',
      difficulty: Difficulty.MEDIUM,
      words: ['我', '喜欢', '吃', '水果'],
      wordCount: 4,
      grammarPattern: 'Subject + Verb + Object',
      grammarExplanation: 'Basic sentence structure',
      topic: 'Food',
      vocabularyLevel: 'HSK2',
      scrambledWords: ['水果', '我', '吃', '喜欢']
    };

    it('should call apiClient.get with correct URL when only difficulty is provided', async () => {
      mockedApiClient.get.mockResolvedValue(mockQuestion);

      const result = await sentenceService.startGame(Difficulty.EASY);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/sentence-game/start?difficulty=EASY');
      expect(result).toEqual(mockQuestion);
    });

    it('should call apiClient.get with difficulty and playerId when both are provided', async () => {
      mockedApiClient.get.mockResolvedValue(mockQuestion);

      const result = await sentenceService.startGame(Difficulty.MEDIUM, 'player-456');

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/sentence-game/start?difficulty=MEDIUM&playerId=player-456');
      expect(result).toEqual(mockQuestion);
    });

    it('should work with EASY difficulty', async () => {
      mockedApiClient.get.mockResolvedValue(mockQuestion);

      await sentenceService.startGame(Difficulty.EASY);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/sentence-game/start?difficulty=EASY');
    });

    it('should work with MEDIUM difficulty', async () => {
      mockedApiClient.get.mockResolvedValue(mockQuestion);

      await sentenceService.startGame(Difficulty.MEDIUM);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/sentence-game/start?difficulty=MEDIUM');
    });

    it('should work with HARD difficulty', async () => {
      mockedApiClient.get.mockResolvedValue(mockQuestion);

      await sentenceService.startGame(Difficulty.HARD);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/sentence-game/start?difficulty=HARD');
    });

    it('should work with EXPERT difficulty', async () => {
      mockedApiClient.get.mockResolvedValue(mockQuestion);

      await sentenceService.startGame(Difficulty.EXPERT);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/sentence-game/start?difficulty=EXPERT');
    });

    it('should propagate errors from apiClient', async () => {
      const mockError = new Error('Network error');
      mockedApiClient.get.mockRejectedValue(mockError);

      await expect(sentenceService.startGame(Difficulty.EASY)).rejects.toThrow('Network error');
    });

    it('should handle playerId with special characters', async () => {
      mockedApiClient.get.mockResolvedValue(mockQuestion);

      await sentenceService.startGame(Difficulty.EASY, 'player#456');

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/sentence-game/start?difficulty=EASY&playerId=player%23456');
    });
  });

  describe('validateAnswer', () => {
    const mockRequest: SentenceValidationRequest = {
      sentenceId: 'sentence-1',
      userAnswer: ['我', '喜欢', '吃', '水果'],
      timeTaken: 20000,
      hintsUsed: 0
    };

    const mockResponse: SentenceValidationResponse = {
      correct: true,
      score: 95,
      translation: 'I like to eat fruit',
      grammarExplanation: 'Basic sentence structure',
      correctAnswer: ['我', '喜欢', '吃', '水果'],
      feedback: 'Perfect!'
    };

    it('should call apiClient.post with correct URL and payload without playerId', async () => {
      mockedApiClient.post.mockResolvedValue(mockResponse);

      const result = await sentenceService.validateAnswer(mockRequest);

      expect(mockedApiClient.post).toHaveBeenCalledWith('/api/sentence-game/submit', mockRequest);
      expect(result).toEqual(mockResponse);
    });

    it('should call apiClient.post with playerId included in payload', async () => {
      mockedApiClient.post.mockResolvedValue(mockResponse);

      const result = await sentenceService.validateAnswer(mockRequest, 'player-456');

      expect(mockedApiClient.post).toHaveBeenCalledWith('/api/sentence-game/submit', {
        ...mockRequest,
        playerId: 'player-456'
      });
      expect(result).toEqual(mockResponse);
    });

    it('should not modify original request object when adding playerId', async () => {
      mockedApiClient.post.mockResolvedValue(mockResponse);

      await sentenceService.validateAnswer(mockRequest, 'player-456');

      expect(mockRequest).not.toHaveProperty('playerId');
    });

    it('should handle validation with zero hints used', async () => {
      const requestWithNoHints = { ...mockRequest, hintsUsed: 0 };
      mockedApiClient.post.mockResolvedValue(mockResponse);

      await sentenceService.validateAnswer(requestWithNoHints);

      expect(mockedApiClient.post).toHaveBeenCalledWith('/api/sentence-game/submit', requestWithNoHints);
    });

    it('should handle validation with multiple hints used', async () => {
      const requestWithMultipleHints = { ...mockRequest, hintsUsed: 2 };
      mockedApiClient.post.mockResolvedValue(mockResponse);

      await sentenceService.validateAnswer(requestWithMultipleHints);

      expect(mockedApiClient.post).toHaveBeenCalledWith('/api/sentence-game/submit', requestWithMultipleHints);
    });

    it('should propagate errors from apiClient', async () => {
      const mockError = new Error('Validation failed');
      mockedApiClient.post.mockRejectedValue(mockError);

      await expect(sentenceService.validateAnswer(mockRequest)).rejects.toThrow('Validation failed');
    });

    it('should handle incorrect answer response', async () => {
      const incorrectResponse: SentenceValidationResponse = {
        correct: false,
        score: 0,
        translation: 'I like to eat fruit',
        grammarExplanation: 'Basic sentence structure',
        correctAnswer: ['我', '喜欢', '吃', '水果'],
        feedback: 'Word order is incorrect'
      };
      mockedApiClient.post.mockResolvedValue(incorrectResponse);

      const result = await sentenceService.validateAnswer(mockRequest);

      expect(result.correct).toBe(false);
      expect(result.score).toBe(0);
    });

    it('should handle array of words in userAnswer', async () => {
      const longSentenceRequest = {
        ...mockRequest,
        userAnswer: ['他', '每天', '都', '在', '学习', '中文']
      };
      mockedApiClient.post.mockResolvedValue(mockResponse);

      await sentenceService.validateAnswer(longSentenceRequest);

      expect(mockedApiClient.post).toHaveBeenCalledWith('/api/sentence-game/submit', longSentenceRequest);
    });
  });

  describe('getHint', () => {
    const mockHintRequest: SentenceHintRequest = {
      sentenceId: 'sentence-1',
      hintLevel: 1
    };

    const mockHintResponse: SentenceHintResponse = {
      hint: 'First word should be 我',
      pointPenalty: 15
    };

    it('should call apiClient.post with correct URL and payload without playerId', async () => {
      mockedApiClient.post.mockResolvedValue(mockHintResponse);

      const result = await sentenceService.getHint(mockHintRequest);

      expect(mockedApiClient.post).toHaveBeenCalledWith('/api/sentence-game/hint', mockHintRequest);
      expect(result).toEqual(mockHintResponse);
    });

    it('should call apiClient.post with playerId included in payload', async () => {
      mockedApiClient.post.mockResolvedValue(mockHintResponse);

      const result = await sentenceService.getHint(mockHintRequest, 'player-456');

      expect(mockedApiClient.post).toHaveBeenCalledWith('/api/sentence-game/hint', {
        ...mockHintRequest,
        playerId: 'player-456'
      });
      expect(result).toEqual(mockHintResponse);
    });

    it('should not modify original request object when adding playerId', async () => {
      mockedApiClient.post.mockResolvedValue(mockHintResponse);

      await sentenceService.getHint(mockHintRequest, 'player-456');

      expect(mockHintRequest).not.toHaveProperty('playerId');
    });

    it('should handle hint level 1', async () => {
      const level1Request = { ...mockHintRequest, hintLevel: 1 };
      mockedApiClient.post.mockResolvedValue(mockHintResponse);

      await sentenceService.getHint(level1Request);

      expect(mockedApiClient.post).toHaveBeenCalledWith('/api/sentence-game/hint', level1Request);
    });

    it('should handle hint level 2', async () => {
      const level2Request = { ...mockHintRequest, hintLevel: 2 };
      const level2Response = { hint: 'Second word should be 喜欢', pointPenalty: 30 };
      mockedApiClient.post.mockResolvedValue(level2Response);

      const result = await sentenceService.getHint(level2Request);

      expect(result).toEqual(level2Response);
    });

    it('should handle hint level 3', async () => {
      const level3Request = { ...mockHintRequest, hintLevel: 3 };
      mockedApiClient.post.mockResolvedValue(mockHintResponse);

      await sentenceService.getHint(level3Request);

      expect(mockedApiClient.post).toHaveBeenCalledWith('/api/sentence-game/hint', level3Request);
    });

    it('should propagate errors from apiClient', async () => {
      const mockError = new Error('Hint unavailable');
      mockedApiClient.post.mockRejectedValue(mockError);

      await expect(sentenceService.getHint(mockHintRequest)).rejects.toThrow('Hint unavailable');
    });

    it('should return correct point penalty', async () => {
      mockedApiClient.post.mockResolvedValue(mockHintResponse);

      const result = await sentenceService.getHint(mockHintRequest);

      expect(result.pointPenalty).toBe(15);
    });
  });

  describe('getTopics', () => {
    const mockTopics = ['Food', 'Travel', 'Education', 'Sports'];

    it('should call apiClient.get with correct URL', async () => {
      mockedApiClient.get.mockResolvedValue(mockTopics);

      const result = await sentenceService.getTopics();

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/sentence-game/topics');
      expect(result).toEqual(mockTopics);
    });

    it('should return array of topics', async () => {
      mockedApiClient.get.mockResolvedValue(mockTopics);

      const result = await sentenceService.getTopics();

      expect(Array.isArray(result)).toBe(true);
      expect(result.length).toBe(4);
    });

    it('should handle empty topics list', async () => {
      mockedApiClient.get.mockResolvedValue([]);

      const result = await sentenceService.getTopics();

      expect(result).toEqual([]);
    });

    it('should propagate errors from apiClient', async () => {
      const mockError = new Error('Failed to fetch topics');
      mockedApiClient.get.mockRejectedValue(mockError);

      await expect(sentenceService.getTopics()).rejects.toThrow('Failed to fetch topics');
    });

    it('should handle topics with Chinese characters', async () => {
      const chineseTopics = ['食物', '旅行', '教育', '运动'];
      mockedApiClient.get.mockResolvedValue(chineseTopics);

      const result = await sentenceService.getTopics();

      expect(result).toEqual(chineseTopics);
    });
  });
});
