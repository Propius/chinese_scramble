/**
 * Unit tests for idiomService with feature flag behavior
 * Verifies URL construction and excludedIds parameter handling
 */

import { idiomService } from '../idiomService';
import apiClient from '../api';
import { Difficulty } from '../../constants/difficulties';
import { DEFAULT_FEATURE_FLAGS } from '../../constants/game.constants';

jest.mock('../api');

const mockApiClient = apiClient as jest.Mocked<typeof apiClient>;

describe('idiomService - Restart and Feature Flag', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('startGame() URL construction', () => {
    const mockQuestion = {
      id: '1',
      text: '一马当先',
      pinyin: 'yī mǎ dāng xiān',
      meaning: '比喻走在前面，起带头作用',
      translation: 'To take the lead',
      difficulty: Difficulty.EASY,
      category: '动物',
      usageExample: '在这次比赛中，他一马当先。',
      exampleTranslation: 'In this competition, he took the lead.',
      characterCount: 4,
      scrambledCharacters: ['马', '一', '先', '当'],
      correctAnswer: '一马当先',
    };

    beforeEach(() => {
      mockApiClient.get.mockResolvedValue(mockQuestion);
    });

    describe('Feature Flag OFF', () => {
      beforeEach(() => {
        (DEFAULT_FEATURE_FLAGS as any).ENABLE_NO_REPEAT_QUESTIONS = false;
      });

      it('should NOT include excludedIds in URL when flag is OFF and excludedIds is undefined', async () => {
        await idiomService.startGame(Difficulty.EASY, 'player1', undefined);

        expect(mockApiClient.get).toHaveBeenCalledWith(
          '/api/idiom-game/start?difficulty=EASY&playerId=player1'
        );
      });

      it('should NOT include excludedIds in URL when flag is OFF and excludedIds is empty array', async () => {
        await idiomService.startGame(Difficulty.MEDIUM, 'player2', []);

        expect(mockApiClient.get).toHaveBeenCalledWith(
          '/api/idiom-game/start?difficulty=MEDIUM&playerId=player2'
        );
      });

      it('should NOT include excludedIds in URL when flag is OFF even with valid array', async () => {
        await idiomService.startGame(Difficulty.HARD, 'player3', ['1', '2', '3']);

        // Should NOT include excludedIds because feature flag is OFF
        expect(mockApiClient.get).toHaveBeenCalledWith(
          '/api/idiom-game/start?difficulty=HARD&playerId=player3'
        );
      });

      it('should construct URL without playerId when not provided and flag OFF', async () => {
        await idiomService.startGame(Difficulty.EXPERT, undefined, undefined);

        expect(mockApiClient.get).toHaveBeenCalledWith(
          '/api/idiom-game/start?difficulty=EXPERT'
        );
      });
    });

    describe('Feature Flag ON', () => {
      beforeEach(() => {
        (DEFAULT_FEATURE_FLAGS as any).ENABLE_NO_REPEAT_QUESTIONS = true;
      });

      it('should include excludedIds in URL when flag is ON and array has items', async () => {
        await idiomService.startGame(Difficulty.EASY, 'player1', ['1', '2', '3']);

        expect(mockApiClient.get).toHaveBeenCalledWith(
          '/api/idiom-game/start?difficulty=EASY&playerId=player1&excludedIds=1%2C2%2C3'
        );
      });

      it('should NOT include excludedIds when flag is ON but array is empty', async () => {
        await idiomService.startGame(Difficulty.MEDIUM, 'player2', []);

        expect(mockApiClient.get).toHaveBeenCalledWith(
          '/api/idiom-game/start?difficulty=MEDIUM&playerId=player2'
        );
      });

      it('should NOT include excludedIds when flag is ON but excludedIds is undefined', async () => {
        await idiomService.startGame(Difficulty.HARD, 'player3', undefined);

        expect(mockApiClient.get).toHaveBeenCalledWith(
          '/api/idiom-game/start?difficulty=HARD&playerId=player3'
        );
      });

      it('should handle large excludedIds array correctly', async () => {
        const largeArray = Array.from({ length: 50 }, (_, i) => `${i + 1}`);
        await idiomService.startGame(Difficulty.EXPERT, 'player4', largeArray);

        const expectedIds = largeArray.join('%2C');
        expect(mockApiClient.get).toHaveBeenCalledWith(
          `/api/idiom-game/start?difficulty=EXPERT&playerId=player4&excludedIds=${expectedIds}`
        );
      });

      it('should handle special characters in excludedIds', async () => {
        await idiomService.startGame(Difficulty.EASY, 'player5', ['id-1', 'id_2', 'id.3']);

        expect(mockApiClient.get).toHaveBeenCalledWith(
          '/api/idiom-game/start?difficulty=EASY&playerId=player5&excludedIds=id-1%2Cid_2%2Cid.3'
        );
      });
    });

    describe('All Difficulty Levels', () => {
      beforeEach(() => {
        (DEFAULT_FEATURE_FLAGS as any).ENABLE_NO_REPEAT_QUESTIONS = false;
      });

      it.each([
        [Difficulty.EASY, 'EASY'],
        [Difficulty.MEDIUM, 'MEDIUM'],
        [Difficulty.HARD, 'HARD'],
        [Difficulty.EXPERT, 'EXPERT'],
      ])('should construct correct URL for %s difficulty', async (difficulty, difficultyStr) => {
        await idiomService.startGame(difficulty, 'player1', undefined);

        expect(mockApiClient.get).toHaveBeenCalledWith(
          `/api/idiom-game/start?difficulty=${difficultyStr}&playerId=player1`
        );
      });
    });

    describe('Error Handling', () => {
      beforeEach(() => {
        (DEFAULT_FEATURE_FLAGS as any).ENABLE_NO_REPEAT_QUESTIONS = false;
      });

      it('should propagate network errors', async () => {
        const error = new Error('Network error');
        mockApiClient.get.mockRejectedValue(error);

        await expect(
          idiomService.startGame(Difficulty.EASY, 'player1', undefined)
        ).rejects.toThrow('Network error');
      });

      it('should propagate 400 errors from backend', async () => {
        const error = { response: { status: 400, data: { error: 'Bad Request' } } };
        mockApiClient.get.mockRejectedValue(error);

        await expect(
          idiomService.startGame(Difficulty.MEDIUM, 'player2', undefined)
        ).rejects.toEqual(error);
      });

      it('should propagate completion errors', async () => {
        const error = {
          response: {
            status: 400,
            data: {
              error: '已完成所有题目',
              details: { allQuestionsCompleted: true },
            },
          },
        };
        mockApiClient.get.mockRejectedValue(error);

        await expect(
          idiomService.startGame(Difficulty.HARD, 'player3', undefined)
        ).rejects.toEqual(error);
      });
    });
  });

  describe('Integration with page restart flow', () => {
    beforeEach(() => {
      (DEFAULT_FEATURE_FLAGS as any).ENABLE_NO_REPEAT_QUESTIONS = false;
      mockApiClient.get.mockResolvedValue({ id: '1', text: 'test' });
    });

    it('should work correctly when called multiple times in succession', async () => {
      await idiomService.startGame(Difficulty.EASY, 'player1', undefined);
      await idiomService.startGame(Difficulty.MEDIUM, 'player1', undefined);
      await idiomService.startGame(Difficulty.HARD, 'player1', undefined);

      expect(mockApiClient.get).toHaveBeenCalledTimes(3);

      // Verify none of the calls included excludedIds
      mockApiClient.get.mock.calls.forEach(call => {
        expect(call[0]).not.toContain('excludedIds');
      });
    });

    it('should handle rapid restart scenarios', async () => {
      const promises = [
        idiomService.startGame(Difficulty.EASY, 'player1', undefined),
        idiomService.startGame(Difficulty.EASY, 'player1', undefined),
        idiomService.startGame(Difficulty.EASY, 'player1', undefined),
      ];

      await Promise.all(promises);

      expect(mockApiClient.get).toHaveBeenCalledTimes(3);
    });
  });
});
