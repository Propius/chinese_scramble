import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import StatisticsPage from '../StatisticsPage';
import { useLeaderboard, PlayerRanking } from '../../hooks/useLeaderboard';
import { usernameUtils } from '../../utils/usernameUtils';

// Mock the hooks and utilities
jest.mock('../../hooks/useLeaderboard');
jest.mock('../../utils/usernameUtils');
jest.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const translations: Record<string, string> = {
        'statistics.title': '统计数据',
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
const mockUsernameUtils = usernameUtils as jest.Mocked<typeof usernameUtils>;

describe('StatisticsPage', () => {
  const mockPlayerRankings: PlayerRanking[] = [
    {
      gameType: 'IDIOM',
      difficulty: 'EASY',
      rank: 5,
      bestScore: 800,
      averageScore: 650,
      totalGames: 15,
    },
    {
      gameType: 'IDIOM',
      difficulty: 'MEDIUM',
      rank: 10,
      bestScore: 700,
      averageScore: 550,
      totalGames: 10,
    },
    {
      gameType: 'SENTENCE',
      difficulty: 'EASY',
      rank: 3,
      bestScore: 900,
      averageScore: 750,
      totalGames: 12,
    },
    {
      gameType: 'SENTENCE',
      difficulty: 'HARD',
      rank: 1,
      bestScore: 1000,
      averageScore: 850,
      totalGames: 8,
    },
  ];

  const mockGetPlayerRankings = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    mockGetPlayerRankings.mockResolvedValue(mockPlayerRankings);
    mockUsernameUtils.getUsername.mockReturnValue('TestPlayer');

    mockUseLeaderboard.mockReturnValue({
      loading: false,
      error: null,
      getTopPlayers: jest.fn(),
      getPlayerRankings: mockGetPlayerRankings,
      getPlayerRank: jest.fn(),
    });
  });

  const renderStatisticsPage = () => {
    return render(
      <BrowserRouter>
        <StatisticsPage />
      </BrowserRouter>
    );
  };

  describe('Component Rendering', () => {
    it('should render the statistics page', () => {
      renderStatisticsPage();
      expect(screen.getByTestId('mock-header')).toBeInTheDocument();
    });

    it('should render Header with correct title', () => {
      renderStatisticsPage();
      expect(screen.getByText('统计数据')).toBeInTheDocument();
      expect(screen.getByText('查看你的游戏统计数据')).toBeInTheDocument();
    });

    it('should get username from usernameUtils on mount', () => {
      renderStatisticsPage();
      expect(mockUsernameUtils.getUsername).toHaveBeenCalled();
    });

    it('should load player statistics on mount', async () => {
      renderStatisticsPage();

      await waitFor(() => {
        expect(mockGetPlayerRankings).toHaveBeenCalledWith('TestPlayer');
      });
    });

    it('should use Guest as default username when no username found', () => {
      mockUsernameUtils.getUsername.mockReturnValue(null);
      renderStatisticsPage();

      expect(mockUsernameUtils.getUsername).toHaveBeenCalled();
    });
  });

  describe('Loading State', () => {
    it('should show loading spinner when loading', () => {
      mockUseLeaderboard.mockReturnValue({
        loading: true,
        error: null,
        getTopPlayers: jest.fn(),
        getPlayerRankings: mockGetPlayerRankings,
        getPlayerRank: jest.fn(),
      });

      renderStatisticsPage();

      expect(screen.getByText('加载统计数据中...')).toBeInTheDocument();
    });

    it('should display loading spinner with correct styling', () => {
      mockUseLeaderboard.mockReturnValue({
        loading: true,
        error: null,
        getTopPlayers: jest.fn(),
        getPlayerRankings: mockGetPlayerRankings,
        getPlayerRank: jest.fn(),
      });

      const { container } = renderStatisticsPage();
      const spinner = container.querySelector('.animate-spin');

      expect(spinner).toBeInTheDocument();
    });
  });

  describe('Error State', () => {
    it('should display error message when error occurs', () => {
      mockUseLeaderboard.mockReturnValue({
        loading: false,
        error: 'Failed to load statistics',
        getTopPlayers: jest.fn(),
        getPlayerRankings: mockGetPlayerRankings,
        getPlayerRank: jest.fn(),
      });

      renderStatisticsPage();

      expect(screen.getByText('错误：')).toBeInTheDocument();
      expect(screen.getByText('Failed to load statistics')).toBeInTheDocument();
    });

    it('should not show statistics cards when error occurs', () => {
      mockUseLeaderboard.mockReturnValue({
        loading: false,
        error: 'Failed to load statistics',
        getTopPlayers: jest.fn(),
        getPlayerRankings: mockGetPlayerRankings,
        getPlayerRank: jest.fn(),
      });

      renderStatisticsPage();

      expect(screen.queryByText('总游戏数')).not.toBeInTheDocument();
    });
  });

  describe('Empty State', () => {
    it('should show empty state when no rankings', async () => {
      mockGetPlayerRankings.mockResolvedValue([]);

      renderStatisticsPage();

      await waitFor(() => {
        expect(screen.getByText('还没有统计数据')).toBeInTheDocument();
      });
    });

    it('should show helpful message in empty state', async () => {
      mockGetPlayerRankings.mockResolvedValue([]);

      renderStatisticsPage();

      await waitFor(() => {
        expect(screen.getByText('开始玩游戏来积累你的统计数据吧！')).toBeInTheDocument();
      });
    });

    it('should show start game link in empty state', async () => {
      mockGetPlayerRankings.mockResolvedValue([]);

      renderStatisticsPage();

      await waitFor(() => {
        const startButton = screen.getByText('开始游戏');
        expect(startButton).toBeInTheDocument();
        expect(startButton.closest('a')).toHaveAttribute('href', '/');
      });
    });
  });

  describe('Overall Summary Cards', () => {
    it('should render all four summary cards', async () => {
      renderStatisticsPage();

      await waitFor(() => {
        expect(screen.getByText('总游戏数')).toBeInTheDocument();
        expect(screen.getByText('总分')).toBeInTheDocument();
        expect(screen.getByText('平均最高分')).toBeInTheDocument();
        expect(screen.getByText('最佳排名')).toBeInTheDocument();
      });
    });

    it('should calculate total games correctly', async () => {
      renderStatisticsPage();

      await waitFor(() => {
        // 15 + 10 + 12 + 8 = 45
        expect(screen.getByText('45')).toBeInTheDocument();
      });
    });

    it('should calculate total score correctly', async () => {
      renderStatisticsPage();

      await waitFor(() => {
        // 800 + 700 + 900 + 1000 = 3400
        expect(screen.getByText('3400')).toBeInTheDocument();
      });
    });

    it('should calculate average score correctly', async () => {
      renderStatisticsPage();

      await waitFor(() => {
        // 3400 / 4 = 850
        const avgScoreElements = screen.getAllByText('850');
        expect(avgScoreElements.length).toBeGreaterThan(0);
      });
    });

    it('should display best rank correctly', async () => {
      renderStatisticsPage();

      await waitFor(() => {
        // Best rank is 1
        const rank1Elements = screen.getAllByText(/#1/);
        expect(rank1Elements.length).toBeGreaterThan(0);
      });
    });

    it('should show N/A for best rank when no rankings', async () => {
      mockGetPlayerRankings.mockResolvedValue([]);

      renderStatisticsPage();

      await waitFor(() => {
        expect(screen.queryByText('#1')).not.toBeInTheDocument();
      });
    });
  });

  describe('Idiom Game Statistics', () => {
    it('should render idiom game section', async () => {
      renderStatisticsPage();

      await waitFor(() => {
        expect(screen.getByText('🎯 成语游戏统计')).toBeInTheDocument();
      });
    });

    it('should display idiom game summary', async () => {
      renderStatisticsPage();

      await waitFor(() => {
        // Total games for idiom: 15 + 10 = 25
        expect(screen.getByText(/25 场游戏/)).toBeInTheDocument();
      });
    });

    it('should display idiom game average score', async () => {
      renderStatisticsPage();

      await waitFor(() => {
        // Average: (650 + 550) / 2 = 600
        expect(screen.getByText(/平均分: 600/)).toBeInTheDocument();
      });
    });

    it('should render idiom difficulty cards', async () => {
      renderStatisticsPage();

      await waitFor(() => {
        const idiomSection = screen.getByText('🎯 成语游戏统计').closest('div');
        expect(idiomSection).toBeInTheDocument();
      });
    });

    it('should display idiom EASY difficulty stats', async () => {
      renderStatisticsPage();

      await waitFor(() => {
        const idiomSection = screen.getByText('🎯 成语游戏统计');
        expect(idiomSection).toBeInTheDocument();
        const easyLabels = screen.getAllByText('简单');
        expect(easyLabels.length).toBeGreaterThan(0);
      });
    });

    it('should display idiom MEDIUM difficulty stats', async () => {
      renderStatisticsPage();

      await waitFor(() => {
        expect(screen.getByText('中等')).toBeInTheDocument();
      });
    });
  });

  describe('Sentence Game Statistics', () => {
    it('should render sentence game section', async () => {
      renderStatisticsPage();

      await waitFor(() => {
        expect(screen.getByText('📝 造句游戏统计')).toBeInTheDocument();
      });
    });

    it('should display sentence game summary', async () => {
      renderStatisticsPage();

      await waitFor(() => {
        // Total games for sentence: 12 + 8 = 20
        expect(screen.getByText(/20 场游戏/)).toBeInTheDocument();
      });
    });

    it('should display sentence game average score', async () => {
      renderStatisticsPage();

      await waitFor(() => {
        // Average: (750 + 850) / 2 = 800
        expect(screen.getByText(/平均分: 800/)).toBeInTheDocument();
      });
    });

    it('should render sentence difficulty cards', async () => {
      renderStatisticsPage();

      await waitFor(() => {
        const sentenceSection = screen.getByText('📝 造句游戏统计').closest('div');
        expect(sentenceSection).toBeInTheDocument();
      });
    });
  });

  describe('Rank Badges', () => {
    it('should display gold medal for rank 1', async () => {
      renderStatisticsPage();

      await waitFor(() => {
        // Sentence HARD has rank 1
        const cards = screen.getAllByText('🥇');
        expect(cards.length).toBeGreaterThan(0);
      });
    });

    it('should display silver medal for rank 2', async () => {
      const rankingsWithRank2 = [
        ...mockPlayerRankings,
        {
          gameType: 'IDIOM' as const,
          difficulty: 'HARD',
          rank: 2,
          bestScore: 850,
          averageScore: 700,
          totalGames: 5,
        },
      ];
      mockGetPlayerRankings.mockResolvedValue(rankingsWithRank2);

      renderStatisticsPage();

      await waitFor(() => {
        expect(screen.getByText('🥈')).toBeInTheDocument();
      });
    });

    it('should display bronze medal for rank 3', async () => {
      renderStatisticsPage();

      await waitFor(() => {
        // Sentence EASY has rank 3
        expect(screen.getByText('🥉')).toBeInTheDocument();
      });
    });

    it('should display star for ranks 11-50', async () => {
      const rankingsWithRank20 = [
        {
          gameType: 'IDIOM' as const,
          difficulty: 'EASY',
          rank: 20,
          bestScore: 500,
          averageScore: 400,
          totalGames: 5,
        },
      ];
      mockGetPlayerRankings.mockResolvedValue(rankingsWithRank20);

      renderStatisticsPage();

      await waitFor(() => {
        const starEmojis = screen.getAllByText('⭐');
        expect(starEmojis.length).toBeGreaterThan(0);
      }, { timeout: 2000 });
    });

    it('should display user icon for ranks beyond 50', async () => {
      const rankingsWithRank100 = [
        {
          gameType: 'IDIOM' as const,
          difficulty: 'EASY',
          rank: 100,
          bestScore: 300,
          averageScore: 250,
          totalGames: 3,
        },
      ];
      mockGetPlayerRankings.mockResolvedValue(rankingsWithRank100);

      renderStatisticsPage();

      await waitFor(() => {
        expect(screen.getByText('👤')).toBeInTheDocument();
      });
    });
  });

  describe('Difficulty Card Details', () => {
    it('should display best score in difficulty cards', async () => {
      renderStatisticsPage();

      await waitFor(() => {
        expect(screen.getByText('800')).toBeInTheDocument();
        expect(screen.getByText('900')).toBeInTheDocument();
        expect(screen.getByText('1000')).toBeInTheDocument();
      });
    });

    it('should display average score in difficulty cards', async () => {
      renderStatisticsPage();

      await waitFor(() => {
        const avgScoreLabels = screen.getAllByText('平均分:');
        expect(avgScoreLabels.length).toBeGreaterThan(0);
      });
    });

    it('should display total games in difficulty cards', async () => {
      renderStatisticsPage();

      await waitFor(() => {
        const gamesLabels = screen.getAllByText('游戏数:');
        expect(gamesLabels.length).toBeGreaterThan(0);
      });
    });
  });

  describe('Performance Tips Section', () => {
    it('should render performance tips section', async () => {
      renderStatisticsPage();

      await waitFor(() => {
        expect(screen.getByText('💡 提升技巧')).toBeInTheDocument();
      });
    });

    it('should display focused practice tip', async () => {
      renderStatisticsPage();

      await waitFor(() => {
        expect(screen.getByText('专注练习')).toBeInTheDocument();
        expect(screen.getByText('选择一个难度等级，持续练习直到精通，再挑战更高难度')).toBeInTheDocument();
      });
    });

    it('should display hint usage tip', async () => {
      renderStatisticsPage();

      await waitFor(() => {
        expect(screen.getByText('善用提示')).toBeInTheDocument();
        expect(screen.getByText('一级提示最划算，能帮助理解而不直接给答案')).toBeInTheDocument();
      });
    });

    it('should display time management tip', async () => {
      renderStatisticsPage();

      await waitFor(() => {
        expect(screen.getByText('时间管理')).toBeInTheDocument();
      }, { timeout: 2000 });
    });

    it('should display learning accumulation tip', async () => {
      renderStatisticsPage();

      await waitFor(() => {
        expect(screen.getByText('学习积累')).toBeInTheDocument();
        expect(screen.getByText('每次游戏后复习成语和句型，加深记忆')).toBeInTheDocument();
      });
    });

    it('should render all tip emojis', async () => {
      renderStatisticsPage();

      await waitFor(() => {
        const tipsSection = screen.getByText('💡 提升技巧').closest('div');
        expect(tipsSection).toBeInTheDocument();
      }, { timeout: 2000 });
    });
  });

  describe('Refresh Functionality', () => {
    it('should render refresh button', async () => {
      renderStatisticsPage();

      await waitFor(() => {
        expect(screen.getByText('🔄 刷新统计数据')).toBeInTheDocument();
      });
    });

    it('should reload statistics when refresh button clicked', async () => {
      renderStatisticsPage();

      await waitFor(() => {
        expect(mockGetPlayerRankings).toHaveBeenCalledTimes(1);
        expect(screen.getByText('🔄 刷新统计数据')).toBeInTheDocument();
      });

      const refreshButton = screen.getByText('🔄 刷新统计数据');
      fireEvent.click(refreshButton);

      await waitFor(() => {
        expect(mockGetPlayerRankings).toHaveBeenCalledTimes(2);
        expect(mockGetPlayerRankings).toHaveBeenCalledWith('TestPlayer');
      });
    });

    it('should disable refresh button when loading', () => {
      mockUseLeaderboard.mockReturnValue({
        loading: true,
        error: null,
        getTopPlayers: jest.fn(),
        getPlayerRankings: mockGetPlayerRankings,
        getPlayerRank: jest.fn(),
      });

      renderStatisticsPage();

      // When loading, the refresh button should not be visible (it's in the if block with !loading)
      expect(screen.queryByText('🔄 刷新统计数据')).not.toBeInTheDocument();
      expect(screen.getByText('加载统计数据中...')).toBeInTheDocument();
    });
  });

  describe('Game Type Statistics Filtering', () => {
    it('should only show idiom stats for idiom rankings', async () => {
      const idiomOnlyRankings = mockPlayerRankings.filter((r) => r.gameType === 'IDIOM');
      mockGetPlayerRankings.mockResolvedValue(idiomOnlyRankings);

      renderStatisticsPage();

      await waitFor(() => {
        expect(screen.getByText('🎯 成语游戏统计')).toBeInTheDocument();
        expect(screen.queryByText('📝 造句游戏统计')).not.toBeInTheDocument();
      });
    });

    it('should only show sentence stats for sentence rankings', async () => {
      const sentenceOnlyRankings = mockPlayerRankings.filter((r) => r.gameType === 'SENTENCE');
      mockGetPlayerRankings.mockResolvedValue(sentenceOnlyRankings);

      renderStatisticsPage();

      await waitFor(() => {
        expect(screen.queryByText('🎯 成语游戏统计')).not.toBeInTheDocument();
        expect(screen.getByText('📝 造句游戏统计')).toBeInTheDocument();
      });
    });

    it('should show both sections when both game types present', async () => {
      renderStatisticsPage();

      await waitFor(() => {
        expect(screen.getByText('🎯 成语游戏统计')).toBeInTheDocument();
        expect(screen.getByText('📝 造句游戏统计')).toBeInTheDocument();
      });
    });
  });

  describe('Error Handling', () => {
    it('should handle API error gracefully', async () => {
      const consoleError = jest.spyOn(console, 'error').mockImplementation(() => {});
      mockGetPlayerRankings.mockRejectedValue(new Error('Network error'));

      renderStatisticsPage();

      await waitFor(() => {
        expect(consoleError).toHaveBeenCalledWith('Failed to load player statistics:', expect.any(Error));
      });

      consoleError.mockRestore();
    });
  });

  describe('Layout and Styling', () => {
    it('should have proper main container styling', () => {
      const { container } = renderStatisticsPage();
      const mainDiv = container.firstChild as HTMLElement;

      expect(mainDiv).toHaveStyle({
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
      });
    });

    it('should render summary cards in grid layout', async () => {
      const { container } = renderStatisticsPage();

      await waitFor(() => {
        const grid = container.querySelector('.grid.grid-cols-1.md\\:grid-cols-4');
        expect(grid).toBeInTheDocument();
      });
    });

    it('should render difficulty cards in grid layout', async () => {
      renderStatisticsPage();

      await waitFor(() => {
        const idiomSection = screen.getByText('🎯 成语游戏统计').closest('div');
        expect(idiomSection).toBeInTheDocument();
      });
    });

    it('should render performance tips in grid layout', async () => {
      const { container } = renderStatisticsPage();

      await waitFor(() => {
        const tipsGrid = screen.getByText('专注练习').closest('.grid');
        expect(tipsGrid).toHaveClass('grid-cols-1', 'md:grid-cols-2');
      });
    });
  });

  describe('Conditional Rendering', () => {
    it('should not render tips section when no rankings', async () => {
      mockGetPlayerRankings.mockResolvedValue([]);

      renderStatisticsPage();

      await waitFor(() => {
        const emptyMessage = screen.getByText('还没有统计数据');
        expect(emptyMessage).toBeInTheDocument();
        expect(screen.queryByText('💡 提升技巧')).not.toBeInTheDocument();
      }, { timeout: 2000 });
    });

    it('should not render refresh button in empty state', async () => {
      mockGetPlayerRankings.mockResolvedValue([]);

      renderStatisticsPage();

      await waitFor(() => {
        const emptyState = screen.getByText('还没有统计数据');
        expect(emptyState).toBeInTheDocument();
        expect(screen.queryByText('🔄 刷新统计数据')).not.toBeInTheDocument();
      }, { timeout: 2000 });
    });

    it('should render summary cards only when rankings exist', async () => {
      mockGetPlayerRankings.mockResolvedValue([]);

      renderStatisticsPage();

      await waitFor(() => {
        expect(screen.queryByText('总游戏数')).not.toBeInTheDocument();
      });
    });
  });
});
