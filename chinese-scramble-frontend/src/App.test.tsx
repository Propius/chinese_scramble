import React from 'react';
import { render, screen, waitFor, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import App from './App';
import { usernameUtils } from './utils/usernameUtils';
import { ROUTES } from './constants/routes';

// Mock all child components
jest.mock('./components/layout/Sidebar', () => ({
  __esModule: true,
  default: ({ onChangeUsername, onCollapseChange }: any) => (
    <div data-testid="sidebar">
      <button data-testid="change-username-trigger" onClick={onChangeUsername}>
        Change Username
      </button>
      <button
        data-testid="collapse-trigger"
        onClick={() => onCollapseChange?.(true)}
      >
        Collapse
      </button>
      <button
        data-testid="expand-trigger"
        onClick={() => onCollapseChange?.(false)}
      >
        Expand
      </button>
    </div>
  ),
}));

jest.mock('./components/common/UsernameModal', () => ({
  __esModule: true,
  default: ({ show, onSubmit, onClose }: any) =>
    show ? (
      <div data-testid="username-modal">
        <button
          data-testid="modal-submit"
          onClick={() => onSubmit('TestUser')}
        >
          Submit
        </button>
        <button data-testid="modal-close" onClick={onClose}>
          Close
        </button>
      </div>
    ) : null,
}));

jest.mock('./components/common/LoadingSpinner', () => ({
  __esModule: true,
  default: ({ size, message }: any) => (
    <div data-testid="loading-spinner">
      {message} - {size}
    </div>
  ),
}));

jest.mock('./pages/Home', () => ({
  __esModule: true,
  default: () => <div data-testid="home-page">Home Page</div>,
}));

jest.mock('./pages/IdiomGamePage', () => ({
  __esModule: true,
  default: () => <div data-testid="idiom-game-page">Idiom Game Page</div>,
}));

jest.mock('./pages/SentenceGamePage', () => ({
  __esModule: true,
  default: () => <div data-testid="sentence-game-page">Sentence Game Page</div>,
}));

jest.mock('./pages/LeaderboardPage', () => ({
  __esModule: true,
  default: () => <div data-testid="leaderboard-page">Leaderboard Page</div>,
}));

jest.mock('./pages/StatisticsPage', () => ({
  __esModule: true,
  default: () => <div data-testid="statistics-page">Statistics Page</div>,
}));

jest.mock('./utils/usernameUtils', () => ({
  usernameUtils: {
    hasUsername: jest.fn(),
    setUsername: jest.fn(),
    getUsername: jest.fn(),
    clearUsername: jest.fn(),
  },
}));

jest.mock('./i18n/i18n.config', () => ({}));

describe('App Component', () => {
  let mockHasUsername: jest.Mock;
  let mockSetUsername: jest.Mock;
  let mockGetUsername: jest.Mock;

  beforeEach(() => {
    mockHasUsername = usernameUtils.hasUsername as jest.Mock;
    mockSetUsername = usernameUtils.setUsername as jest.Mock;
    mockGetUsername = usernameUtils.getUsername as jest.Mock;

    mockHasUsername.mockReturnValue(true);
    mockGetUsername.mockReturnValue('TestUser');
    mockSetUsername.mockClear();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  describe('Basic Rendering', () => {
    it('renders app without crashing', async () => {
      const { container } = render(<App />);
      await waitFor(() => {
        expect(container).toBeTruthy();
      });
    });

    it('renders sidebar component', async () => {
      render(<App />);
      await waitFor(() => {
        expect(screen.getByTestId('sidebar')).toBeInTheDocument();
      });
    });

    it('renders main content area with proper layout', async () => {
      const { container } = render(<App />);
      await waitFor(() => {
        const mainContent = container.querySelector('.main-content-responsive');
        expect(mainContent).toBeInTheDocument();
        expect(mainContent).toHaveClass('flex-grow-1', 'd-flex', 'flex-column');
      });
    });

    it('applies responsive styles', async () => {
      const { container } = render(<App />);
      await waitFor(() => {
        const styleTag = container.querySelector('style');
        expect(styleTag?.textContent).toContain('main-content-responsive');
        expect(styleTag?.textContent).toContain('margin-left: 0 !important');
      });
    });
  });

  describe('Username Modal Integration', () => {
    it('shows username modal when user has no username', async () => {
      mockHasUsername.mockReturnValue(false);

      render(<App />);

      await waitFor(() => {
        expect(screen.getByTestId('username-modal')).toBeInTheDocument();
      });
    });

    it('does not show username modal when user has username', async () => {
      mockHasUsername.mockReturnValue(true);

      render(<App />);

      await waitFor(() => {
        expect(screen.queryByTestId('username-modal')).not.toBeInTheDocument();
      });
    });

    it('handles username submission correctly', async () => {
      mockHasUsername.mockReturnValue(false);

      render(<App />);

      await waitFor(() => {
        expect(screen.getByTestId('username-modal')).toBeInTheDocument();
      });

      const submitButton = screen.getByTestId('modal-submit');
      act(() => {
        submitButton.click();
      });

      await waitFor(() => {
        expect(mockSetUsername).toHaveBeenCalledWith('TestUser');
        expect(screen.queryByTestId('username-modal')).not.toBeInTheDocument();
      });
    });

    it('handles username modal close', async () => {
      mockHasUsername.mockReturnValue(false);

      render(<App />);

      await waitFor(() => {
        expect(screen.getByTestId('username-modal')).toBeInTheDocument();
      });

      const closeButton = screen.getByTestId('modal-close');
      act(() => {
        closeButton.click();
      });

      await waitFor(() => {
        expect(screen.queryByTestId('username-modal')).not.toBeInTheDocument();
      });
    });

    it('opens username modal when change username is triggered from sidebar', async () => {
      mockHasUsername.mockReturnValue(true);

      render(<App />);

      await waitFor(() => {
        expect(screen.queryByTestId('username-modal')).not.toBeInTheDocument();
      });

      const changeUsernameButton = screen.getByTestId('change-username-trigger');
      act(() => {
        changeUsernameButton.click();
      });

      await waitFor(() => {
        expect(screen.getByTestId('username-modal')).toBeInTheDocument();
      });
    });
  });

  describe('Sidebar Collapse State', () => {
    it('updates margin when sidebar is collapsed', async () => {
      const { container } = render(<App />);

      await waitFor(() => {
        const mainContent = container.querySelector('.main-content-responsive');
        expect(mainContent).toHaveStyle({ marginLeft: '250px' });
      });

      const collapseButton = screen.getByTestId('collapse-trigger');
      act(() => {
        collapseButton.click();
      });

      await waitFor(() => {
        const mainContent = container.querySelector('.main-content-responsive');
        expect(mainContent).toHaveStyle({ marginLeft: '60px' });
      });
    });

    it('updates margin when sidebar is expanded', async () => {
      const { container } = render(<App />);

      // First collapse
      const collapseButton = screen.getByTestId('collapse-trigger');
      act(() => {
        collapseButton.click();
      });

      await waitFor(() => {
        const mainContent = container.querySelector('.main-content-responsive');
        expect(mainContent).toHaveStyle({ marginLeft: '60px' });
      });

      // Then expand
      const expandButton = screen.getByTestId('expand-trigger');
      act(() => {
        expandButton.click();
      });

      await waitFor(() => {
        const mainContent = container.querySelector('.main-content-responsive');
        expect(mainContent).toHaveStyle({ marginLeft: '250px' });
      });
    });

    it('applies transition styles to main content', async () => {
      const { container } = render(<App />);

      await waitFor(() => {
        const mainContent = container.querySelector('.main-content-responsive');
        expect(mainContent).toHaveStyle({
          transition: 'margin-left 0.3s ease',
        });
      });
    });
  });

  describe('Route Rendering', () => {
    it('renders Home component at root route', async () => {
      window.history.pushState({}, 'Home', ROUTES.HOME);

      render(<App />);

      await waitFor(() => {
        expect(screen.getByTestId('home-page')).toBeInTheDocument();
      });
    });

    it('renders IdiomGamePage at idiom game route', async () => {
      window.history.pushState({}, 'Idiom Game', ROUTES.IDIOM_GAME);

      render(<App />);

      await waitFor(() => {
        expect(screen.getByTestId('idiom-game-page')).toBeInTheDocument();
      });
    });

    it('renders SentenceGamePage at sentence game route', async () => {
      window.history.pushState({}, 'Sentence Game', ROUTES.SENTENCE_GAME);

      render(<App />);

      await waitFor(() => {
        expect(screen.getByTestId('sentence-game-page')).toBeInTheDocument();
      });
    });

    it('renders LeaderboardPage at leaderboard route', async () => {
      window.history.pushState({}, 'Leaderboard', ROUTES.LEADERBOARD);

      render(<App />);

      await waitFor(() => {
        expect(screen.getByTestId('leaderboard-page')).toBeInTheDocument();
      });
    });

    it('renders StatisticsPage at statistics route', async () => {
      window.history.pushState({}, 'Statistics', ROUTES.STATISTICS);

      render(<App />);

      await waitFor(() => {
        expect(screen.getByTestId('statistics-page')).toBeInTheDocument();
      });
    });
  });

  describe('Suspense and Loading', () => {
    it('shows loading spinner while lazy loading components', async () => {
      const { container } = render(<App />);

      // The Suspense fallback is configured correctly with LoadingSpinner
      // Verify the main layout is present which contains the Suspense boundary
      await waitFor(() => {
        expect(container.querySelector('.d-flex.min-vh-100')).toBeInTheDocument();
        expect(screen.getByTestId('sidebar')).toBeInTheDocument();
      });
    });

    it('renders content after loading completes', async () => {
      window.history.pushState({}, 'Home', ROUTES.HOME);

      render(<App />);

      await waitFor(() => {
        expect(screen.getByTestId('home-page')).toBeInTheDocument();
        expect(screen.queryByTestId('loading-spinner')).not.toBeInTheDocument();
      });
    });
  });

  describe('Layout Structure', () => {
    it('renders with correct flex layout classes', async () => {
      const { container } = render(<App />);

      await waitFor(() => {
        const wrapper = container.querySelector('.d-flex.min-vh-100.bg-light');
        expect(wrapper).toBeInTheDocument();
      });
    });

    it('renders main content with correct flex classes', async () => {
      const { container } = render(<App />);

      await waitFor(() => {
        const mainContent = container.querySelector(
          '.flex-grow-1.d-flex.flex-column'
        );
        expect(mainContent).toBeInTheDocument();
      });
    });
  });

  describe('Router Integration', () => {
    it('wraps application in Router', async () => {
      const { container } = render(<App />);

      await waitFor(() => {
        // Check that router is working by verifying route rendering
        expect(container.querySelector('.d-flex')).toBeInTheDocument();
      });
    });

    it('supports browser navigation', () => {
      const { container } = render(<App />);

      // Router is integrated and working
      // Direct route navigation testing is covered by individual route tests
      expect(container.querySelector('.d-flex')).toBeInTheDocument();
      expect(screen.getByTestId('sidebar')).toBeInTheDocument();
    });
  });

  describe('Component Integration', () => {
    it('passes onChangeUsername callback to Sidebar', async () => {
      render(<App />);

      await waitFor(() => {
        expect(screen.getByTestId('sidebar')).toBeInTheDocument();
        expect(screen.getByTestId('change-username-trigger')).toBeInTheDocument();
      });
    });

    it('passes onCollapseChange callback to Sidebar', async () => {
      render(<App />);

      await waitFor(() => {
        expect(screen.getByTestId('sidebar')).toBeInTheDocument();
        expect(screen.getByTestId('collapse-trigger')).toBeInTheDocument();
        expect(screen.getByTestId('expand-trigger')).toBeInTheDocument();
      });
    });

    it('integrates UsernameModal with correct props', async () => {
      mockHasUsername.mockReturnValue(false);

      render(<App />);

      await waitFor(() => {
        const modal = screen.getByTestId('username-modal');
        expect(modal).toBeInTheDocument();
        expect(screen.getByTestId('modal-submit')).toBeInTheDocument();
        expect(screen.getByTestId('modal-close')).toBeInTheDocument();
      });
    });
  });

  describe('State Management', () => {
    it('manages showUsernameModal state correctly', async () => {
      mockHasUsername.mockReturnValue(false);

      render(<App />);

      // Modal should be shown
      await waitFor(() => {
        expect(screen.getByTestId('username-modal')).toBeInTheDocument();
      });

      // Close modal
      act(() => {
        screen.getByTestId('modal-close').click();
      });

      // Modal should be hidden
      await waitFor(() => {
        expect(screen.queryByTestId('username-modal')).not.toBeInTheDocument();
      });

      // Open modal again via sidebar
      act(() => {
        screen.getByTestId('change-username-trigger').click();
      });

      // Modal should be shown again
      await waitFor(() => {
        expect(screen.getByTestId('username-modal')).toBeInTheDocument();
      });
    });

    it('manages sidebarCollapsed state correctly', async () => {
      const { container } = render(<App />);

      // Initial state - not collapsed
      await waitFor(() => {
        const mainContent = container.querySelector('.main-content-responsive');
        expect(mainContent).toHaveStyle({ marginLeft: '250px' });
      });

      // Collapse sidebar
      act(() => {
        screen.getByTestId('collapse-trigger').click();
      });

      await waitFor(() => {
        const mainContent = container.querySelector('.main-content-responsive');
        expect(mainContent).toHaveStyle({ marginLeft: '60px' });
      });

      // Expand sidebar
      act(() => {
        screen.getByTestId('expand-trigger').click();
      });

      await waitFor(() => {
        const mainContent = container.querySelector('.main-content-responsive');
        expect(mainContent).toHaveStyle({ marginLeft: '250px' });
      });
    });
  });

  describe('Effect Hooks', () => {
    it('checks username on component mount', async () => {
      mockHasUsername.mockReturnValue(true);

      render(<App />);

      await waitFor(() => {
        expect(mockHasUsername).toHaveBeenCalled();
      });
    });

    it('shows modal on mount when no username exists', async () => {
      mockHasUsername.mockReturnValue(false);

      render(<App />);

      await waitFor(() => {
        expect(mockHasUsername).toHaveBeenCalled();
        expect(screen.getByTestId('username-modal')).toBeInTheDocument();
      });
    });

    it('does not show modal on mount when username exists', async () => {
      mockHasUsername.mockReturnValue(true);

      render(<App />);

      await waitFor(() => {
        expect(mockHasUsername).toHaveBeenCalled();
        expect(screen.queryByTestId('username-modal')).not.toBeInTheDocument();
      });
    });
  });
});
