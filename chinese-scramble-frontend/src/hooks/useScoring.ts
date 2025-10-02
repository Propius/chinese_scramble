import { useMemo } from 'react';
import { Difficulty } from '../constants/difficulties';
import { DIFFICULTY_SETTINGS, DIFFICULTY_MULTIPLIERS, HINT_PENALTIES } from '../constants/game.constants';

export interface ScoringParams {
  difficulty: Difficulty;
  timeTaken: number;
  timeLimit: number;
  hintsUsed: number;
  correct: boolean;
}

export interface ScoreBreakdown {
  basePoints: number;
  timeBonus: number;
  difficultyMultiplier: number;
  hintPenalty: number;
  finalScore: number;
}

export const useScoring = () => {
  const calculateScore = useMemo(() => {
    return (params: ScoringParams): ScoreBreakdown => {
      const { difficulty, timeTaken, timeLimit, hintsUsed, correct } = params;

      if (!correct) {
        return {
          basePoints: 0,
          timeBonus: 0,
          difficultyMultiplier: 1,
          hintPenalty: 0,
          finalScore: 0
        };
      }

      const settings = DIFFICULTY_SETTINGS[difficulty];
      const basePoints = settings?.basePoints || 100;

      const timeRemaining = Math.max(0, timeLimit - timeTaken);
      const timeBonus = Math.floor((timeRemaining / timeLimit) * (settings?.timeBonus || 50));

      const difficultyMultiplier = DIFFICULTY_MULTIPLIERS[difficulty] || 1.0;

      let hintPenalty = 0;
      if (hintsUsed > 0) {
        hintPenalty = hintsUsed * HINT_PENALTIES.LEVEL1;
      }

      const scoreBeforePenalty = Math.floor((basePoints + timeBonus) * difficultyMultiplier);
      const finalScore = Math.max(0, scoreBeforePenalty - hintPenalty);

      return {
        basePoints,
        timeBonus,
        difficultyMultiplier,
        hintPenalty,
        finalScore
      };
    };
  }, []);

  return { calculateScore };
};

export default useScoring;
