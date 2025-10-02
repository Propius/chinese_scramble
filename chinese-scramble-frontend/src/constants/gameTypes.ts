export enum GameType {
  IDIOM = 'IDIOM',
  SENTENCE = 'SENTENCE'
}

export const GAME_TYPE_LABELS = {
  [GameType.IDIOM]: '成语拼字',
  [GameType.SENTENCE]: '造句游戏'
} as const;

export const GAME_TYPES = Object.values(GameType);
