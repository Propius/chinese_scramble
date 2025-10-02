import apiClient from '../api';
import idiomService, {
  IdiomQuestion,
  IdiomValidationRequest,
  IdiomValidationResponse,
  IdiomHintRequest,
  IdiomHintResponse
} from '../idiomService';
import { Difficulty } from '../../constants/difficulties';

jest.mock('../api');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

describe('idiomService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('startGame', () => {
    const mockQuestion: IdiomQuestion = {
      id: 'idiom-1',
      text: '画蛇添足',
      pinyin: 'huà shé tiān zú',
      meaning: 'Drawing a snake and adding feet',
      translation: 'To ruin something by adding unnecessary details',
      difficulty: Difficulty.MEDIUM,
      category: 'Animals',
      usageExample: '这个设计已经很完美了，不要画蛇添足',
      exampleTranslation: 'This design is already perfect, don\'t overdo it',
      characterCount: 4,
      scrambledCharacters: ['足', '画', '添', '蛇']
    };

    it('should call apiClient.get with correct URL when only difficulty is provided', async () => {
      mockedApiClient.get.mockResolvedValue(mockQuestion);

      const result = await idiomService.startGame(Difficulty.EASY);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/idiom-game/start?difficulty=EASY');
      expect(result).toEqual(mockQuestion);
    });

    it('should call apiClient.get with difficulty and playerId when both are provided', async () => {
      mockedApiClient.get.mockResolvedValue(mockQuestion);

      const result = await idiomService.startGame(Difficulty.MEDIUM, 'player-123');

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/idiom-game/start?difficulty=MEDIUM&playerId=player-123');
      expect(result).toEqual(mockQuestion);
    });

    it('should work with EASY difficulty', async () => {
      mockedApiClient.get.mockResolvedValue(mockQuestion);

      await idiomService.startGame(Difficulty.EASY);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/idiom-game/start?difficulty=EASY');
    });

    it('should work with MEDIUM difficulty', async () => {
      mockedApiClient.get.mockResolvedValue(mockQuestion);

      await idiomService.startGame(Difficulty.MEDIUM);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/idiom-game/start?difficulty=MEDIUM');
    });

    it('should work with HARD difficulty', async () => {
      mockedApiClient.get.mockResolvedValue(mockQuestion);

      await idiomService.startGame(Difficulty.HARD);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/idiom-game/start?difficulty=HARD');
    });

    it('should work with EXPERT difficulty', async () => {
      mockedApiClient.get.mockResolvedValue(mockQuestion);

      await idiomService.startGame(Difficulty.EXPERT);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/idiom-game/start?difficulty=EXPERT');
    });

    it('should propagate errors from apiClient', async () => {
      const mockError = new Error('Network error');
      mockedApiClient.get.mockRejectedValue(mockError);

      await expect(idiomService.startGame(Difficulty.EASY)).rejects.toThrow('Network error');
    });

    it('should handle playerId with special characters', async () => {
      mockedApiClient.get.mockResolvedValue(mockQuestion);

      await idiomService.startGame(Difficulty.EASY, 'player@123');

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/idiom-game/start?difficulty=EASY&playerId=player%40123');
    });
  });

  describe('validateAnswer', () => {
    const mockRequest: IdiomValidationRequest = {
      idiomId: 'idiom-1',
      userAnswer: '画蛇添足',
      timeTaken: 15000,
      hintsUsed: 1
    };

    const mockResponse: IdiomValidationResponse = {
      correct: true,
      score: 85,
      meaning: 'Drawing a snake and adding feet',
      example: '这个设计已经很完美了，不要画蛇添足',
      correctAnswer: '画蛇添足',
      explanation: 'Great job!'
    };

    it('should call apiClient.post with correct URL and payload without playerId', async () => {
      mockedApiClient.post.mockResolvedValue(mockResponse);

      const result = await idiomService.validateAnswer(mockRequest);

      expect(mockedApiClient.post).toHaveBeenCalledWith('/api/idiom-game/submit', mockRequest);
      expect(result).toEqual(mockResponse);
    });

    it('should call apiClient.post with playerId included in payload', async () => {
      mockedApiClient.post.mockResolvedValue(mockResponse);

      const result = await idiomService.validateAnswer(mockRequest, 'player-123');

      expect(mockedApiClient.post).toHaveBeenCalledWith('/api/idiom-game/submit', {
        ...mockRequest,
        playerId: 'player-123'
      });
      expect(result).toEqual(mockResponse);
    });

    it('should not modify original request object when adding playerId', async () => {
      mockedApiClient.post.mockResolvedValue(mockResponse);

      await idiomService.validateAnswer(mockRequest, 'player-123');

      expect(mockRequest).not.toHaveProperty('playerId');
    });

    it('should handle validation with zero hints used', async () => {
      const requestWithNoHints = { ...mockRequest, hintsUsed: 0 };
      mockedApiClient.post.mockResolvedValue(mockResponse);

      await idiomService.validateAnswer(requestWithNoHints);

      expect(mockedApiClient.post).toHaveBeenCalledWith('/api/idiom-game/submit', requestWithNoHints);
    });

    it('should handle validation with multiple hints used', async () => {
      const requestWithMultipleHints = { ...mockRequest, hintsUsed: 3 };
      mockedApiClient.post.mockResolvedValue(mockResponse);

      await idiomService.validateAnswer(requestWithMultipleHints);

      expect(mockedApiClient.post).toHaveBeenCalledWith('/api/idiom-game/submit', requestWithMultipleHints);
    });

    it('should propagate errors from apiClient', async () => {
      const mockError = new Error('Validation failed');
      mockedApiClient.post.mockRejectedValue(mockError);

      await expect(idiomService.validateAnswer(mockRequest)).rejects.toThrow('Validation failed');
    });

    it('should handle incorrect answer response', async () => {
      const incorrectResponse: IdiomValidationResponse = {
        correct: false,
        score: 0,
        meaning: 'Drawing a snake and adding feet',
        example: '这个设计已经很完美了，不要画蛇添足',
        correctAnswer: '画蛇添足',
        explanation: 'Try again!'
      };
      mockedApiClient.post.mockResolvedValue(incorrectResponse);

      const result = await idiomService.validateAnswer(mockRequest);

      expect(result.correct).toBe(false);
      expect(result.score).toBe(0);
    });
  });

  describe('getHint', () => {
    const mockHintRequest: IdiomHintRequest = {
      idiomId: 'idiom-1',
      hintLevel: 1
    };

    const mockHintResponse: IdiomHintResponse = {
      hint: 'First character is 画',
      pointPenalty: 10
    };

    it('should call apiClient.post with correct URL and payload without playerId', async () => {
      mockedApiClient.post.mockResolvedValue(mockHintResponse);

      const result = await idiomService.getHint(mockHintRequest);

      expect(mockedApiClient.post).toHaveBeenCalledWith('/api/idiom-game/hint', mockHintRequest);
      expect(result).toEqual(mockHintResponse);
    });

    it('should call apiClient.post with playerId included in payload', async () => {
      mockedApiClient.post.mockResolvedValue(mockHintResponse);

      const result = await idiomService.getHint(mockHintRequest, 'player-123');

      expect(mockedApiClient.post).toHaveBeenCalledWith('/api/idiom-game/hint', {
        ...mockHintRequest,
        playerId: 'player-123'
      });
      expect(result).toEqual(mockHintResponse);
    });

    it('should not modify original request object when adding playerId', async () => {
      mockedApiClient.post.mockResolvedValue(mockHintResponse);

      await idiomService.getHint(mockHintRequest, 'player-123');

      expect(mockHintRequest).not.toHaveProperty('playerId');
    });

    it('should handle hint level 1', async () => {
      const level1Request = { ...mockHintRequest, hintLevel: 1 };
      mockedApiClient.post.mockResolvedValue(mockHintResponse);

      await idiomService.getHint(level1Request);

      expect(mockedApiClient.post).toHaveBeenCalledWith('/api/idiom-game/hint', level1Request);
    });

    it('should handle hint level 2', async () => {
      const level2Request = { ...mockHintRequest, hintLevel: 2 };
      const level2Response = { hint: 'Second character is 蛇', pointPenalty: 20 };
      mockedApiClient.post.mockResolvedValue(level2Response);

      const result = await idiomService.getHint(level2Request);

      expect(result).toEqual(level2Response);
    });

    it('should handle hint level 3', async () => {
      const level3Request = { ...mockHintRequest, hintLevel: 3 };
      mockedApiClient.post.mockResolvedValue(mockHintResponse);

      await idiomService.getHint(level3Request);

      expect(mockedApiClient.post).toHaveBeenCalledWith('/api/idiom-game/hint', level3Request);
    });

    it('should propagate errors from apiClient', async () => {
      const mockError = new Error('Hint unavailable');
      mockedApiClient.post.mockRejectedValue(mockError);

      await expect(idiomService.getHint(mockHintRequest)).rejects.toThrow('Hint unavailable');
    });

    it('should return correct point penalty', async () => {
      mockedApiClient.post.mockResolvedValue(mockHintResponse);

      const result = await idiomService.getHint(mockHintRequest);

      expect(result.pointPenalty).toBe(10);
    });
  });

  describe('getCategories', () => {
    const mockCategories = ['Animals', 'Nature', 'History', 'Literature'];

    it('should call apiClient.get with correct URL', async () => {
      mockedApiClient.get.mockResolvedValue(mockCategories);

      const result = await idiomService.getCategories();

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/idiom-game/categories');
      expect(result).toEqual(mockCategories);
    });

    it('should return array of categories', async () => {
      mockedApiClient.get.mockResolvedValue(mockCategories);

      const result = await idiomService.getCategories();

      expect(Array.isArray(result)).toBe(true);
      expect(result.length).toBe(4);
    });

    it('should handle empty categories list', async () => {
      mockedApiClient.get.mockResolvedValue([]);

      const result = await idiomService.getCategories();

      expect(result).toEqual([]);
    });

    it('should propagate errors from apiClient', async () => {
      const mockError = new Error('Failed to fetch categories');
      mockedApiClient.get.mockRejectedValue(mockError);

      await expect(idiomService.getCategories()).rejects.toThrow('Failed to fetch categories');
    });

    it('should handle categories with Chinese characters', async () => {
      const chineseCategories = ['动物', '自然', '历史', '文学'];
      mockedApiClient.get.mockResolvedValue(chineseCategories);

      const result = await idiomService.getCategories();

      expect(result).toEqual(chineseCategories);
    });
  });
});
