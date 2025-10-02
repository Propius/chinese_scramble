import { storage } from '../storageUtils';

describe('storageUtils', () => {
  beforeEach(() => {
    localStorage.clear();
    jest.clearAllMocks();
  });

  afterEach(() => {
    localStorage.clear();
  });

  describe('setItem', () => {
    it('should store string value', () => {
      storage.setItem('testKey', 'testValue');
      const stored = localStorage.getItem('testKey');
      expect(stored).toBe('"testValue"');
    });

    it('should store number value', () => {
      storage.setItem('testKey', 123);
      const stored = localStorage.getItem('testKey');
      expect(stored).toBe('123');
    });

    it('should store boolean value', () => {
      storage.setItem('testKey', true);
      const stored = localStorage.getItem('testKey');
      expect(stored).toBe('true');
    });

    it('should store object value', () => {
      const obj = { name: 'test', value: 42 };
      storage.setItem('testKey', obj);
      const stored = localStorage.getItem('testKey');
      expect(stored).toBe(JSON.stringify(obj));
    });

    it('should store array value', () => {
      const arr = [1, 2, 3];
      storage.setItem('testKey', arr);
      const stored = localStorage.getItem('testKey');
      expect(stored).toBe(JSON.stringify(arr));
    });

    it('should store null value', () => {
      storage.setItem('testKey', null);
      const stored = localStorage.getItem('testKey');
      expect(stored).toBe('null');
    });

    it('should overwrite existing value', () => {
      storage.setItem('testKey', 'value1');
      storage.setItem('testKey', 'value2');
      const stored = localStorage.getItem('testKey');
      expect(stored).toBe('"value2"');
    });

    it('should handle errors gracefully', () => {
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
      const setItemSpy = jest.spyOn(Storage.prototype, 'setItem').mockImplementation(() => {
        throw new Error('Storage error');
      });

      expect(() => storage.setItem('testKey', 'value')).not.toThrow();

      consoleSpy.mockRestore();
      setItemSpy.mockRestore();
    });
  });

  describe('getItem', () => {
    it('should retrieve stored string', () => {
      storage.setItem('testKey', 'testValue');
      const result = storage.getItem<string>('testKey');
      expect(result).toBe('testValue');
    });

    it('should retrieve stored number', () => {
      storage.setItem('testKey', 123);
      const result = storage.getItem<number>('testKey');
      expect(result).toBe(123);
    });

    it('should retrieve stored boolean', () => {
      storage.setItem('testKey', true);
      const result = storage.getItem<boolean>('testKey');
      expect(result).toBe(true);
    });

    it('should retrieve stored object', () => {
      const obj = { name: 'test', value: 42 };
      storage.setItem('testKey', obj);
      const result = storage.getItem<typeof obj>('testKey');
      expect(result).toEqual(obj);
    });

    it('should retrieve stored array', () => {
      const arr = [1, 2, 3];
      storage.setItem('testKey', arr);
      const result = storage.getItem<number[]>('testKey');
      expect(result).toEqual(arr);
    });

    it('should return null for non-existent key', () => {
      const result = storage.getItem('nonExistentKey');
      expect(result).toBeNull();
    });

    it('should return null for invalid JSON', () => {
      localStorage.setItem('testKey', 'invalid json {');
      const result = storage.getItem('testKey');
      expect(result).toBeNull();
    });

    it('should handle errors gracefully', () => {
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
      const getItemSpy = jest.spyOn(Storage.prototype, 'getItem').mockImplementation(() => {
        throw new Error('Storage error');
      });

      const result = storage.getItem('testKey');
      expect(result).toBeNull();

      consoleSpy.mockRestore();
      getItemSpy.mockRestore();
    });
  });

  describe('removeItem', () => {
    it('should remove stored item', () => {
      storage.setItem('testKey', 'testValue');
      storage.removeItem('testKey');
      const result = storage.getItem('testKey');
      expect(result).toBeNull();
    });

    it('should not throw error for non-existent key', () => {
      expect(() => storage.removeItem('nonExistentKey')).not.toThrow();
    });

    it('should handle multiple removes', () => {
      storage.setItem('testKey', 'testValue');
      storage.removeItem('testKey');
      storage.removeItem('testKey');
      const result = storage.getItem('testKey');
      expect(result).toBeNull();
    });

    it('should handle errors gracefully', () => {
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
      const removeItemSpy = jest.spyOn(Storage.prototype, 'removeItem').mockImplementation(() => {
        throw new Error('Storage error');
      });

      expect(() => storage.removeItem('testKey')).not.toThrow();

      consoleSpy.mockRestore();
      removeItemSpy.mockRestore();
    });
  });

  describe('Integration tests', () => {
    it('should handle complete set-get-remove cycle', () => {
      const testData = { id: 1, name: 'Test', active: true };

      storage.setItem('user', testData);
      const retrieved = storage.getItem<typeof testData>('user');
      expect(retrieved).toEqual(testData);

      storage.removeItem('user');
      const afterRemove = storage.getItem('user');
      expect(afterRemove).toBeNull();
    });

    it('should handle multiple keys independently', () => {
      storage.setItem('key1', 'value1');
      storage.setItem('key2', 'value2');
      storage.setItem('key3', 'value3');

      expect(storage.getItem('key1')).toBe('value1');
      expect(storage.getItem('key2')).toBe('value2');
      expect(storage.getItem('key3')).toBe('value3');

      storage.removeItem('key2');

      expect(storage.getItem('key1')).toBe('value1');
      expect(storage.getItem('key2')).toBeNull();
      expect(storage.getItem('key3')).toBe('value3');
    });

    it('should handle complex nested objects', () => {
      const complexData = {
        user: {
          id: 1,
          profile: {
            name: 'Test User',
            scores: [100, 200, 300],
            settings: {
              theme: 'dark',
              notifications: true
            }
          }
        }
      };

      storage.setItem('complexData', complexData);
      const retrieved = storage.getItem<typeof complexData>('complexData');
      expect(retrieved).toEqual(complexData);
    });
  });
});
