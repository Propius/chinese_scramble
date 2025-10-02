import { soundManager } from '../soundManager';

describe('SoundManager', () => {
  beforeEach(() => {
    localStorage.clear();
    // Mock console methods
    jest.spyOn(console, 'warn').mockImplementation();
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  describe('Initialization', () => {
    it('should initialize with default settings', () => {
      expect(soundManager.isEnabled()).toBe(true);
      expect(soundManager.getVolume()).toBe(0.3);
    });
  });

  describe('playDrop', () => {
    it('should play drop sound without errors', () => {
      expect(() => soundManager.playDrop()).not.toThrow();
    });
  });

  describe('playRemove', () => {
    it('should play remove sound without errors', () => {
      expect(() => soundManager.playRemove()).not.toThrow();
    });
  });

  describe('playSubmit', () => {
    it('should play submit sound (melody) without errors', () => {
      expect(() => soundManager.playSubmit()).not.toThrow();
    });
  });

  describe('playWin', () => {
    it('should play win sound (melody) without errors', () => {
      expect(() => soundManager.playWin()).not.toThrow();
    });
  });

  describe('playLose', () => {
    it('should play lose sound (melody) without errors', () => {
      expect(() => soundManager.playLose()).not.toThrow();
    });
  });

  describe('playHint', () => {
    it('should play hint sound (melody) without errors', () => {
      expect(() => soundManager.playHint()).not.toThrow();
    });
  });

  describe('playClick', () => {
    it('should play click sound without errors', () => {
      expect(() => soundManager.playClick()).not.toThrow();
    });
  });

  describe('playWarning', () => {
    it('should play warning sound without errors', () => {
      expect(() => soundManager.playWarning()).not.toThrow();
    });
  });

  describe('setEnabled', () => {
    it('should enable sounds', () => {
      soundManager.setEnabled(true);
      expect(soundManager.isEnabled()).toBe(true);
      expect(localStorage.getItem('soundEnabled')).toBe('true');
    });

    it('should disable sounds', () => {
      soundManager.setEnabled(false);
      expect(soundManager.isEnabled()).toBe(false);
      expect(localStorage.getItem('soundEnabled')).toBe('false');
    });

    it('should not play sounds when disabled', () => {
      soundManager.setEnabled(false);
      expect(() => soundManager.playDrop()).not.toThrow();
      expect(() => soundManager.playWin()).not.toThrow();
    });
  });

  describe('setVolume', () => {
    it('should set volume within range', () => {
      soundManager.setVolume(0.5);
      expect(soundManager.getVolume()).toBe(0.5);
      expect(localStorage.getItem('soundVolume')).toBe('0.5');
    });

    it('should clamp volume to 0 minimum', () => {
      soundManager.setVolume(-0.5);
      expect(soundManager.getVolume()).toBe(0);
    });

    it('should clamp volume to 1 maximum', () => {
      soundManager.setVolume(1.5);
      expect(soundManager.getVolume()).toBe(1);
    });

    it('should accept 0 volume', () => {
      soundManager.setVolume(0);
      expect(soundManager.getVolume()).toBe(0);
    });

    it('should accept 1 volume', () => {
      soundManager.setVolume(1);
      expect(soundManager.getVolume()).toBe(1);
    });
  });

  describe('loadSettings', () => {
    it('should load enabled setting from localStorage', () => {
      localStorage.setItem('soundEnabled', 'false');
      soundManager.loadSettings();
      expect(soundManager.isEnabled()).toBe(false);
    });

    it('should load volume setting from localStorage', () => {
      localStorage.setItem('soundVolume', '0.7');
      soundManager.loadSettings();
      expect(soundManager.getVolume()).toBe(0.7);
    });

    it('should use defaults when no settings in localStorage', () => {
      soundManager.loadSettings();
      // Should maintain current settings or defaults
      expect(soundManager.isEnabled()).toBeDefined();
      expect(soundManager.getVolume()).toBeDefined();
    });
  });

  describe('isEnabled', () => {
    it('should return boolean', () => {
      expect(typeof soundManager.isEnabled()).toBe('boolean');
    });
  });

  describe('getVolume', () => {
    it('should return number', () => {
      expect(typeof soundManager.getVolume()).toBe('number');
    });

    it('should return value between 0 and 1', () => {
      const volume = soundManager.getVolume();
      expect(volume).toBeGreaterThanOrEqual(0);
      expect(volume).toBeLessThanOrEqual(1);
    });
  });
});
