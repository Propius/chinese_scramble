import { DifficultySettings, FeatureFlags } from '../types/game.types';

export const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

export const DEFAULT_FEATURE_FLAGS: FeatureFlags = {
  ENABLE_IDIOM_SCRAMBLE: true,
  ENABLE_SENTENCE_CRAFTING: true,
  ENABLE_LEADERBOARD: true,
  ENABLE_AUDIO_PRONUNCIATION: true,
  ENABLE_HINTS: true,
  ENABLE_PRACTICE_MODE: true,
  ENABLE_ACHIEVEMENTS: true,
  ENABLE_NO_REPEAT_QUESTIONS: false
  ,
};

export const DIFFICULTY_SETTINGS: Record<string, DifficultySettings> = {
  EASY: {
    minCharacters: 4,
    maxCharacters: 4,
    timeLimit: 180,
    basePoints: 100,
    timeBonus: 50,
  },
  MEDIUM: {
    minCharacters: 4,
    maxCharacters: 4,
    timeLimit: 120,
    basePoints: 200,
    timeBonus: 100,
  },
  HARD: {
    minCharacters: 4,
    maxCharacters: 6,
    timeLimit: 90,
    basePoints: 300,
    timeBonus: 150,
  },
  EXPERT: {
    minCharacters: 6,
    maxCharacters: 8,
    timeLimit: 60,
    basePoints: 500,
    timeBonus: 250,
  },
};

export const SENTENCE_DIFFICULTY_SETTINGS: Record<string, DifficultySettings> = {
  EASY: {
    minWords: 5,
    maxWords: 8,
    timeLimit: 180,
    basePoints: 100,
    timeBonus: 50,
  },
  MEDIUM: {
    minWords: 8,
    maxWords: 12,
    timeLimit: 120,
    basePoints: 200,
    timeBonus: 100,
  },
  HARD: {
    minWords: 12,
    maxWords: 15,
    timeLimit: 90,
    basePoints: 300,
    timeBonus: 150,
  },
  EXPERT: {
    minWords: 15,
    maxWords: 20,
    timeLimit: 60,
    basePoints: 500,
    timeBonus: 250,
  },
};

export const HINT_PENALTIES = {
  LEVEL1: 10,
  LEVEL2: 20,
  LEVEL3: 30,
};

export const MAX_HINTS_PER_QUESTION = 3;

export const DIFFICULTY_MULTIPLIERS = {
  EASY: 1.0,
  MEDIUM: 1.5,
  HARD: 2.0,
  EXPERT: 3.0,
};

export const AUDIO_SPEEDS = {
  slow: 0.7,
  normal: 1.0,
  fast: 1.3,
};

export const ROUTES = {
  HOME: '/',
  IDIOM_GAME: '/idiom',
  SENTENCE_GAME: '/sentence',
  LEADERBOARD: '/leaderboard',
  STATISTICS: '/statistics',
  SETTINGS: '/settings',
};

export const LEADERBOARD_TOP_COUNT = 10;

export const PERFORMANCE_TARGETS = {
  LCP: 2500,
  FID: 100,
  CLS: 0.1,
  FONT_LOAD_TIME: 500,
  DRAG_FPS: 60,
};

export const STORAGE_KEYS = {
  PLAYER_NAME: 'word_scramble_player_name',
  AUDIO_SETTINGS: 'word_scramble_audio_settings',
  GAME_PREFERENCES: 'word_scramble_game_preferences',
  FEATURE_FLAGS: 'word_scramble_feature_flags',
};

// Maximum number of excluded question IDs to send to backend
// Limits URL length to prevent performance issues
// Trade-off: Questions may repeat after playing many games
export const MAX_EXCLUDED_QUESTIONS = 50;