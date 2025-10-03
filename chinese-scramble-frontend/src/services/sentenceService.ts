import apiClient from './api';
import { Difficulty } from '../constants/difficulties';
import { DEFAULT_FEATURE_FLAGS } from '../constants/game.constants';

export interface SentenceQuestion {
  id: string;
  text: string;
  pinyin: string;
  translation: string;
  difficulty: Difficulty;
  words: string[];
  wordCount: number;
  grammarPattern: string;
  grammarExplanation: string;
  topic: string;
  vocabularyLevel: string;
  scrambledWords: string[];
  correctAnswer?: string[];
}

export interface SentenceValidationRequest {
  sentenceId: string;
  userAnswer: string[];
  timeTaken: number;
  hintsUsed: number;
}

export interface SentenceValidationResponse {
  correct: boolean;
  score: number;
  translation: string;
  grammarExplanation: string;
  correctAnswer: string[];
  feedback?: string;
}

export interface SentenceHintRequest {
  sentenceId: string;
  hintLevel: number;
}

export interface SentenceHintResponse {
  hint: string;
  pointPenalty: number;
}

export const sentenceService = {
  async startGame(difficulty: Difficulty, playerId?: string, excludedIds?: string[]): Promise<SentenceQuestion> {
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

    const response = await apiClient.get<SentenceQuestion>(`/api/sentence-game/start?${params.toString()}`);
    return response;
  },

  async validateAnswer(request: SentenceValidationRequest, playerId?: string): Promise<SentenceValidationResponse> {
    const params = playerId ? `?playerId=${encodeURIComponent(playerId)}` : '';
    return apiClient.post<SentenceValidationResponse>(`/api/sentence-game/submit${params}`, request);
  },

  async getHint(request: SentenceHintRequest, playerId?: string): Promise<SentenceHintResponse> {
    const params = playerId ? `?playerId=${encodeURIComponent(playerId)}` : '';
    return apiClient.post<SentenceHintResponse>(`/api/sentence-game/hint${params}`, request);
  },

  async getTopics(): Promise<string[]> {
    return apiClient.get<string[]>('/api/sentence-game/topics');
  }
};

export default sentenceService;
