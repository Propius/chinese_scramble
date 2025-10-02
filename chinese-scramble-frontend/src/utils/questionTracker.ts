type GameType = 'IDIOM' | 'SENTENCE';

const STORAGE_KEY_PREFIX = 'question_tracker';

/**
 * Generates a storage key for a specific game type and difficulty
 */
const getStorageKey = (gameType: GameType, difficulty: string): string => {
  return `${STORAGE_KEY_PREFIX}_${gameType}_${difficulty}`;
};

/**
 * Safely reads data from localStorage
 */
const safelyReadFromStorage = (key: string): Set<string> => {
  try {
    const data = localStorage.getItem(key);
    if (!data) {
      return new Set<string>();
    }
    const parsed = JSON.parse(data);
    if (!Array.isArray(parsed)) {
      console.warn(`Invalid data format for key ${key}, expected array`);
      return new Set<string>();
    }
    return new Set<string>(parsed.map(String));
  } catch (error) {
    console.error(`Error reading from localStorage for key ${key}:`, error);
    return new Set<string>();
  }
};

/**
 * Safely writes data to localStorage
 */
const safelyWriteToStorage = (key: string, data: Set<string>): boolean => {
  try {
    const array = Array.from(data);
    localStorage.setItem(key, JSON.stringify(array));
    return true;
  } catch (error) {
    if (error instanceof DOMException && error.name === 'QuotaExceededError') {
      console.error('localStorage quota exceeded:', error);
    } else {
      console.error(`Error writing to localStorage for key ${key}:`, error);
    }
    return false;
  }
};

/**
 * Marks a question as seen for a specific game type and difficulty
 */
export const markQuestionAsSeen = (
  gameType: GameType,
  difficulty: string,
  questionId: number | string
): boolean => {
  const key = getStorageKey(gameType, difficulty);
  const seenQuestions = safelyReadFromStorage(key);
  seenQuestions.add(String(questionId));
  return safelyWriteToStorage(key, seenQuestions);
};

/**
 * Checks if a question has been seen for a specific game type and difficulty
 */
export const hasSeenQuestion = (
  gameType: GameType,
  difficulty: string,
  questionId: number | string
): boolean => {
  const key = getStorageKey(gameType, difficulty);
  const seenQuestions = safelyReadFromStorage(key);
  return seenQuestions.has(String(questionId));
};

/**
 * Gets all seen questions for a specific game type and difficulty
 */
export const getSeenQuestions = (
  gameType: GameType,
  difficulty: string
): Set<string> => {
  const key = getStorageKey(gameType, difficulty);
  return safelyReadFromStorage(key);
};

/**
 * Resets seen questions for a specific game type and difficulty, or all difficulties if not specified
 */
export const resetSeenQuestions = (
  gameType: GameType,
  difficulty?: string
): boolean => {
  try {
    if (difficulty) {
      const key = getStorageKey(gameType, difficulty);
      localStorage.removeItem(key);
      return true;
    } else {
      const difficulties = ['EASY', 'MEDIUM', 'HARD', 'EXPERT'];
      difficulties.forEach(diff => {
        const key = getStorageKey(gameType, diff);
        localStorage.removeItem(key);
      });
      return true;
    }
  } catch (error) {
    console.error('Error resetting seen questions:', error);
    return false;
  }
};

/**
 * Gets the count of seen questions for a specific game type and difficulty
 */
export const getSeenCount = (
  gameType: GameType,
  difficulty: string
): number => {
  const seenQuestions = getSeenQuestions(gameType, difficulty);
  return seenQuestions.size;
};

const questionTracker = {
  markQuestionAsSeen,
  hasSeenQuestion,
  getSeenQuestions,
  resetSeenQuestions,
  getSeenCount,
};

export default questionTracker;
