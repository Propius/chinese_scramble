import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { BrowserRouter, MemoryRouter } from 'react-router-dom';
import Navigation from '../Navigation';
import { ROUTES } from '../../../constants/routes';

// Mock react-i18next
jest.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const translations: Record<string, string> = {
        'navigation.home': 'Home',
        'navigation.idiomGame': 'Idiom Game',
        'navigation.sentenceGame': 'Sentence Game',
        'navigation.leaderboard': 'Leaderboard',
        'navigation.statistics': 'Statistics',
      };
      return translations[key] || key;
    },
  }),
}));

describe('Navigation', () => {
  const renderWithRouter = (ui: React.ReactElement, { route = '/' } = {}) => {
    return render(
      <MemoryRouter initialEntries={[route]}>
        {ui}
      </MemoryRouter>
    );
  };

  it('should render without crashing', () => {
    renderWithRouter(<Navigation />);
    const nav = screen.getByRole('navigation');
    expect(nav).toBeInTheDocument();
  });

  it('should render brand logo/title', () => {
    renderWithRouter(<Navigation />);
    expect(screen.getByText('汉字游戏')).toBeInTheDocument();
  });

  it('should render all navigation links', () => {
    renderWithRouter(<Navigation />);

    expect(screen.getByText('Home')).toBeInTheDocument();
    expect(screen.getByText('Idiom Game')).toBeInTheDocument();
    expect(screen.getByText('Sentence Game')).toBeInTheDocument();
    expect(screen.getByText('Leaderboard')).toBeInTheDocument();
    expect(screen.getByText('Statistics')).toBeInTheDocument();
  });

  it('should have correct number of navigation items', () => {
    renderWithRouter(<Navigation />);
    const navLinks = screen.getAllByRole('link');

    // 5 nav items + 1 brand link = 6 total links
    expect(navLinks.length).toBe(6);
  });

  it('should brand link to home route', () => {
    renderWithRouter(<Navigation />);
    const brandLink = screen.getByText('汉字游戏').closest('a');

    expect(brandLink).toHaveAttribute('href', ROUTES.HOME);
  });

  it('should have correct href for Home link', () => {
    renderWithRouter(<Navigation />);
    const homeLink = screen.getByText('Home').closest('a');

    expect(homeLink).toHaveAttribute('href', ROUTES.HOME);
  });

  it('should have correct href for Idiom Game link', () => {
    renderWithRouter(<Navigation />);
    const idiomLink = screen.getByText('Idiom Game').closest('a');

    expect(idiomLink).toHaveAttribute('href', ROUTES.IDIOM_GAME);
  });

  it('should have correct href for Sentence Game link', () => {
    renderWithRouter(<Navigation />);
    const sentenceLink = screen.getByText('Sentence Game').closest('a');

    expect(sentenceLink).toHaveAttribute('href', ROUTES.SENTENCE_GAME);
  });

  it('should have correct href for Leaderboard link', () => {
    renderWithRouter(<Navigation />);
    const leaderboardLink = screen.getByText('Leaderboard').closest('a');

    expect(leaderboardLink).toHaveAttribute('href', ROUTES.LEADERBOARD);
  });

  it('should have correct href for Statistics link', () => {
    renderWithRouter(<Navigation />);
    const statsLink = screen.getByText('Statistics').closest('a');

    expect(statsLink).toHaveAttribute('href', ROUTES.STATISTICS);
  });

  it('should highlight active link on home page', () => {
    renderWithRouter(<Navigation />, { route: ROUTES.HOME });
    const homeLinks = screen.getAllByText('Home');
    const activeHomeLink = homeLinks.find(link => link.classList.contains('active'));

    expect(activeHomeLink).toBeInTheDocument();
  });

  it('should highlight active link on idiom game page', () => {
    renderWithRouter(<Navigation />, { route: ROUTES.IDIOM_GAME });
    const idiomLink = screen.getByText('Idiom Game');

    expect(idiomLink).toHaveClass('active');
  });

  it('should highlight active link on sentence game page', () => {
    renderWithRouter(<Navigation />, { route: ROUTES.SENTENCE_GAME });
    const sentenceLink = screen.getByText('Sentence Game');

    expect(sentenceLink).toHaveClass('active');
  });

  it('should highlight active link on leaderboard page', () => {
    renderWithRouter(<Navigation />, { route: ROUTES.LEADERBOARD });
    const leaderboardLink = screen.getByText('Leaderboard');

    expect(leaderboardLink).toHaveClass('active');
  });

  it('should highlight active link on statistics page', () => {
    renderWithRouter(<Navigation />, { route: ROUTES.STATISTICS });
    const statsLink = screen.getByText('Statistics');

    expect(statsLink).toHaveClass('active');
  });

  it('should only highlight one active link at a time', () => {
    renderWithRouter(<Navigation />, { route: ROUTES.IDIOM_GAME });
    const activeLinks = screen.getAllByRole('link').filter(link =>
      link.classList.contains('active')
    );

    expect(activeLinks.length).toBe(1);
  });

  it('should have mobile toggle button', () => {
    renderWithRouter(<Navigation />);
    const toggleButton = screen.getByRole('button', { name: /toggle navigation/i });

    expect(toggleButton).toBeInTheDocument();
  });

  it('should toggle mobile menu when button is clicked', () => {
    const { container } = renderWithRouter(<Navigation />);
    const toggleButton = screen.getByRole('button', { name: /toggle navigation/i });
    const navbarCollapse = container.querySelector('#navbarNav');

    // Initially collapsed (no 'show' class)
    expect(navbarCollapse).not.toHaveClass('show');

    // Click to expand
    fireEvent.click(toggleButton);
    expect(navbarCollapse).toHaveClass('show');

    // Click to collapse
    fireEvent.click(toggleButton);
    expect(navbarCollapse).not.toHaveClass('show');
  });

  it('should update aria-expanded attribute on toggle', () => {
    renderWithRouter(<Navigation />);
    const toggleButton = screen.getByRole('button', { name: /toggle navigation/i });

    // Initially collapsed
    expect(toggleButton).toHaveAttribute('aria-expanded', 'false');

    // Click to expand
    fireEvent.click(toggleButton);
    expect(toggleButton).toHaveAttribute('aria-expanded', 'true');
  });

  it('should close mobile menu when nav link is clicked', () => {
    const { container } = renderWithRouter(<Navigation />);
    const toggleButton = screen.getByRole('button', { name: /toggle navigation/i });
    const navbarCollapse = container.querySelector('#navbarNav');

    // Open menu
    fireEvent.click(toggleButton);
    expect(navbarCollapse).toHaveClass('show');

    // Click a nav link
    const homeLink = screen.getByText('Home');
    fireEvent.click(homeLink);

    // Menu should close
    expect(navbarCollapse).not.toHaveClass('show');
  });

  it('should have correct navbar CSS classes', () => {
    const { container } = renderWithRouter(<Navigation />);
    const nav = container.querySelector('nav');

    expect(nav).toHaveClass('navbar');
    expect(nav).toHaveClass('navbar-expand-lg');
    expect(nav).toHaveClass('navbar-dark');
    expect(nav).toHaveClass('bg-primary');
    expect(nav).toHaveClass('shadow');
  });

  it('should have correct brand CSS classes', () => {
    renderWithRouter(<Navigation />);
    const brandLink = screen.getByText('汉字游戏');

    expect(brandLink).toHaveClass('navbar-brand');
    expect(brandLink).toHaveClass('fw-bold');
    expect(brandLink).toHaveClass('fs-4');
  });

  it('should have correct nav-link class for all links', () => {
    renderWithRouter(<Navigation />);
    const homeLink = screen.getByText('Home');
    const idiomLink = screen.getByText('Idiom Game');
    const sentenceLink = screen.getByText('Sentence Game');

    expect(homeLink).toHaveClass('nav-link');
    expect(idiomLink).toHaveClass('nav-link');
    expect(sentenceLink).toHaveClass('nav-link');
  });

  it('should have navbar-toggler-icon for mobile button', () => {
    const { container } = renderWithRouter(<Navigation />);
    const togglerIcon = container.querySelector('.navbar-toggler-icon');

    expect(togglerIcon).toBeInTheDocument();
  });

  it('should have container-fluid class', () => {
    const { container } = renderWithRouter(<Navigation />);
    const containerFluid = container.querySelector('.container-fluid');

    expect(containerFluid).toBeInTheDocument();
  });

  it('should have ms-auto class for right-aligned nav items', () => {
    const { container } = renderWithRouter(<Navigation />);
    const navbarNav = container.querySelector('.navbar-nav');

    expect(navbarNav).toHaveClass('ms-auto');
  });

  it('should render nav items in unordered list', () => {
    const { container } = renderWithRouter(<Navigation />);
    const ul = container.querySelector('ul.navbar-nav');
    const listItems = ul?.querySelectorAll('li.nav-item');

    expect(ul).toBeInTheDocument();
    expect(listItems?.length).toBe(5);
  });

  it('should maintain navigation state across rerenders', () => {
    const { rerender } = renderWithRouter(<Navigation />, { route: ROUTES.HOME });
    const firstRender = screen.getByText('Home');

    expect(firstRender).toBeInTheDocument();

    rerender(
      <MemoryRouter initialEntries={[ROUTES.HOME]}>
        <Navigation />
      </MemoryRouter>
    );

    const secondRender = screen.getByText('Home');
    expect(secondRender).toBeInTheDocument();
  });

  it('should have proper accessibility attributes', () => {
    renderWithRouter(<Navigation />);
    const toggleButton = screen.getByRole('button', { name: /toggle navigation/i });

    expect(toggleButton).toHaveAttribute('aria-controls', 'navbarNav');
    expect(toggleButton).toHaveAttribute('aria-label', 'Toggle navigation');
  });

  it('should handle rapid toggle clicks', () => {
    const { container } = renderWithRouter(<Navigation />);
    const toggleButton = screen.getByRole('button', { name: /toggle navigation/i });
    const navbarCollapse = container.querySelector('#navbarNav');

    // Rapid clicks
    fireEvent.click(toggleButton);
    fireEvent.click(toggleButton);
    fireEvent.click(toggleButton);

    // Should be expanded after odd number of clicks
    expect(navbarCollapse).toHaveClass('show');
  });
});
