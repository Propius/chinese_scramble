import {
  extractUniqueCharacters,
  isChinese,
  containsChinese,
  getChineseCharacterCount,
  splitIntoCharacters,
  validateChineseIdiom,
  validateChineseSentence,
  formatPinyin,
  getFontFallbackStack,
} from '../chineseUtils';

describe('chineseUtils', () => {
  describe('isChinese', () => {
    it('should return true for Chinese characters', () => {
      expect(isChinese('中')).toBe(true);
      expect(isChinese('国')).toBe(true);
      expect(isChinese('龙')).toBe(true);
      expect(isChinese('画')).toBe(true);
    });

    it('should return false for non-Chinese characters', () => {
      expect(isChinese('a')).toBe(false);
      expect(isChinese('A')).toBe(false);
      expect(isChinese('1')).toBe(false);
      expect(isChinese(' ')).toBe(false);
      expect(isChinese('!')).toBe(false);
    });

    it('should handle empty string', () => {
      expect(isChinese('')).toBe(false);
    });

    it('should check only first character', () => {
      expect(isChinese('中国')).toBe(true); // checks first char
    });
  });

  describe('containsChinese', () => {
    it('should return true for strings with Chinese characters', () => {
      expect(containsChinese('中国')).toBe(true);
      expect(containsChinese('hello中文')).toBe(true);
      expect(containsChinese('123中')).toBe(true);
    });

    it('should return false for strings without Chinese', () => {
      expect(containsChinese('hello')).toBe(false);
      expect(containsChinese('123')).toBe(false);
      expect(containsChinese('abc 123 !@#')).toBe(false);
    });

    it('should handle empty string', () => {
      expect(containsChinese('')).toBe(false);
    });

    it('should handle single Chinese character', () => {
      expect(containsChinese('中')).toBe(true);
    });
  });

  describe('getChineseCharacterCount', () => {
    it('should count Chinese characters correctly', () => {
      expect(getChineseCharacterCount('中国')).toBe(2);
      expect(getChineseCharacterCount('画龙点睛')).toBe(4);
      expect(getChineseCharacterCount('中华人民共和国')).toBe(7);
    });

    it('should ignore non-Chinese characters', () => {
      expect(getChineseCharacterCount('hello中国123')).toBe(2);
      expect(getChineseCharacterCount('abc中def文xyz')).toBe(2);
    });

    it('should return 0 for non-Chinese text', () => {
      expect(getChineseCharacterCount('hello')).toBe(0);
      expect(getChineseCharacterCount('123')).toBe(0);
    });

    it('should return 0 for empty string', () => {
      expect(getChineseCharacterCount('')).toBe(0);
    });

    it('should handle mixed content', () => {
      expect(getChineseCharacterCount('Test 测试 123')).toBe(2);
    });
  });

  describe('extractUniqueCharacters', () => {
    it('should extract unique Chinese characters', () => {
      const result = extractUniqueCharacters(['中国', '中华']);
      expect(result).toContain('中');
      expect(result).toContain('国');
      expect(result).toContain('华');
      expect(result.length).toBe(3);
    });

    it('should remove duplicates', () => {
      const result = extractUniqueCharacters(['中中', '国国']);
      expect(result).toContain('中');
      expect(result).toContain('国');
      expect(result.length).toBe(2);
    });

    it('should ignore non-Chinese characters', () => {
      const result = extractUniqueCharacters(['abc中def国123']);
      expect(result).toContain('中');
      expect(result).toContain('国');
      expect(result.length).toBe(2);
    });

    it('should handle empty array', () => {
      const result = extractUniqueCharacters([]);
      expect(result).toEqual([]);
    });

    it('should handle array with empty strings', () => {
      const result = extractUniqueCharacters(['', '', '']);
      expect(result).toEqual([]);
    });

    it('should handle array with non-Chinese text', () => {
      const result = extractUniqueCharacters(['hello', 'world']);
      expect(result).toEqual([]);
    });

    it('should return array', () => {
      const result = extractUniqueCharacters(['中国']);
      expect(Array.isArray(result)).toBe(true);
    });
  });

  describe('splitIntoCharacters', () => {
    it('should split Chinese text into characters', () => {
      expect(splitIntoCharacters('中国')).toEqual(['中', '国']);
      expect(splitIntoCharacters('画龙点睛')).toEqual(['画', '龙', '点', '睛']);
    });

    it('should split English text', () => {
      expect(splitIntoCharacters('abc')).toEqual(['a', 'b', 'c']);
    });

    it('should handle empty string', () => {
      expect(splitIntoCharacters('')).toEqual([]);
    });

    it('should handle single character', () => {
      expect(splitIntoCharacters('中')).toEqual(['中']);
    });

    it('should handle mixed content', () => {
      const result = splitIntoCharacters('中a国');
      expect(result).toEqual(['中', 'a', '国']);
    });
  });

  describe('validateChineseIdiom', () => {
    it('should return true for 4-character idioms', () => {
      expect(validateChineseIdiom('画龙点睛')).toBe(true);
      expect(validateChineseIdiom('一帆风顺')).toBe(true);
    });

    it('should return true for 5-8 character idioms', () => {
      expect(validateChineseIdiom('五谷丰登年')).toBe(true);
      expect(validateChineseIdiom('百年树人计')).toBe(true);
    });

    it('should return false for less than 4 characters', () => {
      expect(validateChineseIdiom('中国')).toBe(false);
      expect(validateChineseIdiom('好')).toBe(false);
    });

    it('should return false for more than 8 characters', () => {
      expect(validateChineseIdiom('一二三四五六七八九')).toBe(false);
    });

    it('should ignore non-Chinese characters in count', () => {
      expect(validateChineseIdiom('中a国b人c民')).toBe(true); // 4 Chinese chars
    });

    it('should return false for empty string', () => {
      expect(validateChineseIdiom('')).toBe(false);
    });

    it('should return false for non-Chinese text', () => {
      expect(validateChineseIdiom('hello')).toBe(false);
    });
  });

  describe('validateChineseSentence', () => {
    it('should return true for matching sentence and words', () => {
      expect(validateChineseSentence('中国', ['中', '国'])).toBe(true);
      expect(validateChineseSentence('我爱中国', ['我', '爱', '中', '国'])).toBe(true);
    });

    it('should handle spaces in original sentence', () => {
      expect(validateChineseSentence('中 国', ['中', '国'])).toBe(true);
      expect(validateChineseSentence('我 爱 中国', ['我', '爱', '中国'])).toBe(true);
    });

    it('should return false for non-matching words', () => {
      expect(validateChineseSentence('中国', ['国', '中'])).toBe(false);
      expect(validateChineseSentence('中国', ['中'])).toBe(false);
    });

    it('should handle empty sentence', () => {
      expect(validateChineseSentence('', [])).toBe(true);
    });

    it('should return false for extra words', () => {
      expect(validateChineseSentence('中国', ['中', '国', '人'])).toBe(false);
    });

    it('should be order-sensitive', () => {
      expect(validateChineseSentence('中国', ['中', '国'])).toBe(true);
      expect(validateChineseSentence('中国', ['国', '中'])).toBe(false);
    });
  });

  describe('formatPinyin', () => {
    it('should convert to lowercase', () => {
      expect(formatPinyin('ZHONGGUO')).toBe('zhongguo');
      expect(formatPinyin('ZhongGuo')).toBe('zhongguo');
    });

    it('should normalize spaces', () => {
      expect(formatPinyin('zhong   guo')).toBe('zhong guo');
      expect(formatPinyin('  zhong guo  ')).toBe('zhong guo');
    });

    it('should trim whitespace', () => {
      expect(formatPinyin('  zhongguo  ')).toBe('zhongguo');
    });

    it('should handle empty string', () => {
      expect(formatPinyin('')).toBe('');
    });

    it('should handle single word', () => {
      expect(formatPinyin('zhongguo')).toBe('zhongguo');
    });

    it('should handle multiple spaces between words', () => {
      expect(formatPinyin('ni     hao')).toBe('ni hao');
    });
  });

  describe('getFontFallbackStack', () => {
    it('should return a string', () => {
      const result = getFontFallbackStack();
      expect(typeof result).toBe('string');
    });

    it('should contain common Chinese fonts', () => {
      const result = getFontFallbackStack();
      expect(result).toContain('PingFang SC');
      expect(result).toContain('Microsoft YaHei');
      expect(result).toContain('SimSun');
    });

    it('should contain fallback font', () => {
      const result = getFontFallbackStack();
      expect(result).toContain('sans-serif');
    });

    it('should be comma-separated', () => {
      const result = getFontFallbackStack();
      expect(result).toContain(',');
    });

    it('should not be empty', () => {
      const result = getFontFallbackStack();
      expect(result.length).toBeGreaterThan(0);
    });
  });
});
