import apiClient from './api';
import { Difficulty } from '../constants/difficulties';
import { GameType } from '../constants/gameTypes';

export interface LeaderboardEntry {
  rank: number;
  playerName: string;
  score: number;
  accuracy: number;
  completedCount: number;
  lastPlayed: string;
}

export interface LeaderboardFilter {
  gameType?: GameType;
  difficulty?: Difficulty;
  limit?: number;
}

export const leaderboardService = {
  async getCombinedLeaderboard(filter: LeaderboardFilter = {}): Promise<LeaderboardEntry[]> {
    const params = new URLSearchParams();
    if (filter.gameType) params.append('gameType', filter.gameType);
    if (filter.difficulty) params.append('difficulty', filter.difficulty);
    if (filter.limit) params.append('limit', filter.limit.toString());

    const queryString = params.toString();
    const url = `/api/leaderboard/combined${queryString ? `?${queryString}` : ''}`;
    return apiClient.get<LeaderboardEntry[]>(url);
  },

  async getIdiomLeaderboard(difficulty?: Difficulty, limit: number = 10): Promise<LeaderboardEntry[]> {
    const params = new URLSearchParams({ limit: limit.toString() });
    if (difficulty) params.append('difficulty', difficulty);

    return apiClient.get<LeaderboardEntry[]>(`/api/leaderboard/idiom?${params.toString()}`);
  },

  async getSentenceLeaderboard(difficulty?: Difficulty, limit: number = 10): Promise<LeaderboardEntry[]> {
    const params = new URLSearchParams({ limit: limit.toString() });
    if (difficulty) params.append('difficulty', difficulty);

    return apiClient.get<LeaderboardEntry[]>(`/api/leaderboard/sentence?${params.toString()}`);
  }
};

export default leaderboardService;
