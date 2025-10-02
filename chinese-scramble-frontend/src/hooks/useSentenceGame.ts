import { useState, useCallback } from 'react';
import { Difficulty } from '../constants/difficulties';
import sentenceService, { SentenceQuestion, SentenceValidationResponse } from '../services/sentenceService';
import { usernameUtils } from '../utils/usernameUtils';
import questionTracker from '../utils/questionTracker';
import { DEFAULT_FEATURE_FLAGS, MAX_EXCLUDED_QUESTIONS } from '../constants/game.constants';

export interface SentenceGameState {
  question: SentenceQuestion | null;
  loading: boolean;
  error: string | null;
  userAnswer: string[];
  hintsUsed: number;
  timeTaken: number;
  validationResult: SentenceValidationResponse | null;
}

export const useSentenceGame = () => {
  const [state, setState] = useState<SentenceGameState>({
    question: null,
    loading: false,
    error: null,
    userAnswer: [],
    hintsUsed: 0,
    timeTaken: 0,
    validationResult: null
  });

  const startGame = useCallback(async (difficulty: Difficulty) => {
    setState(prev => ({ ...prev, loading: true, error: null }));
    try {
      // Always send playerId for leaderboard tracking
      const playerId = usernameUtils.getUsername() || undefined;

      // Get excluded IDs if no-repeat feature is enabled
      let excludedIds: string[] | undefined;
      if (DEFAULT_FEATURE_FLAGS.ENABLE_NO_REPEAT_QUESTIONS) {
        const seenQuestions = questionTracker.getSeenQuestions('SENTENCE', difficulty);
        const allExcluded = Array.from(seenQuestions);

        // Limit to last N questions to prevent long URLs and performance issues
        // Takes most recent questions (end of array) to maximize no-repeat effectiveness
        excludedIds = allExcluded.slice(-MAX_EXCLUDED_QUESTIONS);
      } else {
        // Clear localStorage when feature is OFF to prevent stale data issues
        questionTracker.resetSeenQuestions('SENTENCE', difficulty);
      }

      const question = await sentenceService.startGame(difficulty, playerId, excludedIds);

      if (DEFAULT_FEATURE_FLAGS.ENABLE_NO_REPEAT_QUESTIONS && question && question.id) {
        questionTracker.markQuestionAsSeen('SENTENCE', difficulty, question.id);
      }

      setState({
        question,
        loading: false,
        error: null,
        userAnswer: [],
        hintsUsed: 0,
        timeTaken: 0,
        validationResult: null
      });
    } catch (error) {
      setState(prev => ({
        ...prev,
        loading: false,
        error: '加载失败，请稍后重试'
      }));
      // Re-throw error so page component can handle completion/special cases
      throw error;
    }
  }, []);

  const submitAnswer = useCallback(async (answer: string[], timeTaken: number) => {
    if (!state.question) return;

    setState(prev => ({ ...prev, loading: true }));
    try {
      const playerId = usernameUtils.getUsername() || undefined;
      const result = await sentenceService.validateAnswer({
        sentenceId: state.question.id,
        userAnswer: answer,
        timeTaken,
        hintsUsed: state.hintsUsed
      }, playerId);

      setState(prev => ({
        ...prev,
        loading: false,
        validationResult: result,
        timeTaken
      }));

      return result;
    } catch (error) {
      setState(prev => ({
        ...prev,
        loading: false,
        error: '提交失败，请稍后重试'
      }));
    }
  }, [state.question, state.hintsUsed]);

  const getHint = useCallback(async (level: number) => {
    if (!state.question) return;

    setState(prev => ({ ...prev, loading: true }));
    try {
      const playerId = usernameUtils.getUsername() || undefined;
      const hint = await sentenceService.getHint({
        sentenceId: state.question.id,
        hintLevel: level
      }, playerId);

      setState(prev => ({
        ...prev,
        hintsUsed: prev.hintsUsed + 1,
        loading: false
      }));

      return hint;
    } catch (error) {
      setState(prev => ({ ...prev, loading: false }));
      console.error('Failed to get hint:', error);
    }
  }, [state.question]);

  const setUserAnswer = useCallback((answer: string[]) => {
    setState(prev => ({ ...prev, userAnswer: answer }));
  }, []);

  const reset = useCallback(() => {
    setState({
      question: null,
      loading: false,
      error: null,
      userAnswer: [],
      hintsUsed: 0,
      timeTaken: 0,
      validationResult: null
    });
  }, []);

  return {
    ...state,
    startGame,
    submitAnswer,
    getHint,
    setUserAnswer,
    reset
  };
};

export default useSentenceGame;
