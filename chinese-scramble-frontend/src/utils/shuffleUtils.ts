export const shuffleArray = <T>(array: T[]): T[] => {
  const shuffled = [...array];
  for (let i = shuffled.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
  }
  return shuffled;
};

export const isShuffled = <T>(original: T[], shuffled: T[]): boolean => {
  if (original.length !== shuffled.length) return false;

  for (let i = 0; i < original.length; i++) {
    if (original[i] !== shuffled[i]) return true;
  }

  return false;
};

export const ensureShuffled = <T>(array: T[], maxAttempts: number = 10): T[] => {
  let shuffled = shuffleArray(array);
  let attempts = 0;

  while (!isShuffled(array, shuffled) && attempts < maxAttempts) {
    shuffled = shuffleArray(array);
    attempts++;
  }

  return shuffled;
};