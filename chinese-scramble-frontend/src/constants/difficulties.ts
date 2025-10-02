export enum Difficulty {
  EASY = 'EASY',
  MEDIUM = 'MEDIUM',
  HARD = 'HARD',
  EXPERT = 'EXPERT'
}

export const DIFFICULTY_LABELS = {
  [Difficulty.EASY]: '简单',
  [Difficulty.MEDIUM]: '中等',
  [Difficulty.HARD]: '困难',
  [Difficulty.EXPERT]: '专家'
} as const;

export const DIFFICULTIES = Object.values(Difficulty);
