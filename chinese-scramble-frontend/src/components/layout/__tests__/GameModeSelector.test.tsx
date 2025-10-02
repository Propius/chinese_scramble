import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { BrowserRouter, MemoryRouter } from 'react-router-dom';
import GameModeSelector from '../GameModeSelector';
import { GameType, GAME_TYPE_LABELS } from '../../../constants/gameTypes';
import { ROUTES } from '../../../constants/routes';

const mockNavigate = jest.fn();

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
}));

describe('GameModeSelector', () => {
  beforeEach(() => {
    mockNavigate.mockClear();
  });

  const renderWithRouter = (ui: React.ReactElement) => {
    return render(<BrowserRouter>{ui}</BrowserRouter>);
  };

  describe('Basic Rendering', () => {
    it('should render without crashing', () => {
      renderWithRouter(<GameModeSelector />);
      expect(screen.getByText(GAME_TYPE_LABELS[GameType.IDIOM])).toBeInTheDocument();
    });

    it('should render both game mode buttons', () => {
      renderWithRouter(<GameModeSelector />);
      expect(screen.getByText(GAME_TYPE_LABELS[GameType.IDIOM])).toBeInTheDocument();
      expect(screen.getByText(GAME_TYPE_LABELS[GameType.SENTENCE])).toBeInTheDocument();
    });

    it('should render idiom game mode with correct label', () => {
      renderWithRouter(<GameModeSelector />);
      expect(screen.getByText('成语拼字')).toBeInTheDocument();
    });

    it('should render sentence game mode with correct label', () => {
      renderWithRouter(<GameModeSelector />);
      expect(screen.getByText('造句游戏')).toBeInTheDocument();
    });

    it('should render idiom game description', () => {
      renderWithRouter(<GameModeSelector />);
      expect(screen.getByText('将打乱的汉字重新排列成正确的成语')).toBeInTheDocument();
    });

    it('should render sentence game description', () => {
      renderWithRouter(<GameModeSelector />);
      expect(screen.getByText('将打乱的词语重新排列成正确的句子')).toBeInTheDocument();
    });

    it('should render idiom emoji icon', () => {
      renderWithRouter(<GameModeSelector />);
      expect(screen.getByText('📝')).toBeInTheDocument();
    });

    it('should render sentence emoji icon', () => {
      renderWithRouter(<GameModeSelector />);
      expect(screen.getByText('✍️')).toBeInTheDocument();
    });
  });

  describe('Navigation', () => {
    it('should navigate to idiom game when idiom button is clicked', () => {
      renderWithRouter(<GameModeSelector />);
      const idiomButton = screen.getByText(GAME_TYPE_LABELS[GameType.IDIOM]).closest('button');

      fireEvent.click(idiomButton!);

      expect(mockNavigate).toHaveBeenCalledWith(ROUTES.IDIOM_GAME);
      expect(mockNavigate).toHaveBeenCalledTimes(1);
    });

    it('should navigate to sentence game when sentence button is clicked', () => {
      renderWithRouter(<GameModeSelector />);
      const sentenceButton = screen.getByText(GAME_TYPE_LABELS[GameType.SENTENCE]).closest('button');

      fireEvent.click(sentenceButton!);

      expect(mockNavigate).toHaveBeenCalledWith(ROUTES.SENTENCE_GAME);
      expect(mockNavigate).toHaveBeenCalledTimes(1);
    });

    it('should call navigate only once per click', () => {
      renderWithRouter(<GameModeSelector />);
      const idiomButton = screen.getByText(GAME_TYPE_LABELS[GameType.IDIOM]).closest('button');

      fireEvent.click(idiomButton!);

      expect(mockNavigate).toHaveBeenCalledTimes(1);
    });

    it('should handle multiple clicks on different buttons', () => {
      renderWithRouter(<GameModeSelector />);
      const idiomButton = screen.getByText(GAME_TYPE_LABELS[GameType.IDIOM]).closest('button');
      const sentenceButton = screen.getByText(GAME_TYPE_LABELS[GameType.SENTENCE]).closest('button');

      fireEvent.click(idiomButton!);
      fireEvent.click(sentenceButton!);

      expect(mockNavigate).toHaveBeenCalledTimes(2);
      expect(mockNavigate).toHaveBeenNthCalledWith(1, ROUTES.IDIOM_GAME);
      expect(mockNavigate).toHaveBeenNthCalledWith(2, ROUTES.SENTENCE_GAME);
    });
  });

  describe('Styling and Layout', () => {
    it('should have grid layout with correct classes', () => {
      const { container } = renderWithRouter(<GameModeSelector />);
      const grid = container.querySelector('.grid');

      expect(grid).toHaveClass('grid-cols-1');
      expect(grid).toHaveClass('md:grid-cols-2');
      expect(grid).toHaveClass('gap-6');
      expect(grid).toHaveClass('max-w-3xl');
      expect(grid).toHaveClass('mx-auto');
      expect(grid).toHaveClass('mb-16');
    });

    it('should render idiom button with correct base styling classes', () => {
      const { container } = renderWithRouter(<GameModeSelector />);
      const buttons = container.querySelectorAll('button');
      const idiomButton = buttons[0];

      expect(idiomButton).toHaveClass('p-8');
      expect(idiomButton).toHaveClass('transition-all');
      expect(idiomButton).toHaveClass('transform');
      expect(idiomButton).toHaveClass('hover:scale-105');
      expect(idiomButton).toHaveClass('shadow-lg');
      expect(idiomButton).toHaveClass('border-2');
    });

    it('should render sentence button with correct base styling classes', () => {
      const { container } = renderWithRouter(<GameModeSelector />);
      const buttons = container.querySelectorAll('button');
      const sentenceButton = buttons[1];

      expect(sentenceButton).toHaveClass('p-8');
      expect(sentenceButton).toHaveClass('transition-all');
      expect(sentenceButton).toHaveClass('transform');
      expect(sentenceButton).toHaveClass('hover:scale-105');
      expect(sentenceButton).toHaveClass('shadow-lg');
      expect(sentenceButton).toHaveClass('border-2');
    });

    it('should have correct inline styles for idiom button', () => {
      const { container } = renderWithRouter(<GameModeSelector />);
      const buttons = container.querySelectorAll('button');
      const idiomButton = buttons[0] as HTMLElement;

      expect(idiomButton.style.borderRadius).toBe('32px');
      expect(idiomButton.style.borderColor).toBe('rgba(234, 179, 8, 0.4)');
      expect(idiomButton.style.color).toBe('rgb(120, 53, 15)');
      expect(idiomButton.style.minHeight).toBe('200px');
      // Note: React doesn't serialize background shorthand to style attribute
    });

    it('should have correct inline styles for sentence button', () => {
      const { container } = renderWithRouter(<GameModeSelector />);
      const buttons = container.querySelectorAll('button');
      const sentenceButton = buttons[1] as HTMLElement;

      expect(sentenceButton.style.borderRadius).toBe('32px');
      expect(sentenceButton.style.borderColor).toBe('rgba(52, 211, 153, 0.4)');
      expect(sentenceButton.style.color).toBe('rgb(6, 78, 59)');
      expect(sentenceButton.style.minHeight).toBe('200px');
      // Note: React doesn't serialize background shorthand to style attribute
    });

    it('should render emoji with correct styling', () => {
      const { container } = renderWithRouter(<GameModeSelector />);
      const emojis = container.querySelectorAll('.text-6xl');

      expect(emojis.length).toBe(2);
      emojis.forEach(emoji => {
        expect(emoji).toHaveClass('text-6xl');
        expect(emoji).toHaveClass('mb-4');
      });
    });

    it('should render game mode titles with correct styling', () => {
      const { container } = renderWithRouter(<GameModeSelector />);
      const titles = container.querySelectorAll('.text-3xl');

      expect(titles.length).toBe(2);
      titles.forEach(title => {
        expect(title).toHaveClass('text-3xl');
        expect(title).toHaveClass('font-extrabold');
        expect(title).toHaveClass('mb-3');
      });
    });

    it('should render descriptions with correct styling', () => {
      const { container } = renderWithRouter(<GameModeSelector />);
      const descriptions = container.querySelectorAll('.text-lg');

      expect(descriptions.length).toBe(2);
      descriptions.forEach(desc => {
        expect(desc).toHaveClass('text-lg');
        expect(desc).toHaveClass('font-medium');
      });
    });

    it('should have correct text color for idiom description', () => {
      const { container } = renderWithRouter(<GameModeSelector />);
      const descriptions = container.querySelectorAll('.text-lg');
      const idiomDesc = descriptions[0] as HTMLElement;

      expect(idiomDesc.style.color).toBe('rgb(161, 98, 7)');
    });

    it('should have correct text color for sentence description', () => {
      const { container } = renderWithRouter(<GameModeSelector />);
      const descriptions = container.querySelectorAll('.text-lg');
      const sentenceDesc = descriptions[1] as HTMLElement;

      expect(sentenceDesc.style.color).toBe('rgb(6, 95, 70)');
    });
  });

  describe('Hover Effects', () => {
    it('should apply hover styles to idiom button on mouse enter', () => {
      const { container } = renderWithRouter(<GameModeSelector />);
      const buttons = container.querySelectorAll('button');
      const idiomButton = buttons[0] as HTMLElement;

      fireEvent.mouseEnter(idiomButton);

      expect(idiomButton.style.boxShadow).toBe('0 20px 40px rgba(234, 179, 8, 0.3)');
      expect(idiomButton.style.transform).toBe('scale(1.05)');
    });

    it('should remove hover styles from idiom button on mouse leave', () => {
      const { container } = renderWithRouter(<GameModeSelector />);
      const buttons = container.querySelectorAll('button');
      const idiomButton = buttons[0] as HTMLElement;

      fireEvent.mouseEnter(idiomButton);
      fireEvent.mouseLeave(idiomButton);

      expect(idiomButton.style.boxShadow).toBe('0 10px 25px rgba(0, 0, 0, 0.1)');
      expect(idiomButton.style.transform).toBe('scale(1)');
    });

    it('should apply hover styles to sentence button on mouse enter', () => {
      const { container } = renderWithRouter(<GameModeSelector />);
      const buttons = container.querySelectorAll('button');
      const sentenceButton = buttons[1] as HTMLElement;

      fireEvent.mouseEnter(sentenceButton);

      expect(sentenceButton.style.boxShadow).toBe('0 20px 40px rgba(52, 211, 153, 0.3)');
      expect(sentenceButton.style.transform).toBe('scale(1.05)');
    });

    it('should remove hover styles from sentence button on mouse leave', () => {
      const { container } = renderWithRouter(<GameModeSelector />);
      const buttons = container.querySelectorAll('button');
      const sentenceButton = buttons[1] as HTMLElement;

      fireEvent.mouseEnter(sentenceButton);
      fireEvent.mouseLeave(sentenceButton);

      expect(sentenceButton.style.boxShadow).toBe('0 10px 25px rgba(0, 0, 0, 0.1)');
      expect(sentenceButton.style.transform).toBe('scale(1)');
    });

    it('should handle multiple hover events', () => {
      const { container } = renderWithRouter(<GameModeSelector />);
      const buttons = container.querySelectorAll('button');
      const idiomButton = buttons[0] as HTMLElement;

      fireEvent.mouseEnter(idiomButton);
      fireEvent.mouseLeave(idiomButton);
      fireEvent.mouseEnter(idiomButton);

      expect(idiomButton.style.boxShadow).toBe('0 20px 40px rgba(234, 179, 8, 0.3)');
      expect(idiomButton.style.transform).toBe('scale(1.05)');
    });
  });

  describe('Button Interaction', () => {
    it('should render two clickable buttons', () => {
      const { container } = renderWithRouter(<GameModeSelector />);
      const buttons = container.querySelectorAll('button');

      expect(buttons.length).toBe(2);
    });

    it('should be able to click idiom button multiple times', () => {
      renderWithRouter(<GameModeSelector />);
      const idiomButton = screen.getByText(GAME_TYPE_LABELS[GameType.IDIOM]).closest('button');

      fireEvent.click(idiomButton!);
      fireEvent.click(idiomButton!);

      expect(mockNavigate).toHaveBeenCalledTimes(2);
    });

    it('should maintain state after clicking', () => {
      renderWithRouter(<GameModeSelector />);
      const idiomButton = screen.getByText(GAME_TYPE_LABELS[GameType.IDIOM]).closest('button');

      fireEvent.click(idiomButton!);

      // Component should still render after navigation
      expect(screen.getByText(GAME_TYPE_LABELS[GameType.IDIOM])).toBeInTheDocument();
    });
  });

  describe('Accessibility', () => {
    it('should render buttons with button type', () => {
      const { container } = renderWithRouter(<GameModeSelector />);
      const buttons = container.querySelectorAll('button');

      buttons.forEach(button => {
        expect(button.tagName).toBe('BUTTON');
      });
    });

    it('should have descriptive content for screen readers', () => {
      renderWithRouter(<GameModeSelector />);
      expect(screen.getByText('成语拼字')).toBeInTheDocument();
      expect(screen.getByText('将打乱的汉字重新排列成正确的成语')).toBeInTheDocument();
      expect(screen.getByText('造句游戏')).toBeInTheDocument();
      expect(screen.getByText('将打乱的词语重新排列成正确的句子')).toBeInTheDocument();
    });

    it('should have keyboard accessible buttons', () => {
      const { container } = renderWithRouter(<GameModeSelector />);
      const buttons = container.querySelectorAll('button');

      buttons.forEach(button => {
        expect(button).not.toHaveAttribute('disabled');
      });
    });
  });

  describe('Export', () => {
    it('should be exportable as default export', () => {
      expect(GameModeSelector).toBeDefined();
      expect(typeof GameModeSelector).toBe('function');
    });
  });

  describe('Edge Cases', () => {
    it('should handle rapid button clicks', () => {
      renderWithRouter(<GameModeSelector />);
      const idiomButton = screen.getByText(GAME_TYPE_LABELS[GameType.IDIOM]).closest('button');

      // Rapid clicks
      for (let i = 0; i < 5; i++) {
        fireEvent.click(idiomButton!);
      }

      expect(mockNavigate).toHaveBeenCalledTimes(5);
    });

    it('should render consistently on multiple renders', () => {
      const { container, rerender } = renderWithRouter(<GameModeSelector />);
      const firstRender = container.innerHTML;

      rerender(<BrowserRouter><GameModeSelector /></BrowserRouter>);
      const secondRender = container.innerHTML;

      expect(firstRender).toBe(secondRender);
    });

    it('should handle clicking on button content (emoji, title, description)', () => {
      renderWithRouter(<GameModeSelector />);

      // Click on emoji
      const emoji = screen.getByText('📝');
      fireEvent.click(emoji);
      expect(mockNavigate).toHaveBeenCalledWith(ROUTES.IDIOM_GAME);

      mockNavigate.mockClear();

      // Click on title
      const title = screen.getByText('成语拼字');
      fireEvent.click(title);
      expect(mockNavigate).toHaveBeenCalledWith(ROUTES.IDIOM_GAME);

      mockNavigate.mockClear();

      // Click on description
      const description = screen.getByText('将打乱的汉字重新排列成正确的成语');
      fireEvent.click(description);
      expect(mockNavigate).toHaveBeenCalledWith(ROUTES.IDIOM_GAME);
    });
  });

  describe('Component Structure', () => {
    it('should render exactly two buttons', () => {
      const { container } = renderWithRouter(<GameModeSelector />);
      const buttons = container.querySelectorAll('button');

      expect(buttons.length).toBe(2);
    });

    it('should have idiom button as first button', () => {
      const { container } = renderWithRouter(<GameModeSelector />);
      const buttons = container.querySelectorAll('button');
      const firstButton = buttons[0];

      expect(firstButton.textContent).toContain('成语拼字');
    });

    it('should have sentence button as second button', () => {
      const { container } = renderWithRouter(<GameModeSelector />);
      const buttons = container.querySelectorAll('button');
      const secondButton = buttons[1];

      expect(secondButton.textContent).toContain('造句游戏');
    });
  });
});
