import { usernameUtils } from '../usernameUtils';

describe('usernameUtils', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  afterEach(() => {
    localStorage.clear();
  });

  describe('getUsername', () => {
    it('should return null when no username is stored', () => {
      expect(usernameUtils.getUsername()).toBeNull();
    });

    it('should return stored username', () => {
      localStorage.setItem('chinese_scramble_username', 'testuser');
      expect(usernameUtils.getUsername()).toBe('testuser');
    });

    it('should handle localStorage errors gracefully', () => {
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
      const getItemSpy = jest.spyOn(Storage.prototype, 'getItem').mockImplementation(() => {
        throw new Error('Storage error');
      });

      expect(usernameUtils.getUsername()).toBeNull();

      consoleSpy.mockRestore();
      getItemSpy.mockRestore();
    });
  });

  describe('setUsername', () => {
    it('should store username in localStorage', () => {
      usernameUtils.setUsername('newuser');
      expect(localStorage.getItem('chinese_scramble_username')).toBe('newuser');
    });

    it('should overwrite existing username', () => {
      usernameUtils.setUsername('user1');
      usernameUtils.setUsername('user2');
      expect(usernameUtils.getUsername()).toBe('user2');
    });

    it('should handle localStorage errors gracefully', () => {
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
      const setItemSpy = jest.spyOn(Storage.prototype, 'setItem').mockImplementation(() => {
        throw new Error('Storage error');
      });

      usernameUtils.setUsername('testuser');

      consoleSpy.mockRestore();
      setItemSpy.mockRestore();
    });
  });

  describe('clearUsername', () => {
    it('should remove username from localStorage', () => {
      usernameUtils.setUsername('testuser');
      usernameUtils.clearUsername();
      expect(usernameUtils.getUsername()).toBeNull();
    });

    it('should handle errors gracefully', () => {
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
      const removeItemSpy = jest.spyOn(Storage.prototype, 'removeItem').mockImplementation(() => {
        throw new Error('Storage error');
      });

      usernameUtils.clearUsername();

      consoleSpy.mockRestore();
      removeItemSpy.mockRestore();
    });
  });

  describe('hasUsername', () => {
    it('should return false when no username is stored', () => {
      expect(usernameUtils.hasUsername()).toBe(false);
    });

    it('should return false when username is empty string', () => {
      localStorage.setItem('chinese_scramble_username', '   ');
      expect(usernameUtils.hasUsername()).toBe(false);
    });

    it('should return true when username exists', () => {
      usernameUtils.setUsername('testuser');
      expect(usernameUtils.hasUsername()).toBe(true);
    });
  });
});
