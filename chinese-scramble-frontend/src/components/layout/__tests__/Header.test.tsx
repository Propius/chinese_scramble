import React from 'react';
import { render, screen } from '@testing-library/react';
import Header from '../Header';

describe('Header', () => {
  describe('Basic Rendering', () => {
    it('should render without crashing', () => {
      render(<Header title="Test Title" />);
      expect(screen.getByText('Test Title')).toBeInTheDocument();
    });

    it('should render with title prop', () => {
      render(<Header title="Welcome to Game" />);
      expect(screen.getByText('Welcome to Game')).toBeInTheDocument();
    });

    it('should render with subtitle prop', () => {
      render(<Header title="Test Title" subtitle="Test Subtitle" />);
      expect(screen.getByText('Test Title')).toBeInTheDocument();
      expect(screen.getByText('Test Subtitle')).toBeInTheDocument();
    });

    it('should render without subtitle when not provided', () => {
      const { container } = render(<Header title="Test Title" />);
      const paragraphs = container.querySelectorAll('p');
      expect(paragraphs.length).toBe(0);
    });

    it('should render title as h1 element', () => {
      render(<Header title="Test Title" />);
      const heading = screen.getByRole('heading', { level: 1 });
      expect(heading).toHaveTextContent('Test Title');
    });
  });

  describe('Card Variant (Default)', () => {
    it('should render card variant by default', () => {
      const { container } = render(<Header title="Test Title" />);
      const header = container.querySelector('header');

      // Card variant should NOT have header element
      expect(header).not.toBeInTheDocument();
    });

    it('should render card variant when explicitly specified', () => {
      const { container } = render(<Header title="Test Title" variant="card" />);
      const header = container.querySelector('header');

      expect(header).not.toBeInTheDocument();
    });

    it('should have correct inline styles for card variant', () => {
      const { container } = render(<Header title="Test Title" variant="card" />);
      const wrapper = container.firstChild as HTMLElement;

      expect(wrapper).toHaveStyle({
        padding: '2rem 1.5rem 0.75rem 1.5rem',
        textAlign: 'center'
      });
    });

    it('should render card variant with subtitle', () => {
      render(<Header title="Test Title" subtitle="Test Subtitle" variant="card" />);
      expect(screen.getByText('Test Title')).toBeInTheDocument();
      expect(screen.getByText('Test Subtitle')).toBeInTheDocument();
    });

    it('should have correct CSS classes for title in card variant', () => {
      render(<Header title="Test Title" variant="card" />);
      const title = screen.getByText('Test Title');

      expect(title).toHaveClass('text-3xl');
      expect(title).toHaveClass('font-bold');
      expect(title).toHaveClass('text-gray-900');
    });

    it('should have correct CSS classes for subtitle in card variant', () => {
      render(<Header title="Test Title" subtitle="Test Subtitle" variant="card" />);
      const subtitle = screen.getByText('Test Subtitle');

      expect(subtitle).toHaveClass('mt-2');
      expect(subtitle).toHaveClass('text-sm');
      expect(subtitle).toHaveClass('text-gray-600');
    });

    it('should not render header tag in card variant', () => {
      const { container } = render(<Header title="Test Title" variant="card" />);
      const headerTag = container.querySelector('header');

      expect(headerTag).not.toBeInTheDocument();
    });
  });

  describe('Standalone Variant', () => {
    it('should render standalone variant when specified', () => {
      const { container } = render(<Header title="Test Title" variant="standalone" />);
      const header = container.querySelector('header');

      expect(header).toBeInTheDocument();
    });

    it('should have semantic header element in standalone variant', () => {
      render(<Header title="Test Title" variant="standalone" />);
      const header = screen.getByRole('banner');

      expect(header).toBeInTheDocument();
    });

    it('should have correct CSS classes for header in standalone variant', () => {
      const { container } = render(<Header title="Test Title" variant="standalone" />);
      const header = container.querySelector('header');

      expect(header).toHaveClass('bg-white');
      expect(header).toHaveClass('shadow');
    });

    it('should have container with correct classes in standalone variant', () => {
      const { container } = render(<Header title="Test Title" variant="standalone" />);
      const containerDiv = container.querySelector('.container');

      expect(containerDiv).toHaveClass('mx-auto');
      expect(containerDiv).toHaveClass('px-4');
      expect(containerDiv).toHaveClass('py-6');
      expect(containerDiv).toHaveClass('text-center');
    });

    it('should render title with correct classes in standalone variant', () => {
      render(<Header title="Test Title" variant="standalone" />);
      const title = screen.getByText('Test Title');

      expect(title).toHaveClass('text-3xl');
      expect(title).toHaveClass('font-bold');
      expect(title).toHaveClass('text-gray-900');
    });

    it('should render subtitle with correct classes in standalone variant', () => {
      render(<Header title="Test Title" subtitle="Test Subtitle" variant="standalone" />);
      const subtitle = screen.getByText('Test Subtitle');

      expect(subtitle).toHaveClass('mt-2');
      expect(subtitle).toHaveClass('text-sm');
      expect(subtitle).toHaveClass('text-gray-600');
    });

    it('should render subtitle as paragraph element in standalone variant', () => {
      render(<Header title="Test Title" subtitle="Test Subtitle" variant="standalone" />);
      const subtitle = screen.getByText('Test Subtitle');

      expect(subtitle.tagName).toBe('P');
    });

    it('should not render subtitle paragraph when subtitle not provided in standalone', () => {
      const { container } = render(<Header title="Test Title" variant="standalone" />);
      const paragraphs = container.querySelectorAll('p');

      expect(paragraphs.length).toBe(0);
    });
  });

  describe('Props Handling', () => {
    it('should handle title with special characters', () => {
      render(<Header title="汉字游戏 - Chinese Game!" />);
      expect(screen.getByText('汉字游戏 - Chinese Game!')).toBeInTheDocument();
    });

    it('should handle subtitle with special characters', () => {
      render(<Header title="Test" subtitle="成语拼字 & Idioms!" />);
      expect(screen.getByText('成语拼字 & Idioms!')).toBeInTheDocument();
    });

    it('should handle empty string title', () => {
      render(<Header title="" />);
      const heading = screen.getByRole('heading', { level: 1 });
      expect(heading).toHaveTextContent('');
    });

    it('should handle empty string subtitle', () => {
      const { container } = render(<Header title="Test" subtitle="" />);
      // Empty subtitle should not render the paragraph element (falsy check in component)
      const paragraphs = container.querySelectorAll('p');
      expect(paragraphs.length).toBe(0);
    });

    it('should handle long title text', () => {
      const longTitle = 'This is a very long title that should still render correctly without breaking the layout or causing issues';
      render(<Header title={longTitle} />);
      expect(screen.getByText(longTitle)).toBeInTheDocument();
    });

    it('should handle long subtitle text', () => {
      const longSubtitle = 'This is a very long subtitle that provides detailed information about the game and its features';
      render(<Header title="Test" subtitle={longSubtitle} />);
      expect(screen.getByText(longSubtitle)).toBeInTheDocument();
    });
  });

  describe('Variant Switching', () => {
    it('should switch from card to standalone variant', () => {
      const { container, rerender } = render(<Header title="Test" variant="card" />);
      let header = container.querySelector('header');
      expect(header).not.toBeInTheDocument();

      rerender(<Header title="Test" variant="standalone" />);
      header = container.querySelector('header');
      expect(header).toBeInTheDocument();
    });

    it('should switch from standalone to card variant', () => {
      const { container, rerender } = render(<Header title="Test" variant="standalone" />);
      let header = container.querySelector('header');
      expect(header).toBeInTheDocument();

      rerender(<Header title="Test" variant="card" />);
      header = container.querySelector('header');
      expect(header).not.toBeInTheDocument();
    });

    it('should maintain subtitle when switching variants', () => {
      const { rerender } = render(<Header title="Test" subtitle="Sub" variant="card" />);
      expect(screen.getByText('Sub')).toBeInTheDocument();

      rerender(<Header title="Test" subtitle="Sub" variant="standalone" />);
      expect(screen.getByText('Sub')).toBeInTheDocument();
    });
  });

  describe('CSS Class Consistency', () => {
    it('should have same title classes in both variants', () => {
      const { container: cardContainer } = render(<Header title="Test" variant="card" />);
      const { container: standaloneContainer } = render(<Header title="Test" variant="standalone" />);

      const cardTitle = cardContainer.querySelector('h1');
      const standaloneTitle = standaloneContainer.querySelector('h1');

      expect(cardTitle?.className).toBe(standaloneTitle?.className);
    });

    it('should have same subtitle classes in both variants', () => {
      const { container: cardContainer } = render(<Header title="Test" subtitle="Sub" variant="card" />);
      const { container: standaloneContainer } = render(<Header title="Test" subtitle="Sub" variant="standalone" />);

      const cardSubtitle = cardContainer.querySelector('p');
      const standaloneSubtitle = standaloneContainer.querySelector('p');

      expect(cardSubtitle?.className).toBe(standaloneSubtitle?.className);
    });
  });

  describe('Export', () => {
    it('should be exportable as default export', () => {
      expect(Header).toBeDefined();
      expect(typeof Header).toBe('function');
    });
  });

  describe('Accessibility', () => {
    it('should have proper heading hierarchy', () => {
      render(<Header title="Main Title" />);
      const heading = screen.getByRole('heading', { level: 1 });
      expect(heading).toBeInTheDocument();
    });

    it('should have banner role in standalone variant', () => {
      render(<Header title="Test" variant="standalone" />);
      const banner = screen.getByRole('banner');
      expect(banner).toBeInTheDocument();
    });

    it('should have semantic HTML in both variants', () => {
      const { container: cardContainer } = render(<Header title="Test" variant="card" />);
      const { container: standaloneContainer } = render(<Header title="Test" variant="standalone" />);

      const cardH1 = cardContainer.querySelector('h1');
      const standaloneH1 = standaloneContainer.querySelector('h1');

      expect(cardH1).toBeInTheDocument();
      expect(standaloneH1).toBeInTheDocument();
    });
  });

  describe('Edge Cases', () => {
    it('should handle undefined subtitle gracefully', () => {
      const { container } = render(<Header title="Test" subtitle={undefined} />);
      const paragraphs = container.querySelectorAll('p');
      expect(paragraphs.length).toBe(0);
    });

    it('should render consistently on multiple rerenders', () => {
      const { container, rerender } = render(<Header title="Test" subtitle="Sub" />);
      const firstRender = container.innerHTML;

      rerender(<Header title="Test" subtitle="Sub" />);
      const secondRender = container.innerHTML;

      expect(firstRender).toBe(secondRender);
    });
  });
});
