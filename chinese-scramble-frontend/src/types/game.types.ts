export type DifficultyLevel = 'EASY' | 'MEDIUM' | 'HARD' | 'EXPERT';
export type GameStatus = 'idle' | 'playing' | 'paused' | 'completed' | 'timeout';
export type GameType = 'IDIOM' | 'SENTENCE';
export type HintLevel = 1 | 2 | 3;
export type AudioSpeed = 'slow' | 'normal' | 'fast';

export interface Idiom {
  id: string;
  text: string;
  pinyin: string;
  meaning: string;
  translation: string;
  difficulty: DifficultyLevel;
  category: string;
  usageExample: string;
  exampleTranslation: string;
  characterCount: number;
  commonUsage: boolean;
  origin?: string;
}

export interface Sentence {
  id: string;
  text: string;
  pinyin: string;
  translation: string;
  difficulty: DifficultyLevel;
  words: string[];
  wordCount: number;
  grammarPattern: string;
  grammarExplanation: string;
  topic: string;
  vocabularyLevel: string;
}

export interface GameScore {
  basePoints: number;
  timeBonus: number;
  difficultyMultiplier: number;
  hintPenalty: number;
  totalScore: number;
}

export interface LeaderboardEntry {
  playerName: string;
  score: number;
  difficulty: DifficultyLevel;
  accuracy: number;
  completedCount: number;
  rank: number;
}

export interface Achievement {
  id: string;
  playerName: string;
  achievementType: string;
  achievementName: string;
  description: string;
  achievedAt: Date;
}

export interface GameSession {
  id: string;
  playerName: string;
  gameType: GameType;
  difficulty: DifficultyLevel;
  practiceMode: boolean;
  startedAt: Date;
  endedAt?: Date;
  questionsCompleted: number;
  totalScore: number;
}

export interface GameStatistics {
  totalScore: number;
  gamesPlayed: number;
  accuracy: number;
  averageTime: number;
  bestScore: number;
}

export interface ValidationResult {
  correct: boolean;
  score: number;
  meaning?: string;
  example?: string;
  translation?: string;
  grammarExplanation?: string;
  errors?: string[];
}

export interface HintResponse {
  hint: string;
  pointPenalty: number;
  hintLevel: HintLevel;
}

export interface FeatureFlags {
  ENABLE_IDIOM_SCRAMBLE: boolean;
  ENABLE_SENTENCE_CRAFTING: boolean;
  ENABLE_LEADERBOARD: boolean;
  ENABLE_AUDIO_PRONUNCIATION: boolean;
  ENABLE_HINTS: boolean;
  ENABLE_PRACTICE_MODE: boolean;
  ENABLE_ACHIEVEMENTS: boolean;
  ENABLE_NO_REPEAT_QUESTIONS: boolean;
}

export interface AudioSettings {
  volume: number;
  speed: AudioSpeed;
  showPinyin: boolean;
  enabled: boolean;
}

export interface GamePreferences {
  practiceMode: boolean;
  hintsEnabled: boolean;
  timerEnabled: boolean;
  defaultDifficulty: DifficultyLevel;
}

export interface DifficultySettings {
  minCharacters?: number;
  maxCharacters?: number;
  minWords?: number;
  maxWords?: number;
  timeLimit: number;
  basePoints: number;
  timeBonus: number;
}

export interface ScoreSubmission {
  playerName: string;
  score: number;
  difficulty: DifficultyLevel;
  accuracy: number;
  completedCount: number;
  timeTaken: number;
  hintsUsed: number;
}