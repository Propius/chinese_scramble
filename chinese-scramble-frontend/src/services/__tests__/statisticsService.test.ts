import apiClient from '../api';
import statisticsService, {
  PlayerStatistics,
  GameStatistics,
  Achievement,
  ProgressData
} from '../statisticsService';
import { GameType } from '../../constants/gameTypes';

jest.mock('../api');
const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

describe('statisticsService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  const mockGameStatistics: GameStatistics = {
    totalScore: 2500,
    gamesPlayed: 50,
    averageAccuracy: 92.5,
    averageTime: 45.2,
    bestScore: 150,
    easyCompleted: 15,
    mediumCompleted: 20,
    hardCompleted: 10,
    expertCompleted: 5
  };

  const mockPlayerStatistics: PlayerStatistics = {
    playerName: 'TestPlayer',
    totalScore: 5000,
    gamesPlayed: 100,
    averageAccuracy: 90.0,
    idiomStats: mockGameStatistics,
    sentenceStats: mockGameStatistics,
    achievements: []
  };

  const mockAchievements: Achievement[] = [
    {
      id: 'ach-1',
      achievementType: 'FIRST_WIN',
      achievementName: 'First Victory',
      description: 'Win your first game',
      achievedAt: '2025-10-01T12:00:00Z'
    },
    {
      id: 'ach-2',
      achievementType: 'SCORE_100',
      achievementName: 'Century',
      description: 'Score 100 points in a single game',
      achievedAt: '2025-10-02T14:30:00Z'
    }
  ];

  const mockProgressData: ProgressData[] = [
    {
      date: '2025-10-01',
      score: 150,
      accuracy: 95.0,
      gamesPlayed: 5
    },
    {
      date: '2025-10-02',
      score: 180,
      accuracy: 97.5,
      gamesPlayed: 6
    }
  ];

  describe('getPlayerStatistics', () => {
    it('should call apiClient.get with correct URL for simple player name', async () => {
      mockedApiClient.get.mockResolvedValue(mockPlayerStatistics);

      const result = await statisticsService.getPlayerStatistics('TestPlayer');

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/statistics/player/TestPlayer');
      expect(result).toEqual(mockPlayerStatistics);
    });

    it('should URL encode player name with spaces', async () => {
      mockedApiClient.get.mockResolvedValue(mockPlayerStatistics);

      await statisticsService.getPlayerStatistics('Test Player');

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/statistics/player/Test%20Player');
    });

    it('should URL encode player name with special characters', async () => {
      mockedApiClient.get.mockResolvedValue(mockPlayerStatistics);

      await statisticsService.getPlayerStatistics('Player@123');

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/statistics/player/Player%40123');
    });

    it('should URL encode player name with Chinese characters', async () => {
      mockedApiClient.get.mockResolvedValue(mockPlayerStatistics);

      await statisticsService.getPlayerStatistics('玩家一');

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/statistics/player/%E7%8E%A9%E5%AE%B6%E4%B8%80');
    });

    it('should URL encode player name with symbols', async () => {
      mockedApiClient.get.mockResolvedValue(mockPlayerStatistics);

      await statisticsService.getPlayerStatistics('Player#1&Test');

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/statistics/player/Player%231%26Test');
    });

    it('should propagate errors from apiClient', async () => {
      const mockError = new Error('Player not found');
      mockedApiClient.get.mockRejectedValue(mockError);

      await expect(statisticsService.getPlayerStatistics('TestPlayer')).rejects.toThrow('Player not found');
    });
  });

  describe('getIdiomStatistics', () => {
    it('should call apiClient.get with correct URL for simple player name', async () => {
      mockedApiClient.get.mockResolvedValue(mockGameStatistics);

      const result = await statisticsService.getIdiomStatistics('TestPlayer');

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/statistics/idiom/TestPlayer');
      expect(result).toEqual(mockGameStatistics);
    });

    it('should URL encode player name with special characters', async () => {
      mockedApiClient.get.mockResolvedValue(mockGameStatistics);

      await statisticsService.getIdiomStatistics('Player@456');

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/statistics/idiom/Player%40456');
    });

    it('should URL encode player name with spaces', async () => {
      mockedApiClient.get.mockResolvedValue(mockGameStatistics);

      await statisticsService.getIdiomStatistics('Test Player Name');

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/statistics/idiom/Test%20Player%20Name');
    });

    it('should propagate errors from apiClient', async () => {
      const mockError = new Error('Statistics not found');
      mockedApiClient.get.mockRejectedValue(mockError);

      await expect(statisticsService.getIdiomStatistics('TestPlayer')).rejects.toThrow('Statistics not found');
    });
  });

  describe('getSentenceStatistics', () => {
    it('should call apiClient.get with correct URL for simple player name', async () => {
      mockedApiClient.get.mockResolvedValue(mockGameStatistics);

      const result = await statisticsService.getSentenceStatistics('TestPlayer');

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/statistics/sentence/TestPlayer');
      expect(result).toEqual(mockGameStatistics);
    });

    it('should URL encode player name with special characters', async () => {
      mockedApiClient.get.mockResolvedValue(mockGameStatistics);

      await statisticsService.getSentenceStatistics('Player#789');

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/statistics/sentence/Player%23789');
    });

    it('should URL encode player name with spaces', async () => {
      mockedApiClient.get.mockResolvedValue(mockGameStatistics);

      await statisticsService.getSentenceStatistics('Another Test Player');

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/statistics/sentence/Another%20Test%20Player');
    });

    it('should propagate errors from apiClient', async () => {
      const mockError = new Error('Statistics not available');
      mockedApiClient.get.mockRejectedValue(mockError);

      await expect(statisticsService.getSentenceStatistics('TestPlayer')).rejects.toThrow('Statistics not available');
    });
  });

  describe('getAchievements', () => {
    it('should call apiClient.get with correct URL for simple player name', async () => {
      mockedApiClient.get.mockResolvedValue(mockAchievements);

      const result = await statisticsService.getAchievements('TestPlayer');

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/statistics/achievements/TestPlayer');
      expect(result).toEqual(mockAchievements);
    });

    it('should URL encode player name with special characters', async () => {
      mockedApiClient.get.mockResolvedValue(mockAchievements);

      await statisticsService.getAchievements('Player/123');

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/statistics/achievements/Player%2F123');
    });

    it('should URL encode player name with spaces', async () => {
      mockedApiClient.get.mockResolvedValue(mockAchievements);

      await statisticsService.getAchievements('Pro Player');

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/statistics/achievements/Pro%20Player');
    });

    it('should return empty array when player has no achievements', async () => {
      mockedApiClient.get.mockResolvedValue([]);

      const result = await statisticsService.getAchievements('NewPlayer');

      expect(result).toEqual([]);
    });

    it('should propagate errors from apiClient', async () => {
      const mockError = new Error('Failed to fetch achievements');
      mockedApiClient.get.mockRejectedValue(mockError);

      await expect(statisticsService.getAchievements('TestPlayer')).rejects.toThrow('Failed to fetch achievements');
    });
  });

  describe('getProgressData', () => {
    it('should call apiClient.get with default days when only player name is provided', async () => {
      mockedApiClient.get.mockResolvedValue(mockProgressData);

      const result = await statisticsService.getProgressData('TestPlayer');

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/statistics/progress/TestPlayer?days=30');
      expect(result).toEqual(mockProgressData);
    });

    it('should build URL with custom days parameter', async () => {
      mockedApiClient.get.mockResolvedValue(mockProgressData);

      await statisticsService.getProgressData('TestPlayer', undefined, 7);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/statistics/progress/TestPlayer?days=7');
    });

    it('should build URL with gameType parameter', async () => {
      mockedApiClient.get.mockResolvedValue(mockProgressData);

      await statisticsService.getProgressData('TestPlayer', GameType.IDIOM);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/statistics/progress/TestPlayer?days=30&gameType=IDIOM');
    });

    it('should build URL with all parameters', async () => {
      mockedApiClient.get.mockResolvedValue(mockProgressData);

      await statisticsService.getProgressData('TestPlayer', GameType.SENTENCE, 14);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/statistics/progress/TestPlayer?days=14&gameType=SENTENCE');
    });

    it('should work with IDIOM game type', async () => {
      mockedApiClient.get.mockResolvedValue(mockProgressData);

      await statisticsService.getProgressData('TestPlayer', GameType.IDIOM, 30);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/statistics/progress/TestPlayer?days=30&gameType=IDIOM');
    });

    it('should work with SENTENCE game type', async () => {
      mockedApiClient.get.mockResolvedValue(mockProgressData);

      await statisticsService.getProgressData('TestPlayer', GameType.SENTENCE, 60);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/statistics/progress/TestPlayer?days=60&gameType=SENTENCE');
    });

    it('should URL encode player name with spaces', async () => {
      mockedApiClient.get.mockResolvedValue(mockProgressData);

      await statisticsService.getProgressData('Test Player');

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/statistics/progress/Test%20Player?days=30');
    });

    it('should URL encode player name with special characters', async () => {
      mockedApiClient.get.mockResolvedValue(mockProgressData);

      await statisticsService.getProgressData('Player&Test', GameType.IDIOM, 15);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/statistics/progress/Player%26Test?days=15&gameType=IDIOM');
    });

    it('should handle custom days values', async () => {
      mockedApiClient.get.mockResolvedValue(mockProgressData);

      await statisticsService.getProgressData('TestPlayer', undefined, 90);

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/statistics/progress/TestPlayer?days=90');
    });

    it('should return empty array for new players', async () => {
      mockedApiClient.get.mockResolvedValue([]);

      const result = await statisticsService.getProgressData('NewPlayer');

      expect(result).toEqual([]);
    });

    it('should propagate errors from apiClient', async () => {
      const mockError = new Error('Failed to fetch progress data');
      mockedApiClient.get.mockRejectedValue(mockError);

      await expect(statisticsService.getProgressData('TestPlayer')).rejects.toThrow('Failed to fetch progress data');
    });
  });
});
