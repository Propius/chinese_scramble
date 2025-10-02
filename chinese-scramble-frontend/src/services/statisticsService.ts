import apiClient from './api';
import { GameType } from '../constants/gameTypes';

export interface PlayerStatistics {
  playerName: string;
  totalScore: number;
  gamesPlayed: number;
  averageAccuracy: number;
  idiomStats: GameStatistics;
  sentenceStats: GameStatistics;
  achievements: Achievement[];
}

export interface GameStatistics {
  totalScore: number;
  gamesPlayed: number;
  averageAccuracy: number;
  averageTime: number;
  bestScore: number;
  easyCompleted: number;
  mediumCompleted: number;
  hardCompleted: number;
  expertCompleted: number;
}

export interface Achievement {
  id: string;
  achievementType: string;
  achievementName: string;
  description: string;
  achievedAt: string;
}

export interface ProgressData {
  date: string;
  score: number;
  accuracy: number;
  gamesPlayed: number;
}

export const statisticsService = {
  async getPlayerStatistics(playerName: string): Promise<PlayerStatistics> {
    return apiClient.get<PlayerStatistics>(`/api/statistics/player/${encodeURIComponent(playerName)}`);
  },

  async getIdiomStatistics(playerName: string): Promise<GameStatistics> {
    return apiClient.get<GameStatistics>(`/api/statistics/idiom/${encodeURIComponent(playerName)}`);
  },

  async getSentenceStatistics(playerName: string): Promise<GameStatistics> {
    return apiClient.get<GameStatistics>(`/api/statistics/sentence/${encodeURIComponent(playerName)}`);
  },

  async getAchievements(playerName: string): Promise<Achievement[]> {
    return apiClient.get<Achievement[]>(`/api/statistics/achievements/${encodeURIComponent(playerName)}`);
  },

  async getProgressData(playerName: string, gameType?: GameType, days: number = 30): Promise<ProgressData[]> {
    const params = new URLSearchParams({ days: days.toString() });
    if (gameType) params.append('gameType', gameType);

    return apiClient.get<ProgressData[]>(`/api/statistics/progress/${encodeURIComponent(playerName)}?${params.toString()}`);
  }
};

export default statisticsService;
