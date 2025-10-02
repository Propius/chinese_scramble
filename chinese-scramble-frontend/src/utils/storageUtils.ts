/**
 * Storage Utilities for Chinese Word Scramble Game
 *
 * SECURITY NOTE: Client-side encryption provides FALSE SECURITY.
 * - Encryption keys in client code are visible to all users
 * - localStorage is accessible via browser DevTools
 * - Any "encrypted" data can be decrypted by inspecting the code
 *
 * RECOMMENDATION:
 * - Store ONLY non-sensitive data in localStorage (UI preferences, game settings)
 * - NEVER store passwords, tokens, or PII client-side
 * - Use HTTPS for transport security
 * - Store sensitive data server-side only
 */

/**
 * Simple localStorage wrapper with error handling
 * Use for non-sensitive data only (game preferences, UI settings)
 */
export const storage = {
  setItem: (key: string, value: any): void => {
    try {
      localStorage.setItem(key, JSON.stringify(value));
    } catch (error) {
      console.error('Failed to store data:', error);
    }
  },

  getItem: <T>(key: string): T | null => {
    try {
      const item = localStorage.getItem(key);
      return item ? JSON.parse(item) as T : null;
    } catch (error) {
      console.error('Failed to retrieve data:', error);
      return null;
    }
  },

  removeItem: (key: string): void => {
    try {
      localStorage.removeItem(key);
    } catch (error) {
      console.error('Failed to remove data:', error);
    }
  },
};