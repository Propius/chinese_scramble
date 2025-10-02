import apiClient from '../api';
import leaderboardService, { LeaderboardEntry, LeaderboardFilter } from '../leaderboardService';
import { Difficulty } from '../../constants/difficulties';
import { GameType } from '../../constants/gameTypes';

jest.mock('../api');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

describe('leaderboardService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  const mockLeaderboardData: LeaderboardEntry[] = [
    {
      rank: 1,
      playerName: 'Player1',
      score: 1500,
      accuracy: 95.5,
      completedCount: 100,
      lastPlayed: '2025-10-02T10:30:00Z'
    },
    {
      rank: 2,
      playerName: 'Player2',
      score: 1400,
      accuracy: 92.3,
      completedCount: 90,
      lastPlayed: '2025-10-02T09:15:00Z'
    }
  ];

  describe('getCombinedLeaderboard', () => {
    it('should call apiClient.get with correct URL when no filter is provided', async () => {
      mockedApiClient.get.mockResolvedValue(mockLeaderboardData);

      const result = await leaderboardService.getCombinedLeaderboard();

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/leaderboard/combined');
      expect(result).toEqual(mockLeaderboardData);
    });

    it('should call apiClient.get with correct URL when empty filter is provided', async () => {
      mockedApiClient.get.mockResolvedValue(mockLeaderboardData);

      const result = await leaderboardService.getCombinedLeaderboard({});

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/leaderboard/combined');
      expect(result).toEqual(mockLeaderboardData);
    });

    it('should build URL with gameType filter only', async () => {
      mockedApiClient.get.mockResolvedValue(mockLeaderboardData);

      const filter: LeaderboardFilter = { gameType: GameType.IDIOM };
      await leaderboardService.getCombinedLeaderboard(filter);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/leaderboard/combined?gameType=IDIOM');
    });

    it('should build URL with difficulty filter only', async () => {
      mockedApiClient.get.mockResolvedValue(mockLeaderboardData);

      const filter: LeaderboardFilter = { difficulty: Difficulty.HARD };
      await leaderboardService.getCombinedLeaderboard(filter);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/leaderboard/combined?difficulty=HARD');
    });

    it('should build URL with limit filter only', async () => {
      mockedApiClient.get.mockResolvedValue(mockLeaderboardData);

      const filter: LeaderboardFilter = { limit: 20 };
      await leaderboardService.getCombinedLeaderboard(filter);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/leaderboard/combined?limit=20');
    });

    it('should build URL with all filters combined', async () => {
      mockedApiClient.get.mockResolvedValue(mockLeaderboardData);

      const filter: LeaderboardFilter = {
        gameType: GameType.SENTENCE,
        difficulty: Difficulty.MEDIUM,
        limit: 15
      };
      await leaderboardService.getCombinedLeaderboard(filter);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/leaderboard/combined?gameType=SENTENCE&difficulty=MEDIUM&limit=15');
    });

    it('should build URL with gameType and difficulty', async () => {
      mockedApiClient.get.mockResolvedValue(mockLeaderboardData);

      const filter: LeaderboardFilter = {
        gameType: GameType.IDIOM,
        difficulty: Difficulty.EXPERT
      };
      await leaderboardService.getCombinedLeaderboard(filter);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/leaderboard/combined?gameType=IDIOM&difficulty=EXPERT');
    });

    it('should build URL with gameType and limit', async () => {
      mockedApiClient.get.mockResolvedValue(mockLeaderboardData);

      const filter: LeaderboardFilter = {
        gameType: GameType.SENTENCE,
        limit: 5
      };
      await leaderboardService.getCombinedLeaderboard(filter);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/leaderboard/combined?gameType=SENTENCE&limit=5');
    });

    it('should build URL with difficulty and limit', async () => {
      mockedApiClient.get.mockResolvedValue(mockLeaderboardData);

      const filter: LeaderboardFilter = {
        difficulty: Difficulty.EASY,
        limit: 25
      };
      await leaderboardService.getCombinedLeaderboard(filter);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/leaderboard/combined?difficulty=EASY&limit=25');
    });

    it('should propagate errors from apiClient', async () => {
      const mockError = new Error('Failed to fetch leaderboard');
      mockedApiClient.get.mockRejectedValue(mockError);

      await expect(leaderboardService.getCombinedLeaderboard()).rejects.toThrow('Failed to fetch leaderboard');
    });
  });

  describe('getIdiomLeaderboard', () => {
    it('should call apiClient.get with default limit when no parameters provided', async () => {
      mockedApiClient.get.mockResolvedValue(mockLeaderboardData);

      const result = await leaderboardService.getIdiomLeaderboard();

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/leaderboard/idiom?limit=10');
      expect(result).toEqual(mockLeaderboardData);
    });

    it('should use default limit of 10 when only difficulty is provided', async () => {
      mockedApiClient.get.mockResolvedValue(mockLeaderboardData);

      await leaderboardService.getIdiomLeaderboard(Difficulty.EASY);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/leaderboard/idiom?limit=10&difficulty=EASY');
    });

    it('should build URL with custom limit', async () => {
      mockedApiClient.get.mockResolvedValue(mockLeaderboardData);

      await leaderboardService.getIdiomLeaderboard(undefined, 20);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/leaderboard/idiom?limit=20');
    });

    it('should build URL with difficulty and custom limit', async () => {
      mockedApiClient.get.mockResolvedValue(mockLeaderboardData);

      await leaderboardService.getIdiomLeaderboard(Difficulty.HARD, 15);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/leaderboard/idiom?limit=15&difficulty=HARD');
    });

    it('should work with EASY difficulty', async () => {
      mockedApiClient.get.mockResolvedValue(mockLeaderboardData);

      await leaderboardService.getIdiomLeaderboard(Difficulty.EASY);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/leaderboard/idiom?limit=10&difficulty=EASY');
    });

    it('should work with MEDIUM difficulty', async () => {
      mockedApiClient.get.mockResolvedValue(mockLeaderboardData);

      await leaderboardService.getIdiomLeaderboard(Difficulty.MEDIUM);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/leaderboard/idiom?limit=10&difficulty=MEDIUM');
    });

    it('should work with EXPERT difficulty', async () => {
      mockedApiClient.get.mockResolvedValue(mockLeaderboardData);

      await leaderboardService.getIdiomLeaderboard(Difficulty.EXPERT);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/leaderboard/idiom?limit=10&difficulty=EXPERT');
    });

    it('should propagate errors from apiClient', async () => {
      const mockError = new Error('Failed to fetch idiom leaderboard');
      mockedApiClient.get.mockRejectedValue(mockError);

      await expect(leaderboardService.getIdiomLeaderboard()).rejects.toThrow('Failed to fetch idiom leaderboard');
    });
  });

  describe('getSentenceLeaderboard', () => {
    it('should call apiClient.get with default limit when no parameters provided', async () => {
      mockedApiClient.get.mockResolvedValue(mockLeaderboardData);

      const result = await leaderboardService.getSentenceLeaderboard();

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/leaderboard/sentence?limit=10');
      expect(result).toEqual(mockLeaderboardData);
    });

    it('should use default limit of 10 when only difficulty is provided', async () => {
      mockedApiClient.get.mockResolvedValue(mockLeaderboardData);

      await leaderboardService.getSentenceLeaderboard(Difficulty.MEDIUM);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/leaderboard/sentence?limit=10&difficulty=MEDIUM');
    });

    it('should build URL with custom limit', async () => {
      mockedApiClient.get.mockResolvedValue(mockLeaderboardData);

      await leaderboardService.getSentenceLeaderboard(undefined, 25);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/leaderboard/sentence?limit=25');
    });

    it('should build URL with difficulty and custom limit', async () => {
      mockedApiClient.get.mockResolvedValue(mockLeaderboardData);

      await leaderboardService.getSentenceLeaderboard(Difficulty.EXPERT, 30);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/leaderboard/sentence?limit=30&difficulty=EXPERT');
    });

    it('should work with EASY difficulty', async () => {
      mockedApiClient.get.mockResolvedValue(mockLeaderboardData);

      await leaderboardService.getSentenceLeaderboard(Difficulty.EASY);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/leaderboard/sentence?limit=10&difficulty=EASY');
    });

    it('should work with HARD difficulty', async () => {
      mockedApiClient.get.mockResolvedValue(mockLeaderboardData);

      await leaderboardService.getSentenceLeaderboard(Difficulty.HARD);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/leaderboard/sentence?limit=10&difficulty=HARD');
    });

    it('should work with EXPERT difficulty', async () => {
      mockedApiClient.get.mockResolvedValue(mockLeaderboardData);

      await leaderboardService.getSentenceLeaderboard(Difficulty.EXPERT);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/leaderboard/sentence?limit=10&difficulty=EXPERT');
    });

    it('should propagate errors from apiClient', async () => {
      const mockError = new Error('Failed to fetch sentence leaderboard');
      mockedApiClient.get.mockRejectedValue(mockError);

      await expect(leaderboardService.getSentenceLeaderboard()).rejects.toThrow('Failed to fetch sentence leaderboard');
    });
  });
});
