import { useState, useEffect } from 'react';
import { DEFAULT_FEATURE_FLAGS } from '../constants/game.constants';

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

export const useFeatureFlags = () => {
  const [featureFlags, setFeatureFlags] = useState<FeatureFlags>(DEFAULT_FEATURE_FLAGS);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadFeatureFlags = async () => {
      try {
        const storedFlags = localStorage.getItem('featureFlags');
        if (storedFlags) {
          setFeatureFlags(JSON.parse(storedFlags));
        }
      } catch (error) {
        console.error('Failed to load feature flags:', error);
      } finally {
        setLoading(false);
      }
    };

    loadFeatureFlags();
  }, []);

  const isFeatureEnabled = (feature: keyof FeatureFlags): boolean => {
    return featureFlags[feature] ?? false;
  };

  return {
    featureFlags,
    isFeatureEnabled,
    loading
  };
};

export default useFeatureFlags;
