import apiClient from './api';
import { Difficulty } from '../constants/difficulties';
import { DEFAULT_FEATURE_FLAGS } from '../constants/game.constants';

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

    // CRITICAL: Only send excludedIds if ALL conditions are met
    const shouldSendExcludedIds =
      DEFAULT_FEATURE_FLAGS.ENABLE_NO_REPEAT_QUESTIONS &&
      excludedIds !== undefined &&
      excludedIds !== null &&
      Array.isArray(excludedIds) &&
      excludedIds.length > 0;

    if (shouldSendExcludedIds) {
      params.append('excludedIds', excludedIds!.join(','));
    }

    const response = await apiClient.get<IdiomQuestion>(`/api/idiom-game/start?${params.toString()}`);
    return response;
  },

  async validateAnswer(request: IdiomValidationRequest, playerId?: string): Promise<IdiomValidationResponse> {
    const params = playerId ? `?playerId=${encodeURIComponent(playerId)}` : '';
    return apiClient.post<IdiomValidationResponse>(`/api/idiom-game/submit${params}`, request);
  },

  async getHint(request: IdiomHintRequest, playerId?: string): Promise<IdiomHintResponse> {
    const params = playerId ? `?playerId=${encodeURIComponent(playerId)}` : '';
    return apiClient.post<IdiomHintResponse>(`/api/idiom-game/hint${params}`, request);
  },

  async getCategories(): Promise<string[]> {
    return apiClient.get<string[]>('/api/idiom-game/categories');
  }
};

export default idiomService;
