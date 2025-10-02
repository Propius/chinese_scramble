import apiClient from './api';
import { Difficulty } from '../constants/difficulties';

export interface IdiomQuestion {
  id: string;
  text: string;
  pinyin: string;
  meaning: string;
  translation: string;
  difficulty: Difficulty;
  category: string;
  usageExample: string;
  exampleTranslation: string;
  characterCount: number;
  origin?: string;
  scrambledCharacters: string[];
  correctAnswer?: string;
}

export interface IdiomValidationRequest {
  idiomId: string;
  userAnswer: string;
  timeTaken: number;
  hintsUsed: number;
}

export interface IdiomValidationResponse {
  correct: boolean;
  score: number;
  meaning: string;
  example: string;
  correctAnswer: string;
  explanation?: string;
}

export interface IdiomHintRequest {
  idiomId: string;
  hintLevel: number;
}

export interface IdiomHintResponse {
  hint: string;
  pointPenalty: number;
}

export const idiomService = {
  async startGame(difficulty: Difficulty, playerId?: string, excludedIds?: string[]): Promise<IdiomQuestion> {
    const params = new URLSearchParams({ difficulty });
    if (playerId) {
      params.append('playerId', playerId);
    }
    if (excludedIds && excludedIds.length > 0) {
      params.append('excludedIds', excludedIds.join(','));
    }
    return apiClient.get<IdiomQuestion>(`/api/idiom-game/start?${params.toString()}`);
  },

  async validateAnswer(request: IdiomValidationRequest, playerId?: string): Promise<IdiomValidationResponse> {
    const payload = playerId ? { ...request, playerId } : request;
    return apiClient.post<IdiomValidationResponse>('/api/idiom-game/submit', payload);
  },

  async getHint(request: IdiomHintRequest, playerId?: string): Promise<IdiomHintResponse> {
    const payload = playerId ? { ...request, playerId } : request;
    return apiClient.post<IdiomHintResponse>('/api/idiom-game/hint', payload);
  },

  async getCategories(): Promise<string[]> {
    return apiClient.get<string[]>('/api/idiom-game/categories');
  }
};

export default idiomService;
