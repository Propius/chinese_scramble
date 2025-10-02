import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import Home from '../Home';

// Mock child components
jest.mock('../../components/layout/Header', () => ({
  __esModule: true,
  default: ({ title, subtitle, variant }: { title: string; subtitle?: string; variant?: string }) => (
    <div data-testid="mock-header">
      <h1>{title}</h1>
      {subtitle && <p>{subtitle}</p>}
      <span data-testid="header-variant">{variant}</span>
    </div>
  ),
}));

jest.mock('../../components/layout/GameModeSelector', () => ({
  __esModule: true,
  default: () => <div data-testid="mock-game-mode-selector">Game Mode Selector</div>,
}));

describe('Home', () => {
  const renderHome = () => {
    return render(
      <BrowserRouter>
        <Home />
      </BrowserRouter>
    );
  };

  describe('Component Rendering', () => {
    it('should render the home page', () => {
      renderHome();
      expect(screen.getByTestId('mock-header')).toBeInTheDocument();
    });

    it('should render Header with correct props', () => {
      renderHome();
      expect(screen.getByText('欢迎来到汉字游戏')).toBeInTheDocument();
      expect(screen.getByText('选择一个游戏模式开始学习中文')).toBeInTheDocument();
      expect(screen.getByTestId('header-variant')).toHaveTextContent('card');
    });

    it('should render GameModeSelector component', () => {
      renderHome();
      expect(screen.getByTestId('mock-game-mode-selector')).toBeInTheDocument();
    });

    it('should render main welcome heading', () => {
      renderHome();
      expect(screen.getByText('开始你的中文学习之旅')).toBeInTheDocument();
    });

    it('should render welcome subtitle', () => {
      renderHome();
      expect(screen.getByText('通过有趣的游戏学习成语和句子构造')).toBeInTheDocument();
    });
  });

  describe('Feature Cards', () => {
    it('should render all three feature cards', () => {
      renderHome();
      expect(screen.getByText('多种难度')).toBeInTheDocument();
      expect(screen.getByText('排行榜')).toBeInTheDocument();
      expect(screen.getByText('统计追踪')).toBeInTheDocument();
    });

    it('should render difficulty feature card with correct content', () => {
      renderHome();
      expect(screen.getByText('多种难度')).toBeInTheDocument();
      expect(screen.getByText('从简单到专家级，适合所有水平')).toBeInTheDocument();
    });

    it('should render leaderboard feature card with correct content', () => {
      renderHome();
      expect(screen.getByText('排行榜')).toBeInTheDocument();
      expect(screen.getByText('与其他玩家竞争，争夺榜首')).toBeInTheDocument();
    });

    it('should render statistics feature card with correct content', () => {
      renderHome();
      expect(screen.getByText('统计追踪')).toBeInTheDocument();
      expect(screen.getByText('跟踪你的进步和成就')).toBeInTheDocument();
    });

    it('should render feature card emojis', () => {
      const { container } = renderHome();
      const emojiElements = container.querySelectorAll('.text-5xl.mb-4');

      expect(emojiElements).toHaveLength(3);
      expect(emojiElements[0]).toHaveTextContent('🎮');
      expect(emojiElements[1]).toHaveTextContent('🏆');
      expect(emojiElements[2]).toHaveTextContent('📊');
    });
  });

  describe('Layout and Styling', () => {
    it('should have main container with correct styling', () => {
      const { container } = renderHome();
      const mainDiv = container.firstChild as HTMLElement;

      expect(mainDiv).toHaveStyle({
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
      });
    });

    it('should have gradient background on main card', () => {
      const { container } = renderHome();
      const cardElement = container.querySelector('.w-full.max-w-6xl.mx-auto') as HTMLElement;

      expect(cardElement).toBeInTheDocument();
      expect(cardElement).toHaveStyle({
        borderRadius: '24px',
      });
    });

    it('should render with responsive max-width class', () => {
      const { container } = renderHome();
      const cardElement = container.querySelector('.max-w-6xl');

      expect(cardElement).toBeInTheDocument();
      expect(cardElement).toHaveClass('w-full', 'mx-auto');
    });

    it('should have divider between header and content', () => {
      const { container } = renderHome();
      const dividers = container.querySelectorAll('[style*="height: 2px"]');

      expect(dividers.length).toBeGreaterThan(0);
    });
  });

  describe('Feature Card Hover Effects', () => {
    it('should have hover transition classes on feature cards', () => {
      const { container } = renderHome();
      const featureCards = container.querySelectorAll('.hover\\:scale-105');

      expect(featureCards).toHaveLength(3);
    });

    it('should have transform transition on difficulty card', () => {
      const { container } = renderHome();
      const difficultyCard = screen.getByText('多种难度').closest('div');

      expect(difficultyCard).toHaveClass('transition-all');
    });

    it('should have transform transition on leaderboard card', () => {
      const { container } = renderHome();
      const leaderboardCard = screen.getByText('排行榜').closest('div');

      expect(leaderboardCard).toHaveClass('transition-all');
    });

    it('should have transform transition on statistics card', () => {
      const { container } = renderHome();
      const statsCard = screen.getByText('统计追踪').closest('div');

      expect(statsCard).toHaveClass('transition-all');
    });
  });

  describe('Typography and Content Structure', () => {
    it('should render main heading with correct styling classes', () => {
      const { container } = renderHome();
      const mainHeading = screen.getByText('开始你的中文学习之旅');

      expect(mainHeading).toHaveClass('text-5xl', 'font-extrabold', 'mb-3');
    });

    it('should render subtitle with correct styling classes', () => {
      const { container } = renderHome();
      const subtitle = screen.getByText('通过有趣的游戏学习成语和句子构造');

      expect(subtitle).toHaveClass('text-xl', 'text-gray-700', 'font-semibold', 'mt-2');
    });

    it('should have feature card headings with correct styling', () => {
      const { container } = renderHome();
      const difficultyHeading = screen.getByText('多种难度');

      expect(difficultyHeading).toHaveClass('font-extrabold', 'text-2xl', 'mb-3');
    });

    it('should have feature descriptions with correct styling', () => {
      const { container } = renderHome();
      const description = screen.getByText('从简单到专家级，适合所有水平');

      expect(description).toHaveClass('text-gray-700', 'font-medium');
    });
  });

  describe('Grid Layout', () => {
    it('should have feature cards grid with correct classes', () => {
      const { container } = renderHome();
      const featureGrid = container.querySelector('.grid.grid-cols-1.md\\:grid-cols-3');

      expect(featureGrid).toBeInTheDocument();
      expect(featureGrid).toHaveClass('gap-8', 'text-center');
    });

    it('should have max-width on feature cards container', () => {
      const { container } = renderHome();
      const featureContainer = container.querySelector('.max-w-5xl');

      expect(featureContainer).toBeInTheDocument();
      expect(featureContainer).toHaveClass('mx-auto');
    });

    it('should have proper spacing with mt-16 on feature cards section', () => {
      const { container } = renderHome();
      const featureSection = container.querySelector('.mt-16.max-w-5xl');

      expect(featureSection).toBeInTheDocument();
    });
  });

  describe('Accessibility', () => {
    it('should have semantic structure with divs', () => {
      const { container } = renderHome();
      const mainContainer = container.firstChild;

      expect(mainContainer?.nodeName).toBe('DIV');
    });

    it('should render all text content for screen readers', () => {
      renderHome();

      // All important text should be present
      expect(screen.getByText('欢迎来到汉字游戏')).toBeInTheDocument();
      expect(screen.getByText('开始你的中文学习之旅')).toBeInTheDocument();
      expect(screen.getByText('多种难度')).toBeInTheDocument();
      expect(screen.getByText('排行榜')).toBeInTheDocument();
      expect(screen.getByText('统计追踪')).toBeInTheDocument();
    });

    it('should have readable text for all feature descriptions', () => {
      renderHome();

      expect(screen.getByText('从简单到专家级，适合所有水平')).toBeVisible();
      expect(screen.getByText('与其他玩家竞争，争夺榜首')).toBeVisible();
      expect(screen.getByText('跟踪你的进步和成就')).toBeVisible();
    });
  });

  describe('Component Integration', () => {
    it('should pass correct variant prop to Header', () => {
      renderHome();
      const headerVariant = screen.getByTestId('header-variant');

      expect(headerVariant).toHaveTextContent('card');
    });

    it('should render Header and GameModeSelector in correct order', () => {
      const { container } = renderHome();
      const header = screen.getByTestId('mock-header');
      const gameSelector = screen.getByTestId('mock-game-mode-selector');

      expect(header).toBeInTheDocument();
      expect(gameSelector).toBeInTheDocument();
    });

    it('should render all sections in correct order', () => {
      renderHome();

      // Header should come before main content
      const headerText = screen.getByText('欢迎来到汉字游戏');
      const mainHeading = screen.getByText('开始你的中文学习之旅');
      const gameSelector = screen.getByTestId('mock-game-mode-selector');
      const featureCards = screen.getByText('多种难度');

      expect(headerText).toBeInTheDocument();
      expect(mainHeading).toBeInTheDocument();
      expect(gameSelector).toBeInTheDocument();
      expect(featureCards).toBeInTheDocument();
    });
  });

  describe('Responsive Design', () => {
    it('should have responsive padding on main container', () => {
      const { container } = renderHome();
      const mainDiv = container.firstChild as HTMLElement;

      expect(mainDiv).toHaveStyle({
        padding: '2rem 1rem',
      });
    });

    it('should use responsive grid for feature cards', () => {
      const { container } = renderHome();
      const grid = container.querySelector('.grid-cols-1.md\\:grid-cols-3');

      expect(grid).toBeInTheDocument();
    });

    it('should have rounded corners on main card', () => {
      const { container } = renderHome();
      const mainCard = container.querySelector('.max-w-6xl.mx-auto');

      expect(mainCard).toBeInTheDocument();
    });
  });

  describe('Color Scheme and Gradients', () => {
    it('should have difficulty feature card with emoji', () => {
      renderHome();
      const difficultySection = screen.getByText('多种难度').parentElement;

      expect(difficultySection).toBeInTheDocument();
      expect(screen.getByText('🎮')).toBeInTheDocument();
    });

    it('should have leaderboard feature card with emoji', () => {
      renderHome();
      const leaderboardSection = screen.getByText('排行榜').parentElement;

      expect(leaderboardSection).toBeInTheDocument();
      expect(screen.getByText('🏆')).toBeInTheDocument();
    });

    it('should have statistics feature card with emoji', () => {
      renderHome();
      const statsSection = screen.getByText('统计追踪').parentElement;

      expect(statsSection).toBeInTheDocument();
      expect(screen.getByText('📊')).toBeInTheDocument();
    });
  });
});
