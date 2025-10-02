import React from 'react';
import { render, screen } from '@testing-library/react';
import Footer from '../Footer';

describe('Footer', () => {
  it('should render without crashing', () => {
    render(<Footer />);
    const footer = screen.getByRole('contentinfo');
    expect(footer).toBeInTheDocument();
  });

  it('should render copyright text in Chinese', () => {
    render(<Footer />);
    expect(screen.getByText('© 2025 汉字游戏. 版权所有.')).toBeInTheDocument();
  });

  it('should render subtitle in English', () => {
    render(<Footer />);
    expect(screen.getByText('Chinese Word Scramble Game')).toBeInTheDocument();
  });

  it('should have correct footer structure with container', () => {
    const { container } = render(<Footer />);
    const footer = container.querySelector('footer');
    const footerContainer = footer?.querySelector('.container');
    const footerContent = footerContainer?.querySelector('.text-center');

    expect(footer).toBeInTheDocument();
    expect(footerContainer).toBeInTheDocument();
    expect(footerContent).toBeInTheDocument();
  });

  it('should have correct CSS classes for footer', () => {
    const { container } = render(<Footer />);
    const footer = container.querySelector('footer');

    expect(footer).toHaveClass('bg-gray-800');
    expect(footer).toHaveClass('text-white');
    expect(footer).toHaveClass('mt-auto');
  });

  it('should have correct CSS classes for container', () => {
    const { container } = render(<Footer />);
    const footerContainer = container.querySelector('.container');

    expect(footerContainer).toHaveClass('mx-auto');
    expect(footerContainer).toHaveClass('px-4');
    expect(footerContainer).toHaveClass('py-6');
  });

  it('should have text-center class for content alignment', () => {
    const { container } = render(<Footer />);
    const textCenter = container.querySelector('.text-center');

    expect(textCenter).toBeInTheDocument();
    expect(textCenter).toHaveClass('text-center');
  });

  it('should render copyright text with correct styling', () => {
    render(<Footer />);
    const copyrightText = screen.getByText('© 2025 汉字游戏. 版权所有.');

    expect(copyrightText).toBeInTheDocument();
    expect(copyrightText).toHaveClass('text-sm');
  });

  it('should render subtitle with correct styling', () => {
    render(<Footer />);
    const subtitle = screen.getByText('Chinese Word Scramble Game');

    expect(subtitle).toBeInTheDocument();
    expect(subtitle).toHaveClass('text-xs');
    expect(subtitle).toHaveClass('text-gray-400');
    expect(subtitle).toHaveClass('mt-2');
  });

  it('should render both copyright and subtitle as paragraph elements', () => {
    const { container } = render(<Footer />);
    const paragraphs = container.querySelectorAll('p');

    expect(paragraphs.length).toBe(2);
  });

  it('should maintain consistent spacing between elements', () => {
    render(<Footer />);
    const subtitle = screen.getByText('Chinese Word Scramble Game');

    expect(subtitle).toHaveClass('mt-2');
  });

  it('should have semantic HTML structure', () => {
    const { container } = render(<Footer />);
    const footer = container.querySelector('footer');

    expect(footer?.tagName).toBe('FOOTER');
  });

  it('should have proper accessibility role', () => {
    render(<Footer />);
    const footer = screen.getByRole('contentinfo');

    expect(footer).toBeInTheDocument();
  });

  it('should be exportable as default export', () => {
    expect(Footer).toBeDefined();
    expect(typeof Footer).toBe('function');
  });

  it('should render consistently on multiple renders', () => {
    const { container, rerender } = render(<Footer />);
    const firstRender = container.innerHTML;

    rerender(<Footer />);
    const secondRender = container.innerHTML;

    expect(firstRender).toBe(secondRender);
  });

  it('should contain all expected text content', () => {
    const { container } = render(<Footer />);
    const footerText = container.textContent;

    expect(footerText).toContain('© 2025 汉字游戏. 版权所有.');
    expect(footerText).toContain('Chinese Word Scramble Game');
  });
});
