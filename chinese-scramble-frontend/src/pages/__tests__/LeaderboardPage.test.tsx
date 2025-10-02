import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import LeaderboardPage from '../LeaderboardPage';
import { Difficulty } from '../../constants/difficulties';
import { useLeaderboard, LeaderboardEntry } from '../../hooks/useLeaderboard';

// Mock the hooks
jest.mock('../../hooks/useLeaderboard');
jest.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const translations: Record<string, string> = {
        'leaderboard.title': '排行榜',
      };
      return translations[key] || key;
    },
  }),
}));

// Mock Header component
jest.mock('../../components/layout/Header', () => ({
  __esModule: true,
  default: ({ title, subtitle, variant }: { title: string; subtitle?: string; variant?: string }) => (
    <div data-testid="mock-header">
      <h1>{title}</h1>
      {subtitle && <p>{subtitle}</p>}
    </div>
  ),
}));

const mockUseLeaderboard = useLeaderboard as jest.MockedFunction<typeof useLeaderboard>;

describe('LeaderboardPage', () => {
  const mockLeaderboardData: LeaderboardEntry[] = [
    {
      rank: 1,
      playerId: 1,
      username: 'Player1',
      bestScore: 1000,
      averageScore: 850,
      totalGames: 10,
      lastPlayed: '2025-10-01T10:00:00Z',
    },
    {
      rank: 2,
      playerId: 2,
      username: 'Player2',
      bestScore: 900,
      averageScore: 750,
      totalGames: 8,
      lastPlayed: '2025-10-01T09:00:00Z',
    },
    {
      rank: 3,
      playerId: 3,
      username: 'Player3',
      bestScore: 800,
      averageScore: 650,
      totalGames: 12,
      lastPlayed: '2025-10-01T08:00:00Z',
    },
  ];

  const mockGetTopPlayers = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    mockGetTopPlayers.mockResolvedValue(mockLeaderboardData);

    mockUseLeaderboard.mockReturnValue({
      loading: false,
      error: null,
      getTopPlayers: mockGetTopPlayers,
      getPlayerRankings: jest.fn(),
      getPlayerRank: jest.fn(),
    });
  });

  const renderLeaderboardPage = () => {
    return render(
      <BrowserRouter>
        <LeaderboardPage />
      </BrowserRouter>
    );
  };

  describe('Component Rendering', () => {
    it('should render the leaderboard page', () => {
      renderLeaderboardPage();
      expect(screen.getByTestId('mock-header')).toBeInTheDocument();
    });

    it('should render Header with correct title', () => {
      renderLeaderboardPage();
      expect(screen.getByText('排行榜')).toBeInTheDocument();
      expect(screen.getByText('查看顶尖玩家排名')).toBeInTheDocument();
    });

    it('should load leaderboard data on mount', async () => {
      renderLeaderboardPage();

      await waitFor(() => {
        expect(mockGetTopPlayers).toHaveBeenCalledWith('IDIOM', Difficulty.EASY, 10);
      });
    });

    it('should render filters section', () => {
      renderLeaderboardPage();
      expect(screen.getByText('筛选条件')).toBeInTheDocument();
    });

    it('should render leaderboard table header', async () => {
      renderLeaderboardPage();

      await waitFor(() => {
        expect(screen.getByText('排行榜')).toBeInTheDocument();
      });
    });
  });

  describe('Game Type Filter', () => {
    it('should render game type filter buttons', () => {
      renderLeaderboardPage();
      expect(screen.getByText('🎯 成语游戏')).toBeInTheDocument();
      expect(screen.getByText('📝 造句游戏')).toBeInTheDocument();
    });

    it('should have IDIOM selected by default', () => {
      const { container } = renderLeaderboardPage();
      const idiomButton = screen.getByText('🎯 成语游戏').closest('div');

      expect(idiomButton).toHaveStyle({ backgroundColor: '#16a34a' });
    });

    it('should switch to SENTENCE game type when clicked', async () => {
      renderLeaderboardPage();
      const sentenceButton = screen.getByText('📝 造句游戏');

      fireEvent.click(sentenceButton);

      await waitFor(() => {
        expect(mockGetTopPlayers).toHaveBeenCalledWith('SENTENCE', Difficulty.EASY, 10);
      });
    });

    it('should switch to IDIOM game type when clicked', async () => {
      renderLeaderboardPage();

      // First switch to SENTENCE
      const sentenceButton = screen.getByText('📝 造句游戏');
      fireEvent.click(sentenceButton);

      await waitFor(() => {
        expect(mockGetTopPlayers).toHaveBeenCalledWith('SENTENCE', Difficulty.EASY, 10);
      });

      // Then switch back to IDIOM
      const idiomButton = screen.getByText('🎯 成语游戏');
      fireEvent.click(idiomButton);

      await waitFor(() => {
        expect(mockGetTopPlayers).toHaveBeenCalledWith('IDIOM', Difficulty.EASY, 10);
      });
    });

    it('should handle keyboard Enter key on game type buttons', async () => {
      renderLeaderboardPage();
      const sentenceButton = screen.getByText('📝 造句游戏');

      fireEvent.keyPress(sentenceButton, { key: 'Enter', code: 'Enter', charCode: 13 });

      await waitFor(() => {
        expect(mockGetTopPlayers).toHaveBeenCalledWith('SENTENCE', Difficulty.EASY, 10);
      });
    });
  });

  describe('Difficulty Filter', () => {
    it('should render all difficulty buttons', () => {
      renderLeaderboardPage();
      expect(screen.getByText('简单')).toBeInTheDocument();
      expect(screen.getByText('中等')).toBeInTheDocument();
      expect(screen.getByText('困难')).toBeInTheDocument();
      expect(screen.getByText('专家')).toBeInTheDocument();
    });

    it('should have EASY difficulty selected by default', () => {
      const { container } = renderLeaderboardPage();
      const easyButton = screen.getByText('简单').closest('div');

      expect(easyButton).toHaveStyle({ backgroundColor: '#16a34a' });
    });

    it('should switch to MEDIUM difficulty when clicked', async () => {
      renderLeaderboardPage();
      const mediumButton = screen.getByText('中等');

      fireEvent.click(mediumButton);

      await waitFor(() => {
        expect(mockGetTopPlayers).toHaveBeenCalledWith('IDIOM', Difficulty.MEDIUM, 10);
      });
    });

    it('should switch to HARD difficulty when clicked', async () => {
      renderLeaderboardPage();
      const hardButton = screen.getByText('困难');

      fireEvent.click(hardButton);

      await waitFor(() => {
        expect(mockGetTopPlayers).toHaveBeenCalledWith('IDIOM', Difficulty.HARD, 10);
      });
    });

    it('should switch to EXPERT difficulty when clicked', async () => {
      renderLeaderboardPage();
      const expertButton = screen.getByText('专家');

      fireEvent.click(expertButton);

      await waitFor(() => {
        expect(mockGetTopPlayers).toHaveBeenCalledWith('IDIOM', Difficulty.EXPERT, 10);
      });
    });

    it('should handle keyboard Enter key on difficulty buttons', async () => {
      renderLeaderboardPage();
      const mediumButton = screen.getByText('中等');

      fireEvent.keyPress(mediumButton, { key: 'Enter', code: 'Enter', charCode: 13 });

      await waitFor(() => {
        expect(mockGetTopPlayers).toHaveBeenCalledWith('IDIOM', Difficulty.MEDIUM, 10);
      });
    });
  });

  describe('Limit Selection', () => {
    it('should render limit dropdown', () => {
      renderLeaderboardPage();
      expect(screen.getByText('显示数量')).toBeInTheDocument();
    });

    it('should have default limit of 10', () => {
      renderLeaderboardPage();
      const select = screen.getByRole('combobox') as HTMLSelectElement;
      expect(select.value).toBe('10');
    });

    it('should change limit to 25 when selected', async () => {
      renderLeaderboardPage();
      const select = screen.getByRole('combobox');

      fireEvent.change(select, { target: { value: '25' } });

      await waitFor(() => {
        expect(mockGetTopPlayers).toHaveBeenCalledWith('IDIOM', Difficulty.EASY, 25);
      });
    });

    it('should change limit to 50 when selected', async () => {
      renderLeaderboardPage();
      const select = screen.getByRole('combobox');

      fireEvent.change(select, { target: { value: '50' } });

      await waitFor(() => {
        expect(mockGetTopPlayers).toHaveBeenCalledWith('IDIOM', Difficulty.EASY, 50);
      });
    });

    it('should change limit to 100 when selected', async () => {
      renderLeaderboardPage();
      const select = screen.getByRole('combobox');

      fireEvent.change(select, { target: { value: '100' } });

      await waitFor(() => {
        expect(mockGetTopPlayers).toHaveBeenCalledWith('IDIOM', Difficulty.EASY, 100);
      });
    });
  });

  describe('Loading State', () => {
    it('should show loading spinner when loading', () => {
      mockUseLeaderboard.mockReturnValue({
        loading: true,
        error: null,
        getTopPlayers: mockGetTopPlayers,
        getPlayerRankings: jest.fn(),
        getPlayerRank: jest.fn(),
      });

      renderLeaderboardPage();

      expect(screen.getByText('加载排行榜中...')).toBeInTheDocument();
    });

    it('should hide refresh button when loading', () => {
      mockUseLeaderboard.mockReturnValue({
        loading: true,
        error: null,
        getTopPlayers: mockGetTopPlayers,
        getPlayerRankings: jest.fn(),
        getPlayerRank: jest.fn(),
      });

      renderLeaderboardPage();

      expect(screen.queryByText('🔄 刷新')).not.toBeInTheDocument();
    });

    it('should show refresh button when not loading', async () => {
      renderLeaderboardPage();

      await waitFor(() => {
        expect(screen.getByText('🔄 刷新')).toBeInTheDocument();
      });
    });
  });

  describe('Error State', () => {
    it('should display error message when error occurs', () => {
      mockUseLeaderboard.mockReturnValue({
        loading: false,
        error: 'Failed to load leaderboard',
        getTopPlayers: mockGetTopPlayers,
        getPlayerRankings: jest.fn(),
        getPlayerRank: jest.fn(),
      });

      renderLeaderboardPage();

      expect(screen.getByText('错误：')).toBeInTheDocument();
      expect(screen.getByText('Failed to load leaderboard')).toBeInTheDocument();
    });

    it('should not show leaderboard table when error occurs', () => {
      mockUseLeaderboard.mockReturnValue({
        loading: false,
        error: 'Failed to load leaderboard',
        getTopPlayers: mockGetTopPlayers,
        getPlayerRankings: jest.fn(),
        getPlayerRank: jest.fn(),
      });

      renderLeaderboardPage();

      expect(screen.queryByText('排名')).not.toBeInTheDocument();
    });
  });

  describe('Empty State', () => {
    it('should show empty state message when no data', async () => {
      mockGetTopPlayers.mockResolvedValue([]);

      renderLeaderboardPage();

      await waitFor(() => {
        expect(screen.getByText('暂无排行榜数据')).toBeInTheDocument();
      });
    });

    it('should show helpful message in empty state', async () => {
      mockGetTopPlayers.mockResolvedValue([]);

      renderLeaderboardPage();

      await waitFor(() => {
        expect(screen.getByText('开始游戏后，您的分数将出现在这里！')).toBeInTheDocument();
      });
    });
  });

  describe('Leaderboard Table Rendering', () => {
    it('should render table headers', async () => {
      renderLeaderboardPage();

      await waitFor(() => {
        expect(screen.getByText('排名')).toBeInTheDocument();
        expect(screen.getByText('玩家')).toBeInTheDocument();
        expect(screen.getByText('最高分')).toBeInTheDocument();
        expect(screen.getByText('平均分')).toBeInTheDocument();
        expect(screen.getByText('游戏数')).toBeInTheDocument();
        expect(screen.getByText('最后游戏')).toBeInTheDocument();
      });
    });

    it('should render all leaderboard entries', async () => {
      renderLeaderboardPage();

      await waitFor(() => {
        expect(screen.getByText('Player1')).toBeInTheDocument();
        expect(screen.getByText('Player2')).toBeInTheDocument();
        expect(screen.getByText('Player3')).toBeInTheDocument();
      });
    });

    it('should display player scores correctly', async () => {
      renderLeaderboardPage();

      await waitFor(() => {
        expect(screen.getByText('1000')).toBeInTheDocument();
        expect(screen.getByText('900')).toBeInTheDocument();
        expect(screen.getByText('800')).toBeInTheDocument();
      });
    });

    it('should display average scores correctly', async () => {
      renderLeaderboardPage();

      await waitFor(() => {
        expect(screen.getByText('850')).toBeInTheDocument();
        expect(screen.getByText('750')).toBeInTheDocument();
        expect(screen.getByText('650')).toBeInTheDocument();
      });
    });

    it('should display total games correctly', async () => {
      renderLeaderboardPage();

      await waitFor(() => {
        expect(screen.getByText('10')).toBeInTheDocument();
        expect(screen.getByText('8')).toBeInTheDocument();
        expect(screen.getByText('12')).toBeInTheDocument();
      });
    });
  });

  describe('Rank Display', () => {
    it('should display rank numbers correctly', async () => {
      renderLeaderboardPage();

      await waitFor(() => {
        expect(screen.getByText('#1')).toBeInTheDocument();
        expect(screen.getByText('#2')).toBeInTheDocument();
        expect(screen.getByText('#3')).toBeInTheDocument();
      });
    });

    it('should display gold medal emoji for rank 1', async () => {
      renderLeaderboardPage();

      await waitFor(() => {
        const rankCell = screen.getByText('#1').closest('td');
        expect(rankCell).toBeInTheDocument();
        expect(rankCell?.textContent).toContain('🥇');
      });
    });

    it('should display silver medal emoji for rank 2', async () => {
      renderLeaderboardPage();

      await waitFor(() => {
        const rankCell = screen.getByText('#2').closest('td');
        expect(rankCell).toBeInTheDocument();
        expect(rankCell?.textContent).toContain('🥈');
      });
    });

    it('should display bronze medal emoji for rank 3', async () => {
      renderLeaderboardPage();

      await waitFor(() => {
        const rankCell = screen.getByText('#3').closest('td');
        expect(rankCell).toBeInTheDocument();
        expect(rankCell?.textContent).toContain('🥉');
      });
    });
  });

  describe('Date Formatting', () => {
    it('should format last played date correctly', async () => {
      renderLeaderboardPage();

      await waitFor(() => {
        // Check that dates are rendered in the table
        const table = screen.getByRole('table');
        expect(table).toBeInTheDocument();
        const dateColumns = screen.getAllByText('最后游戏');
        expect(dateColumns.length).toBeGreaterThan(0);
      });
    });
  });

  describe('Refresh Functionality', () => {
    it('should reload leaderboard when refresh button is clicked', async () => {
      renderLeaderboardPage();

      await waitFor(() => {
        expect(mockGetTopPlayers).toHaveBeenCalledTimes(1);
      });

      const refreshButton = screen.getByText('🔄 刷新');
      fireEvent.click(refreshButton);

      await waitFor(() => {
        expect(mockGetTopPlayers).toHaveBeenCalledTimes(2);
      });
    });

    it('should use current filters when refreshing', async () => {
      renderLeaderboardPage();

      // Change to SENTENCE and HARD
      const sentenceButton = screen.getByText('📝 造句游戏');
      fireEvent.click(sentenceButton);

      const hardButton = screen.getByText('困难');
      fireEvent.click(hardButton);

      await waitFor(() => {
        expect(mockGetTopPlayers).toHaveBeenCalledWith('SENTENCE', Difficulty.HARD, 10);
      });

      // Click refresh
      const refreshButton = screen.getByText('🔄 刷新');
      fireEvent.click(refreshButton);

      await waitFor(() => {
        expect(mockGetTopPlayers).toHaveBeenLastCalledWith('SENTENCE', Difficulty.HARD, 10);
      });
    });
  });

  describe('Filter Combination', () => {
    it('should reload leaderboard when both game type and difficulty change', async () => {
      renderLeaderboardPage();

      const sentenceButton = screen.getByText('📝 造句游戏');
      const mediumButton = screen.getByText('中等');

      fireEvent.click(sentenceButton);
      fireEvent.click(mediumButton);

      await waitFor(() => {
        expect(mockGetTopPlayers).toHaveBeenCalledWith('SENTENCE', Difficulty.MEDIUM, 10);
      });
    });

    it('should reload leaderboard when all filters change', async () => {
      renderLeaderboardPage();

      const sentenceButton = screen.getByText('📝 造句游戏');
      const expertButton = screen.getByText('专家');
      const select = screen.getByRole('combobox');

      fireEvent.click(sentenceButton);
      fireEvent.click(expertButton);
      fireEvent.change(select, { target: { value: '50' } });

      await waitFor(() => {
        expect(mockGetTopPlayers).toHaveBeenCalledWith('SENTENCE', Difficulty.EXPERT, 50);
      });
    });
  });

  describe('Error Handling', () => {
    it('should handle API error gracefully', async () => {
      const consoleError = jest.spyOn(console, 'error').mockImplementation(() => {});
      mockGetTopPlayers.mockRejectedValue(new Error('Network error'));

      renderLeaderboardPage();

      await waitFor(() => {
        expect(consoleError).toHaveBeenCalledWith('Failed to load leaderboard:', expect.any(Error));
      });

      consoleError.mockRestore();
    });
  });

  describe('Accessibility', () => {
    it('should have proper role attributes on game type buttons', () => {
      renderLeaderboardPage();
      const idiomButton = screen.getByText('🎯 成语游戏').closest('div');
      const sentenceButton = screen.getByText('📝 造句游戏').closest('div');

      expect(idiomButton).toHaveAttribute('role', 'button');
      expect(sentenceButton).toHaveAttribute('role', 'button');
    });

    it('should have tabIndex on game type buttons', () => {
      renderLeaderboardPage();
      const idiomButton = screen.getByText('🎯 成语游戏').closest('div');
      const sentenceButton = screen.getByText('📝 造句游戏').closest('div');

      expect(idiomButton).toHaveAttribute('tabIndex', '0');
      expect(sentenceButton).toHaveAttribute('tabIndex', '0');
    });

    it('should have tabIndex on difficulty buttons', () => {
      renderLeaderboardPage();
      const easyButton = screen.getByText('简单').closest('div');

      expect(easyButton).toHaveAttribute('tabIndex', '0');
    });
  });
});
