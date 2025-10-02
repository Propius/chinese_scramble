import { useState } from 'react';
import apiClient from '../services/api';
import { Difficulty } from '../constants/difficulties';

export interface LeaderboardEntry {
  rank: number;
  playerId: number;
  username: string;
  bestScore: number;
  averageScore: number;
  totalGames: number;
  lastPlayed: string;
}

export interface PlayerRanking {
  gameType: string;
  difficulty: string;
  rank: number;
  bestScore: number;
  averageScore: number;
  totalGames: number;
}

export const useLeaderboard = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const getTopPlayers = async (
    gameType: 'IDIOM' | 'SENTENCE',
    difficulty: Difficulty,
    limit: number = 10
  ): Promise<LeaderboardEntry[]> => {
    setLoading(true);
    setError(null);

    try {
      const data = await apiClient.get<LeaderboardEntry[]>('/api/leaderboards/top', {
        params: {
          gameType,
          difficulty: difficulty.toUpperCase(),
          limit,
        },
      });

      setLoading(false);
      return data;
    } catch (err: any) {
      const errorMsg = err.response?.data?.error || 'Failed to fetch leaderboard';
      setError(errorMsg);
      setLoading(false);
      throw err;
    }
  };

  const getPlayerRankings = async (playerId: string | number): Promise<PlayerRanking[]> => {
    setLoading(true);
    setError(null);

    try {
      const data = await apiClient.get<PlayerRanking[]>(`/api/leaderboards/player/${encodeURIComponent(playerId)}`);
      setLoading(false);
      return data;
    } catch (err: any) {
      const errorMsg = err.response?.data?.error || 'Failed to fetch player rankings';
      setError(errorMsg);
      setLoading(false);
      throw err;
    }
  };

  const getPlayerRank = async (
    playerId: string | number,
    gameType: 'IDIOM' | 'SENTENCE',
    difficulty: Difficulty
  ) => {
    setLoading(true);
    setError(null);

    try {
      const data = await apiClient.get(`/api/leaderboards/player/${encodeURIComponent(playerId)}/rank`, {
        params: {
          gameType,
          difficulty: difficulty.toUpperCase(),
        },
      });

      setLoading(false);
      return data;
    } catch (err: any) {
      const errorMsg = err.response?.data?.error || 'Failed to fetch player rank';
      setError(errorMsg);
      setLoading(false);
      throw err;
    }
  };

  return {
    loading,
    error,
    getTopPlayers,
    getPlayerRankings,
    getPlayerRank,
  };
};
