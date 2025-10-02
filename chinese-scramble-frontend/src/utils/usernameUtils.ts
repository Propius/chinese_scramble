const USERNAME_STORAGE_KEY = 'chinese_scramble_username';

export const usernameUtils = {
  getUsername: (): string | null => {
    try {
      return localStorage.getItem(USERNAME_STORAGE_KEY);
    } catch (error) {
      console.error('Failed to get username from localStorage:', error);
      return null;
    }
  },

  setUsername: (username: string): void => {
    try {
      localStorage.setItem(USERNAME_STORAGE_KEY, username);
    } catch (error) {
      console.error('Failed to set username in localStorage:', error);
    }
  },

  clearUsername: (): void => {
    try {
      localStorage.removeItem(USERNAME_STORAGE_KEY);
    } catch (error) {
      console.error('Failed to clear username from localStorage:', error);
    }
  },

  hasUsername: (): boolean => {
    const username = usernameUtils.getUsername();
    return username !== null && username.trim().length > 0;
  }
};

export default usernameUtils;
