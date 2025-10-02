import apiClient from './api';
import { Difficulty } from '../constants/difficulties';

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
    if (excludedIds && excludedIds.length > 0) {
      params.append('excludedIds', excludedIds.join(','));
    }
    return apiClient.get<SentenceQuestion>(`/api/sentence-game/start?${params.toString()}`);
  },

  async validateAnswer(request: SentenceValidationRequest, playerId?: string): Promise<SentenceValidationResponse> {
    const payload = playerId ? { ...request, playerId } : request;
    return apiClient.post<SentenceValidationResponse>('/api/sentence-game/submit', payload);
  },

  async getHint(request: SentenceHintRequest, playerId?: string): Promise<SentenceHintResponse> {
    const payload = playerId ? { ...request, playerId } : request;
    return apiClient.post<SentenceHintResponse>('/api/sentence-game/hint', payload);
  },

  async getTopics(): Promise<string[]> {
    return apiClient.get<string[]>('/api/sentence-game/topics');
  }
};

export default sentenceService;
