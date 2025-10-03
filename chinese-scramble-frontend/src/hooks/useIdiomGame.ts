import { useState, useCallback } from 'react';
import { Difficulty } from '../constants/difficulties';
import idiomService, { IdiomQuestion, IdiomValidationResponse } from '../services/idiomService';
import { usernameUtils } from '../utils/usernameUtils';
import questionTracker from '../utils/questionTracker';
import { DEFAULT_FEATURE_FLAGS, MAX_EXCLUDED_QUESTIONS } from '../constants/game.constants';

export interface IdiomGameState {
  question: IdiomQuestion | null;
  loading: boolean;
  error: string | null;
  userAnswer: string[];
  hintsUsed: number;
  timeTaken: number;
  validationResult: IdiomValidationResponse | null;
}

export const useIdiomGame = () => {
  const [state, setState] = useState<IdiomGameState>({
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
        const seenQuestions = questionTracker.getSeenQuestions('IDIOM', difficulty);
        const allExcluded = Array.from(seenQuestions);
        excludedIds = allExcluded.slice(-MAX_EXCLUDED_QUESTIONS);
      } else {
        questionTracker.resetSeenQuestions('IDIOM', difficulty);
      }

      const question = await idiomService.startGame(difficulty, playerId, excludedIds);

      if (DEFAULT_FEATURE_FLAGS.ENABLE_NO_REPEAT_QUESTIONS && question && question.id) {
        questionTracker.markQuestionAsSeen('IDIOM', difficulty, question.id);
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
      const result = await idiomService.validateAnswer({
        idiomId: state.question.id,
        userAnswer: answer.join(''),
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
      const hint = await idiomService.getHint({
        idiomId: state.question.id,
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

  const clearError = useCallback(() => {
    setState(prev => ({ ...prev, error: null }));
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
    clearError,
    reset
  };
};

export default useIdiomGame;
