import { DifficultyLevel, GameScore } from '../types/game.types';
import { DIFFICULTY_MULTIPLIERS, DIFFICULTY_SETTINGS, HINT_PENALTIES } from '../constants/game.constants';

export const calculateScore = (
  difficulty: DifficultyLevel,
  timeRemaining: number,
  hintsUsed: number,
  correct: boolean
): GameScore => {
  if (!correct) {
    return {
      basePoints: 0,
      timeBonus: 0,
      difficultyMultiplier: 0,
      hintPenalty: 0,
      totalScore: 0,
    };
  }

  const settings = DIFFICULTY_SETTINGS[difficulty];
  const basePoints = settings.basePoints;

  const timeBonusMultiplier = 0.5;
  const timeBonus = Math.floor(timeRemaining * timeBonusMultiplier);

  const hintPenalty = calculateHintPenalty(hintsUsed);

  const difficultyMultiplier = DIFFICULTY_MULTIPLIERS[difficulty];

  const subtotal = (basePoints + timeBonus - hintPenalty) * difficultyMultiplier;
  const totalScore = Math.max(0, Math.floor(subtotal));

  return {
    basePoints,
    timeBonus,
    difficultyMultiplier,
    hintPenalty,
    totalScore,
  };
};

export const calculateHintPenalty = (hintsUsed: number): number => {
  let penalty = 0;
  for (let i = 1; i <= hintsUsed && i <= 3; i++) {
    penalty += HINT_PENALTIES[`LEVEL${i}` as keyof typeof HINT_PENALTIES];
  }
  return penalty;
};

export const calculateAccuracy = (
  correctAnswers: number,
  totalQuestions: number
): number => {
  if (totalQuestions === 0) return 0;
  return Math.round((correctAnswers / totalQuestions) * 100);
};

export const calculateTimeTaken = (timeRemaining: number, totalTime: number): number => {
  return totalTime - timeRemaining;
};

export const formatScore = (score: number): string => {
  return score.toLocaleString('zh-CN');
};

export const formatTime = (seconds: number): string => {
  const minutes = Math.floor(seconds / 60);
  const remainingSeconds = seconds % 60;
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`;
};