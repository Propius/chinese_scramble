import React from 'react';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter, MemoryRouter } from 'react-router-dom';
import Sidebar from '../Sidebar';
import { ROUTES } from '../../../constants/routes';
import { usernameUtils } from '../../../utils/usernameUtils';

// Mock dependencies
jest.mock('../../../utils/usernameUtils', () => ({
  usernameUtils: {
    getUsername: jest.fn(),
    hasUsername: jest.fn(),
    setUsername: jest.fn(),
    clearUsername: jest.fn(),
  },
}));

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

describe('Sidebar Component', () => {
  let mockGetUsername: jest.Mock;
  let mockOnChangeUsername: jest.Mock;
  let mockOnCollapseChange: jest.Mock;

  beforeEach(() => {
    mockGetUsername = usernameUtils.getUsername as jest.Mock;
    mockOnChangeUsername = jest.fn();
    mockOnCollapseChange = jest.fn();

    mockGetUsername.mockReturnValue('TestUser');
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  const renderSidebar = (
    initialRoute: string = ROUTES.HOME,
    props?: Partial<React.ComponentProps<typeof Sidebar>>
  ) => {
    return render(
      <MemoryRouter initialEntries={[initialRoute]}>
        <Sidebar
          onChangeUsername={mockOnChangeUsername}
          onCollapseChange={mockOnCollapseChange}
          {...props}
        />
      </MemoryRouter>
    );
  };

  describe('Basic Rendering', () => {
    it('renders sidebar correctly', () => {
      renderSidebar();

      expect(screen.getByText('汉字游戏')).toBeInTheDocument();
      expect(screen.getByText('Home')).toBeInTheDocument();
      expect(screen.getByText('Idiom Game')).toBeInTheDocument();
      expect(screen.getByText('Sentence Game')).toBeInTheDocument();
      expect(screen.getByText('Leaderboard')).toBeInTheDocument();
      expect(screen.getByText('Statistics')).toBeInTheDocument();
    });

    it('renders brand logo link', () => {
      renderSidebar();

      const brandLink = screen.getByRole('link', { name: /汉字游戏/i });
      expect(brandLink).toBeInTheDocument();
      expect(brandLink).toHaveAttribute('href', ROUTES.HOME);
    });

    it('renders all navigation items with icons', () => {
      renderSidebar();

      expect(screen.getByText('🏠')).toBeInTheDocument();
      expect(screen.getByText('🎯')).toBeInTheDocument();
      expect(screen.getByText('📝')).toBeInTheDocument();
      expect(screen.getByText('🏆')).toBeInTheDocument();
      expect(screen.getByText('📊')).toBeInTheDocument();
    });

    it('renders footer copyright', () => {
      renderSidebar();

      expect(screen.getByText('© 2025 汉字游戏')).toBeInTheDocument();
    });
  });

  describe('Username Display', () => {
    it('displays current username when available', () => {
      mockGetUsername.mockReturnValue('TestUser');

      renderSidebar();

      expect(screen.getByText('当前用户:')).toBeInTheDocument();
      expect(screen.getByText('TestUser')).toBeInTheDocument();
    });

    it('displays Guest when no username is available', () => {
      mockGetUsername.mockReturnValue(null);

      renderSidebar();

      expect(screen.getByText('Guest')).toBeInTheDocument();
    });

    it('displays Guest when username is empty string', () => {
      mockGetUsername.mockReturnValue('');

      renderSidebar();

      expect(screen.getByText('Guest')).toBeInTheDocument();
    });

    it('renders change username button when callback is provided', () => {
      renderSidebar();

      const changeButton = screen.getByRole('button', { name: '更改用户名' });
      expect(changeButton).toBeInTheDocument();
    });

    it('does not render change username button when callback is not provided', () => {
      render(
        <MemoryRouter>
          <Sidebar />
        </MemoryRouter>
      );

      expect(screen.queryByRole('button', { name: '更改用户名' })).not.toBeInTheDocument();
    });

    it('calls onChangeUsername when change button is clicked', async () => {
      const user = userEvent.setup();
      renderSidebar();

      const changeButton = screen.getByRole('button', { name: '更改用户名' });
      await user.click(changeButton);

      expect(mockOnChangeUsername).toHaveBeenCalledTimes(1);
    });
  });

  describe('Navigation Links', () => {
    it('renders all navigation links', () => {
      renderSidebar();

      const homeLink = screen.getByRole('link', { name: /Home/i });
      const idiomLink = screen.getByRole('link', { name: /Idiom Game/i });
      const sentenceLink = screen.getByRole('link', { name: /Sentence Game/i });
      const leaderboardLink = screen.getByRole('link', { name: /Leaderboard/i });
      const statisticsLink = screen.getByRole('link', { name: /Statistics/i });

      expect(homeLink).toHaveAttribute('href', ROUTES.HOME);
      expect(idiomLink).toHaveAttribute('href', ROUTES.IDIOM_GAME);
      expect(sentenceLink).toHaveAttribute('href', ROUTES.SENTENCE_GAME);
      expect(leaderboardLink).toHaveAttribute('href', ROUTES.LEADERBOARD);
      expect(statisticsLink).toHaveAttribute('href', ROUTES.STATISTICS);
    });

    it('highlights active link on home route', () => {
      renderSidebar(ROUTES.HOME);

      const homeLink = screen.getByRole('link', { name: /Home/i });
      expect(homeLink).toHaveClass('text-primary', 'fw-semibold');
    });

    it('highlights active link on idiom game route', () => {
      renderSidebar(ROUTES.IDIOM_GAME);

      const idiomLink = screen.getByRole('link', { name: /Idiom Game/i });
      expect(idiomLink).toHaveClass('text-primary', 'fw-semibold');
    });

    it('highlights active link on sentence game route', () => {
      renderSidebar(ROUTES.SENTENCE_GAME);

      const sentenceLink = screen.getByRole('link', { name: /Sentence Game/i });
      expect(sentenceLink).toHaveClass('text-primary', 'fw-semibold');
    });

    it('highlights active link on leaderboard route', () => {
      renderSidebar(ROUTES.LEADERBOARD);

      const leaderboardLink = screen.getByRole('link', { name: /Leaderboard/i });
      expect(leaderboardLink).toHaveClass('text-primary', 'fw-semibold');
    });

    it('highlights active link on statistics route', () => {
      renderSidebar(ROUTES.STATISTICS);

      const statisticsLink = screen.getByRole('link', { name: /Statistics/i });
      expect(statisticsLink).toHaveClass('text-primary', 'fw-semibold');
    });

    it('applies background color to active link', () => {
      renderSidebar(ROUTES.HOME);

      const homeLink = screen.getByRole('link', { name: /Home/i });
      expect(homeLink).toHaveStyle({ backgroundColor: '#dbeafe' });
    });

    it('applies transparent background to inactive links', () => {
      renderSidebar(ROUTES.HOME);

      const idiomLink = screen.getByRole('link', { name: /Idiom Game/i });
      expect(idiomLink).toHaveStyle({ backgroundColor: 'transparent' });
    });
  });

  describe('Mobile Toggle Functionality', () => {
    it('renders mobile toggle button', () => {
      renderSidebar();

      const mobileToggle = screen.getByRole('button', { name: 'Toggle sidebar' });
      expect(mobileToggle).toBeInTheDocument();
      expect(mobileToggle).toHaveClass('d-lg-none');
    });

    it('mobile toggle button shows menu icon when closed', () => {
      renderSidebar();

      const mobileToggle = screen.getByRole('button', { name: 'Toggle sidebar' });
      expect(mobileToggle).toHaveTextContent('☰');
    });

    it('mobile toggle button shows close icon when open', async () => {
      const user = userEvent.setup();
      renderSidebar();

      const mobileToggle = screen.getByRole('button', { name: 'Toggle sidebar' });
      await user.click(mobileToggle);

      expect(mobileToggle).toHaveTextContent('✕');
    });

    it('opens mobile sidebar when toggle is clicked', async () => {
      const user = userEvent.setup();
      const { container } = renderSidebar();

      // Find the main sidebar container
      const sidebar = container.querySelector('div.position-fixed[style*="width"]');
      expect(sidebar).toHaveClass('d-none', 'd-lg-flex');

      const mobileToggle = screen.getByRole('button', { name: 'Toggle sidebar' });
      await user.click(mobileToggle);

      // After click, sidebar should be visible (not have d-none class)
      expect(sidebar).not.toHaveClass('d-none');
    });

    it('shows overlay when mobile sidebar is open', async () => {
      const user = userEvent.setup();
      const { container } = renderSidebar();

      const mobileToggle = screen.getByRole('button', { name: 'Toggle sidebar' });
      await user.click(mobileToggle);

      // Find overlay by checking for position-fixed div with backgroundColor
      const overlay = Array.from(container.querySelectorAll('.position-fixed.d-lg-none')).find(el => {
        const style = (el as HTMLElement).style;
        return style.backgroundColor && style.backgroundColor.includes('rgba');
      });
      expect(overlay).toBeDefined();
    });

    it('closes mobile sidebar when overlay is clicked', async () => {
      const user = userEvent.setup();
      const { container } = renderSidebar();

      // Open mobile sidebar
      const mobileToggle = screen.getByRole('button', { name: 'Toggle sidebar' });
      await user.click(mobileToggle);

      // Click overlay
      const overlay = Array.from(container.querySelectorAll('.position-fixed.d-lg-none')).find(el => {
        const style = (el as HTMLElement).style;
        return style.backgroundColor && style.backgroundColor.includes('rgba');
      });
      if (overlay) {
        await user.click(overlay as HTMLElement);
      }

      // Sidebar should be closed
      const sidebar = container.querySelector('div.position-fixed[style*="width"]');
      expect(sidebar).toHaveClass('d-none', 'd-lg-flex');
    });

    it('closes mobile sidebar when navigation link is clicked', async () => {
      const user = userEvent.setup();
      const { container } = renderSidebar();

      // Open mobile sidebar
      const mobileToggle = screen.getByRole('button', { name: 'Toggle sidebar' });
      await user.click(mobileToggle);

      // Click a navigation link
      const homeLink = screen.getByRole('link', { name: /Home/i });
      await user.click(homeLink);

      // Sidebar should be closed
      const sidebar = container.querySelector('div.position-fixed[style*="width"]');
      expect(sidebar).toHaveClass('d-none', 'd-lg-flex');
    });

    it('closes mobile sidebar when brand link is clicked', async () => {
      const user = userEvent.setup();
      const { container } = renderSidebar(ROUTES.STATISTICS);

      // Open mobile sidebar
      const mobileToggle = screen.getByRole('button', { name: 'Toggle sidebar' });
      await user.click(mobileToggle);

      // Click brand link
      const brandLink = screen.getByRole('link', { name: /汉字游戏/i });
      await user.click(brandLink);

      // Sidebar should be closed
      const sidebar = container.querySelector('div.position-fixed[style*="width"]');
      expect(sidebar).toHaveClass('d-none', 'd-lg-flex');
    });
  });

  describe('Desktop Collapse Functionality', () => {
    it('renders desktop collapse toggle button', () => {
      renderSidebar();

      const desktopToggle = screen.getByRole('button', { name: 'Toggle sidebar collapse' });
      expect(desktopToggle).toBeInTheDocument();
      expect(desktopToggle).toHaveClass('d-none', 'd-lg-block');
    });

    it('desktop toggle shows collapse icon when expanded', () => {
      renderSidebar();

      const desktopToggle = screen.getByRole('button', { name: 'Toggle sidebar collapse' });
      expect(desktopToggle).toHaveTextContent('◀');
    });

    it('desktop toggle shows expand icon when collapsed', async () => {
      const user = userEvent.setup();
      renderSidebar();

      const desktopToggle = screen.getByRole('button', { name: 'Toggle sidebar collapse' });
      await user.click(desktopToggle);

      expect(desktopToggle).toHaveTextContent('☰');
    });

    it('collapses sidebar when desktop toggle is clicked', async () => {
      const user = userEvent.setup();
      const { container } = renderSidebar();

      const desktopToggle = screen.getByRole('button', { name: 'Toggle sidebar collapse' });
      await user.click(desktopToggle);

      const sidebar = container.querySelector('.position-fixed[style*="width: 60px"]');
      expect(sidebar).toBeInTheDocument();
    });

    it('expands sidebar when desktop toggle is clicked again', async () => {
      const user = userEvent.setup();
      const { container } = renderSidebar();

      const desktopToggle = screen.getByRole('button', { name: 'Toggle sidebar collapse' });

      // Collapse
      await user.click(desktopToggle);
      let sidebar = container.querySelector('.position-fixed[style*="width: 60px"]');
      expect(sidebar).toBeInTheDocument();

      // Expand
      await user.click(desktopToggle);
      sidebar = container.querySelector('.position-fixed[style*="width: 250px"]');
      expect(sidebar).toBeInTheDocument();
    });

    it('calls onCollapseChange with true when collapsing', async () => {
      const user = userEvent.setup();
      renderSidebar();

      const desktopToggle = screen.getByRole('button', { name: 'Toggle sidebar collapse' });
      await user.click(desktopToggle);

      expect(mockOnCollapseChange).toHaveBeenCalledWith(true);
    });

    it('calls onCollapseChange with false when expanding', async () => {
      const user = userEvent.setup();
      renderSidebar();

      const desktopToggle = screen.getByRole('button', { name: 'Toggle sidebar collapse' });

      // Collapse first
      await user.click(desktopToggle);
      expect(mockOnCollapseChange).toHaveBeenCalledWith(true);

      // Then expand
      await user.click(desktopToggle);
      expect(mockOnCollapseChange).toHaveBeenCalledWith(false);
    });

    it('does not call onCollapseChange when callback is not provided', async () => {
      const user = userEvent.setup();
      render(
        <MemoryRouter>
          <Sidebar onChangeUsername={mockOnChangeUsername} />
        </MemoryRouter>
      );

      const desktopToggle = screen.getByRole('button', { name: 'Toggle sidebar collapse' });
      await user.click(desktopToggle);

      // Should not throw error
      expect(mockOnCollapseChange).not.toHaveBeenCalled();
    });

    it('adjusts desktop toggle button position when collapsed', async () => {
      const user = userEvent.setup();
      renderSidebar();

      const desktopToggle = screen.getByRole('button', { name: 'Toggle sidebar collapse' });

      // Initially at expanded position
      expect(desktopToggle).toHaveStyle({ left: '200px' });

      // After collapse
      await user.click(desktopToggle);
      expect(desktopToggle).toHaveStyle({ left: '0.5rem' });
    });
  });

  describe('Collapsed State Rendering', () => {
    it('hides text labels when collapsed', async () => {
      const user = userEvent.setup();
      renderSidebar();

      const desktopToggle = screen.getByRole('button', { name: 'Toggle sidebar collapse' });
      await user.click(desktopToggle);

      await waitFor(() => {
        expect(screen.queryByText('汉字游戏')).not.toBeInTheDocument();
        expect(screen.queryByText('Home')).not.toBeInTheDocument();
        expect(screen.queryByText('Idiom Game')).not.toBeInTheDocument();
      });
    });

    it('shows icons when collapsed', async () => {
      const user = userEvent.setup();
      renderSidebar();

      const desktopToggle = screen.getByRole('button', { name: 'Toggle sidebar collapse' });
      await user.click(desktopToggle);

      expect(screen.getByText('🀄')).toBeInTheDocument();
      expect(screen.getByText('🏠')).toBeInTheDocument();
      expect(screen.getByText('🎯')).toBeInTheDocument();
    });

    it('adds title attribute to navigation links when collapsed', async () => {
      const user = userEvent.setup();
      renderSidebar();

      const desktopToggle = screen.getByRole('button', { name: 'Toggle sidebar collapse' });
      await user.click(desktopToggle);

      await waitFor(() => {
        const links = screen.getAllByRole('link');
        const homeLink = links.find(link => link.getAttribute('title') === 'Home');
        expect(homeLink).toBeDefined();
      });
    });

    it('shows user icon button when collapsed', async () => {
      const user = userEvent.setup();
      renderSidebar();

      const desktopToggle = screen.getByRole('button', { name: 'Toggle sidebar collapse' });
      await user.click(desktopToggle);

      const userButton = await screen.findByTitle('用户设置');
      expect(userButton).toBeInTheDocument();
      expect(userButton).toHaveTextContent('👤');
    });

    it('hides username text when collapsed', async () => {
      const user = userEvent.setup();
      renderSidebar();

      const desktopToggle = screen.getByRole('button', { name: 'Toggle sidebar collapse' });
      await user.click(desktopToggle);

      await waitFor(() => {
        expect(screen.queryByText('当前用户:')).not.toBeInTheDocument();
        expect(screen.queryByText('TestUser')).not.toBeInTheDocument();
      });
    });

    it('shows abbreviated copyright when collapsed', async () => {
      const user = userEvent.setup();
      renderSidebar();

      const desktopToggle = screen.getByRole('button', { name: 'Toggle sidebar collapse' });
      await user.click(desktopToggle);

      await waitFor(() => {
        expect(screen.queryByText('© 2025 汉字游戏')).not.toBeInTheDocument();
        expect(screen.getByText('©')).toBeInTheDocument();
      });
    });

    it('centers content when collapsed', async () => {
      const user = userEvent.setup();
      renderSidebar();

      const desktopToggle = screen.getByRole('button', { name: 'Toggle sidebar collapse' });
      await user.click(desktopToggle);

      await waitFor(() => {
        const brandLink = screen.getByRole('link', { name: /🀄/i });
        expect(brandLink).toHaveStyle({ justifyContent: 'center' });
      });
    });

    it('calls onChangeUsername when user icon is clicked in collapsed state', async () => {
      const user = userEvent.setup();
      renderSidebar();

      const desktopToggle = screen.getByRole('button', { name: 'Toggle sidebar collapse' });
      await user.click(desktopToggle);

      const userButton = await screen.findByTitle('用户设置');
      await user.click(userButton);

      expect(mockOnChangeUsername).toHaveBeenCalled();
    });
  });

  describe('Sidebar Styling', () => {
    it('applies correct width when expanded', () => {
      const { container } = renderSidebar();

      const sidebar = container.querySelector('.position-fixed[style*="width: 250px"]');
      expect(sidebar).toBeInTheDocument();
    });

    it('applies correct width when collapsed', async () => {
      const user = userEvent.setup();
      const { container } = renderSidebar();

      const desktopToggle = screen.getByRole('button', { name: 'Toggle sidebar collapse' });
      await user.click(desktopToggle);

      const sidebar = container.querySelector('.position-fixed[style*="width: 60px"]');
      expect(sidebar).toBeInTheDocument();
    });

    it('applies transition effect', () => {
      const { container } = renderSidebar();

      const sidebar = container.querySelector('.position-fixed[style*="transition: width 0.3s ease"]');
      expect(sidebar).toBeInTheDocument();
    });

    it('applies shadow and styling', () => {
      const { container } = renderSidebar();

      // Check for the sidebar with position-fixed and shadow styling
      const sidebars = container.querySelectorAll('.position-fixed');
      const sidebarWithShadow = Array.from(sidebars).find(el => {
        const style = (el as HTMLElement).style;
        return style.boxShadow && style.boxShadow.includes('rgba');
      });
      expect(sidebarWithShadow).toBeDefined();
    });
  });

  describe('Accessibility', () => {
    it('has aria-label on mobile toggle button', () => {
      renderSidebar();

      const mobileToggle = screen.getByRole('button', { name: 'Toggle sidebar' });
      expect(mobileToggle).toHaveAttribute('aria-label', 'Toggle sidebar');
    });

    it('has aria-label on desktop toggle button', () => {
      renderSidebar();

      const desktopToggle = screen.getByRole('button', { name: 'Toggle sidebar collapse' });
      expect(desktopToggle).toHaveAttribute('aria-label', 'Toggle sidebar collapse');
    });

    it('has title attribute on desktop toggle button', () => {
      renderSidebar();

      const desktopToggle = screen.getByRole('button', { name: 'Toggle sidebar collapse' });
      expect(desktopToggle).toHaveAttribute('title', 'Collapse sidebar');
    });

    it('updates title attribute when collapsed', async () => {
      const user = userEvent.setup();
      renderSidebar();

      const desktopToggle = screen.getByRole('button', { name: 'Toggle sidebar collapse' });
      await user.click(desktopToggle);

      expect(desktopToggle).toHaveAttribute('title', 'Expand sidebar');
    });

    it('all navigation links are keyboard accessible', () => {
      renderSidebar();

      const links = screen.getAllByRole('link');
      links.forEach(link => {
        expect(link).toBeInTheDocument();
      });
    });
  });

  describe('Props Handling', () => {
    it('works without optional props', () => {
      render(
        <MemoryRouter>
          <Sidebar />
        </MemoryRouter>
      );

      expect(screen.getByText('汉字游戏')).toBeInTheDocument();
    });

    it('handles onChangeUsername prop correctly', async () => {
      const user = userEvent.setup();
      const customCallback = jest.fn();

      render(
        <MemoryRouter>
          <Sidebar onChangeUsername={customCallback} />
        </MemoryRouter>
      );

      const changeButton = screen.getByRole('button', { name: '更改用户名' });
      await user.click(changeButton);

      expect(customCallback).toHaveBeenCalledTimes(1);
    });

    it('handles onCollapseChange prop correctly', async () => {
      const user = userEvent.setup();
      const customCallback = jest.fn();

      render(
        <MemoryRouter>
          <Sidebar onCollapseChange={customCallback} />
        </MemoryRouter>
      );

      const desktopToggle = screen.getByRole('button', { name: 'Toggle sidebar collapse' });
      await user.click(desktopToggle);

      expect(customCallback).toHaveBeenCalledWith(true);
    });
  });
});
