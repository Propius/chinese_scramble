export const extractUniqueCharacters = (texts: string[]): string[] => {
  const uniqueChars = new Set<string>();

  texts.forEach(text => {
    for (const char of text) {
      if (isChinese(char)) {
        uniqueChars.add(char);
      }
    }
  });

  return Array.from(uniqueChars);
};

export const isChinese = (char: string): boolean => {
  const code = char.charCodeAt(0);
  return code >= 0x4E00 && code <= 0x9FFF;
};

export const containsChinese = (text: string): boolean => {
  for (const char of text) {
    if (isChinese(char)) {
      return true;
    }
  }
  return false;
};

export const getChineseCharacterCount = (text: string): number => {
  let count = 0;
  for (const char of text) {
    if (isChinese(char)) {
      count++;
    }
  }
  return count;
};

export const splitIntoCharacters = (text: string): string[] => {
  return Array.from(text);
};

export const validateChineseIdiom = (idiom: string): boolean => {
  const charCount = getChineseCharacterCount(idiom);
  return charCount >= 4 && charCount <= 8;
};

export const validateChineseSentence = (sentence: string, words: string[]): boolean => {
  const reconstructed = words.join('');
  const originalNoSpaces = sentence.replace(/\s/g, '');
  return reconstructed === originalNoSpaces;
};

export const formatPinyin = (pinyin: string): string => {
  return pinyin.toLowerCase().replace(/\s+/g, ' ').trim();
};

export const getFontFallbackStack = (): string => {
  return [
    '-apple-system-font',
    "'PingFang SC'",
    "'Microsoft YaHei'",
    "'SimSun'",
    'sans-serif'
  ].join(', ');
};