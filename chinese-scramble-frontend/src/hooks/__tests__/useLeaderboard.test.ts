import { renderHook, act, waitFor } from '@testing-library/react';
import { useLeaderboard } from '../useLeaderboard';
import { Difficulty } from '../../constants/difficulties';
import apiClient from '../../services/api';

// Mock apiClient
jest.mock('../../services/api');

const mockedApiClient = apiClient as jest.Mocked<typeof apiClient>;

describe('useLeaderboard', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Initial state', () => {
    it('should have correct initial state', () => {
      const { result } = renderHook(() => useLeaderboard());

      expect(result.current.loading).toBe(false);
      expect(result.current.error).toBeNull();
    });

    it('should have all required methods', () => {
      const { result } = renderHook(() => useLeaderboard());

      expect(typeof result.current.getTopPlayers).toBe('function');
      expect(typeof result.current.getPlayerRankings).toBe('function');
      expect(typeof result.current.getPlayerRank).toBe('function');
    });
  });

  describe('getTopPlayers', () => {
    const mockLeaderboard = [
      {
        rank: 1,
        playerId: 1,
        username: 'player1',
        bestScore: 1000,
        averageScore: 850,
        totalGames: 10,
        lastPlayed: '2025-10-02T10:00:00Z',
      },
      {
        rank: 2,
        playerId: 2,
        username: 'player2',
        bestScore: 900,
        averageScore: 750,
        totalGames: 8,
        lastPlayed: '2025-10-02T09:00:00Z',
      },
    ];

    it('should fetch top players successfully for IDIOM game', async () => {
      mockedApiClient.get.mockResolvedValue(mockLeaderboard);

      const { result } = renderHook(() => useLeaderboard());

      let data;
      await act(async () => {
        data = await result.current.getTopPlayers('IDIOM', Difficulty.EASY, 10);
      });

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/leaderboards/top', {
        params: {
          gameType: 'IDIOM',
          difficulty: 'EASY',
          limit: 10,
        },
      });
      expect(data).toEqual(mockLeaderboard);
      expect(result.current.loading).toBe(false);
      expect(result.current.error).toBeNull();
    });

    it('should fetch top players successfully for SENTENCE game', async () => {
      mockedApiClient.get.mockResolvedValue(mockLeaderboard);

      const { result } = renderHook(() => useLeaderboard());

      await act(async () => {
        await result.current.getTopPlayers('SENTENCE', Difficulty.MEDIUM, 10);
      });

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/leaderboards/top', {
        params: {
          gameType: 'SENTENCE',
          difficulty: 'MEDIUM',
          limit: 10,
        },
      });
    });

    it('should handle different difficulty levels', async () => {
      mockedApiClient.get.mockResolvedValue(mockLeaderboard);

      const { result } = renderHook(() => useLeaderboard());

      await act(async () => {
        await result.current.getTopPlayers('IDIOM', Difficulty.HARD, 10);
      });

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/leaderboards/top', {
        params: {
          gameType: 'IDIOM',
          difficulty: 'HARD',
          limit: 10,
        },
      });
    });

    it('should use default limit of 10 when not specified', async () => {
      mockedApiClient.get.mockResolvedValue(mockLeaderboard);

      const { result } = renderHook(() => useLeaderboard());

      await act(async () => {
        await result.current.getTopPlayers('IDIOM', Difficulty.EASY);
      });

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/leaderboards/top', {
        params: {
          gameType: 'IDIOM',
          difficulty: 'EASY',
          limit: 10,
        },
      });
    });

    it('should handle custom limit values', async () => {
      mockedApiClient.get.mockResolvedValue(mockLeaderboard.slice(0, 5));

      const { result } = renderHook(() => useLeaderboard());

      await act(async () => {
        await result.current.getTopPlayers('IDIOM', Difficulty.EASY, 5);
      });

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/leaderboards/top', {
        params: {
          gameType: 'IDIOM',
          difficulty: 'EASY',
          limit: 5,
        },
      });
    });

    it('should handle loading state', async () => {
      mockedApiClient.get.mockImplementation(
        () => new Promise((resolve) => setTimeout(() => resolve(mockLeaderboard), 100))
      );

      const { result } = renderHook(() => useLeaderboard());

      act(() => {
        result.current.getTopPlayers('IDIOM', Difficulty.EASY, 10);
      });

      expect(result.current.loading).toBe(true);

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });
    });

    it('should handle error with response data', async () => {
      const error = {
        response: {
          data: {
            error: 'Failed to fetch leaderboard data',
          },
        },
      };
      mockedApiClient.get.mockRejectedValue(error);

      const { result } = renderHook(() => useLeaderboard());

      await act(async () => {
        try {
          await result.current.getTopPlayers('IDIOM', Difficulty.EASY, 10);
        } catch (err) {
          // Expected error
        }
      });

      expect(result.current.error).toBe('Failed to fetch leaderboard data');
      expect(result.current.loading).toBe(false);
    });

    it('should handle error without response data', async () => {
      const error = new Error('Network error');
      mockedApiClient.get.mockRejectedValue(error);

      const { result } = renderHook(() => useLeaderboard());

      await act(async () => {
        try {
          await result.current.getTopPlayers('IDIOM', Difficulty.EASY, 10);
        } catch (err) {
          // Expected error
        }
      });

      expect(result.current.error).toBe('Failed to fetch leaderboard');
      expect(result.current.loading).toBe(false);
    });
  });

  describe('getPlayerRankings', () => {
    const mockPlayerRankings = [
      {
        gameType: 'IDIOM',
        difficulty: 'EASY',
        rank: 5,
        bestScore: 800,
        averageScore: 650,
        totalGames: 15,
      },
      {
        gameType: 'SENTENCE',
        difficulty: 'MEDIUM',
        rank: 3,
        bestScore: 900,
        averageScore: 750,
        totalGames: 10,
      },
    ];

    it('should fetch player rankings successfully with string playerId', async () => {
      mockedApiClient.get.mockResolvedValue(mockPlayerRankings);

      const { result } = renderHook(() => useLeaderboard());

      let data;
      await act(async () => {
        data = await result.current.getPlayerRankings('player123');
      });

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/leaderboards/player/player123');
      expect(data).toEqual(mockPlayerRankings);
      expect(result.current.loading).toBe(false);
      expect(result.current.error).toBeNull();
    });

    it('should fetch player rankings successfully with numeric playerId', async () => {
      mockedApiClient.get.mockResolvedValue(mockPlayerRankings);

      const { result } = renderHook(() => useLeaderboard());

      await act(async () => {
        await result.current.getPlayerRankings(123);
      });

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/leaderboards/player/123');
    });

    it('should handle URL encoding for playerId with special characters', async () => {
      mockedApiClient.get.mockResolvedValue(mockPlayerRankings);

      const { result } = renderHook(() => useLeaderboard());

      await act(async () => {
        await result.current.getPlayerRankings('player@123');
      });

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/leaderboards/player/player%40123');
    });

    it('should handle loading state', async () => {
      mockedApiClient.get.mockImplementation(
        () => new Promise((resolve) => setTimeout(() => resolve(mockPlayerRankings), 100))
      );

      const { result } = renderHook(() => useLeaderboard());

      act(() => {
        result.current.getPlayerRankings('player123');
      });

      expect(result.current.loading).toBe(true);

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });
    });

    it('should handle error with response data', async () => {
      const error = {
        response: {
          data: {
            error: 'Player not found',
          },
        },
      };
      mockedApiClient.get.mockRejectedValue(error);

      const { result } = renderHook(() => useLeaderboard());

      await act(async () => {
        try {
          await result.current.getPlayerRankings('player123');
        } catch (err) {
          // Expected error
        }
      });

      expect(result.current.error).toBe('Player not found');
      expect(result.current.loading).toBe(false);
    });

    it('should handle error without response data', async () => {
      const error = new Error('Network error');
      mockedApiClient.get.mockRejectedValue(error);

      const { result } = renderHook(() => useLeaderboard());

      await act(async () => {
        try {
          await result.current.getPlayerRankings('player123');
        } catch (err) {
          // Expected error
        }
      });

      expect(result.current.error).toBe('Failed to fetch player rankings');
      expect(result.current.loading).toBe(false);
    });
  });

  describe('getPlayerRank', () => {
    const mockPlayerRank = {
      rank: 5,
      totalPlayers: 100,
      percentile: 95,
    };

    it('should fetch player rank successfully for IDIOM game', async () => {
      mockedApiClient.get.mockResolvedValue(mockPlayerRank);

      const { result } = renderHook(() => useLeaderboard());

      let data;
      await act(async () => {
        data = await result.current.getPlayerRank('player123', 'IDIOM', Difficulty.EASY);
      });

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/leaderboards/player/player123/rank', {
        params: {
          gameType: 'IDIOM',
          difficulty: 'EASY',
        },
      });
      expect(data).toEqual(mockPlayerRank);
      expect(result.current.loading).toBe(false);
      expect(result.current.error).toBeNull();
    });

    it('should fetch player rank successfully for SENTENCE game', async () => {
      mockedApiClient.get.mockResolvedValue(mockPlayerRank);

      const { result } = renderHook(() => useLeaderboard());

      await act(async () => {
        await result.current.getPlayerRank('player123', 'SENTENCE', Difficulty.MEDIUM);
      });

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/leaderboards/player/player123/rank', {
        params: {
          gameType: 'SENTENCE',
          difficulty: 'MEDIUM',
        },
      });
    });

    it('should handle numeric playerId', async () => {
      mockedApiClient.get.mockResolvedValue(mockPlayerRank);

      const { result } = renderHook(() => useLeaderboard());

      await act(async () => {
        await result.current.getPlayerRank(123, 'IDIOM', Difficulty.HARD);
      });

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/leaderboards/player/123/rank', {
        params: {
          gameType: 'IDIOM',
          difficulty: 'HARD',
        },
      });
    });

    it('should handle URL encoding for playerId with special characters', async () => {
      mockedApiClient.get.mockResolvedValue(mockPlayerRank);

      const { result } = renderHook(() => useLeaderboard());

      await act(async () => {
        await result.current.getPlayerRank('player@123', 'IDIOM', Difficulty.EASY);
      });

      expect(mockedApiClient.get).toHaveBeenCalledWith('/api/leaderboards/player/player%40123/rank', {
        params: {
          gameType: 'IDIOM',
          difficulty: 'EASY',
        },
      });
    });

    it('should handle loading state', async () => {
      mockedApiClient.get.mockImplementation(
        () => new Promise((resolve) => setTimeout(() => resolve(mockPlayerRank), 100))
      );

      const { result } = renderHook(() => useLeaderboard());

      act(() => {
        result.current.getPlayerRank('player123', 'IDIOM', Difficulty.EASY);
      });

      expect(result.current.loading).toBe(true);

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });
    });

    it('should handle error with response data', async () => {
      const error = {
        response: {
          data: {
            error: 'Player rank not found',
          },
        },
      };
      mockedApiClient.get.mockRejectedValue(error);

      const { result } = renderHook(() => useLeaderboard());

      await act(async () => {
        try {
          await result.current.getPlayerRank('player123', 'IDIOM', Difficulty.EASY);
        } catch (err) {
          // Expected error
        }
      });

      expect(result.current.error).toBe('Player rank not found');
      expect(result.current.loading).toBe(false);
    });

    it('should handle error without response data', async () => {
      const error = new Error('Network error');
      mockedApiClient.get.mockRejectedValue(error);

      const { result } = renderHook(() => useLeaderboard());

      await act(async () => {
        try {
          await result.current.getPlayerRank('player123', 'IDIOM', Difficulty.EASY);
        } catch (err) {
          // Expected error
        }
      });

      expect(result.current.error).toBe('Failed to fetch player rank');
      expect(result.current.loading).toBe(false);
    });
  });

  describe('Error state reset', () => {
    it('should reset error state when making new getTopPlayers request', async () => {
      const mockLeaderboard = [
        {
          rank: 1,
          playerId: 1,
          username: 'player1',
          bestScore: 1000,
          averageScore: 850,
          totalGames: 10,
          lastPlayed: '2025-10-02T10:00:00Z',
        },
      ];

      // First request fails
      mockedApiClient.get.mockRejectedValueOnce(new Error('Network error'));

      const { result } = renderHook(() => useLeaderboard());

      await act(async () => {
        try {
          await result.current.getTopPlayers('IDIOM', Difficulty.EASY, 10);
        } catch (err) {
          // Expected error
        }
      });

      expect(result.current.error).not.toBeNull();

      // Second request succeeds
      mockedApiClient.get.mockResolvedValueOnce(mockLeaderboard);

      await act(async () => {
        await result.current.getTopPlayers('IDIOM', Difficulty.EASY, 10);
      });

      expect(result.current.error).toBeNull();
    });
  });
});
